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

package org.openintents.samples.lendme.data;

import org.openintents.samples.lendme.R;
import org.openintents.samples.lendme.data.Item.Owner;
import org.openintents.samples.lendme.data.persistence.ItemsLoader;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * 
 * 
 * @author berke.andras
 */
public class ItemsAdapter extends BaseAdapter {

	private Activity mContext;

	private ItemsLoader mLoader;
	private Cursor mCursor;
	private Owner mFilterForOwner;

	private static final int ITEM_TYPE_ADD = 0;
	private static final int ITEM_TYPE_NORMAL = 1;
	
	
	public ItemsAdapter(Activity context, Owner filterForOwner) {

		mContext = context;
		mLoader = new ItemsLoader();
		mFilterForOwner = filterForOwner;
		load();
	}

	/** Open cursor. */
	public void load() {

		mCursor = mLoader.openCursor(mContext, mFilterForOwner);
		notifyDataSetChanged();
	}

	public int getCount() {
		return mCursor == null ? 1 : mCursor.getCount()+1;
	}

	public Item getItem(int position) {
		return mCursor == null ? null : mLoader.loadFromCursor(mCursor,
				position-1);
	}

	public long getItemId(int position) {
		return -1;
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}
	
	@Override
	public int getItemViewType(int position) {
		return position == 0 ? ITEM_TYPE_ADD : ITEM_TYPE_NORMAL;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {

				
		if (convertView == null || !convertView.getTag().equals(getItemViewType(position))) {
			
			if(position==0)
				convertView = ((LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.items_listitem_add, null);
			else
				convertView = ((LayoutInflater) mContext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
						.inflate(R.layout.items_listitem, null);
			
			convertView.setTag(Integer.valueOf(getItemViewType(position)));
		}
		
		
		if(position>0) {
			Item item = getItem(position);

			TextView tv = (TextView)convertView.findViewById(R.id.items_listitem_txtName);
			tv.setText(item.getName());
			
			String contactName = ContactOperations.loadContactName(mContext.getContentResolver(), item.getContactKey());
			String startDate = DateUtils.formatDateTime(mContext, item.getLendingStart(), 0); 
			
			tv = (TextView)convertView.findViewById(R.id.items_listitem_txtLendingStart);
			tv.setText(contactName+"\n"+startDate);
			
			tv = (TextView)convertView.findViewById(R.id.items_listitem_txtDescription);
			tv.setText(item.getDescription()==null ? "" : item.getDescription());
		}
		
		return convertView;
	}

	
	public long insert(Bundle parameterSet) {
		
		long retval = mLoader.insert(mContext, parameterSet);
		load();
		return retval;
	}

	public void delete(long itemId) {
		
		mLoader.delete(mContext, itemId);
		load();
	}
	
	public void close() {
		mCursor.close();
	}

	public Item getItemById(long itemId) {
		
		for(int i=1;i<getCount();i++) {
			if(getItem(i).getId()==itemId) return getItem(i);
		}
		return null;
	}

	public int getItemPosById(long itemId) {

		for(int i=1;i<getCount();i++) {
			if(getItem(i).getId()==itemId) return i;
		}
		return -1;
	}
}
