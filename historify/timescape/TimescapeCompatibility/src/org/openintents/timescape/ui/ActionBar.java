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

package org.openintents.timescape.ui;

import java.util.ArrayList;
import java.util.List;

import org.openintents.historify.uri.Actions;
import org.openintents.timescape.R;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

/**
 * 
 * ActionBar implementation for devices prior to 3.0
 * 
 * @author berke.andras
 *
 */
public class ActionBar {

	public static class Action {
		public final View.OnClickListener onClickListener;
		public final String title;
		public final int iconResId;
		
		public Action(int iconResId, View.OnClickListener onClickListener) {
			this.iconResId = iconResId;
			this.title = "";
			this.onClickListener = onClickListener;
		}

		public Action(String title, OnClickListener onClickListener) {
			this.iconResId = 0;
			this.title = title;
			this.onClickListener = onClickListener;
		}
		
		@Override
		public String toString() {
			return title;
		}
	}
	
	private Context mContext;
	private String mTitle;
	private List<Action> mActions;
	
	private ViewGroup mContentView;
	private ImageView mLogo;
	
	public ActionBar(ViewGroup contentView, int titleResId) {
		init(contentView, contentView.getContext().getString(titleResId));		
	}

	public ActionBar(ViewGroup contentView, String title) {
		init(contentView, title);
	}

	private void init(ViewGroup contentView, String title) {
		mContext = contentView.getContext();
		mContentView = contentView;
		mTitle = title;
		mActions = new ArrayList<Action>();
	}

	public void add(Action action) {
		mActions.add(action);
	}
	
	public void setup() {
		
		if(mTitle==null) {
			addTitleAndSpacing();
		} else {
			//display short logo and title
			addImage(R.drawable.actionbar_logo_short);
			addTitleAndSpacing();
		}
				
	}

	private void addImage(int imageResId) {
		
		mLogo = new ImageView(mContext);
		mLogo.setImageResource(imageResId);
		mLogo.setClickable(false);
		mLogo.setScaleType(ScaleType.CENTER);
        mContentView.addView(mLogo);

	}
	
	private void addTitleAndSpacing() {
		
        LinearLayout.LayoutParams spacing = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.FILL_PARENT);
        spacing.weight = 1;

		TextView title = (TextView) inflate(R.layout.actionbar_title);
		title.setText(mTitle == null ? "" : mTitle);
		title.setLayoutParams(spacing);
		
		mContentView.addView(title);
	}

	private View inflate(int resId) {
		return ((LayoutInflater)mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE)).inflate(resId, null);
	}

}
