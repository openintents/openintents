package org.openintents.convertcsv.notepad;

import java.io.IOException;
import java.io.Reader;

import org.openintents.convertcsv.opencsv.CSVReader;

import android.content.Context;

public class ImportCsv {

	Context mContext;
	
	public ImportCsv(Context context) {
		mContext = context;
	}
	
	/**
	 * @param dis
	 * @throws IOException
	 */
	public void importCsv(Reader reader) throws IOException {
		
		CSVReader csvreader = new CSVReader(reader);
	    String [] nextLine;
	    while ((nextLine = csvreader.readNext()) != null) {
	        // nextLine[] is an array of values from the line
	    	
	    	// We use the first column as note
	    	String note = nextLine[0];
	    	
	    	// And ignore the other columns
	    	
	    	// Third column would be category.

	    	NotepadUtils.addNote(mContext, note);
	    }
	}

}
