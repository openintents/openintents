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
import org.openintents.historify.data.loaders.FilterLoader;
import org.openintents.historify.data.loaders.SourceIconHelper;
import org.openintents.historify.data.loaders.SourceLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.source.AbstractSource;
import org.openintents.historify.data.model.source.ExternalSource;
import org.openintents.historify.data.model.source.InternalSource;
import org.openintents.historify.ui.SourcesActivity;
import org.openintents.historify.ui.fragments.SourcesConfigurationFragment;
import org.openintents.historify.uri.ContentUris;
import org.openintents.historify.utils.URLHelper;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.text.Spannable;
import android.text.style.RelativeSizeSpan;
import android.text.style.URLSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.BufferType;

/**
 * 
 * Adapter for the list of sources on {@link SourcesActivity}. Provides sources
 * and their filtered state for all contact, or a particular contact if filter
 * mode is set.
 * 
 * @author berke.andras
 */
public class SourcesAdapter extends BaseAdapter {

	private static final int HEADER_OFFSET = 1;

	// different types of views in this adapter
	// HEADER is a section header displaying 'external'.
	// ITEM is a source.
	// NEED_MORE_MESSAGE is a textview shown as the very last list item
	protected static final int VIEW_TYPE_HEADER = 0;
	protected static final int VIEW_TYPE_ITEM = 1;
	protected static final int VIEW_TYPE_NEED_MORE_MESSAGE = 2;

	protected Activity mContext;

	private SourceLoader mSourceLoader;
	protected SourceIconHelper mSourceIconHelper;
	private FilterLoader mFilterLoader;

	protected List<AbstractSource> mInternalSources;
	protected List<AbstractSource> mExternalSources;

	// checked items (enabled sources), shared with listview
	private SparseBooleanArray mCheckedItems;

	// if not null, source filters for the given contact are loaded
	private Contact mFilterModeContact = null;
	private int mFilterModePosition = 0;
	
	private SourcesChangedObserver mObserver;
	
	private class SourcesChangedObserver extends ContentObserver {

		public SourcesChangedObserver(Handler handler) {
			super(handler);
		}
		
		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}
		
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			load();
		}
	}

	/** Constructor. */
	public SourcesAdapter(Activity context, ListView listView, boolean basicColumnsOnly, Uri sourcesUri) {

		mContext = context;
		mSourceLoader = new SourceLoader(basicColumnsOnly, sourcesUri);
		mSourceIconHelper = new SourceIconHelper();
		mFilterLoader = new FilterLoader();
		mInternalSources = new ArrayList<AbstractSource>();
		mExternalSources = new ArrayList<AbstractSource>();
		mCheckedItems = listView.getCheckedItemPositions();
		
		load();
	}

	/** Load external and internal sources with their state. */
	public void load() {

		if(mObserver!=null) {
			mContext.getContentResolver().unregisterContentObserver(mObserver);
			mObserver = null;
		}
			
		mInternalSources.clear();
		mExternalSources.clear();

		Cursor c = mSourceLoader.openCursor(mContext, mFilterModeContact);
		for (int i = 0; i < c.getCount(); i++) {
			AbstractSource source = mSourceLoader.loadFromCursor(c, i);
			if (source != null) {

				if (mFilterModeContact != null
						&& source.getSourceFilter() != null)
					source.getSourceFilter().setContact(mFilterModeContact);

				if (source.isInternal())
					mInternalSources.add((InternalSource) source);
				else
					mExternalSources.add((ExternalSource) source);
			}
		}
		
		c.close();

		mObserver = new SourcesChangedObserver(new Handler());
		mContext.getContentResolver().registerContentObserver(
				mFilterModeContact==null ? ContentUris.Sources : ContentUris.FilteredSources,true, mObserver);
		
		notifyDataSetChanged();
	}

	/** Update the enabled / disabled state of a source */
	public void update(AbstractSource source) {

		if (mFilterModeContact == null) // all contacts
			mSourceLoader.update(mContext, source);

		else
			// single contact
			mFilterLoader.update(mContext, source.getSourceFilter());
	}

	public void setFilterMode(Contact contact, int position) {

		mFilterModeContact = contact;
		mFilterModePosition = position;
		
	}

	public int getFilterModePosition() {
		return mFilterModePosition;
	}

	public int getCount() {

		return 
			mInternalSources.size() + 
			mExternalSources.size() + 2 * HEADER_OFFSET + 
			(mExternalSources.isEmpty() ? 1 : 0);

	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	@Override
	public int getItemViewType(int position) {

		if (position == mInternalSources.size())
			return VIEW_TYPE_HEADER;
		else if (position == mInternalSources.size() + HEADER_OFFSET + mExternalSources.size()) {
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
		return getItemViewType(position) != VIEW_TYPE_HEADER;
	}

	public long getItemId(int position) {
		AbstractSource item = getItem(position);
		return item == null ? -1 : item.getId();
	}

	public AbstractSource getItem(int position) {

		if (getItemViewType(position)!=VIEW_TYPE_ITEM) {
			return null;
		} else {
			return (position > mInternalSources.size()) ? mExternalSources
					.get(position - mInternalSources.size() - HEADER_OFFSET)
					: mInternalSources.get(position);
		}

	}

	public List<AbstractSource> getItems() {

		ArrayList<AbstractSource> retval = new ArrayList<AbstractSource>();
		retval.addAll(mInternalSources);
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

			((TextView) convertView)
					.setText(R.string.sources_external_sources);

		} else if (viewType == VIEW_TYPE_NEED_MORE_MESSAGE) { 

			if (convertView == null || !viewType.equals(convertView.getTag())) {
				convertView = ((LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.list_empty_view, null);
				convertView.setVisibility(View.VISIBLE);
			}

			TextView tv = ((TextView) convertView);
			String text = mContext.getString(R.string.sources_need_mode);
			tv.setText(text, BufferType.SPANNABLE);
			
			//style text
			//increase font size in line1
			((Spannable)tv.getText()).setSpan(new RelativeSizeSpan(2.0f), 0, text.indexOf('\n'), 0);
			//make last line url-like
			((Spannable)tv.getText()).setSpan(new URLSpan(new URLHelper().getMoreInfoURL()), text.lastIndexOf('\n')+1, text.length(), 0);
			
			convertView.setBackgroundResource(
					(position - 1) % 2 == 0 ? R.drawable.listitem_background1 : R.drawable.listitem_background2);


		} else { // list item

			if (convertView == null || !viewType.equals(convertView.getTag())) {
				convertView = ((LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.listitem_source, null);
				
				View btnMore = convertView.findViewById(R.id.sources_listitem_btnMore);
				btnMore.setOnClickListener(new SourcesConfigurationFragment.OnMoreButtonClickedListener());
			}

			AbstractSource item = getItem(position);
			int pos = item.isInternal() ? position : position - 1;
			convertView.setBackgroundResource(
					pos % 2 == 0 ? R.drawable.listitem_background1 : R.drawable.listitem_background2);

			
			TextView tv = (TextView) convertView.findViewById(R.id.sources_listitem_txtName);
			tv.setText(item.getName());
			
			((CheckedTextView)tv).setChecked(item.isEnabled());
			mCheckedItems.put(position, item.isEnabled());
			
			tv = (TextView) convertView.findViewById(R.id.sources_listitem_txtDescription);
			tv.setText(item.getDescription() == null ? "" : item.getDescription());

			ImageView iv = (ImageView)convertView.findViewById(R.id.sources_listitem_imgIcon);
			mSourceIconHelper.toImageView(mContext, item,null,iv);
			
			View btnMore = convertView.findViewById(R.id.sources_listitem_btnMore);
			if(item.getConfigIntent()==null) 
				btnMore.setVisibility(View.INVISIBLE);
			else  {
				btnMore.setVisibility(View.VISIBLE);
				btnMore.setTag(item.getConfigIntent());
			}
							
		}

		convertView.setTag(viewType);
		return convertView;
	}

	public void onDestroy() {
		if(mObserver!=null) {
			mContext.getContentResolver().unregisterContentObserver(mObserver);
			mObserver = null;	
		}
	}

}
