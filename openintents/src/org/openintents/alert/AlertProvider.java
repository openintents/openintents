package org.openintents.alert;

/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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


import java.util.HashMap;

import org.openintents.provider.Alert;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DatabaseContentProvider;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AlertProvider extends DatabaseContentProvider {

		
	private static final String DATABASE_NAME="alerts.db";
	private static final int DATABASE_VERSION=2;

	private static final String TABLE_ALERTS="alerts";

	private static final String TAG="org.openintents.alert.AlertProvider";

	private static final int ALERT_GENERIC=100;
	private static final int ALERT_GENERIC_ID=101;
	private static final int ALERT_LOCATION=102;
	private static final int ALERT_LOCATION_ID=103;
	private static final int ALERT_COMBINED=104;
	private static final int ALERT_COMBINED_ID=105;
	private static final int ALERT_SENSOR=106;
	private static final int ALERT_SENSOR_ID=106;
	private static final int ALERT_DATE_TIME=107;
	private static final int ALERT_DATE_TIME_ID=108;

	private static final UriMatcher URL_MATCHER;
	private static final HashMap<String,String> GENERIC_PROJECTION_MAP;


	private static class AlertDBHelper extends SQLiteOpenHelper{

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG,"Creating table "+TABLE_ALERTS);
			db.execSQL("CREATE TABLE "+TABLE_ALERTS+" ("+
				Alert.Generic._ID +" INTEGER PRIMARY KEY,"+
				Alert.Generic._COUNT+" INTEGER,"+
				Alert.Generic.CONDITION1+" STRING,"+
				Alert.Generic.CONDITION2+" STRING,"+
				Alert.Generic.TYPE+" STRING,"+
				Alert.Generic.RULE+" STRING,"+
				Alert.Generic.NATURE+" STRING,"+
				Alert.Generic.INTENT+" STRING,"+
				Alert.Generic.INTENT_CATEGORY+" STRING,"+
				Alert.Generic.INTENT_URI+" STRING,"+
				Alert.Generic.INTENT_MIME_TYPE+" STRING,"+
				Alert.Generic.ACTIVE+" INTEGER,"+
				Alert.Generic.ACTIVATE_ON_BOOT+" INTEGER"+
				");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			Log.w(TAG,"upgrade not supported");
			//Log.v(TAG, "");
			
			
		}
					
	}//class helper

	
	public AlertProvider() {
		super(DATABASE_NAME, DATABASE_VERSION);
	}
	
	@Override
	protected void upgradeDatabase(int oldVersion, int newVersion) {
		AlertDBHelper dbHelper = new AlertDBHelper();
		dbHelper.onUpgrade(getDatabase(), oldVersion, newVersion);

	}
	
	@Override
	protected void bootstrapDatabase() {
		super.bootstrapDatabase();
		AlertDBHelper dbHelper = new AlertDBHelper();
		dbHelper.onCreate(getDatabase());
	}
	

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void test(){
		Log.d(TAG,"TEST CALL ACCEPTED");
	}

	public static void test(Uri u){
		int match=URL_MATCHER.match(u);
		Log.d(TAG,"TEST CALL ACCEPTED");
		Log.d(TAG,"uri>>"+u+"<< matched >>"+match+"<<");
	}



	/*
	 * @author Zero
	 * @version 1.0
	 * @argument uri ContentURI NOT NULL
	 * @argument values ContentValues NOT NULL
	 * @return uri of the new item.
	 * 
	 */
	@Override
	protected Uri insertInternal(Uri uri, ContentValues values) {
		int match=URL_MATCHER.match(uri);
		Log.d(this.TAG,"INSERT,URI MATCHER RETURNED >>"+match+"<<");
		long rowID=0;
		//if nature is not given, it's user.
		if (!values.containsKey(Alert.Generic.NATURE))
		{
			values.put(Alert.Generic.NATURE,Alert.NATURE_USER);
		}

		switch (match){
			case ALERT_GENERIC:
				
				rowID=getDatabase().insert(TABLE_ALERTS, "", values);			
				if (rowID > 0) {
					Uri nUri = ContentUris.withAppendedId(Alert.Generic.CONTENT_URI,rowID);
					getContext().getContentResolver().notifyChange(nUri, null);
					return nUri;
				}
				throw new SQLException("Failed to insert row into " + uri);		

				
			case ALERT_LOCATION:

				if (!values.containsKey(Alert.Location.TYPE)){
					values.put(Alert.Location.TYPE,Alert.TYPE_LOCATION);
					
				}

				rowID=getDatabase().insert(TABLE_ALERTS, "", values);			
				if (rowID > 0) {
					Uri nUri = ContentUris.withAppendedId(Alert.Location.CONTENT_URI,rowID);
					getContext().getContentResolver().notifyChange(nUri, null);
					return nUri;
				}
				throw new SQLException("Failed to insert row into " + uri);		


		}

		return null;
	}

	@Override	
	protected Cursor queryInternal(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		int match=URL_MATCHER.match(uri);
		Log.d(this.TAG,"query,URI is >>"+uri+"<<");
		Log.d(this.TAG,"query,URI MATCHER RETURNED >>"+match+"<<");
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy=null;

		//actually all alerts share one id space. 
		//so we just have to match any url set sort order
		//then query
		boolean didMatch=false;
		long rowID=0;
		switch (match){

			case ALERT_GENERIC:

				if (TextUtils.isEmpty(sortOrder)) {
					orderBy = Alert.Generic.DEFAULT_SORT_ORDER;
				} else {
					orderBy = sortOrder;
				}
				break;
			case ALERT_LOCATION:
				if (TextUtils.isEmpty(sortOrder)) {
					orderBy = Alert.Generic.DEFAULT_SORT_ORDER;
				} else {
					orderBy = sortOrder;
				}
				break;
			case ALERT_COMBINED:
				if (TextUtils.isEmpty(sortOrder)) {
					orderBy = Alert.Generic.DEFAULT_SORT_ORDER;
				} else {
					orderBy = sortOrder;
				}
				break;
			case ALERT_GENERIC_ID:
				qb.appendWhere("_id=" + uri.getLastPathSegment());
				if (TextUtils.isEmpty(sortOrder)) {
					orderBy = Alert.Location.DEFAULT_SORT_ORDER;
				} else {
					orderBy = sortOrder;
				}        	
				break;
			case ALERT_LOCATION_ID:
				qb.appendWhere("_id=" + uri.getLastPathSegment());
				if (TextUtils.isEmpty(sortOrder)) {
					orderBy = Alert.Location.DEFAULT_SORT_ORDER;
				} else {
					orderBy = sortOrder;
				}        	
				break;
			case ALERT_COMBINED_ID:
				qb.appendWhere("_id=" + uri.getLastPathSegment());
				if (TextUtils.isEmpty(sortOrder)) {
					orderBy = Alert.Location.DEFAULT_SORT_ORDER;
				} else {
					orderBy = sortOrder;
				}        	
				break;
			default:
				throw new IllegalArgumentException("Unknown URL " + uri);
		}

		qb.setTables(TABLE_ALERTS);
							
		qb.setProjectionMap(GENERIC_PROJECTION_MAP);            
        Cursor c = qb.query(getDatabase(), projection, selection, selectionArgs, null,null, orderBy);
        Log.v(TAG, "query result for " + selection + " " + (selectionArgs != null  && selectionArgs.length > 0 ? selectionArgs[0]: selectionArgs) + ": " + c.count());
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	
	protected int updateInternal(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int result=0;
		String alertID="";
		int match=URL_MATCHER.match(uri);
		Log.d(this.TAG,"UPDATE,URI MATCHER RETURNED >>"+match+"<<");
		String rowID="";


		switch (match){

			case ALERT_GENERIC:
				result= getDatabase().update(TABLE_ALERTS, values, selection,selectionArgs);
				//getContext().getContentResolver().notifyChange(nUri, null);
				getContext().getContentResolver().notifyChange(uri, null);
				break;
			case ALERT_GENERIC_ID:
				alertID=uri.getPathSegments().get(1);
				result= getDatabase()
						.update(TABLE_ALERTS,
								values,
								"_id="+alertID
								+(!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""),
								selectionArgs);
					
				getContext().getContentResolver().notifyChange(uri, null);
				break;
			case ALERT_LOCATION:
				result= getDatabase().update(TABLE_ALERTS, values, selection,selectionArgs);
				//getContext().getContentResolver().notifyChange(nUri, null);
				getContext().getContentResolver().notifyChange(uri, null);
				break;
			case ALERT_LOCATION_ID:
				alertID=uri.getPathSegments().get(1);
				result= getDatabase()
						.update(TABLE_ALERTS,
								values,
								"_id="+alertID
								+(!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""),
								selectionArgs);
					
				getContext().getContentResolver().notifyChange(uri, null);
				break;


		}
		return result;
	}


	@Override
	protected int deleteInternal(Uri uri, String selection, String[] selectionArgs) {
		int res=0;
		String alertID="";
		int match=URL_MATCHER.match(uri);
		Log.d(this.TAG,"INSERT,URI MATCHER RETURNED >>"+match+"<<");
		long rowID=0;
		switch (match){

			case ALERT_GENERIC:
				res =  getDatabase().delete(
					TABLE_ALERTS,
					selection,
					selectionArgs
					);		
				break;
			case ALERT_GENERIC_ID:
				alertID=uri.getPathSegments().get(1);
				res =  getDatabase().delete(
					TABLE_ALERTS,
					"_id="+alertID
					+(!TextUtils.isEmpty(selection) ? " AND (" + selection
					+ ')' : ""),
					selectionArgs);
				break;
			case ALERT_LOCATION:
				res =  getDatabase().delete(
					TABLE_ALERTS,
					selection,
					selectionArgs
					);		
				break;
			case ALERT_LOCATION_ID:
				alertID=uri.getPathSegments().get(1);
				res =  getDatabase().delete(
					TABLE_ALERTS,
					"_id="+alertID
					+(!TextUtils.isEmpty(selection) ? " AND (" + selection
					+ ')' : ""),
					selectionArgs);
				break;

		}
		getContext().getContentResolver().notifyChange(uri, null);
		return res;
	}

	@Override
	public boolean isSyncable(){return false;}


	static{
	
		URL_MATCHER=new UriMatcher(UriMatcher.NO_MATCH);
		
		URL_MATCHER.addURI("org.openintents.alert","generic/",ALERT_GENERIC);
		URL_MATCHER.addURI("org.openintents.alert","generic/#",ALERT_GENERIC_ID);
		URL_MATCHER.addURI("org.openintents.alert","location",ALERT_LOCATION);
		URL_MATCHER.addURI("org.openintents.alert","location/#",ALERT_LOCATION_ID);
		URL_MATCHER.addURI("org.openintents.alert","combined",ALERT_COMBINED);
		URL_MATCHER.addURI("org.openintents.alert","combined/#",ALERT_COMBINED_ID);
		URL_MATCHER.addURI("org.openintents.alert","",6000);
		URL_MATCHER.addURI("org.openintents.alert","/",6001);
		
		GENERIC_PROJECTION_MAP=new HashMap<String,String>();
		GENERIC_PROJECTION_MAP.put(Alert.Generic._ID,Alert.Generic._ID);
		GENERIC_PROJECTION_MAP.put(Alert.Generic._COUNT,Alert.Generic._COUNT);
		GENERIC_PROJECTION_MAP.put(Alert.Generic.TYPE,Alert.Generic.TYPE);
		GENERIC_PROJECTION_MAP.put(Alert.Generic.CONDITION1,Alert.Generic.CONDITION1);
		GENERIC_PROJECTION_MAP.put(Alert.Generic.CONDITION2,Alert.Generic.CONDITION2);
		GENERIC_PROJECTION_MAP.put(Alert.Generic.NATURE,Alert.Generic.NATURE);
		GENERIC_PROJECTION_MAP.put(Alert.Generic.ACTIVE,Alert.Generic.ACTIVE);
		GENERIC_PROJECTION_MAP.put(Alert.Generic.ACTIVATE_ON_BOOT,Alert.Generic.ACTIVATE_ON_BOOT);
		GENERIC_PROJECTION_MAP.put(Alert.Generic.RULE,Alert.Generic.RULE);
		GENERIC_PROJECTION_MAP.put(Alert.Generic.INTENT,Alert.Generic.INTENT);
		GENERIC_PROJECTION_MAP.put(Alert.Generic.INTENT_CATEGORY,Alert.Generic.INTENT_CATEGORY);
		GENERIC_PROJECTION_MAP.put(Alert.Generic.INTENT_URI,Alert.Generic.INTENT_URI);
		GENERIC_PROJECTION_MAP.put(Alert.Generic.INTENT_MIME_TYPE,Alert.Generic.INTENT_MIME_TYPE);

	}

}/*eoc*/