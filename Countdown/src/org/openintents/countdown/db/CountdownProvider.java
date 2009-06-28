/* 
 * Copyright (C) 2008 OpenIntents.org
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

package org.openintents.countdown.db;


import java.util.HashMap;

import org.openintents.countdown.db.Countdown.Durations;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


/**
 * Provides access to a database of notes. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class CountdownProvider extends ContentProvider {

    private static final String TAG = "CountdownProvider";

    private static final String DATABASE_NAME = "countdown.db";
    
    /**
     * Database version.
     * <ul>
     * <li>Version 1 (1.0.0 - 1.0.1): title, duration, deadline, 
     *     ring, ringtone, vibrate, created, modified</li>
     * <li>Version 2 (1.1.0-beta1): tags, encrypted, theme</li>
     * <li>Version 3 (1.1.0-rc1 - ): notification, light</li>
     * </ul>
     */
    private static final int DATABASE_VERSION = 3;
    
    private static final String DURATIONS_TABLE_NAME = "durations";

    private static HashMap<String, String> sDurationsProjectionMap;

    private static final int DURATIONS = 1;
    private static final int DURATION_ID = 2;

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
            db.execSQL("CREATE TABLE " + DURATIONS_TABLE_NAME + " ("
            		// Version 1:
                    + Durations._ID + " INTEGER PRIMARY KEY,"
                    + Durations.TITLE + " TEXT,"
                    + Durations.DURATION + " INTEGER,"
                    + Durations.DEADLINE_DATE + " INTEGER,"
                    + Durations.RING + " INTEGER,"
                    + Durations.RINGTONE + " TEXT,"
                    + Durations.VIBRATE + " INTEGER,"
                    + Durations.CREATED_DATE + " INTEGER,"
                    + Durations.MODIFIED_DATE + " INTEGER,"
                    // Version 2:
                    + Durations.USER_DEADLINE_DATE + " INTEGER,"
                    + Durations.AUTOMATE + " INTEGER,"
                    + Durations.AUTOMATE_INTENT + " TEXT,"
                    + Durations.AUTOMATE_TEXT + " TEXT,"
                    // Version 3:
                    + Durations.NOTIFICATION + " INTEGER,"
                    + Durations.LIGHT + " INTEGER"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            if (newVersion > oldVersion) {
            	// Upgrade
	            switch(oldVersion) {
	            case 1:
	            	// Upgrade from version 1 to 2.
	            	// It seems SQLite3 only allows to add one column at a time,
	            	// so we need three SQL statements:
	            	try {
		            	db.execSQL("ALTER TABLE " + DURATIONS_TABLE_NAME + " ADD COLUMN "
		                        + Durations.USER_DEADLINE_DATE + " INTEGER;");
		            	db.execSQL("ALTER TABLE " + DURATIONS_TABLE_NAME + " ADD COLUMN "
		                        + Durations.AUTOMATE + " INTEGER;");
		            	db.execSQL("ALTER TABLE " + DURATIONS_TABLE_NAME + " ADD COLUMN "
		                        + Durations.AUTOMATE_INTENT + " TEXT;");
		            	db.execSQL("ALTER TABLE " + DURATIONS_TABLE_NAME + " ADD COLUMN "
		                        + Durations.AUTOMATE_TEXT + " TEXT;");

		            	// Set Durations to 0 for all old tasks
		            	// Otherwise with the next reboot, all old countdowns are started again.
		            	long now = System.currentTimeMillis();
		            	ContentValues values = new ContentValues();
		            	values.put(Durations.DEADLINE_DATE, 0);
		                db.update(DURATIONS_TABLE_NAME, values, Durations.DEADLINE_DATE + " < " + now, null);
	            	} catch (SQLException e) {
	            		Log.e(TAG, "Error executing SQL: ", e);
	            		// If the error is "duplicate column name" then everything is fine,
	            		// as this happens after upgrading 1->2, then downgrading 2->1, 
	            		// and then upgrading again 1->2.
	            	}
	            	
	                
	            	
	            	// fall through for further upgrades.
	            case 2:
	            	// Upgrade from version 2 to 3.
	            	// It seems SQLite3 only allows to add one column at a time,
	            	// so we need three SQL statements:
	            	try {
		            	db.execSQL("ALTER TABLE " + DURATIONS_TABLE_NAME + " ADD COLUMN "
		                        + Durations.NOTIFICATION + " INTEGER;");
		            	db.execSQL("ALTER TABLE " + DURATIONS_TABLE_NAME + " ADD COLUMN "
		                        + Durations.LIGHT + " INTEGER;");

		            	// Set Notification and light to 1 for all old tasks
		            	ContentValues values = new ContentValues();
		            	values.put(Durations.NOTIFICATION, 1);
		            	values.put(Durations.LIGHT, 1);
		                db.update(DURATIONS_TABLE_NAME, values, null, null);
	            	} catch (SQLException e) {
	            		Log.e(TAG, "Error executing SQL: ", e);
	            		// If the error is "duplicate column name" then everything is fine,
	            		// as this happens after upgrading 2->3, then downgrading 3->2, 
	            		// and then upgrading again 2->3.
	            	}
	                
	            	// fall through for further upgrades.
	            	
	            	break;
	            default:
	            	Log.w(TAG, "Unknown version " + oldVersion + ". Creating new database.");
	                db.execSQL("DROP TABLE IF EXISTS " + DURATIONS_TABLE_NAME);
	            	onCreate(db);
	            }
            } else { // newVersion <= oldVersion
            	// Downgrade
            	Log.w(TAG, "Don't know how to downgrade. Will not touch database and hope they are compatible.");
                // Do nothing.
            }
        }
    }

    
    private DatabaseHelper mOpenHelper;

    @Override
    public boolean onCreate() {
        mOpenHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case DURATIONS:
            qb.setTables(DURATIONS_TABLE_NAME);
            qb.setProjectionMap(sDurationsProjectionMap);
            break;

        case DURATION_ID:
            qb.setTables(DURATIONS_TABLE_NAME);
            qb.setProjectionMap(sDurationsProjectionMap);
            qb.appendWhere(Countdown.Durations._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Countdown.Durations.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case DURATIONS:
            return Durations.CONTENT_TYPE;

        case DURATION_ID:
            return Durations.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != DURATIONS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        Long now = Long.valueOf(System.currentTimeMillis());

        // Make sure that the fields are all set
        if (values.containsKey(Countdown.Durations.CREATED_DATE) == false) {
            values.put(Countdown.Durations.CREATED_DATE, now);
        }

        if (values.containsKey(Countdown.Durations.MODIFIED_DATE) == false) {
            values.put(Countdown.Durations.MODIFIED_DATE, now);
        }

        if (values.containsKey(Countdown.Durations.TITLE) == false) {
            Resources r = Resources.getSystem();
            values.put(Countdown.Durations.TITLE, "");//r.getString(android.R.string.untitled));
        }

        if (values.containsKey(Countdown.Durations.DURATION) == false) {
            values.put(Countdown.Durations.DURATION, 0);
        }
        
        if (values.containsKey(Countdown.Durations.DEADLINE_DATE) == false) {
            values.put(Countdown.Durations.DEADLINE_DATE, 0);
        }

        if (values.containsKey(Countdown.Durations.RING) == false) {
            values.put(Countdown.Durations.RING, 0);
        }

        if (values.containsKey(Countdown.Durations.RINGTONE) == false) {
            values.put(Countdown.Durations.RINGTONE, (String) null);
        }

        if (values.containsKey(Countdown.Durations.VIBRATE) == false) {
            values.put(Countdown.Durations.VIBRATE, 0);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(DURATIONS_TABLE_NAME, Durations.TITLE, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(Countdown.Durations.CONTENT_URI, rowId);
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
        case DURATIONS:
            count = db.delete(DURATIONS_TABLE_NAME, where, whereArgs);
            break;

        case DURATION_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(DURATIONS_TABLE_NAME, Durations._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case DURATIONS:
            count = db.update(DURATIONS_TABLE_NAME, values, where, whereArgs);
            break;

        case DURATION_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.update(DURATIONS_TABLE_NAME, values, Durations._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(Countdown.AUTHORITY, Durations.PATH, DURATIONS);
        sUriMatcher.addURI(Countdown.AUTHORITY, Durations.PATH + "/#", DURATION_ID);

        sDurationsProjectionMap = new HashMap<String, String>();
        sDurationsProjectionMap.put(Durations._ID, Durations._ID);
        sDurationsProjectionMap.put(Durations.TITLE, Durations.TITLE);
        sDurationsProjectionMap.put(Durations.DURATION, Durations.DURATION);
        sDurationsProjectionMap.put(Durations.DEADLINE_DATE, Durations.DEADLINE_DATE);
        sDurationsProjectionMap.put(Durations.RING, Durations.RING);
        sDurationsProjectionMap.put(Durations.RINGTONE, Durations.RINGTONE);
        sDurationsProjectionMap.put(Durations.VIBRATE, Durations.VIBRATE);
        sDurationsProjectionMap.put(Durations.CREATED_DATE, Durations.CREATED_DATE);
        sDurationsProjectionMap.put(Durations.MODIFIED_DATE, Durations.MODIFIED_DATE);
        sDurationsProjectionMap.put(Durations.USER_DEADLINE_DATE, Durations.USER_DEADLINE_DATE);
        sDurationsProjectionMap.put(Durations.AUTOMATE, Durations.AUTOMATE);
        sDurationsProjectionMap.put(Durations.AUTOMATE_INTENT, Durations.AUTOMATE_INTENT);
        sDurationsProjectionMap.put(Durations.AUTOMATE_TEXT, Durations.AUTOMATE_TEXT);
        sDurationsProjectionMap.put(Durations.NOTIFICATION, Durations.NOTIFICATION);
        sDurationsProjectionMap.put(Durations.LIGHT, Durations.LIGHT);
    }
}
