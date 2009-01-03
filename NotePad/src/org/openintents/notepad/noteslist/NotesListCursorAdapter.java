package org.openintents.notepad.noteslist;

import org.openintents.notepad.R;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class NotesListCursorAdapter extends CursorAdapter {
	private static final String TAG = "NotesListCursorAdapter";

	Context mContext;
	public TitleHash mTitleHash;

	/**
	 * Flag for slow list adapter.
	 */
    public boolean mBusy;
    
	public NotesListCursorAdapter(Context context, Cursor c) {
		super(context, c);
		mContext = context;
		mTitleHash = new TitleHash(mContext);
		
		mBusy = false;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		NotesListItemView nliv = (NotesListItemView) view;

		String title = cursor.getString(NotesList.COLUMN_INDEX_TITLE);
		String tags = cursor.getString(NotesList.COLUMN_INDEX_TAGS);
		long encrypted = cursor.getLong(NotesList.COLUMN_INDEX_ENCRYPTED);
		
		nliv.setEncrypted(encrypted);
		if (encrypted == 0) {
			// Not encrypted:
			nliv.setTitle(title);
			nliv.setTags(tags);
			// Null tag means the view has the correct data
            nliv.setTag(null);
		} else {
			// encrypted
			String decrypted = mTitleHash.getDecryptedTitle(title);
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
			*/
		}
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new NotesListItemView(context);
	}	

}
