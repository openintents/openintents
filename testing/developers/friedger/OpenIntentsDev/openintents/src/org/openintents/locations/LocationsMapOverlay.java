package org.openintents.locations;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.google.android.maps.Overlay;
import com.google.android.maps.Point;

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
	
	
    public void draw(Canvas canvas, PixelCalculator pixelCalculator, boolean b) {
        super.draw(canvas, pixelCalculator, b);



        Point location = mMap.getPoint();
        if (location != null) {
        	    int[] screenCoords = new int[2];
                Point point = new Point(location.getLatitudeE6(),
                        location.getLongitudeE6());
                pixelCalculator.getPointXY(point, screenCoords);
                canvas.drawCircle(screenCoords[0], screenCoords[1], 9, paint1);
                canvas.drawText(Integer.toString(location.getLatitudeE6()),
                        screenCoords[0] + 9,
                        screenCoords[1] + 9, paint2);
                canvas.drawText(Integer.toString(location.getLongitudeE6()),
                		screenCoords[0] + 9,
                		screenCoords[1] + 20, paint2);
            }
    }
}
