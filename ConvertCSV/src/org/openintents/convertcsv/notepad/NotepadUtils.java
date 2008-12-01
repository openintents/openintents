package org.openintents.convertcsv.notepad;

import org.openintents.provider.NotePad.Notes;

public class NotepadUtils {

	private static final String TAG = "NotepadUtils";
	
	public static final String[] PROJECTION_NOTES = new String[] { Notes._ID,
			Notes.TITLE, Notes.NOTE, Notes.CREATED_DATE, Notes.MODIFIED_DATE};
}
