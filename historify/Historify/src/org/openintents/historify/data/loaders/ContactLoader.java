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
import android.provider.ContactsContract.Contacts;

/**
 * 
 * Helper class for loading {@link Contact} objects.
 * 
 * @author berke.andras
 */
public class ContactLoader {

	public static abstract class LoadingStrategy {
		
		private String mFilterText = "";
		
		public String getSelection() {
			String strategySelection = getStrategySelection();
			
			if(mFilterText.equals("")) {
				return strategySelection;
			} else {
				StringBuilder selection = new StringBuilder(strategySelection == null ? "" : strategySelection + " AND ");
				selection.append("("+Contacts.DISPLAY_NAME + " LIKE ?");
				selection.append(" OR "+Contacts.DISPLAY_NAME + " LIKE ?)");
				return selection.toString();
			}
		}
		
		public String[] getSelectionArgs() {
			if(mFilterText.equals("")) return null;
			else return new String[] {mFilterText+"%", "% "+mFilterText+"%"};
		}
		
		public abstract String getStrategySelection();
		public String getStrategySortOrder() {
			return ContactsContract.Contacts.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
		}
		
		public String getFilterText() {
			return mFilterText;
		}
		
		public void setFilterText(String filterText) {
			this.mFilterText = filterText;
		}
	}

	public static class SimpleLoadingStrategy extends LoadingStrategy {
		@Override
		public String getStrategySelection() {
			return null;
		}
	}

	public static class StarredContactsLoadingStrategy extends LoadingStrategy {
		@Override
		public String getStrategySelection() {
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
		public String getStrategySelection() {
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
		public String getStrategySelection() {
			return ContactsContract.Contacts.LAST_TIME_CONTACTED + ">0";
		}
		@Override
		public String getStrategySortOrder() {
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
        
        String sortOrder = loadingStrategy.getStrategySortOrder();
        
        return context.managedQuery(uri, projection, querySelection.toString(), loadingStrategy.getSelectionArgs(), sortOrder);
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
