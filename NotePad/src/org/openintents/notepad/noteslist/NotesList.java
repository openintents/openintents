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

/*
 * Original copyright:
 * Based on the Android SDK sample application NotePad.
 * Copyright (C) 2007 Google Inc.
 * Licensed under the Apache License, Version 2.0.
 */

package org.openintents.notepad.noteslist;

import org.openintents.distribution.AboutActivity;
import org.openintents.distribution.EulaActivity;
import org.openintents.distribution.UpdateMenu;
import org.openintents.intents.CryptoIntents;
import org.openintents.notepad.NotePad;
import org.openintents.notepad.NotePadIntents;
import org.openintents.notepad.NotePadProvider;
import org.openintents.notepad.R;
import org.openintents.notepad.NotePad.Notes;
import org.openintents.notepad.R.id;
import org.openintents.notepad.R.layout;
import org.openintents.notepad.R.string;
import org.openintents.notepad.crypto.EncryptActivity;
import org.openintents.util.MenuIntentOptionsWithIcons;

import android.app.ListActivity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

/**
 * Displays a list of notes. Will display notes from the {@link Uri} provided in
 * the intent if there is one, otherwise defaults to displaying the contents of
 * the {@link NotePadProvider}
 */
public class NotesList extends ListActivity {
	private static final String TAG = "NotesList";

	// Menu item ids
	private static final int MENU_ITEM_DELETE = Menu.FIRST;
	private static final int MENU_ITEM_INSERT = Menu.FIRST + 1;
	private static final int MENU_ITEM_SEND_BY_EMAIL = Menu.FIRST + 2;
	private static final int MENU_ABOUT = Menu.FIRST + 3;
	private static final int MENU_UPDATE = Menu.FIRST + 4;
	private static final int MENU_ITEM_ENCRYPT = Menu.FIRST + 5;
	
	/**
	 * A group id for alternative menu items.
	 */
	private final static int CATEGORY_ALTERNATIVE_GLOBAL = 1;
	
	/**
	 * The columns we are interested in from the database
	 */
	protected static final String[] PROJECTION = new String[] { Notes._ID, // 0
			Notes.TITLE, // 1
			Notes.TAGS, // 2
			Notes.ENCRYPTED // 3
	};

	/** The index of the title column */
	protected static final int COLUMN_INDEX_TITLE = 1;
	protected static final int COLUMN_INDEX_TAGS = 2;
	protected static final int COLUMN_INDEX_ENCRYPTED = 3;

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
			intent.setData(Notes.CONTENT_URI);
		}

		// Inform the list we provide context menus for items
		setContentView(R.layout.noteslist);
		getListView().setOnCreateContextMenuListener(this);
		getListView().setEmptyView(findViewById(R.id.empty));

		/*
		 * Button b = (Button) findViewById(R.id.add); b.setOnClickListener(new
		 * Button.OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) { insertNewNote(); }
		 * 
		 * });
		 */

		// Perform a managed query. The Activity will handle closing and
		// requerying the cursor
		// when needed.
		Cursor cursor = managedQuery(getIntent().getData(), PROJECTION, null,
				null, Notes.DEFAULT_SORT_ORDER);

		/*
		// Used to map notes entries from the database to views
		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				R.layout.noteslist_item, cursor, new String[] { Notes.TITLE },
				new int[] { android.R.id.text1 });
				*/
		NotesListCursorAdapter adapter = new NotesListCursorAdapter(this, cursor);
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// This is our one standard application action -- inserting a
		// new note into the list.
		menu.add(0, MENU_ITEM_INSERT, 0, R.string.menu_insert).setShortcut('3',
				'a').setIcon(android.R.drawable.ic_menu_add);

		UpdateMenu.addUpdateMenu(this, menu, 0, MENU_UPDATE, 0, R.string.update);
		
		menu.add(0, MENU_ABOUT, 0, R.string.about).setIcon(
				android.R.drawable.ic_menu_info_details).setShortcut('0', 'a');

		// Generate any additional actions that can be performed on the
		// overall list. In a normal install, there are no additional
		// actions found here, but this allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		Log.i(TAG, "Building options menu for: " + intent.getDataString());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		//menu
		//		.addIntentOptions(CATEGORY_ALTERNATIVE_GLOBAL, 0, 0,
		//				new ComponentName(this, NotesList.class), null, intent,
		//				0, null);

        // Workaround to add icons:
        MenuIntentOptionsWithIcons menu2 = new MenuIntentOptionsWithIcons(this, menu);
        menu2.addIntentOptions(CATEGORY_ALTERNATIVE_GLOBAL, 0, 0,
                        new ComponentName(this, NotesList.class), null, intent, 0, null);
        
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		final boolean haveItems = getListAdapter().getCount() > 0;

		// If there are any notes in the list (which implies that one of
		// them is selected), then we need to generate the actions that
		// can be performed on the current selection. This will be a combination
		// of our own specific actions along with any extensions that can be
		// found.
		if (haveItems) {
			// This is the selected item.
			Uri uri = ContentUris.withAppendedId(getIntent().getData(),
					getSelectedItemId());

			// Build menu... always starts with the EDIT action...
			Intent[] specifics = new Intent[1];
			specifics[0] = new Intent(Intent.ACTION_EDIT, uri);
			MenuItem[] items = new MenuItem[1];

			// ... is followed by whatever other actions are available...
			Intent intent = new Intent(null, uri);
			intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
			//menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, null,
			//		specifics, intent, 0, items);
			
			// Workaround to add icons:
	        MenuIntentOptionsWithIcons menu2 = new MenuIntentOptionsWithIcons(this, menu);
	        menu2.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
	                        null, specifics, intent, 0, items);
	        
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
	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
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

		// Add a menu item to send the note
		menu.add(0, MENU_ITEM_SEND_BY_EMAIL, 0, R.string.menu_send_by_email);

		// Add a menu item to send the note
		menu.add(0, MENU_ITEM_ENCRYPT, 0, R.string.menu_encrypt);
		
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
			// Delete the note that the context menu is for
			Uri noteUri = ContentUris.withAppendedId(getIntent().getData(),
					info.id);
			getContentResolver().delete(noteUri, null, null);
			return true;
		}
		case MENU_ITEM_SEND_BY_EMAIL:
			sendNoteByEmail(info.id);
			return true;
		case MENU_ITEM_ENCRYPT:
			encryptNote(info.id);
			return true;
		}
		return false;
	}

	private void sendNoteByEmail(long id) {
		// Obtain Uri for the context menu
		Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), id);
		// getContentResolver().(noteUri, null, null);

		Cursor c = getContentResolver().query(noteUri,
				new String[] { NotePad.Notes.TITLE, NotePad.Notes.NOTE }, null,
				null, Notes.DEFAULT_SORT_ORDER);

		String title = "";
		String content = getString(R.string.empty_note);
		if (c != null) {
			c.moveToFirst();
			title = c.getString(0);
			content = c.getString(1);
		}

		Log.i(TAG, "Title to send: " + title);
		Log.i(TAG, "Content to send: " + content);

		Intent i = new Intent();
		i.setAction(Intent.ACTION_SEND);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_SUBJECT, title);
		i.putExtra(Intent.EXTRA_TEXT, content);

		try {
			startActivity(i);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(this, R.string.email_not_available,
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Email client not installed");
		}
	}

	private void encryptNote(long id) {
		// Obtain Uri for the context menu
		Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), id);
		// getContentResolver().(noteUri, null, null);

		Cursor c = getContentResolver().query(noteUri,
				new String[] { NotePad.Notes.TITLE, NotePad.Notes.NOTE, NotePad.Notes.ENCRYPTED }, null,
				null, Notes.DEFAULT_SORT_ORDER);

		String title = "";
		String text = getString(R.string.empty_note);
		int encrypted = 0;
		if (c != null) {
			c.moveToFirst();
			title = c.getString(0);
			text = c.getString(1);
			encrypted = c.getInt(2);
		}

		if (encrypted != 0) {
			Toast.makeText(this,
					R.string.already_encrypted,
					Toast.LENGTH_SHORT).show();
			return;
		}

		Intent i = new Intent(this, EncryptActivity.class);
		i.putExtra(CryptoIntents.EXTRA_TEXT_ARRAY, new String[] {text, title});
		i.putExtra(NotePadIntents.EXTRA_URI, noteUri.toString());
		startActivity(i);
	}
	
	private void showAboutBox() {
		startActivity(new Intent(this, AboutActivity.class));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Uri uri = ContentUris.withAppendedId(getIntent().getData(), id);

		String action = getIntent().getAction();
		if (Intent.ACTION_PICK.equals(action)
				|| Intent.ACTION_GET_CONTENT.equals(action)) {
			// The caller is waiting for us to return a note selected by
			// the user. The have clicked on one, so return it now.
			setResult(RESULT_OK, new Intent().setData(uri));
		} else {
			// Launch activity to view/edit the currently selected item
			startActivity(new Intent(Intent.ACTION_EDIT, uri));
		}
	}
	
}
