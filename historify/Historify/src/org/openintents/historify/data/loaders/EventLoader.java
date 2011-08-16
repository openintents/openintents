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

import android.content.Context;
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

	/**
	 * Opens a cursor based on the given authority and contact.
	 * 
	 * @param context
	 *            Context.
	 * @param providerAuthority
	 *            The authority of the provider which the events should be
	 *            loaded from.
	 * @param contact
	 *            The contact whom the events should be filtered for. Might be
	 *            <b>null</b>.
	 * @return A cursor containing the events provided.
	 */
	public Cursor openCursor(Context context, Uri providerAuthority,
			Contact contact) {

		Builder eventsUri = providerAuthority.buildUpon().appendPath(
				Events.EVENTS_PATH);
		if (contact != null) {
			eventsUri.appendPath(Events.EVENTS_FOR_CONTACTS_PATH);
			eventsUri.appendPath(contact.getLookupKey());
		}

		return context.getContentResolver().query(eventsUri.build(), null,
				null, null, null);
	}

	/**
	 * Opens a cursor based on the given authority and event id.
	 * 
	 * @param context
	 *            Context
	 * @param providerAuthority
	 *            The authority of the provider which the event should be loaded
	 *            from.
	 * @param eventId
	 *            The id of event that should be loaded.
	 * @return A cursor containing the event identified by the given id, if
	 *         available in the provider.
	 */
	public Cursor openCursor(Context context, Uri providerAuthority,
			long eventId) {

		Uri eventUri = providerAuthority.buildUpon().appendPath(
				Events.EVENTS_PATH).appendPath(String.valueOf(eventId)).build();

		return context.getContentResolver().query(eventUri, null, null, null,
				null);
	}

	/**
	 * Loads an Event instance from the cursor.
	 * 
	 * @param cursor
	 * @param position
	 *            The position to load from.
	 * @return The new Event instance.
	 */
	public Event loadFromCursor(Cursor cursor, int position) {

		cursor.moveToPosition(position);

		int column_id = cursor.getColumnIndex(Events._ID);
		int column_event_key = cursor.getColumnIndex(Events.EVENT_KEY);
		int column_contact_key = cursor.getColumnIndex(Events.CONTACT_KEY);
		int column_published_time = cursor
				.getColumnIndex(Events.PUBLISHED_TIME);
		int column_message = cursor.getColumnIndex(Events.MESSAGE);
		int column_originator = cursor.getColumnIndex(Events.ORIGINATOR);

		int column_icon_uri = cursor.getColumnIndex(Events.ICON_URI);
		Uri iconUri = null;
		if (column_icon_uri != -1) {
			String uriString = cursor.getString(column_icon_uri);
			if (uriString != null) {
				iconUri = Uri.parse(uriString);
			}
		}

		return new Event(cursor.getLong(column_id), cursor
				.getString(column_event_key), cursor
				.getString(column_contact_key), cursor
				.getLong(column_published_time), cursor
				.getString(column_message), Events.Originator
				.parseString(cursor.getString(column_originator)), iconUri);
	}

}
