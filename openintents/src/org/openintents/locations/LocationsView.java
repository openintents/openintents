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

import org.openintents.OpenIntents;
import org.openintents.R;
import org.openintents.provider.Alert;
import org.openintents.provider.Intents;
import org.openintents.provider.Location.Extras;
import org.openintents.provider.Location.Locations;
import org.openintents.provider.Tag.Tags;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.ContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleCursorAdapter.ViewBinder;

/**
 * Simple activity to show a list of locations and add the current location to
 * the list.
 * 
 * 
 */
public class LocationsView extends Activity {

	private static final int MENU_ADD_CURRENT_LOCATION = 1;
	private static final int MENU_VIEW = 2;
	private static final int MENU_TAG = 3;
	private static final int MENU_DELETE = 4;
	private static final int MENU_ADD_ALERT = 5;
	protected static final int MENU_MANAGE_EXTRAS = 6;

	private static final int TAG_ACTIVITY = 1;
	private static final int REQUEST_PICK_INTENT = 2;

	private org.openintents.provider.Location mLocation;
	private Cursor c;

	/** tag for logging */
	private static final String TAG = "locationsView";

	private static final String MLAST = "mlast";

	private ListView mList;
	private int mlastPosition;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.locations);

		c = getContentResolver().query(
				Locations.CONTENT_URI,
				new String[] { Locations._ID, Locations.LATITUDE,
						Locations.LONGITUDE }, null, null,
				Locations.DEFAULT_SORT_ORDER);
		mLocation = new org.openintents.provider.Location(this
				.getContentResolver());
		mList = (ListView) findViewById(R.id.locations);

		fillData();
		mList.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView parent, View v, int position,
					long id) {
				if (getCallingActivity() != null
						&& Intent.PICK_ACTION.equals(getIntent().getAction())) {

					Cursor cursor = (Cursor) mList.getAdapter().getItem(
							position);
					String geo = getGeoString(cursor);
					Bundle extras = new Bundle();
					extras.putString(Locations.EXTRA_GEO, geo);
					setResult(Activity.RESULT_OK, ContentUris.withAppendedId(
							Locations.CONTENT_URI, id).toString(), extras);
					finish();
				} else {
					viewLocationWithMapView(position);
				}
			}

		});		
	}

	private String getGeoString(Cursor cursor) {
		String latitude = String.valueOf(cursor.getDouble(cursor
				.getColumnIndex(Locations.LATITUDE)));
		String longitude = String.valueOf(cursor.getString(cursor
				.getColumnIndex(Locations.LONGITUDE)));
		return "geo:" + latitude + ":" + longitude;
	}

	private void fillData() {

		// Get a cursor for all locations
		startManagingCursor(c);

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
		// Use a template that displays a text view
				R.layout.location_row,
				// Give the cursor to the list adapter
				c,
				// Map the LATITUDE and LONGITUDE columns in the
				// database to...
				new String[] { Locations.LATITUDE, Locations.LONGITUDE,
						Locations._ID },
				// The view defined in the XML template
				new int[] { R.id.latitude, R.id.longitude, R.id.tags });
		ViewBinder viewBinder = new TagsViewBinder(this);
		adapter.setViewBinder(viewBinder);
		mList.setAdapter(adapter);
		mList
				.setOnPopulateContextMenuListener(new View.OnPopulateContextMenuListener() {

					public void onPopulateContextMenu(ContextMenu contextmenu,
							View view, Object obj) {
						contextmenu.add(0, MENU_MANAGE_EXTRAS,
								R.string.locations_manage_extras);
					}

				});
	}

	@Override
	public boolean onContextItemSelected(Item item) {

		super.onContextItemSelected(item);

		ContextMenuInfo menuInfo = (ContextMenuInfo) item.getMenuInfo();
		switch (item.getId()) {
		case MENU_MANAGE_EXTRAS:
			Intent intent = new Intent(this, ExtrasView.class);
			long locationId = ((Cursor) mList.getAdapter().getItem(
					menuInfo.position)).getLong(0);
			if (locationId != 0L) {
				intent.putExtra(Extras.LOCATION_ID, locationId);
				startActivity(intent);
			}
			break;
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_ADD_CURRENT_LOCATION, R.string.add_current_location,
				R.drawable.locations_add001a);
		menu.add(0, MENU_VIEW, R.string.view_location,
				R.drawable.locations_view001a);
		menu.add(0, MENU_TAG, R.string.tag_location,
				R.drawable.locations_favorite_application001a);
		menu.add(0, MENU_DELETE, R.string.delete_location,
				R.drawable.locations_delete001a);
		menu.add(0, MENU_ADD_ALERT, R.string.add_alert,
				R.drawable.locations_add_alert001a);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(Item item) {
		super.onOptionsItemSelected(item);
		switch (item.getId()) {
		case MENU_ADD_CURRENT_LOCATION:
			addCurrentLocation();
			break;
		case MENU_VIEW:
			Cursor cursor = (Cursor) mList.getSelectedItem();
			if (cursor != null) {
				viewLocation(cursor);
			}
			break;
		case MENU_TAG:
			long id = mList.getSelectedItemId();
			if (id >= 0) {
				tagLocation(id);
			}
			break;
		case MENU_DELETE:
			id = mList.getSelectedItemId();
			if (id >= 0) {
				deleteLocation(id);
			}
			break;
		case MENU_ADD_ALERT:
			id = mList.getSelectedItemId();
			if (id >= 0) {
				mlastPosition = mList.getSelectedItemPosition();
				Intent intent = new Intent(Intent.PICK_ACTION,
						Intents.CONTENT_URI);
				startSubActivity(intent, REQUEST_PICK_INTENT);
			}
		}
		return true;
	}

	private void addAlert(String locationUri, String data, String actionName,
			String type, String uri) {

		ContentValues values = new ContentValues();
		values.put(Alert.Location.ACTIVE, Boolean.TRUE);
		values.put(Alert.Location.ACTIVATE_ON_BOOT, Boolean.TRUE);
		values.put(Alert.Location.DISTANCE, 100L);
		values.put(Alert.Location.POSITION, locationUri);
		values.put(Alert.Location.INTENT, actionName);
		values.put(Alert.Location.INTENT_URI, uri);
		// TODO convert type to uri (?) or add INTENT_MIME_TYPE column
		getContentResolver().insert(Alert.Location.CONTENT_URI, values);

	}

	private void deleteLocation(long id) {
		mLocation.deleteLocation(id);

	}

	private void addCurrentLocation() {
		Location location = getCurrentLocation();

		if (location == null) {
			AlertDialog.show(this, "info", 0,
					"curr. location could not be determined", getResources()
							.getString(R.string.ok), null, false, null);
		} else {
			mLocation.addLocation(location);
			fillData();
		}

	}

	private Location getCurrentLocation() {
		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (locationManager != null) {
			LocationProvider locationProvider = locationManager.getProviders()
					.get(0);
			Location location = locationManager
					.getCurrentLocation(locationProvider.getName());
			return location;
		} else {
			return null;
		}
	}

	private void tagLocation(long id) {
		Uri uriToTag = ContentUris.withAppendedId(Locations.CONTENT_URI, id);

		Intent intent = new Intent(OpenIntents.TAG_ACTION, Tags.CONTENT_URI);
		intent.putExtra(Tags.QUERY_URI, uriToTag.toString());

		try {
			startSubActivity(intent, TAG_ACTIVITY);
		} catch (Exception e) {
			e.printStackTrace();
			showAlert("tag action failed", 0, e.toString(), "ok", false);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			String data, Bundle extras) {
		super.onActivityResult(requestCode, resultCode, data, extras);

		if (requestCode == TAG_ACTIVITY && resultCode == Activity.RESULT_OK) {
			c.requery();
			fillData();
		} else if (requestCode == REQUEST_PICK_INTENT
				&& resultCode == Activity.RESULT_OK) {

			Cursor cursor = (Cursor) mList.getAdapter().getItem(mlastPosition);
			if (cursor == null) {
				c.requery();
				cursor = (Cursor) mList.getAdapter().getItem(mlastPosition);
			}

			String locationUri = getGeoString(cursor);
			addAlert(locationUri, data, extras.getString(Intents.EXTRA_TYPE),
					extras.getString(Intents.EXTRA_ACTION), extras
							.getString(Intents.EXTRA_URI));
		}
	}

	@Override
	protected void onFreeze(Bundle outState) {
		super.onFreeze(outState);
		outState.putInt(MLAST, mList.getSelectedItemPosition());
		stopManagingCursor(c);
	}

	@Override
	protected void onResume() {
		startManagingCursor(c);
	}
	
	private void viewLocation(Cursor cursor) {
		String geoString = getGeoString(cursor);
		Uri uri;
		// try {
		uri = Uri.parse(geoString);
		// } catch (URISyntaxException e) {
		// TODO Auto-generated catch block
		// e.printStackTrace();
		// uri = null;
		// }

		if (uri != null) {
			Intent intent = new Intent(Intent.VIEW_ACTION, uri);
			startSubActivity(intent, 0);
		}

	}

	private void viewLocationWithMapView(int position) {
		Double longitude = 0.0, latitude = 0.0;

		c.moveTo(position);
		latitude = c.getDouble(c.getColumnIndex(Locations.LATITUDE)) * 1E6;
		longitude = c.getDouble(c.getColumnIndex(Locations.LONGITUDE)) * 1E6;

		Bundle bundle = new Bundle();
		bundle.putInt("latitude", latitude.intValue());
		bundle.putInt("longitude", longitude.intValue());
		bundle.putLong("_id", c.getLong(c.getColumnIndex(Locations._ID)));

		Intent intent = new Intent();
		intent.setClass(this, LocationsMapView.class);
		intent.putExtras(bundle);

		startSubActivity(intent, 0);
	}

}