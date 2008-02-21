/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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

package org.openintents.locations;

import org.openintents.R;
import org.openintents.provider.Location.Locations;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Simple activity to show a list of locations and
 * add the current location to the list.
 * 
 *
 */
public class LocationsView extends Activity {

	private static final int MENU_ADD_CURRENT_LOCATION = 1;
	private Cursor c; 
	
	/** tag for logging */
	private static final String TAG = "locationsView";

	private ListView mList;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.locations);

		c = getContentResolver().query(
				Locations.CONTENT_URI,
				new String[] { Locations._ID, Locations.LATITUDE,
						Locations.LONGITUDE },
						null, null,
				Locations.DEFAULT_SORT_ORDER);
		
		mList = (ListView) findViewById(R.id.locations);
		
		fillData();
		mList.setOnItemClickListener(new OnItemClickListener(){

			
			public void onItemClick(AdapterView parent, View v, int position,
					long id) {
				int longitude = 0, latitude = 0;
				
				c.moveTo(position);
				latitude = c.getInt(1);
				longitude = c.getInt(2);
				
				Bundle bundle = new Bundle();
				bundle.putInt("latitude", latitude);
				bundle.putInt("longitude", longitude);

				Intent intent = new Intent();
				intent.setClass(v.getContext(), LocationsMapView.class);
				intent.putExtras(bundle);

				startActivity(intent);				
			}
			
		});
	}

	private void fillData() {		

		// Get a cursor for all locations
		startManagingCursor(c);

		ListAdapter adapter = new SimpleCursorAdapter(this,
				// Use a template that displays a text view
				R.layout.location_row,
				// Give the cursor to the list adapter
				c,
				// Map the LATITUDE and LONGITUDE columns in the
				// database to...
				new String[] { Locations.LATITUDE, Locations.LONGITUDE },
				// The view defined in the XML template
				new int[] { R.id.latitude, R.id.longitude });
		mList.setAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_ADD_CURRENT_LOCATION, R.string.add_current_location);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(Item item) {
		super.onOptionsItemSelected(item);
		switch (item.getId()) {
		case MENU_ADD_CURRENT_LOCATION:
			addCurrentLocation();
			break;	
		}
		return true;
	}


	private void addCurrentLocation() {
		Location location = getCurrentLocation();

		ContentValues values = new ContentValues(2);
		values.put(Locations.LATITUDE, String.valueOf(location.getLatitude()));
		values
				.put(Locations.LONGITUDE, String.valueOf(location
						.getLongitude()));
		getContentResolver().insert(Locations.CONTENT_URI, values);

		fillData();

	}

	private Location getCurrentLocation() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		LocationProvider locationProvider = locationManager.getProviders().get(
				0);
		Location location = locationManager.getCurrentLocation(locationProvider
				.getName());
		return location;
	}
	
}