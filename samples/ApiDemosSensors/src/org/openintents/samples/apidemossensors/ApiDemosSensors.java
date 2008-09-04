/* 
 * Copyright (C) 2008 OpenIntents.org
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

/*
 * This application is based on Google's ApiDemos
 * which are licensed under the same license (Apache License, Version 2.0)
 */

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