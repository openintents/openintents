/* 
 * Copyright (C) 2008 OpenIntents.org
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

package org.openintents.countdown.list;

import org.openintents.countdown.R;
import org.openintents.countdown.list.CountdownCursorAdapter.OnCountdownClickListener;
import org.openintents.countdown.util.CountdownUtils;

import android.content.Context;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CountdownListItemView extends LinearLayout {

	Context mContext;
	
	private long mDuration;
	private long mDeadline;
	private TextView mTitle;
	private TextView mDurationView;
	private TextView mCountdownView;
	private Button mStart;
	private LinearLayout mCountdownPanel;
	
	public CountdownListItemView(Context context) {
		super(context);
		mContext = context;

		// inflate rating
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(
				R.layout.countdownlist_item, this, true);
		
		mTitle = (TextView) findViewById(R.id.text);
		mDurationView = (TextView) findViewById(R.id.duration);
		mCountdownView = (TextView) findViewById(R.id.countdown);
		mStart = (Button) findViewById(R.id.start);
		mCountdownPanel = (LinearLayout) findViewById(R.id.countdownpanel);
		
	}

	/**
	 * Convenience method to set the title of a NewsView
	 */
	public void setTitle(String title) {
		mTitle.setText(title);
	}

	public void setDuration(long duration) {
		mDuration = duration;
		mStart.setText(mContext.getText(R.string.start) + "\n" + CountdownUtils.getDurationString(mDuration));
		updateCountdown();
	}
	
	public void setDeadline(long deadline) {
		mDeadline = deadline;
		updateCountdown();
	}
	
	
	/**
	 * Update countdown.
	 * 
	 * @return True if another update is necessary in a second.
	 */
	public boolean updateCountdown() {
		long now = System.currentTimeMillis();
		
		long delta = mDeadline - now;
		
		mDurationView.setText("" + CountdownUtils.getDurationString(mDuration));
		
		if (delta > 0) {
			mCountdownView.setText("" + CountdownUtils.getDurationString(delta));
			mCountdownView.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
			mStart.setVisibility(View.GONE);
			return true;
		} else if (delta > -3000) {
			mCountdownView.setText("" + CountdownUtils.getDurationString(0));
			mCountdownView.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
			mCountdownView.setTextColor(0xffff0000);
			mStart.setVisibility(View.GONE);
			return true;
			
		} else {
			mCountdownView.setText("");
			mStart.setVisibility(View.VISIBLE);
			return false;
		}
	}
	
	public void setListeners(OnCountdownClickListener listener, long id) {
		
		final OnCountdownClickListener mListener = listener;
		final long mId = id;

		mStart.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				mListener.onStartClick(mId);
			}
		});
		
		mCountdownPanel.setOnClickListener(new OnClickListener() {
			
			public void onClick(View view) {
				mListener.onCountdownPanelClick(mId);
			}
		});
	}

}
