package org.openintents.mail;


import java.util.HashMap;

import org.openintents.provider.Mail;

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

public class MailProvider extends ContentProvider {

	private SQLiteDatabase mDB;
	private static final String DATABASE_NAME="mail.db";
	private static final int DATABASE_VERSION=1;

	private static final String TABLE_ACCOUNTS="accounts";
	private static final String TABLE_MESSAGES="messages";
	private static final String TABLE_FOLDERS="folders";
	private static final String TABLE_SIGNATURES="signatures";


	private static final String TAG="MailProvider";
	

	private static final int MAIL_ACCOUNTS=101;
	private static final int MAIL_ACCOUNT_ID=102;
	private static final int MAIL_MESSAGES=103;
	private static final int MAIL_MESSAGE_ID=104;
	private static final int MAIL_FOLDERS=105;
	private static final int MAIL_FOLDER_ID=106;
	private static final int MAIL_SIGNATURES=107;
	private static final int MAIL_SIGNATURE_ID=108;

	private static final UriMatcher URL_MATCHER;
	

	
	
	private static class MailDBHelper extends SQLiteOpenHelper{

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG,"Creating table "+TABLE_ACCOUNTS);
			
			db.execSQL("CREATE TABLE "+TABLE_ACCOUNTS+" ("+
					Mail.Account._ID +" INTEGER PRIMARY KEY,"+
					Mail.Account._COUNT+" INTEGER,"+
					Mail.Account.TYPE+" STRING,"+
					Mail.Account.NAME+" STRING,"+
					Mail.Account.SEND_PROTOCOL+" STRING,"+
					Mail.Account.RECV_PROTOCOL+" STRING,"+
					Mail.Account.SEND_URI+" STRING,"+
					Mail.Account.RECV_URI+" STRING,"+
					Mail.Account.REPLY_TO+" STRING,"+
					Mail.Account.MAIL_ADDRESS+" STRING,"+
					Mail.Account.USERNAME+" STRING,"+
					Mail.Account.PASSWORD+" STRING,"+
					Mail.Account.DEFAULT_SIGNATURE+" STRING,"+
					Mail.Account.ACCOUNT_REFERENCE+" STRING"+
					");");
			
			Log.d(TAG,"Creating table"+TABLE_MESSAGES);
			
			db.execSQL("CREATE TABLE "+TABLE_MESSAGES+"("+
					Mail.Message._ID+" INTEGER PRIMARY KEY,"+
					Mail.Message._COUNT+" INTEGER,"+
					Mail.Message.ACCOUNT_ID+" INTEGER,"+
					Mail.Message.ACCOUNT_NAME+" STRING,"+
					Mail.Message.SUBJECT+" STRING,"+
					Mail.Message.SEND_DATE+" STRING,"+
					Mail.Message.RECV_DATE+" STRING,"+
					Mail.Message.CONTENT_TYPE+" STRING,"+
					Mail.Message.BODY+" STRING,"+
					Mail.Message.TO+" STRING,"+
					Mail.Message.CC+" STRING,"+
					Mail.Message.BCC+" STRING,"+
					Mail.Message.FROM+" STRING,"+
					Mail.Message.SENDER+" STRING,"+
					Mail.Message.REPLY_TO+" STRING,"+
					Mail.Message.MESSAGE_ID+" STRING,"+
					Mail.Message.IN_REPLY_TO+" STRING,"+
					Mail.Message.TAG_NAMES+" STRING,"+
					Mail.Message.HEADER+" STRING"+
					");"
					);
			
			db.execSQL("CREATE TABLE "+TABLE_FOLDERS+"("+
				Mail.Folders._ID+" INTEGER PRIMARY KEY,"+
				Mail.Folders._COUNT+" INTEGER"+
				");"
			);

			db.execSQL("CREATE TABLE "+TABLE_SIGNATURES+"+ ("+
				Mail.Signatures._ID+" INTEGER PRIMARY KEY,"+
				Mail.Signatures._COUNT+" INTEGER,"+
				Mail.Signatures.NAME+" STRING,"+
				Mail.Signatures.SIG+" STRING"+
				");"
			);



			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

			Log.w(TAG,"upgrade not supported");
			//Log.v(TAG, "");
			
			
		}
		
		
		
	}//class helper

	@Override
	public boolean onCreate() {		 
		MailDBHelper dbHelper=new MailDBHelper();
		mDB=dbHelper.openDatabase(getContext(),DATABASE_NAME,null,DATABASE_VERSION);
		
		return mDB!=null;
	}


	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int match=URL_MATCHER.match(uri);
		Log.d(this.TAG,"INSERT,URI MATCHER RETURNED >>"+match+"<<");
		long rowID=0;
		switch (match){
			case MAIL_ACCOUNTS:
				break;
			case MAIL_ACCOUNT_ID:
				break;
			case MAIL_MESSAGES:
				break;
			case MAIL_MESSAGE_ID:
				break;
			case MAIL_FOLDERS:
				break;
			case MAIL_FOLDER_ID:
				break;
			case MAIL_SIGNATURES:
				break;
			case MAIL_SIGNATURE_ID:
				break;
		}


		//TODO
		return 0;
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
			case MAIL_ACCOUNTS:
				break;
			case MAIL_ACCOUNT_ID:
				break;
			case MAIL_MESSAGES:
				break;
			case MAIL_MESSAGE_ID:
				break;
			case MAIL_FOLDERS:
				break;
			case MAIL_FOLDER_ID:
				break;
			case MAIL_SIGNATURES:
				break;
			case MAIL_SIGNATURE_ID:
				break;
		}
		return null;
	}



	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		int match=URL_MATCHER.match(uri);
		Log.d(this.TAG,"INSERT,URI MATCHER RETURNED >>"+match+"<<");
		long rowID=0;
		switch (match){
			case MAIL_ACCOUNTS:
				break;
			case MAIL_ACCOUNT_ID:
				break;
			case MAIL_MESSAGES:
				break;
			case MAIL_MESSAGE_ID:
				break;
			case MAIL_FOLDERS:
				break;
			case MAIL_FOLDER_ID:
				break;
			case MAIL_SIGNATURES:
				break;
			case MAIL_SIGNATURE_ID:
				break;
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int match=URL_MATCHER.match(uri);
		Log.d(this.TAG,"INSERT,URI MATCHER RETURNED >>"+match+"<<");
		long rowID=0;
		switch (match){
			case MAIL_ACCOUNTS:
				break;
			case MAIL_ACCOUNT_ID:
				break;
			case MAIL_MESSAGES:
				break;
			case MAIL_MESSAGE_ID:
				break;
			case MAIL_FOLDERS:
				break;
			case MAIL_FOLDER_ID:
				break;
			case MAIL_SIGNATURES:
				break;
			case MAIL_SIGNATURE_ID:
				break;
		}
			return 0;
		}

	@Override
	public boolean isSyncable(){return false;}



	static{
	
		URL_MATCHER=new UriMatcher(UriMatcher.NO_MATCH);
		
		URL_MATCHER.addURI("org.openintents.mail","accounts",MAIL_ACCOUNTS);
		URL_MATCHER.addURI("org.openintents.mail","accounts/#",MAIL_ACCOUNT_ID);
		URL_MATCHER.addURI("org.openintents.mail","messages",MAIL_MESSAGES);
		URL_MATCHER.addURI("org.openintents.mail","messages/#",MAIL_MESSAGE_ID);
		URL_MATCHER.addURI("org.openintents.mail","signatures",MAIL_SIGNATURES);
		URL_MATCHER.addURI("org.openintents.mail","signatures/#",MAIL_SIGNATURE_ID);
		URL_MATCHER.addURI("org.openintents.mail","folders",MAIL_FOLDERS);
		URL_MATCHER.addURI("org.openintents.mail","folders/#",MAIL_FOLDER_ID);

		
	}


}/*eoc*/