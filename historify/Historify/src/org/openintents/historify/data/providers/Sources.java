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

import org.openintents.historify.data.model.source.AbstractSource;
import org.openintents.historify.data.providers.internal.FactoryTest;
import org.openintents.historify.data.providers.internal.Messaging;
import org.openintents.historify.data.providers.internal.Telephony;
import org.openintents.historify.uri.Actions;
import org.openintents.historify.utils.UriUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * 
 * Helper class for constants and db creation in {@link SourcesProvider}.
 * 
 * @author berke.andras
 */
public final class Sources {

	static final String DB_NAME = "sources.db";
	static final int DB_VERSION = 25;

	// table of sources
	public static final class SourcesTable {

		public static final String _TABLE = "sources";

		public static final String _ID = BaseColumns._ID;
		public static final String NAME = "name";
		public static final String DESCRIPTION = "description";
		public static final String ICON_URI = "icon_uri";

		public static final String AUTHORITY = "authority";
		public static final String EVENT_INTENT = "event_intent";

		public static final String IS_INTERNAL = "is_internal";
		public static final String STATE = "state";

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.historify.source";
		public static final String ITEM_CONTENT_TYPE = "vnd.android.cursor.item/vnd.historify.source";

	}

	// table of filters
	public static final class FiltersTable {

		public static final String _TABLE = "filters";

		public static final String _ID = "filter_id";
		public static final String CONTACT_LOOKUP_KEY = "contact_lookup_key";
		public static final String SOURCE_ID = "source_id";
		public static final String FILTERED_STATE = "filtered_state";

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.historify.source_filter";
		public static final String ITEM_CONTENT_TYPE = "vnd.android.cursor.item/vnd.historify.source_filter";
	}

	// view construed by joining the sources and filters table in order to query
	// the filters for a particular contact
	public static final class FilteredSourcesView {

		public static final String _VIEW = "filtered_sources";

		public static final String JOIN_CLAUSE = SourcesTable._TABLE
				+ " LEFT OUTER JOIN " + FiltersTable._TABLE + " ON "
				+ SourcesTable._TABLE + "." + SourcesTable._ID + " = "
				+ FiltersTable._TABLE + "." + FiltersTable.SOURCE_ID;

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.historify.filtered_source";

	}

	/**
	 * SQLite helper class.
	 */
	static class OpenHelper extends SQLiteOpenHelper {

		OpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL("CREATE TABLE " + SourcesTable._TABLE + " ("
					+ SourcesTable._ID + " INTEGER PRIMARY KEY,"
					+ SourcesTable.NAME + " TEXT NOT NULL,"
					+ SourcesTable.DESCRIPTION + " TEXT,"
					+ SourcesTable.ICON_URI + " TEXT," + SourcesTable.AUTHORITY
					+ " TEXT NOT NULL," + SourcesTable.EVENT_INTENT + " TEXT,"
					+ SourcesTable.STATE + " TEXT DEFAULT "
					+ AbstractSource.SourceState.ENABLED + ","
					+ SourcesTable.IS_INTERNAL + " INTEGER DEFAULT 0);");

			db.execSQL("CREATE TABLE " + FiltersTable._TABLE + " ("
					+ FiltersTable._ID + " INTEGER PRIMARY KEY,"
					+ FiltersTable.CONTACT_LOOKUP_KEY + " TEXT NOT NULL,"
					+ FiltersTable.SOURCE_ID + " INTEGER NOT NULL,"
					+ FiltersTable.FILTERED_STATE + " TEXT NOT NULL,"
					+ " FOREIGN KEY (" + FiltersTable.SOURCE_ID
					+ ") REFERENCES " + SourcesTable._TABLE + " ("
					+ SourcesTable._ID + "));");

			db.execSQL("CREATE INDEX " + FiltersTable._TABLE + "_"
					+ FiltersTable.CONTACT_LOOKUP_KEY + " ON "
					+ FiltersTable._TABLE + " ("
					+ FiltersTable.CONTACT_LOOKUP_KEY + ");");

			// adding test data
			ContentValues cv = new ContentValues();
			cv.put(SourcesTable.NAME, FactoryTest.SOURCE_NAME);
			cv.put(SourcesTable.DESCRIPTION, FactoryTest.DESCRIPTION);
			cv.put(SourcesTable.AUTHORITY, FactoryTest.FACTORY_TEST_AUTHORITY);
			cv.put(SourcesTable.IS_INTERNAL, 1);
			cv.put(SourcesTable.ICON_URI, UriUtils.drawableToUri("source_factory_test").toString());
			db.insert(SourcesTable._TABLE, null, cv);
			
			cv = new ContentValues();
			cv.put(SourcesTable.NAME, Messaging.SOURCE_NAME);
			cv.put(SourcesTable.DESCRIPTION, Messaging.DESCRIPTION);
			cv.put(SourcesTable.AUTHORITY, Messaging.MESSAGING_AUTHORITY);
			cv.put(SourcesTable.EVENT_INTENT, Actions.VIEW_MESSAGING_EVENT);
			cv.put(SourcesTable.IS_INTERNAL, 1);
			cv.put(SourcesTable.ICON_URI, UriUtils.drawableToUri("source_messaging").toString());
			db.insert(SourcesTable._TABLE, null, cv);

			cv = new ContentValues();
			cv.put(SourcesTable.NAME, Telephony.SOURCE_NAME);
			cv.put(SourcesTable.DESCRIPTION, Telephony.DESCRIPTION);
			cv.put(SourcesTable.AUTHORITY, Telephony.TELEPHONY_AUTHORITY);
			cv.put(SourcesTable.IS_INTERNAL, 1);
			cv.put(SourcesTable.ICON_URI, UriUtils.drawableToUri("source_telephony").toString());
			db.insert(SourcesTable._TABLE, null, cv);

		}

		private void onErase(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS " + SourcesTable._TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + FiltersTable._TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			Log.w(SourcesProvider.NAME, "Upgrading database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data");

			onErase(db);
			onCreate(db);
		}

	}
}
