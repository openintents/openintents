/* 
 * Copyright (C) 2011 OpenIntents.org
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

package org.openintents.historify.data.loaders;

import org.openintents.historify.data.model.Contact;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

/**
 * 
 * Helper class for loading {@link Contact} objects.
 * 
 * @author berke.andras
 */
public class ContactLoader {

	public static abstract class LoadingStrategy {
		public abstract String getSelection();
		public String getSortOrder() {
			return ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
		}
	}

	public static class SimpleLoadingStrategy extends LoadingStrategy {
		@Override
		public String getSelection() {
			return null;
		}
	}

	public static class StarredContactsLoadingStrategy extends LoadingStrategy {
		@Override
		public String getSelection() {
			return ContactsContract.Contacts.STARRED + " = '1'";
		}		
	}
	
	public static class FilteredContactsLoadingStrategy extends LoadingStrategy {
		
		private String[] filteredLookupKeys;
		
		public FilteredContactsLoadingStrategy(String filteredLookupKey) {
			this.filteredLookupKeys = new String[] {filteredLookupKey};
		}
		
		public FilteredContactsLoadingStrategy(String[] filteredLookupKeys) {
			this.filteredLookupKeys = filteredLookupKeys;
		}
		
		@Override
		public String getSelection() {
			StringBuilder selection = new StringBuilder(ContactsContract.Contacts.LOOKUP_KEY+ " IN (");
        	for(String s : filteredLookupKeys) {
        		selection.append('\'');
        		selection.append(s);
        		selection.append('\'');
        		selection.append(',');
        	}
        	if(filteredLookupKeys.length!=0) 
        		selection.setCharAt(selection.length()-1, ')');
			
        	return selection.toString();
		}
	}
	
	public static class RecentlyContactedLoadingStrategy extends LoadingStrategy {
		@Override
		public String getSelection() {
			return null;
		}
		@Override
		public String getSortOrder() {
			return ContactsContract.Contacts.LAST_TIME_CONTACTED + " DESC";
		}
	}
	
	public static String[] PROJECTION = new String[] {
		ContactsContract.Contacts.LOOKUP_KEY,
		ContactsContract.Contacts.DISPLAY_NAME,
		ContactsContract.Contacts.STARRED,
		ContactsContract.Contacts.LAST_TIME_CONTACTED
	};
	
	private static final int COLUMN_LOOKUP_KEY = 0;
	private static final int COLUMN_DISPLAYNAME = 1;
	//private static final int COLUMN_STARRED = 2;
	private static final int COLUMN_LAST_TIME_CONTACTED = 3;

	
	public Cursor openCursor(Activity context, LoadingStrategy loadingStrategy) {
		
		Uri uri = ContactsContract.Contacts.CONTENT_URI;
        String[] projection = PROJECTION;
        
        StringBuilder querySelection = new StringBuilder();
        querySelection.append(ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1'");
        String selection = loadingStrategy.getSelection();
        if(selection!=null) {
        	querySelection.append(" AND ");
        	querySelection.append(selection);
        }
        
        String sortOrder = loadingStrategy.getSortOrder();
        
        return context.managedQuery(uri, projection, querySelection.toString(), null, sortOrder);
	}
	
	public Contact loadFromCursor(Cursor cursor, int position) {
		
		cursor.moveToPosition(position);
		return new Contact(
				cursor.getString(COLUMN_LOOKUP_KEY),
				cursor.getString(COLUMN_DISPLAYNAME),
				cursor.getLong(COLUMN_LAST_TIME_CONTACTED));
		
	}

	public Contact loadFromLookupKey(Activity context, String contactLookupKey) {
		
		Cursor cursor = openCursor(context, new FilteredContactsLoadingStrategy(contactLookupKey));		
		return (cursor==null || cursor.getCount()==0) ? null : loadFromCursor(cursor, 0);
	}
	
}
