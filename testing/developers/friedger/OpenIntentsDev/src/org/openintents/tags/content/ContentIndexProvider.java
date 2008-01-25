package org.openintents.tags.content;

import java.util.Map;

import org.openintents.provider.ContentIndex;

import android.content.ContentProviderDatabaseHelper;
import android.content.ContentURIParser;
import android.content.ContentValues;
import android.content.QueryBuilder;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ContentURI;
import android.text.TextUtils;
import android.util.Log;

public class ContentIndexProvider extends
		android.content.DatabaseContentProvider {

	private static final String TAG = "Tables";

	static final String DATABASE_NAME = "deepdroid.db";

	private static final int DATABASE_VERSION = 26;

	private static final ContentURIParser URL_MATCHER;
	private static final int DIRECTORIES = 1;
	private static final int DIRECTORY = 2;
	private static final int INDEX_ENTRIES = 3;

	private static final Map<String, String> DIRECTORY_PROJECTION_MAP = null;

	static class DatabaseHelper extends ContentProviderDatabaseHelper {

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "Create tables");

			StringBuffer dirs = new StringBuffer();
			dirs.append("CREATE TABLE dirs (");
			dirs.append("_id INTEGER PRIMARY KEY AUTOINCREMENT,");
			dirs.append("parent_id INTEGER,");
			dirs.append("uri TEXT NOT NULL,");
			dirs.append("package TEXT NOT NULL,");
			dirs.append("name TEXT,");
			dirs.append("text_columns TEXT,");
			dirs.append("id_column TEXT,");
			dirs.append("time_column TEXT,");
			dirs.append("intent_uri TEXT,");
			dirs.append("intent_action TEXT,");
			dirs.append("refreshed LONG,");
			dirs.append("updated LONG);");
			db.execSQL(dirs.toString());

			StringBuffer items = new StringBuffer();
			items.append("CREATE TABLE items (");
			items.append("item_id INTEGER NOT NULL,");
			items.append("dir_id INTEGER NOT NULL,");
			items.append("body TEXT NOT NULL,");
			items.append("updated LONG,");
			items.append("UNIQUE(dir_id, item_id));");
			db.execSQL(items.toString());

			StringBuffer del_items = new StringBuffer();
			del_items.append("CREATE TRIGGER del_items DELETE ON dirs");
			del_items.append(" BEGIN");
			del_items.append(" DELETE FROM items WHERE dir_id = old._id;");
			del_items.append(" END");
			db.execSQL(del_items.toString());
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS dirs");
			db.execSQL("DROP TABLE IF EXISTS items");
			onCreate(db);
		}

	}

	private SQLiteDatabase mDB;;

	public ContentIndexProvider(String dbName, int dbVersion) {
		super(dbName, dbVersion);
	}

	@Override
	public boolean onCreate() {
		DatabaseHelper dbHelper = new DatabaseHelper();
		mDB = dbHelper.openDatabase(getContext(), DATABASE_NAME, null,
				DATABASE_VERSION);
		return (mDB == null) ? false : true;
	}

	@Override
	protected void upgradeDatabase(int oldVersion, int newVersion) {
		DatabaseHelper dbHelper = new DatabaseHelper();
		dbHelper.onUpgrade(mDB, oldVersion, newVersion);
	}

	@Override
	public Cursor queryInternal(ContentURI url, String[] projection,
			String selection, String[] selectionArgs, String groupBy,
			String having, String sort) {
		QueryBuilder qb = new QueryBuilder();

		switch (URL_MATCHER.match(url)) {
		case DIRECTORIES:
			qb.setTables("dir");
			break;

		case DIRECTORY:
			qb.setTables("dir");
			qb.appendWhere("_id=" + url.getPathSegment(1));
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sort)) {
			orderBy = ContentIndex.Entry.DEFAULT_SORT_ORDER;
		} else {
			orderBy = sort;
		}

		Cursor c = qb.query(mDB, projection, selection, selectionArgs, groupBy,
				having, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), url);
		return c;
	}

	@Override
	public String getType(ContentURI url) {
		switch (URL_MATCHER.match(url)) {
		case DIRECTORIES:
			return "vnd.openintents.cursor.dir/contentdirectory";

		case DIRECTORY:
			return "vnd.openintents.cursor.item/contentdirectory";
		case INDEX_ENTRIES:
			return "vnd.openintents.cursor.item/contentindexentry";
		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}

	@Override
	public ContentURI insertInternal(ContentURI url, ContentValues initialValues) {
		return null;
	}

	@Override
	protected void bootstrapDatabase() {
		// TODO Auto-generated method stub
	}

	@Override
	public int deleteInternal(ContentURI url, String where, String[] whereArgs) {
		// do not allow to delete directories
		return 0;
	}

	@Override
	public int updateInternal(ContentURI url, ContentValues values,
			String where, String[] whereArgs) {
		int count;
		switch (URL_MATCHER.match(url)) {
		case DIRECTORIES:
			count = mDB.update("dir", values, where, whereArgs);
			break;

		case DIRECTORY:
			String segment = url.getPathSegment(1);
			count = mDB.update("dir", values,
					"_id="
							+ segment
							+ (!TextUtils.isEmpty(where) ? " AND (" + where
									+ ')' : ""), whereArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	static {
		URL_MATCHER = new ContentURIParser(ContentURIParser.NO_MATCH);
		URL_MATCHER.addURI("org.openintents.contentindices", "directories",
				DIRECTORIES);
		URL_MATCHER.addURI("org.openintents.contentindices", "directories/#",
				DIRECTORY);

	}

}
