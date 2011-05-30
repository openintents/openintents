package org.openintents.historify.data.providers;

import org.openintents.historify.data.providers.Sources.FilteredSourcesView;
import org.openintents.historify.data.providers.Sources.FiltersTable;
import org.openintents.historify.data.providers.Sources.OpenHelper;
import org.openintents.historify.data.providers.Sources.SourcesTable;
import org.openintents.historify.uri.ContentUris;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class SourcesProvider extends ContentProvider {

	public static final String NAME = "SourcesProvider";

	private static final UriMatcher sUriMatcher;
	private static final int SOURCES = 1;
	private static final int SOURCE_ID = 2;
	private static final int FILTERED_SOURCES = 3;
	private static final int FILTERS = 4;
	private static final int FILTER_ID = 5;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		sUriMatcher.addURI(ContentUris.SOURCES_AUTHORITY,
				SourcesTable._TABLE, SOURCES);
		sUriMatcher.addURI(ContentUris.SOURCES_AUTHORITY, SourcesTable._TABLE+"/#",
				SOURCE_ID);
		sUriMatcher.addURI(ContentUris.SOURCES_AUTHORITY, FilteredSourcesView._VIEW,
				FILTERED_SOURCES);
		sUriMatcher.addURI(ContentUris.SOURCES_AUTHORITY,
				FiltersTable._TABLE, FILTERS);
		sUriMatcher.addURI(ContentUris.SOURCES_AUTHORITY, FiltersTable._TABLE+"/#",
				FILTER_ID);
	}

	private OpenHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new OpenHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case SOURCES:
			return SourcesTable.CONTENT_TYPE; 
		case SOURCE_ID:
			return SourcesTable.ITEM_CONTENT_TYPE;
		case FILTERED_SOURCES:
			return FilteredSourcesView.CONTENT_TYPE;
		case FILTERS:
			return FiltersTable.CONTENT_TYPE;
		case FILTER_ID:
			return FiltersTable.ITEM_CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		switch (sUriMatcher.match(uri)) {

		case SOURCES:
			qb.setTables(SourcesTable._TABLE);
			break;
		
		case FILTERS:
			qb.setTables(FiltersTable._TABLE);
			break;

		case FILTERED_SOURCES:
			qb.setTables(FilteredSourcesView.JOIN_CLAUSE);
			break;
			
		case SOURCE_ID:
			qb.setTables(SourcesTable._TABLE);
			qb.appendWhere(SourcesTable._ID + " = "+uri.getPathSegments().get(1));
			break;
			
		case FILTER_ID:
			qb.setTables(FiltersTable._TABLE);
			qb.appendWhere(FiltersTable._ID + " = "+uri.getPathSegments().get(1));
			break;
			
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		return qb.query(db, projection, selection, selectionArgs, null, null, null);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {

		String table, where = null;
		
		switch (sUriMatcher.match(uri)) {

		case SOURCES:
			table = SourcesTable._TABLE;
			break;
			
		case FILTERS:
			table = FiltersTable._TABLE;
			break;

		case SOURCE_ID:
			table = SourcesTable._TABLE;
			where = SourcesTable._ID + " = "+uri.getPathSegments().get(1);
			break;
			
		case FILTER_ID:
			table = FiltersTable._TABLE;
			where = FiltersTable._ID + " = "+uri.getPathSegments().get(1);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		if(where!=null) {
			if(selection==null) selection = where;
			else selection+=" AND "+where;
		}
		
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		return db.update(table, values, selection,
				selectionArgs);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		
		String table = null;
		
		switch (sUriMatcher.match(uri)) {

//		case SOURCES:
//			table = SourcesTable._TABLE;
//			break;
			
		case FILTERS:
			table = FiltersTable._TABLE;
			break;

//		case SOURCE_ID:
//			table = SourcesTable._TABLE;
//			where = BaseColumns._ID + " = "+uri.getPathSegments().get(1);
//			break;
//			
//		case FILTER_ID:
//			table = FiltersTable._TABLE;
//			where = BaseColumns._ID + " = "+uri.getPathSegments().get(1);
//			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		
		if(table!=null) {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			return db.delete(table, selection, selectionArgs);
		}

		return 0;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {

		String table = null;
		
		switch (sUriMatcher.match(uri)) {

//		case SOURCES:
//			table = SourcesTable._TABLE;
//			break;
			
		case FILTERS:
			table = FiltersTable._TABLE;
			break;

//		case SOURCE_ID:
//			table = SourcesTable._TABLE;
//			where = BaseColumns._ID + " = "+uri.getPathSegments().get(1);
//			break;
//			
//		case FILTER_ID:
//			table = FiltersTable._TABLE;
//			where = BaseColumns._ID + " = "+uri.getPathSegments().get(1);
//			break;

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		
		if(table!=null) {
			SQLiteDatabase db = mOpenHelper.getWritableDatabase();
			long id = db.insert(table, null, values);
			if(id>0) {
				return uri.buildUpon().appendPath(String.valueOf(id)).build();
			}
		}
		
		return null;
	}

}
