package org.openintents.locations;

import android.os.Bundle;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Point;

public class LocationsMapView extends MapActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        int longitude = 0;
        int latitude = 0;
        
        Bundle bundle = getIntent().getExtras();
        if( bundle != null){
        	longitude = bundle.getInteger("longitude");
        	latitude = bundle.getInteger("latitude");
        }
        
        MapView view = new MapView(this);
        MapController controller = view.getController();
        controller.centerMapTo(new Point(latitude * 1000000,longitude * 1000000), true); 
        controller.zoomTo(9);
        setContentView(view);  //R.layout.main);       
    }
}
