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
import org.openintents.historify.data.adapters.RecentlyContactedAdapter;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.ui.ContactsActivity;
import org.openintents.historify.ui.SourcesActivity;
import org.openintents.historify.uri.Actions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 
 * Fragment for displaying the main screen.
 * 
 * @author berke.andras
 */
public class MainScreenFragment extends Fragment {
	
	private Button btnMore, btnFavorites, btnSources;
	private Gallery galleryContacts;
	
	/** Called to have the fragment instantiate its user interface view.*/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_main_screen, container, false);
		
		//init buttons
		btnMore = (Button)layout.findViewById(R.id.main_screen_btnMore);
		btnFavorites = (Button)layout.findViewById(R.id.main_screen_btnFavorites);
		btnSources = (Button)layout.findViewById(R.id.main_screen_btnSources);

		btnMore.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onMoreClicked();
			}
		});
		
		btnFavorites.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onFavoritesClicked();
			}
		});
		
		btnSources.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onSourcesClicked();
			}
		});
		
		
		//init gallery for contacts
		galleryContacts = (Gallery)layout.findViewById(R.id.main_screen_galleryContacts);
		galleryContacts.setAdapter(new RecentlyContactedAdapter(getActivity()));
		
		galleryContacts.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> adapterView, View view, int pos,
					long id) {
				if(pos==galleryContacts.getSelectedItemPosition())
					onContactClicked((Contact)adapterView.getItemAtPosition(pos));
			}
		});
		
		return layout;
	}
	
	protected void onContactClicked(Contact selected) {
		
		String contactLookupKey = String.valueOf(selected.getLookupKey());

		Intent intent = new Intent();
		intent.setAction(Actions.ACTION_SHOW_TIMELINE);
		intent.putExtra(Actions.EXTRA_CONTACT_LOOKUP_KEY, contactLookupKey);

		startActivity(intent);

	}

	private void onMoreClicked() {
		Intent intent = new Intent(getActivity(), ContactsActivity.class);
		startActivity(intent);
	}
	
	private void onFavoritesClicked() {
		Intent intent = new Intent(getActivity(), ContactsActivity.class);
		intent.putExtra(Actions.EXTRA_MODE_FAVORITES, true);
		startActivity(intent);
	}
	
	private void onSourcesClicked() {
		Intent intent = new Intent(getActivity(), SourcesActivity.class);
		startActivity(intent);
	}

}
