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

package org.openintents.historify.data.providers.internal;

import org.openintents.historify.data.providers.Events;
import org.openintents.historify.data.providers.EventsProvider;
import org.openintents.historify.data.providers.internal.Messaging.Messages;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.ContactsContract.CommonDataKinds.Phone;

/**
 * 
 * Content Provider for accessing the sms messages.
 * 
 * @author berke.andras
 */
public class MessagingProvider extends EventsProvider {

	@Override
	public boolean onCreate() {
		boolean retval =  super.onCreate();
		
		if(retval)
			setEventsUri(Messages.CONTENT_URI);
		
		return retval;
	}

	@Override
	protected String getAuthority() {
		return Messaging.MESSAGING_AUTHORITY;
	}

	private Cursor rawQuery(String where, String lookupKey) {
		
		// execute query
		// column names are mapped as defined in .data.providers.Events
		return getContext().getContentResolver().query(
				Messaging.Messages.CONTENT_URI,
				new String[] {
						Messaging.Messages._ID + " AS " + Events._ID,
						"NULL AS " + Events.EVENT_KEY,
						Messaging.Messages.BODY + " AS " + Events.MESSAGE,
						Messaging.Messages.DATE + " AS "
								+ Events.PUBLISHED_TIME,
						"'" + lookupKey + "' AS " + Events.CONTACT_KEY,
						"REPLACE(" + "REPLACE(" + Messaging.Messages.TYPE + ","
								+ Messaging.Messages.INCOMING_TYPE + ",'"
								+ Events.Originator.contact + "'),"
								+ Messaging.Messages.OUTGOING_TYPE + ",'"
								+ Events.Originator.user + "') AS "
								+ Events.ORIGINATOR }, where, null,
				Messaging.Messages.DATE + " DESC");

	
	}

	@Override
	protected Cursor queryEventsForContact(String lookupKey) {
		
		String phoneSelection = Phone.LOOKUP_KEY + " = '" + lookupKey + "'";
		
		ContentResolver resolver = getContext().getContentResolver();

		// querying phone numbers of the contact
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,
				new String[] { Phone.NUMBER }, phoneSelection, null, null);

		StringBuilder phoneNumbers = new StringBuilder();
		while (phoneCursor.moveToNext()) {
			phoneNumbers.append("'");
			phoneNumbers.append(phoneCursor.getString(0));
			phoneNumbers.append("'");
			phoneNumbers.append(",");
		}
		if (phoneNumbers.length() > 0)
			phoneNumbers.setLength(phoneNumbers.length() - 1);

		phoneCursor.close();

		// build where clause for message log query
		String where = Messaging.Messages.ADDRESS + " IN ("
				+ phoneNumbers.toString() + ")";
		
		return rawQuery(where, lookupKey);

	}

	@Override
	protected Cursor queryEvent(long eventId) {
		
		String where = Messaging.Messages._ID + " = "+eventId;
		return rawQuery(where, null);
	}

}
