package org.openintents.testing.peli.contentprovider01a;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.ContentURI;

public class ContentProviderSystem01a extends ContentProvider {

	public static final ContentURI CONTENT_URI = ContentURI.create( "content://org.openintents.testing.peli.contentprovider01a");
	
	@Override
	public int delete(ContentURI arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(ContentURI arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ContentURI insert(ContentURI uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Cursor query(ContentURI uri, String[] projection, String selection,
			String[] selectionArgs, String groupBy, String having,
			String sortOrder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int update(ContentURI uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
