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

package org.openintents.historify.ui;

import org.openintents.historify.R;
import org.openintents.historify.ui.fragments.ContactsListFragment;
import org.openintents.historify.ui.views.ActionBar;
import org.openintents.historify.ui.views.ActionBar.Action;
import org.openintents.historify.ui.views.ActionBar.MoreMenuFunction;
import org.openintents.historify.uri.Actions;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

/**
 * 
 * Contacts list view. Contains a fragment for displaying the list of contacts.
 * 
 * @author berke.andras
 */
public class ContactsActivity extends FragmentActivity {

	private ActionBar actionBar;
	private ContactsListFragment contactsListFragment;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		setContentView(R.layout.activity_contacts);
		
		boolean starredOnly = getIntent().getBooleanExtra(Actions.EXTRA_MODE_FAVORITES, false); 
		
		setFragmentParameters(starredOnly);
		setupActionBar(starredOnly);
	}

	private void setFragmentParameters(boolean starredOnly) {
		
		contactsListFragment = (ContactsListFragment)getSupportFragmentManager().findFragmentById(R.id.contacts_fragment);
		contactsListFragment.setStarredOnly(starredOnly);
	}

	private void setupActionBar(boolean starredOnly) {
		
		actionBar = new ActionBar((ViewGroup) findViewById(R.id.actionbar),
				starredOnly ? R.string.contacts_title_favorites : R.string.contacts_title_all);
		
		Action searchAction = new ActionBar.Action(R.drawable.ic_menu_search, new OnClickListener() {
			public void onClick(View v) {
				onSearchSelected();
			}
		});
		actionBar.add(searchAction);
		actionBar.setInactiveFunction(starredOnly ? MoreMenuFunction.favorites : MoreMenuFunction.contacts);
		actionBar.setup();

	}

	private void onSearchSelected() {
		contactsListFragment.onSearchSelected();
	}
		
}
