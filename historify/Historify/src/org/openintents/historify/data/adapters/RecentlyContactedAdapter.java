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
import org.openintents.historify.data.loaders.ContactIconHelper;
import org.openintents.historify.data.loaders.ContactLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.utils.DateUtils;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class RecentlyContactedAdapter extends ContactsAdapter {
	
	public RecentlyContactedAdapter(Activity context) {
		super(context, new ContactLoader.RecentlyContactedLoadingStrategy());
		mContactIconHelper = new ContactIconHelper(mContext,R.drawable.contact_default_large);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		Contact contact = getItem(position);

		if (convertView == null) {
			convertView = ((LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.listitem_recently_contacted, null);
		}

		TextView tv = (TextView) convertView
				.findViewById(R.id.contacts_listitem_txtName);
		tv.setText(contact.getName());

		
		ImageView iv = (ImageView)convertView.findViewById(R.id.contacts_listitem_imgIcon);
		iv.setImageResource(R.drawable.contact_default_large);
		mContactIconHelper.loadContactIcon(contact, iv);
		
		tv = (TextView)convertView.findViewById(R.id.contacts_listitem_txtContacted);
		tv.setText(DateUtils.formatPrettyDate(new Date(contact.getLastTimeContacted())));
		
		return convertView;

	}

}
