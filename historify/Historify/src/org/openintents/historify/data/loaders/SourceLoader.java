package org.openintents.historify.data.loaders;

import org.openintents.historify.data.model.AbstractSource;
import org.openintents.historify.data.providers.Sources;
import org.openintents.historify.uri.ContentUris;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class SourceLoader {

	public static String[] PROJECTION = new String[] {
		Sources.SourcesTable._ID,
		Sources.SourcesTable.NAME,
		Sources.SourcesTable.IS_INTERNAL,
		Sources.SourcesTable.STATE
	};
	
	private static final int COLUMN_ID = 0;
	private static final int COLUMN_NAME = 1;
	private static final int COLUMN_IS_INTERNAL = 2;
	private static final int COLUMN_STATE = 3;

	
	public Cursor openCursor(Activity context) {
		return openCursor(context, null, null);
	}
	
	public Cursor openCursor(Activity context, String selection, String[] selectionArgs) {

		String sortOrder = Sources.SourcesTable.NAME;
		return context.getContentResolver().query(ContentUris.Sources, PROJECTION, selection, null, sortOrder);
	}
	
	public AbstractSource loadFromCursor(Cursor cursor, int position) {
		
		cursor.moveToPosition(position);
		
		return AbstractSource.factoryMethod(
				cursor.getInt(COLUMN_IS_INTERNAL)>0,
				cursor.getLong(COLUMN_ID), 
				cursor.getString(COLUMN_NAME), 
				cursor.getString(COLUMN_STATE));
		
	}

	public void update(Context context, AbstractSource source) {

		Uri sourceUri = ContentUris.Sources.buildUpon().appendPath(String.valueOf(source.getId())).build();
		
		ContentValues cv = new ContentValues();
		cv.put(Sources.SourcesTable.STATE, source.getState().toString());
		
		context.getContentResolver().update(sourceUri, cv, null, null);
	}

	
}
