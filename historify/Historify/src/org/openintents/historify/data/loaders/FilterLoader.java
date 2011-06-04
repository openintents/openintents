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

/**
 * 
 * Helper class for loading and updating {@link Filter} objects, as well as inserting
 * new filters for a particular contact.
 * 
 * @author berke.andras
 */
public class FilterLoader {

	public static String[] PROJECTION = new String[] {
			Sources.FiltersTable._ID, Sources.FiltersTable.CONTACT_LOOKUP_KEY,
			Sources.FiltersTable.SOURCE_ID, Sources.FiltersTable.FILTERED_STATE };

	private static final int COLUMN_ID = 0;
	private static final int COLUMN_CONTACT_LOOKUP_KEY = 1;
	private static final int COLUMN_SOURCE_ID = 2;
	private static final int COLUMN_FILTERED_STATE = 3;

	public Cursor openCursor(Activity context, Contact contact) {

		String selection = Sources.FiltersTable.CONTACT_LOOKUP_KEY + " = ?";
		String[] selectionArgs = new String[] { contact.getLookupKey() };

		return openCursor(context, selection, selectionArgs);
	}

	public Cursor openCursor(Activity context, String selection,
			String[] selectionArgs) {

		return context.managedQuery(ContentUris.Filters, PROJECTION, selection,
				selectionArgs, null);
	}

	public SourceFilter loadFromCursor(Cursor cursor, int position) {

		cursor.moveToPosition(position);

		return new SourceFilter(cursor.getLong(COLUMN_ID), SourceState
				.parseString(cursor.getString(COLUMN_FILTERED_STATE)));

	}

	/** Update the state of a filter in the db. */
	public void update(Context context, SourceFilter filter) {

		Uri filterUri = ContentUris.Filters.buildUpon().appendPath(
				String.valueOf(filter.getId())).build();

		ContentValues cv = new ContentValues();
		cv.put(Sources.FiltersTable.FILTERED_STATE, filter.getFilteredState()
				.toString());

		context.getContentResolver().update(filterUri, cv, null, null);
	}

	/**
	 * Insert new filters for all given {@link AbstractSource} assigned to the
	 * given {@link Contact}. <br/>
	 * The filter state of the new filters will be set to the default state of
	 * the source.
	 * 
	 * @param context
	 *            Activity context.
	 * @param contact
	 *            The Contact to insert filters for.
	 * @param sources
	 *            The list of installed sources.
	 * @return True if filters have been successfully inserted.
	 */
	public boolean insertFilters(Context context, Contact contact,
			List<AbstractSource> sources) {

		ContentValues[] cvs = new ContentValues[sources.size()];
		String contactLookupKey = contact.getLookupKey();

		// inserting filters for each source with default values
		for (int i = 0; i < cvs.length; i++) {
			ContentValues cv = new ContentValues();
			cv.put(FiltersTable.CONTACT_LOOKUP_KEY, contactLookupKey);
			cv.put(FiltersTable.SOURCE_ID, sources.get(i).getId());
			cv.put(FiltersTable.FILTERED_STATE, sources.get(i).getState()
					.toString());
			cvs[i] = cv;
		}

		int ret = context.getContentResolver().bulkInsert(ContentUris.Filters,
				cvs);
		return ret > 0;
	}

	/**
	 * Delete all filters defined for the given {@link Contact}.
	 * 
	 * @param context
	 *            Context.
	 * @param contact
	 *            The Contact whose filters should be deleted.
	 */
	public void deleteFilters(Context context, Contact contact) {

		String where = FiltersTable.CONTACT_LOOKUP_KEY + " = ?";
		String[] selectionArgs = new String[] { contact.getLookupKey() };
		context.getContentResolver().delete(ContentUris.Filters, where,
				selectionArgs);
	}

	/**
	 * Queries the db to collect all contact lookup keys that have source
	 * filters defined for.
	 * 
	 * @param context
	 * @return The array of strings containing the lookup keys.
	 */
	public String[] loadFilterModeLookupKeys(Context context) {

		Cursor c = context.getContentResolver().query(ContentUris.Filters,
				new String[] { FiltersTable.CONTACT_LOOKUP_KEY }, null, null,
				"GROUP BY " + FiltersTable.CONTACT_LOOKUP_KEY);

		String[] retval = new String[c.getCount()];
		for (int i = 0; i < retval.length; i++) {
			c.moveToPosition(i);
			retval[i] = c.getString(0);
		}
		c.close();

		return retval;
	}

}
