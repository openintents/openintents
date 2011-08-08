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
import org.openintents.historify.ui.PreferencesActivity;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.TextView;

public class MyAvatarPopupWindow extends AbstractPopupWindow {

	private TextView mTxtChangeAvatar;
	
	public MyAvatarPopupWindow(Context context) {
		super(context);
		
		setArrowGravity(Gravity.LEFT);
	}
	
	@Override
	protected void addContent(ViewGroup contentRoot) {
		
		ViewGroup contentView = (ViewGroup) ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popupwindow_myavatar, contentRoot);
		mTxtChangeAvatar = (TextView) contentView.findViewById(R.id.popupwindow_myavatar_txtChangeAvatar);
		mTxtChangeAvatar.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dismiss();
				mContext.startActivity(new Intent(mContext, PreferencesActivity.class));
			}
		});
	}

}
