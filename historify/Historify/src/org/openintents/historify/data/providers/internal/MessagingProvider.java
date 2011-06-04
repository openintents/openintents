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
import org.openintents.historify.uri.ContentUris;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;

/**
 * 
 * Content Provider for accessing the sms messages.
 * 
 * @author berke.andras
 */
public class MessagingProvider extends ContentProvider {

	public static final String NAME = "TelephonyProvider";

	private static final UriMatcher sUriMatcher;
	private static final int EVENTS = 1;
	private static final int EVENT_ID = 2;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		sUriMatcher.addURI(ContentUris.SOURCES_AUTHORITY, Events.EVENTS_PATH
				+ "/#", EVENT_ID);

		sUriMatcher.addURI(Messaging.MESSAGING_AUTHORITY, Events.EVENTS_PATH
				+ "/*", EVENTS);
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public String getType(Uri uri) {

		switch (sUriMatcher.match(uri)) {
		case EVENTS:
			return Events.CONTENT_TYPE;
		case EVENT_ID:
			return Events.ITEM_CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		String lookupKey = null;
		String phoneSelection = null;

		switch (sUriMatcher.match(uri)) {
		case EVENTS:
			// 2nd path segment contains the lookup key
			// (content:://authority/events/{CONTACT_LOOKUP_KEY})
			lookupKey = uri.getPathSegments().get(1);
			phoneSelection = Phone.LOOKUP_KEY + " = '" + lookupKey + "'";
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

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
	public Uri insert(Uri uri, ContentValues values) {
		// not supported
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// not supported
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// not supported
		return 0;
	}

}
