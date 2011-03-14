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
import org.openintents.convertcsv.common.ConvertCsvBaseActivity;
import org.openintents.convertcsv.common.WrongFormatException;
import org.openintents.convertcsv.opencsv.CSVReader;
import org.openintents.shopping.library.provider.Shopping;
import org.openintents.shopping.library.provider.Shopping.Status;

import android.content.Context;
import android.text.TextUtils;

public class ImportCsv {

	Context mContext;
	Boolean mDuplicate = true;
    Boolean mUpdate = false;
    
	public ImportCsv(Context context, int importPolicy) {
		mContext = context;
	    switch (importPolicy) {
	    case ConvertCsvBaseActivity.IMPORT_POLICY_KEEP:
	    	mDuplicate = false;
	    	mUpdate = false;
	    	break;
	    case ConvertCsvBaseActivity.IMPORT_POLICY_RESTORE:
	    	// not implemented, treat as overwrite for now.
	    case ConvertCsvBaseActivity.IMPORT_POLICY_OVERWRITE:
	    	mDuplicate = false;
	    	mUpdate = true;
	    	break;
	    case ConvertCsvBaseActivity.IMPORT_POLICY_DUPLICATE:
	    	mDuplicate = true;
	    	mUpdate = false;
	    	break;
	    }
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
			long itemId = Shopping.getItem(mContext, itemname, tags, null, 
					null, null, mDuplicate, mUpdate);
			
			if (status == 1) {
				status = Status.BOUGHT;
			} else if (status == 0) {
				status = Status.WANT_TO_BUY;
			} else {
				status = Status.REMOVED_FROM_LIST;
			}
			
			
			Shopping.addItemToList(mContext, itemId, listId, status, 1, 1);
	    }
	    
	}

	public void importHandyShopperCsv(Reader reader, Boolean importStores) throws IOException, WrongFormatException {
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
			String note = nextLine[18]; // Note
			String units = nextLine[5];
			
			if (nextLine[3].length() > 0) {
				if (tags.length() == 0) {
					tags = nextLine[3];
				} else {
					tags += "," + nextLine[3];
				}
			}
			
			double quantity;
			try {
			    quantity = Double.parseDouble(nextLine[4]); // Quantity
			} catch (java.lang.NumberFormatException nfe){
				quantity = 1.0;		
			}
			long priority;
			try {
			    priority = Integer.parseInt(nextLine[1]); // Priority
			} catch (java.lang.NumberFormatException nfe){
				priority = 1;		
			}
			
			// Add item to list
			long listId = Shopping.getDefaultList(mContext);
			long itemId = Shopping.getItem(mContext, itemname, tags, price, units, note,
					mDuplicate, mUpdate);
			Shopping.addItemToList(mContext, itemId, listId, status, priority, quantity);
			
			// Two columns contain per-store information. Column 10 lists 
			// all stores which carry this item, delimited by semicolons. Column 11 
			// lists aisles and prices for some subset of those stores.
			//
			// First deal with the stores themselves from column 10.
			String [] stores;
			
			if (nextLine[10].length() > 0)
			{
				stores = nextLine[10].split(";");
				for (int i_store = 0; i_store < stores.length; i_store ++)
				{
					if (importStores){	// real store import
						long storeId = Shopping.getStore(mContext, stores[i_store], listId);
						long item_store = Shopping.addItemToStore(mContext, itemId, storeId, 0, "");
					} else if (!TextUtils.isEmpty(stores[i_store])){
						// store names added as tags. 
						Shopping.addTagToItem(mContext, itemId, stores[i_store]);
					}
				}
			}
			// Now go back and deal with per-store aisles and prices from column 11.
			// example value for column 11:    Big Y=/0.50;BJ's=11/0.42
			if (nextLine[11].length() > 0 && importStores) {
				stores = nextLine[11].split(";");

				for (int i_store = 0; i_store < stores.length; i_store ++)
				{
					String [] key_vals = stores[i_store].split("=");
					String store_name = key_vals[0];
					String [] aisle_price = key_vals[1].split("/");
					if (aisle_price.length == 0)
						continue;
					int aisle;
					try {
						aisle = Integer.parseInt(aisle_price[0]);
					} catch (java.lang.NumberFormatException nfe) {
						aisle = -1;
					}
					String store_price = "";
					if (aisle_price.length > 1) {
						try {
							Float fprice = Float.parseFloat(aisle_price[1]);
							fprice *= 100;
							store_price = fprice.toString();
						} catch (java.lang.NumberFormatException nfe) {
						}
					}
			
					long storeId = Shopping.getStore(mContext, store_name, listId);
					long item_store = Shopping.addItemToStore(mContext, itemId, storeId, aisle, store_price);
				
				}
			}
	    }
		
	}
}
