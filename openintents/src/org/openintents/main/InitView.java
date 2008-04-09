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

package org.openintents.main;

import org.openintents.R;
import org.openintents.provider.Location;
import org.openintents.provider.Tag;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Init dialog
 *
 * @author openintent.org
 *
 */
public class InitView extends Activity {


	private static final String LOG_TAG = "init_OI";
	private Location mLocation;
	private Tag mTag;

	@Override
	protected void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);

		LinearLayout mainView = new LinearLayout(this);
		mainView.setGravity(Gravity.LEFT);
		mainView.setOrientation(LinearLayout.VERTICAL);
		setContentView(mainView);
		setTheme(android.R.style.Theme_Dialog);
		
		mLocation = new Location(this.getContentResolver());
		mTag= new Tag(this);
		
		Button button = new Button(this);
		android.widget.LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		button.setText(R.string.init_add_locations);
		button.setLayoutParams(params );
		button.setOnClickListener(new View.OnClickListener(){

			public void onClick(View view) {
				addLocations();				
			}
			
		});
		mainView.addView(button );
	}

	protected void addLocations() {
		
		Object[] locations = new Object[]{//				
				37.421902, -122.101198, "Rainbow grocery",
				37.433378, -122.106258, "Alemany Farmer's Market",
				37.449353, -122.119524, "Trader Joe's",
				37.454663, -122.130391, "Cheese Plus",
				37.458778, -122.137215, "Gino's Grocery Co"
		};
		android.location.Location loc = new android.location.Location();
		for (int i = 0; i < locations.length / 3; i++) {
			loc.setLatitude((Double)locations[i * 3]);
			loc.setLongitude((Double)locations[i * 3 + 1]);
			Uri locUri = mLocation.addLocation(loc);
			Log.v(LOG_TAG, locUri.toString());
			mTag.insertTag((String)locations[i * 3 + 2], locUri.toString());			
			mTag.insertTag("Shop", locUri.toString());
		}
		
		Toast.makeText(this, R.string.init_done, Toast.LENGTH_SHORT).show();
		
	}

	
}
