package org.openintents.historify.data.loaders;

import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.source.AbstractSource;
import org.openintents.historify.data.model.source.SourceFilter;
import org.openintents.historify.data.model.source.AbstractSource.SourceState;
import org.openintents.historify.data.providers.Sources;
import org.openintents.historify.data.providers.Sources.FiltersTable;
import org.openintents.historify.uri.ContentUris;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class SourceLoader {

	public static String[] SOURCES_PROJECTION = new String[] {
		Sources.SourcesTable._ID,
		Sources.SourcesTable.NAME,
		Sources.SourcesTable.IS_INTERNAL,
		Sources.SourcesTable.STATE, 
	};
	
	public static String[] FILTERED_SOURCES_PROJECTION = new String[] {
		Sources.SourcesTable._ID,
		Sources.SourcesTable.NAME,
		Sources.SourcesTable.IS_INTERNAL,
		Sources.SourcesTable.STATE, 
		Sources.FiltersTable._ID,
		Sources.FiltersTable.FILTERED_STATE
	};
	
	
	private static final int COLUMN_ID = 0;
	private static final int COLUMN_NAME = 1;
	private static final int COLUMN_IS_INTERNAL = 2;
	private static final int COLUMN_STATE = 3;
	private static final int COLUMN_FILTER_ID = 4;
	private static final int COLUMN_FILTERED_STATE = 5;

	
	public Cursor openCursor(Activity context, Contact filterModeContact) {
		
		String selection = null;
		String[] selectionArgs = null;
		
		Uri uri = null;
		String[] projection = null;
		
		if(filterModeContact!=null) {
			selection = FiltersTable.CONTACT_LOOKUP_KEY + " = ?";
			selectionArgs = new String[] {filterModeContact.getLookupKey()};
			
			uri = ContentUris.FilteredSources;
			projection = FILTERED_SOURCES_PROJECTION;
			
		} else {
			
			uri = ContentUris.Sources;
			projection = SOURCES_PROJECTION;
		}
				 
		return context.getContentResolver().query(uri, projection, selection, selectionArgs, Sources.SourcesTable.NAME);
	}
	
	
	public AbstractSource loadFromCursor(Cursor cursor, int position) {
		
		cursor.moveToPosition(position);
				
		AbstractSource retval = AbstractSource.factoryMethod(
				cursor.getInt(COLUMN_IS_INTERNAL)>0,
				cursor.getLong(COLUMN_ID), 
				cursor.getString(COLUMN_NAME), 
				cursor.getString(COLUMN_STATE));
		
		long filterId = cursor.isNull(COLUMN_FILTER_ID) ? -1 : cursor.getLong(COLUMN_FILTER_ID);
		if(filterId>-1) {
			SourceFilter filter = new SourceFilter(
					filterId, 
					SourceState.parseString(cursor.getString(COLUMN_FILTERED_STATE)));
			filter.setSource(retval);
			retval.setSourceFilter(filter);
		}
		
		return retval;
		
	}

	public void update(Context context, AbstractSource source) {

		Uri sourceUri = ContentUris.Sources.buildUpon().appendPath(String.valueOf(source.getId())).build();
		
		ContentValues cv = new ContentValues();
		cv.put(Sources.SourcesTable.STATE, source.getState().toString());
		
		context.getContentResolver().update(sourceUri, cv, null, null);
	}

	
}
