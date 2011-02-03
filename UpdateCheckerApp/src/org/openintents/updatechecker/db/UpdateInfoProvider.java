/*
 * Copyright (C) 2008  OpenIntents.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.openintents.updatechecker.db;

import java.util.HashMap;

import org.openintents.updatechecker.util.AlarmUtils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class UpdateInfoProvider extends ContentProvider {

	private static final String TAG = "UpdateInfoProvider";

	private static final String DATABASE_NAME = "updateinfo.db";
	private static final int DATABASE_VERSION = 1;
	private static final String UPDATE_INFO_TABLE = "update_info";

	private static HashMap<String, String> sUpdateInfoProjectionMap;

	private static final int UPDATE_INFOS = 1;
	private static final int UPDATE_INFOS_ID = 2;

	private static final UriMatcher sUriMatcher;

	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			createTable(db, UPDATE_INFO_TABLE);
		}

		private void createTable(SQLiteDatabase db, String tableName) {
			db.execSQL("CREATE TABLE " + tableName + " (" + UpdateInfo._ID
					+ " INTEGER PRIMARY KEY," + UpdateInfo.PACKAGE_NAME
					+ " TEXT," +  
					UpdateInfo.UPDATE_URL
					+ " TEXT,"+ UpdateInfo.LAST_CHECK + " LONG," + 
					UpdateInfo.IGNORE_VERSION_CODE
					+ " INTEGER,"+
					UpdateInfo.IGNORE_VERSION_NAME
					+ " TEXT,"+
					UpdateInfo.NO_NOTIFICATIONS
					+ " INTEGER,"+
					UpdateInfo.LATEST_VERSION_CODE
					+ " INTEGER,"+
					UpdateInfo.LATEST_VERSION_NAME
					+ " TEXT,"+
					UpdateInfo.LATEST_COMMENT
					+ " TEXT"+
					");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + UPDATE_INFO_TABLE);
			onCreate(db);
		}
	}

	private DatabaseHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());	
		AlarmUtils.refreshUpdateAlarm(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		switch (sUriMatcher.match(uri)) {
		case UPDATE_INFOS:
			qb.setTables(UPDATE_INFO_TABLE);
			qb.setProjectionMap(sUpdateInfoProjectionMap);
			break;

		case UPDATE_INFOS_ID:
			qb.setTables(UPDATE_INFO_TABLE);
			qb.setProjectionMap(sUpdateInfoProjectionMap);
			qb.appendWhere(UpdateInfo._ID + "=" + uri.getLastPathSegment());
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			orderBy = UpdateInfo.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sortOrder;
		}

		// Get the database and run the query
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null,
				null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data
		// changes
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case UPDATE_INFOS:

			return UpdateInfo.CONTENT_TYPE;

		case UPDATE_INFOS_ID:
			return UpdateInfo.CONTENT_ITEM_TYPE;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {

		switch (sUriMatcher.match(uri)) {
		case UPDATE_INFOS:
			return insertInternal(initialValues, UPDATE_INFO_TABLE, uri);

		default:
			// Validate the requested uri
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	private Uri insertInternal(ContentValues initialValues,
			String appointmentTableName, Uri uri) {
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);
		} else {
			values = new ContentValues();
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowId = db.insert(appointmentTableName, UpdateInfo.PACKAGE_NAME,
				values);
		if (rowId > 0) {
			Uri noteUri = ContentUris.withAppendedId(UpdateInfo.CONTENT_URI,
					rowId);
			getContext().getContentResolver().notifyChange(noteUri, null);
			return noteUri;
		}

		throw new SQLException("Failed to insert row into " + uri);

	}

	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case UPDATE_INFOS:
			count = db.delete(UPDATE_INFO_TABLE, where, whereArgs);
			break;

		case UPDATE_INFOS_ID:
			String id = uri.getLastPathSegment();
			count = db.delete(UPDATE_INFO_TABLE,
					UpdateInfo._ID
							+ "="
							+ id
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where,
			String[] whereArgs) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		switch (sUriMatcher.match(uri)) {
		case UPDATE_INFOS:
			count = db.update(UPDATE_INFO_TABLE, values, where, whereArgs);
			break;

		case UPDATE_INFOS_ID:
			String id = uri.getLastPathSegment();
			count = db.update(UPDATE_INFO_TABLE, values,
					UpdateInfo._ID
							+ "="
							+ id
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(UpdateInfo.AUTHORITY, "info", UPDATE_INFOS);
		sUriMatcher.addURI(UpdateInfo.AUTHORITY, "info/#", UPDATE_INFOS_ID);
		sUpdateInfoProjectionMap = new HashMap<String, String>();
		sUpdateInfoProjectionMap.put(UpdateInfo._ID, UpdateInfo._ID);
		sUpdateInfoProjectionMap.put(UpdateInfo.PACKAGE_NAME,
				UpdateInfo.PACKAGE_NAME);
		sUpdateInfoProjectionMap.put(UpdateInfo.UPDATE_URL,
				UpdateInfo.UPDATE_URL);
		sUpdateInfoProjectionMap.put(UpdateInfo.LAST_CHECK,
				UpdateInfo.LAST_CHECK);
		sUpdateInfoProjectionMap.put(UpdateInfo.IGNORE_VERSION_CODE,
				UpdateInfo.IGNORE_VERSION_CODE);
		sUpdateInfoProjectionMap.put(UpdateInfo.IGNORE_VERSION_NAME,
				UpdateInfo.IGNORE_VERSION_NAME);
		sUpdateInfoProjectionMap.put(UpdateInfo.NO_NOTIFICATIONS,
				UpdateInfo.NO_NOTIFICATIONS);
		sUpdateInfoProjectionMap.put(UpdateInfo.LATEST_VERSION_CODE,
				UpdateInfo.LATEST_VERSION_CODE);
		sUpdateInfoProjectionMap.put(UpdateInfo.LATEST_VERSION_NAME,
				UpdateInfo.LATEST_VERSION_NAME);
		sUpdateInfoProjectionMap.put(UpdateInfo.LATEST_COMMENT,
				UpdateInfo.LATEST_COMMENT);
	}
}
