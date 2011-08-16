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

import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.source.EventSource.SourceState;
import org.openintents.historify.data.providers.Sources;
import org.openintents.historify.data.providers.Sources.FiltersTable;
import org.openintents.historify.data.providers.Sources.OpenHelper;
import org.openintents.historify.data.providers.Sources.SourcesTable;
import org.openintents.historify.uri.ContentUris;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * 
 * Helper class for executing database tasks related to source filters.
 * 
 * @author berke.andras
 */
public class SourceFilterOperation {

	/**
	 * Inserting source filters.
	 * 
	 * @param context
	 *            Context
	 * @param contactLookupKey
	 *            The lookup key of the contact the filters should be inserted
	 *            for.
	 * @param sourceIds
	 *            the ids of the sources the filters should be inserted for.
	 * @param states
	 *            the states of the newly inserted filters.
	 * @return <b>true</b> if successfully inserted, <b>false</b> otherwise.
	 */
	public boolean insertFilters(Context context, String contactLookupKey,
			long[] sourceIds, SourceState[] states) {

		ContentValues[] cvs = new ContentValues[sourceIds.length];

		// inserting filters for each source with default values
		for (int i = 0; i < cvs.length; i++) {
			ContentValues cv = new ContentValues();
			cv.put(FiltersTable.CONTACT_LOOKUP_KEY, contactLookupKey);
			cv.put(FiltersTable.SOURCE_ID, sourceIds[i]);
			cv.put(FiltersTable.FILTERED_STATE, states[i].toString());
			cvs[i] = cv;
		}

		int ret = context.getContentResolver().bulkInsert(ContentUris.Filters,
				cvs);
		return ret > 0;
	}

	/**
	 * Inserting filters for a newly added source.
	 * 
	 * @param context
	 *            context
	 * @param newSourceId
	 *            the id of newly added source.
	 * @param defaultState
	 *            the state of the newly added source.
	 * @return
	 */
	public boolean insertFiltersForNewSource(Context context, Long newSourceId,
			SourceState defaultState) {

		// load all contacts that have filters
		Set<String> lookupKeys = getAllLookupKeysWithFilters(context);

		ContentValues[] cvs = new ContentValues[lookupKeys.size()];

		// inserting filters for each contact with default value
		int i = 0;
		for (String lookupKey : lookupKeys) {
			ContentValues cv = new ContentValues();
			cv.put(FiltersTable.CONTACT_LOOKUP_KEY, lookupKey);
			cv.put(FiltersTable.SOURCE_ID, newSourceId);
			cv.put(FiltersTable.FILTERED_STATE, defaultState.toString());
			cvs[i] = cv;
			i++;
		}

		int ret = context.getContentResolver().bulkInsert(ContentUris.Filters,
				cvs);
		return ret > 0;
	}

	/**
	 * Gets the lookupkeys of contacts with filters defined.
	 * 
	 * @param context
	 * @return The list of lookup keys.
	 */
	private Set<String> getAllLookupKeysWithFilters(Context context) {

		Cursor c = context.getContentResolver().query(ContentUris.Filters,
				new String[] { FiltersTable.CONTACT_LOOKUP_KEY }, null, null,
				null);

		HashSet<String> lookupKeys = new HashSet<String>();

		while (c.moveToNext()) {
			lookupKeys.add(c.getString(0));
		}

		return lookupKeys;

	}

	/**
	 * Remove all filters defined for a particular source.
	 * 
	 * @param context
	 *            Context
	 * @param sourceId
	 *            The id of the source which has to be deleted.
	 */
	public void removeFiltersOfDeletedSource(Context context, long sourceId) {
		String where = FiltersTable.SOURCE_ID + " = " + sourceId;
		context.getContentResolver().delete(ContentUris.Filters, where, null);
	}

	/**
	 * Remove all filters defined for a particular contact
	 * 
	 * @param context
	 *            Context
	 * @param contact
	 *            The contact whose filters has to be deleted.
	 */
	public void removeFiltersOfContact(Context context, Contact contact) {
		String where = FiltersTable.CONTACT_LOOKUP_KEY + " = ?";
		String[] whereArgs = new String[] { contact.getLookupKey() };
		context.getContentResolver().delete(ContentUris.Filters, where,
				whereArgs);
	}

	/**
	 * Checks if has source filters.
	 * 
	 * @param context
	 *            Context
	 * @param contact
	 *            The contact that has to be checked.
	 * @return <b>true</b> if the contact has filters, <b>false</b> otherwise.
	 */
	public boolean hasFilters(Context context, Contact contact) {

		String[] projection = new String[] { FiltersTable.CONTACT_LOOKUP_KEY };
		String selection = Sources.FiltersTable.CONTACT_LOOKUP_KEY + " = ?";
		String[] selectionArgs = new String[] { contact.getLookupKey() };

		Cursor c = context.getContentResolver().query(ContentUris.Filters,
				projection, selection, selectionArgs, null);
		boolean retval = c.moveToNext();
		c.close();

		return retval;
	}

	/**
	 * Checks if the contact's source filters generate exactly the same set of
	 * events as the default source state list.
	 * 
	 * @param context
	 *            Context
	 * @param contact
	 *            The contact whose filters has to be checked.
	 * @return <b>true</b> if the contact's filters are equivalent to the
	 *         default states, <b>false</b> otherwise.
	 */
	public boolean filteredEqualsDefault(Context context, Contact contact) {
		return querySourceFilters(context, contact, "f."
				+ FiltersTable.FILTERED_STATE + " != s." + SourcesTable.STATE);
	}

	/**
	 * Checks if the contact's source filters are more strict than the default
	 * source state list.
	 * 
	 * @param context
	 *            Context
	 * @param contact
	 *            The contact whose filters has to be checked.
	 * @return <b>true</b> if the contact's has more than zero source filters
	 *         which are disabled while the original state of the source is
	 *         enabled, <b>false</b> otherwise.
	 */
	public boolean filteredMoreThanDefault(Context context, Contact contact) {
		return querySourceFilters(context, contact, "f."
				+ FiltersTable.FILTERED_STATE + " = '" + SourceState.DISABLED
				+ "' AND s." + SourcesTable.STATE + " = '"
				+ SourceState.ENABLED + "'");
	}

	/**
	 * Query the source filers of a given contact for a given selection. The
	 * function counts the rows that satisfy the selection criteria and returns
	 * true if the result set has at least one row.
	 * 
	 * @param context
	 *            Context
	 * @param contact
	 *            The contact whose filters has to be queried.
	 * @param selection
	 *            The selection criteria.
	 * @return <b>true</b> if the result set has more than zero rows,
	 *         <b>false</b> otherwise.
	 */
	private boolean querySourceFilters(Context context, Contact contact,
			String selection) {

		Sources.OpenHelper dbHelper = new OpenHelper(context);

		String q = "SELECT COUNT(*) FROM " + Sources.FiltersTable._TABLE
				+ " f JOIN " + Sources.SourcesTable._TABLE + " s ON s."
				+ SourcesTable._ID + " = f." + FiltersTable.SOURCE_ID
				+ " WHERE " + FiltersTable.CONTACT_LOOKUP_KEY + " = ? AND "
				+ selection;

		boolean retval = false;

		SQLiteDatabase db = dbHelper.getWritableDatabase();
		if (db != null) {
			Cursor c = dbHelper.getWritableDatabase().rawQuery(q,
					new String[] { contact.getLookupKey() });
			c.moveToFirst();
			retval = c.getInt(0) == 0;
			c.close();
			db.close();
		}

		return retval;
	}
}
