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

import java.util.List;

import org.openintents.OpenIntents;
import org.openintents.distribution.AboutDialog;
import org.openintents.distribution.EulaActivity;
import org.openintents.distribution.UpdateMenu;
import org.openintents.provider.Alert;
import org.openintents.provider.Shopping;
import org.openintents.provider.Location.Locations;
import org.openintents.provider.Shopping.Contains;
import org.openintents.provider.Shopping.ContainsFull;
import org.openintents.provider.Shopping.Items;
import org.openintents.provider.Shopping.Lists;
import org.openintents.shopping.share.GTalkSender;
import org.openintents.util.MenuIntentOptionsWithIcons;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * 
 * Displays a shopping list.
 * 
 */
public class ShoppingActivity extends Activity { // implements
	// AdapterView.OnItemClickListener
	// {

	/**
	 * TAG for logging.
	 */
	private static final String TAG = "ShoppingView";

	private static final int MENU_NEW_LIST = Menu.FIRST;
	private static final int MENU_CLEAN_UP_LIST = Menu.FIRST + 1;
	private static final int MENU_DELETE_LIST = Menu.FIRST + 2;

	private static final int MENU_SHARE = Menu.FIRST + 3;
	private static final int MENU_THEME = Menu.FIRST + 4;

	private static final int MENU_ADD_LOCATION_ALERT = Menu.FIRST + 5;

	private static final int MENU_RENAME_LIST = Menu.FIRST + 6;

	private static final int MENU_MARK_ITEM = Menu.FIRST + 7;
	private static final int MENU_EDIT_ITEM = Menu.FIRST + 8; // includes rename
	private static final int MENU_DELETE_ITEM = Menu.FIRST + 9;

	private static final int MENU_SENSOR_SERVICE = Menu.FIRST + 10; // shake
	// control

	private static final int MENU_ABOUT = Menu.FIRST + 11;

	// TODO: Implement the following menu items
	private static final int MENU_EDIT_LIST = Menu.FIRST + 12; // includes
	// rename
	private static final int MENU_SORT = Menu.FIRST + 13; // sort alphabetically
	// or modified
	private static final int MENU_PICK_ITEMS = Menu.FIRST + 14; // pick from
	// previously
	// used items

	// TODO: Implement "select list" action
	// that can be called by other programs.
	private static final int MENU_SELECT_LIST = Menu.FIRST + 15; // select a
	// shopping list
	private static final int MENU_UPDATE = Menu.FIRST + 16;
	private static final int MENU_PREFERENCES = Menu.FIRST + 17;
	private static final int MENU_SEND = Menu.FIRST + 18;

	private static final int DIALOG_ABOUT = 1;

	/**
	 * The main activity.
	 * 
	 * Displays the shopping list that was used last time.
	 */
	private static final int STATE_MAIN = 0;

	/**
	 * VIEW action on a item/list URI.
	 */
	private static final int STATE_VIEW_LIST = 1;

	/**
	 * PICK action on an dir/item URI.
	 */
	private static final int STATE_PICK_ITEM = 2;

	/**
	 * GET_CONTENT action on an item/item URI.
	 */
	private static final int STATE_GET_CONTENT_ITEM = 3;

	/**
	 * Current state
	 */
	private int mState;

	/**
	 * URI of current list
	 */
	private Uri mListUri;

	/**
	 * URI of selected item
	 */
	private Uri mItemUri;

	/**
	 * Definition of the requestCode for the subactivity.
	 */
	static final private int SUBACTIVITY_LIST_SHARE_SETTINGS = 0;

	/**
	 * Definition for message handler:
	 */
	static final private int MESSAGE_UPDATE_CURSORS = 1;

	/**
	 * Update interval for automatic requeries.
	 * 
	 * (Workaround since ContentObserver does not work.)
	 */
	private int mUpdateInterval;

	private boolean mUpdating;

	private LinearLayout mLinearLayoutBackground;

	/**
	 * Sets how many pixels the EditBox shall be away from the bottom of the
	 * screen, when the height of the list-box is limited in checkListLength()
	 */
	private static int mBottomPadding = 50; // this is changed by

	/**
	 * Maximum number of lines on the screen. (should be calculated later, for
	 * now hardcoded.)
	 */
	private static int mMaxListCount = 6; // This value is changed by
	// setListTheme()

	/**
	 * Private members connected to Spinner ListFilter.
	 */
	private Spinner mSpinnerListFilter;
	private Cursor mCursorListFilter;
	private static final String[] mStringListFilter = new String[] { Lists._ID,
			Lists.NAME, Lists.IMAGE, Lists.SHARE_NAME, Lists.SHARE_CONTACTS,
			Lists.SKIN_BACKGROUND };
	private static final int mStringListFilterID = 0;
	private static final int mStringListFilterNAME = 1;
	private static final int mStringListFilterIMAGE = 2;
	private static final int mStringListFilterSHARENAME = 3;
	private static final int mStringListFilterSHARECONTACTS = 4;
	private static final int mStringListFilterSKINBACKGROUND = 5;

	private ListView mListItems;
	private Cursor mCursorItems;

	private static final String[] mStringItems = new String[] {
			ContainsFull._ID, ContainsFull.ITEM_NAME, ContainsFull.ITEM_IMAGE,
			ContainsFull.STATUS, ContainsFull.ITEM_ID,
			ContainsFull.SHARE_CREATED_BY, ContainsFull.SHARE_MODIFIED_BY };
	private static final int mStringItemsCONTAINSID = 0;
	private static final int mStringItemsITEMNAME = 1;
	private static final int mStringItemsITEMIMAGE = 2;
	private static final int mStringItemsSTATUS = 3;
	private static final int mStringItemsITEMID = 4;
	private static final int mStringItemsSHARECREATEDBY = 5;
	private static final int mStringItemsSHAREMODIFIEDBY = 6;

	private LinearLayout.LayoutParams mLayoutParamsItems;
	private int mAllowedListHeight; // Height for the list allowed in this view.

	private EditText mEditText;

	protected Context mDialogContext;
	protected Dialog mDialog;

	// TODO: Set up state information for onFreeze(), ...
	// State data to be stored when freezing:
	private final String ORIGINAL_ITEM = "original item";

	// Skins --------------------------
	public Typeface mTypeface;
	public Typeface mTypefaceHandwriting;
	public Typeface mTypefaceDigital;

	public boolean mUpperCaseFont;
	public int mTextColor;
	public float mTextSize;
	public int mMarkTextColor;

	public int mMarkType;
	public static final int mMarkCheckbox = 1;
	public static final int mMarkStrikethrough = 2;
	public static final int mMarkAddtext = 3;

	// GTalk --------------------------
	private GTalkSender mGTalkSender;

	// Sensor service -----------------

	// private SensorEventListener mSensorListener;

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		Log.i(TAG, "Shopping list onCreate()");

		if (!EulaActivity.checkEula(this)) {
			return;
		}
		// setTheme(android.R.style.Theme_White);
		// setTheme(android.R.style.Theme_Dialog);
		// setTheme(android.R.style.Theme_Dark);
		// setTheme(android.R.style.Theme_Black);
		// setTheme(android.R.style.Theme_Dark);
		setContentView(R.layout.shopping);

		// Initialize the convenience functions:
		Shopping.mContentResolver = getContentResolver();

		// Initialize GTalkSender (but don't bind yet!)
		mGTalkSender = new GTalkSender(this);

		// Automatic requeries (once a second)
		mUpdateInterval = 2000;
		mUpdating = false;

		// Sensor service
		// mSensorListener = new SensorEventListener(this);

		// General Uris:
		mListUri = Shopping.Lists.CONTENT_URI;
		mItemUri = Shopping.Items.CONTENT_URI;

		int defaultShoppingList = (int) Shopping.getDefaultList();

		// Handle the calling intent
		final Intent intent = getIntent();
		final String type = intent.resolveType(this);
		final String action = intent.getAction();

		if (action == null) {
			// Main action
			mState = STATE_MAIN;

			mListUri = Uri.withAppendedPath(Shopping.Lists.CONTENT_URI, ""
					+ defaultShoppingList);

			intent.setData(mListUri);
		} else if (Intent.ACTION_MAIN.equals(action)) {
			// Main action
			mState = STATE_MAIN;

			mListUri = Uri.withAppendedPath(Shopping.Lists.CONTENT_URI, ""
					+ defaultShoppingList);

			intent.setData(mListUri);

		} else if (Intent.ACTION_VIEW.equals(action)) {
			mState = STATE_VIEW_LIST;

			if (Shopping.ITEM_TYPE.equals(type)) {
				mListUri = Shopping.getListForItem(intent.getData()
						.getLastPathSegment());
			} else if (intent.getData() != null) {
				mListUri = intent.getData();
			}

		} else if (Intent.ACTION_PICK.equals(action)) {
			mState = STATE_PICK_ITEM;

			mListUri = Uri.withAppendedPath(Shopping.Lists.CONTENT_URI, ""
					+ defaultShoppingList);
		} else if (Intent.ACTION_GET_CONTENT.equals(action)) {
			mState = STATE_GET_CONTENT_ITEM;

			mListUri = Uri.withAppendedPath(Shopping.Lists.CONTENT_URI, ""
					+ defaultShoppingList);
		} else {
			// Unknown action.
			Log.e(TAG, "Shopping: Unknown action, exiting");
			finish();
			return;
		}

		// Read fonts
		mTypefaceHandwriting = Typeface.createFromAsset(getAssets(),
				"fonts/AnkeHand.ttf");
		mTypefaceDigital = Typeface.createFromAsset(getAssets(),
				"fonts/Crysta.ttf");
		mTypeface = null;
		mTypeface = mTypefaceHandwriting;

		// hook up all buttons, lists, edit text:
		createView();

		// Called after createView
		// (as we need the view already retrieved from the ids.
		setListTheme(1);

		// populate the lists
		fillListFilter();

		// Get last part of URI:
		int selectList;
		try {
			selectList = Integer.parseInt(mListUri.getLastPathSegment());
		} catch (NumberFormatException e) {
			selectList = defaultShoppingList;
		}

		// select the default shopping list at the beginning:
		setSelectedListId(selectList);
		// TODO: Select the last shopping list viewed
		// instead of the default shopping list.
		// (requires saving that information as
		// preference).

		// now fill all items
		fillItems();

		// Set the theme based on the selected list:
		setListTheme(loadListTheme());

		// Bind GTalk if currently selected shopping list needs it:
		bindGTalkIfNeeded();

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
		Log.i(TAG, "Shopping list onResume()");

		// Modify our overall title depending on the mode we are running in.
		if (mState == STATE_MAIN || mState == STATE_VIEW_LIST) {
			// App name is default
			// setTitle(getText(R.string.app_name));
		} else if ((mState == STATE_PICK_ITEM)
				|| (mState == STATE_GET_CONTENT_ITEM)) {
			setTitle(getText(R.string.pick_item));
			setTitleColor(0xFFAAAAFF);
		}

		checkListLength();

		setListTheme(loadListTheme());

		if (!mUpdating) {
			mUpdating = true;
			// mHandler.sendMessageDelayed(mHandler.obtainMessage(
			// MESSAGE_UPDATE_CURSORS), mUpdateInterval);
		}

		// fillItems();

		// TODO ???
		/*
		 * ??? // Bind GTalk service if (mGTalkSender != null) {
		 * bindGTalkIfNeeded(); }
		 * 
		 * // Register intent receiver for refresh intents: IntentFilter
		 * intentfilter = new IntentFilter(OpenIntents.REFRESH_ACTION);
		 * registerReceiver(mIntentReceiver, intentfilter);
		 */
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.i(TAG, "Shopping list onPause()");

		// TODO ???
		/*
		 * // Unregister refresh intent receiver
		 * unregisterReceiver(mIntentReceiver);
		 * 
		 * // Unbind GTalk service if (mGTalkSender != null) {
		 * mGTalkSender.unbindGTalkService(); }
		 */
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.i(TAG, "Shopping list onFreeze()");

		// Save original text from edit box
		String s = mEditText.getText().toString();
		outState.putString(ORIGINAL_ITEM, s);

		mUpdating = false;
	}

	/**
	 * Hook up buttons, lists, and edittext with functionality.
	 */
	private void createView() {
		mLinearLayoutBackground = (LinearLayout) findViewById(R.id.background);

		mSpinnerListFilter = (Spinner) findViewById(R.id.spinner_listfilter);
		mSpinnerListFilter
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView parent, View v,
							int position, long id) {
						fillItems();
						// Now set the theme based on the selected list:
						setListTheme(loadListTheme());
						checkListLength();
						bindGTalkIfNeeded();
					}

					public void onNothingSelected(AdapterView arg0) {
						fillItems();
						checkListLength();
					}
				});

		mEditText = (EditText) findViewById(R.id.edittext_add_item);
		mEditText.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(View v, int keyCode, KeyEvent key) {
				// Log.i(TAG, "KeyCode: " + keyCode
				// + " =?= "
				// +Integer.parseInt(getString(R.string.key_return)) );

				// Shortcut: Instead of pressing the button,
				// one can also press the "Enter" key.
				Log.i(TAG, "Key action: " + key.getAction());
				Log.i(TAG, "Key code: " + keyCode);
				if (key.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_ENTER) {
					insertNewItem();
					return true;
				}
				;
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
		mListItems.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView parent, View v, int pos, long id) {
				Log.i(TAG, "onItemClick: pos = " + pos);
				Cursor c = (Cursor) parent.getItemAtPosition(pos);
				if (mState == STATE_PICK_ITEM) {
					pickItem(c);
				} else {
					toggleItemBought(c);
				}
			}

		});
		mListItems.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView parent, View v,
					int position, long id) {
				// Log.i(TAG, "mListItems selected: pos:"
				// + position + ", id:" + id);
				checkListLength();
			}

			public void onNothingSelected(AdapterView arg0) {
				// TODO Auto-generated method stub
				checkListLength();
			}
		});
		mListItems
				.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

					public void onCreateContextMenu(ContextMenu contextmenu,
							View view, ContextMenuInfo info) {
						contextmenu.add(0, MENU_MARK_ITEM, 0,
								R.string.mark_item).setShortcut('1', 'm');
						contextmenu.add(0, MENU_EDIT_ITEM, 0,
								R.string.edit_item).setShortcut('2', 'e');
						contextmenu.add(0, MENU_DELETE_ITEM, 0,
								R.string.delete_item).setShortcut('3', 'd');
					}

				});
	}

	/**
	 * Inserts new item from edit box into currently selected shopping list.
	 */
	private void insertNewItem() {
		String newItem = mEditText.getText().toString();

		// Only add if there is something to add:
		if (newItem.compareTo("") != 0) {
			long listId = getSelectedListId();
			if (listId < 0) {
				// No valid list - probably view is not active
				// and no item is selected.
				return;
			}

			// mCursorListFilter has been set to correct position
			// by calling getSelectedListId(),
			// so we can read out further elements:
			String shareName = mCursorListFilter
					.getString(mStringListFilterSHARENAME);
			String recipients = mCursorListFilter
					.getString(mStringListFilterSHARECONTACTS);

			long itemId = Shopping.getItem(newItem);

			Log.i(TAG, "Insert new item. " + " itemId = " + itemId
					+ ", listId = " + listId);
			Shopping.addItemToList(itemId, listId);

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
			//http://groups.google.com/group/android-developers/browse_frm/thread
			// /3b2f4063a2221acb/36462ba1301a18c8
			/*
			 * int NUMBER_OF_ELEMENTS_BELOW_MIDDLE = 4; if
			 * (mListItems.getCount() > NUMBER_OF_ELEMENTS_BELOW_MIDDLE) {
			 * mListItems.setSelection(mListItems.getCount() -
			 * NUMBER_OF_ELEMENTS_BELOW_MIDDLE); } ;
			 */
			// mListItems.setSelection(mListItems.getCount() - 1);
			// mListItems.getChildAt(mListItems.getCount()-1).setSelected(true);
			// Set the item that we have just selected:
			// Get position of ID:
			mCursorItems.moveToPosition(-1);
			while (mCursorItems.moveToNext()) {
				if (mCursorItems.getLong(mStringItemsITEMID) == itemId) {
					int pos = mCursorItems.getPosition();
					if (pos > 0) {
						// Set selection one before, so that the item is fully
						// visible.
						mListItems.setSelection(pos - 1);
					} else {
						mListItems.setSelection(pos);
					}
					break;
				}
			}

			// If we share items, send this item also to other lists:
			// TODO ??
			/*
			 * if (! recipients.equals("")) { Log.i(TAG, "Share new item. " +
			 * " recipients: " + recipients + ", shareName: " + shareName +
			 * ", newItem: " + newItem); mGTalkSender.sendItem(recipients,
			 * shareName, newItem); }
			 */
		}
	}

	/**
	 * strike item through or undo this.
	 */
	private void toggleItemBought(Cursor c) {
		/*
		 * long listId = getSelectedListId(); if (listId < 0) { // No valid list
		 * - probably view is not active // and no item is selected. return; }
		 */

		// mCursorListFilter has been set to correct position
		// by calling getSelectedListId(),
		// so we can read out further elements:
		String shareName = mCursorListFilter
				.getString(mStringListFilterSHARENAME);
		String recipients = mCursorListFilter
				.getString(mStringListFilterSHARECONTACTS);
		String itemName = c.getString(mStringItemsITEMNAME);
		long oldstatus = c.getLong(mStringItemsSTATUS);

		// Toggle status:
		long newstatus = Shopping.Status.BOUGHT;
		if (oldstatus == Shopping.Status.BOUGHT) {
			newstatus = Shopping.Status.WANT_TO_BUY;
		}

		ContentValues values = new ContentValues();
		values.put(Shopping.Contains.STATUS, newstatus);
		Log.d(TAG, "update row " + c.getString(0) + ", newstatus " + newstatus);
		getContentResolver().update(
				Uri.withAppendedPath(Shopping.Contains.CONTENT_URI, c
						.getString(0)), values, null, null);

		// Log.i(TAG, "Requery now:");
		c.requery();

		// fillItems();

		mListItems.invalidate();

		// If we share items, send this item also to other lists:
		// TODO ???
		/*
		 * if (! recipients.equals("")) { Log.i(TAG, "Update shared item. " +
		 * " recipients: " + recipients + ", shareName: " + shareName +
		 * ", status: " + newstatus); mGTalkSender.sendItemUpdate(recipients,
		 * shareName, itemName, itemName, oldstatus, newstatus); }
		 */

	}

	/**
	 * Picks an item and returns to calling activity.
	 */
	private void pickItem(Cursor c) {
		long itemId = c.getLong(mStringItemsITEMID);
		Uri url = ContentUris
				.withAppendedId(Shopping.Items.CONTENT_URI, itemId);

		Intent intent = new Intent();
		intent.setData(url);
		setResult(RESULT_OK, intent);
		finish();
	}

	// Menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Standard menu
		menu.add(0, MENU_NEW_LIST, 0, R.string.new_list).setIcon(
				R.drawable.ic_menu_add_list).setShortcut('0', 'n');
		menu.add(0, MENU_CLEAN_UP_LIST, 0, R.string.clean_up_list).setIcon(
				R.drawable.ic_menu_clean_up).setShortcut('1', 'c');
		menu.add(0, MENU_RENAME_LIST, 0, R.string.rename_list).setIcon(
				android.R.drawable.ic_menu_edit).setShortcut('2', 'r');
		;
		menu.add(0, MENU_DELETE_LIST, 0, R.string.delete_list).setIcon(
				android.R.drawable.ic_menu_delete).setShortcut('3', 'd');

		/*
		 * menu.add(0, MENU_SHARE, 0, R.string.share)
		 * .setIcon(R.drawable.contact_share001a) .setShortcut('4', 's');
		 */

		menu.add(0, MENU_THEME, 0, R.string.theme).setIcon(
				android.R.drawable.ic_menu_manage).setShortcut('5', 't');

		menu.add(0, MENU_PREFERENCES, 0, R.string.preferences).setIcon(
				android.R.drawable.ic_menu_preferences);

		menu.add(0, MENU_SEND, 0, R.string.send).setIcon(
				android.R.drawable.ic_menu_send);

		if (addLocationAlertPossible()) {
			menu
					.add(0, MENU_ADD_LOCATION_ALERT, 0,
							R.string.shopping_add_alert).setIcon(
							android.R.drawable.ic_menu_mylocation).setShortcut(
							'6', 'l');
		}

		/*
		 * menu.add(0, MENU_SENSOR_SERVICE, 0, R.string.shake_control)
		 * .setIcon(R.drawable.mobile_shake001a) .setShortcut('0', 's');
		 */

		/*
		 * menu.add(0, MENU_SETTINGS, R.string.sensorsimulator_settings)
		 * .setShortcut('0', 's'); menu.add(0, MENU_CONNECT_SIMULATOR,
		 * R.string.connect_to_sensorsimulator) .setShortcut('1', 'c');
		 */

		UpdateMenu
				.addUpdateMenu(this, menu, 0, MENU_UPDATE, 0, R.string.update);
		menu.add(0, MENU_ABOUT, 0, R.string.about).setIcon(
				android.R.drawable.ic_menu_info_details).setShortcut('0', 'a');

		/*
		 * // Generate any additional actions that can be performed on the //
		 * overall list. This allows other applications to extend // our menu
		 * with their own actions. Intent intent = new Intent(null,
		 * getIntent().getData());
		 * intent.addCategory(Intent.ALTERNATIVE_CATEGORY);
		 * menu.addIntentOptions( Menu.ALTERNATIVE, 0, new ComponentName(this,
		 * ShoppingView.class), null, intent, 0, null);
		 */

		/*
		 * // Generate any additional actions that can be performed on the //
		 * overall list. This allows other applications to extend // our menu
		 * with their own actions. Intent intent = new Intent(null,
		 * getIntent().getData());
		 * intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		 * //menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, // new
		 * ComponentName(this, NoteEditor.class), null, intent, 0, null);
		 * 
		 * // Workaround to add icons: MenuIntentOptionsWithIcons menu2 = new
		 * MenuIntentOptionsWithIcons(this, menu);
		 * menu2.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new
		 * ComponentName(this, ShoppingActivity.class), null, intent, 0, null);
		 */

		// Set checkable items:
		// TODO SDK 0.9???
		// menu.setItemCheckable(MENU_CONNECT_SIMULATOR, true);
		return true;
	}

	/**
	 * Check whether an application exists that handles the pick activity.
	 * 
	 * @return
	 */
	private boolean addLocationAlertPossible() {

		// Test whether intent exists for picking a location:
		PackageManager pm = getPackageManager();
		Intent intent = new Intent(Intent.ACTION_PICK, Locations.CONTENT_URI);
		List<ResolveInfo> resolve_pick_location = pm.queryIntentActivities(
				intent, PackageManager.MATCH_DEFAULT_ONLY);
		/*
		 * for (int i = 0; i < resolve_pick_location.size(); i++) { Log.d(TAG,
		 * "Activity name: " + resolve_pick_location.get(i).activityInfo.name);
		 * }
		 */

		// Check whether adding alerts is possible.
		intent = new Intent(Intent.ACTION_VIEW, Alert.Generic.CONTENT_URI);
		List<ResolveInfo> resolve_view_alerts = pm.queryIntentActivities(
				intent, PackageManager.MATCH_DEFAULT_ONLY);

		boolean pick_location_possible = (resolve_pick_location.size() > 0);
		boolean view_alerts_possible = (resolve_view_alerts.size() > 0);
		Log.d(TAG, "Pick location possible: " + pick_location_possible);
		Log.d(TAG, "View alerts possible: " + view_alerts_possible);
		if (pick_location_possible && view_alerts_possible) {
			return true;
		}

		return false;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		// TODO: Add item-specific menu items (see NotesList.java example)
		// like edit, strike-through, delete.

		// Selected list:
		long listId = getSelectedListId();

		// Delete list is possible, if we have more than one list:
		// AND
		// the current list is not the default list (listId == 0) - issue #105
		// TODO: Later, the default list should be user-selectable,
		// and not deletable.

		// TODO ???
		/*
		 * menu.setItemShown(MENU_DELETE_LIST, mCursorListFilter.count() > 1 &&
		 * listId != 1); // 1 is hardcoded number of default first list.
		 */

		// Set additional menu items that can be used with the
		// currently selected list.
		// TODO ???
		/*
		 * Intent intent = new Intent(null, mListUri);
		 * intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		 * menu.addIntentOptions( Menu.CATEGORY_ALTERNATIVE, 0, new
		 * ComponentName(this, ShoppingView.class), null, intent, 0, null);
		 * 
		 * menu.setItemChecked(MENU_CONNECT_SIMULATOR,
		 * SensorsPlus.isConnectedSimulator());
		 */

		// The following code is put from onCreateOptionsMenu to
		// onPrepareOptionsMenu,
		// because the URI of the shopping list can change if the user switches
		// to
		// another list.
		// Generate any additional actions that can be performed on the
		// overall list. This allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		// menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
		// new ComponentName(this, NoteEditor.class), null, intent, 0, null);

		// Workaround to add icons:
		MenuIntentOptionsWithIcons menu2 = new MenuIntentOptionsWithIcons(this,
				menu);
		menu2.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
				new ComponentName(this, ShoppingActivity.class), null, intent,
				0, null);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case MENU_NEW_LIST:
			showListDialog(MENU_NEW_LIST);
			return true;

		case MENU_CLEAN_UP_LIST:
			cleanupList();
			return true;

		case MENU_RENAME_LIST:
			showListDialog(MENU_RENAME_LIST);
			return true;

		case MENU_DELETE_LIST:
			deleteListConfirm();
			return true;

		case MENU_SHARE:
			setShareSettings();
			return true;

		case MENU_THEME:
			setThemeSettings();
			return true;

		case MENU_ADD_LOCATION_ALERT:
			addLocationAlert();
			return true;

		case MENU_SENSOR_SERVICE:
			// toggleSensorService();
			return true;

		case MENU_UPDATE:
			UpdateMenu.showUpdateBox(this);
			return true;

		case MENU_ABOUT:
			showAboutBox();
			return true;

		case MENU_PREFERENCES:
			intent = new Intent(this, PreferenceActivity.class);
			startActivity(intent);
			return true;
		case MENU_SEND:
			sendList();
			/*
			 * case MENU_SETTINGS: Intent intent = new
			 * Intent(Intent.MAIN_ACTION, Hardware.Preferences.CONTENT_URI);
			 * startActivity(intent); return true;
			 * 
			 * case MENU_CONNECT_SIMULATOR: // check if accelerometer is
			 * supported: if
			 * (SensorsPlus.isSupportedSensor(Sensors.SENSOR_ACCELEROMETER)) {
			 * // first disable the current sensors:
			 * Sensors.disableSensor(Sensors.SENSOR_ACCELEROMETER); }
			 * 
			 * if (!SensorsPlus.isConnectedSimulator()) { // now connect to
			 * simulator Sensors.connectSimulator(); } else { // or disconnect
			 * to simulator Sensors.disconnectSimulator(); }
			 * 
			 * // check if accelerometer is supported: if
			 * (SensorsPlus.isSupportedSensor(Sensors.SENSOR_ACCELEROMETER)) {
			 * // enable the sensor:
			 * Sensors.enableSensor(Sensors.SENSOR_ACCELEROMETER); }
			 * 
			 * return true;
			 */
		}
		return super.onOptionsItemSelected(item);

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case MENU_MARK_ITEM:
			markItem(menuInfo.position);
			break;
		case MENU_EDIT_ITEM:
			editItem(menuInfo.position);
			break;
		case MENU_DELETE_ITEM:
			deleteItem(menuInfo.position);
			break;
		}

		return true;
	}

	// /////////////////////////////////////////////////////
	//
	// Menu functions
	//

	/**
	 * Opens a dialog to add a new shopping list or to rename it.
	 */
	private void showListDialog(final int menuAction) {

		// TODO Shall we implement this as action?
		// Then other applications may call this as well.

		mDialog = new Dialog(ShoppingActivity.this);

		mDialog.setContentView(R.layout.input_box);

		EditText et = (EditText) mDialog.findViewById(R.id.edittext);
		// et.setText(getString(R.string.new_list));
		et.setHint(getString(R.string.new_list_hint));
		et.selectAll();

		switch (menuAction) {
		case MENU_NEW_LIST:
			mDialog.setTitle(getString(R.string.ask_new_list));
			break;
		case MENU_RENAME_LIST:
			mDialog.setTitle(getString(R.string.ask_rename_list));

			if (mCursorListFilter != null
					&& mCursorListFilter.getPosition() >= 0) {
				et.setText(mCursorListFilter.getString(mStringListFilterNAME));
			}
			break;
		case MENU_EDIT_ITEM:
			mDialog.setTitle(getString(R.string.ask_edit_item));

			// Cursor is supposed to be set to correct row already:
			et.setText(mCursorItems.getString(mStringItemsITEMNAME));
			break;
		}

		// Accept OK also when user hits "Enter"
		et.setOnKeyListener(new OnKeyListener() {

			public boolean onKey(final View v, final int keyCode,
					final KeyEvent key) {
				// Log.i(TAG, "KeyCode: " + keyCode);

				if (key.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_ENTER) {
					doListDialogAction(menuAction);
					return true;
				}
				return false;
			}

		});

		Button bOk = (Button) mDialog.findViewById(R.id.ok);
		bOk.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				doListDialogAction(menuAction);
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

	void doListDialogAction(int menuAction) {
		switch (menuAction) {
		case MENU_NEW_LIST:
			if (createNewList()) {
				// New list created. Exit:
				mDialog.dismiss();
			}
			break;
		case MENU_RENAME_LIST:
			if (renameList()) {
				// Rename successful. Exit:
				mDialog.dismiss();
			}
			break;
		case MENU_EDIT_ITEM:
			if (renameItem()) {
				// Rename successful. Exit:
				mDialog.dismiss();
			}
			break;
		}
	}

	/**
	 * Creates a new list from dialog.
	 * 
	 * @return true if new list was created. False if new list was not created,
	 *         because user has not given any name.
	 */
	private boolean createNewList() {
		EditText edittext = (EditText) mDialog.findViewById(R.id.edittext);
		String s = edittext.getText().toString();

		if (s.equals("")) {
			// User has not provided any name
			Toast.makeText(this, getString(R.string.please_enter_name),
					Toast.LENGTH_SHORT).show();
			return false;
		}

		int newId = (int) Shopping.getList(s);

		edittext.setText("");
		fillListFilter();

		setSelectedListId(newId);

		// Now set the theme based on the selected list:
		setListTheme(loadListTheme());

		// A newly created list will not yet be shared via GTalk:
		// bindGTalkIfNeeded();
		return true;
	}

	/**
	 * Rename list from dialog.
	 * 
	 * @return true if new list was renamed. False if new list was not renamed,
	 *         because user has not given any name.
	 */
	private boolean renameList() {
		EditText edittext = (EditText) mDialog.findViewById(R.id.edittext);
		String s = edittext.getText().toString();

		if (s.equals("")) {
			// User has not provided any name
			Toast.makeText(this, getString(R.string.please_enter_name),
					Toast.LENGTH_SHORT).show();
			return false;
		}

		// Rename currently selected list:
		ContentValues values = new ContentValues();
		values.put(Lists.NAME, "" + s);
		getContentResolver().update(
				Uri.withAppendedPath(Lists.CONTENT_URI, mCursorListFilter
						.getString(0)), values, null, null);

		mCursorListFilter.requery();

		edittext.setText("");

		return true;
	}

	/**
	 * Rename item from dialog.
	 * 
	 * @return true if new list was renamed. False if new list was not renamed,
	 *         because user has not given any name.
	 */
	private boolean renameItem() {
		EditText edittext = (EditText) mDialog.findViewById(R.id.edittext);
		String s = edittext.getText().toString();

		if (s.equals("")) {
			// User has not provided any name
			Toast.makeText(this, getString(R.string.please_enter_name),
					Toast.LENGTH_SHORT).show();
			return false;
		}

		String oldItemName = mCursorItems.getString(mStringItemsITEMNAME);
		String newItemName = s;

		// Rename currently selected item:
		/*
		 * mCursorItems.updateString(mStringItemsITEMNAME, newItemName);
		 * 
		 * mCursorItems.commitUpdates(); mCursorItems.requery();
		 * 
		 * edittext.setText("");
		 */
		// Can not work on ContainsFull table, have to work on items table:
		long itemId = mCursorItems.getLong(mStringItemsITEMID);

		Cursor cursor = getContentResolver()
				.query(Shopping.Items.CONTENT_URI, Shopping.Items.PROJECTION,
						Shopping.Items._ID + " = ?",
						new String[] { "" + itemId },
						Shopping.Items.DEFAULT_SORT_ORDER);
		if (cursor != null && cursor.moveToNext()) {
			// Rename item in items table
			ContentValues values = new ContentValues();
			values.put(Items.NAME, newItemName);
			getContentResolver().update(
					Uri
							.withAppendedPath(Items.CONTENT_URI, cursor
									.getString(0)), values, null, null);

		} else {
			Log.e(TAG, "ShoppingView: Could not rename item.");
		}
		mCursorItems.requery();

		// If we share items, send this item also to other lists:
		// TODO ???
		/*
		 * String recipients =
		 * mCursorListFilter.getString(mStringListFilterSHARECONTACTS); if (!
		 * recipients.equals("")) { String shareName =
		 * mCursorListFilter.getString(mStringListFilterSHARENAME); long status
		 * = mCursorItems.getLong(mStringItemsSTATUS);
		 * 
		 * mGTalkSender.sendItemUpdate(recipients, shareName, oldItemName,
		 * newItemName, status, status); }
		 */

		return true;
	}

	private void sendList() {
		if (mListItems.getAdapter() instanceof CursorAdapter) {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < mListItems.getAdapter().getCount(); i++) {
				Cursor item = (Cursor) mListItems.getAdapter().getItem(i);
				if (item.getLong(mStringItemsSTATUS) == Shopping.Status.BOUGHT) {
					sb.append("[X]");
				} else {
					sb.append("[ ]");
				}
				sb.append(item.getString(mStringItemsITEMNAME));
				sb.append("\n");
			}
			
			Intent i = new Intent();
			i.setAction(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_SUBJECT, ((Cursor)mSpinnerListFilter.getSelectedItem()).getString(mStringListFilterNAME));
			i.putExtra(Intent.EXTRA_TEXT, sb.toString());

			try {
				startActivity(Intent.createChooser(i, getString(R.string.send)));
			} catch (ActivityNotFoundException e) {
				Toast.makeText(this, R.string.email_not_available,
						Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Email client not installed");
			}
		} else {
			Toast.makeText(this, R.string.empty_list_not_sent, Toast.LENGTH_SHORT);
		}

	}

	/**
	 * Clean up the currently visible shopping list by removing items from list
	 * that are marked BOUGHT.
	 */
	private void cleanupList() {
		// Delete all items from current list
		// which have STATUS = Status.BOUGHT

		// TODO One could write one SQL statement to delete all at once.
		// But as long as shopping lists stay small, it should not matter.
		String listId = mCursorListFilter.getString(0);
		boolean nothingdeleted = true;
		nothingdeleted = getContentResolver().delete(
				Shopping.Contains.CONTENT_URI,
				Shopping.Contains.LIST_ID + " = " + listId + " AND "
						+ Shopping.Contains.STATUS + " = "
						+ Shopping.Status.BOUGHT, null) == 0;
		mCursorItems.requery();

		if (nothingdeleted) {
			// Show toast
			Toast.makeText(this, R.string.no_items_marked, Toast.LENGTH_SHORT)
					.show();

			// Show dialog:
			/*
			 * AlertDialog.show(ShoppingView.this,
			 * getString(R.string.clean_up_list), 0, // TODO choose IconID
			 * getString(R.string.no_items_marked), getString(R.string.ok),
			 * false);
			 */
		} else {
			checkListLength();
		}
	}

	/**
	 * Confirm 'delete list' command by AlertDialog.
	 */
	private void deleteListConfirm() {
		new AlertDialog.Builder(this)
		// .setIcon(R.drawable.alert_dialog_icon)
				.setTitle(R.string.delete_list).setPositiveButton(R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// click Ok
								deleteList();
							}
						}).setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								// click Cancel
							}
						})
				// .create()
				.show();
	}

	/**
	 * Deletes currently selected shopping list.
	 */
	private void deleteList() {
		String listId = mCursorListFilter.getString(0);
		// First delete all items in list
		getContentResolver().delete(Contains.CONTENT_URI,
				"list_id = " + listId, null);

		// Then delete currently selected list
		getContentResolver().delete(Lists.CONTENT_URI, "_id = " + listId, null);

		// Update view
		fillListFilter();
		fillItems();

		// Now set the theme based on the selected list:
		setListTheme(loadListTheme());

		bindGTalkIfNeeded();
	}

	/** Mark item */
	void markItem(int position) {
		mCursorItems.moveToPosition(position);
		toggleItemBought(mCursorItems);
	}

	/** Edit item */
	void editItem(int position) {
		mCursorItems.moveToPosition(position);
		showListDialog(MENU_EDIT_ITEM);
	}

	/** Delete item */
	void deleteItem(int position) {
		// Remember old values before delete (for share below)
		String itemName = mCursorItems.getString(mStringItemsITEMNAME);
		long oldstatus = mCursorItems.getLong(mStringItemsSTATUS);

		// Delete item
		// getContentResolver().delete(Items.CONTENT_URI, "_id = ?", new
		// String[]{mCursorItems.getString(0)});
		getContentResolver()
				.delete(
						Contains.CONTENT_URI,
						"_id = ?",
						new String[] { mCursorItems
								.getString(mStringItemsCONTAINSID) });
		mCursorItems.requery();

		// If we share items, mark item on other lists:
		// TODO ???
		/*
		 * String recipients =
		 * mCursorListFilter.getString(mStringListFilterSHARECONTACTS); if (!
		 * recipients.equals("")) { String shareName =
		 * mCursorListFilter.getString(mStringListFilterSHARENAME); long
		 * newstatus = Shopping.Status.BOUGHT;
		 * 
		 * Log.i(TAG, "Update shared item. " + " recipients: " + recipients +
		 * ", shareName: " + shareName + ", status: " + newstatus);
		 * mGTalkSender.sendItemUpdate(recipients, shareName, itemName,
		 * itemName, oldstatus, newstatus); }
		 */

	}

	/**
	 * Calls the share settings with the currently selected list.
	 */
	void setShareSettings() {
		// Obtain URI of current list

		// Call share settings as subactivity
		Intent intent = new Intent(OpenIntents.SET_SHARE_SETTINGS_ACTION,
				mListUri);
		startActivityForResult(intent, SUBACTIVITY_LIST_SHARE_SETTINGS);

		// Also, start to bind as we will likely need it:
		// TODO ???
		/*
		 * if (mGTalkSender != null) { mGTalkSender.bindGTalkService(); }
		 */
	}

	/**
	 * Displays a dialog to select a theme for the current shopping list.
	 */
	void setThemeSettings() {

		/* Display a custom progress bar */
		LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflate.inflate(R.layout.shopping_theme_settings,
				null);

		final RadioGroup radiogroup = (RadioGroup) view
				.findViewById(R.id.radiogroup);

		// Set Theme according to database
		radiogroup.check(R.id.radio1);

		int themeId = loadListTheme();
		switch (themeId) {
		case 1:
			radiogroup.check(R.id.radio1);
			break;
		case 2:
			radiogroup.check(R.id.radio2);
			break;
		case 3:
			radiogroup.check(R.id.radio3);
			break;
		}

		radiogroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {
						case R.id.radio1:
							setListTheme(1);
							break;
						case R.id.radio2:
							setListTheme(2);
							break;
						case R.id.radio3:
							setListTheme(3);
							break;

						}
					}
				});

		new AlertDialog.Builder(ShoppingActivity.this).setIcon(
				android.R.drawable.ic_menu_manage)
				.setTitle(R.string.theme_pick).setView(view).setPositiveButton(
						R.string.ok, new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked Yes so do some stuff */
								int r = radiogroup.getCheckedRadioButtonId();
								int themeId = 0;
								switch (r) {
								case R.id.radio1:
									themeId = 1;
									break;
								case R.id.radio2:
									themeId = 2;
									break;
								case R.id.radio3:
									themeId = 3;
									break;
								}
								saveListTheme(themeId);
								setListTheme(themeId);

							}
						}).setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked No so do some stuff */
								int themeId = loadListTheme();
								setListTheme(themeId);
							}
						}).setOnCancelListener(new OnCancelListener() {

					public void onCancel(DialogInterface arg0) {
						int themeId = loadListTheme();
						setListTheme(themeId);
					}
				}).show();

	}

	/**
	 * Set theme according to Id.
	 * 
	 * @param themeId
	 */
	void setListTheme(int themeId) {
		int textSize = getDefaultTextSize();
		switch (themeId) {
		case 1:
			mTypeface = null;
			mUpperCaseFont = false;
			mTextColor = 0xffffffff; // white

			if (textSize == 1) {
				mTextSize = 18;
			} else if (textSize == 2) {
				mTextSize = 23;
			} else {
				mTextSize = 28;
			}

			mMarkTextColor = 0xffcccccc; // white gray
			mMarkType = mMarkCheckbox;

			mLinearLayoutBackground.setPadding(0, 0, 0, 0);
			mLinearLayoutBackground.setBackgroundDrawable(null);

			mMaxListCount = 5;
			mBottomPadding = 50;

			break;
		case 2:
			mTypeface = mTypefaceHandwriting;
			mUpperCaseFont = false;
			mTextColor = 0xff000000; // black
			if (textSize == 1) {
				mTextSize = 15;
			} else if (textSize == 2) {
				mTextSize = 20;
			} else {
				mTextSize = 25;
			}

			mMarkTextColor = 0xff008800; // dark green
			mMarkType = mMarkStrikethrough;

			// 9-patch drawable defines padding by itself
			mLinearLayoutBackground
					.setBackgroundResource(R.drawable.shoppinglist01d);

			mMaxListCount = 5;
			mBottomPadding = 85;

			break;
		case 3:
			mTypeface = mTypefaceDigital;

			// Digital only supports upper case fonts.
			mUpperCaseFont = true;
			mTextColor = 0xffff0000; // red
			if (textSize == 1) {
				mTextSize = 21;
			} else if (textSize == 2) {
				mTextSize = 26;
			} else {
				mTextSize = 31;
			}

			mMarkTextColor = 0xff00ff00; // light green
			mMarkType = mMarkAddtext;

			mLinearLayoutBackground.setPadding(0, 0, 0, 0);
			mLinearLayoutBackground
					.setBackgroundResource(R.drawable.theme_android);

			mMaxListCount = 7;
			mBottomPadding = 50;

			break;
		}

		mListItems.invalidate();
		if (mCursorItems != null) {
			mCursorItems.requery();
		}
	}

	/**
	 * @return
	 */
	private int getDefaultTextSize() {
		return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(
				this).getString(PreferenceActivity.PREFS_FONTSIZE,
				PreferenceActivity.PREFS_FONTSIZE_DEFAULT));
	}

	/**
	 * Loads the theme settings for the currently selected theme.
	 * 
	 * Currently only one of 3 hardcoded themes are available. These are stored
	 * in 'skin_background' as '1', '2', or '3'.
	 * 
	 * @return
	 */
	public int loadListTheme() {
		/*
		 * long listId = getSelectedListId(); if (listId < 0) { // No valid list
		 * - probably view is not active // and no item is selected. return 1;
		 * // return default theme }
		 */

		// Return default theme if something unexpected happens:
		if (mCursorListFilter == null)
			return 1;
		if (mCursorListFilter.getPosition() < 0)
			return 1;

		// mCursorListFilter has been set to correct position
		// by calling getSelectedListId(),
		// so we can read out further elements:
		String skinBackground = mCursorListFilter
				.getString(mStringListFilterSKINBACKGROUND);

		int themeId;
		try {
			themeId = Integer.parseInt(skinBackground);
		} catch (NumberFormatException e) {
			themeId = 1;
		}
		if (themeId < 1 || themeId > 3) {
			themeId = 1;
		}
		return themeId;
	}

	public void saveListTheme(int themeId) {
		long listId = getSelectedListId();
		if (listId < 0) {
			// No valid list - probably view is not active
			// and no item is selected.
			return; // return default theme
		}

		if (themeId < 1 || themeId > 3) {
			themeId = 1;
		}

		ContentValues values = new ContentValues();
		values.put(Lists.SKIN_BACKGROUND, "" + themeId);
		getContentResolver().update(
				Uri.withAppendedPath(Lists.CONTENT_URI, mCursorListFilter
						.getString(0)), values, null, null);

		mCursorListFilter.requery();
	}

	/**
	 * Calls a dialog for setting the locations alert.
	 */
	void addLocationAlert() {

		// Call dialog as activity
		Intent intent = new Intent(OpenIntents.ADD_LOCATION_ALERT_ACTION,
				mListUri);
		// startSubActivity(intent, SUBACTIVITY_ADD_LOCATION_ALERT);
		startActivity(intent);
	}

	/**
	 * Turns on or off sensor service (shake control).
	 */
	/*
	 * void toggleSensorService() { if (!mSensorListener.isBound()) {
	 * mSensorListener.bindService();
	 * mSensorListener.setOnSensorListener(mOnSensorListener); } else {
	 * mSensorListener.unbindService();
	 * mSensorListener.setOnSensorListener(null); } }
	 */

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_ABOUT:
			return new AboutDialog(this);
		}
		return null;

	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);

		switch (id) {
		case DIALOG_ABOUT:
			break;
		}
	}

	private void showAboutBox() {
		AboutDialog.showDialogOrStartActivity(this, DIALOG_ABOUT);
	}

	// /////////////////////////////////////////////////////
	//
	// Helper functions
	//
	/**
	 * Returns the ID of the selected shopping list.
	 * 
	 * As a side effect, the item URI is updated. Returns -1 if nothing is
	 * selected.
	 * 
	 * @return ID of selected shopping list.
	 */
	private long getSelectedListId() {
		int pos = mSpinnerListFilter.getSelectedItemPosition();
		if (pos < 0) {
			// nothing selected - probably view is out of focus:
			// Do nothing.
			return -1;
		}

		// Obtain Id of currently selected shopping list:
		mCursorListFilter.moveToPosition(pos);

		long listId = mCursorListFilter.getLong(mStringListFilterID);

		mListUri = Uri
				.withAppendedPath(Shopping.Lists.CONTENT_URI, "" + listId);

		getIntent().setData(mListUri);

		return listId;
	};

	/**
	 * sets the selected list to a specific list Id
	 */
	private void setSelectedListId(int id) {
		// Is there a nicer way to accomplish the following?
		// (we look through all elements to look for the
		// one entry that has the same ID as returned by
		// getDefaultList()).
		//
		// unfortunately, a SQL query won't work, as it would
		// return 1 row, but I still would not know which
		// row in the mCursorListFilter corresponds to that id.
		//
		// one could use: findViewById() but how will this
		// translate to the position in the list?
		mCursorListFilter.moveToPosition(-1);
		while (mCursorListFilter.moveToNext()) {
			int posId = mCursorListFilter.getInt(mStringListFilterID);
			if (posId == id) {
				int row = mCursorListFilter.getPosition();

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
				mStringListFilter, null, null, Lists.DEFAULT_SORT_ORDER);
		startManagingCursor(mCursorListFilter);

		if (mCursorListFilter == null) {
			Log.e(TAG, "missing shopping provider");

			mSpinnerListFilter.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_spinner_item,
					new String[] { getString(R.string.no_shopping_provider) }));
			return;
		}

		if (mCursorListFilter.getCount() < 1) {
			// We have to create default shopping list:
			// TODO Put the following string into resource my_shopping_list
			long listId = Shopping.getList(getText(R.string.my_shopping_list)
					.toString());

			// Check if insertion really worked. Otherwise
			// we may end up in infinite recursion.
			if (listId < 0) {
				// for some reason insertion did not work.
				return;
			}

			// The insertion should have worked, so let us call ourselves
			// to try filling the list again:
			fillListFilter();
			return;
		}

		class mListContentObserver extends ContentObserver {

			public mListContentObserver(Handler handler) {
				super(handler);
				Log.i(TAG, "mListContentObserver: Constructor");
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.database.ContentObserver#deliverSelfNotifications()
			 */
			@Override
			public boolean deliverSelfNotifications() {
				// TODO Auto-generated method stub
				Log.i(TAG, "mListContentObserver: deliverSelfNotifications");
				return super.deliverSelfNotifications();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see android.database.ContentObserver#onChange(boolean)
			 */
			@Override
			public void onChange(boolean arg0) {
				// TODO Auto-generated method stub
				Log.i(TAG, "mListContentObserver: onChange");

				mCursorListFilter.requery();

				super.onChange(arg0);
			}

		}
		;

		mCursorListFilter.registerContentObserver(new mListContentObserver(
				new Handler()));

		// Register a ContentObserver, so that a new list can be
		// automatically detected.
		// mCursor

		/*
		 * ArrayList<String> list = new ArrayList<String>(); // TODO Create
		 * summary of all lists // list.add(ALL); while
		 * (mCursorListFilter.next()) {
		 * list.add(mCursorListFilter.getString(mStringListFilterNAME)); }
		 * ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		 * android.R.layout.simple_spinner_item, list);
		 * adapter.setDropDownViewResource(
		 * android.R.layout.simple_spinner_dropdown_item);
		 * mSpinnerListFilter.setAdapter(adapter);
		 */

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				// Use a template that displays a text view
				android.R.layout.simple_spinner_item,
				// Give the cursor to the list adapter
				mCursorListFilter, new String[] { Lists.NAME },
				new int[] { android.R.id.text1 });
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mSpinnerListFilter.setAdapter(adapter);

	}

	private void fillItems() {
		// Log.i(TAG, "Starting fillItems()");

		long listId = getSelectedListId();
		if (listId < 0) {
			// No valid list - probably view is not active
			// and no item is selected.
			return;
		}

		String sortOrder = PreferenceActivity.getSortOrderFromPrefs(this);
		// Older default: ContainsFull.DEFAULT_SORT_ORDER

		// Get a cursor for all items that are contained
		// in currently selected shopping list.
		mCursorItems = getContentResolver().query(ContainsFull.CONTENT_URI,
				mStringItems, "list_id = " + listId, null, sortOrder);
		startManagingCursor(mCursorItems);

		// Activate the following for a striped list.
		// setupListStripes(mListItems, this);

		if (mCursorItems == null) {
			Log.e(TAG, "missing shopping provider");
			mListItems.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no shopping provider" }));
			return;
		}

		int layout_row = R.layout.shopping_item_row;

		int textSize = getDefaultTextSize();
		if (textSize < 3) {
			layout_row = R.layout.shopping_item_row_small;
		}

		ListAdapter adapter = new mSimpleCursorAdapter(
				this,
				// Use a template that displays a text view
				layout_row,
				// Give the cursor to the list adapter
				mCursorItems,
				// Map the IMAGE and NAME to...
				new String[] { ContainsFull.ITEM_NAME, ContainsFull.ITEM_IMAGE },
				// the view defined in the XML template
				new int[] { R.id.name, R.id.image_URI });
		mListItems.setAdapter(adapter);

	}

	/**
	 * Add stripes to the a list view.
	 * 
	 * @param listView
	 * @param activity
	 */
	public static void setupListStripes(ListView listView, Activity activity) {
		// Get Drawables for alternating stripes
		Drawable[] lineBackgrounds = new Drawable[2];

		lineBackgrounds[0] = activity.getResources().getDrawable(
				R.drawable.gold);
		lineBackgrounds[1] = activity.getResources().getDrawable(
				R.drawable.yellow_green);

		// Make and measure a sample TextView of the sort our adapter will
		// return
		View view = activity.getLayoutInflater().inflate(
				android.R.layout.simple_list_item_1, null);

		TextView v = (TextView) view.findViewById(android.R.id.text1);
		v.setText("X");
		// Make it 100 pixels wide, and let it choose its own height.
		v.measure(View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.EXACTLY,
				100), View.MeasureSpec.makeMeasureSpec(
				View.MeasureSpec.UNSPECIFIED, 0));
		int height = v.getMeasuredHeight();

		// TODO SDK 0.9?
		// listView.setStripes(lineBackgrounds, height);
	}

	/**
	 * This function checks the length of the list. If the number of items is
	 * too large, the height of the list is limited, such that the EditText
	 * element will not drop out of the view.
	 */
	private void checkListLength() {
		if (true)
			return;

		Log.i(TAG, "checkListLength()");
		/*
		 * // Now we check whether we reach the lower border already: int[]
		 * locEditText = new int[2];
		 * mEditText.getAbsoluteLocationOnScreen(locEditText); int bottomEdit =
		 * locEditText[1] + mEditText.getBottom();
		 * 
		 * int[] locBackground = new int[2];
		 * mLinearLayoutBackground.getAbsoluteLocationOnScreen(locBackground);
		 * int bottomBackground = locBackground[1] +
		 * mLinearLayoutBackground.getBottom();
		 */
		// number of items in the list
		// int count = mListItems.getCount();
		int count = mCursorItems.getCount();

		// Let's hardcode the number of items:
		if (count <= mMaxListCount) {
			mLayoutParamsItems.height = LinearLayout.LayoutParams.WRAP_CONTENT;
			// mEditText.append("l");
		} else {
			// mEditText.append("m");
			WindowManager w = getWindowManager();
			Display d = w.getDefaultDisplay();
			int width = d.getWidth();
			int height = d.getHeight();

			mAllowedListHeight =
			// mLinearLayoutBackground.getHeight()
			height
					// - mListItems.getTop()
					- mSpinnerListFilter.getHeight() - mEditText.getHeight()
					- mBottomPadding;
			if (mAllowedListHeight < 0) {
				mAllowedListHeight = 0;
			}
			// we have to limit the height:
			mLayoutParamsItems.height = mAllowedListHeight;
		}

		mListItems.setLayoutParams(mLayoutParamsItems);

		/*
		 * 
		 * if (count < 1) { mLayoutParamsItems.height =
		 * LinearLayout.LayoutParams.WRAP_CONTENT; } else { // for now, let us
		 * take the height of // the first element and multiply by count. //
		 * TODO later: Some items may be larger // (if they contain images). //
		 * How can we best determine the size // there? //int singleHeight =
		 * mListItems.getChildAt(0).getHeight(); int listHeight =
		 * mListItems.getHeight(); Log.i(TAG, "listHeight: " + listHeight);
		 * 
		 * int editTextHeight = mEditText.getHeight();
		 * 
		 * // calculate the allowed height of the list // TODO: could be moved
		 * to initialization after freeze? mAllowedListHeight =
		 * mLinearLayoutBackground.getHeight() - mListItems.getTop() -
		 * editTextHeight - mBottomPadding;
		 * 
		 * if ((mAllowedListHeight < 1) || (listHeight + editTextHeight <
		 * mAllowedListHeight)) { mLayoutParamsItems.height =
		 * LinearLayout.LayoutParams.WRAP_CONTENT; } else { // we have to limit
		 * the height: mLayoutParamsItems.height = mAllowedListHeight; } }
		 * Log.i(TAG, "Items.height: " + mLayoutParamsItems.height); if
		 * (mLayoutParamsItems.height > 0) {
		 * mListItems.setLayoutParams(mLayoutParamsItems); };
		 */
		/*
		 * int listLen =
		 * 
		 * if (bottomEdit > bottomBackground) { // The list is too long, the
		 * edit text field // would fall off the screen, so we have // to limit
		 * the height of the list: int[] locList = new int[2];
		 * mListItems.getAbsoluteLocationOnScreen(locList); int topList =
		 * locList[1];
		 * 
		 * int newListSize = bottomBackground - topList - mEditText.getHeight();
		 * 
		 * Log.i(TAG, "newListSize : " + newListSize);
		 * 
		 * LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
		 * LinearLayout.LayoutParams.FILL_PARENT, newListSize);
		 * //LinearLayout.LayoutParams.WRAP_CONTENT);
		 * //mLinearLayoutBackground.updateViewLayout(mListItems, params);
		 * mListItems.setLayoutParams(params); }
		 */

	}

	/**
	 * Initialized GTalk if the currently selected list requires it.
	 */
	void bindGTalkIfNeeded() {
		if (isCurrentListShared()) {
			// Only bind the first time a shared shopping list is opened.
			// TODO ???
			/*
			 * mGTalkSender.bindGTalkService();
			 */
		}
	}

	/**
	 * Tests whether the current list is shared via GTalk. (not local sharing!)
	 * 
	 * @return true if SHARE_CONTACTS contains the '@' character.
	 */
	boolean isCurrentListShared() {
		long listId = getSelectedListId();
		if (listId < 0) {
			// No valid list - probably view is not active
			// and no item is selected.
			return false;
		}

		// mCursorListFilter has been set to correct position
		// by calling getSelectedListId(),
		// so we can read out further elements:
		// String shareName =
		// mCursorListFilter.getString(mStringListFilterSHARENAME);
		String recipients = mCursorListFilter
				.getString(mStringListFilterSHARECONTACTS);

		// If recipients contains the '@' symbol, it is shared.
		return recipients.contains("@");
	}

	/**
	 * Extend the SimpleCursorAdapter to strike through items. if STATUS ==
	 * Shopping.Status.BOUGHT
	 */
	public class mSimpleCursorAdapter extends SimpleCursorAdapter {

		/**
		 * Constructor simply calls super class.
		 * 
		 * @param context
		 *            Context.
		 * @param layout
		 *            Layout.
		 * @param c
		 *            Cursor.
		 * @param from
		 *            Projection from.
		 * @param to
		 *            Projection to.
		 */
		mSimpleCursorAdapter(final Context context, final int layout,
				final Cursor c, final String[] from, final int[] to) {
			super(context, layout, c, from, to);
		}

		/**
		 * Additionally to the standard bindView, we also check for STATUS, and
		 * strike the item through if BOUGHT.
		 */
		@Override
		public void bindView(final View view, final Context context,
				final Cursor cursor) {
			// Log.i(TAG, "bindView " + view.toString());
			super.bindView(view, context, cursor);

			TextView t = (TextView) view.findViewById(R.id.name);
			// we have a check box now.. more visual and gets the point across
			CheckBox c = (CheckBox) view.findViewById(R.id.check);

			Log.i(TAG, "bindview: pos = " + cursor.getPosition());

			c.setTag(new Integer(cursor.getPosition()));

			// Set font
			t.setTypeface(mTypeface);

			// Set size
			t.setTextSize(mTextSize);

			// Check for upper case:
			if (mUpperCaseFont) {
				// Only upper case should be displayed
				CharSequence cs = t.getText();
				t.setText(cs.toString().toUpperCase());
			}

			t.setTextColor(mTextColor);

			if (mMarkType == mMarkCheckbox) {
				c.setVisibility(CheckBox.VISIBLE);
				if (cursor.getLong(mStringItemsSTATUS) == Shopping.Status.BOUGHT) {
					c.setChecked(true);
				} else {
					c.setChecked(false);
				}
			} else {
				c.setVisibility(CheckBox.GONE);
			}

			if (cursor.getLong(mStringItemsSTATUS) == Shopping.Status.BOUGHT) {
				t.setTextColor(mMarkTextColor);

				if (mMarkType == mMarkStrikethrough) {
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
					// Drawable colorStrikethrough = context
					// .getResources().getDrawable(R.drawable.strikethrough);
					// str.setSpan(new ForegroundColorSpan(0xFF006600), 0,
					// str.setSpan(new ForegroundColorSpan
					// (getResources().getColor(R.color.darkgreen)), 0,
					// str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					// color: 0x33336600
				}

				if (mMarkType == mMarkAddtext) {
					// very simple
					t.append("... OK");
				}

			}
			/*
			 * c.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			 * 
			 * @Override public void onCheckedChanged(CompoundButton buttonView,
			 * boolean isChecked) { // TODO Auto-generated method stub
			 * Log.d(TAG, "check clicked");
			 * 
			 * int pos = (Integer) buttonView.getTag();
			 * 
			 * Log.i(TAG, "bindview: move to pos = " + pos); AdapterView av =
			 * (AdapterView) buttonView.getParent().getParent(); Cursor c =
			 * (Cursor) av.getItemAtPosition(pos);
			 * 
			 * if (mState == STATE_PICK_ITEM) { pickItem(c); } else {
			 * toggleItemBought(c); }
			 * 
			 * }
			 * 
			 * });
			 */

			// The parent view knows how to deal with clicks.
			// We just pass the click through.
			c.setClickable(false);
		}

	}

	/**
	 * Sensor service callback
	 */
	/*
	 * private OnSensorListener mOnSensorListener = new OnSensorListener() {
	 * 
	 * public boolean onSensorEvent(final SensorEvent event) { Log.i(TAG,
	 * "onSensorEvent: " + event); int action = event.getAction(); switch
	 * (action) { case SensorEvent.ACTION_MOVE:
	 * 
	 * return true; case SensorEvent.ACTION_SHAKE:
	 * 
	 * // Clean up list cleanupList();
	 * 
	 * // ToDo: Make this // 1) step by step // 2) animate elements // 3) more
	 * than 3 shakes -> remove all
	 * 
	 * return true; default: assert false; } return false; }
	 * 
	 * };
	 */

	// Handle the process of automatically updating enabled sensors:
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MESSAGE_UPDATE_CURSORS) {
				mCursorListFilter.requery();

				if (mUpdating) {
					sendMessageDelayed(obtainMessage(MESSAGE_UPDATE_CURSORS),
							mUpdateInterval);
				}

			}
		}
	};

	/**
	 * Listens for intents for updates in the database.
	 * 
	 * @param context
	 * @param intent
	 */
	// TODO ???
	/*
	 * public class ListIntentReceiver extends IntentReceiver {
	 * 
	 * public void onReceiveIntent(Context context, Intent intent) { String
	 * action = intent.getAction(); Log.i(TAG, "ShoppingList received intent " +
	 * action);
	 * 
	 * if (action.equals(OpenIntents.REFRESH_ACTION)) {
	 * mCursorListFilter.requery();
	 * 
	 * } } }
	 */
	/*
	 * ListIntentReceiver mIntentReceiver;
	 */

	/**
	 * This method is called when the sending activity has finished, with the
	 * result it supplied.
	 * 
	 * @param requestCode
	 *            The original request code as given to startActivity().
	 * @param resultCode
	 *            From sending activity as per setResult().
	 * @param data
	 *            From sending activity as per setResult().
	 * @param extras
	 *            From sending activity as per setResult().
	 * 
	 * @see android.app.Activity#onActivityResult(int, int, java.lang.String,
	 *      android.os.Bundle)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.i(TAG, "ShoppingView: onActivityResult. ");

		if (requestCode == SUBACTIVITY_LIST_SHARE_SETTINGS) {
			Log.i(TAG, "SUBACTIVITY_LIST_SHARE_SETTINGS");

			if (resultCode == RESULT_CANCELED) {
				// Don't do anything.
				Log.i(TAG, "RESULT_CANCELED");

			} else {
				// Broadcast the intent
				Log.i(TAG, "Broadcast intent.");

				// TODO ???
				/*
				 * Uri uri = Uri.parse(data);
				 */
				Uri uri = Uri.parse(data.getDataString());

				if (!mListUri.equals(uri)) {
					Log.e(TAG, "Unexpected uri returned: Should be " + mListUri
							+ " but was " + uri);
					return;
				}

				// TODO ???
				Bundle extras = data.getExtras();

				String sharename = extras.getString(Shopping.Lists.SHARE_NAME);
				String contacts = extras
						.getString(Shopping.Lists.SHARE_CONTACTS);

				Log.i(TAG, "Received bundle: sharename: " + sharename
						+ ", contacts: " + contacts);
				// TODO ???
				/*
				 * mGTalkSender.sendList(contacts, sharename);
				 */

				// Here we also send the current content of the list
				// to all recipients.
				// This could probably be optimized - by sending
				// content only to the new recipients, as the
				// old ones should be in sync already.
				// First delete all items in list
				mCursorItems.moveToPosition(-1);
				while (mCursorItems.moveToNext()) {
					String itemName = mCursorItems
							.getString(mStringItemsITEMNAME);
					Long status = mCursorItems.getLong(mStringItemsSTATUS);
					Log.i(TAG, "Update shared item. " + " recipients: "
							+ contacts + ", shareName: " + sharename
							+ ", item: " + itemName);
					// TODO ???
					/*
					 * mGTalkSender.sendItemUpdate(contacts, sharename,
					 * itemName, itemName, status, status);
					 */
				}
			}

		}
		// super.onActivityResult(requestCode, resultCode, data, extras);
	}

}
