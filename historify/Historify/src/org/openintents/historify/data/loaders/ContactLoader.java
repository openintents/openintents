package org.openintents.historify.data.loaders;

import org.openintents.historify.data.model.Contact;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

public class ContactLoader {

	public static String[] PROJECTION = new String[] {
		ContactsContract.Contacts.LOOKUP_KEY,
		ContactsContract.Contacts.DISPLAY_NAME,
		ContactsContract.Contacts.STARRED
	};
	
	private static final int COLUMN_LOOKUP_KEY = 0;
	private static final int COLUMN_DISPLAYNAME = 1;
	//private static final int COLUMN_STARRED = 2;

	public Cursor openCursor(Activity context, boolean starredOnly) {
		return openCursor(context, starredOnly,null);
	}
	
	public Cursor openCursor(Activity context, boolean starredOnly, String[] selection) {
		
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = PROJECTION;
        
        StringBuilder querySelection = new StringBuilder();
        querySelection.append(ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'");
        if(starredOnly)
        	querySelection.append(" AND "+ContactsContract.Contacts.STARRED + " = '1'");
        
        if(selection!=null) {
        	querySelection.append(" AND "+ContactsContract.Contacts.LOOKUP_KEY+ " IN (");
        	for(String s : selection) {
        		querySelection.append('\'');
        		querySelection.append(s);
        		querySelection.append('\'');
        		querySelection.append(',');
        	}
        	if(selection.length!=0) 
        		querySelection.setCharAt(querySelection.length()-1, ')');
        }
        
        String[] selectionArgs = null;
        String sortOrder = ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
        
        return context.managedQuery(uri, projection, querySelection.toString(), selectionArgs, sortOrder);

	}
	
	public Contact loadFromCursor(Cursor cursor, int position) {
		
		cursor.moveToPosition(position);
		return new Contact(
				cursor.getString(COLUMN_LOOKUP_KEY),
				cursor.getString(COLUMN_DISPLAYNAME));
		
	}

	public Contact loadFromLookupKey(Activity context, String contactLookupKey) {
		
		String[] selection  = new String[] {
				contactLookupKey	
		};
		
		Cursor cursor = openCursor(context, false, selection);
		
		return (cursor==null || cursor.getCount()==0) ? null : loadFromCursor(cursor, 0);
	}
	
}
