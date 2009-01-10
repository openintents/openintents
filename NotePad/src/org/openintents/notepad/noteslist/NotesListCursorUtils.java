package org.openintents.notepad.noteslist;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.openintents.notepad.R;
import org.openintents.notepad.NotePad.Notes;
import org.openintents.notepad.util.OpenMatrixCursor;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

public class NotesListCursorUtils {

	private static final String TAG = "NotesListCursorUtils";
	
	static final String TITLE_DECRYPTED = "title_decrypted";
	static final String TAGS_DECRYPTED = "tags_decrypted";
	
	/**
	 * The columns we are interested in from the database
	 */
	protected static final String[] PROJECTION_DB = new String[] { 
			Notes._ID, // 0
			Notes.TITLE, // 1
			Notes.TAGS, // 2
			Notes.ENCRYPTED // 3
	};

	
	/**
	 * This cursors' columns
	 */
	protected static final String[] PROJECTION = new String[] { 
			Notes._ID, // 0
			Notes.TITLE, // 1
			Notes.TAGS, // 2
			Notes.ENCRYPTED, // 3
			TITLE_DECRYPTED, // 4
			TAGS_DECRYPTED // 5
	};

	protected static final int COLUMN_INDEX_ID = 0;
	/** The index of the title column */
	protected static final int COLUMN_INDEX_TITLE = 1;
	protected static final int COLUMN_INDEX_TAGS = 2;
	protected static final int COLUMN_INDEX_ENCRYPTED = 3;
	/** Contains the encrypted title if it has not been decrypted yet */
	protected static final int COLUMN_INDEX_TITLE_ENCRYPTED = 4;
	protected static final int COLUMN_INDEX_TAGS_ENCRYPTED = 5;
	
	Context mContext;
	Intent mIntent;

	public String mCurrentFilter;
	
	/**
	 * Map encrypted titles to decrypted ones.
	 */
	public static HashMap<String,String> mEncryptedStringHashMap = new HashMap<String,String>();
	
	/**
	 * List containing all encrypted strings. These are decrypted one at a time while idle.
	 * The list is synchronized because background threads may add items to it.
	 */
	public static List<String> mEncryptedStringList = Collections.synchronizedList(new LinkedList<String>());
	
	public NotesListCursorUtils(Context context, Intent intent) {
		mContext = context;
		mIntent = intent;
	}

	/** 
	 * Return a cursor with decrypted information.
	 * 
	 * @param constraint
	 */
	public Cursor query(CharSequence constraint) {

		// We have to query all items and return a new object, because notes may be encrypted.

		if (constraint != null) {
			mCurrentFilter = constraint.toString();
		} else {
			mCurrentFilter = "";
		}
		
		Cursor dbcursor = mContext.getContentResolver().query(mIntent.getData(), PROJECTION_DB, 
				null, null, Notes.DEFAULT_SORT_ORDER);
		
		Log.i(TAG, "Cursor count: " + dbcursor.getCount());
		
		OpenMatrixCursor cursor = new OpenMatrixCursor(PROJECTION, dbcursor.getCount());
		
		String encryptedlabel = mContext.getString(R.string.encrypted);
		
		dbcursor.moveToPosition(-1);
		while (dbcursor.moveToNext()) {
			long id = dbcursor.getLong(COLUMN_INDEX_ID);
			String title = dbcursor.getString(COLUMN_INDEX_TITLE);
			String tags = dbcursor.getString(COLUMN_INDEX_TAGS);
			long encrypted = dbcursor.getLong(COLUMN_INDEX_ENCRYPTED);
			String title_encrypted = null;
			String tags_encrypted = null;
			
			// Skip encrypted notes in filter.
			boolean skipEncrypted = false;
			
			if (encrypted > 0) {
				// get decrypted title:
				String decrypted = mEncryptedStringHashMap.get(title);
				if (decrypted != null) {
					title = decrypted;
				} else {
					title_encrypted = title;
					title = encryptedlabel;
					// decrypt later
					mEncryptedStringList.add(title_encrypted);
					skipEncrypted = true;
				}
			}
			
			// apply filter:
			if (TextUtils.isEmpty(mCurrentFilter) ||
					(!skipEncrypted && ((" " + title.toUpperCase()).contains(" " + mCurrentFilter.toUpperCase())))) {
				if (tags == null) {
					tags = "";
				}
				
				Object[] row = new Object[] {id, title, tags, encrypted, title_encrypted, tags_encrypted};
				cursor.addRow(row);
			}
		}
		
		return cursor;
	}

    public static void flushDecryptedStringHashMap() {
    	mEncryptedStringHashMap = new HashMap<String,String>();
    }
}
