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
import java.util.List;

import org.openintents.historify.R;
import org.openintents.historify.data.loaders.SourceLoader;
import org.openintents.historify.data.model.source.EventSource;
import org.openintents.historify.uri.ContentUris;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Customized source adapter for loading sources provided by the
 * {@link QuickPostsProvider} content provider which stores the sources of
 * QuickPost events.
 * 
 * @author berke.andras
 */
public class QuickPostSourcesAdapter extends SourcesAdapter {

	/** Constructor. */
	public QuickPostSourcesAdapter(Activity context, ListView listView) {
		super(context, listView, new SourceLoader(ContentUris.QuickPostSources,
				SourceLoader.BASIC_COLUMNS_PROJECTION),
				R.layout.listitem_quickpost_source);
	}

	/**
	 * Gets the set of loaded items.
	 * 
	 * @return List of all sources loaded.
	 */
	public List<EventSource> getItems() {

		ArrayList<EventSource> retval = new ArrayList<EventSource>();
		retval.addAll(mExternalSources);
		return retval;
	}

	// ---------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------
	// STANDARD ADAPTER METHODS
	// ---------------------------------------------------------------------------------

	public int getCount() {
		return mExternalSources.size();
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	public long getItemId(int position) {
		EventSource item = getItem(position);
		return item == null ? -1 : item.getId();
	}

	public EventSource getItem(int position) {
		return mExternalSources.get(position);
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		if (convertView == null) {
			convertView = ((LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.listitem_quickpost_source, null);

		}

		convertView
				.setBackgroundResource(position % 2 == 0 ? R.drawable.listitem_background1
						: R.drawable.listitem_background2);

		EventSource item = getItem(position);
		TextView tv = (TextView) convertView
				.findViewById(R.id.sources_listitem_txtName);
		tv.setText(item.getName());

		tv = (TextView) convertView
				.findViewById(R.id.sources_listitem_txtDescription);
		tv.setText(item.getDescription() == null ? "" : item.getDescription());

		ImageView iv = (ImageView) convertView
				.findViewById(R.id.sources_listitem_imgIcon);
		mSourceIconHelper.toImageView(mContext, item, null, iv);

		return convertView;
	}

}
