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
import org.openintents.provider.Shopping.Status;
import org.openintents.shopping.share.GTalkSender;
import org.openintents.util.MenuIntentOptionsWithIcons;
import org.openintents.util.ShakeSensorListener;

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
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Typeface;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SimpleCursorAdapter.ViewBinder;

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
	private static final boolean debug = !false;

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

	private static final int MENU_ABOUT = Menu.FIRST + 11;

	// TODO: Implement the following menu items
	private static final int MENU_EDIT_LIST = Menu.FIRST + 12; // includes
	// rename
	private static final int MENU_SORT = Menu.FIRST + 13; // sort alphabetically
	// or modified
	private static final int MENU_CHANGE_MODE = Menu.FIRST + 14; // pick from
	// previously
	// used items

	// TODO: Implement "select list" action
	// that can be called by other programs.
	private static final int MENU_SELECT_LIST = Menu.FIRST + 15; // select a
	// shopping list
	private static final int MENU_UPDATE = Menu.FIRST + 16;
	private static final int MENU_PREFERENCES = Menu.FIRST + 17;
	private static final int MENU_SEND = Menu.FIRST + 18;
	private static final int MENU_REMOVE_ITEM_FROM_LIST = Menu.FIRST + 19;

	private static final int DIALOG_ABOUT = 1;
	private static final int DIALOG_TEXT_ENTRY = 2;
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
	 * mode: add items from existing list
	 */
	static final int MODE_ADD_ITEMS = 2;

	/**
	 * mode: I am in the shop
	 */
	static final int MODE_IN_SHOP = 1;

	/**
	 * current mode, in shop, or adding items
	 */
	private int mMode = MODE_IN_SHOP;

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
	 * Update interval for automatic requires.
	 * 
	 * (Workaround since ContentObserver does not work.)
	 */
	private int mUpdateInterval;

	private boolean mUpdating;

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

	private ShoppingListView mListItems;
	private Cursor mCursorItems;

	static final String[] mStringItems = new String[] { ContainsFull._ID,
			ContainsFull.ITEM_NAME, ContainsFull.ITEM_IMAGE,
			ContainsFull.ITEM_TAGS, ContainsFull.ITEM_PRICE,
			ContainsFull.QUANTITY, ContainsFull.STATUS, ContainsFull.ITEM_ID,
			ContainsFull.SHARE_CREATED_BY, ContainsFull.SHARE_MODIFIED_BY };
	private static final int mStringItemsCONTAINSID = 0;
	private static final int mStringItemsITEMNAME = 1;
	private static final int mStringItemsITEMIMAGE = 2;
	private static final int mStringItemsITEMTAGS = 3;
	static final int mStringItemsITEMPRICE = 4;
	private static final int mStringItemsQUANTITY = 5;
	static final int mStringItemsSTATUS = 6;
	static final int mStringItemsITEMID = 7;
	private static final int mStringItemsSHARECREATEDBY = 8;
	private static final int mStringItemsSHAREMODIFIEDBY = 9;

	private LinearLayout.LayoutParams mLayoutParamsItems;
	private int mAllowedListHeight; // Height for the list allowed in this view.

	private AutoCompleteTextView mEditText;

	protected Context mDialogContext;

	// TODO: Set up state information for onFreeze(), ...
	// State data to be stored when freezing:
	private final String ORIGINAL_ITEM = "original item";

	private static final String BUNDLE_TEXT_ENTRY_MENU = "text entry menu";

	// Skins --------------------------

	// GTalk --------------------------
	private GTalkSender mGTalkSender;

	private int mTextEntryMenu;
	private Cursor mItemsCursor;

	public int mPriceVisiblity;
	private int mTagsVisiblity;
	private SensorManager mSensorManager;
	private SensorListener mMySensorListener = new ShakeSensorListener() {

		@Override
		public void onShake() {
			cleanupList();
		}

	};

	/**
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// Log.i(TAG, "Shopping list onCreate()");

		if (!EulaActivity.checkEula(this)) {
			return;
		}
		setContentView(R.layout.shopping);

		// Initialize the convenience functions:
		Shopping.mContentResolver = getContentResolver();

		// Initialize GTalkSender (but don't bind yet!)
		mGTalkSender = new GTalkSender(this);

		// Automatic requeries (once a second)
		mUpdateInterval = 2000;
		mUpdating = false;

		// General Uris:
		mListUri = Shopping.Lists.CONTENT_URI;
		mItemUri = Shopping.Items.CONTENT_URI;

		int defaultShoppingList = initFromPreferences();

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

		// hook up all buttons, lists, edit text:
		createView();

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

		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

		// Bind GTalk if currently selected shopping list needs it:
		bindGTalkIfNeeded();

		if (icicle != null) {
			String prevText = icicle.getString(ORIGINAL_ITEM);
			if (prevText != null) {
				mEditText.setTextKeepState(prevText);
			}
			mTextEntryMenu = icicle.getInt(BUNDLE_TEXT_ENTRY_MENU);
		}

		// set focus to the edit line:
		mEditText.requestFocus();

		// TODO remove initFromPreferences from onCreate
		// we need it in resume to update after settings have changed
		initFromPreferences();
		// now update title and fill all items
		onModeChanged();
	}

	private int initFromPreferences() {
		// if set to "last used", override the default list.
		SharedPreferences sp = getSharedPreferences(
				"org.openintents.shopping_preferences", MODE_PRIVATE);
		final boolean loadLastUsed = sp.getBoolean(
				PreferenceActivity.PREFS_LOADLASTUSED, false);

		Log.e(TAG, "load last used ?" + loadLastUsed);
		int defaultShoppingList = 1;
		if (loadLastUsed) {
			defaultShoppingList = sp.getInt(PreferenceActivity.PREFS_LASTUSED,
					1);
		} else {
			defaultShoppingList = (int) Shopping.getDefaultList();
		}

		if (sp.getBoolean(PreferenceActivity.PREFS_SHOW_PRICE, false)) {
			mPriceVisiblity = View.VISIBLE;
		} else {
			mPriceVisiblity = View.GONE;
		}

		if (sp.getBoolean(PreferenceActivity.PREFS_SHOW_TAGS, false)) {
			mTagsVisiblity = View.VISIBLE;
		} else {
			mTagsVisiblity = View.GONE;
		}
		return defaultShoppingList;
	}

	private void registerSensor() {
		if (mMode == MODE_IN_SHOP) {
			mSensorManager.registerListener(mMySensorListener,
					SensorManager.SENSOR_ACCELEROMETER,
					SensorManager.SENSOR_DELAY_UI);
		}

	}

	private void unregisterSensor() {
		mSensorManager.unregisterListener(mMySensorListener);
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

		mListItems.setListTheme(loadListTheme());
		mEditText
				.setKeyListener(PreferenceActivity
						.getCapitalizationKeyListenerFromPrefs(getApplicationContext()));

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

		registerSensor();
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

		unregisterSensor();

		SharedPreferences sp = getSharedPreferences(
				"org.openintents.shopping_preferences", MODE_PRIVATE);
		SharedPreferences.Editor editor = sp.edit();
		editor.putInt(PreferenceActivity.PREFS_LASTUSED, new Long(
				getSelectedListId()).intValue());
		editor.commit();
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
		outState.putInt(BUNDLE_TEXT_ENTRY_MENU, mTextEntryMenu);

		mUpdating = false;
	}

	/**
	 * Hook up buttons, lists, and edittext with functionality.
	 */
	private void createView() {

		
		mSpinnerListFilter = (Spinner) findViewById(R.id.spinner_listfilter);
		mSpinnerListFilter
				.setOnItemSelectedListener(new OnItemSelectedListener() {
					public void onItemSelected(AdapterView parent, View v,
							int position, long id) {
						fillItems();
						// Now set the theme based on the selected list:
						mListItems.setListTheme(loadListTheme());

						bindGTalkIfNeeded();
					}

					public void onNothingSelected(AdapterView arg0) {
						fillItems();

					}
				});

		mEditText = (AutoCompleteTextView) findViewById(R.id.autocomplete_add_item);
		mItemsCursor = managedQuery(Items.CONTENT_URI, new String[] {
				Items._ID, Items.NAME }, null, null, "name desc");
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				android.R.layout.simple_dropdown_item_1line, mItemsCursor,
				new String[] { Items.NAME }, new int[] { android.R.id.text1 });
		adapter.setStringConversionColumn(1);
		adapter.setFilterQueryProvider(new FilterQueryProvider() {

			public Cursor runQuery(CharSequence constraint) {
				mItemsCursor = managedQuery(Items.CONTENT_URI, new String[] {
						Items._ID, Items.NAME }, "upper(name) like '%"
						+ (constraint == null ? "" : constraint.toString()
								.toUpperCase()) + "%'", null, "name desc");
				return mItemsCursor;
			}

		});
		mEditText.setAdapter(adapter);
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

		mListItems = (ShoppingListView) findViewById(R.id.list_items);
		mListItems.setThemedBackground(findViewById(R.id.background));
		
		mListItems.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView parent, View v, int pos, long id) {
				Cursor c = (Cursor) parent.getItemAtPosition(pos);
				if (mState == STATE_PICK_ITEM) {
					pickItem(c);
				} else {
					mListItems.toggleItemBought(pos);
				}
			}

		});

		mListItems
				.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

					public void onCreateContextMenu(ContextMenu contextmenu,
							View view, ContextMenuInfo info) {
						contextmenu.add(0, MENU_MARK_ITEM, 0,
								R.string.menu_mark_item).setShortcut('1', 'm');
						contextmenu.add(0, MENU_EDIT_ITEM, 0,
								R.string.menu_edit_item).setShortcut('2', 'e');
						contextmenu.add(0, MENU_REMOVE_ITEM_FROM_LIST, 0,
								R.string.menu_remove_item)
								.setShortcut('3', 'r');
						contextmenu.add(0, MENU_DELETE_ITEM, 0,
								R.string.menu_delete_item)
								.setShortcut('4', 'd');
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

			mListItems.insertNewItem(newItem);
			mEditText.setText("");
		}
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

		menu.add(0, MENU_CHANGE_MODE, 0, R.string.menu_pick_items).setIcon(
				android.R.drawable.ic_menu_add).setShortcut('2', 'p');
		;

		/*
		 * menu.add(0, MENU_SHARE, 0, R.string.share)
		 * .setIcon(R.drawable.contact_share001a) .setShortcut('4', 's');
		 */

		menu.add(0, MENU_THEME, 0, R.string.theme).setIcon(
				android.R.drawable.ic_menu_manage).setShortcut('3', 't');

		menu.add(0, MENU_PREFERENCES, 0, R.string.preferences).setIcon(
				android.R.drawable.ic_menu_preferences).setShortcut('4', 'p');

		menu.add(0, MENU_DELETE_LIST, 0, R.string.delete_list).setIcon(
				android.R.drawable.ic_menu_delete).setShortcut('5', 'd');

		menu.add(0, MENU_SEND, 0, R.string.send).setIcon(
				android.R.drawable.ic_menu_send).setShortcut('6', 's');

		if (addLocationAlertPossible()) {
			menu
					.add(0, MENU_ADD_LOCATION_ALERT, 0,
							R.string.shopping_add_alert).setIcon(
							android.R.drawable.ic_menu_mylocation).setShortcut(
							'7', 'l');
		}

		UpdateMenu
				.addUpdateMenu(this, menu, 0, MENU_UPDATE, 0, R.string.update);

		menu.add(0, MENU_ABOUT, 0, R.string.about).setIcon(
				android.R.drawable.ic_menu_info_details).setShortcut('0', 'a');

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

		// set menu title for change mode
		MenuItem menuItem = menu.findItem(MENU_CHANGE_MODE);
		if (mMode == MODE_ADD_ITEMS) {
			menuItem.setTitle(R.string.menu_start_shopping);
			menuItem.setIcon(android.R.drawable.ic_menu_myplaces);

		} else {
			menu.findItem(MENU_CHANGE_MODE).setTitle(R.string.menu_pick_items);
			menuItem.setIcon(android.R.drawable.ic_menu_add);
		}

		// set menu title for change mode
		menuItem = menu.findItem(MENU_CLEAN_UP_LIST).setVisible(
				mMode == MODE_IN_SHOP);

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

		case MENU_CHANGE_MODE:
//			if (mMode == MODE_IN_SHOP) {
//				mMode = MODE_ADD_ITEMS;
//			} else {
//				mMode = MODE_IN_SHOP;
//			}
//			onModeChanged();
			intent = new Intent(this, PickItemsActivity.class);
			intent.setData(mListUri);
			startActivity(intent);
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
		case MENU_REMOVE_ITEM_FROM_LIST:
			removeItemFromList(menuInfo.position);
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

		switch (menuAction) {
		case MENU_NEW_LIST:
			mTextEntryMenu = MENU_NEW_LIST;
			showDialog(DIALOG_TEXT_ENTRY);
			break;
		case MENU_RENAME_LIST:
			mTextEntryMenu = MENU_RENAME_LIST;
			showDialog(DIALOG_TEXT_ENTRY);
			break;
		case MENU_EDIT_ITEM:
			mTextEntryMenu = MENU_EDIT_ITEM;
			showDialog(DIALOG_TEXT_ENTRY);

			break;
		}
	}

	void doTextEntryDialogAction(int menuAction, Dialog dialog) {
		if (debug)
			Log.i(TAG, "doListDialogAction: menuAction: " + menuAction);
		EditText et = (EditText) dialog.findViewById(R.id.edittext);
		String newName = et.getText().toString();
		switch (menuAction) {
		case MENU_NEW_LIST:
			if (createNewList(newName)) {
				// New list created. Exit:
				dialog.dismiss();
				et.setText("");
			}
			break;
		case MENU_RENAME_LIST:
			if (renameList(newName)) {
				// Rename successful. Exit:
				dialog.dismiss();
				et.setText("");
			}
			break;
		case MENU_EDIT_ITEM:
			et = (EditText) dialog.findViewById(R.id.edittags);
			String tags = et.getText().toString();
			et = (EditText) dialog.findViewById(R.id.editprice);
			String price = et.getText().toString();
			Long priceLong;
			if (TextUtils.isEmpty(price)) {
				priceLong = 0L;
			} else {
				try {
					priceLong = (long) (100 * Double.parseDouble(price));
				} catch (NumberFormatException e) {
					priceLong = null;
				}
			}
			if (updateItem(newName, tags, priceLong)) {
				// Rename successful. Exit:
				dialog.dismiss();
				et.setText("");
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
	private boolean createNewList(String name) {

		if (name.equals("")) {
			// User has not provided any name
			Toast.makeText(this, getString(R.string.please_enter_name),
					Toast.LENGTH_SHORT).show();
			return false;
		}

		int newId = (int) Shopping.getList(name);
		fillListFilter();

		setSelectedListId(newId);

		// Now set the theme based on the selected list:
		mListItems.setListTheme(loadListTheme());

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
	private boolean renameList(String newName) {

		if (newName.equals("")) {
			// User has not provided any name
			Toast.makeText(this, getString(R.string.please_enter_name),
					Toast.LENGTH_SHORT).show();
			return false;
		}

		// Rename currently selected list:
		ContentValues values = new ContentValues();
		values.put(Lists.NAME, "" + newName);
		getContentResolver().update(
				Uri.withAppendedPath(Lists.CONTENT_URI, mCursorListFilter
						.getString(0)), values, null, null);

		mCursorListFilter.requery();
		return true;
	}

	/**
	 * Rename item from dialog.
	 * 
	 * @param price
	 * @param tags
	 * 
	 * @return true if new list was renamed. False if new list was not renamed,
	 *         because user has not given any name.
	 */
	private boolean updateItem(String newName, String tags, Long price) {

		if (newName.equals("")) {
			// User has not provided any name
			Toast.makeText(this, getString(R.string.please_enter_name),
					Toast.LENGTH_SHORT).show();
			return false;
		}

		String oldItemName = mCursorItems.getString(mStringItemsITEMNAME);
		String newItemName = newName;

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
			values.put(Items.TAGS, tags);
			if (price != null) {
				values.put(Items.PRICE, price);
			}
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
					sb.append("[X] ");
				} else {
					sb.append("[ ] ");
				}
				sb.append(item.getString(mStringItemsITEMNAME));
				sb.append("\n");
			}

			Intent i = new Intent();
			i.setAction(Intent.ACTION_SEND);
			i.setType("text/plain");
			i.putExtra(Intent.EXTRA_SUBJECT, getCurrentListName());
			i.putExtra(Intent.EXTRA_TEXT, sb.toString());

			try {
				startActivity(Intent.createChooser(i, getString(R.string.send)));
			} catch (ActivityNotFoundException e) {
				Toast.makeText(this, R.string.email_not_available,
						Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Email client not installed");
			}
		} else {
			Toast.makeText(this, R.string.empty_list_not_sent,
					Toast.LENGTH_SHORT);
		}

	}

	/**
	 * Clean up the currently visible shopping list by removing items from list
	 * that are marked BOUGHT.
	 */
	private void cleanupList() {
		// Remove all items from current list
		// which have STATUS = Status.BOUGHT

		if (!mListItems.cleanupList()) {
			// Show toast
			Toast.makeText(this, R.string.no_items_marked, Toast.LENGTH_SHORT)
					.show();
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
		mListItems.setListTheme(loadListTheme());

		bindGTalkIfNeeded();
	}

	/** Mark item */
	void markItem(int position) {		
		mListItems.toggleItemBought(position);
	}

	/** Edit item */
	void editItem(int position) {
		mCursorItems.moveToPosition(position);
		showListDialog(MENU_EDIT_ITEM);
	}

	/** delete item */
	void deleteItem(int position) {
		// Delete item from all lists
		// by deleting contains row
		getContentResolver().delete(Contains.CONTENT_URI, "item_id = ?",
				new String[] { mCursorItems.getString(mStringItemsITEMID) });

		// and delete item
		getContentResolver().delete(Items.CONTENT_URI, "_id = ?",
				new String[] { mCursorItems.getString(mStringItemsITEMID) });

		mCursorItems.requery();
	}

	/** removeItemFromList */
	void removeItemFromList(int position) {
		// Remember old values before delete (for share below)
		String itemName = mCursorItems.getString(mStringItemsITEMNAME);
		long oldstatus = mCursorItems.getLong(mStringItemsSTATUS);

		// Delete item from list
		// by deleting contains row
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
							mListItems.setListTheme(1);
							break;
						case R.id.radio2:
							mListItems.setListTheme(2);
							break;
						case R.id.radio3:
							mListItems.setListTheme(3);
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
								mListItems.setListTheme(themeId);

							}
						}).setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								/* User clicked No so do some stuff */
								int themeId = loadListTheme();
								mListItems.setListTheme(themeId);
							}
						}).setOnCancelListener(new OnCancelListener() {

					public void onCancel(DialogInterface arg0) {
						int themeId = loadListTheme();
						mListItems.setListTheme(themeId);
					}
				}).show();

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

	@Override
	protected Dialog onCreateDialog(int id) {
		if (debug)
			Log.d(TAG, "onCreateDialog: mTextEntryMenu: " + mTextEntryMenu);

		switch (id) {
		case DIALOG_ABOUT:
			return new AboutDialog(this);
		case DIALOG_TEXT_ENTRY:
			LayoutInflater factory = LayoutInflater.from(this);
			final View textEntryView = factory
					.inflate(R.layout.input_box, null);
			final Dialog dlg = new AlertDialog.Builder(this).setIcon(
					android.R.drawable.ic_menu_edit).setTitle(
					R.string.ask_new_list).setView(textEntryView)
					.setPositiveButton(R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									dialog.dismiss();
									doTextEntryDialogAction(mTextEntryMenu,
											(Dialog) dialog);

								}
							}).setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									dialog.cancel();
								}
							}).create();

			// Accept OK also when user hits "Enter"
			EditText et = (EditText) textEntryView.findViewById(R.id.edittext);

			et
					.setKeyListener(PreferenceActivity
							.getCapitalizationKeyListenerFromPrefs(getApplicationContext()));
			// et.setKeyListener(PreferenceActivity.
			// getCapitalizationKeyListenerFromPrefs(this));

			et.setOnKeyListener(new OnKeyListener() {

				public boolean onKey(final View v, final int keyCode,
						final KeyEvent key) {
					// Log.i(TAG, "KeyCode: " + keyCode);

					if (mTextEntryMenu != MENU_EDIT_ITEM
							&& key.getAction() == KeyEvent.ACTION_DOWN
							&& keyCode == KeyEvent.KEYCODE_ENTER) {
						doTextEntryDialogAction(mTextEntryMenu, dlg);
						return true;
					}
					return false;
				}

			});
			return dlg;

		}
		return null;

	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);

		switch (id) {
		case DIALOG_ABOUT:
			break;
		case DIALOG_TEXT_ENTRY:
			if (debug)
				Log
						.d(TAG, "onPrepareDialog: mTextEntryMenu: "
								+ mTextEntryMenu);
			EditText et = (EditText) dialog.findViewById(R.id.edittext);
			et.selectAll();
			EditText tags = (EditText) dialog.findViewById(R.id.edittags);
			tags.selectAll();
			EditText price = (EditText) dialog.findViewById(R.id.editprice);
			price.selectAll();

			View tagsPanel = dialog.findViewById(R.id.tags_panel);
			View pricePanel = dialog.findViewById(R.id.price_panel);

			switch (mTextEntryMenu) {

			case MENU_NEW_LIST:
				dialog.setTitle(R.string.ask_new_list);
				tagsPanel.setVisibility(View.GONE);
				pricePanel.setVisibility(View.GONE);
				break;
			case MENU_RENAME_LIST:
				dialog.setTitle(R.string.ask_rename_list);
				if (mCursorListFilter != null
						&& mCursorListFilter.getPosition() >= 0) {
					et.setText(mCursorListFilter
							.getString(mStringListFilterNAME));
				}
				tagsPanel.setVisibility(View.GONE);
				pricePanel.setVisibility(View.GONE);
				break;
			case MENU_EDIT_ITEM:
				dialog.setTitle(R.string.ask_edit_item);
				// Cursor is supposed to be set to correct row already:
				et.setText(mCursorItems.getString(mStringItemsITEMNAME));
				tags.setText(mCursorItems.getString(mStringItemsITEMTAGS));
				price.setText(String.valueOf(mCursorItems
						.getLong(mStringItemsITEMPRICE) * 0.01d));
				tagsPanel.setVisibility(View.VISIBLE);
				pricePanel.setVisibility(View.VISIBLE);
				break;
			}
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

	private void onModeChanged() {

		fillItems();

		if (mMode == MODE_IN_SHOP) {
			setTitle(getString(R.string.shopping_title, getCurrentListName()));		
			registerSensor();
		} else {
			setTitle(getString(R.string.pick_items_titel, getCurrentListName()));		
			unregisterSensor();
		}
	}

	private String getCurrentListName() {
		return ((Cursor) mSpinnerListFilter.getSelectedItem())
				.getString(mStringListFilterNAME);
	}

	private void fillItems() {

		long listId = getSelectedListId();
		if (listId < 0) {
			// No valid list - probably view is not active
			// and no item is selected.
			return;
		}
		startManagingCursor(mListItems.fillItems(listId));

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
	}

}
