/* 
 * Copyright (C) 2008 OpenIntents.org
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

package org.openintents.countdown.list;

import org.openintents.countdown.LogConstants;
import org.openintents.countdown.PreferenceActivity;
import org.openintents.countdown.R;
import org.openintents.countdown.automation.AutomationActions;
import org.openintents.countdown.db.Countdown.Durations;
import org.openintents.countdown.util.CountdownUtils;
import org.openintents.countdown.util.NotificationState;
import org.openintents.distribution.DistributionLibraryListActivity;
import org.openintents.util.DateTimeFormater;
import org.openintents.util.MenuIntentOptionsWithIcons;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.ListView;


/**
 * Displays a list of notes. Will display notes from the {@link Uri}
 * provided in the intent if there is one, otherwise defaults to displaying the
 * contents of the {@link NotePadProvider}
 */
public class CountdownListActivity extends DistributionLibraryListActivity 
	implements CountdownCursorAdapter.OnCountdownClickListener,
	ListView.OnScrollListener{
    private static final String TAG = "CountdownListActivity";
    private static final boolean debug = LogConstants.debug;

    // Menu item ids
    private static final int MENU_ITEM_DELETE = Menu.FIRST;
    private static final int MENU_ITEM_INSERT = Menu.FIRST + 1;
    private static final int MENU_ITEM_SEND_BY_EMAIL = Menu.FIRST + 2;
 	private static final int MENU_SETTINGS = Menu.FIRST + 5;
    private static final int MENU_ITEM_EDIT = Menu.FIRST + 6;
    private static final int MENU_DISTRIBUTION_START = Menu.FIRST + 100; // must be last
	
	private static final int REQUEST_CODE_VERSION_CHECK = 1;
	
    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            Durations._ID, // 0
            Durations.TITLE, // 1
            Durations.DURATION, // 2
            Durations.DEADLINE_DATE, // 3
            Durations.USER_DEADLINE_DATE, // 4
            Durations.AUTOMATE_TEXT // 5
    };

    /** The index of the title column */
    private static final int COLUMN_INDEX_TITLE = 1;
    private static final int COLUMN_INDEX_DURATION = 2;
    
    private static final int MSG_UPDATE_DISPLAY = 1;

    // The different distinct states the activity can be run in.
    private int mState;
    protected static final int STATE_VIEW = 0;
    protected static final int STATE_PICK = 1;
    
    private Cursor mCursor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDistribution.setFirst(MENU_DISTRIBUTION_START, DIALOG_DISTRIBUTION_START);
        
        // Check whether EULA has been accepted
        // or information about new version can be presented.
		if (mDistribution.showEulaOrNewVersion()) {
            return;
        }
		
		CountdownUtils.setLocalizedStrings(this);
		
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        final Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Durations.CONTENT_URI);
        }
        
        mState = STATE_VIEW;

        final String action = intent.getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
        	mState = STATE_PICK;
        }

        // Inform the list we provide context menus for items
        setContentView(R.layout.countdownlist);
        getListView().setOnCreateContextMenuListener(this);
        getListView().setEmptyView(findViewById(R.id.empty));
        
        //getListView().setAddStatesFromChildren(false);
        getListView().setItemsCanFocus(true);
        
        if (mState == STATE_PICK) {
        	setTitle(R.string.title_pick);
        }
        
        // Perform a managed query. The Activity will handle closing and requerying the cursor
        // when needed.
        mCursor = managedQuery(getIntent().getData(), PROJECTION, null, null,
                Durations.DEFAULT_SORT_ORDER);
        
        if (mCursor.getCount() == 0) {
        	// Create a new timer immediately
        	insertNewNote();
        }

        // Used to map notes entries from the database to views
        //SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, R.layout.countdownlist_item, cursor,
        //        new String[] { Durations.TITLE , Durations.DURATION }, new int[] { android.R.id.text1 , android.R.id.text2 });
        
        Uri baseUri = getIntent().getData();
        boolean showButton = (mState == STATE_VIEW);
        CountdownCursorAdapter adapter = new CountdownCursorAdapter(this, mCursor, this, baseUri, showButton);
        
        setListAdapter(adapter);

        getListView().setOnScrollListener(this);
    }


	@Override
	protected void onResume() {
		super.onResume();

        IntentFilter filter = new IntentFilter(NotificationState.ACTION_NOTIFICATION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        
        getContentResolver().registerContentObserver(Durations.CONTENT_URI, true, mObserver);
        
		DateTimeFormater.getFormatFromPreferences(this);
		
		// Start periodic update
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_DISPLAY), 1000);
	}
    
    @Override
	protected void onPause() {
		super.onPause();

		getContentResolver().unregisterContentObserver(mObserver);
		
        unregisterReceiver(mReceiver);
        
		mHandler.removeMessages(MSG_UPDATE_DISPLAY);
	}



    @Override
	protected void onDestroy() {
		super.onDestroy();
	}



	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // This is our one standard application action -- inserting a
        // new note into the list.
        menu.add(0, MENU_ITEM_INSERT, 0, R.string.menu_insert)
                .setShortcut('3', 'c')
                .setIcon(android.R.drawable.ic_menu_add);
        
		menu.add(0, MENU_SETTINGS, 0, R.string.settings).setIcon(
				android.R.drawable.ic_menu_preferences).setShortcut('9', 's');

 		mDistribution.onCreateOptionsMenu(menu);

        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        //menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
        //        new ComponentName(this, CountdownListActivity.class), null, intent, 0, null);

        // Workaround to add icons:
        MenuIntentOptionsWithIcons menu2 = new MenuIntentOptionsWithIcons(this, menu);
        menu2.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                        new ComponentName(this, CountdownListActivity.class), null, intent, 0, null);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final boolean haveItems = getListAdapter().getCount() > 0;

        // If there are any notes in the list (which implies that one of
        // them is selected), then we need to generate the actions that
        // can be performed on the current selection.  This will be a combination
        // of our own specific actions along with any extensions that can be
        // found.
        if (haveItems) {
            // This is the selected item.
            Uri uri = ContentUris.withAppendedId(getIntent().getData(), getSelectedItemId());

            // Build menu...  always starts with the EDIT action...
            Intent[] specifics = new Intent[1];
            specifics[0] = new Intent(Intent.ACTION_EDIT, uri);
            MenuItem[] items = new MenuItem[1];

            // ... is followed by whatever other actions are available...
            Intent intent = new Intent(null, uri);
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, null, specifics, intent, 0,
                    items);

            // Give a shortcut to the edit action.
            if (items[0] != null) {
                items[0].setShortcut('1', 'e');
            }
        } else {
            menu.removeGroup(Menu.CATEGORY_ALTERNATIVE);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case MENU_ITEM_INSERT:
            insertNewNote();
            return true;
		case MENU_SETTINGS:
			showNotesListSettings();
			return true;
        }
        return super.onOptionsItemSelected(item);
    }

	/**
	 * Launch activity to insert a new item.
	 */
	private void insertNewNote() {
		// Launch activity to insert a new item
		startActivity(new Intent(Intent.ACTION_INSERT, getIntent().getData()));
	}

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return;
        }

        Cursor cursor = (Cursor) getListAdapter().getItem(info.position);
        if (cursor == null) {
            // For some reason the requested item isn't available, do nothing
            return;
        }

        // Setup the menu header
        menu.setHeaderTitle(cursor.getString(COLUMN_INDEX_TITLE));

        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_EDIT, 0, R.string.menu_edit);
        
        // Add a menu item to delete the note
        menu.add(0, MENU_ITEM_DELETE, 0, R.string.menu_delete);
    }
        
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info;
        try {
             info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        } catch (ClassCastException e) {
            Log.e(TAG, "bad menuInfo", e);
            return false;
        }

        switch (item.getItemId()) {
	        case MENU_ITEM_EDIT:
	        	editCountdown(info.id);
	            return true;
            case MENU_ITEM_DELETE: {
            	Uri uri = ContentUris.withAppendedId(getIntent().getData(), info.id);
                
            	// Cancel an ongoing alarm (if any)
            	AutomationActions.stopCountdown(this, uri);
            	
                // Delete the alarm that the context menu is for
                getContentResolver().delete(uri, null, null);
                return true;
            }
        }
        return false;
    }

	private void showNotesListSettings() {
		startActivity(new Intent(this, PreferenceActivity.class));
	}
	
    protected void onListItemClick(ListView l, View v, int position, long id) {
        pickOrEditCountdown(id);
    }

	public void onCountdownPanelClick(long id) {
        pickOrEditCountdown(id);
	}
	
	public void onStartClick(long id) {
		startCountdown(id);
	}
	
	public void onDismissClick(long id) {
		dismissCountdown(id);
	}

	/**
	 * Show the Countdown editor activity.
	 * @param id
	 */
	private void pickOrEditCountdown(long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            // The caller is waiting for us to return an alarm selected by
            // the user.  They have clicked on one, so return it now.
            setResult(RESULT_OK, new Intent().setData(uri));
            finish();
        } else {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
	}
	
	/**
	 * Show the Countdown editor activity.
	 * @param id
	 */
	private void editCountdown(long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        
        // Launch activity to view/edit the currently selected item
        startActivity(new Intent(Intent.ACTION_EDIT, uri));
	}

	private void startCountdown(long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		
		AutomationActions.startCountdown(this, uri);
        
        mCursor.requery();
    	
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_DISPLAY));
	}

	/**
	 * Dismiss the notification for the countdown.
	 * @param id
	 */
	private void dismissCountdown(long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		
		AutomationActions.stopCountdown(this, uri);

		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_DISPLAY));
	}
	

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
            int totalItemCount) {
    }
    

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        switch (scrollState) {
        case OnScrollListener.SCROLL_STATE_IDLE:
        	// Touch the views again
        	updateViews(false);
            break;
        case OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
            break;
        case OnScrollListener.SCROLL_STATE_FLING:
            break;
        }
    }
    
	/** Handle the process of updating the timer */
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (debug) Log.i(TAG, "Handle message");
			if (msg.what == MSG_UPDATE_DISPLAY) {
				updateViews(false);
			}
		}

	};

	private void updateViews(boolean forceAnotherUpdate) {
		// Update all visible views.
		ListView view = getListView();
		
		int first = view.getFirstVisiblePosition();
		int count = view.getChildCount();
		
		boolean update = false;
		
		for (int i=0; i<count; i++) {
			CountdownListItemView cliv = (CountdownListItemView) view.getChildAt(i);
			update |= cliv.updateCountdown();
		}
		
		mHandler.removeMessages(MSG_UPDATE_DISPLAY);
		
		if (update || forceAnotherUpdate) {
			mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_DISPLAY), 1000);
		}
	}
	
	BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// Update internal state:
			if (debug) Log.v(TAG, "onReceive()");

			// Force another update, because in the first
			// a view may still be in the old state,
			// so we check again a second later.
			updateViews(true);
		}
		
	};
	
	ContentObserver mObserver = new ContentObserver(mHandler) {

		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			if (debug) Log.v(TAG, "onChange()");
			
			// Force another update, because in the first
			// a view may still be in the old state,
			// so we check again a second later.
			updateViews(true);
		}
		
	};
}
