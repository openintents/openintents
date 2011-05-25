package org.openintents.historify.data.providers;

import org.openintents.historify.uri.ContentUris;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class SourcesProvider extends ContentProvider {

	public static final String NAME = "SourcesProvider";

	private static final UriMatcher sUriMatcher;
	private static final int SOURCES = 1;
	private static final int SOURCE_ID = 2;
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(ContentUris.SOURCES_AUTHORITY,
				Sources.SourcesTable._TABLE, SOURCES);
		sUriMatcher.addURI(ContentUris.SOURCES_AUTHORITY, "sources/#",
				SOURCE_ID);
	}

	private Sources.OpenHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new Sources.OpenHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		return Sources.SourcesTable.CONTENT_TYPE;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		switch (sUriMatcher.match(uri)) {

		case SOURCES:
			break;

		case SOURCE_ID:
			if (selection != null) {
				selection += " AND";
			} else {
				selection = "";
			}
			selection += " " + Sources.SourcesTable._ID + " = "
					+ uri.getPathSegments().get(1);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		return db.query(Sources.SourcesTable._TABLE, projection, selection,
				selectionArgs, null, null, sortOrder);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		switch (sUriMatcher.match(uri)) {

		case SOURCES:
			break;

		case SOURCE_ID:
			if (selection != null) {
				selection += " AND";
			} else {
				selection = "";
			}
			selection += " " + Sources.SourcesTable._ID + " = "
					+ uri.getPathSegments().get(1);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		return db.update(Sources.SourcesTable._TABLE, values, selection,
				selectionArgs);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

}
