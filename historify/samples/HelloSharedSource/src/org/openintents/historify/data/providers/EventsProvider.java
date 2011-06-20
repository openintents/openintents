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

package org.openintents.historify.data.providers;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/**
 * 
 * Base class of source content providers.
 * 
 * @author berke.andras
 */
public abstract class EventsProvider extends ContentProvider {

	protected UriMatcher mUriMatcher;
	protected static final int EVENTS_UNFILTERED = 1;
	protected static final int EVENTS_FOR_A_CONTACT = 2;
	protected static final int EVENT_BY_EVENT_KEY = 3;
	
	protected static final int USER_DEFINED_MATCH = 10;

	@Override
	public boolean onCreate() {

		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(getAuthority(), 
				Events.EVENTS_PATH, EVENTS_UNFILTERED);
		mUriMatcher.addURI(getAuthority(), 
				Events.EVENTS_PATH + "/" + Events.EVENTS_FOR_CONTACTS_PATH + "/*", EVENTS_FOR_A_CONTACT);
		mUriMatcher.addURI(getAuthority(), 
				Events.EVENTS_PATH + "/" + Events.EVENTS_BY_EVENT_KEYS_PATH + "/*", EVENT_BY_EVENT_KEY);
		
		return true;
	}

	/**
	 * Derived classes must override this to define the authority of the
	 * provider.
	 * 
	 * @return The authority string of the defined provider.
	 */
	protected abstract String getAuthority();

	/**
	 * Derived classes must override this to let the client able to query a
	 * particular event identified with the given EVENT_KEY.
	 * 
	 * @param eventKey
	 *            The eventKey of the requested event.
	 * @return The Cursor containing the row associated with the requested
	 *         event. <br/>
	 *         The Cursor should contain the columns defined in the
	 *         {@link Events} class.
	 */
	protected abstract Cursor queryEvent(String eventKey);

	/**
	 * Derived classes must override this to let the client able to query the
	 * events associated with a particular contact.
	 * 
	 * @param lookupKey
	 *            The lookup key of the requested contact.
	 * @return The Cursor containing the rows associated with the requested
	 *         contact. <br/>
	 *         The Cursor should contain the columns defined in the
	 *         {@link Events} class.
	 */
	protected abstract Cursor queryEvents(String lookupKey);

	@Override
	public String getType(Uri uri) {

		switch (mUriMatcher.match(uri)) {
		case EVENTS_UNFILTERED:
		case EVENTS_FOR_A_CONTACT:
			return Events.CONTENT_TYPE;
		case EVENT_BY_EVENT_KEY:
			return Events.ITEM_CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	/**
	 * ContentProvider query method.<br/>
	 * <br/>
	 * The function executes a matching on the given Uri and calls the derived
	 * class' queryXXX method. Derived classes should not override this in
	 * normal circumstances.
	 * 
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		switch (mUriMatcher.match(uri)) {
		case EVENTS_UNFILTERED:
			return null;
		case EVENTS_FOR_A_CONTACT:
			// 3rd path segment contains the lookup key
			// (content://authority/events/contacts/{CONTACT_LOOKUP_KEY})
			try {
				return queryEvents(uri.getPathSegments().get(2));
			} catch (Exception e) {
				e.printStackTrace();
			}
		case EVENT_BY_EVENT_KEY:
			// 3rd path segment contains the event id
			try {
				return queryEvent(uri.getPathSegments().get(2));
			} catch (Exception e) {
				e.printStackTrace();
			}
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// not supported by default
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// not supported by default
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// not supported by default
		return 0;
	}

}
