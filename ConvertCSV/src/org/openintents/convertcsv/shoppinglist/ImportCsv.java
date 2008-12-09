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

package org.openintents.convertcsv.shoppinglist;

import java.io.IOException;
import java.io.Reader;

import org.openintents.convertcsv.R;
import org.openintents.convertcsv.opencsv.CSVReader;
import org.openintents.provider.Shopping;

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
	    	if (nextLine[1].equals(mContext.getString(R.string.header_percent_complete))) {
	    		// First line is just subject, so let us skip it
	    		continue;
	    	}
	    	String itemname = nextLine[0];
			long status = (nextLine[1].equals("1")) ? 1 : 0;
			String listname = nextLine[2];

			// Add item to list
			long listId = ShoppingUtils.getOrCreateListId(mContext, listname);
			long itemId = ShoppingUtils.getItemId(mContext, itemname);
			
			if (status == 1) {
				status = Shopping.Status.BOUGHT;
			} else {
				status = Shopping.Status.WANT_TO_BUY;
			}
			
			ShoppingUtils.addItemToList(mContext, itemId, listId, status);
	    }
	    
	}

}
