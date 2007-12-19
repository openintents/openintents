/* 
 * Copyright (C) 2007 Google Inc.
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
 * 
 * This file is based on NotePadProvider.java
 * 
 * Copyright (C) 2007 openIntents.org.
 */

package org.openintents.locations;

import java.util.HashMap;

import org.openintents.provider.Location;

import android.content.ContentProvider;
import android.content.ContentProviderDatabaseHelper;
import android.content.ContentURIParser;
import android.content.ContentValues;
import android.content.QueryBuilder;
import android.content.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ContentURI;
import android.text.TextUtils;
import android.util.Log;

/**
 * Provides access to a database of locations. Each location has a latitude and longitude,
 *  a creation date and a modified data.
 *  
 *  supports urls of the format
 *  org.openintents.locations/locations
 *  org.openintents.locations/locations/23
 *  
 */
public class LocationsProvider extends ContentProvider {

	private SQLiteDatabase mDB;

	private static final String TAG = "LocationsProvider";
	private static final String DATABASE_NAME = "locations.db";
	private static final int DATABASE_VERSION = 1;

	private static HashMap<String, String> LOCATION_PROJECTION_MAP;

	private static final int LOCATIONS = 1;
	private static final int LOCATION_ID = 2;

	private static final ContentURIParser URL_MATCHER;

	private static class DatabaseHelper extends ContentProviderDatabaseHelper {
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE locations (_id INTEGER PRIMARY KEY,"
					+ "latitude VARCHAR," + "longitude VARCHAR,"
					+ "created INTEGER," + "modified INTEGER" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS locations");
			onCreate(db);
		}
	}

	@Override
	public boolean onCreate() {
		DatabaseHelper dbHelper = new DatabaseHelper();
		mDB = dbHelper.openDatabase(getContext(), DATABASE_NAME, null,
				DATABASE_VERSION);
		return mDB != null;
	}

	@Override
	public Cursor query(ContentURI url, String[] projection, String selection,
			String[] selectionArgs, String groupBy, String having, String sort) {
		QueryBuilder qb = new QueryBuilder();

		switch (URL_MATCHER.match(url)) {
		case LOCATIONS:
			qb.setTables("locations");
			qb.setProjectionMap(LOCATION_PROJECTION_MAP);
			break;

		case LOCATION_ID:
			qb.setTables("locations");
			qb.appendWhere("_id=" + url.getPathSegment(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sort)) {
			orderBy = Location.Locations.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sort;
		}

		Cursor c = qb.query(mDB, projection, selection, selectionArgs, groupBy,
				having, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), url);
		return c;
	}

	@Override
	public String getType(ContentURI url) {
		switch (URL_MATCHER.match(url)) {
		case LOCATIONS:
			return "vnd.openintents.cursor.dir/location";

		case LOCATION_ID:
			return "vnd.openintents.cursor.item/location";

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}

	@Override
	public ContentURI insert(ContentURI url, ContentValues initialValues) {
		long rowID;
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		// only allow inserts for locations (as list)
		if (URL_MATCHER.match(url) != LOCATIONS) {
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		Long now = Long.valueOf(System.currentTimeMillis());
		Resources r = Resources.getSystem();

		// Make sure that the fields are all set
		if (!values.containsKey(Location.Locations.CREATED_DATE)) {
			values.put(Location.Locations.CREATED_DATE, now);
		}

		if (!values.containsKey(Location.Locations.MODIFIED_DATE)) {
			values.put(Location.Locations.MODIFIED_DATE, now);
		}

		rowID = mDB.insert("locations", "location", values);
		if (rowID > 0) {
			ContentURI uri = Location.Locations.CONTENT_URI.addId(rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}

		throw new SQLException("Failed to insert row into " + url);
	}

	@Override
	public int delete(ContentURI url, String where, String[] whereArgs) {
		int count;
		long rowId = 0;
		switch (URL_MATCHER.match(url)) {
		case LOCATIONS:
			count = mDB.delete("locations", where, whereArgs);
			break;

		case LOCATION_ID:
			String segment = url.getPathSegment(1);
			rowId = Long.parseLong(segment);
			String whereString;
			if (!TextUtils.isEmpty(where)) {
				whereString = " AND (" + where + ')';
			} else {
				whereString = "";
			}

			count = mDB.delete("locations", "_id=" + segment + whereString,
					whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	@Override
	public int update(ContentURI url, ContentValues values, String where,
			String[] whereArgs) {
		int count;
		switch (URL_MATCHER.match(url)) {
		case LOCATIONS:
			count = mDB.update("locations", values, where, whereArgs);
			break;

		case LOCATION_ID:
			String segment = url.getPathSegment(1);
			
			String whereString;
				if (!TextUtils.isEmpty(where)) {
				whereString = " AND (" + where + ')';
			} else {
				whereString = "";
			}
			
			count = mDB.update("locations", values,
					"_id="
							+ segment
							+ whereString, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	static {
		URL_MATCHER = new ContentURIParser(ContentURIParser.NO_MATCH);
		URL_MATCHER.addURI("org.openintents.locations", "locations", LOCATIONS);
		URL_MATCHER.addURI("org.openintents.locations", "locations/#",
				LOCATION_ID);

		LOCATION_PROJECTION_MAP = new HashMap<String, String>();
		LOCATION_PROJECTION_MAP.put(Location.Locations._ID, "_id");
		LOCATION_PROJECTION_MAP.put(Location.Locations.LATITUDE, "latitude");
		LOCATION_PROJECTION_MAP.put(Location.Locations.LONGITUDE, "longitude");
		LOCATION_PROJECTION_MAP.put(Location.Locations.CREATED_DATE, "created");
		LOCATION_PROJECTION_MAP.put(Location.Locations.MODIFIED_DATE,
				"modified");
	}
}
