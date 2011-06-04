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

import org.openintents.historify.uri.Actions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * 
 * Historify's main view.
 * 
 * @author berke.andras
 */
public class MainActivity extends Activity {

	public static final String NAME = "MainActivity";

	/**
	 * Class describing menu element.
	 */
	private class MenuItem {
		
		//displayed title and Intent to be fired
		private String mTitle;
		private Intent mIntent;

		public MenuItem(Context context, int stringResource, Intent intent) {
			mTitle = context.getResources().getString(stringResource);
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
				this, ContactsActivity.class));
		
		//favorites
		Intent intent = new Intent(this, ContactsActivity.class);
		intent.putExtra(Actions.EXTRA_MODE_FAVORITES, true);
		retval[1] =new MenuItem(this, R.string.main_menu_favorites, intent);
		
		//sources
		retval[2] =new MenuItem(this, R.string.main_menu_sources, new Intent(
				this, SourcesActivity.class));
		
		return retval;
		
	}
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//init menu
		ListView lstMenu = (ListView) findViewById(R.id.main_lstMenu);
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
				this, R.layout.main_listitem, menuItems);
		lstMenu.setAdapter(menuItemAdapter);
				
	}

}