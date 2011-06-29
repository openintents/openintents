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

package org.openintents.historify.ui.fragments;

import org.openintents.historify.R;
import org.openintents.historify.data.adapters.ContactsAdapter;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.uri.Actions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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
public class ContactsListFragment extends Fragment {

	// list
	private ListView mLstContacts;
	private ContactsAdapter mAdapter;
	
	/** Called to have the fragment instantiate its user interface view. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ViewGroup layout = (ViewGroup) inflater.inflate(
				R.layout.fragment_contacts_list, container, false);

		// init listview
		mLstContacts = (ListView) layout
				.findViewById(R.id.contacts_lstContacts);
		mLstContacts
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Contact selected = (Contact) parent
								.getItemAtPosition(position);
						onContactSelected(selected);
					}
				});

		// init list empty view
		View lstContactsEmptyView = inflater.inflate(R.layout.list_empty_view,
				null);
		((TextView) lstContactsEmptyView)
				.setText(R.string.contacts_no_contacts);
		((ViewGroup) mLstContacts.getParent()).addView(lstContactsEmptyView);
		mLstContacts.setEmptyView(lstContactsEmptyView);

		return layout;

	}

	/**
	 * Called when the fragment's activity has been created and this fragment's
	 * view hierarchy instantiated.
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		// init adapter
		mAdapter = new ContactsAdapter(getActivity(), isStarredOnly());
		mLstContacts.setAdapter(mAdapter);
		
	}

	
	/**
	 * Checks if fragment is for displaying favorite contacts only.
	 * 
	 * @return true if only favorite contacts have to be shown.
	 */
	private boolean isStarredOnly() {
		return getArguments().getBoolean(
				Actions.EXTRA_MODE_FAVORITES, false);

	}

	/**
	 * Fires Intent to show the selected contact's timeline. Future releases
	 * might have to add support for parallel fragments.
	 * 
	 * @param selected
	 *            Selected contact.
	 */
	private void onContactSelected(Contact selected) {

		String contactLookupKey = String.valueOf(selected.getLookupKey());

		Intent intent = new Intent();
		intent.setAction(Actions.ACTION_SHOW_TIMELINE);
		intent.putExtra(Actions.EXTRA_CONTACT_LOOKUP_KEY, contactLookupKey);

		startActivity(intent);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mAdapter.releaseThread();
	}
}
