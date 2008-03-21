package org.openintents.alert;


import java.util.HashMap;

import org.openintents.provider.Alert;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class AlertProvider extends ContentProvider {

	private SQLiteDatabase mDB;
	private static final String DATABASE_NAME="alerts.db";
	private static final int DATABASE_VERSION=1;

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

	private static final UriMatcher URL_MATCHER;



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

	@Override
	public boolean onCreate() {		 
		AlertDBHelper dbHelper=new AlertDBHelper();
		mDB=dbHelper.openDatabase(getContext(),DATABASE_NAME,null,DATABASE_VERSION);
		
		return mDB!=null;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * @author Zero
	 * @version 1.0
	 * @argument uri ContentURI NOT NULL
	 * @argument values ContentValues NOT NULL
	 * @return uri of the new item.
	 * 
	 */
	public Uri insert(Uri uri, ContentValues values) {
		int match=URL_MATCHER.match(uri);
		Log.d(this.TAG,"INSERT,URI MATCHER RETURNED >>"+match+"<<");
		long rowID=0;
		switch (match){
		}

		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		int match=URL_MATCHER.match(uri);
		Log.d(this.TAG,"INSERT,URI MATCHER RETURNED >>"+match+"<<");
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        String orderBy=null;

		long rowID=0;
		switch (match){
		}
        Cursor c = qb.query(mDB, projection, selection, selectionArgs, null,null, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int result=0;

		int match=URL_MATCHER.match(uri);
		Log.d(this.TAG,"UPDATE,URI MATCHER RETURNED >>"+match+"<<");
		String rowID="";


		switch (match){

		}
		return result;
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int match=URL_MATCHER.match(uri);
		Log.d(this.TAG,"INSERT,URI MATCHER RETURNED >>"+match+"<<");
		long rowID=0;
		switch (match){
		}
		return 0;
	}

	@Override
	public boolean isSyncable(){return false;}


	static{
	
		URL_MATCHER=new UriMatcher(UriMatcher.NO_MATCH);
		
		URL_MATCHER.addURI("org.openintents.alert","",ALERT_GENERIC);
		URL_MATCHER.addURI("org.openintents.alert","/#",ALERT_GENERIC_ID);
		URL_MATCHER.addURI("org.openintents.alert","location",ALERT_LOCATION);
		URL_MATCHER.addURI("org.openintents.alert","location/#",ALERT_LOCATION_ID);
		URL_MATCHER.addURI("org.openintents.alert","combined",ALERT_COMBINED);
		URL_MATCHER.addURI("org.openintents.alert","combined/#",ALERT_COMBINED_ID);
		
		
	}

}/*eoc*/