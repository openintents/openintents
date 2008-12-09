package org.openintents.convertcsv.notepad;

import java.io.IOException;
import java.io.Writer;

import org.openintents.convertcsv.opencsv.CSVWriter;
import org.openintents.provider.NotePad;

import android.content.Context;
import android.database.Cursor;

public class ExportCsv {

	Context mContext;
	
	public ExportCsv(Context context) {
		mContext = context;
	}
	
	/**
	 * @param dos
	 * @throws IOException
	 */
	public void exportCsv(Writer writer) throws IOException {

		CSVWriter csvwriter = new CSVWriter(writer);
		
		/*
		csvwriter.write("Note");
		csvwriter.write("Encrypted");
		csvwriter.write("Category");
		csvwriter.writeNewline();
		*/
		
		Cursor c = mContext.getContentResolver().query(NotePad.Notes.CONTENT_URI, NotepadUtils.PROJECTION_NOTES, null, null,
		        NotePad.Notes.DEFAULT_SORT_ORDER);
		
		if (c != null) {
			int COLUMN_INDEX_NOTE = c.getColumnIndexOrThrow(NotePad.Notes.NOTE);
			int COLUMN_INDEX_ID = c.getColumnIndexOrThrow(NotePad.Notes._ID);
			
			while (c.moveToNext()) {
		    	String note = c.getString(COLUMN_INDEX_NOTE);
		    	long id = c.getLong(COLUMN_INDEX_ID);
		    	
		    	String encrypted = "0"; // Not encrypted
		    	
		    	String category = "";
		    	
		    	csvwriter.write(note);
		    	csvwriter.write(encrypted);
		    	csvwriter.write(category);
		    	csvwriter.writeNewline();
			}
		}
		
		csvwriter.close();
	}

}
