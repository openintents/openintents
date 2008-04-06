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

import org.openintents.OpenIntents;
import org.openintents.R;
import org.openintents.hardware.Sensors;
import org.openintents.hardware.SensorsPlus;
import org.openintents.provider.Hardware;
import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.ContainsFull;
import org.openintents.provider.Shopping.Lists;
import org.openintents.shopping.share.GTalkSender;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentReceiver;
import android.content.DialogInterface.OnCancelListener;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.IContentObserver;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewInflate;
import android.view.WindowManager;
import android.view.Menu.Item;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * 
 * Displays a shopping list.
 *
 */
public class ShoppingView
        extends Activity { //implements AdapterView.OnItemClickListener {

    /**
     * TAG for logging.
     */
    private static final String TAG = "ShoppingView";

    private static final int MENU_NEW_LIST = Menu.FIRST;
    private static final int MENU_CLEAN_UP_LIST = Menu.FIRST + 1;
    private static final int MENU_DELETE_LIST = Menu.FIRST + 2;
    
    private static final int MENU_SHARE = Menu.FIRST + 3;
    private static final int MENU_THEME = Menu.FIRST + 4;

    // TODO: Implement the following menu items
    private static final int MENU_EDIT_LIST = Menu.FIRST + 3; // includes rename
    private static final int MENU_EDIT_ITEM = Menu.FIRST + 4; // includes rename
    private static final int MENU_DELETE_ITEM = Menu.FIRST + 5;
    private static final int MENU_SORT =
            Menu.FIRST + 6; // sort alphabetically or modified
    private static final int MENU_PICK_ITEMS =
            Menu.FIRST + 7; // pick from previously used items

    // TODO: Implement "select list" action
    // that can be called by other programs.
    private static final int MENU_SELECT_LIST =
            Menu.FIRST + 8; // select a shopping list

    // TODO: Further possible actions to implement:
    // * Move items to some other shopping list

    //
    private static final int MENU_SETTINGS = Menu.FIRST + 100;
    private static final int MENU_CONNECT_SIMULATOR = Menu.FIRST + 101;

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
     *  Definition of the requestCode for the subactivity. 
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
    private static final int mBottomPadding = 50;

    /**
     * Maximum number of lines on the screen. (should be calculated later, for now
     * hardcoded.)
     */
    private static int mMaxListCount = 6;

    /**
     * Private members connected to Spinner ListFilter.
     */
    private Spinner mSpinnerListFilter;
    private Cursor mCursorListFilter;
    private static final String[] mStringListFilter =
            new String[]{Lists._ID, Lists.NAME, Lists.IMAGE, 
    		Lists.SHARE_NAME, Lists.SHARE_CONTACTS,
    		Lists.SKIN_BACKGROUND};
    private static final int mStringListFilterID = 0;
    private static final int mStringListFilterNAME = 1;
    private static final int mStringListFilterIMAGE = 2;
    private static final int mStringListFilterSHARENAME = 3;
    private static final int mStringListFilterSHARECONTACTS = 4;
    private static final int mStringListFilterSKINBACKGROUND = 5;
    

    private ListView mListItems;
    private Cursor mCursorItems;
    
    private static final String[] mStringItems =
            new String[]{
                    ContainsFull._ID,
                    ContainsFull.ITEM_NAME,
                    ContainsFull.ITEM_IMAGE,
                    ContainsFull.STATUS,
                    ContainsFull.ITEM_ID,
        			ContainsFull.SHARE_CREATED_BY,
        			ContainsFull.SHARE_MODIFIED_BY};
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
    public int mMarkTextColor;
    
    public int mMarkType;
    public static final int mMarkCheckbox = 1;
    public static final int mMarkStrikethrough = 2;
    public static final int mMarkAddtext = 3;
    
    
    // GTalk --------------------------
    private GTalkSender mGTalkSender;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);
        //setTheme(android.R.style.Theme_White);
        //setTheme(android.R.style.Theme_Dialog);
        //setTheme(android.R.style.Theme_Dark);
        //setTheme(android.R.style.Theme_Black);
        //setTheme(android.R.style.Theme_Dark);
        setContentView(R.layout.shopping);

        // Initialize the convenience functions:
        Shopping.mContentResolver = getContentResolver();
        
        // Initialize GTalk:
        // TODO: Only initialize the first time a shared shopping list is opened.
        mGTalkSender = new GTalkSender(this);
        
        // Automatic requeries (once a second)
        mUpdateInterval = 2000;
        mUpdating = false;
        
        // General Uris:
        mListUri = Shopping.Lists.CONTENT_URI;
        mItemUri = Shopping.Items.CONTENT_URI;
        
        int defaultShoppingList = (int) Shopping.getDefaultList();

        // Handle the calling intent
        final Intent intent = getIntent();
        final String type = intent.resolveType(this);
        final String action = intent.getAction();
        if (action.equals(Intent.MAIN_ACTION)) {
        	// Main action
            mState = STATE_MAIN;
            
            mListUri = Uri.withAppendedPath(Shopping.Lists.CONTENT_URI, 
            		"" + defaultShoppingList);
            
            intent.setData(mListUri);
            
        } else if (action.equals(Intent.VIEW_ACTION)) {
            mState = STATE_VIEW_LIST;
            
            mListUri = intent.getData();
            
        } else if (action.equals(Intent.PICK_ACTION)) {
            mState = STATE_PICK_ITEM; 
            
            mListUri = Uri.withAppendedPath(Shopping.Lists.CONTENT_URI, 
            		"" + defaultShoppingList);
        } else if (action.equals(Intent.GET_CONTENT_ACTION)) {
            mState = STATE_GET_CONTENT_ITEM;
            
            mListUri = Uri.withAppendedPath(Shopping.Lists.CONTENT_URI, 
            		"" + defaultShoppingList);
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
        int selectList = Integer.parseInt(mListUri.getLastPathSegment());
        
        // select the default shopping list at the beginning:
        setSelectedListId(selectList);
        // TODO: Select the last shopping list viewed
        //       instead of the default shopping list.
        //       (requires saving that information as
        //        preference).

        // now fill all items
        fillItems();
        
        // Set the theme based on the selected list:
        setListTheme(loadListTheme());
        
        if (icicle != null)
        {
            String prevText = icicle.getString(ORIGINAL_ITEM);
            if (prevText != null)
            {
                mEditText.setText(prevText);
            }
        }

        // set focus to the edit line:
        mEditText.requestFocus();
        
        // Create Intent receiver:
        mIntentReceiver = new ListIntentReceiver();
    }


    @Override
    protected void onResume()
    {
        super.onResume();

        // Modify our overall title depending on the mode we are running in.
        if (mState == STATE_MAIN ||
                mState == STATE_VIEW_LIST)
        {
            setTitle(getText(R.string.shopping_list));
        }
        else if ((mState == STATE_PICK_ITEM) || (mState == STATE_GET_CONTENT_ITEM))
        {
            setTitle(getText(R.string.pick_item));
            setTitleColor(0xFFAAAAFF);
        }


        checkListLength();
        
        if (! mUpdating) {
        	mUpdating = true;
        	// mHandler.sendMessageDelayed(mHandler.obtainMessage(MESSAGE_UPDATE_CURSORS), mUpdateInterval);
        }
        
        // Bind GTalk service
        mGTalkSender.bindGTalkService();
        
        // Register intent receiver for refresh intents:
        IntentFilter intentfilter = new IntentFilter(OpenIntents.REFRESH_ACTION);
        registerReceiver(mIntentReceiver, intentfilter);
    }


    /* (non-Javadoc)
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		// Unregister refresh intent receiver
		unregisterReceiver(mIntentReceiver);
	}


	@Override
    protected void onFreeze(Bundle outState)
    {
        super.onFreeze(outState);

        // Save original text from edit box
        String s = mEditText.getText().toString();
        outState.putString(ORIGINAL_ITEM, s);
        
        mUpdating = false;

        // Bind GTalk service
        mGTalkSender.unbindGTalkService();
    }

    /**
     * Hook up buttons, lists, and edittext with functionality.
     */
    private void createView()
    {
        mLinearLayoutBackground = (LinearLayout) findViewById(R.id.background);

        mSpinnerListFilter = (Spinner) findViewById(R.id.spinner_listfilter);
        mSpinnerListFilter
                .setOnItemSelectedListener(new OnItemSelectedListener()
                {
                    public void onItemSelected(AdapterView parent, View v,
                            int position, long id)
                    {
                        fillItems();
                        // Now set the theme based on the selected list:
                        setListTheme(loadListTheme());
                        checkListLength();
                    }

                    public void onNothingSelected(AdapterView arg0)
                    {
                        fillItems();
                        checkListLength();
                    }
                });

        mEditText = (EditText) findViewById(R.id.edittext_add_item);
        mEditText.setOnKeyListener(new OnKeyListener()
        {

            public boolean onKey(View v, int keyCode, KeyEvent key)
            {
                //Log.i(TAG, "KeyCode: " + keyCode
                //		+ " =?= "
                //		+Integer.parseInt(getString(R.string.key_return)) );

                // Shortcut: Instead of pressing the button,
                // one can also press the "Enter" key.
                if (key.getAction() == key.ACTION_DOWN &&
                        keyCode == Integer.parseInt(
                                getString(R.string.key_return)))
                {
                    insertNewItem();
                    return true;
                }
                ;
                return false;
            }
        });

        Button button = (Button) findViewById(R.id.button_add_item);
        button.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                insertNewItem();
            }
        });

        mLayoutParamsItems = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);

        mListItems = (ListView) findViewById(R.id.list_items);
        mListItems.setOnItemClickListener(
                new OnItemClickListener()
                {

                    public void onItemClick(AdapterView parent,
                            View v, int pos, long id)
                    {
                        Cursor c = (Cursor) parent.obtainItem(pos);
                        if (mState == STATE_PICK_ITEM)
                        {
                            pickItem(c);
                        }
                        else
                        {
                            toggleItemBought(c);
                        }
                    }

                });
        mListItems.setOnItemSelectedListener(new OnItemSelectedListener()
        {

            public void onItemSelected(AdapterView parent, View v,
                    int position, long id)
            {
                // Log.i(TAG, "mListItems selected: pos:"
                // 	+ position + ", id:" + id);
                checkListLength();
            }

            public void onNothingSelected(AdapterView arg0)
            {
                // TODO Auto-generated method stub
                checkListLength();
            }
        });

    }

    /**
     * Inserts new item from edit box into currently selected shopping list.
     */
    private void insertNewItem()
    {
        String newItem = mEditText.getText().toString();

        // Only add if there is something to add:
        if (newItem.compareTo("") != 0)
        {
            long listId = getSelectedListId();
            if (listId < 0) {
            	// No valid list - probably view is not active
            	// and no item is selected.
            	return;
            }
            
            // mCursorListFilter has been set to correct position
            // by calling getSelectedListId(),
            // so we can read out further elements:
            String shareName = mCursorListFilter.getString(mStringListFilterSHARENAME);
            String recipients = mCursorListFilter.getString(mStringListFilterSHARECONTACTS);
            
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
                mListItems.setSelection(mListItems.getCount() -
                        NUMBER_OF_ELEMENTS_BELOW_MIDDLE);
            }
            ;

            //mListItems.getChildAt(mListItems.getCount()-1).setSelected(true);
            
            // If we share items, send this item also to other lists:
            if (! recipients.equals("")) {
            	Log.i(TAG, "Share new item. "
                        + " recipients: " + recipients + ", shareName: " + shareName + ", newItem: " + newItem);
                    mGTalkSender.sendItem(recipients, shareName, newItem);
            }
        }
    }

    /**
     * strike item through or undo this.
     */
    private void toggleItemBought(Cursor c)
    {
    	long listId = getSelectedListId();
    	if (listId < 0) {
        	// No valid list - probably view is not active
        	// and no item is selected.
        	return;
        }
        
        // mCursorListFilter has been set to correct position
        // by calling getSelectedListId(),
        // so we can read out further elements:
        String shareName = mCursorListFilter.getString(mStringListFilterSHARENAME);
        String recipients = mCursorListFilter.getString(mStringListFilterSHARECONTACTS);
        String itemName = c.getString(mStringItemsITEMNAME);
        long oldstatus = c.getLong(mStringItemsSTATUS);
        
        // Toggle status:
        long newstatus = Shopping.Status.BOUGHT;
        if (oldstatus == Shopping.Status.BOUGHT)
        {
            newstatus = Shopping.Status.WANT_TO_BUY;
        }

        c.updateLong(mStringItemsSTATUS, newstatus);

        //Log.i(TAG, "Commit now:");
        c.commitUpdates();

        //Log.i(TAG, "Requery now:");
        c.requery();

        // fillItems();
        
        mListItems.invalidate();
        

        // If we share items, send this item also to other lists:
    	if (! recipients.equals("")) {
    		Log.i(TAG, "Update shared item. "
                + " recipients: " + recipients + ", shareName: " + shareName + ", status: " + newstatus);
            mGTalkSender.sendItemUpdate(recipients, shareName, 
            	itemName, itemName,
            	oldstatus, newstatus);
        }
        
    }

    /**
     * Picks an item and returns to calling activity.
     */
    private void pickItem(Cursor c)
    {
        long itemId = c.getLong(mStringItemsITEMID);
        Uri url =
                ContentUris.withAppendedId(Shopping.Items.CONTENT_URI, itemId);

        setResult(RESULT_OK, url.toString());
        finish();
    }

    // Menu

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);

        // Standard menu
        menu.add(0, MENU_NEW_LIST, R.string.new_list,
                R.drawable.shoppinglistnew001b)
                .setShortcut('0', 'n');
        menu.add(0, MENU_CLEAN_UP_LIST, R.string.clean_up_list,
                R.drawable.shoppinglistcleanup001b)
                .setShortcut('1', 'c');
        menu.add(0, MENU_DELETE_LIST, R.string.delete_list,
                R.drawable.shoppinglistdelete001b)
                .setShortcut('2', 'd');
       
        menu.add(0, MENU_SHARE, R.string.share,
                R.drawable.contact_share001a)
                .setShortcut('3', 's');
        
        menu.add(0, MENU_THEME, R.string.theme,
        		R.drawable.shoppinglisttheme001a)
        		.setShortcut('4', 't');
                

        /*
          menu.add(0, MENU_SETTINGS, R.string.sensorsimulator_settings)
          .setShortcut('0', 's');
          menu.add(0, MENU_CONNECT_SIMULATOR, R.string.connect_to_sensorsimulator)
          .setShortcut('1', 'c');
           */

        /*
        // Generate any additional actions that can be performed on the
        // overall list.  This allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.ALTERNATIVE_CATEGORY);
        menu.addIntentOptions(
                Menu.ALTERNATIVE, 0,
                new ComponentName(this, ShoppingView.class),
                null, intent, 0, null);
                */

        // Set checkable items:
        menu.setItemCheckable(MENU_CONNECT_SIMULATOR, true);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);

        // TODO: Add item-specific menu items (see NotesList.java example)
        // like edit, strike-through, delete.

        // Delete list is possible, if we have more than one list:
        menu.setItemShown(MENU_DELETE_LIST, mCursorListFilter.count() > 1);
        
        // Set additional menu items that can be used with the 
        // currently selected list.
        Intent intent = new Intent(null, mListUri);
        intent.addCategory(Intent.ALTERNATIVE_CATEGORY);
        menu.addIntentOptions(
                Menu.ALTERNATIVE, 0,
                new ComponentName(this, ShoppingView.class),
                null, intent, 0, null);

        menu.setItemChecked(MENU_CONNECT_SIMULATOR,
                SensorsPlus.isConnectedSimulator());


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(Item item)
    {
        switch (item.getId())
        {
            case MENU_NEW_LIST:
                newListDialog();
                return true;

            case MENU_CLEAN_UP_LIST:
                cleanupList();
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

            case MENU_SETTINGS:
                Intent intent = new Intent(Intent.MAIN_ACTION,
                        Hardware.Preferences.CONTENT_URI);
                startActivity(intent);
                return true;

            case MENU_CONNECT_SIMULATOR:
                // check if accelerometer is supported:
                if (SensorsPlus.isSupportedSensor(Sensors.SENSOR_ACCELEROMETER))
                {
                    // first disable the current sensors:
                    Sensors.disableSensor(Sensors.SENSOR_ACCELEROMETER);
                }

                if (!SensorsPlus.isConnectedSimulator())
                {
                    // now connect to simulator
                    Sensors.connectSimulator();
                }
                else
                {
                    // or disconnect to simulator
                    Sensors.disconnectSimulator();
                }

                // check if accelerometer is supported:
                if (SensorsPlus.isSupportedSensor(Sensors.SENSOR_ACCELEROMETER))
                {
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
    private void newListDialog()
    {

        // TODO Shall we implement this as action?
        // Then other applications may call this as well.

        mDialog = new Dialog(ShoppingView.this);

        mDialog.setContentView(R.layout.input_box);

        mDialog.setTitle(getString(R.string.ask_new_list));

        EditText et = (EditText) mDialog.findViewById(R.id.edittext);
        //et.setText(getString(R.string.new_list));
        et.setHint(getString(R.string.new_list_hint));
        et.selectAll();

        // Accept OK also when user hits "Enter"
        et.setOnKeyListener(new OnKeyListener()
        {

            public boolean onKey(final View v, final int keyCode,
                    final KeyEvent key)
            {
                //Log.i(TAG, "KeyCode: " + keyCode);

                if (key.getAction() == key.ACTION_DOWN
                        && keyCode == Integer
                        .parseInt(getString(R.string.key_return)))
                {
                    // User pressed "Enter"
                    createNewList();
                    mDialog.dismiss();
                    return true;
                }
                return false;
            }

        });


        Button bOk = (Button) mDialog.findViewById(R.id.ok);
        bOk.setOnClickListener(new OnClickListener()
        {
            public void onClick(final View v)
            {
                createNewList();
                mDialog.dismiss();
            }
        });

        Button bCancel = (Button) mDialog.findViewById(R.id.cancel);
        bCancel.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                mDialog.cancel();
            }
        });

        mDialog.show();
    }

    private void createNewList()
    {
        EditText edittext = (EditText)
                mDialog.findViewById(R.id.edittext);

        int newId = (int) Shopping.getList(edittext.getText().toString());

        edittext.setText("");
        fillListFilter();

        setSelectedListId(newId);
        
        // Now set the theme based on the selected list:
        setListTheme(loadListTheme());
    }

    /**
     * Clean up the currently visible shopping list by removing items from list
     * that are marked BOUGHT.
     */
    private void cleanupList()
    {
        // Delete all items from current list
        // which have STATUS = Status.BOUGHT

        // TODO One could write one SQL statement to delete all at once.
        // But as long as shopping lists stay small, it should not matter.

        boolean nothingdeleted = true;
        mCursorItems.moveTo(-1); // move to beginning
        while (mCursorItems.next())
        {
            if (mCursorItems.getLong(mStringItemsSTATUS)
                    == Shopping.Status.BOUGHT)
            {
                mCursorItems.deleteRow();
                mCursorItems.prev(); // Otherwise we would skip an item
                nothingdeleted = false;
            }
        }
        mCursorItems.commitUpdates();
        mCursorItems.requery();

        if (nothingdeleted)
        {
            // Show dialog:
            AlertDialog.show(ShoppingView.this,
                    getString(R.string.clean_up_list),
                    0, // TODO choose IconID
                    getString(R.string.no_items_marked),
                    getString(R.string.ok),
                    false);
        }
        else
        {
            checkListLength();
        }
    }

    /**
     * Confirm 'delete list' command by AlertDialog.
     */
    private void deleteListConfirm()
    {
        AlertDialog.show(ShoppingView.this,
                getString(R.string.delete_list),
                0, // TODO IconID?
                getString(R.string.confirm_delete_list),
                getString(R.string.ok),
                new DialogInterface.OnClickListener()
                {

                    public void onClick(DialogInterface di, int whichDialog)
                    {
                        Log.i(TAG, "Dialog click on:" + whichDialog);
                        deleteList();
                    }
                },
                getString(R.string.cancel),
                new DialogInterface.OnClickListener()
                {

                    public void onClick(DialogInterface di, int whichDialog)
                    {
                        Log.i(TAG, "Dialog click on:" + whichDialog);
                    }
                },
                true,
                new DialogInterface.OnCancelListener()
                {

                    public void onCancel(DialogInterface di)
                    {
                        // TODO Auto-generated method stub
                    }
                });

    }

    /**
     * Deletes currently selected shopping list.
     */
    private void deleteList()
    {
        // First delete all items in list
        mCursorItems.moveTo(0); // move to beginning
        while (mCursorItems.count() > 0)
        {
            mCursorItems.deleteRow();
        }

        // Then delete currently selected list
        mCursorListFilter.deleteRow();

        // Update view
        fillListFilter();
        fillItems();
        
        // Now set the theme based on the selected list:
        setListTheme(loadListTheme());
    }

    /** 
     * Calls the share settings with the currently selected list.
     */
    void setShareSettings() {
    	// Obtain URI of current list
    	
    	// Call share settings as subactivity
    	Intent intent = new Intent(OpenIntents.SET_SHARE_SETTINGS_ACTION, 
				mListUri);
		startSubActivity(intent, SUBACTIVITY_LIST_SHARE_SETTINGS);
    	
    }
    
    /**
     * Displays a dialog to select a theme for the current shopping list.
     */
    void setThemeSettings() {

        /* Display a custom progress bar */
        ViewInflate inflate = (ViewInflate) getSystemService(Context.INFLATE_SERVICE);
        final View view = inflate.inflate(R.layout.shopping_theme_settings, null, null);
        
        final RadioGroup radiogroup = (RadioGroup) view.findViewById(R.id.radiogroup);
        
        // Set Theme according to database
        radiogroup.check(R.id.radio1);
        
        int themeId = loadListTheme();
        switch(themeId) {
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
        
        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
        	
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
                
        new AlertDialog.Builder(ShoppingView.this)
        .setIcon(R.drawable.shoppinglisttheme001a)
        .setTitle(R.string.theme_pick)
        .setView(view)
        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                
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
        })
        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                
                /* User clicked No so do some stuff */
            	int themeId = loadListTheme();
                setListTheme(themeId);
            }
        })
       .setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface arg0) {
				int themeId = loadListTheme();
                setListTheme(themeId);
			}
       })
       .show();
        
    }
    
    /**
     * Set theme according to Id.
     * @param themeId
     */
    void setListTheme(int themeId) {
    	switch (themeId) {
    	case 1:
    		mTypeface = null;
    		mUpperCaseFont = false;
    		mTextColor = 0xffffffff; // white
    		mMarkTextColor = 0xffcccccc; // white gray
    		mMarkType = mMarkCheckbox;

    		mLinearLayoutBackground.setBackground(null);
    		
    		break;
    	case 2:
    		mTypeface = mTypefaceHandwriting;
    		mUpperCaseFont = false;
    		mTextColor = 0xff000000; // black
    		mMarkTextColor = 0xff008800; // dark green
    		mMarkType = mMarkStrikethrough;
    		
    		mLinearLayoutBackground.setBackground(R.drawable.shoppinglist01d);
    		
    		break;
    	case 3:
    		mTypeface = mTypefaceDigital;
    		
    		// Digital only supports upper case fonts.
    		mUpperCaseFont = true;
    		mTextColor = 0xffff0000; // red
    		mMarkTextColor = 0xff00ff00; // light green
    		mMarkType = mMarkAddtext;
    		
    		mLinearLayoutBackground.setBackground(R.drawable.theme_android);
            
    		break;
    	}
    	
    	mListItems.invalidate();
    	if (mCursorItems != null) {
    		mCursorItems.requery();
    	}
    }
    
    /** 
     * Loads the theme settings for the currently selected theme.
     * 
     * Currently only one of 3 hardcoded themes are available.
     * These are stored in 'skin_background' as '1', '2', or '3'.
     * 
     * @return 
     */
    public int loadListTheme() {
    	/*
    	long listId = getSelectedListId();
        if (listId < 0) {
        	// No valid list - probably view is not active
        	// and no item is selected.
        	return 1; // return default theme
        }
        */
    	
    	// Return default theme if something unexpected happens:
    	if (mCursorListFilter == null) return 1;
    	if (mCursorListFilter.position() < 0) return 1;
        
        // mCursorListFilter has been set to correct position
        // by calling getSelectedListId(),
        // so we can read out further elements:
        String skinBackground = mCursorListFilter.getString(mStringListFilterSKINBACKGROUND);
        
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
    	
        mCursorListFilter.updateString(mStringListFilterSKINBACKGROUND, "" + themeId);
        
        mCursorListFilter.commitUpdates();
        mCursorListFilter.requery();
    }
    
    ///////////////////////////////////////////////////////
    //
    // Helper functions
    //
    /**
     * Returns the ID of the selected shopping list.
     * 
     * As a side effect, the item URI is updated.
     * Returns -1 if nothing is selected.
     *
     * @return ID of selected shopping list.
     */
    private long getSelectedListId()
    {
    	int pos = mSpinnerListFilter.getSelectedItemPosition();
    	if (pos < 0) {
    		// nothing selected - probably view is out of focus:
    		// Do nothing.
    		return -1;
    	}
    	
        // Obtain Id of currently selected shopping list:
        mCursorListFilter.moveTo(pos);
        
        long listId = mCursorListFilter.getLong(mStringListFilterID);
        
        mListUri = Uri.withAppendedPath(Shopping.Lists.CONTENT_URI, 
        		"" + listId);
        
        getIntent().setData(mListUri);
        
        return listId;
    };

    /**
     * sets the selected list to a specific list Id
     */
    private void setSelectedListId(int id)
    {
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
        while (mCursorListFilter.next())
        {
            int posId = mCursorListFilter.getInt(mStringListFilterID);
            if (posId == id)
            {
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
    private void fillListFilter()
    {
        // Get a cursor with all lists
        mCursorListFilter = getContentResolver().query(Lists.CONTENT_URI,
                mStringListFilter,
                null, null, Lists.DEFAULT_SORT_ORDER);
        startManagingCursor(mCursorListFilter);

        if (mCursorListFilter == null)
        {
            Log.e(TAG, "missing shopping provider");

            mSpinnerListFilter.setAdapter(new ArrayAdapter(this,
                    android.R.layout.simple_spinner_item,
                    new String[]{getString(R.string.no_shopping_provider)}));
            return;
        }

        if (mCursorListFilter.count() < 1)
        {
            // We have to create default shopping list:
            // TODO Put the following string into resource my_shopping_list
            long listId = Shopping.getList("My shopping list");

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
        	
			/* (non-Javadoc)
			 * @see android.database.ContentObserver#deliverSelfNotifications()
			 */
			@Override
			public boolean deliverSelfNotifications() {
				// TODO Auto-generated method stub
				Log.i(TAG, "mListContentObserver: deliverSelfNotifications");
				return super.deliverSelfNotifications();
			}

			/* (non-Javadoc)
			 * @see android.database.ContentObserver#getContentObserver()
			 */
			@Override
			public IContentObserver getContentObserver() {
				// TODO Auto-generated method stub
				Log.i(TAG, "mListContentObserver: getContentObserver");
				return super.getContentObserver();
			}

			/* (non-Javadoc)
			 * @see android.database.ContentObserver#onChange(boolean)
			 */
			@Override
			public void onChange(boolean arg0) {
				// TODO Auto-generated method stub
				Log.i(TAG, "mListContentObserver: onChange");
				
				mCursorListFilter.requery();
				
				super.onChange(arg0);
			}
        	
        };
        
        mCursorListFilter.registerContentObserver(new mListContentObserver(new Handler()));
        
        
        
        // Register a ContentObserver, so that a new list can be
        // automatically detected.
        //mCursor

        /*
        ArrayList<String> list = new ArrayList<String>();
        // TODO Create summary of all lists
        // list.add(ALL);
        while (mCursorListFilter.next())
        {
            list.add(mCursorListFilter.getString(mStringListFilterNAME));
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mSpinnerListFilter.setAdapter(adapter);
        */
        
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                // Use a template that displays a text view
        		android.R.layout.simple_spinner_item,
                // Give the cursor to the list adapter
        		mCursorListFilter,
                new String[] {Lists.NAME},
                new int[] {android.R.id.text1});
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        
        mSpinnerListFilter.setAdapter(adapter);
        

    }


    private void fillItems()
    {
        //Log.i(TAG, "Starting fillItems()");

        long listId = getSelectedListId();
        if (listId < 0) {
        	// No valid list - probably view is not active
        	// and no item is selected.
        	return;
        }

        // Get a cursor for all items that are contained
        // in currently selected shopping list.
        mCursorItems = getContentResolver().query(
                ContainsFull.CONTENT_URI,
                mStringItems,
                "list_id = " + listId, null,
                ContainsFull.DEFAULT_SORT_ORDER);
        startManagingCursor(mCursorItems);

        // Activate the following for a striped list.
        // setupListStripes(mListItems, this);


        if (mCursorItems == null)
        {
            Log.e(TAG, "missing shopping provider");
            mListItems.setAdapter(new ArrayAdapter(this,
                    android.R.layout.simple_list_item_1,
                    new String[]{"no shopping provider"}));
            return;
        }

        ListAdapter adapter = new mSimpleCursorAdapter(this,
                // Use a template that displays a text view
                R.layout.shopping_item_row,
                // Give the cursor to the list adapter
                mCursorItems,
                // Map the IMAGE and NAME to...
                new String[]{
                        ContainsFull.ITEM_NAME,
                        ContainsFull.ITEM_IMAGE},
                // the view defined in the XML template
                new int[]{
                        R.id.name,
                        R.id.image_URI});
        mListItems.setAdapter(adapter);

        //strikeItems();
        //checkListLength();
    }
    
    /**
     * Add stripes to the a list view.
     *
     * @param listView
     * @param activity
     */
    public static void setupListStripes(ListView listView, Activity activity)
    {
        // Get Drawables for alternating stripes
        Drawable[] lineBackgrounds = new Drawable[2];

        lineBackgrounds[0] =
                activity.getResources().getDrawable(R.drawable.gold);
        lineBackgrounds[1] =
                activity.getResources().getDrawable(R.drawable.yellow_green);

        // Make and measure a sample TextView of the sort our adapter will
        // return
        View view = activity.getViewInflate().inflate(
                android.R.layout.simple_list_item_1, null, null);

        TextView v = (TextView) view.findViewById(android.R.id.text1);
        v.setText("X");
        // Make it 100 pixels wide, and let it choose its own height.
        v.measure(
                View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.EXACTLY, 100),
                View.MeasureSpec.makeMeasureSpec(View.MeasureSpec.UNSPECIFIED,
                        0));
        int height = v.getMeasuredHeight();
        listView.setStripes(lineBackgrounds, height);
    }


    /**
     * This function checks the length of the list. If the number of items is too
     * large, the height of the list is limited, such that the EditText element
     * will not drop out of the view.
     */
    private void checkListLength()
    {
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
        if (count <= mMaxListCount)
        {
            mLayoutParamsItems.height
                    = LinearLayout.LayoutParams.WRAP_CONTENT;
            //mEditText.append("l");
        }
        else
        {
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
     * Extend the SimpleCursorAdapter to strike through items. if STATUS ==
     * Shopping.Status.BOUGHT
     */
    public class mSimpleCursorAdapter
            extends SimpleCursorAdapter
    {

        /**
         * Constructor simply calls super class.
         *
         * @param context Context.
         * @param layout  Layout.
         * @param c       Cursor.
         * @param from    Projection from.
         * @param to      Projection to.
         */
        mSimpleCursorAdapter(final Context context, final int layout,
                final Cursor c, final String[] from, final int[] to)
        {
            super(context, layout, c, from, to);
        }

        /**
         * Additionally to the standard bindView, we also check for STATUS, and strike
         * the item through if BOUGHT.
         */
        @Override
        public void bindView(final View view, final Context context,
                final Cursor cursor)
        {
            //Log.i(TAG, "bindView " + view.toString());
            super.bindView(view, context, cursor);

			TextView t = (TextView) view.findViewById(R.id.name);
            // we have a check box now.. more visual and gets the point across
            CheckBox c = (CheckBox) view.findViewById(R.id.check);
            
            // Set font
            t.setTypeface(mTypeface);
            
            // Check for upper case:
            if (mUpperCaseFont) {
            	// Only upper case should be displayed
            	CharSequence cs = t.getText();
            	t.setText(cs.toString().toUpperCase());
            }
            
            t.setTextColor(mTextColor);
            
            
            if (mMarkType == mMarkCheckbox) {
        		c.setVisibility(CheckBox.VISIBLE);
        		if (cursor.getLong(mStringItemsSTATUS)
                        == Shopping.Status.BOUGHT)
                {
        			c.setChecked(true);
                } else {
                	c.setChecked(false);
                }
        	} else {
        		c.setVisibility(CheckBox.GONE);
        	}
            
            if (cursor.getLong(mStringItemsSTATUS)
                    == Shopping.Status.BOUGHT)
            {
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
                    //Drawable colorStrikethrough = context
                    //	.getResources().getDrawable(R.drawable.strikethrough);
//    				str.setSpan(new ForegroundColorSpan(0xFF006600), 0,
                    //str.setSpan(new ForegroundColorSpan
                    //        (getResources().getColor(R.color.darkgreen)), 0,
    				//		str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    				// color: 0x33336600
            	}
            	
            	if (mMarkType == mMarkAddtext) {
            		// very simple
            		t.append("... OK");
            	}
            	
                
				
			}
		}
		
	}
    
    

	// Handle the process of automatically updating enabled sensors:
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_UPDATE_CURSORS) {
            	mCursorListFilter.requery();
            	
            	
                if (mUpdating) {
                	sendMessageDelayed(obtainMessage(MESSAGE_UPDATE_CURSORS), mUpdateInterval);
                }

            }
        }
    };

    /**
     * Listens for intents for updates in the database.
     * @param context
     * @param intent
     */
    public class ListIntentReceiver extends IntentReceiver { 
	
	    public void onReceiveIntent(Context context, Intent intent) {
	    	String action = intent.getAction();
	    	Log.i(TAG, "ShoppingList received intent " + action);
	    	
	    	if (action.equals(OpenIntents.REFRESH_ACTION)) {
	    		mCursorListFilter.requery();
	    		
	    	}
	    }
    }
    
    ListIntentReceiver mIntentReceiver;
    
    /**
     * This method is called when the sending activity has finished, with the
     * result it supplied.
     * 
     * @param requestCode The original request code as given to
     *                    startActivity().
     * @param resultCode From sending activity as per setResult().
     * @param data From sending activity as per setResult().
     * @param extras From sending activity as per setResult().
     * 
     * @see android.app.Activity#onActivityResult(int, int, java.lang.String, android.os.Bundle)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			String data, Bundle extras) {
		Log.i(TAG, "ShoppingView: onActivityResult. ");

        if (requestCode == SUBACTIVITY_LIST_SHARE_SETTINGS) {
        	Log.i(TAG, "SUBACTIVITY_LIST_SHARE_SETTINGS");

        	if (resultCode == RESULT_CANCELED) {
                // Don't do anything.
        		Log.i(TAG, "RESULT_CANCELED");

            } else {
                // Broadcast the intent
            	Log.i(TAG, "Broadcast intent.");

            	Uri uri = Uri.parse(data);
            	
            	if (! mListUri.equals(uri)) {
            		Log.e(TAG, "Unexpected uri returned: Should be " + mListUri + " but was " + uri);
            		return;
            	}
            	
            	String sharename = extras.getString(Shopping.Lists.SHARE_NAME);
                String contacts = extras.getString(Shopping.Lists.SHARE_CONTACTS);
                
                Log.i(TAG, "Received bundle: sharename: " + sharename + ", contacts: " + contacts);
                mGTalkSender.sendList(contacts, sharename);
                
                // Here we also send the current content of the list
                // to all recipients.
                // This could probably be optimized - by sending
                // content only to the new recipients, as the
                // old ones should be in sync already.
                
             // First delete all items in list
                mCursorItems.moveTo(-1);
                while (mCursorItems.next())
                {
                	String itemName = mCursorItems.getString(mStringItemsITEMNAME);
                	Long status = mCursorItems.getLong(mStringItemsSTATUS);
            		Log.i(TAG, "Update shared item. "
                        + " recipients: " + contacts + ", shareName: " + sharename + ", item: " + itemName);
                    mGTalkSender.sendItemUpdate(contacts, sharename, 
                    	itemName, itemName,
                    	status, status);
                }
            }

        }
	}

}

