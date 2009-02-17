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

package org.openintents.convertcsv.notepad;

import org.openintents.provider.NotePad;
import org.openintents.provider.NotePad.Notes;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class NotepadUtils {

	private static final String TAG = "NotepadUtils";
	
	public static final String[] PROJECTION_NOTES = new String[] { Notes._ID,
			Notes.TITLE, Notes.NOTE, Notes.CREATED_DATE, Notes.MODIFIED_DATE};
	
	public static long addNote(Context context, String note, long encrypted, String tags) {

		// First check which columns are available:
		Uri testuri = ContentUris.withAppendedId(NotePad.Notes.CONTENT_URI, 0);
		Cursor c = context.getContentResolver().query(testuri, null, null, null,
		        NotePad.Notes.DEFAULT_SORT_ORDER);
		
		int COLUMN_INDEX_ENCRYPTED = c.getColumnIndex(NotePad.Notes.ENCRYPTED); // Introduced in 1.1.0
		int COLUMN_INDEX_TAGS = c.getColumnIndex(NotePad.Notes.TAGS); // Introduced in 1.1.0
		
		c.close();
		
		// Add item to list:
		ContentValues values = new ContentValues(1);
		values.put(NotePad.Notes.NOTE, note);
		String title = extractTitle(note);
		values.put(NotePad.Notes.TITLE, title);
		
		if (COLUMN_INDEX_ENCRYPTED > -1) {
			values.put(NotePad.Notes.ENCRYPTED, encrypted);
		}
		if (COLUMN_INDEX_TAGS > -1) {
			values.put(NotePad.Notes.TAGS, tags);
		}
		
		try {
			Uri uri = context.getContentResolver().insert(NotePad.Notes.CONTENT_URI,
					values);
			Log.i(TAG, "Insert new note: " + uri);
			return Long.parseLong(uri.getPathSegments().get(1));
		} catch (Exception e) {
			Log.i(TAG, "Insert item failed", e);
			return -1;
		}
	}
	
	public static String extractTitle(String note) {
        int length = note.length();
		String title = note.substring(0, Math.min(30, length));
        // Break at newline:
        int firstNewline = title.indexOf('\n');
        if (firstNewline > 0) {
            title = title.substring(0, firstNewline);
        } else if (length > 30) {
            // Break at space
            int lastSpace = title.lastIndexOf(' ');
            if (lastSpace > 0) {
                title = title.substring(0, lastSpace);
            }
        }
        return title;
	}
	
	public static void deleteAllNotes(Context context) {
		try {
			Log.i(TAG, "Deleting ALL notes");
			context.getContentResolver().delete(NotePad.Notes.CONTENT_URI, "", null);
			Log.i(TAG, "ALL notes deleted");
		} catch (Exception e) {
			Log.i(TAG, "Failed to delete all notes", e);
		}
	}
	
	public static void deleteNoteById(Context context, int noteId) {
		Log.i(TAG, "Deleting note " + noteId);
		String[] args = { Integer.toString(noteId) };
		
		context.getContentResolver().delete(NotePad.Notes.CONTENT_URI, Notes._ID + " =?", args);
	}
	
	public static int findNoteByTitle(Context context, String title) {
		String[] projection = { Notes._ID, Notes._ID };
		String[] args = { title };
		int result = -1;
		
		Cursor cursor = context.getContentResolver().query(NotePad.Notes.CONTENT_URI, projection, Notes.TITLE + " =?", args, null);
		
		// Did we get any results?
		if (cursor.moveToFirst()) {
			result = cursor.getInt(0);
		}
		
		cursor.close();
		return result;		
	}
}
