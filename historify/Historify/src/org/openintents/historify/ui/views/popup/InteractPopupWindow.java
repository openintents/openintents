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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class InteractPopupWindow extends AbstractPopupWindow {

	public InteractPopupWindow(Context context) {
		super(context);
		setArrowGravity(Gravity.RIGHT);
	}
	
	@Override
	protected void addContent(ViewGroup contentRoot) {
		
		ViewGroup contentView = (ViewGroup) ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popupwindow_interact, contentRoot);
		
		ListView lstInteract = (ListView)contentView.findViewById(R.id.popupwindow_interact_lstInteract);
		lstInteract.setAdapter(new InteractListAdapter());
		
		lstInteract.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				dismiss();
			}
		});
	}

	private class InteractListAdapter extends BaseAdapter {

		public int getCount() {
			return 1;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			
			if(convertView==null) {
				convertView = ((LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listitem_interact, null);
			}
			
			ImageView iv = (ImageView)convertView.findViewById(R.id.interact_listitem_imgIcon);
			iv.setImageResource(R.drawable.source_telephony);
			
			TextView tv = (TextView)convertView.findViewById(R.id.interact_listitem_txtName);
			tv.setText("Call");
			
			return convertView;
		}
		
	}
}
