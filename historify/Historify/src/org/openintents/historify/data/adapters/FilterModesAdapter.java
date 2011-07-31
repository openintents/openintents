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

package org.openintents.historify.data.adapters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openintents.historify.data.loaders.ContactLoader;
import org.openintents.historify.data.loaders.FilterLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.ui.SourcesActivity;

import android.app.Activity;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 
 * Adapter for {@link SourcesActivity} filter modes spinner. <br/>
 * The adapter provides items the following filter modes:<br/>
 * - All Contacts (Default item)<br/>
 * - Items for each Contact that has source filters.<br/>
 * - Add new Contact item (Special item)<br/>
 * 
 * @author berke.andras
 */
public class FilterModesAdapter extends BaseAdapter {

	private Activity mContext;
	private FilterLoader mLoader;

	// items for contacts that have source filters
	private List<Contact> mItems;
	// special items display string
	private final String DEFAULT, NEW;

	/** Constructor. */
	public FilterModesAdapter(Activity context) {

		mContext = context;
		mLoader = new FilterLoader();

		mItems = new ArrayList<Contact>();
		DEFAULT = "";//mContext.getString(R.string.sources_filter_mode_default);
		NEW = "";//mContext.getString(R.string.sources_filter_mode_new);

		load();
	}

	/** Load items. */
	private void load() {

		mItems.clear();

		// load every contact that has filters.
		String[] contactLookupKeys = mLoader.loadFilterModeLookupKeys(mContext);
		if (contactLookupKeys.length != 0) {
			ContactLoader contactLoader = new ContactLoader();
			Cursor cursor = contactLoader.openCursor(mContext, new ContactLoader.FilteredContactsLoadingStrategy(contactLookupKeys));

			for (int i = 0; i < cursor.getCount(); i++) {
				Contact contact = contactLoader.loadFromCursor(cursor, i);
				if (contact != null)
					mItems.add(contact);
			}
		}

		notifyDataSetChanged();
	}

	/**
	 * Insert new filters for a contact with default filter state.
	 * 
	 * @return The position of the new Contact in the filter modes adapter.
	 * */
	public int insert(Contact contact, SourcesAdapter mSourcesAdapter) {

		boolean succ = mLoader.insertFilters(mContext, contact, mSourcesAdapter
				.getItems());
		if (succ) {

			// successfully inserted
			mItems.add(contact);
			Collections.sort(mItems, new Contact.Comparator());

			notifyDataSetChanged();

			for (int i = 0; i < mItems.size(); i++) {
				if (mItems.get(i).equals(contact))
					return i + 1;
			}
		}
		return -1;
	}

	/** Insert all filters for a contact. */
	public void delete(int actFilterModePosition) {

		mLoader.deleteFilters(mContext, mItems.get(actFilterModePosition - 1));
		mItems.remove(actFilterModePosition - 1);
		notifyDataSetChanged();
	}

	/**
	 * Checks if the default filter mode is in the given position.
	 * @param position
	 * @return True, if the default filter mode is in the given position. 
	 */
	public boolean isDefault(int position) {
		return position == 0;
	}

	/**
	 * Checks if the 'add new filter mode' element is in the given position.
	 * @param position
	 * @return True, if the 'add new filter mode' element is in the given position. 
	 */
	public boolean isAddNew(int position) {
		return position == mItems.size() + 1;
	}

	public long getItemId(int position) {
		return -1;
	}

	public Contact getItem(int position) {
		return (!isDefault(position) && !isAddNew(position)) ? mItems
				.get(position - 1) : null;
	}

	public List<Contact> getItems() {
		List<Contact> retval = new ArrayList<Contact>();
		retval.addAll(mItems);
		return retval;
	}

	public int getCount() {
		return 2 + mItems.size();
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = mContext.getLayoutInflater().inflate(
					android.R.layout.simple_spinner_item, null);
		}

		setItemText((TextView) convertView, position);
		return convertView;
	}

	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = mContext.getLayoutInflater().inflate(
					android.R.layout.simple_spinner_dropdown_item, null);
		}

		setItemText((TextView) convertView, position);
		return convertView;
	}

	/**
	 * Sets the display text of the list item.
	 * @param txtItem The view item.
	 * @param position The position of the item in the adapter.
	 */
	private void setItemText(TextView txtItem, int position) {

		if (isDefault(position))
			txtItem.setText(DEFAULT);
		else if (isAddNew(position))
			txtItem.setText(NEW);
		else
			txtItem.setText(mItems.get(position - 1).getName());
	}

}
