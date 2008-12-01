package org.openintents.convertcsv.notepad;

import java.io.DataInputStream;
import java.io.IOException;

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
	public void importCsv(DataInputStream dis) throws IOException {
		String line = dis.readLine();
	
		if (line != null && line.startsWith("Subject")) {
			// ignore first line
			line = dis.readLine();
		}
		while (line != null) {
			String[] tokens = line.split(",");
			if (tokens.length == 3) {
				/*
				String itemname = tokens[0];
				long status = (tokens[1].equals("1")) ? 1 : 0;
				String listname = tokens[2];
	
				// Add item to list
				long listId = NotepadUtils.getOrCreateListId(mContext, listname);
				long itemId = NotepadUtils.getItemId(mContext, itemname);
				
				if (status == 1) {
					status = Shopping.Status.BOUGHT;
				} else {
					status = Shopping.Status.WANT_TO_BUY;
				}
				
				NotepadUtils.addItemToList(mContext, itemId, listId, status);
				*/
			}
			
			
			line = dis.readLine();
		}
	}

}
