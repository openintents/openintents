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

import java.util.HashSet;
import java.util.Set;

import org.openintents.historify.data.model.source.EventSource.SourceState;
import org.openintents.historify.data.providers.Sources.FiltersTable;
import org.openintents.historify.uri.ContentUris;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

public class SourceFilterOperation {

	public boolean insertFilters(Context context, String contactLookupKey, long[] sourceIds, SourceState[] defaultStates) {

		ContentValues[] cvs = new ContentValues[sourceIds.length];

		// inserting filters for each source with default values
		for (int i = 0; i < cvs.length; i++) {
			ContentValues cv = new ContentValues();
			cv.put(FiltersTable.CONTACT_LOOKUP_KEY, contactLookupKey);
			cv.put(FiltersTable.SOURCE_ID, sourceIds[i]);
			cv.put(FiltersTable.FILTERED_STATE, defaultStates[i].toString());
			cvs[i] = cv;
		}

		int ret = context.getContentResolver().bulkInsert(ContentUris.Filters,cvs);
		return ret > 0;
	}
	
	private Set<String> getAllLookupKeysWithFilters(Context context) {
		
		Cursor c = context.getContentResolver().query(ContentUris.Filters, new String[] { FiltersTable.CONTACT_LOOKUP_KEY }, null, null,null);

		HashSet<String> lookupKeys = new HashSet<String>();
		
		while(c.moveToNext()) {
			lookupKeys.add(c.getString(0));
		}
		
		return lookupKeys;

	}

	public boolean insertFiltersForNewSource(Context context,
			Long newSourceId, SourceState defaultState) {
		
		Set<String> lookupKeys = getAllLookupKeysWithFilters(context);
		
		ContentValues[] cvs = new ContentValues[lookupKeys.size()];

		// inserting filters for each contact with default value
		int i=0;
		for(String lookupKey : lookupKeys) {
			ContentValues cv = new ContentValues();
			cv.put(FiltersTable.CONTACT_LOOKUP_KEY, lookupKey);
			cv.put(FiltersTable.SOURCE_ID, newSourceId);
			cv.put(FiltersTable.FILTERED_STATE, defaultState.toString());
			cvs[i] = cv;
			i++;
		}
		
		int ret = context.getContentResolver().bulkInsert(ContentUris.Filters, cvs);
		return ret > 0;
	}


	public void removeFiltersOfDeletedSource(Context context, long sourceId) {
		String where = FiltersTable.SOURCE_ID + " = "+sourceId;
		context.getContentResolver().delete(ContentUris.Filters, where, null);
	}

}
