/* $Id$
 * 
 * Copyright 2007-2008 Steven Osborn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openintents.safe;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * PassEdit Activity
 * 
 * @author Steven Osborn - http://steven.bitsetters.com
 * @author Randy McEoin
 */
public class PassEdit extends Activity {

	private static boolean debug = false;
	private static String TAG = "PassEdit";

	public static final int REQUEST_GEN_PASS = 10;

	public static final int SAVE_PASSWORD_INDEX = Menu.FIRST;
	public static final int DEL_PASSWORD_INDEX = Menu.FIRST + 1;
	public static final int DISCARD_PASSWORD_INDEX = Menu.FIRST + 2;
	public static final int GEN_PASSWORD_INDEX = Menu.FIRST + 3;

	private EditText descriptionText;
	private EditText passwordText;
	private EditText usernameText;
	private EditText websiteText;
	private EditText noteText;
	private Long RowId;
	private DBHelper dbHelper = null;
	private CryptoHelper ch;
	private boolean pass_gen_ret = false;
	private boolean discardEntry = false;
	public static boolean entryEdited = false;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if (debug) Log.d(TAG,"onCreate()");
		String title = getResources().getString(R.string.app_name) + " - "
				+ getResources().getString(R.string.edit_entry);
		setTitle(title);

		ch = new CryptoHelper();
		try {
			ch.init(CryptoHelper.EncryptionMedium,PassList.getSalt());
			ch.setPassword(PassList.getMasterKey());
		} catch (CryptoHelperException e1) {
			e1.printStackTrace();
			Toast.makeText(this,getString(R.string.crypto_error)
				+ e1.getMessage(), Toast.LENGTH_SHORT).show();
		}

		if (dbHelper == null) {
			dbHelper = new DBHelper(this);
		}

		setContentView(R.layout.pass_edit);

		descriptionText = (EditText) findViewById(R.id.description);
		passwordText = (EditText) findViewById(R.id.password);
		usernameText = (EditText) findViewById(R.id.username);
		noteText = (EditText) findViewById(R.id.note);
		websiteText = (EditText) findViewById(R.id.website);

		Button goButton = (Button) findViewById(R.id.go);

		RowId = icicle != null ? icicle.getLong(PassList.KEY_ID) : null;
		if (RowId == null) {
			Bundle extras = getIntent().getExtras();
			RowId = extras != null ? extras.getLong(PassList.KEY_ID) : null;
		}

		entryEdited = false;

		goButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {

				Toast.makeText(PassEdit.this, R.string.copy_to_clipboard,
						Toast.LENGTH_SHORT).show();

				ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				cb.setText(passwordText.getText().toString());

				Intent i = new Intent(Intent.ACTION_VIEW);
				String link = websiteText.getText().toString();
				Uri u = Uri.parse(link);
				i.setData(u);
				try {
					startActivity(i);
				} catch (ActivityNotFoundException e) {
					// Let's try to catch the most common mistake: omitting http:
					u = Uri.parse("http://" + link);
					i.setData(u);
					try {
						startActivity(i);
					} catch (ActivityNotFoundException e2) {
						Toast.makeText(PassEdit.this, R.string.invalid_website,
							Toast.LENGTH_SHORT).show();
					}
				}
			}
		});

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (RowId != null) {
			outState.putLong(PassList.KEY_ID, RowId);
		} else {
			outState.putLong(PassList.KEY_ID, -1);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (discardEntry==false) {
			savePassword();
		}
		dbHelper.close();
		dbHelper = null;
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (debug) Log.d(TAG,"onResume()");

		if (dbHelper == null) {
			dbHelper = new DBHelper(this);
		}
		if (CategoryList.isSignedIn() == false) {
			saveState();
			finish();
		}
		populateFields();
	}

	private void saveState() {
		PassEntry entry = new PassEntry();

		String passwordPlain = passwordText.getText().toString();
		String notePlain = noteText.getText().toString();
		String usernamePlain = usernameText.getText().toString();
		String websitePlain = websiteText.getText().toString();
		String descPlain = descriptionText.getText().toString();

		try {
			entry.category = PassList.getCategoryId();
			entry.description = ch.encrypt(descPlain);
			entry.username = ch.encrypt(usernamePlain);
			entry.password = ch.encrypt(passwordPlain);
			entry.note = ch.encrypt(notePlain);
			entry.website = ch.encrypt(websitePlain);
		} catch (CryptoHelperException e) {
			Log.e(TAG, e.toString());
		}

		entryEdited = true;

		if (RowId == null || RowId == -1) {
			entry.id = 0;	// brand new entry
			RowId = dbHelper.addPassword(entry);
		} else {
			PassEntry storedEntry = dbHelper.fetchPassword (RowId);
			//update fields that aren't set in the UI:
			entry.uniqueName = storedEntry.uniqueName;
			dbHelper.updatePassword(RowId, entry);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, SAVE_PASSWORD_INDEX, 0, R.string.save).setIcon(
				android.R.drawable.ic_menu_save).setShortcut('1', 's');
		menu.add(0, DEL_PASSWORD_INDEX, 0, R.string.password_delete).setIcon(
				android.R.drawable.ic_menu_delete);
		menu.add(0, DISCARD_PASSWORD_INDEX, 0, R.string.discard_changes).setIcon(
				android.R.drawable.ic_notification_clear_all);
		menu.add(0, GEN_PASSWORD_INDEX, 0, "Generate").setIcon(
				android.R.drawable.ic_menu_set_as).setShortcut('4', 'g');

		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * Save the password entry and finish the activity.
	 */
	private void savePassword() {
		saveState();
		Toast.makeText(PassEdit.this, R.string.entry_saved,
			Toast.LENGTH_SHORT).show();
		setResult(RESULT_OK);
	}

	/**
	 * Prompt the user with a dialog asking them if they really want
	 * to delete the password.
	 */
	public void deletePassword(){
		Dialog about = new AlertDialog.Builder(this)
			.setIcon(R.drawable.passicon)
			.setTitle(R.string.dialog_delete_password_title)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					deletePassword2();
				}
			})
			.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					// do nothing
				}
			}) 
			.setMessage(R.string.dialog_delete_password_msg)
			.create();
		about.show();
	}
	
	/**
	 * Follow up for the Delete Password dialog.  If we have a RowId then
	 * delete the password, otherwise just finish this Activity.
	 */
	public void deletePassword2(){
		if ((RowId != null) && (RowId > 0)) {
			delPassword(RowId);
		} else {
			// user specified to delete a new entry
			// so simply exit out
			finish();
		}
	}
	/**
	 * Delete the password entry from the database given the row id within the
	 * database.
	 * 
	 * @param Id
	 */
	private void delPassword(long Id) {
		dbHelper.deletePassword(Id);
		setResult(RESULT_OK);
		finish();
	}

	/**
	 * Handler for when a MenuItem is selected from the Activity.
	 */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case SAVE_PASSWORD_INDEX:
			savePassword();
			finish();
			break;
		case DEL_PASSWORD_INDEX:
			deletePassword();
			break;
		case DISCARD_PASSWORD_INDEX:
			discardEntry=true;
			finish();
			break;
		case GEN_PASSWORD_INDEX:
			Intent i = new Intent(getApplicationContext(), PassGen.class);
			startActivityForResult(i, REQUEST_GEN_PASS);
		}
		return super.onOptionsItemSelected(item);
	}
		
	/**
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent i) {
		super.onActivityResult(requestCode, resultCode, i);

		if (requestCode == REQUEST_GEN_PASS) {
			if(resultCode == PassGen.CHANGE_ENTRY_RESULT) {
				String new_pass = i.getStringExtra(PassGen.NEW_PASS_KEY);
				Log.d(TAG,new_pass);
				passwordText.setText(new_pass);
				pass_gen_ret = true;
			}
		}
	}

	/**
	 * 
	 */
	private void populateFields() {
		if (debug) Log.d(TAG,"populateFields()");
		if(pass_gen_ret == true){
			pass_gen_ret = false;
			return;
		}
		if (RowId != null) {
			PassEntry row = dbHelper.fetchPassword(RowId);
			if (row.id > -1) {
				String cryptDesc = row.description;
				String cryptWebsite = row.website;
				String cryptUsername = row.username;
				String cryptPass = row.password;
				String cryptNote = row.note;
				try {
					descriptionText.setText(ch.decrypt(cryptDesc));
					websiteText.setText(ch.decrypt(cryptWebsite));
					usernameText.setText(ch.decrypt(cryptUsername));
					passwordText.setText(ch.decrypt(cryptPass));
					noteText.setText(ch.decrypt(cryptNote));
				} catch (CryptoHelperException e) {
					Log.e(TAG, e.toString());
				}
			}
		}
	}
}
