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
import org.openintents.historify.data.loaders.ContactLoader.LoadingStrategy;
import org.openintents.historify.data.model.Contact;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
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

	protected Activity mContext;

	private ContactLoader mLoader;
	protected ContactIconHelper mContactIconHelper;
	protected ContactLoader.LoadingStrategy mLoadingStrategy;
	private Cursor mCursor;

	private ContactsChangedObserver mObserver;
	
	private String mFilterText = "";
	
	private class ContactsChangedObserver extends ContentObserver {

		public ContactsChangedObserver(Handler handler) {
			super(handler);
		}
		
		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}
		
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			refresh();
		}
	}

	public ContactsAdapter(Activity context, LoadingStrategy loadingStrategy) {

		mContext = context;
		mLoadingStrategy = loadingStrategy;
		mLoader = new ContactLoader();
		mContactIconHelper = new ContactIconHelper(mContext,R.drawable.contact_default_small);

		load();
	}

	/** Open cursor. */
	public void load() {

		doLoad();
		
		mObserver = new ContactsChangedObserver(new Handler()); 
		mContext
		 .getContentResolver()
		 .registerContentObserver(Contacts.CONTENT_URI, true, mObserver);		
	}
	
	private void doLoad() {
		mCursor = mLoader.openCursor(mContext, mLoadingStrategy);
		notifyDataSetChanged();
	}
	
	public void refresh() {
		
		if(mCursor!=null) {
			mCursor.requery();
			notifyDataSetChanged();
		}
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
					.inflate(R.layout.listitem_contact, null);
		}

		convertView.setBackgroundColor(mContext.getResources().getColor(position % 2 == 0 ? R.color.background_light : R.color.background_soft_light));
		
		TextView txtName = (TextView) convertView
				.findViewById(R.id.contacts_listitem_txtName);
		txtName.setText(contact.getName());

		
		ImageView iv = (ImageView)convertView.findViewById(R.id.contacts_listitem_imgIcon);
		iv.setImageResource(R.drawable.contact_default_small);
		mContactIconHelper.loadContactIcon(contact, iv);
		
		return convertView;
	}
	
	public void onDestroy() {
		mContactIconHelper.stopThread();
		mContext.getContentResolver().unregisterContentObserver(mObserver);
	}

	public void setFilter(String searchText) {
		
		if(!mLoadingStrategy.getFilterText().equals(searchText)) {
			mLoadingStrategy.setFilterText(searchText);
			doLoad();
		}
	}
}
