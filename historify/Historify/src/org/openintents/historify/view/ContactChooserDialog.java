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

package org.openintents.historify.view;

import java.util.HashSet;
import java.util.List;

import org.openintents.historify.R;
import org.openintents.historify.data.adapters.ContactsDialogAdapter;
import org.openintents.historify.data.model.Contact;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * Dialog for displaying a list of contacts which the user could choose from. <br/>
 * The dialog also supports item disabling. Disabled contacts could not be
 * selected.
 * 
 * @author berke.andras
 */
public class ContactChooserDialog extends Dialog {

	private Contact mSelectedContact;
	private OnDismissListener mOnDismissListener;
	private ContactsDialogAdapter mAdapter;

	public ContactChooserDialog(Activity context, List<Contact> disabledContacts) {
		super(context);

		// read disabled contacts' lookup keys
		HashSet<String> disabledKeys = new HashSet<String>();
		for (Contact c : disabledContacts) {
			disabledKeys.add(c.getLookupKey());
		}

		// init view
		setTitle(context.getString(R.string.sources_filter_mode_new_dialog));
		setContentView(R.layout.contacts_dialog);

		// listview
		ListView lstContacts = (ListView) findViewById(R.id.contacts_lstContacts);
		lstContacts
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Contact selected = (Contact) parent
								.getItemAtPosition(position);
						onContactSelected(selected);
					}
				});

		// list empty view
		View lstContactsEmptyView = getLayoutInflater().inflate(
				R.layout.list_empty_view, null);
		((TextView) lstContactsEmptyView)
				.setText(R.string.contacts_no_contacts);
		((ViewGroup) lstContacts.getParent()).addView(lstContactsEmptyView);
		lstContacts.setEmptyView(lstContactsEmptyView);
			
		// adapter
		String alreadyAddedMessage = context
				.getString(R.string.sources_filter_mode_already_added);
		mAdapter = new ContactsDialogAdapter(context,
				disabledKeys, alreadyAddedMessage);
		lstContacts.setAdapter(mAdapter);

		super.setOnDismissListener(new DismissListenerWrapper());
	}
	
	private void onContactSelected(Contact selected) {
		setSelectedContact(selected);
		dismiss();
	}

	private void setSelectedContact(Contact contact) {
		mSelectedContact = contact;
	}

	public Contact getSelectedContact() {
		return mSelectedContact;
	}
	
	
	@Override
	public void setOnDismissListener(OnDismissListener listener) {
		mOnDismissListener = listener;
	}
	
	public class DismissListenerWrapper implements OnDismissListener {

		public void onDismiss(DialogInterface dialog) {
			mAdapter.releaseThread();
			if(mOnDismissListener!=null) mOnDismissListener.onDismiss(dialog);
		}

	}

}
