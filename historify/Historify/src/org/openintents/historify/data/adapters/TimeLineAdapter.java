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
import org.openintents.historify.data.loaders.SourceFilterOperation;
import org.openintents.historify.data.loaders.SourceIconHelper;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.Event;
import org.openintents.historify.preferences.Pref;
import org.openintents.historify.preferences.PreferenceManager;
import org.openintents.historify.utils.DateUtils;
import org.openintents.historify.utils.PrettyTimeRefreshHelper;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * Adapter for the list of events shown on the timeline.
 * 
 * @author berke.andras
 */
public class TimeLineAdapter extends BaseAdapter {

	private Activity mContext;

	private EventAggregator mAggregator;
	private SourceIconHelper mSourceIconHelper;

	private View mFilteredWarningView;

	private String mTimeLineThemeSetting;
	private int mTimeLineItemResId;

	private PrettyTimeRefreshHelper mPrettyTimeRefreshHelper;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            Activity context.
	 * @param contact
	 *            The contact whom the timeline is displayed for.
	 * @param filteredWarningView
	 *            View shown if the currently used source filters are more
	 *            strict that the default source states.
	 */
	public TimeLineAdapter(Activity context, Contact contact,
			View filteredWarningView) {

		mContext = context;
		mAggregator = new EventAggregator(context, this, contact);
		mSourceIconHelper = new SourceIconHelper();
		mFilteredWarningView = filteredWarningView;

		mPrettyTimeRefreshHelper = new PrettyTimeRefreshHelper();
		mPrettyTimeRefreshHelper.startRefresher(this);
		load();
	}

	/**
	 * Loading data. Opens cursor. Registers content observer.
	 */
	public void load() {
		mAggregator.query();
		notifyDataSetChanged();

		if (mFilteredWarningView != null) {
			mFilteredWarningView
					.setVisibility(new SourceFilterOperation()
							.filteredMoreThanDefault(mContext, mAggregator
									.getContact()) ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Temporary disables the content observer to avoid flood of refresh events
	 * in case of bulk update / insert.
	 */
	public void disableObserver() {
		mAggregator.unregisterObserver();
	}

	/**
	 * Enables the observer and sends a notification of change after a
	 * successful bulk update / insert.
	 */
	public void enableObserverAndNotify() {
		mAggregator.registerObserver();
		mAggregator.notifyObserver();
	}

	/**
	 * Called by onDestroy() to stop the thread and unregister the content
	 * observers.
	 */
	public void release() {
		mAggregator.release();
		mPrettyTimeRefreshHelper.stopRefresher();
	}

	@Override
	public void notifyDataSetInvalidated() {
		refreshTheme();
		super.notifyDataSetInvalidated();
	}

	@Override
	public void notifyDataSetChanged() {
		refreshTheme();
		super.notifyDataSetChanged();
	}

	private void refreshTheme() {
		mTimeLineThemeSetting = PreferenceManager.getInstance(mContext)
				.getStringPreference(Pref.TIMELINE_THEME,
						Pref.DEF_TIMELINE_THEME);
		mTimeLineItemResId = mTimeLineThemeSetting.equals(mContext
				.getString(R.string.preferences_timeline_theme_bubbles)) ? R.layout.listitem_timeline_bubble
				: R.layout.listitem_timeline_row;
	}

	// ---------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------
	// STANDARD ADAPTER METHODS
	// ---------------------------------------------------------------------------------

	public int getCount() {
		return mAggregator == null ? 0 : mAggregator.getCount();
	}

	public Event getItem(int position) {
		return mAggregator.getItem(position);
	}

	public long getItemId(int position) {
		return getItem(position).getId();
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		Event event = getItem(position);

		if (convertView == null
				|| !convertView.getTag().equals(mTimeLineItemResId)) {
			convertView = ((LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(mTimeLineItemResId, null);
			convertView.setTag(mTimeLineItemResId);
		}

		loadEventToView(event, convertView);

		if (convertView.getTag().equals(R.layout.listitem_timeline_bubble)) {
			alignBubbleView(event, convertView);
		} else {
			alignRowView(event, convertView);
			convertView
					.setBackgroundResource(position % 2 == 0 ? R.drawable.listitem_background1
							: R.drawable.listitem_background2);
		}

		return convertView;
	}

	private void loadEventToView(Event event, View convertView) {

		TextView tv = (TextView) convertView
				.findViewById(R.id.timeline_listitem_txtMessage);
		tv.setText(event.getMessage());

		tv = (TextView) convertView
				.findViewById(R.id.timeline_listitem_txtDate);
		tv.setText(DateUtils.formatTimelineDate(new Date(event
				.getPublishedTime())));

		ImageView iv = (ImageView) convertView
				.findViewById(R.id.timeline_listitem_imgIcon);
		mSourceIconHelper.toImageView(mContext, event.getSource(), event, iv);
	}

	private void alignBubbleView(Event event, View convertView) {

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

	private void alignRowView(Event event, View convertView) {

		// align the view depending on the originator of the event

		View viewLeftSpacer1 = convertView
				.findViewById(R.id.timeline_listitem_vLeftSpacer1);
		View viewRightSpacer1 = convertView
				.findViewById(R.id.timeline_listitem_vRightSpacer1);

		switch (event.getOriginator()) {
		case user:

			viewLeftSpacer1.setVisibility(View.VISIBLE);
			viewRightSpacer1.setVisibility(View.GONE);
			break;

		case contact:

			viewLeftSpacer1.setVisibility(View.GONE);
			viewRightSpacer1.setVisibility(View.VISIBLE);
			break;

		case both:

			viewLeftSpacer1.setVisibility(View.VISIBLE);
			viewRightSpacer1.setVisibility(View.VISIBLE);
			break;
		}

	}
}
