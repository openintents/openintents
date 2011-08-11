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

import org.openintents.historify.data.loaders.SourceIconHelper.IconLoadingStrategy;
import org.openintents.historify.data.model.source.EventSource;
import org.openintents.historify.uri.ContentUris;

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
	static final int DB_VERSION = 1;

	// table of sources
	public static final class SourcesTable {

		public static final String _TABLE = "sources";

		public static final String _ID = BaseColumns._ID;
		public static final String NAME = "name";
		public static final String DESCRIPTION = "description";
		public static final String ICON_URI = "icon_uri";
		public static final String ICON_LOADING_STRATEGY = "icon_loading_strategy";

		public static final String AUTHORITY = "authority";
		public static final String EVENT_INTENT = "event_intent";
		public static final String CONFIG_INTENT = "config_intent";
		public static final String INTERACT_INTENT = "interact_intent";
		public static final String INTERACT_ACTION_TITLE = "interact_action_title";

		public static final String IS_INTERNAL = "is_internal";
		public static final String UID = "uid";
		public static final String VERSION = "version";
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

		public static final String _VIEW = SourcesTable._TABLE+"/"+ContentUris.FILTERED_SOURCES_PATH;

		public static final String JOIN_CLAUSE = SourcesTable._TABLE
				+ " LEFT OUTER JOIN " + FiltersTable._TABLE + " ON "
				+ SourcesTable._TABLE + "." + SourcesTable._ID + " = "
				+ FiltersTable._TABLE + "." + FiltersTable.SOURCE_ID;

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.historify.filtered_source";

	}

	/**
	 * SQLite helper class.
	 */
	public static class OpenHelper extends SQLiteOpenHelper {

		public OpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL("CREATE TABLE " + SourcesTable._TABLE + " ("
					+ SourcesTable._ID + " INTEGER PRIMARY KEY,"
					+ SourcesTable.NAME + " TEXT NOT NULL,"
					+ SourcesTable.DESCRIPTION + " TEXT,"
					+ SourcesTable.ICON_URI + " TEXT,"
					+ SourcesTable.ICON_LOADING_STRATEGY + " TEXT DEFAULT '"+IconLoadingStrategy.useSourceIcon+"', "
					+ SourcesTable.AUTHORITY + " TEXT NOT NULL," 
					+ SourcesTable.EVENT_INTENT + " TEXT,"
					+ SourcesTable.CONFIG_INTENT + " TEXT,"
					+ SourcesTable.INTERACT_INTENT + " TEXT,"
					+ SourcesTable.INTERACT_ACTION_TITLE + " TEXT,"
					+ SourcesTable.STATE + " TEXT DEFAULT "
					+ EventSource.SourceState.ENABLED + ","
					+ SourcesTable.UID + " INTEGER DEFAULT 0,"
					+ SourcesTable.VERSION + " INTEGER DEFAULT 0,"
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
			new DefaultSources().insert(db);

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
