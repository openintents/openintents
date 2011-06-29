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
import org.openintents.historify.ui.ContactsActivity;
import org.openintents.historify.ui.SourcesActivity;
import org.openintents.historify.uri.Actions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 
 * Fragment for displaying the list of main menu items.
 * 
 * @author berke.andras
 */
public class MainMenuFragment extends Fragment {
	
	
	/** Called to have the fragment instantiate its user interface view.*/
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.fragment_main_menu, container, false);
		
		//init menu
		ListView lstMenu = (ListView) layout.findViewById(R.id.main_lstMenu);
		lstMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent = ((MenuItem) parent.getItemAtPosition(position))
						.getIntent();
				startActivity(intent);
			}
		});
		
		//create menu items
		MenuItem[] menuItems = createMenuItems(); 
		ArrayAdapter<MenuItem> menuItemAdapter = new ArrayAdapter<MenuItem>(
				getActivity(), R.layout.listitem_main, menuItems);
		lstMenu.setAdapter(menuItemAdapter);

		return layout;
	}
	
	/**
	 * Class describing menu element.
	 */
	private class MenuItem {
		
		//displayed title and Intent to be fired
		private String mTitle;
		private Intent mIntent;

		public MenuItem(Fragment fragment, int stringResource, Intent intent) {
			mTitle = fragment.getResources().getString(stringResource);
			mIntent = intent;
		}

		@Override
		public String toString() {
			return mTitle;
		}

		public Intent getIntent() {
			return mIntent;
		}
	}

	/** Creates MenuItem instances. */
	private MenuItem[] createMenuItems() {
		
		MenuItem[] retval = new MenuItem[3];
		
		//contacts
		retval[0] =new MenuItem(this, R.string.main_menu_contacts, new Intent(
				getActivity(), ContactsActivity.class));
		
		//favorites
		Intent intent = new Intent(getActivity(), ContactsActivity.class);
		intent.putExtra(Actions.EXTRA_MODE_FAVORITES, true);
		retval[1] =new MenuItem(this, R.string.main_menu_favorites, intent);
		
		//sources
		retval[2] =new MenuItem(this, R.string.main_menu_sources, new Intent(
				getActivity(), SourcesActivity.class));
		
		return retval;
		
	}
}
