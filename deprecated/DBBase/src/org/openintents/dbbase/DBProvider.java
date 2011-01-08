package org.openintents.dbbase;

import java.util.HashMap;
import java.util.Map;

import org.openintents.dbbase.DBBase.Columns;
import org.openintents.dbbase.DBBase.Tables;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

public class DBProvider extends ContentProvider {

	private SQLiteOpenHelper mUserHelper;
	private SQLiteOpenHelper mMetaHelper;

	private static final String USER_DATABASE_NAME = "user.db";
	private static final String META_DATABASE_NAME = "meta.db";
	public static final int META_DATABASE_VERSION = 1;

	private static final String TAG = "DBProvider";

	private static final String TABLE_META_TABLES = "tables";
	private static final String TABLE_META_COLUMNS = "columns";

	private static final UriMatcher URL_MATCHER;
	private static final int TABLES = 1;
	private static final int TABLES_ID = 2;
	private static final int ROWS = 3;
	private static final int ROWS_ID = 4;
	private static final int COLUMNS = 5;
	private static final int COLUMNS_ID = 6;
	private static final Map<String, String> PROJECTION_MAP_META_MASTER = new HashMap<String, String>();

	private static class MetaHelper extends SQLiteOpenHelper {

		private SQLiteOpenHelper mHelper;

		MetaHelper(Context context, SQLiteOpenHelper userHelper) {
			super(context, META_DATABASE_NAME, null, META_DATABASE_VERSION);
			mHelper = userHelper;
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "Creating table " + TABLE_META_TABLES);

			db.execSQL("CREATE TABLE " + TABLE_META_TABLES + "("
					+ DBBase.Tables._ID + " INTEGER PRIMARY KEY,"
					+ DBBase.Tables.TABLE_NAME + " STRING" + ");");

			db.execSQL("CREATE TABLE " + TABLE_META_COLUMNS + "("
					+ DBBase.Columns._ID + " INTEGER PRIMARY KEY,"
					+ DBBase.Columns.COL_NAME + " TEXT,"
					+ DBBase.Columns.COL_TYPE + " TEXT,"
					+ DBBase.Columns.TABLE_ID + " INTEGER" + ");");
			Cursor cursor = mHelper.getReadableDatabase().query(
					"SQLITE_MASTER", new String[] { "name" }, null, null, null,
					null, "name");
			while (cursor.moveToNext()) {
				ContentValues values = new ContentValues();
				values.put(Tables.TABLE_NAME, cursor.getString(0));
				long id = db.insert(TABLE_META_TABLES,
						DBBase.Tables.TABLE_NAME, values);
				// TODO insert columns
				Log.v(TAG, "inserted table " + cursor.getString(0));
			}
			cursor.close();
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE " + TABLE_META_TABLES);
			db.execSQL("DROP TABLE " + TABLE_META_COLUMNS);
			onCreate(db);

		}// onupgrade

	}

	private static class UserHelper extends SQLiteOpenHelper {

		UserHelper(Context context) {
			super(context, USER_DATABASE_NAME, null, 1);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "Creating user db");

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}// onupgrade

	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.d(TAG, "ENTERING DELETE, uri>>" + uri + "<<");
		SQLiteDatabase db = mMetaHelper.getWritableDatabase();
		int count = 0;
		// if there's nowhere to delete, we fail.
		if (uri == null) {
			throw new IllegalArgumentException(
					"uri and values must be specified");
		}

		int match = URL_MATCHER.match(uri);

		String tableName = null;
		switch (match) {

		case TABLES:

			break;

		case TABLES_ID:

			String id = uri.getLastPathSegment();
			Log.v(TAG, id);
			Cursor cursor = db.query(TABLE_META_TABLES,
					new String[] { DBBase.Tables.TABLE_NAME }, Tables._ID
							+ " = ? ", new String[] { id }, null, null, null);
			if (cursor.moveToNext()) {
				tableName = cursor.getString(0);
			}
			cursor.close();
			count = db.delete(TABLE_META_TABLES, "_id="
					+ id
					+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
							+ ')' : ""), selectionArgs);
			if (tableName != null) {
				mUserHelper.getWritableDatabase().execSQL(
						"DELETE TABLE " + tableName);
			}

			break;

		}

		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public boolean onCreate() {
		mUserHelper = new UserHelper(getContext());
		mMetaHelper = new MetaHelper(getContext(), mUserHelper);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		switch (URL_MATCHER.match(uri)) {
		case TABLES:

			// SQLiteDatabase db = mUserHelper.getReadableDatabase();
			// SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			// qb.setTables("SQLITE_MASTER");
			// qb.setProjectionMap(PROJECTION_MAP_META_MASTER);
			// Cursor cursor = qb.query(db, projection, selection,
			// selectionArgs,
			// null, null, sortOrder);
			// while (cursor.moveToNext()) {
			// Log.v(TAG, cursor.getString(0));
			// }
			// cursor = db.query("SQLITE_MASTER", new String[] { "name" },
			// selection, selectionArgs, null, null, sortOrder);

			SQLiteDatabase db = mMetaHelper.getReadableDatabase();
			SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
			qb.setTables(TABLE_META_TABLES);
			Cursor cursor = qb.query(db, projection, selection, selectionArgs,
					null, null, sortOrder);

			return cursor;

		case COLUMNS:

			db = mMetaHelper.getReadableDatabase();
			qb = new SQLiteQueryBuilder();
			qb.setTables(TABLE_META_COLUMNS);
			cursor = qb.query(db, projection, selection, selectionArgs, null,
					null, sortOrder);

			return cursor;

		case ROWS_ID:
			String tableId = uri.getPathSegments().get(1);
			cursor = mMetaHelper.getReadableDatabase().query(TABLE_META_TABLES,
					new String[] { Tables.TABLE_NAME }, Tables._ID + " = ?",
					new String[] { tableId }, null, null, null);
			String tableName = null;
			if (cursor.moveToFirst()) {
				tableName = cursor.getString(0);
			}
			cursor.close();
			db = mUserHelper.getReadableDatabase();
			cursor = db.query(tableName, new String[] { "*" }, selection,
					selectionArgs, null, null, sortOrder);
			return cursor;
		}

		return null;
	}

	static {

		URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URL_MATCHER.addURI("org.openintents.db", "", TABLES);
		URL_MATCHER.addURI("org.openintents.db", "tables", TABLES);
		URL_MATCHER.addURI("org.openintents.db", "tables/#", TABLES_ID);
		URL_MATCHER.addURI("org.openintents.db", "columns", COLUMNS);
		URL_MATCHER.addURI("org.openintents.db", "columns/#", COLUMNS_ID);
		URL_MATCHER.addURI("org.openintents.db", "tables/#/rows", ROWS);
		URL_MATCHER.addURI("org.openintents.db", "tables/#/rows/#", ROWS_ID);

		PROJECTION_MAP_META_MASTER.put(Tables.TABLE_NAME, "name");
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentvalues) {
		switch (URL_MATCHER.match(uri)) {
		case TABLES:
			SQLiteDatabase db = mMetaHelper.getWritableDatabase();

			long id = db.insert(TABLE_META_TABLES, Tables.TABLE_NAME,
					contentvalues);
			Uri newUri = Uri.withAppendedPath(Tables.CONTENT_URI, String
					.valueOf(id));
			getContext().getContentResolver().notifyChange(newUri, null);
			return newUri;

		case COLUMNS:
			// TODO check for table_id
			db = mMetaHelper.getWritableDatabase();
			id = db.insert(TABLE_META_COLUMNS, Columns.COL_NAME, contentvalues);
			newUri = Uri.withAppendedPath(Columns.CONTENT_URI, String
					.valueOf(id));
			getContext().getContentResolver().notifyChange(newUri, null);
			return newUri;
		case ROWS:
			String tableId = uri.getPathSegments().get(1);
			Cursor cursor = mMetaHelper.getReadableDatabase().query(
					TABLE_META_TABLES, new String[] { Tables.TABLE_NAME },
					Tables._ID + " = ?", new String[] { tableId }, null, null,
					null);
			String tableName = null;
			if (cursor.moveToFirst()) {
				tableName = cursor.getString(0);
			}
			cursor.close();

			cursor = mMetaHelper.getReadableDatabase().query(
					TABLE_META_COLUMNS, new String[] { Columns.COL_NAME },
					Tables._ID + " = ?", new String[] { tableId }, null, null,
					null);
			String nullColumn = null;
			if (cursor.moveToFirst()) {
				nullColumn = cursor.getString(0);
			}
			cursor.close();

			if (tableName != null) {
				db = mUserHelper.getWritableDatabase();
				long newId = db.insert(tableName, nullColumn, contentvalues);
				newUri = Tables.CONTENT_URI.buildUpon().appendPath(
						String.valueOf(tableId)).appendPath("rows").appendPath(
						String.valueOf(newId)).build();
			} else {
				newUri = null;
			}

			return newUri;
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues contentvalues, String s,
			String[] as) {

		switch (URL_MATCHER.match(uri)) {
		case TABLES_ID:
			if (uri.getQueryParameter(Tables.QUERY_CREATE_TABLE) != null) {
				SQLiteDatabase db = mUserHelper.getWritableDatabase();
				SQLiteDatabase metaDb = mMetaHelper.getReadableDatabase();
				Cursor cursor = metaDb.query(TABLE_META_TABLES,
						new String[] { Tables.TABLE_NAME },
						Tables._ID + " = ?", new String[] { uri
								.getLastPathSegment() }, null, null, null);
				if (cursor.moveToNext()) {
					StringBuilder sb = new StringBuilder();
					sb.append("CREATE TABLE " + cursor.getString(0) + "(");

					Cursor colCursor = metaDb
							.query(TABLE_META_COLUMNS, new String[] {
									Columns.COL_NAME, Columns.COL_TYPE },
									Columns.TABLE_ID + " = ?",
									new String[] { uri.getLastPathSegment() },
									null, null, null);
					while (colCursor.moveToNext()) {
						sb.append(colCursor.getString(0) + " "
								+ colCursor.getString(1) + ",");
					}
					colCursor.close();

					sb.append(BaseColumns._ID + " INTEGER PRIMARY KEY");
					sb.append(");");
					db.execSQL(sb.toString());
				}

				cursor.close();

			}
		}

		return 0;
	}

}/* eoc */
