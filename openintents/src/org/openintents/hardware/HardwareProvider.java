/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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

package org.openintents.hardware;

import java.util.HashMap;

import org.openintents.R;
import org.openintents.provider.Hardware.Preferences;

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
 * Provides access to a preferences related to hardware or 
 * hardware simulation. 
 * 
 */
public class HardwareProvider extends ContentProvider {

	private SQLiteDatabase mDB;

	/**
	 * TAG for logging.
	 */
	private static final String TAG = "HardwareProvider";
	private static final String DATABASE_NAME = "hardware.db";
	private static final int DATABASE_VERSION = 1; // Release 0.1.2

	private static HashMap<String, String> PREFERENCES_PROJECTION_MAP;
	
	// Basic tables
	private static final int PREFERENCES = 1;
	private static final int PREFERENCE_ID = 2;
	
	private static final ContentURIParser URL_MATCHER;

	/**
	 * HardwareProvider maintains the following tables:
	 *  * preferences: preferences related to hardware abstraction
	 *                 or hardware simulation.
	 */
	private static class DatabaseHelper extends ContentProviderDatabaseHelper {
		/**
		 * Creates tables "preferences".
		 */
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE preferences ("
					+ "_id INTEGER PRIMARY KEY," // Database Version 1
					+ "name VARCHAR," // V1
					+ "value VARCHAR" // V1
					+ ");");
		}

		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS preferences");
			onCreate(db);
		}
	}

	public boolean onCreate() {
		DatabaseHelper dbHelper = new DatabaseHelper();
		mDB = dbHelper.openDatabase(getContext(), DATABASE_NAME, null,
				DATABASE_VERSION);
		return mDB != null;
	}

	public Cursor query(ContentURI url, String[] projection, String selection,
			String[] selectionArgs, String groupBy, String having, String sort) {
		QueryBuilder qb = new QueryBuilder();

		Log.i(TAG, "Query for URL: " + url);
		
		String defaultOrderBy = null;
		switch (URL_MATCHER.match(url)) {
		case PREFERENCES:
			qb.setTables("preferences");
			qb.setProjectionMap(PREFERENCES_PROJECTION_MAP);
			defaultOrderBy = Preferences.DEFAULT_SORT_ORDER;
			break;

		case PREFERENCE_ID:
			qb.setTables("preferences");
			qb.appendWhere("_id=" + url.getPathSegment(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		// If no sort order is specified use the default

		String orderBy;
		if (TextUtils.isEmpty(sort)) {
			orderBy = defaultOrderBy;
		} else {
			orderBy = sort;
		}

		Cursor c = qb.query(mDB, projection, selection, selectionArgs, groupBy,
				having, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), url);
		return c;
	}

	public ContentURI insert(ContentURI url, ContentValues initialValues) {
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		
		// insert is supported for items or lists
		switch (URL_MATCHER.match(url)) {
		case PREFERENCES:
			return insertPreferences(url, values);
		
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}
	
	public ContentURI insertPreferences(ContentURI url, ContentValues values) {
		long rowID;
		
		Resources r = Resources.getSystem();
		

		// Make sure that the fields are all set
		if (!values.containsKey(Preferences.NAME)) {
			values.put(Preferences.NAME, r.getString(R.string.new_item));
		}
		
		if (!values.containsKey(Preferences.VALUE)) {
			values.put(Preferences.VALUE, "");
		}
		
		
		// TODO: Here we should check, whether item exists already. 
		// (see TagsProvider)
		// insert the item. 
		rowID = mDB.insert("preferences", "preferences", values);
		if (rowID > 0) {
			ContentURI uri = Preferences.CONTENT_URI.addId(rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		
		// If everything works, we should not reach the following line:
		throw new SQLException("Failed to insert row into " + url);
	}

	public int delete(ContentURI url, String where, String[] whereArgs) {
		int count;
		//long rowId;
		switch (URL_MATCHER.match(url)) {
		case PREFERENCES:
			count = mDB.delete("preferences", where, whereArgs);
			break;

		case PREFERENCE_ID:
			String segment = url.getPathSegment(1); // contains rowId
			//rowId = Long.parseLong(segment);
			String whereString;
			if (!TextUtils.isEmpty(where)) {
				whereString = " AND (" + where + ')';
			} else {
				whereString = "";
			}

			count = mDB
					.delete("preferences", "_id=" + segment + whereString, whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	public int update(ContentURI url, ContentValues values, String where,
			String[] whereArgs) {
		Log.i(TAG, "update called for: " + url);
		int count;
		//long rowId;
		switch (URL_MATCHER.match(url)) {
		case PREFERENCES:
			count = mDB.update("preferences", values, where, whereArgs);
			break;

		case PREFERENCE_ID:
			String segment = url.getPathSegment(1); // contains rowId
			//rowId = Long.parseLong(segment);
			String whereString;
			if (!TextUtils.isEmpty(where)) {
				whereString = " AND (" + where + ')';
			} else {
				whereString = "";
			}

			count = mDB
					.update("preferences", values, 
							"_id=" + segment + whereString, whereArgs);
			break;

		default:
			Log.e(TAG, "Update received unknown URL: " + url);
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	public String getType(ContentURI url) {
		switch (URL_MATCHER.match(url)) {
		case PREFERENCES:
			return "vnd.openintents.cursor.dir/hardware.preference";

		case PREFERENCE_ID:
			return "vnd.openintents.cursor.item/hardware.preference";

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}

	static {
		URL_MATCHER = new ContentURIParser(ContentURIParser.NO_MATCH);
		URL_MATCHER.addURI("org.openintents.hardware", "preferences", PREFERENCES);
		URL_MATCHER.addURI("org.openintents.hardware", "preferences/#", PREFERENCE_ID);
		
		PREFERENCES_PROJECTION_MAP = new HashMap<String, String>();
		PREFERENCES_PROJECTION_MAP.put(Preferences._ID, "preferences._id");
		PREFERENCES_PROJECTION_MAP.put(Preferences.NAME, "preferences.name");
		PREFERENCES_PROJECTION_MAP.put(Preferences.VALUE, "preferences.value");
		
	}
}
