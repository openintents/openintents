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
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class NotesListCursorAdapter extends CursorAdapter {
	private static final String TAG = "NotesListCursorAdapter";

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
	
	/**
	 * Flag for slow list adapter.
	 */
    public boolean mBusy;
    
	public NotesListCursorAdapter(Context context, Cursor c, Intent intent) {
		super(context, c);
		mContext = context;
		mIntent = intent;
		
		mBusy = false;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		NotesListItemView nliv = (NotesListItemView) view;

		String title = cursor.getString(NotesList.COLUMN_INDEX_TITLE);
		String tags = cursor.getString(NotesList.COLUMN_INDEX_TAGS);
		long encrypted = cursor.getLong(NotesList.COLUMN_INDEX_ENCRYPTED);
		
		nliv.setEncrypted(encrypted);
		
		nliv.setTitle(title);

		/*
		if (encrypted == 0) {
			// Not encrypted:
			nliv.setTitle(title);
			nliv.setTags(tags);
			// Null tag means the view has the correct data
            nliv.setTag(null);
		} else {
			// encrypted
			String decrypted = mTitleHashMap.get(title);
			if (decrypted != null) {
				nliv.setTitle(decrypted);
				nliv.setTags(tags);
				// Null tag means the view has the correct data
	            nliv.setTag(null);
			} else {
				nliv.setTitle(mContext.getString(R.string.encrypted));
				nliv.setTags(tags);
				// Non-null tag means the view still needs to load it's data
				// Tag contains a pointer to a string with the encrypted title.
	            nliv.setTag(title);
			}
			/*
			if (!mBusy) {
				nliv.setTitle("set");
				nliv.setTitle("wow");
				// Null tag means the view has the correct data
	            nliv.setTag(null);
			} else {
				nliv.setTitle(mContext.getString(R.string.encrypted));
				nliv.setTags(tags);
				// Non-null tag means the view still needs to load it's data
	            nliv.setTag(this);
			}
			* /
		}
	    */
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new NotesListItemView(context);
	}	

    public void flushTitleHashMap() {
    	mEncryptedStringHashMap = new HashMap<String,String>();
    }

    /*
	@Override
	public Filter getFilter() {
		Log.i(TAG, "Request filter");
		
		return super.getFilter();
	}
*/
    
	/*
	@Override
	public CharSequence convertToString(Cursor cursor) {
		//return super.convertToString(cursor);

		Log.i(TAG, "convertToString" + cursor.getPosition() + " / " + cursor.getCount());
		
		return cursor.getString(NotesList.COLUMN_INDEX_TITLE);
	}
*/
	
	@Override
	public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
		Log.i(TAG, "runQueryOnBackgroundThread " + constraint + ", " + mIntent.getData());

		mCurrentFilter = constraint.toString();
		
		/*
		Cursor cursor = mContext.getContentResolver().query(mIntent.getData(), NotesList.PROJECTION, 
				"(" + Notes.TITLE + " like '" + constraint.toString() + "%' ) or ("
				 + Notes.TITLE + " like '% " + constraint.toString() + "%' )",
				new String[] { }, Notes.DEFAULT_SORT_ORDER);
		*/
		
		// We have to query all items and return a new object, because notes may be encrypted.

		Cursor dbcursor = mContext.getContentResolver().query(mIntent.getData(), NotesList.PROJECTION, 
				null, null, Notes.DEFAULT_SORT_ORDER);
		
		OpenMatrixCursor cursor = new OpenMatrixCursor(NotesList.PROJECTION, dbcursor.getCount());
		
		dbcursor.moveToPosition(-1);
		while (dbcursor.moveToNext()) {
			long id = dbcursor.getLong(NotesList.COLUMN_INDEX_ID);
			String title = dbcursor.getString(NotesList.COLUMN_INDEX_TITLE);
			String tags = dbcursor.getString(NotesList.COLUMN_INDEX_TAGS);
			long encrypted = dbcursor.getLong(NotesList.COLUMN_INDEX_ENCRYPTED);
			
			// Skip encrypted notes in filter.
			boolean skipEncrypted = false;
			
			if (encrypted > 0) {
				// get decrypted title:
				String decrypted = mEncryptedStringHashMap.get(title);
				if (decrypted != null) {
					title = decrypted;
				} else {
					// decrypt later
					mEncryptedStringList.add(title);
					
					title = mContext.getString(R.string.encrypted);
					skipEncrypted = true;
				}
			}
			
			// apply filter:
			if (TextUtils.isEmpty(mCurrentFilter) ||
					(!skipEncrypted && ((" " + title.toUpperCase()).contains(" " + mCurrentFilter.toUpperCase())))) {
				if (tags == null) {
					tags = "";
				}
				
				Object[] row = new Object[] {id, title, tags, encrypted};
				cursor.addRow(row);
			}
		}
		
		return cursor;
	}
    
    
}
