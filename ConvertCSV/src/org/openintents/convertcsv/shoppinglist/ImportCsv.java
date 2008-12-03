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
