package org.openintents.notepad.noteslist;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class NotesListCursorAdapter extends CursorAdapter {
	private static final String TAG = "NotesListCursorAdapter";

	Context mContext;

	public NotesListCursorAdapter(Context context, Cursor c) {
		super(context, c);
		mContext = context;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		NotesListItemView nliv = (NotesListItemView) view;

		String title = cursor.getString(NotesList.COLUMN_INDEX_TITLE);
		String tags = cursor.getString(NotesList.COLUMN_INDEX_TAGS);
		long encrypted = cursor.getLong(NotesList.COLUMN_INDEX_ENCRYPTED);
		
		nliv.setTitle(title);
		nliv.setTags(tags);
		nliv.setEncrypted(encrypted);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new NotesListItemView(context);
	}	

}
