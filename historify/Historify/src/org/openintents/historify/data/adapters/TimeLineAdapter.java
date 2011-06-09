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

import java.util.Date;

import org.openintents.historify.R;
import org.openintents.historify.data.aggregation.EventAggregator;
import org.openintents.historify.data.loaders.EventLoader;
import org.openintents.historify.data.loaders.SourceIconHelper;
import org.openintents.historify.data.loaders.SourceLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.Event;
import org.openintents.historify.data.model.source.AbstractSource;
import org.openintents.historify.uri.ContentUris;
import org.openintents.historify.utils.DateUtils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * Adapter for the timeline list.
 * 
 * @author berke.andras
 */
public class TimeLineAdapter extends BaseAdapter {

	private Activity mContext;
	
	private EventAggregator mAggregator;
	private SourceIconHelper mSourceIconHelper;
	
	/** Constructor */
	public TimeLineAdapter(Activity context, Contact contact) {

		mContext = context;		
		mAggregator = new EventAggregator(context, contact);
		mSourceIconHelper = new SourceIconHelper();
		load();
	}

	public void load() {
		mAggregator.query();
		notifyDataSetChanged();
	}

	public int getCount() {
		return mAggregator.getCount();
	}

	public Event getItem(int position) {
		return mAggregator.getItem(position);
	}

	public long getItemId(int position) {
		return getItem(position).getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		Event event = getItem(position);

		if (convertView == null) {
			convertView = ((LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.timeline_listitem, null);
		}

		loadEventToView(event, convertView);
		alignView(event, convertView);

		return convertView;
	}

	private void loadEventToView(Event event, View convertView) {


		TextView tv= (TextView) convertView
				.findViewById(R.id.timeline_listitem_txtMessage);
		tv.setText(event.getMessage());

		tv = (TextView) convertView
				.findViewById(R.id.timeline_listitem_txtDate);
		tv.setText(DateUtils
				.formatDate(new Date(event.getPublishedTime())));
		
		ImageView iv = (ImageView)convertView.findViewById(R.id.timeline_listitem_imgIcon);
		mSourceIconHelper.toImageView(mContext, event.getSource(), iv);
	}

	private void alignView(Event event, View convertView) {

		// align the view depending on the originator of the event
		View viewLeftSpacer1 = convertView
				.findViewById(R.id.timeline_listitem_vLeftSpacer1);
		View viewLeftSpacer2 = convertView
				.findViewById(R.id.timeline_listitem_vLeftSpacer2);
		View viewRightSpacer1 = convertView
				.findViewById(R.id.timeline_listitem_vRightSpacer1);
		View viewRightSpacer2 = convertView
				.findViewById(R.id.timeline_listitem_vRightSpacer2);

		switch (event.getOriginator()) {
		case user:

			viewLeftSpacer1.setVisibility(View.GONE);
			viewLeftSpacer2.setVisibility(View.GONE);
			viewRightSpacer1.setVisibility(View.VISIBLE);
			viewRightSpacer2.setVisibility(View.VISIBLE);
			break;

		case contact:

			viewLeftSpacer1.setVisibility(View.VISIBLE);
			viewLeftSpacer2.setVisibility(View.VISIBLE);
			viewRightSpacer1.setVisibility(View.GONE);
			viewRightSpacer2.setVisibility(View.GONE);
			break;

		case both:

			viewLeftSpacer1.setVisibility(View.VISIBLE);
			viewLeftSpacer2.setVisibility(View.GONE);
			viewRightSpacer1.setVisibility(View.VISIBLE);
			viewRightSpacer2.setVisibility(View.GONE);
			break;
		}

	}

}
