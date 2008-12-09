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
