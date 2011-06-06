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

	private UriMatcher sUriMatcher;
	private static final int EVENTS = 1;
	private static final int EVENT_ID = 2;

	@Override
	public boolean onCreate() {

		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(getAuthority(), Events.EVENTS_PATH + "/#", EVENT_ID);
		sUriMatcher.addURI(getAuthority(), Events.EVENTS_PATH + "/*", EVENTS);

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
	 * particular event identified with the given id.
	 * 
	 * @param eventId
	 *            The id of the requested event.
	 * @return The Cursor containing the row associated with the requested
	 *         event. <br/>
	 *         The Cursor should contain the columns defined in the
	 *         {@link Events} class.
	 */
	protected abstract Cursor queryEvent(long eventId);

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

		switch (sUriMatcher.match(uri)) {
		case EVENTS:
			return Events.CONTENT_TYPE;
		case EVENT_ID:
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

		switch (sUriMatcher.match(uri)) {
		case EVENTS:
			// 2nd path segment contains the lookup key
			// (content:://authority/events/{CONTACT_LOOKUP_KEY})
			try {
				return queryEvents(uri.getPathSegments().get(1));
			} catch (Exception e) {
				e.printStackTrace();
			}
		case EVENT_ID:
			// 2nd path segment contains the event id
			try {
				return queryEvent(Long.valueOf(uri.getPathSegments().get(1)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

	}

	@Override
	public final Uri insert(Uri uri, ContentValues values) {
		// not supported
		return null;
	}

	@Override
	public final int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// not supported
		return 0;
	}

	@Override
	public final int delete(Uri uri, String selection, String[] selectionArgs) {
		// not supported
		return 0;
	}

}
