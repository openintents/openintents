/* $Id$
 * 
 * Copyright (C) 2009 OpenIntents.org
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

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * PassView Activity
 * 
 * @author Randy McEoin
 */
public class PassView extends Activity {

	private static boolean debug = false;
	private static String TAG = "PassView";

	public static final int EDIT_PASSWORD_INDEX = Menu.FIRST;
	public static final int DEL_PASSWORD_INDEX = Menu.FIRST + 1;

	public static final int REQUEST_EDIT_PASS = 1;
	
	private EditText descriptionText;
	private EditText passwordText;
	private EditText usernameText;
	private EditText websiteText;
	private EditText noteText;
	private TextView lastEditedText;
	private TextView uniqueNameText;
	private TextView packageAccessText;
	private Long RowId;
	private Long CategoryId;
	private DBHelper dbHelper = null;
	private CryptoHelper ch;
	public static boolean entryEdited=false;

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if (debug) Log.d(TAG,"onCreate()");
		String title = getResources().getString(R.string.app_name) + " - "
				+ getResources().getString(R.string.view_entry);
		setTitle(title);

		ch = new CryptoHelper();
		try {
			ch.init(CryptoHelper.EncryptionMedium, PassList.getSalt());
			ch.setPassword(PassList.getMasterKey());
		} catch (CryptoHelperException e1) {
			e1.printStackTrace();
			Toast.makeText(this,getString(R.string.crypto_error)
				+ e1.getMessage(), Toast.LENGTH_SHORT).show();
		}

		if (dbHelper == null) {
			dbHelper = new DBHelper(this);
		}

		setContentView(R.layout.pass_view);

		descriptionText = (EditText) findViewById(R.id.description);
		websiteText = (EditText) findViewById(R.id.website);
		usernameText = (EditText) findViewById(R.id.username);
		passwordText = (EditText) findViewById(R.id.password);
		noteText = (EditText) findViewById(R.id.note);
		lastEditedText = (TextView) findViewById(R.id.last_edited);
		uniqueNameText = (TextView) findViewById(R.id.uniquename);
		packageAccessText = (TextView) findViewById(R.id.packageaccess);
		
		entryEdited=false;

		Button goButton = (Button) findViewById(R.id.go);

		RowId = icicle != null ? icicle.getLong(PassList.KEY_ID) : null;
		if (RowId == null) {
			Bundle extras = getIntent().getExtras();
			RowId = extras != null ? extras.getLong(PassList.KEY_ID) : null;
		}
		CategoryId = icicle != null ? icicle.getLong(PassList.KEY_CATEGORY_ID) : null;
		if (CategoryId == null) {
			Bundle extras = getIntent().getExtras();
			CategoryId = extras != null ? extras.getLong(PassList.KEY_CATEGORY_ID) : null;
		}

		populateFields();

		goButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {

				Toast.makeText(PassView.this, R.string.copy_to_clipboard,
						Toast.LENGTH_SHORT).show();

				ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
				cb.setText(passwordText.getText().toString());

				Intent i = new Intent(Intent.ACTION_VIEW);
				Uri u = Uri.parse(websiteText.getText().toString());
				i.setData(u);
				try {
					startActivity(i);
				} catch (ActivityNotFoundException e) {
					Toast.makeText(PassView.this, R.string.invalid_website,
						Toast.LENGTH_SHORT).show();
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
		outState.putLong(PassList.KEY_CATEGORY_ID, CategoryId);
	}

	@Override
	protected void onPause() {
		super.onPause();
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
			finish();
		}
//		populateFields();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, EDIT_PASSWORD_INDEX, 0, R.string.password_edit)
			.setIcon(android.R.drawable.ic_menu_edit).setShortcut('1', 'e');
		menu.add(0, DEL_PASSWORD_INDEX, 0, R.string.password_delete)
			.setIcon(android.R.drawable.ic_menu_delete).setShortcut('2', 'd');

		return super.onCreateOptionsMenu(menu);
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
		case EDIT_PASSWORD_INDEX:
			Intent i = new Intent(getApplicationContext(), PassEdit.class);
			i.putExtra(PassList.KEY_ID, RowId);
			i.putExtra(PassList.KEY_CATEGORY_ID, CategoryId);
			startActivityForResult(i, REQUEST_EDIT_PASS);
			break;
		case DEL_PASSWORD_INDEX:
			deletePassword();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
		
	/**
	 * 
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent i) {
		super.onActivityResult(requestCode, resultCode, i);

		if (debug) Log.d(TAG,"onActivityResult()");
		if (requestCode == REQUEST_EDIT_PASS) {
			if ((resultCode == RESULT_OK) || (PassEdit.entryEdited)){
				populateFields();
				entryEdited=true;
			}
		}
	}

	/**
	 * 
	 */
	private void populateFields() {
		if (debug) Log.d(TAG,"populateFields()");
		if (RowId != null) {
			if (dbHelper == null) {
				dbHelper = new DBHelper(this);
			}
			PassEntry row = dbHelper.fetchPassword(RowId);
			if (row.id > -1) {
				String cryptDesc = row.description;
				String cryptWebsite = row.website;
				String cryptUsername = row.username;
				String cryptPass = row.password;
				String cryptNote = row.note;
				String cryptUniqueName = row.uniqueName;
	    		ArrayList<String> packageAccess = dbHelper.fetchPackageAccess(row.id);
				try {
					descriptionText.setText(ch.decrypt(cryptDesc));
					websiteText.setText(ch.decrypt(cryptWebsite));
					usernameText.setText(ch.decrypt(cryptUsername));
					passwordText.setText(ch.decrypt(cryptPass));
					noteText.setText(ch.decrypt(cryptNote));
					String lastEdited;
					if (row.lastEdited!=null) {
						lastEdited=row.lastEdited;
					} else {
						lastEdited=getString(R.string.last_edited_unknown);
					}
					lastEditedText.setText(getString(R.string.last_edited)+": "+lastEdited);
					if (cryptUniqueName!=null) {
						String plainUniqueName=ch.decrypt(cryptUniqueName);
						if (plainUniqueName!="") {
							uniqueNameText.setText(getString(R.string.uniquename)+
									": "+plainUniqueName);
						}
					}
					String packages="";
					if (packageAccess!=null) {
						for (String packageName : packageAccess) {
							String plainPackageName=ch.decrypt(packageName);
							PackageManager pm=getPackageManager();
							String appLabel="";
							try {
								ApplicationInfo ai=pm.getApplicationInfo(plainPackageName,0);
								appLabel=pm.getApplicationLabel(ai).toString();
							} catch (NameNotFoundException e) {
								appLabel="("+getString(R.string.not_found)+")";
							}
							packages+=plainPackageName+" "+appLabel+" ";
						}
					}
					if (packages!="") {
						packageAccessText.setText(getString(R.string.package_access)+
								": "+packages);
					}
				} catch (CryptoHelperException e) {
					Log.e(TAG, e.toString());
				}
			}
		}
	}
}
