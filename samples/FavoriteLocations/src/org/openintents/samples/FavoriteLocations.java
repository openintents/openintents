/* 
 * Copyright (C) 2007 OpenIntents.org
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

package org.openintents.samples;

/*
 * YOU HAVE TO MANUALLY INCLUDE THE OPENINTENTS-LIB-n.n.n.JAR FILE:
 * 
 * In the Eclipse Package Explorer, right-click on the imported 
 * project FavoriteLocations, select "Properties", then "Java Build Path" 
 * and tab "Libraries". There "Add External JARs..." and select 
 * lib/openintents-lib-n.n.n.jar. 
 */

import org.openintents.OpenIntents;
import org.openintents.provider.Location.Locations;
import org.openintents.provider.Tag.Tags;

import android.app.Activity;
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
import android.view.Menu;
import android.view.Menu.Item;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class FavoriteLocations extends Activity {

	/** tag for logging */
	private static final String TAG = "FavoriteLocations";

	private ListView mList;
	private String mFilter = "FAVORITE";

	private String mScheme = Locations.CONTENT_URI.toString() + "%";

	private ListView mListFavorite;

	private static final int MENU_ADD_CURRENT_LOCATION = 1;
	private static final int MENU_VIEW = 2;
	private static final int MENU_TAG = 3;

	private static final int TAG_ACTIVITY = 0;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);

		mList = (ListView) findViewById(R.id.list);
		mListFavorite = (ListView) findViewById(R.id.list_favorite);

		TextView tagText = (TextView) findViewById(R.id.tag);
		tagText.setText(mFilter);
		fillData();
	}

	private void fillData() {

		String ids = getFavoriteIds();

		// Get a cursor
		Cursor c = getContentResolver().query(
				Locations.CONTENT_URI,
				new String[] { Locations._ID, Locations.LATITUDE,
						Locations.LONGITUDE },
				Locations._ID + " in (" + ids + ")", null,
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
		mListFavorite.setAdapter(adapter);

		// Get a cursor
		c = getContentResolver().query(
				Locations.CONTENT_URI,
				new String[] { Locations._ID, Locations.LATITUDE,
						Locations.LONGITUDE },
				Locations._ID + " not in (" + ids + ")", null,
				Locations.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		adapter = new SimpleCursorAdapter(this,
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

	private String getFavoriteIds() {
		String filter = "uri_1 = ? AND uri_2 like ?";
		String[] filterArray = new String[] { mFilter, mScheme };
		// Get a cursor with all tags
		Cursor c = getContentResolver().query(Tags.CONTENT_URI,
				new String[] { Tags._ID, Tags.URI_1, Tags.URI_2 }, filter,
				filterArray, Tags.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		if (c == null) {
			Log.e(TAG, "missing tag provider");
			return null;
		}

		StringBuffer sb = new StringBuffer();
		while (c.next()) {
			String id = null;
//			try {
				id = Uri.parse(c.getString(2)).getLastPathSegment();
				// TODO ??? IS THIS CORRECT? Formerly it was: .getPathLeaf();
//			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			if (id != null) {
				sb.append(id).append(",");
			}
		}
		if (sb.length() > 0) {
			sb.setLength(sb.length() - 1);
		}
		return sb.toString();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_ADD_CURRENT_LOCATION, R.string.add_current_location);
		menu.add(0, MENU_VIEW, R.string.view_location);
		menu.add(0, MENU_TAG, R.string.tag_location);
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
			} else {
				cursor = (Cursor) mListFavorite.getSelectedItem();
				if (cursor != null) {
					viewLocation(cursor);
				}
			}
			break;
		case MENU_TAG:
			long id = mList.getSelectedItemId();
			if (id >= 0) {
				tagLocation(id);
			}
			break;
		}
		return true;
	}

	private void tagLocation(long id) {
		Uri uriToTag = Locations.CONTENT_URI;
		uriToTag = ContentUris.appendId(uriToTag.buildUpon(), id).build();;
		Intent intent = new Intent(OpenIntents.TAG_ACTION, Tags.CONTENT_URI);
		intent.putExtra(Tags.QUERY_URI, uriToTag.toString());

		try {
			startSubActivity(intent, TAG_ACTIVITY);
		} catch (Exception e) {
			e.printStackTrace();
			showAlert("tag action failed", 0, e.toString(), "ok", false);
		}

	}

	private void viewLocation(Cursor cursor) {
		String latitude = cursor.getString(cursor
				.getColumnIndex(Locations.LATITUDE));
		String longitude = cursor.getString(cursor
				.getColumnIndex(Locations.LONGITUDE));
		Uri uri;
//		try {
			uri = Uri.parse("geo:" + latitude + "," + longitude);
//		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
//			uri = null;
//		}

		if (uri != null) {
			Intent intent = new Intent(Intent.VIEW_ACTION, uri);
			startSubActivity(intent, 0);
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			String data, Bundle extras) {
		super.onActivityResult(requestCode, resultCode, data, extras);

		if (requestCode == TAG_ACTIVITY && resultCode == Activity.RESULT_OK) {
			fillData();
		}
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