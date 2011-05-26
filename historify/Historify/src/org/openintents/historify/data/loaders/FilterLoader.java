package org.openintents.historify.data.loaders;

import java.util.List;

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

public class FilterLoader {

	public static String[] PROJECTION = new String[] {
		Sources.FiltersTable._ID,
		Sources.FiltersTable.CONTACT_LOOKUP_KEY,
		Sources.FiltersTable.SOURCE_ID,
		Sources.FiltersTable.FILTERED_STATE
	};
	
	private static final int COLUMN_ID = 0;
	private static final int COLUMN_CONTACT_LOOKUP_KEY = 1;
	private static final int COLUMN_SOURCE_ID = 2;
	private static final int COLUMN_FILTERED_STATE = 3;

	public Cursor openCursor(Activity context, Contact contact) {
		
		String selection = Sources.FiltersTable.CONTACT_LOOKUP_KEY + " = '?'";
		String[] selectionArgs = new String[] {
			contact.getLookupKey()	
		};
		
		return openCursor(context, selection, selectionArgs);
	}
		
	public Cursor openCursor(Activity context, String selection, String[] selectionArgs) {

		return context.managedQuery(ContentUris.Filters, PROJECTION, selection, null, null);
	}
	
	public SourceFilter loadFromCursor(Cursor cursor, int position) {
		
		cursor.moveToPosition(position);
		
		return new SourceFilter(
				cursor.getLong(COLUMN_ID),
				SourceState.parseString(cursor.getString(COLUMN_FILTERED_STATE)));
		
	}

	public boolean insertFilters(Context context, Contact contact, List<AbstractSource> sources) {

		ContentValues[] cvs = new ContentValues[sources.size()]; 
		String contactLookupKey = contact.getLookupKey();
		
		//inserting filters for each source with default values
		for(int i=0;i<cvs.length;i++) {
			ContentValues cv = new ContentValues();
			cv.put(FiltersTable.CONTACT_LOOKUP_KEY, contactLookupKey);
			cv.put(FiltersTable.SOURCE_ID, sources.get(i).getId());
			cv.put(FiltersTable.FILTERED_STATE, sources.get(i).getState().toString());
			cvs[i] = cv;
		}
		
		int ret = context.getContentResolver().bulkInsert(ContentUris.Filters, cvs);
		return ret>0;
	}

	public void deleteFilters(Context context, Contact contact) {
		
		String where = FiltersTable.CONTACT_LOOKUP_KEY + " = ?";
		String[] selectionArgs = new String[] {contact.getLookupKey()};
		context.getContentResolver().delete(ContentUris.Filters, where, selectionArgs);
	}

	public String[] loadFilterModeLookupKeys(Context context) {
		
		Cursor c = context.getContentResolver().query(ContentUris.Filters, new String[] {
				FiltersTable.CONTACT_LOOKUP_KEY
		}, null, null, "GROUP BY "+FiltersTable.CONTACT_LOOKUP_KEY);
		
		String[] retval = new String[c.getCount()];
		for(int i=0;i<retval.length;i++) {
			c.moveToPosition(i);
			retval[i] = c.getString(0);
		}
		c.close();
		
		return retval;
	}

	public void update(Context context, SourceFilter filter) {
	
		Uri filterUri = ContentUris.Filters.buildUpon().appendPath(String.valueOf(filter.getId())).build();
		
		ContentValues cv = new ContentValues();
		cv.put(Sources.FiltersTable.FILTERED_STATE,filter.getFilteredState().toString());
		
		context.getContentResolver().update(filterUri, cv, null, null);
	}
	
}
