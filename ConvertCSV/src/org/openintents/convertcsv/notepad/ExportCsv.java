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

import java.io.IOException;
import java.io.Writer;

import org.openintents.convertcsv.common.ConvertCsvBaseActivity;
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
	public void exportCsv(Writer writer, String format) throws IOException {

		//boolean isPalm = format.equals(FORMAT_PALM_CSV); // Palm is default.
		boolean isOutlookNotes = format.equals(ImportCsv.FORMAT_OUTLOOK_NOTES);
		
		CSVWriter csvwriter = new CSVWriter(writer);
		
		//csvwriter.setLineEnd("\r\n");
		
		//String lineEnd = "\r\r\n"; 
		//csvwriter.setLineEnd(lineEnd);
		
		if (isOutlookNotes) {
			csvwriter.writeValue("Note Body");
			csvwriter.writeValue("Categories");
			csvwriter.writeValue("Note Color");
			csvwriter.writeValue("Priority");
			csvwriter.writeValue("Sensitivity");
			csvwriter.writeNewline();
		} else {
			// No header line for Palm
			/*
			csvwriter.write("Note");
			csvwriter.write("Encrypted");
			csvwriter.write("Category");
			csvwriter.writeNewline();
			*/
		}
		
		Cursor c = mContext.getContentResolver().query(NotePad.Notes.CONTENT_URI, null, null, null,
		        NotePad.Notes.DEFAULT_SORT_ORDER);
		
		if (c != null) {
			int COLUMN_INDEX_NOTE = c.getColumnIndexOrThrow(NotePad.Notes.NOTE);
			int COLUMN_INDEX_ID = c.getColumnIndexOrThrow(NotePad.Notes._ID);
			int COLUMN_INDEX_ENCRYPTED = c.getColumnIndex(NotePad.Notes.ENCRYPTED); // Introduced in 1.1.0
			int COLUMN_INDEX_TAGS = c.getColumnIndex(NotePad.Notes.TAGS); // Introduced in 1.1.0
			
			ConvertCsvBaseActivity.dispatchSetMaxProgress(c.getCount());
			int progress = 0;
			
			while (c.moveToNext()) {
				ConvertCsvBaseActivity.dispatchConversionProgress(progress++);
				
		    	String note = c.getString(COLUMN_INDEX_NOTE);
		    	long id = c.getLong(COLUMN_INDEX_ID);

		    	String encrypted = "0"; // Not encrypted
		    	
		    	String category = "";
		    	
		    	if (COLUMN_INDEX_ENCRYPTED > -1) {
		    		encrypted = "" + c.getLong(COLUMN_INDEX_ENCRYPTED);
		    	}
		    	
		    	if (COLUMN_INDEX_TAGS > -1) {
		    		category = c.getString(COLUMN_INDEX_TAGS);
		    		if (category == null) {
		    			category = "";
		    		}
		    	}
		    	
		    	if (isOutlookNotes) {
		    		String notecolor = "3";
		    		String priority = "Normal";
		    		String sensitivity = "Normal";
		    		if (!encrypted.equals("0")) {
		    			sensitivity = "Encrypted " + encrypted;
		    		}
			    	csvwriter.write(note);
			    	csvwriter.writeValue(category);
			    	csvwriter.writeValue(notecolor);
			    	csvwriter.writeValue(priority);
			    	csvwriter.writeValue(sensitivity);
		    	} else {
		    		// Palm CSV format
		    		
			    	// Palm Windows specific line ending
					// that is only used within notes.
					note = note.replaceAll("\n", "\r\r\n");
			    	
			    	csvwriter.write(note);
			    	csvwriter.write(encrypted);
			    	csvwriter.write(category);
		    	}
		    	csvwriter.writeNewline();
			}
		}
		
		csvwriter.close();
	}

}
