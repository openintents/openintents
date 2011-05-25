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

public class MainActivity extends Activity {

	public static final String NAME = "MainActivity";

	private class MenuItem {
		
		String mTitle;
		Intent mIntent;

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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		//create menu items
		MenuItem[] menuItems = new MenuItem[3];
		menuItems[0] =new MenuItem(this, R.string.main_menu_contacts, new Intent(
				this, ContactsActivity.class));
		
		Intent intent = new Intent(this, ContactsActivity.class);
		intent.putExtra(Actions.EXTRA_MODE_FAVORITES, true);
		menuItems[1] =new MenuItem(this, R.string.main_menu_favorites, intent);
		
		menuItems[2] =new MenuItem(this, R.string.main_menu_sources, new Intent(
				this, SourcesActivity.class));
		
		
		//init menu
		ListView lstMenu = (ListView) findViewById(R.id.main_lstMenu);
		
		ArrayAdapter<MenuItem> menuItemAdapter = new ArrayAdapter<MenuItem>(
				this, android.R.layout.simple_list_item_1, menuItems);
		lstMenu.setAdapter(menuItemAdapter);
		
		lstMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				Intent intent = ((MenuItem) parent.getItemAtPosition(position))
						.getIntent();

				startActivity(intent);
			}
		});

	}
}