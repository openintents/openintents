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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * Adapter for the contacts list.
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

	/**
	 * Observer for the contact list. If the contact list changes, the data set
	 * will be refreshed.
	 */
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

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            Activity context.
	 * @param loadingStrategy
	 *            The actual {@link ContactLoader.LoadingStrategy} used for
	 *            querying the contacts.
	 */
	public ContactsAdapter(Activity context, LoadingStrategy loadingStrategy) {

		mContext = context;
		mLoadingStrategy = loadingStrategy;
		mLoader = new ContactLoader();
		mContactIconHelper = new ContactIconHelper(mContext,
				R.drawable.contact_default_small);

		load();
	}

	/**
	 * Loading data. Opens cursor. Registers content observer.
	 */
	public void load() {

		doLoad();

		mObserver = new ContactsChangedObserver(new Handler());
		mContext.getContentResolver().registerContentObserver(
				Contacts.CONTENT_URI, true, mObserver);
	}

	/**
	 * Sets a filter for the underlying ContactLoader. Used by the contact
	 * search function.
	 * 
	 * @param searchText
	 *            The searched text.
	 */
	public void setFilter(String searchText) {

		if (!mLoadingStrategy.getFilterText().equals(searchText)) {
			mLoadingStrategy.setFilterText(searchText);
			doLoad();
		}
	}

	private void doLoad() {
		mCursor = mLoader.openManagedCursor(mContext, mLoadingStrategy);
		notifyDataSetChanged();
	}

	private void refresh() {

		if (mCursor != null) {
			mCursor.requery();
			notifyDataSetChanged();
		}
	}

	/**
	 * Called by onDestroy() to stop the thread and unregister the content
	 * observer.
	 */
	public void release() {
		mContactIconHelper.stopThread();
		mContext.getContentResolver().unregisterContentObserver(mObserver);
	}

	// ---------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------
	// ---------------------------------------------------------------------------------
	// STANDARD ADAPTER METHODS
	// ---------------------------------------------------------------------------------

	public int getCount() {
		return mCursor == null || mCursor.isClosed() ? 0 : mCursor.getCount();
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

		convertView
				.setBackgroundResource(position % 2 == 0 ? R.drawable.listitem_background1
						: R.drawable.listitem_background2);

		TextView txtName = (TextView) convertView
				.findViewById(R.id.contacts_listitem_txtName);
		txtName.setText(contact.getDisplayedName());

		ImageView iv = (ImageView) convertView
				.findViewById(R.id.contacts_listitem_imgIcon);
		iv.setImageResource(R.drawable.contact_default_small);
		mContactIconHelper.loadContactIcon(contact, iv);

		return convertView;
	}

}
