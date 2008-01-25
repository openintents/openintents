package org.openintents.news;


import java.util.HashMap;

import org.openintents.provider.News;
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

public class NewsProvider extends ContentProvider {

	private SQLiteDatabase mDB;
	private static final String DATABASE_NAME="newsfeeds.db";
	private static final int DATABASE_VERSION=1;
	private static final String TAG="NewsProvider";
	
	private static final int RSSFEEDS=1001;
	private static final int RSSFEED_ID=1002;
	private static final int RSSFEED_CONTENTS=1003;
	private static final int RSSFEED_CONTENT_ID=1004;
	private static final int ATOMFEEDS=2001;
	private static final int ATOMFEED_ID=2002;
	private static final int ATOMFEED_CONTENTS=2003;
	private static final int ATOMFEED_CONTENT_ID=2004;

	private static final ContentURIParser URL_MATCHER;
	
	public static final HashMap<String,String> RSSFEED_PROJECTION_MAP;
	public static final HashMap<String,String> RSSCONTENT_PROJECTION_MAP;
	
	public static final HashMap<String,String> ATOMFEED_PROJECTION_MAP;
	public static final HashMap<String,String> ATOMCONTENT_PROJECTION_MAP;
	
	
	private static class RSSFeedDBHelper extends ContentProviderDatabaseHelper{

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG,"Creating table rssfeeds");
			
			db.execSQL("CREATE TABLE rssfeeds("+
					News.RSSFeeds._ID +" INTEGER PRIMARY KEY,"+
					News.RSSFeeds._COUNT+" INTEGER,"+
					//News.RSSFeeds.CHANNEL_ID+" "+News.RSSFeeds.CHANNEL_ID_TYPE+","+
					News.RSSFeeds.CHANNEL_LINK+" "+News.RSSFeeds.CHANNEL_LINK_TYPE+","+
					News.RSSFeeds.CHANNEL_NAME+" "+News.RSSFeeds.CHANNEL_NAME_TYPE+","+
					News.RSSFeeds.CHANNEL_DESC+" "+News.RSSFeeds.CHANNEL_DESC_TYPE+","+
					News.RSSFeeds.CHANNEL_LANG+" "+News.RSSFeeds.CHANNEL_LANG_TYPE+","+
					News.RSSFeeds.CHANNEL_COPYRIGHT+" "+News.RSSFeeds.CHANNEL_COPYRIGHT_TYPE+","+
					News.RSSFeeds.CHANNEL_IMAGE_URI+" "+News.RSSFeeds.CHANNEL_IMAGE_URI_TYPE+","+
					News.RSSFeeds.HISTORY_LENGTH+" "+News.RSSFeeds.HISTORY_LENGTH_TYPE+","+
					News.RSSFeeds.UPDATE_CYCLE+" "+News.RSSFeeds.UPDATE_CYCLE_TYPE+","+
					News.RSSFeeds.LAST_UPDATE+" "+News.RSSFeeds.LAST_UPDATE_TYPE+
					");");
			
			Log.d(TAG,"Creating table rssfeedcontents");
			
			db.execSQL("CREATE TABLE rssfeedcontents("+
					News.RSSFeedContents._ID +"INTEGER PRIMARY KEY,"+
					News.RSSFeedContents._COUNT+" INTEGER,"+
					News.RSSFeedContents.CHANNEL_ID+" "+News.RSSFeedContents.CHANNEL_ID_TYPE+","+
					News.RSSFeedContents.ITEM_GUID+" "+News.RSSFeedContents.ITEM_GUID_TYPE+","+
					News.RSSFeedContents.ITEM_TITLE+" "+News.RSSFeedContents.ITEM_TITLE_TYPE+","+
					News.RSSFeedContents.ITEM_AUTHOR+" "+News.RSSFeedContents.ITEM_AUTHOR_TYPE+","+
					News.RSSFeedContents.ITEM_LINK+" "+News.RSSFeedContents.ITEM_LINK_TYPE+","+
					News.RSSFeedContents.ITEM_DESCRIPTION+" "+News.RSSFeedContents.ITEM_DESCRIPTION_TYPE+
					");"
					);
			
			db.execSQL("CREATE TABLE atomfeeds("+
				News.AtomFeeds._ID+"INTEGER PRIMARY KEY,"+
				News.AtomFeeds._COUNT+" INTEGER,"+
				News.AtomFeeds.FEED_ID+" INTEGER,"+
				News.AtomFeeds.FEED_UPDATED+" STRING,"+
				News.AtomFeeds.FEED_LAST_CHECKED+" STRING,"+
				News.AtomFeeds.UPDATE_CYCLE+" INTEGER,"+
				News.AtomFeeds.HISTORY_LENGTH+" INTEGER,"+
				News.AtomFeeds.FEED_LINK+" STRING,"+
				News.AtomFeeds.FEED_LINK_SELF+" STRING,"+
				News.AtomFeeds.FEED_LINK_ALTERNATE+" STRING,"+
				News.AtomFeeds.FEED_ICON+" STRING,"+
				News.AtomFeeds.FEED_RIGHTS+" STRING,"+
				");"
			);

			db.execSQL("CREATE TABLE atomfeedcontents ("+
				News.AtomFeedContents._ID+" INTEGER PRIMARY KEY,"+
				News.AtomFeedContents._COUNT+" INTEGER,"+
				News.AtomFeedContents.FEED_ID+" STRING,"+
				News.AtomFeedContents.ENTRY_ID+" STRING,"+
				News.AtomFeedContents.ENTRY_TITLE+" STRING,"+
				News.AtomFeedContents.ENTRY_LINK+" STRING,"+
				News.AtomFeedContents.ENTRY_LINK_ALTERNATE+" STRING,"+
				News.AtomFeedContents.ENTRY_SUMMARY+" STRING,"+
				News.AtomFeedContents.ENTRY_SUMMARY_TYPE+" STRING"+
				");"
			);



			
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
		Log.d(this.TAG,"ENTERING INSERT, uri>>"+uri+"<<");
		//if there's nowhere to insert, we fail. if  there's no data to insert, we fail.
		if (uri==null||values==null){
			throw new IllegalArgumentException("uri and values must be specified");	
		}
		
		
		int match=URL_MATCHER.match(uri);
		Log.d(this.TAG,"INSERT,URI MATCHER RETURNED >>"+match+"<<");
		long rowID=0;
		switch (match){
		
		case RSSFEEDS:
			//use insert as intended by SQL standard, avoid "nullcolumnhack" at all costs.
			if (!values.containsKey(News.RSSFeeds.CHANNEL_NAME)){
				throw new IllegalArgumentException("At least News.RSSFeedContent.CHANNEL_NAME"+
				"  must be specified in values. Unbound Items are not allowed.");
				
			}
			rowID=mDB.insert("rssfeeds", "", values);			
			if (rowID > 0) {
				ContentURI nUri = News.RSSFeeds.CONTENT_URI.addId(rowID);
				getContext().getContentResolver().notifyChange(nUri, null);
				return nUri;
			}
			throw new SQLException("Failed to insert row into " + uri);						
			
			
		case RSSFEED_CONTENTS:
			//TODO: Update rssfeeds table's "LastUpdated" field to keep track of changes.
			rowID=mDB.insert("rssfeedcontents", "", values);			
			if (rowID > 0) {
				ContentURI nUri = News.RSSFeedContents.CONTENT_URI.addId(rowID);
				getContext().getContentResolver().notifyChange(nUri, null);
				return nUri;
			}
			throw new SQLException("Failed to insert row into " + uri);						
			
		case ATOMFEEDS:
			//use insert as intended by SQL standard, avoid "nullcolumnhack" at all costs.
			if (!values.containsKey(News.RSSFeeds.CHANNEL_NAME)){
				throw new IllegalArgumentException("At least News.RSSFeedContent.CHANNEL_NAME"+
				"  must be specified in values. Unbound Items are not allowed.");
				
			}
			rowID=mDB.insert("atomfeeds", "", values);			
			if (rowID > 0) {
				ContentURI nUri = News.AtomFeeds.CONTENT_URI.addId(rowID);
				getContext().getContentResolver().notifyChange(nUri, null);
				return nUri;
			}
			throw new SQLException("Failed to insert row into " + uri);						
			
		case ATOMFEED_CONTENTS:
			//TODO: Update atomfeeds table's "LastChecked" field to keep track of changes.
			rowID=mDB.insert("atomfeedcontents", "", values);			
			if (rowID > 0) {
				ContentURI nUri = News.AtomFeedContents.CONTENT_URI.addId(rowID);
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
        case RSSFEEDS:
            qb.setTables("rssfeeds");
            qb.setProjectionMap(RSSFEED_PROJECTION_MAP);            
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = News.RSSFeeds.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }
            break;
        case RSSFEED_ID:
            qb.setTables("rssfeeds");
            //qb.setProjectionMap(FEED_PROJECTION_MAP);
            qb.appendWhere("_id=" + uri.getPathLeafId());
            
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = News.RSSFeeds.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }
            break;            
        case RSSFEED_CONTENTS:
        	qb.setTables("rssfeedcontents");
            qb.setProjectionMap(RSSCONTENT_PROJECTION_MAP);
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = News.RSSFeeds.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }            
            break;
        case RSSFEED_CONTENT_ID:
        	qb.setTables("rssfeedcontents");
        	qb.appendWhere("_id=" + uri.getPathLeafId());
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = News.RSSFeeds.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }        	
        	break;

        case ATOMFEEDS:
            qb.setTables("atomfeeds");
            qb.setProjectionMap(ATOMFEED_PROJECTION_MAP);            
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = News.AtomFeeds.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }
            break;
        case ATOMFEED_ID:
            qb.setTables("atomfeeds");
            //qb.setProjectionMap(FEED_PROJECTION_MAP);
            qb.appendWhere("_id=" + uri.getPathLeafId());
            
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = News.AtomFeeds.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }
            break;            
        case ATOMFEED_CONTENTS:
        	qb.setTables("atomfeedcontents");
            qb.setProjectionMap(ATOMCONTENT_PROJECTION_MAP);
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = News.AtomFeeds.DEFAULT_SORT_ORDER;
            } else {
                orderBy = sortOrder;
            }            
            break;
        case ATOMFEED_CONTENT_ID:
        	qb.setTables("atomfeedcontents");
        	qb.appendWhere("_id=" + uri.getPathLeafId());
            if (TextUtils.isEmpty(sortOrder)) {
                orderBy = News.AtomFeeds.DEFAULT_SORT_ORDER;
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
		if (URL_MATCHER.match(uri)==RSSFEEDS){
	
			result= mDB.update("rssfeeds", values, selection,selectionArgs);
			//getContext().getContentResolver().notifyChange(nUri, null);
			getContext().getContentResolver().notifyChange(uri, null);
		}else if (URL_MATCHER.match(uri)==RSSFEED_ID){
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
		URL_MATCHER.addURI("org.openintents.news","rss",RSSFEEDS);
		URL_MATCHER.addURI("org.openintents.news","rss/#",RSSFEED_ID);
		URL_MATCHER.addURI("org.openintents.news","rssfeedcontents",RSSFEED_CONTENTS);
		URL_MATCHER.addURI("org.openintents.news","rssfeedcontents/#",RSSFEED_CONTENT_ID);
		URL_MATCHER.addURI("org.openintents.news","atom",ATOMFEEDS);
		URL_MATCHER.addURI("org.openintents.news","atom/#",ATOMFEED_ID);
		URL_MATCHER.addURI("org.openintents.news","atomfeedcontents",ATOMFEED_CONTENTS);
		URL_MATCHER.addURI("org.openintents.news","atomfeedcontents/#",ATOMFEED_CONTENT_ID);
		



		RSSFEED_PROJECTION_MAP=new HashMap<String,String>();
		//RSSFEED_PROJECTION_MAP
		RSSFEED_PROJECTION_MAP.put(News.RSSFeeds._ID, News.RSSFeeds._ID);
		RSSFEED_PROJECTION_MAP.put(News.RSSFeeds._COUNT,News.RSSFeeds._COUNT);
		//FEED_PROJECTION_MAP.put(News.RSSFeeds.CHANNEL_ID,News.RSSFeeds.CHANNEL_ID);
		RSSFEED_PROJECTION_MAP.put(News.RSSFeeds.CHANNEL_NAME,News.RSSFeeds.CHANNEL_NAME);
		RSSFEED_PROJECTION_MAP.put(News.RSSFeeds.CHANNEL_LINK,News.RSSFeeds.CHANNEL_LINK);
		RSSFEED_PROJECTION_MAP.put(News.RSSFeeds.CHANNEL_DESC,News.RSSFeeds.CHANNEL_DESC);
		RSSFEED_PROJECTION_MAP.put(News.RSSFeeds.CHANNEL_LANG,News.RSSFeeds.CHANNEL_LANG);
		RSSFEED_PROJECTION_MAP.put(News.RSSFeeds.CHANNEL_COPYRIGHT,News.RSSFeeds.CHANNEL_COPYRIGHT);
		RSSFEED_PROJECTION_MAP.put(News.RSSFeeds.CHANNEL_IMAGE_URI,News.RSSFeeds.CHANNEL_IMAGE_URI);
		RSSFEED_PROJECTION_MAP.put(News.RSSFeeds.HISTORY_LENGTH,News.RSSFeeds.HISTORY_LENGTH);
		RSSFEED_PROJECTION_MAP.put(News.RSSFeeds.UPDATE_CYCLE,News.RSSFeeds.UPDATE_CYCLE);
		RSSFEED_PROJECTION_MAP.put(News.RSSFeeds.LAST_UPDATE,News.RSSFeeds.LAST_UPDATE);
		
		RSSCONTENT_PROJECTION_MAP=new HashMap<String,String>();
		RSSCONTENT_PROJECTION_MAP.put(News.RSSFeedContents._ID,News.RSSFeedContents._ID);
		RSSCONTENT_PROJECTION_MAP.put(News.RSSFeedContents._COUNT,News.RSSFeedContents._COUNT);
		RSSCONTENT_PROJECTION_MAP.put(News.RSSFeedContents.CHANNEL_ID,News.RSSFeedContents.CHANNEL_ID);
		RSSCONTENT_PROJECTION_MAP.put(News.RSSFeedContents.ITEM_GUID,News.RSSFeedContents.ITEM_GUID);
		RSSCONTENT_PROJECTION_MAP.put(News.RSSFeedContents.ITEM_TITLE,News.RSSFeedContents.ITEM_TITLE);
		RSSCONTENT_PROJECTION_MAP.put(News.RSSFeedContents.ITEM_AUTHOR,News.RSSFeedContents.ITEM_AUTHOR);
		RSSCONTENT_PROJECTION_MAP.put(News.RSSFeedContents.ITEM_LINK,News.RSSFeedContents.ITEM_LINK);
		RSSCONTENT_PROJECTION_MAP.put(News.RSSFeedContents.ITEM_DESCRIPTION,News.RSSFeedContents.ITEM_DESCRIPTION);
		
	

		ATOMFEED_PROJECTION_MAP=new HashMap<String,String>();
		//ATOMFEED_PROJECTION_MAP
		ATOMFEED_PROJECTION_MAP.put(News.AtomFeeds._ID, News.AtomFeeds._ID);
		ATOMFEED_PROJECTION_MAP.put(News.AtomFeeds._COUNT,News.AtomFeeds._COUNT);
		//FEED_PROJECTION_MAP.put(News.AtomFeeds.CHANNEL_ID,News.AtomFeeds.CHANNEL_ID);
		ATOMFEED_PROJECTION_MAP.put(News.AtomFeeds.FEED_ID,News.AtomFeeds.FEED_ID);
		ATOMFEED_PROJECTION_MAP.put(News.AtomFeeds.FEED_LINK,News.AtomFeeds.FEED_LINK);
		ATOMFEED_PROJECTION_MAP.put(News.AtomFeeds.FEED_LINK_SELF,News.AtomFeeds.FEED_LINK_SELF);
		ATOMFEED_PROJECTION_MAP.put(News.AtomFeeds.FEED_LINK_ALTERNATE,News.AtomFeeds.FEED_LINK_ALTERNATE);
		ATOMFEED_PROJECTION_MAP.put(News.AtomFeeds.FEED_TITLE,News.AtomFeeds.FEED_TITLE);
		
		ATOMFEED_PROJECTION_MAP.put(News.AtomFeeds.FEED_RIGHTS,News.AtomFeeds.FEED_RIGHTS);
		ATOMFEED_PROJECTION_MAP.put(News.AtomFeeds.FEED_ICON,News.AtomFeeds.FEED_ICON);
		ATOMFEED_PROJECTION_MAP.put(News.AtomFeeds.HISTORY_LENGTH,News.AtomFeeds.HISTORY_LENGTH);
		ATOMFEED_PROJECTION_MAP.put(News.AtomFeeds.UPDATE_CYCLE,News.AtomFeeds.UPDATE_CYCLE);
		ATOMFEED_PROJECTION_MAP.put(News.AtomFeeds.FEED_UPDATED,News.AtomFeeds.FEED_UPDATED);
		ATOMFEED_PROJECTION_MAP.put(News.AtomFeeds.FEED_LAST_CHECKED,News.AtomFeeds.FEED_LAST_CHECKED);
		
		ATOMCONTENT_PROJECTION_MAP=new HashMap<String,String>();
		ATOMCONTENT_PROJECTION_MAP.put(News.AtomFeedContents._ID,News.AtomFeedContents._ID);
		ATOMCONTENT_PROJECTION_MAP.put(News.AtomFeedContents._COUNT,News.AtomFeedContents._COUNT);
		ATOMCONTENT_PROJECTION_MAP.put(News.AtomFeedContents.FEED_ID,News.AtomFeedContents.FEED_ID);
		ATOMCONTENT_PROJECTION_MAP.put(News.AtomFeedContents.ENTRY_ID,News.AtomFeedContents.ENTRY_ID);
		ATOMCONTENT_PROJECTION_MAP.put(News.AtomFeedContents.ENTRY_TITLE,News.AtomFeedContents.ENTRY_TITLE);
		ATOMCONTENT_PROJECTION_MAP.put(News.AtomFeedContents.ENTRY_LINK,News.AtomFeedContents.ENTRY_LINK);
		ATOMCONTENT_PROJECTION_MAP.put(News.AtomFeedContents.ENTRY_LINK_ALTERNATE,News.AtomFeedContents.ENTRY_LINK_ALTERNATE);
		ATOMCONTENT_PROJECTION_MAP.put(News.AtomFeedContents.ENTRY_SUMMARY,News.AtomFeedContents.ENTRY_SUMMARY);
		ATOMCONTENT_PROJECTION_MAP.put(News.AtomFeedContents.ENTRY_SUMMARY_TYPE,News.AtomFeedContents.ENTRY_SUMMARY_TYPE);
		ATOMCONTENT_PROJECTION_MAP.put(News.AtomFeedContents.ENTRY_CONTENT,News.AtomFeedContents.ENTRY_CONTENT);
		ATOMCONTENT_PROJECTION_MAP.put(News.AtomFeedContents.ENTRY_CONTENT_TYPE,News.AtomFeedContents.ENTRY_CONTENT_TYPE);
		


	}
	
	
}/*eoc*/
