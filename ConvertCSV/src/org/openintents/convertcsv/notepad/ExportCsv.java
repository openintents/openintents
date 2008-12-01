package org.openintents.convertcsv.notepad;

import java.io.DataOutputStream;
import java.io.IOException;

import org.openintents.provider.NotePad;
import org.openintents.provider.Shopping;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class ExportCsv {

	Context mContext;
	
	public ExportCsv(Context context) {
		mContext = context;
	}
	
	/**
	 * @param dos
	 * @throws IOException
	 */
	public void exportCsv(DataOutputStream dos) throws IOException {
		dos.writeBytes("Subject" + "," + "% Complete" + "," + "Categories" + "\n");
	
		Cursor c = mContext.getContentResolver().query(NotePad.Notes.CONTENT_URI, NotepadUtils.PROJECTION_NOTES, null, null,
		        NotePad.Notes.DEFAULT_SORT_ORDER);
		
		if (c != null) {
			int listcount = c.getCount();
	
			Log.i(ConvertCsvActivity.TAG, "Number of lists: " + listcount);
			
			while (c.moveToNext()) {
		    	String note = c.getString(c.getColumnIndexOrThrow(NotePad.Notes.NOTE));
		    	long id = c.getLong(c.getColumnIndexOrThrow(NotePad.Notes._ID));
		    	
		    	Log.i(ConvertCsvActivity.TAG, "List: " + note);
		    	
		    	String encrypted = "0"; // Not encrypted
		    	
		    	String category = "";
		    	
			    dos.writeBytes("\"" + note + "\",\"" + encrypted + "\",\"" + category + "\"\n");
			}
		}
	}

}
