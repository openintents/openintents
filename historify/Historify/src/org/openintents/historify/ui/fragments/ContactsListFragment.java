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
import org.openintents.historify.data.loaders.ContactLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.uri.Actions;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

/**
 * 
 * Fragment for displaying the list of contacts. The list could be filtered to
 * display favorite contacts only.
 * 
 * @author berke.andras
 */
public class ContactsListFragment extends Fragment {

	private static final String STATE_SEARCH_VISIBILITY = "state_search_visibility";
	private static final String STATE_SEARCH_CONTENT = "state_search_content";
	//search panel
	private ViewGroup mSearchBar;
	private EditText mEditSearch;
	
	// list
	private ListView mLstContacts;
	private ContactsAdapter mAdapter;
	
	private ContactLoader.LoadingStrategy mContactLoadingStrategy;
	
	/** Called to have the fragment instantiate its user interface view. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ViewGroup layout = (ViewGroup) inflater.inflate(
				R.layout.fragment_contacts_list, container, false);

		// init search panel
		mSearchBar = (ViewGroup)layout.findViewById(R.id.searchBar);
		mEditSearch = (EditText)mSearchBar.findViewById(R.id.searchBar_editSearch);
		mEditSearch.setHint(R.string.searchbar_contacts_hint);
		
		if(savedInstanceState!=null) {
			mSearchBar.setVisibility(savedInstanceState.getInt(STATE_SEARCH_VISIBILITY));
			mEditSearch.setText(savedInstanceState.getString(STATE_SEARCH_CONTENT));
		}
		
		mEditSearch.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}
			
			public void afterTextChanged(Editable s) {
				notifySearchTextChanged();
			}
		});
		
		mEditSearch.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if(actionId==EditorInfo.IME_ACTION_DONE || actionId==EditorInfo.IME_ACTION_NONE) {
					if(v.getText().toString().trim().equals(""))
						//empty search field -- so we hide it
						onSearchSelected();					
				}
				return false;
			}
		});
		
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
		
		if(mContactLoadingStrategy == null)
			throw new IllegalStateException("Fragment parameters not set.");
		
		// init adapter
		mAdapter = new ContactsAdapter(getActivity(), mContactLoadingStrategy);
		if(savedInstanceState!=null) {
			mAdapter.setFilter(savedInstanceState.getString(STATE_SEARCH_CONTENT));
		}
		
		mLstContacts.setAdapter(mAdapter);
		
	}
	
	/**
	 * Set up contact list filter parameter.
	 * 
	 * @param starredOnly
	 */
	public void setStarredOnly(boolean starredOnly) {
		
		mContactLoadingStrategy = starredOnly ? 
				new ContactLoader.StarredContactsLoadingStrategy() :
				new ContactLoader.SimpleLoadingStrategy();
	}
	

	/**
	 * Fires Intent to show the selected contact's timeline. Future releases
	 * might have to add support for parallel fragments.
	 * 
	 * @param selected
	 *            Selected contact.
	 */
	private void onContactSelected(Contact selected) {

		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(mEditSearch.getWindowToken(), 0);

		String contactLookupKey = String.valueOf(selected.getLookupKey());

		Intent intent = new Intent();
		intent.setAction(Actions.ACTION_SHOW_TIMELINE);
		intent.putExtra(Actions.EXTRA_CONTACT_LOOKUP_KEY, contactLookupKey);
		
		startActivity(intent);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		mAdapter.release();
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(STATE_SEARCH_VISIBILITY, mSearchBar.getVisibility());
		outState.putString(STATE_SEARCH_CONTENT, mEditSearch.getText().toString());
	}
	
	public void onSearchSelected() {
		boolean needToShow = mSearchBar.getVisibility() == View.GONE;
		
		if(needToShow && mAdapter.isEmpty())
			return;
		
		if(!needToShow) { //hide search
			mEditSearch.setText("");
		}
		
		mSearchBar.setVisibility(needToShow ?  View.VISIBLE : View.GONE);
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		if(needToShow) {
			mEditSearch.requestFocus();
			imm.showSoftInput(mEditSearch, InputMethodManager.SHOW_FORCED);
		} else {
			imm.hideSoftInputFromWindow(mEditSearch.getWindowToken(), 0);
		}
			
	}
	
	protected void notifySearchTextChanged() {
		String searchText = mEditSearch.getText().toString().trim();
		if(mAdapter!=null)
			mAdapter.setFilter(searchText);
	}

}
