package org.openintents.locations;

import org.openintents.R;
import org.openintents.lib.MultiWordAutoCompleteTextView;
import org.openintents.provider.Location;
import org.openintents.provider.Tag;
import org.openintents.provider.Location.Locations;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Point;

public class LocationsMapView extends MapActivity {

	private Point point;
	private MapView view;
	private long pointId;
	private Tag mTag;
	private Location mLocations;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		setContentView(R.layout.locations_map_view);
		mTag = new Tag(LocationsMapView.this);
		mLocations = new Location(this.getContentResolver());
		
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			point = new Point(bundle.getInt("latitude"), bundle
					.getInt("longitude"));
			pointId = bundle.getLong("_id");
		}
		
		if (pointId == 0L && getIntent() != null){
			String id =  getIntent().getData().getLastPathSegment();
			if (id != null && id.length() > 0){
				try {
					pointId = Integer.parseInt(id);	
					point = mLocations.getPoint(pointId);
				} catch (NumberFormatException e) {
					// ignore - no id found
				}				
			}
		}

		view = (MapView) findViewById(R.id.mapview);
		MapController controller = view.getController();
		controller.centerMapTo(new Point(point.getLatitudeE6(), point
				.getLongitudeE6()), true);
		controller.zoomTo(9);

		
		MultiWordAutoCompleteTextView tv = (MultiWordAutoCompleteTextView) findViewById(R.id.tag);		
		// TODO set text, set adapter
		Cursor idTagCursor = mTag.findTags(ContentUris.withAppendedId(Locations.CONTENT_URI, pointId).toString());
		Cursor allTagsCursor = mTag.findTagsForContentType(Locations.CONTENT_URI.toString());
		//tv.setText();
		//tv.setAdapter()
		
		view.createOverlayController().add(new LocationsMapOverlay(this), true);

		Button button = (Button) findViewById(R.id.button);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View view) {
				MultiWordAutoCompleteTextView autoComplete = (MultiWordAutoCompleteTextView) findViewById(R.id.tag);

				if (autoComplete.getText().length() > 0) {
					Point p = LocationsMapView.this.view.getMapCenter();
					Location location = new Location(LocationsMapView.this
							.getContentResolver());
					android.location.Location loc = new android.location.Location();
					loc.setLatitude(p.getLatitudeE6() / 1E6);
					loc.setLongitude(p.getLongitudeE6() / 1E6);
					
					Uri contentUri;
					if (p.getLatitudeE6() == point.getLatitudeE6() &&
							p.getLongitudeE6() == point.getLongitudeE6() &&
							pointId != 0L){
						contentUri = ContentUris.withAppendedId(Locations.CONTENT_URI, pointId);
					} else { 
						contentUri = location.addLocation(loc);
					}
					String content = contentUri.toString();
					

					String[] tags = autoComplete.getText().toString().split(
							autoComplete.getSeparator());

					for (int i = 0; i < tags.length; i++) {
						String s = tags[i].trim();
						mTag.insertTag(s, content);
					}
				}

				finish();
			}

		});
	}

	public Point getPoint() {
		return point;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int level;
		switch (keyCode) {
		case KeyEvent.KEYCODE_I:
			// Zoom In
			level = view.getZoomLevel();
			view.getController().zoomTo(level + 1);
			return true;
		case KeyEvent.KEYCODE_O:
			// Zoom Out
			level = view.getZoomLevel();
			view.getController().zoomTo(level - 1);
			return true;
		}
		return false;
	}
}
