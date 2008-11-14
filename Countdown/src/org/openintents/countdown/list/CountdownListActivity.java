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

import org.openintents.countdown.R;
import org.openintents.countdown.db.Countdown;
import org.openintents.countdown.db.Countdown.Durations;
import org.openintents.countdown.util.CountdownUtils;
import org.openintents.distribution.AboutActivity;
import org.openintents.distribution.EulaActivity;
import org.openintents.distribution.UpdateMenu;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;


/**
 * Displays a list of notes. Will display notes from the {@link Uri}
 * provided in the intent if there is one, otherwise defaults to displaying the
 * contents of the {@link NotePadProvider}
 */
public class CountdownListActivity extends ListActivity 
	implements CountdownCursorAdapter.OnCountdownClickListener {
    private static final String TAG = "NotesList";

    // Menu item ids
    private static final int MENU_ITEM_DELETE = Menu.FIRST;
    private static final int MENU_ITEM_INSERT = Menu.FIRST + 1;
    private static final int MENU_ITEM_SEND_BY_EMAIL = Menu.FIRST + 2;
	private static final int MENU_ABOUT = Menu.FIRST + 3;
	private static final int MENU_UPDATE = Menu.FIRST + 4;
	
	private static final int REQUEST_CODE_VERSION_CHECK = 1;
    /**
     * The columns we are interested in from the database
     */
    private static final String[] PROJECTION = new String[] {
            Durations._ID, // 0
            Durations.TITLE, // 1
            Durations.DURATION, // 2
            Durations.DEADLINE_DATE // 3
    };

    /** The index of the title column */
    private static final int COLUMN_INDEX_TITLE = 1;
    private static final int COLUMN_INDEX_DURATION = 2;
    
    private static final int MSG_UPDATE_DISPLAY = 1;
    
    private Cursor mCursor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

		if (!EulaActivity.checkEula(this)) {
			return;
		}
		
        setDefaultKeyMode(DEFAULT_KEYS_SHORTCUT);

        // If no data was given in the intent (because we were started
        // as a MAIN activity), then use our default content provider.
        Intent intent = getIntent();
        if (intent.getData() == null) {
            intent.setData(Durations.CONTENT_URI);
        }

        // Inform the list we provide context menus for items
        setContentView(R.layout.countdownlist);
        getListView().setOnCreateContextMenuListener(this);
        getListView().setEmptyView(findViewById(R.id.empty));
        
        //getListView().setAddStatesFromChildren(false);
        getListView().setItemsCanFocus(true);
        
        
        
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
        
        CountdownCursorAdapter adapter = new CountdownCursorAdapter(this, mCursor, this);
        
        setListAdapter(adapter);
    }


	@Override
	protected void onResume() {
		super.onResume();
		
		// Start periodic update
		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_DISPLAY), 1000);
	}
    
    @Override
	protected void onPause() {
		super.onPause();
		
		mHandler.removeMessages(MSG_UPDATE_DISPLAY);
	}






	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // This is our one standard application action -- inserting a
        // new note into the list.
        menu.add(0, MENU_ITEM_INSERT, 0, R.string.menu_insert)
                .setShortcut('3', 'a')
                .setIcon(android.R.drawable.ic_menu_add);
        
        UpdateMenu.addUpdateMenu(this, menu, 0, MENU_UPDATE, 0, R.string.menu_update);
		
        menu.add(0, MENU_ABOUT, 0, R.string.about)
		  .setIcon(android.R.drawable.ic_menu_info_details) .setShortcut('0', 'a');

        // Generate any additional actions that can be performed on the
        // overall list.  In a normal install, there are no additional
        // actions found here, but this allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
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
		case MENU_ABOUT:
			showAboutBox();
			return true;
		case MENU_UPDATE:
			UpdateMenu.showUpdateBox(this);
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
            case MENU_ITEM_DELETE: {
            	Uri uri = ContentUris.withAppendedId(getIntent().getData(), info.id);
                
            	// Cancel an ongoing alarm (if any)
            	CountdownUtils.cancelAlarm(this, uri);
            	
                // Delete the alarm that the context menu is for
                getContentResolver().delete(uri, null, null);
                return true;
            }
        }
        return false;
    }

	private void showAboutBox() {
		startActivity(new Intent(this, AboutActivity.class));
	}
	
    protected void onListItemClick(ListView l, View v, int position, long id) {
        editCountdown(id);
    }

	public void onCountdownPanelClick(long id) {
        editCountdown(id);
	}
	
	public void onStartClick(long id) {
		startCountdown(id);
	}

	/**
	 * Show the Countdown editor activity.
	 * @param id
	 */
	private void editCountdown(long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
        
        String action = getIntent().getAction();
        if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_GET_CONTENT.equals(action)) {
            // The caller is waiting for us to return an alarm selected by
            // the user.  They have clicked on one, so return it now.
            setResult(RESULT_OK, new Intent().setData(uri));
        } else {
            // Launch activity to view/edit the currently selected item
            startActivity(new Intent(Intent.ACTION_EDIT, uri));
        }
	}

	private void startCountdown(long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);
		
        // Get the data
        Cursor c = getContentResolver().query(uri, Durations.PROJECTION, null, null,
                Countdown.Durations.DEFAULT_SORT_ORDER);
		
    	long now = System.currentTimeMillis();
    	
    	long duration = 0;
    	
    	if (c != null) {
        	c.moveToFirst();
        	duration = c.getLong(c.getColumnIndexOrThrow(Durations.DURATION));
    	}
		
    	long deadline = now + duration;
    	
    	CountdownUtils.setAlarm(this, uri, deadline);
    	
    	// Write back modification
    	ContentValues values = new ContentValues();
    	values.put(Durations.DEADLINE_DATE, deadline);
    	
        getContentResolver().update(uri, values, null, null);
        mCursor.requery();
    	
		mHandler.sendMessage(mHandler.obtainMessage(MSG_UPDATE_DISPLAY));
	}
	
	/** Handle the process of updating the timer */
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			Log.i(TAG, "Handle message");
			if (msg.what == MSG_UPDATE_DISPLAY) {
				// Update all visible views.
				ListView view = getListView();
				
	            int first = view.getFirstVisiblePosition();
	            int count = view.getChildCount();
	            
	            boolean update = false;
	            
	            for (int i=0; i<count; i++) {
	            	CountdownListItemView cliv = (CountdownListItemView) view.getChildAt(i);
	            	update |= cliv.updateCountdown();
	            }
	            
	            if (update) {
	            	mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_DISPLAY), 1000);
	            }
			}
		}
	};
}
