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

import org.openintents.historify.R;
import org.openintents.historify.data.loaders.ContactIconHelper;
import org.openintents.historify.data.loaders.ContactLoader;
import org.openintents.historify.data.model.Contact;

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
 * Adapter for contacts list.
 * 
 * @author berke.andras
 */
public class ContactsAdapter extends BaseAdapter {

	private Activity mContext;

	private ContactLoader mLoader;
	private ContactIconHelper mContactIconHelper;
	private Cursor mCursor;

	// true if list is filtered for favorite contacts only.
	private boolean mStarredOnly;

	public ContactsAdapter(Activity context, boolean starredOnly) {

		mContext = context;
		mStarredOnly = starredOnly;
		mLoader = new ContactLoader();
		mContactIconHelper = new ContactIconHelper(mContext);

		load();
	}

	/** Open cursor. */
	public void load() {

		mCursor = mLoader.openCursor(mContext, mStarredOnly);
		notifyDataSetChanged();
	}

	public int getCount() {
		return mCursor == null ? 0 : mCursor.getCount();
	}

	public Contact getItem(int position) {
		return mCursor == null ? null : mLoader.loadFromCursor(mCursor,
				position);
	}

	public long getItemId(int position) {
		return -1;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		Contact contact = getItem(position);

		if (convertView == null) {
			convertView = ((LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(R.layout.contacts_listitem, null);
		}

		TextView txtName = (TextView) convertView
				.findViewById(R.id.contacts_listitem_txtName);
		txtName.setText(contact.getName());

		
		ImageView iv = (ImageView)convertView.findViewById(R.id.contacts_listitem_imgIcon);
		iv.setImageResource(R.drawable.contact_default_small);
		mContactIconHelper.loadContactIcon(contact, iv);
		
		return convertView;
	}
	
	public void releaseThread() {
		mContactIconHelper.stopThread();
	}
}
