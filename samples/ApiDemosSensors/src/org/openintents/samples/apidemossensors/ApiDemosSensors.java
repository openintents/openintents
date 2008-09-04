package org.openintents.samples.apidemossensors;

import org.openintents.samples.apidemossensors.graphics.Compass;
import org.openintents.samples.apidemossensors.graphics.SensorTest;
import org.openintents.samples.apidemossensors.os.Sensors;
import org.openintents.samples.apidemossensors.view.MapViewCompassDemo;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ApiDemosSensors extends ListActivity {
	
	private String[] mActivities = {
	        "Graphics / Compass", 
	        "Graphics / SensorTest", 
	        "OS / Sensors",
	        "Views / MapView and Compass"};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setContentView(R.layout.main);
        
        // Use an existing ListAdapter that will map an array
        // of strings to TextViews
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, mActivities));
        getListView().setTextFilterEnabled(true);
    }

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//super.onListItemClick(l, v, position, id);
		Intent intent;
		switch (position) {
		case 0:
			intent = new Intent();
            intent.setClass(this, Compass.class);
            startActivity(intent);
			break;
		case 1:
			intent = new Intent();
            intent.setClass(this, SensorTest.class);
            startActivity(intent);
			break;
		case 2:
			intent = new Intent();
            intent.setClass(this, Sensors.class);
            startActivity(intent);
			break;
		case 3:
			intent = new Intent();
            intent.setClass(this, MapViewCompassDemo.class);
            startActivity(intent);
			break;
		}
	}
    
    
    
}