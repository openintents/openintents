/* 
 * Copyright (C) 2007 OpenIntents.org
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

package org.openintents.shopping;

import java.util.HashMap;

import org.openintents.R;
import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.Items;
import org.openintents.provider.Shopping.Lists;
import org.openintents.provider.Tag.Contents;
import org.openintents.provider.Tag.Tags;

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
 * Provides access to a database of shopping items and shopping lists. 
 * 
 */
public class ShoppingProvider extends ContentProvider {

	private SQLiteDatabase mDB;

	private static final String TAG = "ShoppingProvider";
	private static final String DATABASE_NAME = "shopping.db";
	private static final int DATABASE_VERSION = 1;

	private static HashMap<String, String> ITEMS_PROJECTION_MAP;
	private static HashMap<String, String> LISTS_PROJECTION_MAP;

	private static final int ITEMS = 1;
	private static final int ITEM_ID = 2;
	private static final int LISTS = 3;
	private static final int LIST_ID = 4;

	private static final ContentURIParser URL_MATCHER;

	private static final String DEFAULT_TAG = "DEFAULT";

	private static class DatabaseHelper extends ContentProviderDatabaseHelper {
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE items ("
					+ "_id INTEGER PRIMARY KEY," // Database Version 1
					+ "name VARCHAR," // V1
					+ "image VARCHAR," // V1
					+ "created INTEGER," // V1
					+ "modified INTEGER," // V1
					+ "accessed INTEGER" // V1
					+ ");");
			db.execSQL("CREATE TABLE lists ("
					+ "_id INTEGER PRIMARY KEY," // Database Version 1
					+ "name VARCHAR," // V1
					+ "image VARCHAR," // V1
					+ "created INTEGER," // V1
					+ "modified INTEGER," // V1
					+ "accessed INTEGER" // V1
					+ ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS items");
			db.execSQL("DROP TABLE IF EXISTS lists");
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

		String defaultOrderBy = null;
		switch (URL_MATCHER.match(url)) {
		case ITEMS:
			// queries for tags also return the uris, not only the ids.
			qb.setTables("items");
			qb.setProjectionMap(ITEMS_PROJECTION_MAP);
			defaultOrderBy = Items.DEFAULT_SORT_ORDER;
			break;

		case ITEM_ID:
			// queries for a tags just returns the ids.
			qb.setTables("items");
			qb.appendWhere("_id=" + url.getPathSegment(1));
			break;

		case LISTS:
			qb.setTables("lists");
			qb.setProjectionMap(LISTS_PROJECTION_MAP);
			defaultOrderBy = Lists.DEFAULT_SORT_ORDER;
			break;
			
		case LIST_ID:
			// queries for a tags just returns the ids.
			qb.setTables("lists");
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

	@Override
	public ContentURI insert(ContentURI url, ContentValues initialValues) {
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}
		
		// insert is supported for items or lists
		switch (URL_MATCHER.match(url)) {
		case ITEMS:
			return insertItem(url, values);
			// break; // unreachable code
			
		case LISTS:
			return insertList(url, values);
			// break; // unreachable code
			
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}
	
	public ContentURI insertItem(ContentURI url, ContentValues values) {
		long rowID;
		
		Long now = Long.valueOf(System.currentTimeMillis());
		Resources r = Resources.getSystem();
		

		// Make sure that the fields are all set
		if (!values.containsKey(Items.NAME)) {
			values.put(Items.NAME, r.getString(R.string.new_item));
		}
		
		if (!values.containsKey(Items.IMAGE)) {
			values.put(Items.IMAGE, "");
		}
		
		if (!values.containsKey(Items.CREATED_DATE)) {
			values.put(Items.CREATED_DATE, now);
		}

		if (!values.containsKey(Items.MODIFIED_DATE)) {
			values.put(Items.MODIFIED_DATE, now);
		}

		if (!values.containsKey(Items.ACCESSED_DATE)) {
			values.put(Items.ACCESSED_DATE, now);
		}
		
		// TODO: Here we should check, whether item exists already. 
		// (see TagsProvider)
		// insert the item. 
		rowID = mDB.insert("items", "items", values);
		if (rowID > 0) {
			ContentURI uri = Items.CONTENT_URI.addId(rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		
		// If everything works, we should not reach the following line:
		throw new SQLException("Failed to insert row into " + url);
	}

	public ContentURI insertList(ContentURI url, ContentValues values) {
		long rowID;
		
		Long now = Long.valueOf(System.currentTimeMillis());
		Resources r = Resources.getSystem();
		

		// Make sure that the fields are all set
		if (!values.containsKey(Lists.NAME)) {
			values.put(Lists.NAME, r.getString(R.string.new_list));
		}
		
		if (!values.containsKey(Lists.IMAGE)) {
			values.put(Lists.IMAGE, "");
		}
		
		if (!values.containsKey(Lists.CREATED_DATE)) {
			values.put(Lists.CREATED_DATE, now);
		}

		if (!values.containsKey(Lists.MODIFIED_DATE)) {
			values.put(Lists.MODIFIED_DATE, now);
		}

		if (!values.containsKey(Lists.ACCESSED_DATE)) {
			values.put(Lists.ACCESSED_DATE, now);
		}
		
		// TODO: Here we should check, whether item exists already.
		// (see TagsProvider)
		
		// insert the tag. 
		rowID = mDB.insert("lists", "lists", values);
		if (rowID > 0) {
			ContentURI uri = Items.CONTENT_URI.addId(rowID);
			getContext().getContentResolver().notifyChange(uri, null);
			return uri;
		}
		
		// If everything works, we should not reach the following line:
		throw new SQLException("Failed to insert row into " + url);
		
	}
	

	@Override
	public int delete(ContentURI url, String where, String[] whereArgs) {
		int count;
		long rowId = 0;
		switch (URL_MATCHER.match(url)) {
		case ITEMS:
			count = mDB.delete("items", where, whereArgs);
			break;

		case ITEM_ID:
			String segment = url.getPathSegment(1);
			rowId = Long.parseLong(segment);
			String whereString;
			if (!TextUtils.isEmpty(where)) {
				whereString = " AND (" + where + ')';
			} else {
				whereString = "";
			}

			count = mDB
					.delete("tag", "_id=" + segment + whereString, whereArgs);
			break;

		case LISTS:
			count = mDB.delete("items", where, whereArgs);
			break;

		case LIST_ID:
			segment = url.getPathSegment(1);
			rowId = Long.parseLong(segment);
			if (!TextUtils.isEmpty(where)) {
				whereString = " AND (" + where + ')';
			} else {
				whereString = "";
			}

			count = mDB
					.delete("tag", "_id=" + segment + whereString, whereArgs);
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
		// TODO which values can be updated.
		return 0;
	}

	@Override
	public String getType(ContentURI url) {
		switch (URL_MATCHER.match(url)) {
		case ITEMS:
			return "vnd.openintents.cursor.dir/shopping.item";

		case ITEM_ID:
			return "vnd.openintents.cursor.item/shopping.item";

		case LISTS:
			return "vnd.openintents.cursor.dir/shopping.list";

		case LIST_ID:
			return "vnd.openintents.cursor.item/shopping.list";

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}

	static {
		URL_MATCHER = new ContentURIParser(ContentURIParser.NO_MATCH);
		URL_MATCHER.addURI("org.openintents.shopping", "items", ITEMS);
		URL_MATCHER.addURI("org.openintents.shopping", "items/#", ITEM_ID);
		URL_MATCHER.addURI("org.openintents.shopping", "lists", LISTS);
		URL_MATCHER.addURI("org.openintents.shopping", "lists/#", LIST_ID);

		ITEMS_PROJECTION_MAP = new HashMap<String, String>();
		ITEMS_PROJECTION_MAP.put(Items._ID, "items._id");
		ITEMS_PROJECTION_MAP.put(Items.NAME, "items.name");
		ITEMS_PROJECTION_MAP.put(Items.IMAGE, "items.image");
		ITEMS_PROJECTION_MAP.put(Items.CREATED_DATE, "items.created");
		ITEMS_PROJECTION_MAP.put(Items.MODIFIED_DATE, "items.modified");
		ITEMS_PROJECTION_MAP.put(Items.ACCESSED_DATE, "items.accessed");
		
		LISTS_PROJECTION_MAP = new HashMap<String, String>();
		LISTS_PROJECTION_MAP.put(Lists._ID, "lists._id");
		LISTS_PROJECTION_MAP.put(Lists.NAME, "lists.name");
		LISTS_PROJECTION_MAP.put(Lists.IMAGE, "lists.image");
		LISTS_PROJECTION_MAP.put(Lists.CREATED_DATE, "lists.created");
		LISTS_PROJECTION_MAP.put(Lists.MODIFIED_DATE, "lists.modified");
		LISTS_PROJECTION_MAP.put(Lists.ACCESSED_DATE, "lists.accessed");
	}
}
