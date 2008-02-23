/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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

package org.openintents.shopping;

import java.util.ArrayList;

import org.openintents.R;
import org.openintents.hardware.Sensors;
import org.openintents.hardware.SensorsPlus;
import org.openintents.provider.Hardware;
import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.ContainsFull;
import org.openintents.provider.Shopping.Lists;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.view.Menu.Item;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class ShoppingView extends Activity //implements AdapterView.OnItemClickListener 
{

	/**
	 * TAG for logging.
	 */
	private static final String TAG = "ShoppingView";
	
	private static final int MENU_NEW_LIST = Menu.FIRST;
	private static final int MENU_CLEAN_UP_LIST = Menu.FIRST + 1;
	private static final int MENU_DELETE_LIST = Menu.FIRST + 2;
	
	// TODO: Implement the following menu items
	private static final int MENU_EDIT_LIST = Menu.FIRST + 3; // includes rename
	private static final int MENU_EDIT_ITEM = Menu.FIRST + 4; // includes rename
	private static final int MENU_DELETE_ITEM = Menu.FIRST + 5;
	private static final int MENU_SORT = Menu.FIRST + 6; // sort alphabetically or modified
	private static final int MENU_PICK_ITEMS = Menu.FIRST + 7; // pick from previously used items
	
	// TODO: Implement "select list" action 
	// that can be called by other programs.
	private static final int MENU_SELECT_LIST = Menu.FIRST + 8; // select a shopping list
	
	// TODO: Further possible actions to implement:
	// * Move items to some other shopping list
	
	// 
	private static final int MENU_SETTINGS = Menu.FIRST + 100;
	private static final int MENU_CONNECT_SIMULATOR = Menu.FIRST + 101;

	
	private LinearLayout mLinearLayoutBackground;
	
	/**
	 * Sets how many pixels the EditBox shall be away
	 * from the bottom of the screen, when 
	 * the height of the list-box is limited in
	 * checkListLength()
	 */
	private static final int mBottomPadding = 50;
	
	/**
	 * Maximum number of lines on the screen.
	 * (should be calculated later, for now hardcoded.)
	 */
	private static int mMaxListCount = 7;
	
	/**
	 * Private members connected to Spinner ListFilter.
	 */
	private Spinner mSpinnerListFilter;
	private Cursor mCursorListFilter;
	private static final String[] mStringListFilter = 
		new String[] { Lists._ID, Lists.NAME, Lists.IMAGE};
	private static final int mStringListFilterID = 0;
	private static final int mStringListFilterNAME = 1;
	private static final int mStringListFilterIMAGE = 2;
	
	private ListView mListItems;
	private Cursor mCursorItems;
	private String TEST;
	private static final String[] mStringItems =
		new String[] { 
				ContainsFull._ID, 
				ContainsFull.ITEM_NAME,
				ContainsFull.ITEM_IMAGE,
				ContainsFull.STATUS};
	private static final int mStringItemsCONTAINSID = 0;
	private static final int mStringItemsITEMNAME = 1;
	private static final int mStringItemsITEMIMAGE = 2;
	private static final int mStringItemsSTATUS = 3;
	private LinearLayout.LayoutParams mLayoutParamsItems;
	private int mAllowedListHeight; // Height for the list allowed in this view.
	
	private EditText mEditText;
	
	protected Context mDialogContext;
	protected Dialog mDialog;
	
	// TODO: Set up state information for onFreeze(), ...
	// State data to be stored when freezing:
	private final String ORIGINAL_ITEM = "original item";
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		//setTheme(android.R.style.Theme_White);
		//setTheme(android.R.style.Theme_Dialog);
		//setTheme(android.R.style.Theme_Dark);
		//setTheme(android.R.style.Theme_Black);
		//setTheme(android.R.style.Theme_Dark);
        setContentView(R.layout.shopping);
		
		// Initialize the convenience functions:
		Shopping.mContentResolver = getContentResolver();

		// hook up all buttons, lists, edit text:
		createView();
		
		// populate the lists
		fillListFilter();

		// select the default shopping list at the beginning:
		setSelectedListId((int) Shopping.getDefaultList());
		// TODO: Select the last shopping list viewed
		//       instead of the default shopping list.
		//       (requires saving that information as
		//        preference).
		
		// now fill all items
		fillItems();
		
		if (icicle != null) {
			String prevText = icicle.getString(ORIGINAL_ITEM);
			if (prevText != null) {
				mEditText.setText(prevText);
			}
		}
		
		// set focus to the edit line:
		mEditText.requestFocus();
	}
	
	
	
	@Override
	protected void onResume() {
		super.onResume();
		
		checkListLength();
	}



	@Override
	protected void onFreeze(Bundle outState) {
		super.onFreeze(outState);
		
        // Save original text from edit box
		String s = mEditText.getText().toString();
        outState.putString(ORIGINAL_ITEM, s);
    }

	/**
	 * Hook up buttons, lists, and edittext with functionality.
	 */
	private void createView() {
		mLinearLayoutBackground = (LinearLayout) findViewById(R.id.background);
		
		mSpinnerListFilter = (Spinner) findViewById(R.id.spinner_listfilter);
		mSpinnerListFilter.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView parent, View v,
					int position, long id) {
				fillItems();
				checkListLength();
			}
			
			public void onNothingSelected(AdapterView arg0) {
				fillItems();
				checkListLength();
			}
		});
        
		mEditText = (EditText) findViewById(R.id.edittext_add_item);
		mEditText.setOnKeyListener(new OnKeyListener() {
			
			public boolean onKey(View v, int keyCode, KeyEvent key) {
				//Log.i(TAG, "KeyCode: " + keyCode 
				//		+ " =?= " 
				//		+Integer.parseInt(getString(R.string.key_return)) );
				
				// Shortcut: Instead of pressing the button, 
				// one can also press the "Enter" key.
				if (key.getAction() == key.ACTION_DOWN && 
						keyCode == Integer.parseInt(getString(R.string.key_return)))
				{
					insertNewItem();
					return true;
				};
				return false;
			}
		});
		
		Button button = (Button) findViewById(R.id.button_add_item);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				insertNewItem();
			}
		});
		
		mLayoutParamsItems = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.FILL_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT); 
		
		mListItems = (ListView) findViewById(R.id.list_items);		
		mListItems.setOnItemClickListener(
			new OnItemClickListener() {
				
				public void onItemClick(AdapterView parent, 
						View v, int pos, long id) {
					Cursor c = (Cursor) parent.obtainItem(pos);
					toggleItemBought(c);
				}
				
		});
		mListItems.setOnItemSelectedListener(new OnItemSelectedListener() {
			
			public void onItemSelected(AdapterView parent, View v,
					int position, long id) {
				// Log.i(TAG, "mListItems selected: pos:" 
				// 	+ position + ", id:" + id);
				checkListLength();
			}
			
			public void onNothingSelected(AdapterView arg0) {
				// TODO Auto-generated method stub
				checkListLength();
			}
		});
		
	}
	
	/**
	 * Inserts new item from edit box into 
	 * currently selected shopping list.
	 */
	private void insertNewItem() {		
		String newItem = mEditText.getText().toString();
		
		// Only add if there is something to add:
		if (newItem.compareTo("") != 0) {
			long listId = getSelectedListId();
			
			long itemId = Shopping.getItem(newItem);
			
			Log.i(TAG, "Insert new item. " 
					+ " itemId = " + itemId + ", listId = " + listId);
			Shopping.addItemToList(itemId, 
					listId);
			
			mEditText.setText("");
			
			fillItems();
			
			checkListLength();
			
			// TODO:
			// Now scroll the list to the end, where
			// the new item has been inserted:
			// Can these functions be of use?
			// mListItems.scrollTo(x, y)
			// mListItems.requestChildRectangleOnScreen(child, rectangle)
			
			// now select the new item (which will be at the bottom
			// TODO: THIS IS REALLY A CHEAP FIX:
			// This only works for one specific view size and font size.
			
			// Answer? 
			// http://groups.google.com/group/android-developers/browse_frm/thread/3b2f4063a2221acb/36462ba1301a18c8
			int NUMBER_OF_ELEMENTS_BELOW_MIDDLE = 4;
			if (mListItems.getCount() > NUMBER_OF_ELEMENTS_BELOW_MIDDLE)
			{
				mListItems.setSelection(mListItems.getCount() - NUMBER_OF_ELEMENTS_BELOW_MIDDLE);
			};
			
			//mListItems.getChildAt(mListItems.getCount()-1).setSelected(true);
		}
	}
	
	// strike item through or undo this.
	private void toggleItemBought(Cursor c) {
		// Toggle status:
		long oldstatus = c.getLong(mStringItemsSTATUS);
		long newstatus = Shopping.Status.BOUGHT;
		if (oldstatus == Shopping.Status.BOUGHT) {
			newstatus = Shopping.Status.WANT_TO_BUY;
		}
			
		c.updateLong(mStringItemsSTATUS, newstatus);
		
		Log.i(TAG, "Commit now:");
		c.commitUpdates();
		
		Log.i(TAG, "Requery now:");
		c.requery();
		
		// fillItems();
	}
	
	// Menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Standard menu
		menu.add(0, MENU_NEW_LIST, R.string.new_list, R.drawable.shoppinglistnew001b)
			.setShortcut('0', 'n');
		menu.add(0, MENU_CLEAN_UP_LIST, R.string.clean_up_list, R.drawable.shoppinglistcleanup001b)
			.setShortcut('1', 'c');
		menu.add(0, MENU_DELETE_LIST, R.string.delete_list, R.drawable.shoppinglistdelete001b)
		.setShortcut('2', 'd');
		
		/*
		menu.add(0, MENU_SETTINGS, R.string.sensorsimulator_settings)
		.setShortcut('0', 's');
		menu.add(0, MENU_CONNECT_SIMULATOR, R.string.connect_to_sensorsimulator)
		.setShortcut('1', 'c');
		 */
	
		// Generate any additional actions that can be performed on the
        // overall list.  This allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.ALTERNATIVE_CATEGORY);
        menu.addIntentOptions(
            Menu.ALTERNATIVE, 0, new ComponentName(this, ShoppingView.class),
            null, intent, 0, null);
        
        // Set checkable items:
        menu.setItemCheckable(MENU_CONNECT_SIMULATOR, true);

		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		// TODO: Add item-specific menu items (see NotesList.java example)
		// like edit, strike-through, delete.
		
		// Delete list is possible, if we have more than one list:
		menu.setItemShown(MENU_DELETE_LIST, mCursorListFilter.count() > 1);
		
		menu.setItemChecked(MENU_CONNECT_SIMULATOR, SensorsPlus.isConnectedSimulator());

		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(Item item) {
		switch (item.getId()) {
		case MENU_NEW_LIST:
			newListDialog();
			return true;
			
		case MENU_CLEAN_UP_LIST:
			cleanupList();
			return true;
		
		case MENU_DELETE_LIST:
			deleteListConfirm();
			return true;

		case MENU_SETTINGS:
			Intent intent = new Intent(Intent.MAIN_ACTION, Hardware.Preferences.CONTENT_URI);
			startActivity(intent);
			return true;
			
		case MENU_CONNECT_SIMULATOR:
			// check if accelerometer is supported:
			if (SensorsPlus.isSupportedSensor(Sensors.SENSOR_ACCELEROMETER)) {
				// first disable the current sensors:
				Sensors.disableSensor(Sensors.SENSOR_ACCELEROMETER);
			}
			
			if (!SensorsPlus.isConnectedSimulator()) {
				// now connect to simulator
				Sensors.connectSimulator();
			} else {
				// or disconnect to simulator
				Sensors.disconnectSimulator();				
			}
			
			// check if accelerometer is supported:
	        if (SensorsPlus.isSupportedSensor(Sensors.SENSOR_ACCELEROMETER)) {
	        	// enable the sensor:
		        Sensors.enableSensor(Sensors.SENSOR_ACCELEROMETER);	
	        }
	        
	        return true;
		}
		return super.onOptionsItemSelected(item);
		
	}
	
	///////////////////////////////////////////////////////
	//
	// Menu functions
	//

	/**
	 * Opens a dialog to add a new shopping list.
	 */
	private void newListDialog() {
		
		// TODO Shall we implement this as action?
		// Then other applications may call this as well.

		mDialog = new Dialog(ShoppingView.this);
		
		mDialog.setContentView(R.layout.input_box);
		
		mDialog.setTitle(getString(R.string.ask_new_list));
		
		EditText et = (EditText) mDialog.findViewById(R.id.edittext);
		et.setText(getString(R.string.new_list));
		et.selectAll();
		
		// Accept OK also when user hits "Enter"
		et.setOnKeyListener(new OnKeyListener() {
			
			public boolean onKey(final View v, final int keyCode, 
					final KeyEvent key) {
				//Log.i(TAG, "KeyCode: " + keyCode);
				
				if (key.getAction() == key.ACTION_DOWN
						&& keyCode == Integer
							.parseInt(getString(R.string.key_return))) {
					// User pressed "Enter" 
					createNewList();
					mDialog.dismiss();
					return true;	
				}
				return false;
			}
			
		});
		
		
		Button bOk = (Button) mDialog.findViewById(R.id.ok);
		bOk.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				createNewList();
				mDialog.dismiss();
			}
		});
		
		Button bCancel = (Button) mDialog.findViewById(R.id.cancel);
		bCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mDialog.cancel();
			}
		});
		
		mDialog.show();
	}
	
	private void createNewList() {
		EditText edittext = (EditText) 
			mDialog.findViewById(R.id.edittext);
	
		int newId = (int) Shopping.getList(edittext.getText().toString());
		
		edittext.setText("");
		fillListFilter();
		
		setSelectedListId(newId);
	}
	
	/**
	 * Clean up the currently visible shopping list
	 * by removing items from list that are marked BOUGHT.
	 */
	private void cleanupList() {
		// Delete all items from current list 
		// which have STATUS = Status.BOUGHT
		
		// TODO One could write one SQL statement to delete all at once.
		// But as long as shopping lists stay small, it should not matter.
		
		boolean nothingdeleted = true;
		mCursorItems.moveTo(-1); // move to beginning
		while (mCursorItems.next())
		{
			if (mCursorItems.getLong(mStringItemsSTATUS)
				== Shopping.Status.BOUGHT) {
				mCursorItems.deleteRow();
				mCursorItems.prev(); // Otherwise we would skip an item
				nothingdeleted = false;
			}
		}
		mCursorItems.commitUpdates();
		mCursorItems.requery();
		
		if (nothingdeleted) {
			// Show dialog:
			AlertDialog.show(ShoppingView.this, 
				getString(R.string.clean_up_list),
				0, // TODO choose IconID
				getString(R.string.no_items_marked), 
				getString(R.string.ok),
				false);
		} else {
			checkListLength();
		}
	}

	/**
	 * Confirm 'delete list' command by AlertDialog.
	 */
	private void deleteListConfirm() {
		AlertDialog.show(ShoppingView.this, 
			getString(R.string.delete_list),
			0, // TODO IconID?
			getString(R.string.confirm_delete_list), 
			getString(R.string.ok),
			new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface di, int whichDialog) {
					Log.i(TAG, "Dialog click on:" + whichDialog);
					deleteList();
				}
			},
			getString(R.string.cancel),
			new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface di, int whichDialog) {
					Log.i(TAG, "Dialog click on:" + whichDialog);
				}
			},
			true, 
			new DialogInterface.OnCancelListener() {
				
				public void onCancel(DialogInterface di) {
					// TODO Auto-generated method stub
				}				
			});
		
	}
	
	/**
	 * Deletes currently selected shopping list.
	 */
	private void deleteList() {
		// First delete all items in list
		mCursorItems.moveTo(0); // move to beginning
		while (mCursorItems.count() > 0) {
			mCursorItems.deleteRow();
		}
		
		// Then delete currently selected list
		mCursorListFilter.deleteRow();
		
		// Update view
		fillListFilter();
		fillItems();
	}
	
	///////////////////////////////////////////////////////
	//
	// Helper functions
	//
	/**
	 * Returns the ID of the selected shopping list.
	 * @return ID of selected shopping list.
	 */
	private long getSelectedListId() {
		// Obtain Id of currently selected shopping list:
		mCursorListFilter.moveTo(
				mSpinnerListFilter.getSelectedItemPosition());
		return mCursorListFilter.getLong(mStringListFilterID);
	};
	
	/**
	 * sets the selected list to a specific list Id
	 */
	private void setSelectedListId(int id) {
		// Is there a nicer way to accomplish the following?
		// (we look through all elements to look for the
		//  one entry that has the same ID as returned by
		//  getDefaultList()).
		//
		// unfortunately, a SQL query won't work, as it would
		// return 1 row, but I still would not know which
		// row in the mCursorListFilter corresponds to that id.
		//
		// one could use: findViewById() but how will this
		// translate to the position in the list?
		mCursorListFilter.moveTo(-1);
		while (mCursorListFilter.next()) {
			int posId = mCursorListFilter.getInt(mStringListFilterID);
			if (posId == id) {
				int row = mCursorListFilter.position();
				
				// if we found the Id, then select this in 
				// the Spinner:
				mSpinnerListFilter.setSelection(row);
				break;
			}
		}
	}
	
	/**
	 * 
	 */
	private void fillListFilter() {
		// Get a cursor with all lists
		mCursorListFilter = getContentResolver().query(Lists.CONTENT_URI, 
				mStringListFilter, 
				null, null, Lists.DEFAULT_SORT_ORDER);
		startManagingCursor(mCursorListFilter);

		if (mCursorListFilter == null) {
			Log.e(TAG, "missing shopping provider");
			
			mSpinnerListFilter.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_spinner_item,
					new String[] { getString(R.string.no_shopping_provider) }));
			return;
		}
		
		if (mCursorListFilter.count() < 1) {
			// We have to create default shopping list:
			// TODO Put the following string into resource my_shopping_list
			Shopping.getList("My shopping list");
			
			// TODO Check if insertion really worked. Otherwise
			//      we may end up in infinite recursion.
			
			// The insertion should have worked, so let us call ourselves
			// to try filling the list again:
			fillListFilter();
			return;
		}

		ArrayList<String> list = new ArrayList<String>();
		// TODO Create summary of all lists
		// list.add(ALL);
		while (mCursorListFilter.next()) {
			list.add(mCursorListFilter.getString(mStringListFilterNAME));
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinnerListFilter.setAdapter(adapter);
		
	}


	private void fillItems() {
		//Log.i(TAG, "Starting fillItems()");
		
		long listId = getSelectedListId();
		
		// Get a cursor for all items that are contained 
		// in currently selected shopping list.
		mCursorItems = getContentResolver().query(
				ContainsFull.CONTENT_URI,
				mStringItems,
				"list_id = " + listId, null,
				ContainsFull.DEFAULT_SORT_ORDER);
		startManagingCursor(mCursorItems);
		

		if (mCursorItems == null) {
			Log.e(TAG, "missing shopping provider");
			mListItems.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no shopping provider" }));
			return;
		}
		
		ListAdapter adapter = new mSimpleCursorAdapter(this,
				// Use a template that displays a text view
				R.layout.shopping_item_row,
				// Give the cursor to the list adapter
				mCursorItems,
				// Map the IMAGE and NAME to...
				new String[] { 
					ContainsFull.ITEM_NAME, 
					ContainsFull.ITEM_IMAGE },
				// the view defined in the XML template
				new int[] { 
					R.id.name, 
					R.id.image_URI });
		mListItems.setAdapter(adapter);
		
		//strikeItems();
		 //checkListLength();
	}
	
	/**
	 * This function checks the length of the list.
	 * If the number of items is too large, the height
	 * of the list is limited, such that the EditText
	 * element will not drop out of the view.
	 */
	private void checkListLength() {
		Log.i(TAG, "checkListLength()");
		/*
		// Now we check whether we reach the lower border already:
		int[] locEditText = new int[2];
		mEditText.getAbsoluteLocationOnScreen(locEditText);
		int bottomEdit = locEditText[1] + mEditText.getBottom();
		
		int[] locBackground = new int[2];
		mLinearLayoutBackground.getAbsoluteLocationOnScreen(locBackground);
		int bottomBackground = locBackground[1] + mLinearLayoutBackground.getBottom();
		*/
		// number of items in the list
		//int count = mListItems.getCount();
		int count = mCursorItems.count();
		
		// Let's hardcode the number of items:
		if (count <= mMaxListCount) {
			mLayoutParamsItems.height 
			= LinearLayout.LayoutParams.WRAP_CONTENT;
			//mEditText.append("l");
		} else {
			//mEditText.append("m");
			WindowManager w = getWindowManager(); 
	        Display d = w.getDefaultDisplay(); 
	        int width = d.getWidth(); 
	        int height = d.getHeight(); 

			mAllowedListHeight = 
				//mLinearLayoutBackground.getHeight()
				height
				//- mListItems.getTop()
				- mSpinnerListFilter.getHeight()
				- mEditText.getHeight()
				- mBottomPadding;
			if (mAllowedListHeight < 0)
			{
				mAllowedListHeight = 0;
			}
			// we have to limit the height:
			mLayoutParamsItems.height = mAllowedListHeight;
		}
		
		mListItems.setLayoutParams(mLayoutParamsItems);
		
		
		/*
		
		if (count < 1) {
			mLayoutParamsItems.height = LinearLayout.LayoutParams.WRAP_CONTENT;
		}
		else {
			// for now, let us take the height of 
			// the first element and multiply by count.
			// TODO later: Some items may be larger
			//      (if they contain images).
			//      How can we best determine the size
			//      there?
			//int singleHeight = mListItems.getChildAt(0).getHeight();
			int listHeight = mListItems.getHeight();
			Log.i(TAG, "listHeight: " + listHeight);
			
			int editTextHeight = mEditText.getHeight();
			
			// calculate the allowed height of the list
			// TODO: could be moved to initialization after freeze?
			mAllowedListHeight = 
				mLinearLayoutBackground.getHeight()
				- mListItems.getTop()
				- editTextHeight
				- mBottomPadding;
			
			if ((mAllowedListHeight < 1) 
					|| (listHeight + editTextHeight < mAllowedListHeight)) {
				mLayoutParamsItems.height 
					= LinearLayout.LayoutParams.WRAP_CONTENT;
			} else {
				// we have to limit the height:
				mLayoutParamsItems.height = mAllowedListHeight;
			}
		}
		Log.i(TAG, "Items.height: " + mLayoutParamsItems.height);
		if (mLayoutParamsItems.height > 0)
		{
			mListItems.setLayoutParams(mLayoutParamsItems);
		};
		*/
		/*
		int listLen = 
		
		if (bottomEdit > bottomBackground) {
			// The list is too long, the edit text field
			// would fall off the screen, so we have
			// to limit the height of the list:
			int[] locList = new int[2];
			mListItems.getAbsoluteLocationOnScreen(locList);
			int topList = locList[1];
			
			int newListSize = bottomBackground 
				- topList
				- mEditText.getHeight();
			
			Log.i(TAG, "newListSize : " + newListSize);
			
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.FILL_PARENT,
					newListSize); 
					//LinearLayout.LayoutParams.WRAP_CONTENT);
			//mLinearLayoutBackground.updateViewLayout(mListItems, params);
			mListItems.setLayoutParams(params);
		}
		*/
		
	}
		
	/**
	 * 
	 * Extend the SimpleCursorAdapter to strike through items.
	 * if STATUS == Shopping.Status.BOUGHT
	 * 
	 */
	public class mSimpleCursorAdapter extends SimpleCursorAdapter {

		/**
		 * Constructor simply calls super class.
		 * @param context Context.
		 * @param layout Layout.
		 * @param c Cursor.
		 * @param from Projection from.
		 * @param to Projection to.
		 */
		mSimpleCursorAdapter(final Context context, final int layout, 
				final Cursor c, final String[] from, final int[] to) {
			super(context, layout, c, from, to);
		}
		
		/**
		 * Additionally to the standard bindView, we also
		 * check for STATUS, and strike the item through if BOUGHT.
		 */
		@Override
		public void bindView(final View view, final Context context, 
				final Cursor cursor) {
			//Log.i(TAG, "bindView " + view.toString());
			super.bindView(view, context, cursor);
			
			TextView t = (TextView) view.findViewById(R.id.name);
			if (cursor.getLong(mStringItemsSTATUS) 
					== Shopping.Status.BOUGHT) {
				// We have bought the item,
				// so we strike it through:
				
				// First convert text to 'spannable'
				t.setText(t.getText(), TextView.BufferType.SPANNABLE);
				Spannable str = (Spannable) t.getText();
				
				// Strikethrough
				str.setSpan(new StrikethroughSpan(), 0, str.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				
				// apply color
				// TODO: How to get color from resource?
				//Drawable colorStrikethrough = context
				//	.getResources().getDrawable(R.drawable.strikethrough);
				str.setSpan(new ForegroundColorSpan(0xFF006600), 0,
						str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				// color: 0x33336600
				
			}
		}
		
	}
}

