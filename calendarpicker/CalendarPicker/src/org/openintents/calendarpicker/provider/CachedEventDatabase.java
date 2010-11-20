/*
 * Copyright (C) 2010 Karl Ostmo
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

package org.openintents.calendarpicker.provider;

import java.util.List;

import org.openintents.calendarpicker.container.SimpleEvent;
import org.openintents.calendarpicker.contract.CalendarPickerConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public class CachedEventDatabase extends SQLiteOpenHelper {

	static final String TAG = "CachedEventDatabase"; 

    static final int DATABASE_VERSION = 3;
    static final String DATABASE_NAME = "CACHED_EVENTS";

    public static final String TABLE_EVENTS = "TABLE_EVENTS";
    public static final String TABLE_CALENDARS = "TABLE_CALENDARS";
    

    public static final String KEY_EVENT_ID = BaseColumns._ID;
    public static final String KEY_EVENT_TITLE = CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.TITLE;
    public static final String KEY_CALENDAR_ID = CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.CALENDAR_ID;
    public static final String KEY_EVENT_TIMESTAMP = CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.TIMESTAMP;

    public static final String KEY_EVENT_QUANTITY0 = "QUANTITY0";
    public static final String KEY_EVENT_QUANTITY1 = "QUANTITY1";

    public static final String KEY_CALENDAR_TITLE = CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.TITLE;

    final static String SQL_CREATE_CALENDARS_TABLE =
        "create table " + TABLE_CALENDARS + " (" 
        + BaseColumns._ID + " integer primary key autoincrement, "
        + KEY_CALENDAR_TITLE + " text);";
    
    final static String SQL_CREATE_EVENTS_TABLE =
        "create table " + TABLE_EVENTS + " (" 
        + BaseColumns._ID + " integer primary key, "
        + KEY_CALENDAR_ID + " integer, "
        + KEY_EVENT_QUANTITY0 + " real, "
        + KEY_EVENT_QUANTITY1 + " real, "
        + KEY_EVENT_TIMESTAMP + " integer, "
        + KEY_EVENT_TITLE + " text);";

    
    final static String[] table_list = {TABLE_CALENDARS, TABLE_EVENTS};
    final static String[] table_creation_commands = {SQL_CREATE_CALENDARS_TABLE, SQL_CREATE_EVENTS_TABLE};

    // ============================================================
    public CachedEventDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    // ============================================================
    public void clearData() {

	    SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		
	    for (String table : table_list)
	    	db.delete(table, null, null);

	    try {
	    	db.setTransactionSuccessful();
	    } finally {
	    	db.endTransaction();
	    }
	    db.close();
    }
    
    // ============================================================
    /** Populates the database with random events, given a calendar to specify the year and month */
    public long populateEvents(List<SimpleEvent> events) {

		// Wipe the database clean, first.
		clearData();
    	
	    SQLiteDatabase db = getWritableDatabase();
		db.beginTransaction();
		
		ContentValues cv = new ContentValues();
		cv.put(KEY_CALENDAR_TITLE, "Cached Calendar");
		long calendar_id = db.insert(TABLE_CALENDARS, null, cv);

		for (SimpleEvent event : events) {
			cv.clear();
			cv.put(KEY_CALENDAR_ID, calendar_id);
			cv.put(BaseColumns._ID, event.id);
			cv.put(KEY_EVENT_TITLE, event.title);
			cv.put(KEY_EVENT_TIMESTAMP, event.timestamp.getTime());
			long event_id = db.insert(TABLE_EVENTS, null, cv);
		}

	    try {
	    	db.setTransactionSuccessful();
	    } finally {
	    	db.endTransaction();
	    }
	    db.close();
	    
	    return calendar_id;
    }
    
    // ============================================================
    @Override
    public void onCreate(SQLiteDatabase db) {
    	for (String sql : table_creation_commands)
        	db.execSQL( sql );
    }

    // ============================================================
    public void drop_all_tables(SQLiteDatabase db) {
    	for (String table : table_list)
    		db.execSQL("DROP TABLE IF EXISTS " + table);
    }

    // ============================================================
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion 
                + " to "
                + newVersion + ", which will destroy all old data");
        
        drop_all_tables(db);
        onCreate(db);
    }
}
