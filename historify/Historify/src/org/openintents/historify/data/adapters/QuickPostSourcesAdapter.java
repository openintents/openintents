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
import org.openintents.historify.data.model.source.AbstractSource;
import org.openintents.historify.ui.SourcesActivity;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * Adapter for the list of sources on {@link SourcesActivity}. Provides sources
 * and their filtered state for all contact, or a particular contact if filter
 * mode is set.
 * 
 * @author berke.andras
 */
public class QuickPostSourcesAdapter extends SourcesAdapter {

	/** Constructor. */
	public QuickPostSourcesAdapter(Activity context, ListView listView, Uri sourcesUri) {
		super(context, listView, true, sourcesUri);
	}

	
	public int getCount() {
		return mExternalSources.size() + 1 + (mExternalSources.isEmpty() ? 1 : 0);
	}

	@Override
	public int getItemViewType(int position) {

		if (position == 0)
			return VIEW_TYPE_HEADER;
		else if (position == 1 && mExternalSources.isEmpty()) {
			return VIEW_TYPE_NEED_MORE_MESSAGE;
		} else
			return VIEW_TYPE_ITEM;

	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position) == VIEW_TYPE_ITEM;
	}

	public long getItemId(int position) {
		AbstractSource item = getItem(position);
		return item == null ? -1 : item.getId();
	}

	public AbstractSource getItem(int position) {

		if (position == 0) {
				return null;
		} else {
			return (mExternalSources.isEmpty() ? null : mExternalSources.get(position-1));
		}

	}

	public List<AbstractSource> getItems() {

		ArrayList<AbstractSource> retval = new ArrayList<AbstractSource>();
		retval.addAll(mExternalSources);

		return retval;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		Integer viewType = getItemViewType(position);

		if (viewType == VIEW_TYPE_HEADER) { // list header

			if (convertView == null || !viewType.equals(convertView.getTag())) {
				convertView = ((LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(android.R.layout.preference_category, null);
			}
			((TextView) convertView).setText(R.string.sources_quickpost_sources);

		} else if (viewType == VIEW_TYPE_NEED_MORE_MESSAGE) { // message shown if
			// there are no
			// quickpost sources
			// detected

			if (convertView == null || !viewType.equals(convertView.getTag())) {
				convertView = ((LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.list_empty_view, null);
				convertView.setVisibility(View.VISIBLE);
			}

			((TextView) convertView)
					.setText(R.string.sources_no_quickpost_sources);

		} else { // list item

			if (convertView == null || !viewType.equals(convertView.getTag())) {
				convertView = ((LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.listitem_quickpost_source, null);
				
			}

			AbstractSource item = getItem(position);
			TextView tv = (TextView) convertView.findViewById(R.id.sources_listitem_txtName);
			tv.setText(item.getName());
						
			tv = (TextView) convertView.findViewById(R.id.sources_listitem_txtDescription);
			tv.setText(item.getDescription() == null ? "" : item.getDescription());

			ImageView iv = (ImageView)convertView.findViewById(R.id.sources_listitem_imgIcon);
			mSourceIconHelper.toImageView(mContext, item,null,iv);
										
		}

		convertView.setTag(viewType);
		return convertView;
	}

}
