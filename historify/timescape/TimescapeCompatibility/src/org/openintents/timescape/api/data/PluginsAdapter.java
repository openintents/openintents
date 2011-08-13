/*
 * Copyright (C) 2010 Sony Ericsson Mobile Communications AB.
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
 * limitations under the License
 *
 */

package org.openintents.timescape.api.data;

import org.openintents.timescape.R;
import org.openintents.timescape.api.provider.EventStreamHelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class PluginsAdapter extends BaseAdapter {

	private Context mContext;
	private PluginLoader mLoader;
	
	private Cursor mCursor;
	private ContentObserver mObserver;
	
	
	public PluginsAdapter(Context context) {
		
		mContext = context;
		mLoader = new PluginLoader();
		
		load();
	}
	
	private void load() {
		
		if(mCursor!=null) {
			mCursor.close();
			mCursor = null;
		}
		
		if(mObserver!=null) {
			mContext.getContentResolver().unregisterContentObserver(mObserver);
			mObserver = null;
		}
		
		mCursor = mLoader.openCursor(mContext);
		
		mObserver = new ContentObserver(new Handler()) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				load();
			}
		};
		mContext.getContentResolver().registerContentObserver(EventStreamHelper.getUri(EventStreamHelper.PLUGINS_PATH), true, mObserver);
		
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return mCursor==null ? 0 : mCursor.getCount();
	}

	@Override
	public Plugin getItem(int pos) {
		return mLoader.loadFromCursor(mCursor, pos);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if(convertView==null) {
			convertView = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listitem_plugin, null);
		}

		convertView.setBackgroundResource(
				position % 2 == 0 ? R.drawable.listitem_background1 : R.drawable.listitem_background2);

		
		Plugin item = getItem(position);
		
		TextView tv = (TextView)convertView.findViewById(R.id.plugins_listitem_txtName);
		tv.setText(item.getName());
		
		tv = (TextView)convertView.findViewById(R.id.plugins_listitem_txtDescription);
		tv.setText(item.getConfigurationText());
		
		View btnMore = convertView.findViewById(R.id.plugins_listitem_btnMore);
		String confActivity = item.getConfigurationActivity();
		btnMore.setVisibility(confActivity==null ? View.INVISIBLE : View.VISIBLE);
		
		if(confActivity==null) {
			convertView.setTag(null);
		} else {
			String pkg = confActivity.substring(0, confActivity.indexOf("/"));
			String cls = confActivity.substring(confActivity.indexOf("/")+1);
			ComponentName componentName = new ComponentName(pkg, cls);
			convertView.setTag(componentName);	
		}
		
		ImageView imgIcon = (ImageView)convertView.findViewById(R.id.plugins_listitem_imgIcon);
		new PluginIconHelper().toImageView(mContext, item.getIconUri(), imgIcon);
		
		return convertView;
		
	}

	public void release() {
		
		if(mCursor!=null) {
			mCursor.close();
			mCursor = null;
		}
		
		if(mObserver!=null) {
			mContext.getContentResolver().unregisterContentObserver(mObserver);
			mObserver = null;
		}
	}
}
