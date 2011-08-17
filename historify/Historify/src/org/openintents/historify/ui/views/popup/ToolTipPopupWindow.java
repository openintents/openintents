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

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * Popup window for displaying general tooltip messages.
 * 
 * @author berke.andras
 */
public class ToolTipPopupWindow extends AbstractPopupWindow {

	private TextView mTxtMessage;
	private CheckBox mChkShowAgain;

	public ToolTipPopupWindow(Context context, int msgResId) {
		super(context);

		mTxtMessage.setText(msgResId);

		setArrowGravity(Gravity.LEFT);
	}

	@Override
	protected void addContent(ViewGroup contentRoot) {

		ViewGroup contentView = (ViewGroup) ((LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(
				R.layout.popupwindow_tooltip, contentRoot);
		mTxtMessage = (TextView) contentView
				.findViewById(R.id.tooltip_txtMessage);
		mChkShowAgain = (CheckBox) contentView
				.findViewById(R.id.tooltip_chkShowAgain);
	}

	public boolean needToShowInFuture() {
		return mChkShowAgain.isChecked();
	}
}
