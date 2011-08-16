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

import org.openintents.historify.R;
import org.openintents.historify.data.loaders.SourceFilterLoader;
import org.openintents.historify.data.loaders.SourceFilterOperation;
import org.openintents.historify.data.loaders.SourceLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.source.EventSource;
import org.openintents.historify.data.model.source.EventSource.SourceState;
import org.openintents.historify.uri.ContentUris;

import android.content.Context;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Customized source adapter for loading source filters for a particular
 * contact. <br/>
 * <br/>
 * Note that if a contact hasn't got filters yet, the adapter works as a simple
 * {@link SourcesAdapter}. If the user changes the state of a source, or already
 * has filters, the {@link SourceFilterLoader} class will be used as a loader
 * instead of the default {@link SourceLoader}.
 * 
 * @author berke.andras
 */
public class SourceFiltersAdapter extends SourcesAdapter {

	private Contact mContact;
	private boolean mHasFilters;

	private SourceFilterLoader mSourceFilterLoader;
	private SourceLoader mDefaultSourceLoader;

	public SourceFiltersAdapter(Context context, ListView listView,
			Contact contact) {
		super();

		mContact = contact;
		mSourceFilterLoader = new SourceFilterLoader(contact);
		mDefaultSourceLoader = new SourceLoader(ContentUris.Sources);

		// check if contact has previously defined filters
		mHasFilters = mSourceFilterLoader.hasFilters(context);

		// If the current contact hasnt got any filters, the default values will
		// be shown.
		// We use a simple SourceLoader for that purpose.
		// If the user modifies a list element, the loader will be changed to
		// the SourceFilterLoader instance.
		init(context, listView, mHasFilters ? mSourceFilterLoader
				: mDefaultSourceLoader, R.layout.listitem_source_filter);
	}

	/** Update the enabled / disabled state of a source */
	@Override
	public void update(EventSource source) {

		if (!mHasFilters) {
			// create filters if hasnt got yet
			mSourceFilterLoader.insertFiltersForContact(mContext, mSources);
			mHasFilters = true;
			mSourceLoader = mSourceFilterLoader;
			load();
		} else {
			super.update(source);
		}

	}

	/** Update the enabled / disabled state of all sources */
	@Override
	public void updateAll(SourceState newState) {

		if (!mHasFilters) {
			// create filters if hasnt got yet
			mSourceFilterLoader.insertFiltersForContact(mContext, mSources);
			mHasFilters = true;
			mSourceLoader = mSourceFilterLoader;
			load();
		} else {
			super.updateAll(newState);
		}
	}

	/**
	 * Restores default behaviour, source filters will be removed for the
	 * contact.
	 */
	public void deleteFilters() {

		if (mHasFilters) {
			new SourceFilterOperation().removeFiltersOfContact(mContext,
					mContact);
			mHasFilters = false;
			mSourceLoader = mDefaultSourceLoader;
			load();
		}
	}

	// ---------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------
	// STANDARD ADAPTER METHODS
	// ---------------------------------------------------------------------------------

	@Override
	public int getCount() {
		return super.getCount() - 1;
	}

	@Override
	protected void initListItem(View convertView) {

	}

	@Override
	protected void loadItemToView(View convertView, EventSource item,
			int position) {

		TextView tv = (TextView) convertView
				.findViewById(R.id.sources_listitem_txtName);
		tv.setText(item.getName());

		((CheckedTextView) tv).setChecked(item.isEnabled());
		mCheckedItems.put(position, item.isEnabled());

		ImageView iv = (ImageView) convertView
				.findViewById(R.id.sources_listitem_imgIcon);
		mSourceIconHelper.toImageView(mContext, item, null, iv);

	}
}
