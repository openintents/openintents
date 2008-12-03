package org.openintents.convertcsv.shoppinglist;

import java.io.IOException;
import java.io.Writer;

import org.openintents.convertcsv.R;
import org.openintents.convertcsv.opencsv.CSVWriter;
import org.openintents.provider.Shopping;

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
		
		csvwriter.write(mContext.getString(R.string.header_subject));
		csvwriter.write(mContext.getString(R.string.header_percent_complete));
		csvwriter.write(mContext.getString(R.string.header_categories));
		csvwriter.writeNewline();

		Cursor c = mContext.getContentResolver().query(Shopping.Lists.CONTENT_URI, ShoppingUtils.PROJECTION_LISTS, null, null,
		        Shopping.Lists.DEFAULT_SORT_ORDER);
		
		if (c != null) {
			
			while (c.moveToNext()) {
				
				String listname = c.getString(c.getColumnIndexOrThrow(Shopping.Lists.NAME));
		    	long id = c.getLong(c.getColumnIndexOrThrow(Shopping.Lists._ID));
		    	
		    	//Log.i(ConvertCsvActivity.TAG, "List: " + listname);
		    	
		    	Cursor ci = mContext.getContentResolver().query(Shopping.ContainsFull.CONTENT_URI, 
		    			ShoppingUtils.PROJECTION_CONTAINS_FULL, Shopping.ContainsFull.LIST_ID + " = ?", 
		    			new String[] {""+id}, Shopping.ContainsFull.DEFAULT_SORT_ORDER);
		    	
	
		        if (ci != null) {
		        	int itemcount = ci.getCount();
	
		        	while (ci.moveToNext()) {
		        		String itemname = ci.getString(ci.getColumnIndexOrThrow(Shopping.ContainsFull.ITEM_NAME));
		        		int status = ci.getInt(ci.getColumnIndexOrThrow(Shopping.ContainsFull.STATUS));
			        	int percentage = (status == Shopping.Status.BOUGHT) ? 1 : 0;

				    	csvwriter.write(itemname);
				    	csvwriter.write(percentage);
				    	csvwriter.write(listname);
				    	csvwriter.writeNewline();
		        	}
		        }
			}
		}
		
		csvwriter.close();
	}

}
