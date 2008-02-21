package org.openintents.locations;

import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Point;

public class LocationsMapView extends MapActivity {

	private Point point;
	private MapView view;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        Bundle bundle = getIntent().getExtras();
        if( bundle != null){
        	point = new Point(bundle.getInt("latitude"), bundle.getInt("longitude"));
        }
        
        view = new MapView(this);
        MapController controller = view.getController();
        controller.centerMapTo(new Point(point.getLatitudeE6(),point.getLongitudeE6()), true); 
        controller.zoomTo(9);
        setContentView(view);  //R.layout.main);      
        
        view.createOverlayController().add(new LocationsMapOverlay(this), true);
    }
    
    public Point getPoint(){
    	return point;
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int level;
		switch(keyCode){
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
