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
import org.openintents.historify.data.adapters.InteractionTypesAdapter;
import org.openintents.historify.data.model.source.InteractionType;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class InteractPopupWindow extends AbstractPopupWindow {

	private ListView mLstInteract;
	private View mEmptyHintView;
	
	private String mContactLookupKey;
	
	public InteractPopupWindow(Context context, String contactLookupKey) {
		super(context);
		setArrowGravity(Gravity.RIGHT);
		
		mContactLookupKey = contactLookupKey;		
		mLstInteract.setAdapter(new InteractionTypesAdapter((Activity)mContext, mEmptyHintView, mContactLookupKey));
		mLstInteract.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View arg1, int pos,
					long arg3) {
				Intent i = ((InteractionType)adapterView.getItemAtPosition(pos)).crateIntent(mContactLookupKey);
				mContext.startActivity(i);
				dismiss();
			}
		});
		
		setOnDismissListener(new OnDismissListener() {
			
			public void onDismiss() {
				if(mLstInteract!=null)
					((InteractionTypesAdapter)mLstInteract.getAdapter()).release();
			}
		});

		
	}
	
	@Override
	protected void addContent(ViewGroup contentRoot) {
		
		ViewGroup contentView = (ViewGroup) ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popupwindow_interact, contentRoot);
		
		mLstInteract = (ListView)contentView.findViewById(R.id.popupwindow_interact_lstInteract);
		mEmptyHintView = contentView.findViewById(R.id.popupwindow_interact_txtEmptyHint);
	}

}
