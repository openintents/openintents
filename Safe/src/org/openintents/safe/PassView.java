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

import org.openintents.intents.CryptoIntents;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.TextView;
import android.widget.Toast;

/**
 * PassView Activity
 * 
 * @author Randy McEoin
 */
public class PassView extends Activity implements View.OnClickListener {

	private static boolean debug = false;
	private static String TAG = "PassView";

	public static final int EDIT_PASSWORD_INDEX = Menu.FIRST;
	public static final int DEL_PASSWORD_INDEX = Menu.FIRST + 1;

	public static final int REQUEST_EDIT_PASS = 1;
	
	private TextView descriptionText;
	private TextView passwordText;
	private TextView usernameText;
	private TextView websiteText;
	private TextView noteText;
	private TextView lastEditedText;
	private TextView uniqueNameText;
	private TextView packageAccessText;
	private Long RowId;
	private Long CategoryId;
	public static boolean entryEdited=false;

	Intent frontdoor;
    private Intent restartTimerIntent=null;

    BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CryptoIntents.ACTION_CRYPTO_LOGGED_OUT)) {
            	 if (debug) Log.d(TAG,"caught ACTION_CRYPTO_LOGGED_OUT");
            	 startActivity(frontdoor);
            }
        }
    };

	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if (debug) Log.d(TAG,"onCreate("+icicle+")");
		
		frontdoor = new Intent(this, FrontDoor.class);
		frontdoor.setAction(CryptoIntents.ACTION_AUTOLOCK);
		restartTimerIntent = new Intent (CryptoIntents.ACTION_RESTART_TIMER);

		String title = getResources().getString(R.string.app_name) + " - "
				+ getResources().getString(R.string.view_entry);
		setTitle(title);

		setContentView(R.layout.pass_view);

		descriptionText = (TextView) findViewById(R.id.description);
		websiteText = (TextView) findViewById(R.id.website);
		usernameText = (TextView) findViewById(R.id.username);
		passwordText = (TextView) findViewById(R.id.password);
		noteText = (TextView) findViewById(R.id.note);
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

		if ((RowId==null) || (CategoryId==null) ||
				(RowId<1) || (CategoryId<1)) {
			// invalid Row or Category
			finish();
			return;
		}
		goButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				String link = websiteText.getText().toString();
				if (link == null || link.equals("") || link.equals("http://")) {
					return;
				}

				clipboard(getString(R.string.password), passwordText.getText().toString());

				Intent i = new Intent(Intent.ACTION_VIEW);
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
						Toast.makeText(PassView.this, R.string.invalid_website,
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
		outState.putLong(PassList.KEY_CATEGORY_ID, CategoryId);
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (debug) Log.d(TAG,"onResume()");

		try {
			unregisterReceiver(mIntentReceiver);
		} catch (IllegalArgumentException e) {
			//if (debug) Log.d(TAG,"IllegalArgumentException");
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (debug) Log.d(TAG,"onResume()");

		if (CategoryList.isSignedIn()==false) {
			startActivity(frontdoor);
			return;
		}
        IntentFilter filter = new IntentFilter(CryptoIntents.ACTION_CRYPTO_LOGGED_OUT);
        registerReceiver(mIntentReceiver, filter);

		Passwords.Initialize(this);

        populateFields();
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
		Passwords.deletePassEntry(Id);
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
	    	if (resultCode==PassEdit.RESULT_DELETED) {
				entryEdited=true;
	    		finish();
	    	}
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
			PassEntry row = Passwords.getPassEntry(RowId, true, false);
			if (row==null) {
				if (debug) Log.d(TAG,"populateFields: row=null");
				return;
			}
    		ArrayList<String> packageAccess = Passwords.getPackageAccess(RowId);
			descriptionText.setText(row.plainDescription);
			websiteText.setText(row.plainWebsite);
			usernameText.setText(row.plainUsername);
			usernameText.setOnClickListener(this);
			passwordText.setText(row.plainPassword);
			passwordText.setOnClickListener(this);
			noteText.setText(row.plainNote);
			String lastEdited;
			if (row.lastEdited!=null) {
				lastEdited=row.lastEdited;
			} else {
				lastEdited=getString(R.string.last_edited_unknown);
			}
			lastEditedText.setText(getString(R.string.last_edited)+": "+lastEdited);
			if (row.plainUniqueName!=null) {
				uniqueNameText.setText(getString(R.string.uniquename)+
						": "+row.plainUniqueName);
			}
			String packages="";
			if (packageAccess!=null) {
				for (String packageName : packageAccess) {
					if (debug) Log.d(TAG,"packageName="+packageName);
					PackageManager pm=getPackageManager();
					String appLabel="";
					try {
						ApplicationInfo ai=pm.getApplicationInfo(packageName,0);
						appLabel=pm.getApplicationLabel(ai).toString();
					} catch (NameNotFoundException e) {
						appLabel="("+getString(R.string.not_found)+")";
					}
					packages+=packageName+" "+appLabel+" ";
				}
			}
			if (packages!="") {
				packageAccessText.setText(getString(R.string.package_access)+
						": "+packages);
			}
		}
	}

	/**
	 * 
	 * @author Billy Cui
	 */
	public void onClick(View view) {
		if (view == usernameText) {
			if (debug) Log.d(TAG, "click " + usernameText.getText());
			clipboard(getString(R.string.username),usernameText.getText().toString());
		} else if (view == passwordText) {
			if (debug) Log.d(TAG, "click " + passwordText.getText());
			clipboard(getString(R.string.password),passwordText.getText().toString());
		}
	}

	/**
	 * Copy to clipboard and toast to let user know that we have done so.
	 * 
	 * @author Billy Cui
	 * @param fieldName Name of the field copied from
	 * @param value String to copy to clipboard
	 */
	private void clipboard(String fieldName, String value) {
		Toast.makeText(PassView.this, fieldName+" "+getString(R.string.copied_to_clipboard),
				Toast.LENGTH_SHORT).show();

		ClipboardManager cb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		cb.setText(value);
	}

	@Override
	public void onUserInteraction() {
		super.onUserInteraction();

		if (debug) Log.d(TAG,"onUserInteraction()");

		if (CategoryList.isSignedIn()==false) {
//			startActivity(frontdoor);
		}else{
			if (restartTimerIntent!=null) sendBroadcast (restartTimerIntent);
		}
	}
}
