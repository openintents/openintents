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
import org.openintents.historify.data.model.Event;
import org.openintents.historify.data.providers.Events;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;

/**
 * 
 * Helper class for loading {@link Event} objects.
 * 
 * @author berke.andras
 */
public class EventLoader {

	public Cursor openCursor(Activity context, Uri providerAuthority, Contact contact) {
		
		Builder eventsUri = providerAuthority.buildUpon().appendPath(Events.EVENTS_PATH);
		if(contact!=null) {
			eventsUri.appendPath(Events.EVENTS_FOR_CONTACTS_PATH);
			eventsUri.appendPath(contact.getLookupKey());
		}
		
        return context.managedQuery(eventsUri.build(), null, null, null, null);
	}
	
	public Event loadFromCursor(Cursor cursor, int position) {
		
		cursor.moveToPosition(position);
		
		int column_id = cursor.getColumnIndex(Events._ID);
		int column_event_key = cursor.getColumnIndex(Events.EVENT_KEY);
		int column_contact_key = cursor.getColumnIndex(Events.CONTACT_KEY);
		int column_published_time = cursor.getColumnIndex(Events.PUBLISHED_TIME);
		int column_message = cursor.getColumnIndex(Events.MESSAGE);
		int column_originator = cursor.getColumnIndex(Events.ORIGINATOR);
		
		return new Event(
				cursor.getLong(column_id),
				cursor.getString(column_event_key),
				cursor.getString(column_contact_key),
				cursor.getLong(column_published_time),
				cursor.getString(column_message),
				Events.Originator.parseString(cursor.getString(column_originator)));
	}
	
}
