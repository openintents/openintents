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

package org.openintents.timescape.api.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.sonyericsson.eventstream.EventStreamConstants.FriendColumns;
import com.sonyericsson.eventstream.EventStreamConstants.PluginColumns;

public class EventStreamHelper {

	public static final String DB_NAME = "event_stream.db";
	public static final int DB_VERSION = 1;

	public static final String AUTHORITY = "com.sonyericsson.eventstream";
	public static final String PLUGINS_PATH = "plugins";
	public static final String SOURCES_PATH = "sources";
	public static final String FRIENDS_PATH = "friends";
	public static final String EVENTS_PATH = "events";
	
	public static final class PluginsTable {

		public static final String _TABLE = "plugins";

		public static final String _ID = BaseColumns._ID;
		public static final String NAME = PluginColumns.NAME;
		public static final String DESCRIPTION = PluginColumns.CONFIGURATION_TEXT;
		public static final String ICON_URI = PluginColumns.ICON_URI;
		public static final String CONFIG_ACTIVITY = PluginColumns.CONFIGURATION_ACTIVITY;
		public static final String CONFIG_STATE = PluginColumns.CONFIGURATION_STATE;
		public static final String API_VERSION = PluginColumns.API_VERSION;
		public static final String UID = "uid";
		public static final String PLUGIN_KEY = PluginColumns.PLUGIN_KEY;

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.timescapecompatibility.plugin";
		public static final String ITEM_CONTENT_TYPE = "vnd.android.cursor.item/vnd.timescapecompatibility.plugin";

	}

	public static final class SourcesTable {

		public static final String _TABLE = "sources";

		public static final String _ID = BaseColumns._ID;
		public static final String PLUGIN_ID =  "plugin_id";
		public static final String UID = "uid";
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.timescapecompatibility.source";
		public static final String ITEM_CONTENT_TYPE = "vnd.android.cursor.item/vnd.timescapecompatibility.source";
	}

	public static final class FriendsTable {

		public static final String _TABLE = "friends";
		
		public static final String _ID = BaseColumns._ID;
		public static final String SOURCE_ID = FriendColumns.SOURCE_ID;
		public static final String PLUGIN_ID = FriendColumns.PLUGIN_ID;
		public static final String CONTACTS_REFERENCE = FriendColumns.CONTACTS_REFERENCE;
		public static final String FRIEND_KEY = FriendColumns.FRIEND_KEY;
		public static final String UID = "uid";
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.timescapecompatibility.friend";
		public static final String ITEM_CONTENT_TYPE = "vnd.android.cursor.item/vnd.timescapecompatibility.friend";

	}
	
	public static final class EventsTable {
		
		public static final String _TABLE = "events";
		public static final String _ID = BaseColumns._ID;
		public static final String EVENT_KEY = "event_key";
		public static final String FRIEND_KEY = "friend_key";
		public static final String MESSAGE = "message";
		public static final String OUTGOING = "outgoing";
		public static final String PERSONAL = "personal";
		public static final String PUBLISHED_TIME = "published_time";
		public static final String SOURCE_ID = "source_id";
		public static final String PLUGIN_ID = "plugin_id";
		public static final String UID = "uid";
        
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.timescapecompatibility.event";
		public static final String ITEM_CONTENT_TYPE = "vnd.android.cursor.item/vnd.timescapecompatibility.event";
		
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
			
			db.execSQL("CREATE TABLE " + PluginsTable._TABLE + " ("
					+ PluginsTable._ID + " INTEGER PRIMARY KEY,"
					+ PluginsTable.PLUGIN_KEY + " TEXT,"
					+ PluginsTable.NAME + " TEXT NOT NULL,"
					+ PluginsTable.DESCRIPTION + " TEXT,"
					+ PluginsTable.ICON_URI + " TEXT,"
					+ PluginsTable.CONFIG_ACTIVITY + " TEXT,"
					+ PluginsTable.CONFIG_STATE + " TEXT,"
					+ PluginsTable.API_VERSION + " INTEGER NOT NULL,"
					+ PluginsTable.UID + " INTEGER NOT NULL);");

			db.execSQL("CREATE TABLE " + SourcesTable._TABLE + " ("
					+ SourcesTable._ID + " INTEGER PRIMARY KEY,"
					+ SourcesTable.UID + " INTEGER NOT NULL);");
			
			db.execSQL("CREATE TABLE " + FriendsTable._TABLE + " ("
					+ FriendsTable._ID + " INTEGER PRIMARY KEY,"
					+ FriendsTable.SOURCE_ID + " INTEGER,"
					+ FriendsTable.PLUGIN_ID + " INTEGER,"
					+ FriendsTable.CONTACTS_REFERENCE + " TEXT,"
					+ FriendsTable.FRIEND_KEY + " TEXT,"
					+ FriendsTable.UID + " INTEGER NOT NULL);");
			
			db.execSQL("CREATE TABLE " + EventsTable._TABLE + " ("
					+ EventsTable._ID + " INTEGER PRIMARY KEY,"
					+ EventsTable.EVENT_KEY + " TEXT,"
					+ EventsTable.FRIEND_KEY + " TEXT,"
					+ EventsTable.MESSAGE + " TEXT,"
					+ EventsTable.OUTGOING + " INTEGER DEFAULT 0,"
					+ EventsTable.PERSONAL + " INTEGER DEFAULT 0,"
					+ EventsTable.PUBLISHED_TIME + " INTEGER,"
					+ EventsTable.SOURCE_ID + " INTEGER NOT NULL,"
					+ EventsTable.PLUGIN_ID + " INTEGER NOT NULL,"
					+ EventsTable.UID + " INTEGER NOT NULL);");
		}

		private void onErase(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS " + PluginsTable._TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + SourcesTable._TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + FriendsTable._TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + EventsTable._TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			Log.w(EventStreamCompatibleProvider.N, "Upgrading database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data");

			onErase(db);
			onCreate(db);
		}

	}

	public static Uri getUri(String path) {
		 return new Uri.Builder()
			.scheme("content")
			.authority(EventStreamHelper.AUTHORITY)
			.appendPath(path).build();
	}

}
