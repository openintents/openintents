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

package org.openintents.timescape.sharedsource;

import org.openintents.historify.data.providers.Events;
import org.openintents.historify.data.providers.EventsProvider;
import org.openintents.timescape.api.provider.EventStreamHelper;
import org.openintents.timescape.api.provider.EventStreamHelper.EventsTable;
import org.openintents.timescape.api.provider.EventStreamHelper.FriendsTable;
import org.openintents.timescape.api.provider.EventStreamHelper.OpenHelper;
import org.openintents.timescape.api.provider.EventStreamHelper.PluginsTable;

import android.content.ContentUris;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.RawContacts;

public class TimescapeProvider extends EventsProvider{
	
	private EventStreamHelper.OpenHelper mOpenHelper;
	
	@Override
	public boolean onCreate() {
		if(super.onCreate()) {
			mOpenHelper = new OpenHelper(getContext());
		}
		
		return true;
	}
	
	@Override
	protected String getAuthority() {
		return SourceConstants.AUTHORITY;
	}

	@Override
	protected Cursor queryEventsForContact(String lookupKey) {
		
		//to get the events associated with the contact we
		//1. get the raw contacts
		//2. get the friendkeys associated with the given raw contacts
		//3. query the events table for the events associated with the friendkeys
		
		//4. return the events mapped as h! event fields
		
		//1. get raw contact ids for lookupkey
		//--------------------------------------
		Uri contactUri = Contacts.lookupContact(getContext().getContentResolver(), Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey));
		long contactId = ContentUris.parseId(contactUri);
		
		Cursor c = getContext().getContentResolver().query(
				RawContacts.CONTENT_URI, 
				new String[] {
					RawContacts._ID
				}, RawContacts.CONTACT_ID + " = "+contactId, null, null);

		//get friendkeys selection
		if(!c.moveToFirst()) {
			c.close();
			return null;
		}
		
		StringBuilder friendKeysSelection = new StringBuilder();
		friendKeysSelection.append(FriendsTable.CONTACTS_REFERENCE);
		friendKeysSelection.append(" IN (");
		do {
			Uri contactRawUri = Uri.withAppendedPath(RawContacts.CONTENT_URI, String.valueOf(c.getLong(0)));
			friendKeysSelection.append("'");
			friendKeysSelection.append(contactRawUri.toString());
			friendKeysSelection.append("'");
			friendKeysSelection.append(",");
		} while(c.moveToNext());
		
		friendKeysSelection.setCharAt(friendKeysSelection.length()-1, ')');
		c.close();
		
		//2. get the friendkeys
		//--------------------------------------
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		c = db.query(FriendsTable._TABLE, 
				new String[] {
					FriendsTable.FRIEND_KEY	
				}, friendKeysSelection.toString(), null, null, null, null);
		
		if(!c.moveToFirst()) {
			c.close();
			return null;
		}
		
		StringBuilder eventSelection = new StringBuilder();
		eventSelection.append("e."+EventsTable.FRIEND_KEY);
		eventSelection.append(" IN (");
		do {
			eventSelection.append("'");
			eventSelection.append(c.getString(0));
			eventSelection.append("'");
			eventSelection.append(",");
		} while(c.moveToNext());
		
		eventSelection.setCharAt(eventSelection.length()-1, ')');
		c.close(); 
		

		//3. query the events table for the events associated with the friendkeys		
		//4. return the events mapped as h! event fields

		Cursor retval = db.query(EventsTable._TABLE + " e JOIN "+PluginsTable._TABLE +" p ON e."+EventsTable.PLUGIN_ID+" = p."+PluginsTable._ID,
				new String[] {
					"e."+EventsTable._ID + " AS "+Events._ID,
					"'"+lookupKey+"'" + " AS "+Events.CONTACT_KEY,
					"e."+EventsTable.EVENT_KEY + " AS "+Events.EVENT_KEY,
					"e."+EventsTable.MESSAGE + " AS "+Events.MESSAGE,
					"'"+Events.Originator.contact+"'" + " AS "+Events.ORIGINATOR, //TODO: handle OUTGOING event
					"e."+EventsTable.PUBLISHED_TIME + " AS "+Events.PUBLISHED_TIME,
					"p."+PluginsTable.ICON_URI + " AS "+Events.ICON_URI
				},eventSelection.toString(),null, null, null, "e."+EventsTable.PUBLISHED_TIME + " DESC");
		
		if(retval!=null)
			retval.setNotificationUri(getContext().getContentResolver(), EventStreamHelper.getUri(EventStreamHelper.EVENTS_PATH));
		
		return retval;

	}

}
