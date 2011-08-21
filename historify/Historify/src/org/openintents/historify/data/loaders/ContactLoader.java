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

import org.openintents.historify.R;
import org.openintents.historify.data.model.Contact;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;

/**
 * 
 * Helper class for loading {@link Contact} objects.
 * 
 * @author berke.andras
 */
public class ContactLoader {

	/**
	 * The loading strategy determines the selection and sort order used by the
	 * loader class while querying the content provider for contacts.<br/>
	 * <br/>
	 * Note that the selection is processed from two components: the selection
	 * provided by the concrete strategy and the selection for contact filtering
	 * realized by the abstract base class.
	 */
	public static abstract class LoadingStrategy {

		private String mFilterText = "";

		public String getSelection() {
			String strategySelection = getStrategySelection();

			if (mFilterText.equals("")) {
				return strategySelection;
			} else {
				StringBuilder selection = new StringBuilder(
						strategySelection == null ? "" : strategySelection
								+ " AND ");
				selection.append("(" + Contacts.DISPLAY_NAME + " LIKE ?");
				selection.append(" OR " + Contacts.DISPLAY_NAME + " LIKE ?)");
				return selection.toString();
			}
		}

		public String[] getSelectionArgs() {
			if (mFilterText.equals(""))
				return null;
			else
				return new String[] { mFilterText + "%",
						"% " + mFilterText + "%" };
		}

		public abstract String getStrategySelection();

		public String getStrategySortOrder() {
			return ContactsContract.Contacts.DISPLAY_NAME
					+ " COLLATE LOCALIZED ASC";
		}

		public String getFilterText() {
			return mFilterText;
		}

		public void setFilterText(String filterText) {
			this.mFilterText = filterText;
		}
	}

	/**
	 * Loading strategy for getting every contact.
	 */
	public static class SimpleLoadingStrategy extends LoadingStrategy {
		@Override
		public String getStrategySelection() {
			return null;
		}
	}

	/**
	 * Loading strategy for getting favorite contacts.
	 */
	public static class StarredContactsLoadingStrategy extends LoadingStrategy {
		@Override
		public String getStrategySelection() {
			return ContactsContract.Contacts.STARRED + " = '1'";
		}
	}

	/**
	 * Loading strategy for getting contacts identified by the given lookup
	 * keys.
	 */
	public static class FilteredContactsLoadingStrategy extends LoadingStrategy {

		private String[] filteredLookupKeys;

		public FilteredContactsLoadingStrategy(String filteredLookupKey) {
			this.filteredLookupKeys = new String[] { filteredLookupKey };
		}

		public FilteredContactsLoadingStrategy(String[] filteredLookupKeys) {
			this.filteredLookupKeys = filteredLookupKeys;
		}

		@Override
		public String getStrategySelection() {
			StringBuilder selection = new StringBuilder(
					ContactsContract.Contacts.LOOKUP_KEY + " IN (");
			for (String s : filteredLookupKeys) {
				selection.append('\'');
				selection.append(s);
				selection.append('\'');
				selection.append(',');
			}
			if (filteredLookupKeys.length != 0)
				selection.setCharAt(selection.length() - 1, ')');

			return selection.toString();
		}
	}

	/**
	 * Loading strategy for getting recently contacted persons.
	 */
	public static class RecentlyContactedLoadingStrategy extends
			LoadingStrategy {
		@Override
		public String getStrategySelection() {
			return ContactsContract.Contacts.LAST_TIME_CONTACTED + ">0";
		}

		@Override
		public String getStrategySortOrder() {
			return ContactsContract.Contacts.LAST_TIME_CONTACTED + " DESC";
		}
	}

	public static String[] PROJECTION = new String[] {
			ContactsContract.Contacts.LOOKUP_KEY,
			ContactsContract.Contacts.DISPLAY_NAME,
			/* ContactsContract.Contacts.STARRED, */
			ContactsContract.Contacts.LAST_TIME_CONTACTED };

	private static final int COLUMN_LOOKUP_KEY = 0;
	private static final int COLUMN_DISPLAYNAME = 1;
	// private static final int COLUMN_STARRED = 2;
	private static final int COLUMN_LAST_TIME_CONTACTED = 2;

	/**
	 * Opens a cursor based on the given loading strategy.
	 * 
	 * @param context
	 *            Activity context.
	 * @param loadingStrategy
	 *            the LoadingStrategy used for the query.
	 * @return The cursor containing the fields as defined in
	 *         {@link #PROJECTION}.
	 */
	public Cursor openManagedCursor(Activity context,
			LoadingStrategy loadingStrategy) {

		Uri uri = ContactsContract.Contacts.CONTENT_URI;
		String[] projection = PROJECTION;

		StringBuilder querySelection = new StringBuilder();
		querySelection.append(ContactsContract.Contacts.IN_VISIBLE_GROUP
				+ " = 1");
		String selection = loadingStrategy.getSelection();
		if (selection != null) {
			querySelection.append(" AND ");
			querySelection.append(selection);
		}

		String sortOrder = loadingStrategy.getStrategySortOrder();

		return context.managedQuery(uri, projection, querySelection.toString(),
				loadingStrategy.getSelectionArgs(), sortOrder);
	}

	/**
	 * Loads a Contact instance from the cursor.
	 * 
	 * @param cursor
	 * @param position
	 *            The position to load from.
	 * @return The new Contact instance.
	 */
	public Contact loadFromCursor(Cursor cursor, int position) {

		cursor.moveToPosition(position);
		return new Contact(cursor.getString(COLUMN_LOOKUP_KEY), cursor
				.getString(COLUMN_DISPLAYNAME), cursor
				.getLong(COLUMN_LAST_TIME_CONTACTED));

	}

	/**
	 * Loading a Contact from a given lookupkey.
	 * 
	 * @param context
	 *            Activity context.
	 * @param contactLookupKey
	 *            The lookupkey of the Contact to be loaded.
	 * @param withAdditionalData
	 *            If true, additional fields will be loaded using
	 *            {@link #loadAdditionalData(Context context, Contact contact)}
	 * @return The new Contact instance.
	 */
	public Contact loadFromLookupKey(Activity context, String contactLookupKey,
			boolean withAdditionalData) {

		Cursor cursor = openManagedCursor(context,
				new FilteredContactsLoadingStrategy(contactLookupKey));

		if (cursor == null || cursor.getCount() == 0) {
			return null;
		} else {
			Contact retval = loadFromCursor(cursor, 0);
			if (retval != null && withAdditionalData)
				loadAdditionalData(context, retval);
			return retval;
		}

	}

	/**
	 * Loading additional fields of the given Contact. Currently the additional
	 * data only includes the given name of the contact.
	 * 
	 * @param context
	 *            Activity context.
	 * @param contact
	 *            The contact whom the fields should be loaded for.
	 */
	public void loadAdditionalData(Context context, Contact contact) {
		// load given name if available

		Uri uri = ContactsContract.Data.CONTENT_URI;
		String[] projection = new String[] { ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME };
		String selection = ContactsContract.Data.LOOKUP_KEY + "= ? AND "
				+ ContactsContract.Data.MIMETYPE + "= ? ";
		String[] selectionArgs = new String[] {
				contact.getLookupKey(),
				ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE };

		Cursor c = context.getContentResolver().query(uri, projection,
				selection, selectionArgs, null);
		String givenName = null;
		if (c.moveToFirst())
			givenName = c.getString(0);
		c.close();

		contact.setGivenName(givenName == null ? context
				.getString(R.string.timeline_default_given_name) : givenName);
	}

	/**
	 * Gets the most recently contacted person.
	 * 
	 * @param context
	 *            Activity context.
	 * @return <b>null</b> if the contact history is empty, the lookup key of
	 *         the contact otherwise.
	 */
	public String getMostRecentlyContacted(Activity context) {

		Cursor c = openManagedCursor(context,
				new RecentlyContactedLoadingStrategy());

		if (c.moveToFirst()) {
			return c.getString(COLUMN_LOOKUP_KEY);
		}

		return null;
	}

	/**
	 * Checks if the given lookupkey exists in the provider.
	 * 
	 * @param context
	 *            Activity context.
	 * @param contactLookupKey
	 *            The lookupkey the client is searching for.
	 * @return <b>true</b> if the key exists in the Contacts provider,
	 *         <b>false</b> otherwise.
	 */
	public boolean exists(Activity context, String contactLookupKey) {

		Cursor cursor = openManagedCursor(context,
				new FilteredContactsLoadingStrategy(contactLookupKey));
		return cursor != null && cursor.getCount() != 0;
	}
}
