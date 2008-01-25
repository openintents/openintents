package org.openintents.news.rss;


import java.util.HashMap;

import org.openintents.provider.RSSFeed;
import org.openintents.provider.Tag.Tags;



import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.ContentURI;
import android.content.ContentProviderDatabaseHelper;
import android.content.ContentURIParser;
import android.content.QueryBuilder;
import android.content.Resources;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

public class RSSFeedProvider extends ContentProvider {

	private SQLiteDatabase mDB;
	private static final String DATABASE_NAME="rssfeeds.db";
	private static final int DATABASE_VERSION=1;
	private static final String TAG="RSSFeedProvider";
	
	private static final int FEEDS=1001;
	private static final int FEED_ID=1002;
	private static final int FEED_CONTENTS=1003;
	private static final int FEED_CONTENT_ID=1004;
	private static final ContentURIParser URL_MATCHER;
	
	public static final HashMap<String,String> FEED_PROJECTION_MAP;
	public static final HashMap<String,String> CONTENT_PROJECTION_MAP;
	
	
	private static class RSSFeedDBHelper extends ContentProviderDatabaseHelper{

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG,"Creating table rssfeeds");
			
			db.execSQL("CREATE TABLE rssfeeds("+
					RSSFeed.RSSFeeds._ID +" INTEGER PRIMARY KEY,"+
					RSSFeed.RSSFeeds._COUNT+" INTEGER,"+
					//RSSFeed.RSSFeeds.CHANNEL_ID+" "+RSSFeed.RSSFeeds.CHANNEL_ID_TYPE+","+
					RSSFeed.RSSFeeds.CHANNEL_LINK+" "+RSSFeed.RSSFeeds.CHANNEL_LINK_TYPE+","+
					RSSFeed.RSSFeeds.CHANNEL_NAME+" "+RSSFeed.RSSFeeds.CHANNEL_NAME_TYPE+","+
					RSSFeed.RSSFeeds.CHANNEL_DESC+" "+RSSFeed.RSSFeeds.CHANNEL_DESC_TYPE+","+
					RSSFeed.RSSFeeds.CHANNEL_LANG+" "+RSSFeed.RSSFeeds.CHANNEL_LANG_TYPE+","+
					RSSFeed.RSSFeeds.CHANNEL_COPYRIGHT+" "+RSSFeed.RSSFeeds.CHANNEL_COPYRIGHT_TYPE+","+
					RSSFeed.RSSFeeds.CHANNEL_IMAGE_URI+" "+RSSFeed.RSSFeeds.CHANNEL_IMAGE_URI_TYPE+","+
					RSSFeed.RSSFeeds.HISTORY_LENGTH+" "+RSSFeed.RSSFeeds.HISTORY_LENGTH_TYPE+","+
					RSSFeed.RSSFeeds.UPDATE_CYCLE+" "+RSSFeed.RSSFeeds.UPDATE_CYCLE_TYPE+","+
					RSSFeed.RSSFeeds.LAST_UPDATE+" "+RSSFeed.RSSFeeds.LAST_UPDATE_TYPE+
					");");
			
			Log.d(TAG,"Creating table rssfeedcontents");
			
			db.execSQL("CREATE TABLE rssfeedcontents("+
					RSSFeed.RSSFeedContents._ID +"INTEGER PRIMARY KEY,"+
					RSSFeed.RSSFeedContents._COUNT+" INTEGER,"+
					RSSFeed.RSSFeedContents.CHANNEL_ID+" "+RSSFeed.RSSFeedContents.CHANNEL_ID_TYPE+","+
					RSSFeed.RSSFeedContents.ITEM_GUID+" "+RSSFeed.RSSFeedContents.ITEM_GUID_TYPE+","+
					RSSFeed.RSSFeedContents.ITEM_TITLE+" "+RSSFeed.RSSFeedContents.ITEM_TITLE_TYPE+","+
					RSSFeed.RSSFeedContents.ITEM_AUTHOR+" "+RSSFeed.RSSFeedContents.ITEM_AUTHOR_TYPE+","+
					RSSFeed.RSSFeedContents.ITEM_LINK+" "+RSSFeed.RSSFeedContents.ITEM_LINK_TYPE+","+
					RSSFeed.RSSFeedContents.ITEM_DESCRIPTION+" "+RSSFeed.RSSFeedContents.ITEM_DESCRIPTION_TYPE+
					");"
					);
			
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG,"upgrade not supported");
			//Log.v(TAG, "");
			
			
		}
		
		
		
	}
	
	
	
	@Override
	public int delete(ContentURI uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(ContentURI uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	/*
	 * @author Zero
	 * @version 1.0
	 * @argument uri ContentURI NOT NULL
	 * @argument values ContentValues NOT NULL
	 * @return uri of the new item.
	 * 
	 */
	public ContentURI insert(ContentURI uri, ContentValues values) {
		//if there's nowhere to insert, we fail. if  there's no data to insert, we fail.
		if (uri==null||values==null){
			throw new IllegalArgumentException("uri and values must be specified");	
		}
		
		
		int match=URL_MATCHER.match(uri);
		long rowID=0;
		switch (match){
		
		case FEEDS:
			//use insert as intended by SQL standard, avoid "nullcolumnhack" at all costs.
			if (!values.containsKey(RSSFeed.RSSFeedContents.CHANNEL_ID)){
				throw new IllegalArgumentException("RSSFeed.RSSFeedContent.CHANNEL_ID must be"+
				"specified in values. Unbound Items are not allowed.");
				
			}
			rowID=mDB.insert("rssfeeds", "", values);			
			if (rowID > 0) {
				ContentURI nUri = RSSFeed.RSSFeeds.CONTENT_URI.addId(rowID);
				getContext().getContentResolver().notifyChange(nUri, null);
				return nUri;
			}
			throw new SQLException("Failed to insert row into " + uri);						
			
			
		case FEED_CONTENTS:
			//TODO: Update rssfeeds table's "LastUpdated" field to keep track of changes.
			rowID=mDB.insert("rssfeedcontents", "", values);			
			if (rowID > 0) {
				ContentURI nUri = RSSFeed.RSSFeedContents.CONTENT_URI.addId(rowID);
				getContext().getContentResolver().notifyChange(nUri, null);
				return nUri;
			}
			throw new SQLException("Failed to insert row into " + uri);						
			
			

		default: 
			throw new IllegalArgumentException("Unknown URL " + uri);	
		}
		
		
	}

	@Override
	public boolean onCreate() {		 
		RSSFeedDBHelper dbHelper=new RSSFeedDBHelper();
		mDB=dbHelper.openDatabase(getContext(),DATABASE_NAME,null,DATABASE_VERSION);
		
		return mDB!=null;
	}

	@Override
	public Cursor query(ContentURI uri, String[] projection, String selection, String[] selectionArgs, String groupBy, String having, String sortOrder) {
		// TODO Auto-generated method stub
        QueryBuilder qb = new QueryBuilder();
        String orderBy=null;
        switch (URL_MATCHER.match(uri)) {
        case FEEDS:
            qb.setTables("rssfeeds");
            qb.setProjectionMap(FEED_PROJECTION_MAP);            
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = RSSFeed.RSSFeeds.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }
            break;
        case FEED_ID:
            qb.setTables("rssfeeds");
            //qb.setProjectionMap(FEED_PROJECTION_MAP);
            qb.appendWhere("_id=" + uri.getPathLeafId());
            
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = RSSFeed.RSSFeeds.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }
            break;            
        case FEED_CONTENTS:
        	qb.setTables("rssfeedcontents");
            qb.setProjectionMap(CONTENT_PROJECTION_MAP);
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = RSSFeed.RSSFeeds.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }            
            break;
        case FEED_CONTENT_ID:
        	qb.setTables("rssfeedcontents");
        	qb.appendWhere("_id=" + uri.getPathLeafId());
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = RSSFeed.RSSFeeds.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }        	
        	break;

            
        default:
            throw new IllegalArgumentException("Unknown URL " + uri);
        }

        
        


        Cursor c = qb.query(mDB, projection, selection, selectionArgs, groupBy,
                having, orderBy);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
	}

	@Override
	public int update(ContentURI uri, ContentValues values, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		int result=0;
		if (URL_MATCHER.match(uri)==FEEDS){
	
			result= mDB.update("rssfeeds", values, selection,selectionArgs);
			//getContext().getContentResolver().notifyChange(nUri, null);
			getContext().getContentResolver().notifyChange(uri, null);
		}else if (URL_MATCHER.match(uri)==FEED_ID){
			long feedID=uri.getPathLeafId();
			result= mDB
					.update("rssfeeds",
							values,
							"_id="+feedID
							+(!TextUtils.isEmpty(selection) ? " AND (" + selection
                            + ')' : ""),
							selectionArgs);
				
			
		}else{
			
			throw new IllegalArgumentException("Unsupported URL " + uri);
			
		}
	
		return result;
	}
	
	@Override
	public boolean isSyncable(){return false;}

	static{
	
		URL_MATCHER=new ContentURIParser(ContentURIParser.NO_MATCH);
		URL_MATCHER.addURI("org.openintents.news","/rss",FEEDS);
		URL_MATCHER.addURI("org.openintents.news","/rss/#",FEED_ID);
		URL_MATCHER.addURI("org.openintents.news","/rssfeedcontents",FEED_CONTENTS);
		URL_MATCHER.addURI("org.openintents.news","/rssfeedcontents/#",FEED_CONTENT_ID);
		
		FEED_PROJECTION_MAP=new HashMap<String,String>();
		FEED_PROJECTION_MAP.put(RSSFeed.RSSFeeds._ID, RSSFeed.RSSFeeds._ID);
		FEED_PROJECTION_MAP.put(RSSFeed.RSSFeeds._COUNT,RSSFeed.RSSFeeds._COUNT);
		//FEED_PROJECTION_MAP.put(RSSFeed.RSSFeeds.CHANNEL_ID,RSSFeed.RSSFeeds.CHANNEL_ID);
		FEED_PROJECTION_MAP.put(RSSFeed.RSSFeeds.CHANNEL_NAME,RSSFeed.RSSFeeds.CHANNEL_NAME);
		FEED_PROJECTION_MAP.put(RSSFeed.RSSFeeds.CHANNEL_LINK,RSSFeed.RSSFeeds.CHANNEL_LINK);
		FEED_PROJECTION_MAP.put(RSSFeed.RSSFeeds.CHANNEL_DESC,RSSFeed.RSSFeeds.CHANNEL_DESC_TYPE);
		FEED_PROJECTION_MAP.put(RSSFeed.RSSFeeds.CHANNEL_LANG,RSSFeed.RSSFeeds.CHANNEL_LANG_TYPE);
		FEED_PROJECTION_MAP.put(RSSFeed.RSSFeeds.CHANNEL_COPYRIGHT,RSSFeed.RSSFeeds.CHANNEL_COPYRIGHT_TYPE);
		FEED_PROJECTION_MAP.put(RSSFeed.RSSFeeds.CHANNEL_IMAGE_URI,RSSFeed.RSSFeeds.CHANNEL_IMAGE_URI_TYPE);
		FEED_PROJECTION_MAP.put(RSSFeed.RSSFeeds.HISTORY_LENGTH,RSSFeed.RSSFeeds.HISTORY_LENGTH);
		FEED_PROJECTION_MAP.put(RSSFeed.RSSFeeds.UPDATE_CYCLE,RSSFeed.RSSFeeds.UPDATE_CYCLE);
		FEED_PROJECTION_MAP.put(RSSFeed.RSSFeeds.LAST_UPDATE,RSSFeed.RSSFeeds.LAST_UPDATE);
		
		CONTENT_PROJECTION_MAP=new HashMap<String,String>();
		CONTENT_PROJECTION_MAP.put(RSSFeed.RSSFeedContents._ID,RSSFeed.RSSFeedContents._ID);
		CONTENT_PROJECTION_MAP.put(RSSFeed.RSSFeedContents._COUNT,RSSFeed.RSSFeedContents._COUNT);
		CONTENT_PROJECTION_MAP.put(RSSFeed.RSSFeedContents.CHANNEL_ID,RSSFeed.RSSFeedContents.CHANNEL_ID_TYPE);
		CONTENT_PROJECTION_MAP.put(RSSFeed.RSSFeedContents.ITEM_GUID,RSSFeed.RSSFeedContents.ITEM_GUID_TYPE);
		CONTENT_PROJECTION_MAP.put(RSSFeed.RSSFeedContents.ITEM_TITLE,RSSFeed.RSSFeedContents.ITEM_TITLE_TYPE);
		CONTENT_PROJECTION_MAP.put(RSSFeed.RSSFeedContents.ITEM_AUTHOR,RSSFeed.RSSFeedContents.ITEM_AUTHOR_TYPE);
		CONTENT_PROJECTION_MAP.put(RSSFeed.RSSFeedContents.ITEM_LINK,RSSFeed.RSSFeedContents.ITEM_LINK_TYPE);
		CONTENT_PROJECTION_MAP.put(RSSFeed.RSSFeedContents.ITEM_DESCRIPTION,RSSFeed.RSSFeedContents.ITEM_DESCRIPTION_TYPE);
		
	
	}
	
	
}/*eoc*/
