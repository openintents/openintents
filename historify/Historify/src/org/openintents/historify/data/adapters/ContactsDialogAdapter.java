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

import java.util.HashSet;

import org.openintents.historify.R;
import org.openintents.historify.data.loaders.ContactLoader;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * 
 * Modified contact list adapter to support disabled list items.
 * 
 * @author berke.andras
 */
public class ContactsDialogAdapter extends ContactsAdapter {

	// lookup keys of disabled list items
	private HashSet<String> mDisabledKeys;

	// message to show if item is disabled
	private String mDisabledMessage;

	/**
	 * Constructor.
	 * 
	 * @param context
	 *            Activity context.
	 * @param disabledKeys
	 *            Lookup keys of disabled Contacts.
	 * @param disabledMessage
	 *            Message to show if a Contact is disabled.
	 */
	public ContactsDialogAdapter(Activity context,
			HashSet<String> disabledKeys, String disabledMessage) {
		super(context, new ContactLoader.SimpleLoadingStrategy());

		mDisabledKeys = disabledKeys;
		mDisabledMessage = disabledMessage;
	}

	@Override
	public boolean areAllItemsEnabled() {
		return false;
	}

	@Override
	public boolean isEnabled(int position) {
		return !mDisabledKeys.contains(getItem(position).getLookupKey());
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View retval = super.getView(position, convertView, parent);

		// sets message if position is disabled
		TextView tv = (TextView) retval
				.findViewById(R.id.contacts_listitem_txtStatus);
		tv.setText(isEnabled(position) ? "" : mDisabledMessage);

		return retval;
	}
}
