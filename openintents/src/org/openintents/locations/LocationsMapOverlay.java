package org.openintents.locations;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
public class LocationsMapOverlay extends Overlay{
	
	private LocationsMapView mMap;
	
	Paint paint1;
	Paint paint2;
	
	public LocationsMapOverlay(LocationsMapView mView){
		mMap = mView;
        paint1 = new Paint();
        paint2 = new Paint();
        paint2.setARGB(255, 255, 255, 255);
	}
	
	
    public void draw(Canvas canvas, MapView mapView, boolean b) {
        super.draw(canvas, mapView, b);



        GeoPoint location = mMap.getPoint();
        if (location != null) {
        	    int[] screenCoords = new int[2];
                GeoPoint point = new GeoPoint(location.getLatitudeE6(),
                        location.getLongitudeE6());
				android.graphics.Point graphPoint =new android.graphics.Point();
				//TODO new SDK convert pixels
				// pixelConverter.toPixels(point,graphPoint);
				
                //pixelConverter.getPointXY(point, screenCoords);
                canvas.drawCircle(graphPoint.x, graphPoint.y, 9, paint1);
                canvas.drawText(Integer.toString(location.getLatitudeE6()),
                        screenCoords[0] + 9,
                        screenCoords[1] + 9, paint2);
                canvas.drawText(Integer.toString(location.getLongitudeE6()),
                		screenCoords[0] + 9,
                		screenCoords[1] + 20, paint2);
            }
    }
}
