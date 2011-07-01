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

package org.openintents.samples.lendme.data.persistence;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class ItemsProviderHelper {

	public static final String AUTHORITY = "org.openintents.samples.lendme.items";
	public static final String PATH_ITEMS = "items";
	
	public static final Uri CONTENT_URI = Uri.parse("content://"+AUTHORITY+"/"+PATH_ITEMS);
	
	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.lendme.item";
	public static final String ITEM_CONTENT_TYPE ="vnd.android.cursor.item/vnd.lendme.item";

	private static final String DB_NAME = "items.db";
	private static final int DB_VERSION = 1;
	
	public static class ItemsTable {
		public static final String _ID = BaseColumns._ID;
		public static final String TABLE_NAME = "items";
		
		public static final String CONTACT_KEY = "contact_key";
		public static final String LENDING_START = "lending_start";
		public static final String ITEM_NAME = "item_name";
		public static final String ITEM_DESCRIPTION = "item_description";
		public static final String OWNER = "owner";
			}
	
	public static class OpenHelper extends SQLiteOpenHelper {

		OpenHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {

			db.execSQL("CREATE TABLE " + ItemsTable.TABLE_NAME + " ("
					+ ItemsTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ ItemsTable.CONTACT_KEY + " TEXT NOT NULL,"
					+ ItemsTable.LENDING_START + " INTEGER NOT NULL,"
					+ ItemsTable.ITEM_NAME + " TEXT NOT NULL,"
					+ ItemsTable.ITEM_DESCRIPTION + " TEXT,"
					+ ItemsTable.OWNER + " TEXT NOT NULL);"); 
					
		}

		private void onErase(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS " + ItemsTable.TABLE_NAME);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			Log.w(ItemsProvider.NAME, "Upgrading database from version "
					+ oldVersion + " to " + newVersion
					+ ", which will destroy all old data");

			onErase(db);
			onCreate(db);
		}
		
	}
	
}
