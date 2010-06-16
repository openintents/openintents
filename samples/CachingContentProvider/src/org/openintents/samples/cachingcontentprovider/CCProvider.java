package org.openintents.samples.cachingcontentprovider;
/*
<!-- 
 * Copyright (C) 2010 OpenIntents UG
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
 --> 
 */

import java.util.HashMap;


import org.openintents.samples.cachingcontentprovider.Customers;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
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
 * Provides access to a database of Customers. Each note has a title, the note
 * itself, a creation date and a modified data.
 */
public class CCProvider extends ContentProvider {

    private static final String TAG = "CCProvider";

    private static final String DATABASE_NAME = "ccpexample.db";
    
    /**
     * Database version.
     * <ul>
     * <li>Version 1
     * </ul>
     */
    private static final int DATABASE_VERSION = 3;
    private static final String CUSTOMERS_TABLE_NAME = "customers";

    private static HashMap<String, String> sCustomersProjectionMap;

    private static final int CUSTOMERS = 1;
    private static final int CUSTOMER_ID = 2;

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
            db.execSQL("CREATE TABLE " + CUSTOMERS_TABLE_NAME + " ("
            		//Version 1
                    + Customers._ID + " INTEGER PRIMARY KEY,"
                    + Customers.NAME + " TEXT,"
                    + Customers.EMAIL + " TEXT"
                    + ");");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ".");
            //It's a good idea to do something here in the real world ;-)
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
    	//we start the service that retrieves data from the backend
    	Intent intent=new Intent();
    	intent.setClass(getContext(), DataService.class);
    	getContext().startService(intent);
    	
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case CUSTOMERS:
            qb.setTables(CUSTOMERS_TABLE_NAME);
            qb.setProjectionMap(sCustomersProjectionMap);
            break;

        case CUSTOMER_ID:
            qb.setTables(CUSTOMERS_TABLE_NAME);
            qb.setProjectionMap(sCustomersProjectionMap);
            qb.appendWhere(Customers._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = Customers.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }

        // Get the database and run the query
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        //when run for the first time, this cursor will be empty.
        //it will refresh on it's own after the service started above
        //starts updating our database
        Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

        // Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case CUSTOMERS:
            return Customers.CONTENT_TYPE;

        case CUSTOMER_ID:
            return Customers.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        // Validate the requested uri
        if (sUriMatcher.match(uri) != CUSTOMERS) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        ContentValues values;
        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }

        // Make sure that the fields are all set
        if (values.containsKey(Customers.NAME) == false) {
            Resources r = Resources.getSystem();
            values.put(Customers.NAME, r.getString(android.R.string.untitled));
        }

        if (values.containsKey(Customers.EMAIL) == false) {
            values.put(Customers.EMAIL, "");
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        //the name field will be assigned a null value if the row is empty. see @framework reference
        long rowId = db.insert(CUSTOMERS_TABLE_NAME, Customers.NAME, values);
        if (rowId > 0) {
            Uri rowUri = ContentUris.withAppendedId(Customers.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(rowUri, null);
            //return the uri of the newly created dataset
            return rowUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
		switch (sUriMatcher.match(uri)) {
        case CUSTOMERS:

			count = db.delete(CUSTOMERS_TABLE_NAME, where, whereArgs);
            break;

        case CUSTOMER_ID:
            String noteId = uri.getPathSegments().get(1);
            String whereString = Customers._ID + "=" + noteId
            	+ (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : "");
            

			count = db.delete(CUSTOMERS_TABLE_NAME, whereString, whereArgs);
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
        case CUSTOMERS:
            count = db.update(CUSTOMERS_TABLE_NAME, values, where, whereArgs);
            break;

        case CUSTOMER_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.update(CUSTOMERS_TABLE_NAME, values, Customers._ID + "=" + noteId
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
        sUriMatcher.addURI("org.openintents.ccp", "customers", CUSTOMERS);
        sUriMatcher.addURI("org.openintents.ccp", "customers/#", CUSTOMER_ID);

        sCustomersProjectionMap = new HashMap<String, String>();
        sCustomersProjectionMap.put(Customers._ID, Customers._ID);
        sCustomersProjectionMap.put(Customers.NAME, Customers.NAME);
        sCustomersProjectionMap.put(Customers.EMAIL, Customers.EMAIL);

    }
}
