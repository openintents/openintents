package org.openintents.historify.data.adapters;

import java.util.ArrayList;
import java.util.List;

import org.openintents.historify.R;
import org.openintents.historify.data.loaders.FilterLoader;
import org.openintents.historify.data.loaders.SourceLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.source.AbstractSource;
import org.openintents.historify.data.model.source.ExternalSource;
import org.openintents.historify.data.model.source.InternalSource;
import org.openintents.historify.data.model.source.AbstractSource.SourceState;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

public class SourcesAdapter extends BaseAdapter {

	private static final int HEADER_OFFSET = 1;
	private static final int VIEW_TYPE_HEADER = 0;
	private static final int VIEW_TYPE_ITEM = 1;
	
	private Activity mContext;

	private SourceLoader mSourceLoader;
	private FilterLoader mFilterLoader;
	private List<InternalSource> mInternalSources;
	private List<ExternalSource> mExternalSources;
	private SparseBooleanArray mCheckedItems;

	private Contact mFilterModeContact = null;
	private int mFilterModePosition = 0;
	
	public SourcesAdapter(Activity context, ListView listView) {

		mContext = context;
		mSourceLoader = new SourceLoader();
		mFilterLoader = new FilterLoader();
		mInternalSources = new ArrayList<InternalSource>();
		mExternalSources = new ArrayList<ExternalSource>();
		mCheckedItems = listView.getCheckedItemPositions();
	}

	public void load() {

		mInternalSources.clear();
		mExternalSources.clear();

		Cursor c = mSourceLoader.openCursor(mContext, mFilterModeContact);
		for (int i = 0; i < c.getCount(); i++) {
			AbstractSource source = mSourceLoader.loadFromCursor(c, i);
			if (source != null) {
				
				if(mFilterModeContact!=null && source.getSourceFilter()!=null) 
					source.getSourceFilter().setContact(mFilterModeContact);
				
				if (source.isInternal())
					mInternalSources.add((InternalSource) source);
				else
					mExternalSources.add((ExternalSource) source);
			}
		}
		c.close();
		
		notifyDataSetChanged();
	}

		
	public int getCount() {
		
		return mInternalSources.size() + mExternalSources.size() + 2 * HEADER_OFFSET;
		
	}

	public AbstractSource getItem(int position) {
		
		if(position==0 || position == mInternalSources.size()+HEADER_OFFSET) {
			return null;
		}
		else {
			return (position>mInternalSources.size()) ?  
				mExternalSources.get(position - mInternalSources.size() - 2 * HEADER_OFFSET) :
				mInternalSources.get(position - HEADER_OFFSET);
		}
		
	}

	public List<AbstractSource> getItems() {
		
		ArrayList<AbstractSource> retval = new ArrayList<AbstractSource>();
		retval.addAll(mInternalSources);
		retval.addAll(mExternalSources);
		
		return retval;
	}

	public long getItemId(int position) {
		AbstractSource item = getItem(position);
		return item==null ? -1 : item.getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		Integer viewType = getItemViewType(position);
		
		if(viewType == VIEW_TYPE_HEADER) { //list header
			
			if(convertView == null || !viewType.equals(convertView.getTag())) {
				convertView = ((LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(android.R.layout.preference_category, null);
			}
			
			((TextView)convertView).setText(position == 0 ? R.string.sources_internal_sources : R.string.sources_external_sources);
			
		} else { //list item
			
			if(convertView == null || !viewType.equals(convertView.getTag())) {
				convertView = ((LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(android.R.layout.simple_list_item_multiple_choice, null);
			}
			
			AbstractSource item = getItem(position);
			CheckedTextView ctv = ((CheckedTextView)convertView); 
			ctv.setText(item.getName());
			
			mCheckedItems.put(position, item.isEnabled());
		}
		return convertView;
	}
	
	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public int getItemViewType(int position) {

		if(position==0 || position == mInternalSources.size() + HEADER_OFFSET)
			return VIEW_TYPE_HEADER;
		else
			return VIEW_TYPE_ITEM;

	}
	
	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}
	
	@Override
	public boolean isEnabled(int position) {
		return getItemViewType(position)==VIEW_TYPE_ITEM;
	}

	public void update(AbstractSource source) {
		
		if(mFilterModeContact==null)
			mSourceLoader.update(mContext, source);
		else
			mFilterLoader.update(mContext,source.getSourceFilter());
	}

	public void setFilterMode(Contact contact, int position) {
		
		mFilterModeContact = contact;
		mFilterModePosition = position;
		
		load();
	}

	public int getFilterModePosition() {
		return mFilterModePosition;
	}

}
