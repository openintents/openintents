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

package org.openintents.historify.ui.views.popup;

import org.openintents.historify.R;
import org.openintents.historify.data.adapters.SourceFiltersAdapter;
import org.openintents.historify.data.adapters.SourcesAdapter;
import org.openintents.historify.data.model.source.EventSource;
import org.openintents.historify.ui.views.TimeLineTopPanel;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class TimeLineOptionsPopupWindow extends AbstractPopupWindow {

	private TimeLineTopPanel mPanel;
	
	private Button mBtnAll, mBtnNone, mBtnDefault;
	private ListView mLstSources;
	private TextView mTxtHidePanel;
	
	
	public TimeLineOptionsPopupWindow(TimeLineTopPanel panel) {
		super(panel.getContext());
		mPanel = panel;
	}

	
	@Override
	protected void addContent(ViewGroup contentRoot) {
		
		ViewGroup contentView = (ViewGroup) ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popupwindow_timeline_options, contentRoot);
		
		mBtnAll = (Button)contentView.findViewById(R.id.timeline_options_btnAll);
		mBtnNone = (Button) contentView.findViewById(R.id.timeline_options_btnNone);
		mBtnDefault = (Button) contentView.findViewById(R.id.timeline_options_btnDefault);
		
		mLstSources = (ListView)contentView.findViewById(R.id.timeline_options_lstSources);
		
		mLstSources
		.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				EventSource source = (EventSource) parent.getItemAtPosition(position);
				boolean checked = mLstSources.getCheckedItemPositions().get(position);
				onSourceClicked(source, checked);	
			}
		});

		
		mTxtHidePanel = (TextView)contentView.findViewById(R.id.timeline_options_txtHidePanel);
		mTxtHidePanel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dismiss();
				mPanel.onHide();
			}
		});
	}

	public void initView(boolean hideButtonVisible) {
	
		mTxtHidePanel.setVisibility(hideButtonVisible ? View.VISIBLE : View.GONE);
		mLstSources.setAdapter(new SourceFiltersAdapter(mContext, mLstSources, mPanel.getContact()));
		
	}
	
	/** Called when the user clicks on a source. */
	private void onSourceClicked(EventSource source, boolean checked) {
		source.setEnabled(checked);
		((SourcesAdapter)mLstSources.getAdapter()).update(source);
	}
		
}
