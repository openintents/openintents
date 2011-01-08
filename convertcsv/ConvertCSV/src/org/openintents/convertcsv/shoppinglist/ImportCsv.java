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

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.openintents.convertcsv.R;
import org.openintents.convertcsv.common.WrongFormatException;
import org.openintents.convertcsv.opencsv.CSVReader;
import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.Status;

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
	public void importCsv(Reader reader) throws IOException,
		WrongFormatException {
		CSVReader csvreader = new CSVReader(reader);
	    String [] nextLine;
	    while ((nextLine = csvreader.readNext()) != null) {
	    	if (nextLine.length != 4) {
	    		throw new WrongFormatException();
	    	}
	    	// nextLine[] is an array of values from the line
	    	String statusstring = nextLine[1];
	    	if (statusstring.equals(mContext.getString(R.string.header_percent_complete))) {
	    		// First line is just subject, so let us skip it
	    		continue;
	    	}
	    	String itemname = nextLine[0];
			long status;
			try {
				status = Long.parseLong(statusstring);
			} catch (NumberFormatException e) {
				status = 0;
			}
			String listname = nextLine[2];
			String tags = nextLine[3];
			
			// Add item to list
			long listId = Shopping.getList(mContext, listname);
			long itemId = Shopping.getItem(mContext, itemname, tags, null);
			
			if (status == 1) {
				status = Status.BOUGHT;
			} else if (status == 0) {
				status = Status.WANT_TO_BUY;
			} else {
				status = Status.REMOVED_FROM_LIST;
			}
			
			
			Shopping.addItemToList(mContext, itemId, listId, status, 1);
	    }
	    
	}

	public void importHandyShopperCsv(Reader reader) throws IOException, WrongFormatException {
		CSVReader csvreader = new CSVReader(reader);
	    String [] nextLine;
	    while ((nextLine = csvreader.readNext()) != null) {
	    	if (nextLine.length != 23) {
	    		throw new WrongFormatException();
	    	}
	    	// nextLine[] is an array of values from the line
	    	String statusstring = nextLine[0];
	    	if (statusstring.equals(mContext.getString(R.string.header_need))) {
	    		// First line is just subject, so let us skip it
	    		continue;
	    	}
	    	
			long status;
			if ("x".equalsIgnoreCase(statusstring)){
				status = Status.WANT_TO_BUY;
			} else if ("".equalsIgnoreCase(statusstring)){
				status = Status.BOUGHT;
			} else if ("have".equalsIgnoreCase(statusstring)){
				status = Status.REMOVED_FROM_LIST;
			} else {
				status = Status.REMOVED_FROM_LIST;
			}

			
	    	String itemname = nextLine[2]; // Description					
			String tags = nextLine[9]; // Category
			String price = nextLine[6]; // Price
			long quantity = Integer.parseInt(nextLine[4]); // Quantity
			
			// Add item to list
			long listId = Shopping.getDefaultList();
			long itemId = Shopping.getItem(mContext, itemname, tags, price);
			
			
			Shopping.addItemToList(mContext, itemId, listId, status, quantity);
	    }
		
	}
	
	
}
