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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.openintents.intents.CryptoIntents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.MenuItem;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * PassList Activity
 * 
 * This is the main activity for PasswordSafe all other activities are 
 * spawned as sub-activities of this one.  The basic application 
 * skeleton was based on google's notepad example.
 * 
 * @author Steven Osborn - http://steven.bitsetters.com
 */
public class PassList extends ListActivity {

	private static final boolean debug= false;
    private static final String TAG = "PassList";

    // Menu Item order
    public static final int VIEW_PASSWORD_INDEX = Menu.FIRST;
    public static final int EDIT_PASSWORD_INDEX = Menu.FIRST + 1;
    public static final int ADD_PASSWORD_INDEX = Menu.FIRST + 2;
    public static final int DEL_PASSWORD_INDEX = Menu.FIRST + 3;   
    public static final int MOVE_PASSWORD_INDEX = Menu.FIRST + 4;
    
    public static final int REQUEST_VIEW_PASSWORD = 1;
    public static final int REQUEST_EDIT_PASSWORD = 2;
    public static final int REQUEST_ADD_PASSWORD = 3;
    public static final int REQUEST_MOVE_PASSWORD = 4;
    
    public static final String KEY_ID = "id";  // Intent keys
    public static final String KEY_CATEGORY_ID = "categoryId";  // Intent keys

    private CryptoHelper ch;
    private DBHelper dbHelper=null;
    private static Long CategoryId=null;
    private Intent restartTimerIntent;

    private static String salt;
    private static String masterKey;			

    private List<PassEntry> rows;
    
    /** 
     * Called when the activity is first created. 
     */
    @Override
    public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		if (debug) Log.d(TAG,"onCreate()");
		restartTimerIntent = new Intent (CryptoIntents.ACTION_RESTART_TIMER);
		setContentView(R.layout.pass_list);
		
		if (dbHelper==null) {
			dbHelper = new DBHelper(this);
		}
		CategoryId = icicle != null ? icicle.getLong(CategoryList.KEY_ID) : null;
		if (CategoryId == null) {
		    Bundle extras = getIntent().getExtras();            
		    CategoryId = extras != null ? extras.getLong(CategoryList.KEY_ID) : null;
		}
		if (CategoryId<1) {
			finish();	// no valid category less than one
		}
		
		String categoryName=getCategoryName(CategoryId);
		String title = getResources().getString(R.string.app_name) + " - " +
			getResources().getString(R.string.passwords) + " -" +
			categoryName;
		setTitle(title);

		fillData();

		final ListView list = getListView();
		list.setFocusable(true);
		list.setOnCreateContextMenuListener(this);
		registerForContextMenu(list);
    }
    
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		// remember which Category we're looking at
		if (CategoryId != null) {
			outState.putLong(CategoryList.KEY_ID, CategoryId);
		} else {
			outState.putLong(CategoryList.KEY_ID, -1);
		}
	}

    @Override
    protected void onPause() {
		super.onPause();
		
		if (debug) Log.d(TAG,"onPause()");
		if (dbHelper != null) {
			dbHelper.close();
			dbHelper = null;
		}
    }

    @Override
    protected void onResume() {
		super.onResume();
		
		if (debug) Log.d(TAG,"onResume()");

		if (CategoryList.isSignedIn()==false) {
			finish();
		}
		if (dbHelper == null) {
		    dbHelper = new DBHelper(this);
		}
    }
    
    @Override
    public void onStop() {
		super.onStop();
		
		if (debug) Log.d(TAG,"onStop()");
		if (dbHelper != null) {
			dbHelper.close();
			dbHelper=null;
		}
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
    		ContextMenuInfo menuInfo) {

		AdapterView.AdapterContextMenuInfo info;
		info = (AdapterView.AdapterContextMenuInfo) menuInfo;

		menu.setHeaderTitle(rows.get(info.position).plainDescription);
		menu.add(0, VIEW_PASSWORD_INDEX, 0, R.string.password_view)
			.setIcon(android.R.drawable.ic_menu_view)
			.setAlphabeticShortcut('v');
		menu.add(0, EDIT_PASSWORD_INDEX, 0, R.string.password_edit)
			.setIcon(android.R.drawable.ic_menu_edit)
			.setAlphabeticShortcut('e');
		menu.add(0, DEL_PASSWORD_INDEX, 0, R.string.password_delete)  
			.setIcon(android.R.drawable.ic_menu_delete)
			.setAlphabeticShortcut('d');
		menu.add(0, MOVE_PASSWORD_INDEX, 0, R.string.move)  
			.setIcon(android.R.drawable.ic_menu_more)
			.setAlphabeticShortcut('m');
    }
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		onOptionsItemSelected(item);
		return true;
    }

    /**
     * Populates the password ListView
     */
    private void fillData() {
		// initialize crypto so that we can display readable descriptions in
		// the list view
		ch = new CryptoHelper();
		if(masterKey == null) {
		    masterKey = "";
		}
		try {
			ch.init(CryptoHelper.EncryptionMedium, salt);
			ch.setPassword(masterKey);
		} catch (CryptoHelperException e1) {
			e1.printStackTrace();
			Toast.makeText(this,getString(R.string.crypto_error)
					+ e1.getMessage(), Toast.LENGTH_SHORT).show();
			return;
		}
	
		List<String> items = new ArrayList<String>();
		rows = dbHelper.fetchAllRows(CategoryId);

		for (PassEntry row : rows) {
		    String cryptDesc = row.description;
		    row.plainDescription = "";
		    try {
				row.plainDescription = ch.decrypt(cryptDesc);
		    } catch (CryptoHelperException e) {
				Log.e(TAG,e.toString());
		    }
		}
		Collections.sort(rows, new Comparator<PassEntry>() {
		    public int compare(PassEntry o1, PassEntry o2) {
		        return o1.plainDescription.compareToIgnoreCase(o2.plainDescription);
		    }});
		for (PassEntry row : rows) {
			items.add(row.plainDescription);
		}

		ArrayAdapter<String> entries = 
		    new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		setListAdapter(entries);
		
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
		MenuItem miDel  = menu.findItem(DEL_PASSWORD_INDEX);
		MenuItem miMove = menu.findItem(MOVE_PASSWORD_INDEX);
    	if (getSelectedItemPosition() > -1) {
    		miDel.setEnabled(true);
    		miMove.setEnabled(true);
    	} else {
    		miDel.setEnabled(false);
    		miMove.setEnabled(false);
    	}
    	return super.onMenuOpened(featureId, menu);
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, ADD_PASSWORD_INDEX, 0, R.string.password_add)
			.setIcon(android.R.drawable.ic_menu_add)
			.setShortcut('2', 'a');
		menu.add(0, DEL_PASSWORD_INDEX, 0, R.string.password_delete)
			.setIcon(android.R.drawable.ic_menu_delete)
			.setShortcut('3', 'd');
		menu.add(0, MOVE_PASSWORD_INDEX, 0, R.string.move)
			.setIcon(android.R.drawable.ic_menu_more)
			.setShortcut('4', 'm');
	
		return super.onCreateOptionsMenu(menu);
    }

    static void setSalt(String saltIn) {
		salt = saltIn;
    }

    static String getSalt() {
		return salt;
    }

    static void setMasterKey(String key) {
		masterKey = key;
    }

    static String getMasterKey() {
		return masterKey;
    }

    static long getCategoryId() {
    	return CategoryId;
    }

    private void addPassword() {
		Intent i = new Intent(this, PassEdit.class);
	    startActivityForResult(i,REQUEST_ADD_PASSWORD);
    }
	/**
	 * Prompt the user with a dialog asking them if they really want
	 * to delete the password.
	 */
	public void deletePassword(final int position){
		Dialog about = new AlertDialog.Builder(this)
			.setIcon(R.drawable.passicon)
			.setTitle(R.string.dialog_delete_password_title)
			.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					deletePassword2(position);
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
	public void deletePassword2(int position){
	    try {
	    	delPassword(rows.get(position).id);
	    } catch (IndexOutOfBoundsException e) {
			// This should only happen when there are no
			// entries to delete.
			Log.w(TAG,e.toString());
	    }
	}

    private void delPassword(long Id) {
		dbHelper.deletePassword(Id);
		fillData();
    }
    
    /**
     * Prompt the user with Categories to move the specified 
     * password to and then update the password entry accordingly.
     * 
     * @param passwordId
     */
    private void movePassword(final long passwordId) {
        final HashMap<String, Long> categoryToId=CategoryList.getCategoryToId(dbHelper);
        String categoryName=getCategoryName(CategoryId);
        categoryToId.remove(categoryName);
        Set<String> categories=categoryToId.keySet();
        final String[] items=(String[])categories.toArray(new String[categories.size()]);
        Arrays.sort(items, String.CASE_INSENSITIVE_ORDER);

		new AlertDialog.Builder(PassList.this)
		.setTitle(R.string.move_select)
		.setItems(items, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {

				long newCategoryId=categoryToId.get(items[which]);
				dbHelper.updatePasswordCategory(passwordId, newCategoryId);
				String result=getString(R.string.moved_to) + " " + items[which];
     			Toast.makeText(PassList.this, result,
         				Toast.LENGTH_LONG).show();
				fillData();
			}
		})
		.show();
    }
    
    public boolean onOptionsItemSelected(MenuItem item) {
		sendBroadcast (restartTimerIntent);
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int position=-1;
		if (info==null) {
			position=getSelectedItemPosition();
		} else {
			// used when this is called from a ContextMenu
			position=info.position;
		}

		switch(item.getItemId()) {
		case ADD_PASSWORD_INDEX:
		    addPassword();
		    break;
		case VIEW_PASSWORD_INDEX:
			Intent vi = new Intent(this, PassView.class);
			vi.putExtra(KEY_ID, rows.get(position).id);
			vi.putExtra(KEY_CATEGORY_ID, CategoryId);
			startActivityForResult(vi,REQUEST_VIEW_PASSWORD);
		    break;
		case EDIT_PASSWORD_INDEX:
			Intent i = new Intent(this, PassEdit.class);
			i.putExtra(KEY_ID, rows.get(position).id);
			i.putExtra(KEY_CATEGORY_ID, CategoryId);
			startActivityForResult(i,REQUEST_EDIT_PASSWORD);
			break;
		case DEL_PASSWORD_INDEX:
			deletePassword(position);
		    break;
		case MOVE_PASSWORD_INDEX:
			movePassword(rows.get(position).id);
			break;
		}
		return super.onOptionsItemSelected(item);
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
	
		Intent i = new Intent(this, PassView.class);
		i.putExtra(KEY_ID, rows.get(position).id);
		i.putExtra(KEY_CATEGORY_ID, CategoryId);
	    startActivityForResult(i,REQUEST_VIEW_PASSWORD);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent i) {
    	super.onActivityResult(requestCode, resultCode, i);
    	//Log.d(TAG, "onActivityResult. requestCode: " + requestCode + ", resultCode: " + resultCode);

    	if (dbHelper == null) {
		    dbHelper = new DBHelper(this);
		}
    	if (((requestCode==REQUEST_VIEW_PASSWORD)&&(PassView.entryEdited)) ||
    	    	((requestCode==REQUEST_EDIT_PASSWORD)&&(PassEdit.entryEdited)) ||
    	    	((requestCode==REQUEST_ADD_PASSWORD)&&(PassEdit.entryEdited)) ||
    			(resultCode==RESULT_OK)) {
    		fillData();
    	}
    }
    
    /**
     * Retrieve the decrypted category name based on the provided id.
     *  
     * @param Id category id
     * @return decrypted category name
     */
    private String getCategoryName(long Id) {
		CategoryEntry category=dbHelper.fetchCategory(Id);
		category.plainName="";
		if (ch==null) {
			ch=new CryptoHelper();
		}
		try {
			ch.init(CryptoHelper.EncryptionMedium, salt);
			ch.setPassword(masterKey);
			category.plainName = ch.decrypt(category.name);
	    } catch (CryptoHelperException e) {
			Log.e(TAG,e.toString());
			Toast.makeText(this,getString(R.string.crypto_error)
				+ e.getMessage(), Toast.LENGTH_SHORT).show();
	    }
	    return category.plainName;
    }
}
