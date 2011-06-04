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

package org.openintents.historify;

import org.openintents.historify.data.adapters.ContactsAdapter;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.uri.Actions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 
 * Fragment for displaying the list of contacts. The list could be filtered to
 * display favorite contacts only.
 * 
 * @author berke.andras
 */
public class ContactsActivity extends Activity {

	public static final String NAME = "ContactsActivity";

	// list adapter
	private ContactsAdapter mAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);

		// init listview
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

		// init list empty view
		View lstContactsEmptyView = getLayoutInflater().inflate(
				R.layout.list_empty_view, null);
		((TextView) lstContactsEmptyView)
				.setText(R.string.contacts_no_contacts);
		((ViewGroup) lstContacts.getParent()).addView(lstContactsEmptyView);
		lstContacts.setEmptyView(lstContactsEmptyView);

		// init adapter
		mAdapter = new ContactsAdapter(this, getIntent().hasExtra(
				Actions.EXTRA_MODE_FAVORITES));
		lstContacts.setAdapter(mAdapter);

	}

	/**
	 * Fires Intent to show the selected contact's timeline.
	 * 
	 * @param selected
	 *            Selected contact.
	 */
	private void onContactSelected(Contact selected) {

		String contactLookupKey = String.valueOf(selected.getLookupKey());

		Intent intent = new Intent();
		intent.setAction(Actions.SHOW_TIMELINE);
		intent.putExtra(Actions.EXTRA_CONTACT_LOOKUP_KEY, contactLookupKey);

		startActivity(intent);
	}
}
