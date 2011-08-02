/* 
 * Copyright (C) 2011 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.historify.data.loaders;

import java.util.List;

import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.source.EventSource;
import org.openintents.historify.data.model.source.SourceFilter;
import org.openintents.historify.data.model.source.EventSource.SourceState;
import org.openintents.historify.data.providers.Sources;
import org.openintents.historify.data.providers.Sources.FiltersTable;
import org.openintents.historify.uri.ContentUris;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * 
 * Helper class for loading and updating {@link SourceFilter} objects, as well as inserting
 * new filters for a particular contact.
 * 
 * @author berke.andras
 */
public class SourceFilterLoader extends SourceLoader {

	public static String[] FILTERED_SOURCES_PROJECTION = new String[] {
		Sources.SourcesTable._ID,
		Sources.SourcesTable.NAME,
		Sources.SourcesTable.DESCRIPTION,
		Sources.SourcesTable.ICON_URI,
		Sources.SourcesTable.EVENT_INTENT,
		Sources.SourcesTable.ICON_LOADING_STRATEGY,
		Sources.SourcesTable.AUTHORITY,
		Sources.SourcesTable.CONFIG_INTENT,
		Sources.SourcesTable.IS_INTERNAL,
		Sources.SourcesTable.STATE,
		Sources.FiltersTable._ID,
		Sources.FiltersTable.FILTERED_STATE
	};

	
	private static final int COLUMN_FILTER_ID = 10;
	private static final int COLUMN_FILTERED_STATE = 11;

	private Contact mContact;
	
	public SourceFilterLoader(Contact contact) {
		
		Uri  uri = ContentUris.FilteredSources.buildUpon()
		.appendPath(contact.getLookupKey())
		.build();
	
		init(uri,FILTERED_SOURCES_PROJECTION);
		mContact = contact;
	}

	
	public boolean hasFilters(Context context) {

		String[] projection = new String[] { FiltersTable.CONTACT_LOOKUP_KEY }; 
		String selection = Sources.FiltersTable.CONTACT_LOOKUP_KEY + " = ?";
		String[] selectionArgs = new String[] {
			mContact.getLookupKey()	
		};
		
		Cursor c = context.getContentResolver().query(ContentUris.Filters,projection,selection,selectionArgs,null);
		boolean retval = c.moveToNext();
		c.close();
		
		return retval;
	}

	public boolean insertFiltersForContact(Context context, List<EventSource> sources) {
		
		long[] sourceIds = new long[sources.size()];
		SourceState[] defaultStates = new SourceState[sources.size()];

		for(int i=0;i<sourceIds.length;i++) {
			sourceIds[i] = sources.get(i).getId();
			defaultStates[i] = sources.get(i).getState();
		}

		return new SourceFilterOperation().insertFilters(context, mContact.getLookupKey(), sourceIds, defaultStates);
	}

	@Override
	public SourceFilter loadFromCursor(Cursor cursor, int position) {
		
		EventSource source = super.loadFromCursor(cursor, position);
		
		SourceState filteredState = null;
		if(!cursor.isNull(COLUMN_FILTERED_STATE)) {
			filteredState = SourceState.parseString(cursor.getString(COLUMN_FILTERED_STATE)); 
		}
						
		return new SourceFilter(cursor.getLong(COLUMN_FILTER_ID),source, filteredState);
		
	}

	@Override
	public void update(Context context, EventSource source) {

		SourceFilter filter = (SourceFilter)source;
		Uri filterUri =  Uri.withAppendedPath(ContentUris.Filters, String.valueOf(filter.getFilterId()));
		
		ContentValues cv = new ContentValues();
		cv.put(Sources.FiltersTable.FILTERED_STATE, filter.getFilteredState().toString());
		
		context.getContentResolver().update(filterUri, cv, null, null);
	}

}
