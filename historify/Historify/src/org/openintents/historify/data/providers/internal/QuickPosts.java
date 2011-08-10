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

import org.openintents.historify.data.model.source.EventSource;
import org.openintents.historify.data.providers.Events;
import org.openintents.historify.utils.UriUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * 
 * Helper class for constants in {@link QuikcPostsProvider}.
 * 
 * @author berke.andras
 */
public class QuickPosts {

	public static final String SOURCE_NAME = "QuickPosts";
	public static final String DESCRIPTION = "Events posted by other applications via QuickPost.";
	
	public static final String QUICKPOSTS_AUTHORITY = "org.openintents.historify.internal.quickposts";
	public static final Uri SOURCE_URI = UriUtils.sourceAuthorityToUri(QUICKPOSTS_AUTHORITY);
	
	public static final String QUICKPOST_SOURCES_PATH = "quickpost_sources";
	public static final String QUICKPOST_RAW_EVENTS_PATH = "raw_events";
	
	static final String DB_NAME = "quickposts.db";
	static final int DB_VERSION = 3;

	// table of quickpost sources
	public static final class QuickPostSourcesTable  {

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.historify.quickpostSource";

		public static final String _TABLE = "quickpost_sources";
		public static final String _ID = BaseColumns._ID;
		public static final String NAME = "name";
		public static final String DESCRIPTION = "description";
		public static final String ICON_URI = "icon_uri";

		public static final String VERSION = "version";
		public static final String EVENT_INTENT = "event_intent";
		public static final String INTERACT_INTENT = "interact_intent";
		public static final String INTERACT_ACTION_TITLE = "interact_action_title";

		public static final String UID = "uid";
		public static final String STATE = "state";

	}
	
	public static final class QuickPostEventsTable {
		
		//all columns defined by data.providers.Events
		
		//additional columns:
		public static final String SOURCE_ID = "source_id";
		
		public static final String _TABLE = "quickpost_events";
	}
	
	
	public static final String JOIN_CLAUSE = QuickPostEventsTable._TABLE
		+ " LEFT OUTER JOIN " + QuickPostSourcesTable._TABLE + " ON "
		+ QuickPostEventsTable._TABLE + "." + QuickPostEventsTable.SOURCE_ID + " = "
		+ QuickPostSourcesTable._TABLE + "." + QuickPostSourcesTable._ID;

	
	/**
	 * SQLite helper class.
	 */
	static class OpenHelper extends SQLiteOpenHelper {

		OpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL("CREATE TABLE " + QuickPostSourcesTable._TABLE + " ("
					+ QuickPostSourcesTable._ID + " INTEGER PRIMARY KEY,"
					+ QuickPostSourcesTable.NAME + " TEXT NOT NULL,"
					+ QuickPostSourcesTable.DESCRIPTION + " TEXT,"
					+ QuickPostSourcesTable.ICON_URI + " TEXT," 
					+ QuickPostSourcesTable.EVENT_INTENT + " TEXT,"
					+ QuickPostSourcesTable.INTERACT_INTENT + " TEXT,"
					+ QuickPostSourcesTable.INTERACT_ACTION_TITLE + " TEXT,"
					+ QuickPostSourcesTable.STATE + " TEXT DEFAULT "
					+ EventSource.SourceState.ENABLED + ","
					+ QuickPostSourcesTable.VERSION + " INTEGER DEFAULT 0,"
					+ QuickPostSourcesTable.UID + " INTEGER DEFAULT 0);");

			db.execSQL("CREATE TABLE " + QuickPostEventsTable._TABLE + " ("
					+ Events._ID + " INTEGER PRIMARY KEY,"
					+ QuickPostEventsTable.SOURCE_ID + " INTEGER,"
					+ Events.EVENT_KEY + " TEXT,"
					+ Events.CONTACT_KEY + " TEXT,"
					+ Events.PUBLISHED_TIME + " INTEGER NOT NULL,"
					+ Events.MESSAGE + " TEXT NOT NULL,"
					+ Events.ORIGINATOR + " TEXT NOT NULL);");
					
		}

		private void onErase(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS " + QuickPostSourcesTable._TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + QuickPostEventsTable._TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			Log.w(QuickPostsProvider.NAME, "Upgrading database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data");

			onErase(db);
			onCreate(db);
		}

	}

}
