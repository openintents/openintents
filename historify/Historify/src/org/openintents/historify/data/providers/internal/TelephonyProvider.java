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

import org.openintents.historify.R;
import org.openintents.historify.data.providers.Events;
import org.openintents.historify.data.providers.EventsProvider;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.CallLog;
import android.provider.ContactsContract.CommonDataKinds.Phone;

/**
 * 
 * Content Provider for accessing the call log.
 * 
 * @author berke.andras
 */
public class TelephonyProvider extends EventsProvider {

	@Override
	public boolean onCreate() {
		boolean retval = super.onCreate();
		
		if(retval) 
			setEventsUri(CallLog.Calls.CONTENT_URI);
		
		return retval;
	}

	@Override
	protected String getAuthority() {
		return Telephony.TELEPHONY_AUTHORITY;
	}

	private Cursor rawQuery(String where, String lookupKey) {
		
		// build where clause for call log query
		String selection = where + " AND " + CallLog.Calls.TYPE + " != "
				+ CallLog.Calls.MISSED_TYPE + " AND " + CallLog.Calls.DURATION
				+ " != 0";

		// format call duration string
		String eventMessage = getContext().getString(
				R.string.telephony_event_message);
		eventMessage = String.format("'" + eventMessage + "'", "' || "
				+ "strftime('%M:%S', time(" + CallLog.Calls.DURATION
				+ ", 'unixepoch'))" + " || '");

		// execute query
		// column names are mapped as defined in .data.providers.Events
		return getContext().getContentResolver().query(
				CallLog.Calls.CONTENT_URI,
				new String[] {
						CallLog.Calls._ID + " AS " + Events._ID,
						"NULL AS " + Events.EVENT_KEY,
						eventMessage + " AS " + Events.MESSAGE,
						CallLog.Calls.DATE + " AS " + Events.PUBLISHED_TIME,
						"'" + lookupKey + "' AS " + Events.CONTACT_KEY,
						"REPLACE(" + "REPLACE(" + CallLog.Calls.TYPE + ","
								+ CallLog.Calls.INCOMING_TYPE + ",'"
								+ Events.Originator.contact + "'),"
								+ CallLog.Calls.OUTGOING_TYPE + ",'"
								+ Events.Originator.user + "') AS "
								+ Events.ORIGINATOR }, selection, null,
				CallLog.Calls.DATE + " DESC");

		
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
		
		String where = CallLog.Calls.NUMBER + " IN (" + phoneNumbers.toString() + ")";
		
		return rawQuery(where, lookupKey);

	}

	@Override
	protected Cursor queryEvent(long eventId) {
		
		String where = CallLog.Calls._ID + " = "+eventId;
		return rawQuery(where, null);
	}

	@Override
	protected Cursor queryEvents() {
		return null;
	}

	@Override
	protected Cursor queryEventsByKey(String eventKey) {
		return null;
	}

}
