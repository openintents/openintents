package org.openintents.newsreader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.openintents.provider.News;
import org.openintents.provider.News.Categories;
import org.openintents.provider.News.Channel;
import org.openintents.provider.News.Contents;

import android.app.Activity;
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
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.util.Log;

public class NewsProvider extends ContentProvider {

	private static HashMap<String, String> PROJECTION_MAP_LAST_PUBDATE;

	private SQLiteOpenHelper mOpenHelper;

	private static final String DATABASE_NAME = "newsfeeds.db";
	private static final int DATABASE_VERSION = 5;
	private static final String TAG = "NewsProvider";

	private static final int CATEGORIES = 2005;
	private static final int CATEGORIES_ID = 2006;
	private static final int CONTENTS = 1007;
	private static final int CONTENTS_ID = 1008;
	private static final int CHANNELS = 1009;
	private static final int CHANNELS_ID = 1010;

	private static final UriMatcher URL_MATCHER;

	public static final String TABLE_CHANNELS = "channels";
	public static final String TABLE_CONTENTS = "contents";
	private static final String TABLE_CATEGORIES = "categories";

	public static final HashMap<String, String> CHANNEL_PROJECTION_MAP;
	public static final HashMap<String, String> CONTENT_PROJECTION_MAP;

	public static final HashMap<String, String> CATEGORIES_PROJECTION_MAP;

	private static final Map<String, String> CATEGORIES_CHANNELS_PROJECTION_MAP;
	private static final Map<String, String> CHANNELS_PROJECTION_MAP_LAST_PUBDATE;
	private static final Map<String, String> CHANNELS_PROJECTION_MAP_SORT_UNREAD;

	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "Creating table " + TABLE_CHANNELS);

			db.execSQL("CREATE TABLE " + TABLE_CHANNELS + "("
					+ News.Channel._ID + " INTEGER PRIMARY KEY,"
					+ News.Channel.CHANNEL_NAME + " STRING,"
					+ News.Channel.CHANNEL_LINK + " STRING,"
					+ News.Channel.CHANNEL_DATA_LINK + " STRING,"
					+ News.Channel.CHANNEL_TYPE + " INTEGER,"
					+ News.Channel.CHANNEL_NATURE + " INTEGER,"
					+ News.Channel.CHANNEL_ICON_URI + " STRING,"
					+ News.Channel.CHANNEL_DESC + " STRING,"
					+ News.Channel.UPDATE_CYCLE + " INTEGER,"
					+ News.Channel.HISTORY_LENGTH + " INTEGER,"
					+ News.Channel.LAST_PUBDATE + " DATE,"
					+ News.Channel.LAST_UPDATE + " INTEGER,"
					+ News.Channel.CHANNEL_COPYRIGHT + " STRING,"
					+ News.Channel.CHANNEL_CATEGORIES + " STRING,"
					+ News.Channel.NOTIFY_NEW + " INTEGER,"
					+ News.Channel.UPDATE_MSGS + " INTEGER,"
					+ News.Channel.CHANNEL_LANG + " STRING" + ");");

			Log.d(TAG, "Creating table " + TABLE_CONTENTS);
			db.execSQL("CREATE TABLE " + TABLE_CONTENTS + " ("
					+ News.Contents._ID + " INTEGER PRIMARY KEY,"
					+ News.Contents.CHANNEL_ID + " INTEGER,"
					+ News.Contents.CHANNEL_TYPE + " INTEGER,"
					+ News.Contents.ITEM_GUID + " STRING,"
					+ News.Contents.ITEM_LINK + " STRING,"
					+ News.Contents.ITEM_TITLE + " STRING,"
					+ News.Contents.ITEM_CONTENT + " STRING,"
					+ News.Contents.ITEM_CONTENT_TYPE + " STRING,"
					+ News.Contents.ITEM_AUTHOR + " STRING,"
					+ News.Contents.ITEM_PUB_DATE + " STRING,"
					+ News.Contents.CREATED_ON + " LONG,"
					+ News.Contents.READ_STATUS + " INTEGER DEFAULT "
					+ News.STATUS_UNREAD + ");");

			db.execSQL("CREATE TABLE " + TABLE_CATEGORIES + " ("
					+ News.Categories._ID + " INTEGER PRIMARY KEY,"
					+ News.Categories.NAME + " STRING" + ");");

			/*
			 * db.execSQL("CREATE TABLE rssfeeds("+ News.RSSFeeds._ID
			 * +" INTEGER PRIMARY KEY,"+ News.RSSFeeds._COUNT+" INTEGER,"+
			 * //News.RSSFeeds.CHANNEL_ID+" "+News.RSSFeeds.CHANNEL_ID_TYPE+","+
			 * News
			 * .RSSFeeds.CHANNEL_LINK+" "+News.RSSFeeds.CHANNEL_LINK_TYPE+","+
			 * News
			 * .RSSFeeds.CHANNEL_NAME+" "+News.RSSFeeds.CHANNEL_NAME_TYPE+","+
			 * News
			 * .RSSFeeds.CHANNEL_DESC+" "+News.RSSFeeds.CHANNEL_DESC_TYPE+","+
			 * News
			 * .RSSFeeds.CHANNEL_LANG+" "+News.RSSFeeds.CHANNEL_LANG_TYPE+","+
			 * News
			 * .RSSFeeds.CHANNEL_COPYRIGHT+" "+News.RSSFeeds.CHANNEL_COPYRIGHT_TYPE
			 * +","+News.RSSFeeds.CHANNEL_IMAGE_URI+" "+News.RSSFeeds.
			 * CHANNEL_IMAGE_URI_TYPE+","+
			 * News.RSSFeeds.HISTORY_LENGTH+" "+News.
			 * RSSFeeds.HISTORY_LENGTH_TYPE+","+
			 * News.RSSFeeds.UPDATE_CYCLE+" "+News
			 * .RSSFeeds.UPDATE_CYCLE_TYPE+","+
			 * News.RSSFeeds.LAST_UPDATE+" "+News.RSSFeeds.LAST_UPDATE_TYPE+","+
			 * News.RSSFeeds.FEED_NATURE+" INTEGER"+ ");");
			 */
			/*
			 * Log.d(TAG,"Creating table rssfeedcontents");
			 * 
			 * db.execSQL("CREATE TABLE rssfeedcontents("+
			 * News.RSSFeedContents._ID +" INTEGER PRIMARY KEY,"+
			 * News.RSSFeedContents._COUNT+" INTEGER,"+
			 * News.RSSFeedContents.CHANNEL_ID
			 * +" "+News.RSSFeedContents.CHANNEL_ID_TYPE+","+
			 * News.RSSFeedContents
			 * .ITEM_GUID+" "+News.RSSFeedContents.ITEM_GUID_TYPE+","+
			 * News.RSSFeedContents
			 * .ITEM_TITLE+" "+News.RSSFeedContents.ITEM_TITLE_TYPE+","+
			 * News.RSSFeedContents
			 * .ITEM_AUTHOR+" "+News.RSSFeedContents.ITEM_AUTHOR_TYPE+","+
			 * News.RSSFeedContents
			 * .ITEM_LINK+" "+News.RSSFeedContents.ITEM_LINK_TYPE+","+
			 * News.RSSFeedContents
			 * .ITEM_DESCRIPTION+" "+News.RSSFeedContents.ITEM_DESCRIPTION_TYPE+
			 * ");" ); / db.execSQL("CREATE TABLE atomfeeds("+
			 * News.AtomFeeds._ID+" INTEGER PRIMARY KEY,"+
			 * News.AtomFeeds._COUNT+" INTEGER,"+
			 * News.AtomFeeds.FEED_TITLE+" STRING,"+
			 * News.AtomFeeds.FEED_UPDATED+" STRING,"+
			 * News.AtomFeeds.FEED_LAST_CHECKED+" STRING,"+
			 * News.AtomFeeds.UPDATE_CYCLE+" INTEGER,"+
			 * News.AtomFeeds.HISTORY_LENGTH+" INTEGER,"+
			 * News.AtomFeeds.FEED_LINK+" STRING,"+
			 * News.AtomFeeds.FEED_LINK_SELF+" STRING,"+
			 * News.AtomFeeds.FEED_LINK_ALTERNATE+" STRING,"+
			 * News.AtomFeeds.FEED_ICON+" STRING,"+
			 * News.AtomFeeds.FEED_RIGHTS+" STRING,"+
			 * News.AtomFeeds.FEED_NATURE+" INTEGER"+ ");" );
			 */
			/*
			 * db.execSQL("CREATE TABLE atomfeedcontents ("+
			 * News.AtomFeedContents._ID+" INTEGER PRIMARY KEY,"+
			 * News.AtomFeedContents._COUNT+" INTEGER,"+
			 * News.AtomFeedContents.FEED_ID+" STRING,"+
			 * News.AtomFeedContents.ENTRY_ID+" STRING,"+
			 * News.AtomFeedContents.ENTRY_TITLE+" STRING,"+
			 * News.AtomFeedContents.ENTRY_LINK+" STRING,"+
			 * News.AtomFeedContents.ENTRY_LINK_ALTERNATE+" STRING,"+
			 * News.AtomFeedContents.ENTRY_SUMMARY+" STRING,"+
			 * News.AtomFeedContents.ENTRY_SUMMARY_TYPE+" STRING"+ ");" );
			 */

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// we can't vacuum here.
			// db.execSQL("VACUUM;");
			// TODO: add fields FEED_NATURE to rssatom feeds.
			// News.RSSFeeds.FEED_NATURE+" INTEGER"+

			// Log.v(TAG, "");

		}// onupgrade

	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(TAG, "ENTERING DELETE, uri>>" + uri + "<<");
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count = 0;
		// if there's nowhere to delete, we fail.
		if (uri == null) {
			throw new IllegalArgumentException(
					"uri and values must be specified");
		}

		String feedID;
		int match = URL_MATCHER.match(uri);

		switch (match) {

		case CATEGORIES:
			if (Categories.QUERY_VALUE_Y.equals(uri.getQueryParameter(Categories.QUERY_COMPRESS))){
			
			count = db.delete(TABLE_CATEGORIES, "NOT EXISTS(SELECT " + TABLE_CHANNELS + "." + Channel._ID + " from " + TABLE_CHANNELS + " WHERE upper(" + TABLE_CHANNELS
						+ "." + Channel.CHANNEL_CATEGORIES +") like upper('%' || " + TABLE_CATEGORIES + "."
						+ Categories.NAME +  "|| '%'))"
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			} else {
				count = 0;
			}
			break;
			
		case CATEGORIES_ID:
			String catID = uri.getLastPathSegment();
			Log.v(TAG, catID);
			count = db.delete(TABLE_CATEGORIES, "_id="
					+ catID
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;

		case CHANNELS_ID:
			feedID = uri.getPathSegments().get(1);
			count = db.delete(TABLE_CHANNELS, "_id="
					+ feedID
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			break;
		case CHANNELS:
			count = db.delete(TABLE_CHANNELS, selection, selectionArgs);
			break;
		case CONTENTS_ID:
			feedID = uri.getPathSegments().get(1);
			count = db.delete(TABLE_CONTENTS, "_id="
					+ feedID
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);

			break;
		case CONTENTS:
			count = db.delete(TABLE_CONTENTS, selection, selectionArgs);
			break;

		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	/*
	 * @author Zero
	 * 
	 * @version 1.0
	 * 
	 * @argument uri ContentURI NOT NULL
	 * 
	 * @argument values ContentValues NOT NULL
	 * 
	 * @return uri of the new item.
	 */
	public Uri insert(Uri uri, ContentValues values) {
		Log.d(this.TAG, "ENTERING INSERT, uri>>" + uri + "<<");
		// if there's nowhere to insert, we fail. if there's no data to insert,
		// we fail.
		if (uri == null || values == null) {
			throw new IllegalArgumentException(
					"uri and values must be specified");
		}

		int match = URL_MATCHER.match(uri);
		Log.d(this.TAG, "INSERT,URI MATCHER RETURNED >>" + match + "<<");
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long rowID = 0;
		switch (match) {

		case CHANNELS:
			// use insert as intended by SQL standard, avoid "nullcolumnhack" at
			// all costs.
			if (!values.containsKey(News.Channel.CHANNEL_NAME)) {
				throw new IllegalArgumentException(
						"At least News.Channel.CHANNEL_NAME"
								+ "  must be specified in values. Unbound Items are not allowed.");

			}
			if (!values.containsKey(News.Channel.CHANNEL_TYPE)) {
				throw new IllegalArgumentException(
						"At least News.Channel.CHANNEL_TYPE"
								+ "  must be specified in values. Unbound Items are not allowed.");
			}

			if (!values.containsKey(News.Channel.CHANNEL_NATURE)) {
				values.put(News.Channel.CHANNEL_NATURE,
						News.CHANNEL_NATURE_USER);
			}

			rowID = db.insert(TABLE_CHANNELS, "", values);
			if (rowID > 0) {
				Uri nUri = ContentUris.withAppendedId(News.Channel.CONTENT_URI,
						rowID);
				getContext().getContentResolver().notifyChange(nUri, null);
				return nUri;
			}
			throw new SQLException("Failed to insert row into " + uri);

		case CONTENTS:
			// Update of feeds table's "LastUpdated" handled in newsreader
			// service after all inserts.
			rowID = db.insert(TABLE_CONTENTS, "", values);
			if (rowID > 0) {
				Uri nUri = ContentUris.withAppendedId(
						News.Contents.CONTENT_URI, rowID);
				getContext().getContentResolver().notifyChange(nUri, null);
				return nUri;
			}
			throw new SQLException("Failed to insert row into " + uri);

		case CATEGORIES:
			rowID = db.insert(TABLE_CATEGORIES, "", values);
			if (rowID > 0) {
				Uri nUri = ContentUris.withAppendedId(
						News.Categories.CONTENT_URI, rowID);
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
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String orderBy = null;
		String groupBy = null;
		switch (URL_MATCHER.match(uri)) {

		case CATEGORIES:

			if (Categories.QUERY_VALUE_Y.equals(uri
					.getQueryParameter(Categories.QUERY_USED_ONLY))) {
				qb.setTables(TABLE_CATEGORIES + "," + TABLE_CHANNELS);
				qb.setProjectionMap(CATEGORIES_CHANNELS_PROJECTION_MAP);
				qb.appendWhere("upper(" + TABLE_CHANNELS
						+ "." + Channel.CHANNEL_CATEGORIES +") like upper('%' || " + TABLE_CATEGORIES + "."
						+ Categories.NAME +  "|| '%')");
			} else {
				qb.setTables(TABLE_CATEGORIES);
				qb.setProjectionMap(CATEGORIES_PROJECTION_MAP);
			}
			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = News.Categories.DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			break;
		case CATEGORIES_ID:
			qb.setTables(TABLE_CATEGORIES);
			qb.setProjectionMap(CATEGORIES_PROJECTION_MAP);
			qb.appendWhere("_id=" + uri.getLastPathSegment());
			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = News.Categories.DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			break;

		case CHANNELS:
			if ("Y".equals(uri.getQueryParameter(Channel.CONTENT_CREATED))) {
				qb.setTables(TABLE_CHANNELS + " LEFT OUTER JOIN "
						+ TABLE_CONTENTS + " ON " + TABLE_CHANNELS + "."
						+ Channel._ID + " = " + TABLE_CONTENTS + "."
						+ Contents.CHANNEL_ID);

				if (sortOrder.equals(Channel.SORT_ORDER_WITH_CONTENT_LATEST))
				{
					qb.setProjectionMap(CHANNELS_PROJECTION_MAP_LAST_PUBDATE);
				}else{
					qb.setProjectionMap(CHANNELS_PROJECTION_MAP_SORT_UNREAD);
				}
				if ("Y"
						.equals(uri
								.getQueryParameter(Channel.QUERY_STATUS_UNREAD))) {
					qb.appendWhere(TABLE_CONTENTS + "." + Contents.READ_STATUS
							+ " = 0 OR " + TABLE_CONTENTS + "."
							+ Contents.READ_STATUS + " is null ");
				}
				groupBy = Channel._ID;
			} else {
				qb.setTables(TABLE_CHANNELS);
				qb.setProjectionMap(CHANNEL_PROJECTION_MAP);
			}
			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = News.Channel.DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			break;
		case CHANNELS_ID:
			if ("Y".equals(uri.getQueryParameter(Channel.CONTENT_CREATED))) {
				qb.setTables(TABLE_CONTENTS);
				qb.appendWhere("channel_id=" + uri.getLastPathSegment());
				qb.setProjectionMap(PROJECTION_MAP_LAST_PUBDATE);
			} else {
				qb.setTables(TABLE_CHANNELS);
				// qb.setProjectionMap(FEED_PROJECTION_MAP);
				qb.appendWhere("_id=" + uri.getLastPathSegment());

				if (TextUtils.isEmpty(sortOrder)) {
					orderBy = News.Channel.DEFAULT_SORT_ORDER;
				} else {
					orderBy = sortOrder;
				}
			}
			break;
		case CONTENTS:
			qb.setTables(TABLE_CONTENTS);
			// Log.i(TAG, "type: " + News.Channel.CHANNEL_TYPE);
			//qb.appendWhere(News.Channel.CHANNEL_TYPE+"="+News.CHANNEL_TYPE_RSS
			// );
			qb.setProjectionMap(CONTENT_PROJECTION_MAP);
			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = News.Contents.DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			break;
		case CONTENTS_ID:
			qb.setTables(TABLE_CONTENTS);
			qb.appendWhere("_id=" + uri.getLastPathSegment());
			if (TextUtils.isEmpty(sortOrder)) {
				orderBy = News.Contents.DEFAULT_SORT_ORDER;
			} else {
				orderBy = sortOrder;
			}
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}

		Log.d(TAG, "going for query:\n selection>" + selection + "\n selArgs>"
				+ selectionArgs + "\n ------------");
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, groupBy,
				null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count = 0;
		switch (URL_MATCHER.match(uri)) {
		case CHANNELS:

			count = db.update(TABLE_CHANNELS, values, selection, selectionArgs);

			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case CHANNELS_ID:
			String feedID = uri.getLastPathSegment();
			count = db.update(TABLE_CHANNELS, values, "_id="
					+ feedID
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			Log.v(TAG, "update " + feedID
					+ values.getAsString(Channel.NOTIFY_NEW));
			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case CONTENTS_ID:
			feedID = uri.getLastPathSegment();
			count = db.update(TABLE_CONTENTS, values, "_id="
					+ feedID
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);

			getContext().getContentResolver().notifyChange(uri, null);
			break;
		case CONTENTS:
			count = db.update(TABLE_CONTENTS, values, selection, selectionArgs);

			getContext().getContentResolver().notifyChange(uri, null);
			break;
		default:

			throw new IllegalArgumentException("Unsupported URL " + uri);

		}

		return count;
	}

	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		ParcelFileDescriptor pfd = null;
		switch (URL_MATCHER.match(uri)) {
		case CONTENTS_ID:
			String[] projection = new String[] { Contents._ID,
					Contents.ITEM_CONTENT };
			Cursor c = query(uri, projection, null, null, null);
			c.moveToFirst();
			String content = c.getString(1);
			c.close();

			File f = getContext().getFileStreamPath("temp.xml");

			try {
				FileOutputStream fos = getContext().openFileOutput("temp.xml",
						Activity.MODE_PRIVATE);
				fos.write(content.getBytes());
				fos.close();
				Log.v(TAG, "temp file size:" + f.length());

			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(getContext().getString(
						R.string.offline_read_exception));
			}
			Log.v(TAG, "written temp file");

			int m = ParcelFileDescriptor.MODE_READ_ONLY;
			if (mode.equalsIgnoreCase("rw"))
				m = ParcelFileDescriptor.MODE_READ_WRITE;

			pfd = ParcelFileDescriptor.open(f, m);
			break;
		}

		return pfd;
	}

	static {

		URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URL_MATCHER.addURI("org.openintents.news", "rss", CHANNELS);
		URL_MATCHER.addURI("org.openintents.news", "rss/#", CHANNELS_ID);
		URL_MATCHER.addURI("org.openintents.news", "rsscontents", CONTENTS);
		URL_MATCHER
				.addURI("org.openintents.news", "rsscontents/#", CONTENTS_ID);
		URL_MATCHER.addURI("org.openintents.news", "atom", CHANNELS);
		URL_MATCHER.addURI("org.openintents.news", "atom/#", CHANNELS_ID);
		URL_MATCHER.addURI("org.openintents.news", "atomcontents", CONTENTS);
		URL_MATCHER.addURI("org.openintents.news", "atomcontents/#",
				CONTENTS_ID);
		URL_MATCHER.addURI("org.openintents.news", "categories", CATEGORIES);
		URL_MATCHER.addURI("org.openintents.news", "categories/#",
				CATEGORIES_ID);
		URL_MATCHER.addURI("org.openintents.news", "channels", CHANNELS);
		URL_MATCHER.addURI("org.openintents.news", "channels/#", CHANNELS_ID);
		URL_MATCHER.addURI("org.openintents.news", "contents", CONTENTS);
		URL_MATCHER.addURI("org.openintents.news", "contents/#", CONTENTS_ID);

		CHANNEL_PROJECTION_MAP = new HashMap<String, String>();
		// CHANNEL_PROJECTION_MAP
		CHANNEL_PROJECTION_MAP.put(News.Channel._ID, News.Channel._ID);
		CHANNEL_PROJECTION_MAP.put(News.Channel.CHANNEL_NAME,
				News.Channel.CHANNEL_NAME);
		CHANNEL_PROJECTION_MAP.put(News.Channel.CHANNEL_TYPE,
				News.Channel.CHANNEL_TYPE);
		CHANNEL_PROJECTION_MAP.put(News.Channel.CHANNEL_NATURE,
				News.Channel.CHANNEL_NATURE);
		CHANNEL_PROJECTION_MAP.put(News.Channel.CHANNEL_LINK,
				News.Channel.CHANNEL_LINK);
		CHANNEL_PROJECTION_MAP.put(News.Channel.CHANNEL_DATA_LINK,
				News.Channel.CHANNEL_DATA_LINK);
		CHANNEL_PROJECTION_MAP.put(News.Channel.CHANNEL_DESC,
				News.Channel.CHANNEL_DESC);
		CHANNEL_PROJECTION_MAP.put(News.Channel.CHANNEL_LANG,
				News.Channel.CHANNEL_LANG);
		CHANNEL_PROJECTION_MAP.put(News.Channel.CHANNEL_COPYRIGHT,
				News.Channel.CHANNEL_COPYRIGHT);
		CHANNEL_PROJECTION_MAP.put(News.Channel.CHANNEL_CATEGORIES,
				News.Channel.CHANNEL_CATEGORIES);
		CHANNEL_PROJECTION_MAP.put(News.Channel.CHANNEL_ICON_URI,
				News.Channel.CHANNEL_ICON_URI);
		CHANNEL_PROJECTION_MAP.put(News.Channel.NOTIFY_NEW,
				News.Channel.NOTIFY_NEW);
		CHANNEL_PROJECTION_MAP.put(News.Channel.HISTORY_LENGTH,
				News.Channel.HISTORY_LENGTH);
		CHANNEL_PROJECTION_MAP.put(News.Channel.UPDATE_CYCLE,
				News.Channel.UPDATE_CYCLE);
		CHANNEL_PROJECTION_MAP.put(News.Channel.UPDATE_MSGS,
				News.Channel.UPDATE_MSGS);
		CHANNEL_PROJECTION_MAP.put(News.Channel.LAST_UPDATE,
				News.Channel.LAST_UPDATE);

		CONTENT_PROJECTION_MAP = new HashMap<String, String>();
		CONTENT_PROJECTION_MAP.put(News.Contents._ID, News.Contents._ID);
		CONTENT_PROJECTION_MAP.put(News.Contents.CHANNEL_ID,
				News.Contents.CHANNEL_ID);
		CONTENT_PROJECTION_MAP.put(News.Contents.ITEM_GUID,
				News.Contents.ITEM_GUID);
		CONTENT_PROJECTION_MAP.put(News.Contents.ITEM_TITLE,
				News.Contents.ITEM_TITLE);
		CONTENT_PROJECTION_MAP.put(News.Contents.ITEM_AUTHOR,
				News.Contents.ITEM_AUTHOR);
		CONTENT_PROJECTION_MAP.put(News.Contents.ITEM_LINK,
				News.Contents.ITEM_LINK);
		CONTENT_PROJECTION_MAP.put(News.Contents.ITEM_SUMMARY,
				News.Contents.ITEM_SUMMARY);
		CONTENT_PROJECTION_MAP.put(News.Contents.ITEM_SUMMARY_TYPE,
				News.Contents.ITEM_SUMMARY_TYPE);
		CONTENT_PROJECTION_MAP.put(News.Contents.ITEM_CONTENT,
				News.Contents.ITEM_CONTENT);
		CONTENT_PROJECTION_MAP.put(News.Contents.ITEM_CONTENT_TYPE,
				News.Contents.ITEM_CONTENT_TYPE);
		CONTENT_PROJECTION_MAP.put(News.Contents.ITEM_PUB_DATE,
				News.Contents.ITEM_PUB_DATE);
		CONTENT_PROJECTION_MAP.put(News.Contents.CREATED_ON,
				News.Contents.CREATED_ON);
		CONTENT_PROJECTION_MAP.put(News.Contents.READ_STATUS,
				News.Contents.READ_STATUS);

		CATEGORIES_PROJECTION_MAP = new HashMap<String, String>();
		CATEGORIES_PROJECTION_MAP.put(News.Categories._ID, News.Categories._ID);
		CATEGORIES_PROJECTION_MAP.put(News.Categories.NAME,
				News.Categories.NAME);

		CATEGORIES_CHANNELS_PROJECTION_MAP = new HashMap<String, String>();
		CATEGORIES_CHANNELS_PROJECTION_MAP.put(News.Categories._ID,
				TABLE_CATEGORIES + "." + News.Categories._ID);
		CATEGORIES_CHANNELS_PROJECTION_MAP.put(News.Categories.NAME,
				TABLE_CATEGORIES + "." + News.Categories.NAME);

		PROJECTION_MAP_LAST_PUBDATE = new HashMap<String, String>();
		PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.CONTENT_CREATED, "max("
				+ Contents.CREATED_ON + ")");

		CHANNELS_PROJECTION_MAP_LAST_PUBDATE = new HashMap<String, String>();
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel._ID,
				TABLE_CHANNELS + "." + News.Channel._ID + " as "
						+ News.Channel._ID);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.CHANNEL_NAME,
				News.Channel.CHANNEL_NAME);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.CHANNEL_TYPE,
				TABLE_CHANNELS + "." + News.Channel.CHANNEL_TYPE + " as "
						+ News.Channel.CHANNEL_TYPE);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.CHANNEL_NATURE,
				News.Channel.CHANNEL_NATURE);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.CHANNEL_LINK,
				News.Channel.CHANNEL_LINK);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(
				News.Channel.CHANNEL_DATA_LINK, News.Channel.CHANNEL_DATA_LINK);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.CHANNEL_DESC,
				News.Channel.CHANNEL_DESC);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.CHANNEL_LANG,
				News.Channel.CHANNEL_LANG);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(
				News.Channel.CHANNEL_COPYRIGHT, News.Channel.CHANNEL_COPYRIGHT);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(
				News.Channel.CHANNEL_CATEGORIES,
				News.Channel.CHANNEL_CATEGORIES);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.CHANNEL_ICON_URI,
				News.Channel.CHANNEL_ICON_URI);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.NOTIFY_NEW,
				News.Channel.NOTIFY_NEW);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.HISTORY_LENGTH,
				News.Channel.HISTORY_LENGTH);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.UPDATE_CYCLE,
				News.Channel.UPDATE_CYCLE);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.UPDATE_MSGS,
				News.Channel.UPDATE_MSGS);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.LAST_UPDATE,
				TABLE_CHANNELS + "." + News.Channel.LAST_UPDATE + " as "
						+ News.Channel.LAST_UPDATE);
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.CONTENT_COUNT,
		
				"count(" + TABLE_CONTENTS + "." + Contents._ID + ")  as "
						+ News.Channel.CONTENT_COUNT);
				
		CHANNELS_PROJECTION_MAP_LAST_PUBDATE.put(News.Channel.CONTENT_CREATED,
				"max(" + TABLE_CONTENTS + "." + Contents.CREATED_ON + ") as "
						+ News.Channel.CONTENT_CREATED);
		
		//---------------------------------------------

		CHANNELS_PROJECTION_MAP_SORT_UNREAD = new HashMap<String, String>();
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel._ID,
				TABLE_CHANNELS + "." + News.Channel._ID + " as "
						+ News.Channel._ID);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.CHANNEL_NAME,
				News.Channel.CHANNEL_NAME);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.CHANNEL_TYPE,
				TABLE_CHANNELS + "." + News.Channel.CHANNEL_TYPE + " as "
						+ News.Channel.CHANNEL_TYPE);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.CHANNEL_NATURE,
				News.Channel.CHANNEL_NATURE);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.CHANNEL_LINK,
				News.Channel.CHANNEL_LINK);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(
				News.Channel.CHANNEL_DATA_LINK, News.Channel.CHANNEL_DATA_LINK);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.CHANNEL_DESC,
				News.Channel.CHANNEL_DESC);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.CHANNEL_LANG,
				News.Channel.CHANNEL_LANG);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(
				News.Channel.CHANNEL_COPYRIGHT, News.Channel.CHANNEL_COPYRIGHT);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(
				News.Channel.CHANNEL_CATEGORIES,
				News.Channel.CHANNEL_CATEGORIES);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.CHANNEL_ICON_URI,
				News.Channel.CHANNEL_ICON_URI);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.NOTIFY_NEW,
				News.Channel.NOTIFY_NEW);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.HISTORY_LENGTH,
				News.Channel.HISTORY_LENGTH);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.UPDATE_CYCLE,
				News.Channel.UPDATE_CYCLE);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.UPDATE_MSGS,
				News.Channel.UPDATE_MSGS);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.LAST_UPDATE,
				TABLE_CHANNELS + "." + News.Channel.LAST_UPDATE + " as "
						+ News.Channel.LAST_UPDATE);
		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.CONTENT_COUNT,
				//"count(" + TABLE_CONTENTS + "." + Contents._ID + ")  as " + News.Channel.CONTENT_COUNT + " WHERE );
				"(SELECT count( distinct(" + TABLE_CONTENTS + "." + Contents._ID + "))  "
				+"FROM "+TABLE_CONTENTS//+" INNER JOIN "+TABLE_CHANNELS
				//+" ON "+TABLE_CONTENTS+"."+Contents.CHANNEL_ID+"="+TABLE_CHANNELS+"."+Channel._ID
				+" WHERE ("+News.Contents.READ_STATUS+"=0 OR "+News.Contents.READ_STATUS+" is null)"
				+" AND ("+TABLE_CONTENTS+"."+Contents.CHANNEL_ID+"="+TABLE_CHANNELS+"."+Channel._ID+")"
				+") as " + News.Channel.CONTENT_COUNT  );



		CHANNELS_PROJECTION_MAP_SORT_UNREAD.put(News.Channel.CONTENT_CREATED,
				"max(" + TABLE_CONTENTS + "." + Contents.CREATED_ON + ") as "
						+ News.Channel.CONTENT_CREATED);






	}

}/* eoc */
