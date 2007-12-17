package org.openintents.locations;

import java.net.URISyntaxException;

import org.openintents.R;
import org.openintents.provider.Location.Locations;
import org.openintents.provider.Tag.Contents;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ContentURI;
import android.os.Bundle;
import android.view.Menu;
import android.view.Menu.Item;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class LocationsView extends Activity {

	private static final int MENU_ADD_CURRENT_LOCATION = 1;
	
	/** tag for logging */
	private static final String TAG = "locationsView";

	private ListView mList;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.locations);

		mList = (ListView) findViewById(R.id.locations);
		
		fillData();

	}

	private void fillData() {		
		
		getContentResolver().getProvider(Locations.CONTENT_URI);
		// Get a cursor with location with given id
		Cursor c = getContentResolver().query(
				Locations.CONTENT_URI,
				new String[] { Locations._ID, Locations.LATITUDE,
						Locations.LONGITUDE },
						null, null,
				Locations.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		ListAdapter adapter = new SimpleCursorAdapter(this,
		// Use a template that displays a text view
				R.layout.location_row,
				// Give the cursor to the list adapter
				c,
				// Map the NAME column in the people database to...
				new String[] { Locations.LATITUDE, Locations.LONGITUDE },
				// The "text1" view defined in the XML template
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