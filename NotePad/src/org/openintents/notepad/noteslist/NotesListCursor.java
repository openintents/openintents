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
import android.database.AbstractCursor;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;

public class NotesListCursor extends OpenMatrixCursor {

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
	
	static boolean mLoggedIn = true;
	
	Context mContext;
	Intent mIntent;
	//OpenMatrixCursor mCursor;
	
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
	
	public NotesListCursor(Context context, Intent intent) {
		super(PROJECTION);
		mContext = context;
		mIntent = intent;
		mCurrentFilter = null;
	}

	@Override
	public boolean requery() {
		runQuery(mCurrentFilter);
		
		return super.requery();
	}


	/** 
	 * Return a new cursor with decrypted information.
	 * 
	 * @param constraint
	 */
	public Cursor query(CharSequence constraint) {
		NotesListCursor cursor = new NotesListCursor(mContext, mIntent);
		cursor.runQuery(constraint);
		return cursor;
	}
	
	/** 
	 * Return a query with decrypted information on the current cursor.
	 * 
	 * @param constraint
	 */
	public void runQuery(CharSequence constraint) {

		// We have to query all items and return a new object, because notes may be encrypted.

		if (constraint != null) {
			mCurrentFilter = constraint.toString();
		} else {
			mCurrentFilter = null;
		}
		
		Cursor dbcursor = mContext.getContentResolver().query(mIntent.getData(), PROJECTION_DB, 
				null, null, Notes.DEFAULT_SORT_ORDER);
		
		Log.i(TAG, "Cursor count: " + dbcursor.getCount());
		
		//mCursor = new OpenMatrixCursor(PROJECTION, dbcursor.getCount());
		
		reset();
		
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
					addForEncryption(title_encrypted);
					skipEncrypted = true;
				}

				if (!mLoggedIn) {
					// suppress all decrypted output
					title = encryptedlabel;
				}
			}
			
			// apply filter:
			if (TextUtils.isEmpty(mCurrentFilter) ||
					(!skipEncrypted && ((" " + title.toUpperCase()).contains(" " + mCurrentFilter.toUpperCase())))) {
				if (tags == null) {
					tags = "";
				}
				
				Object[] row = new Object[] {id, title, tags, encrypted, title_encrypted, tags_encrypted};
				addRow(row);
			}
		}
	}

    public static void flushDecryptedStringHashMap() {
    	mEncryptedStringHashMap = new HashMap<String,String>();
    	mLoggedIn = false;
    }

    public static void addForEncryption(String encryptedString) {
    	// Check whether it does not already exist:
    	if (!mEncryptedStringList.contains(encryptedString)) {
    		mEncryptedStringList.add(encryptedString);
    	}
    }
    
    public static String getNextEncryptedString() {
    	if (!NotesListCursor.mEncryptedStringList.isEmpty()) {
	    	String encryptedString = NotesListCursor.mEncryptedStringList.remove(0);
	    	return encryptedString;
	    } else {
	    	return null;
	    }
    }
}
