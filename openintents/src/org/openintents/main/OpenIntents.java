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
import org.openintents.hardware.SensorSimulatorView;
import org.openintents.locations.LocationsView;
import org.openintents.shopping.ShoppingView;
import org.openintents.tags.MagnoliaSettings;
import org.openintents.tags.MagnoliaTagging;
import org.openintents.tags.TagsView;
import org.openintents.tags.content.ContentBrowserView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Main activity to start simple activities for ContentProviders.
 * 
 * Currently supported:
 * LocationsProvider
 * TagsProvider
 * 
 *
 */
public class OpenIntents extends Activity implements OnItemClickListener {

	private String[] activitylist = { 
			"Show locations", 
			"Show tags", 
			"Shopping list",
			"SensorSimulator",
			"Content browser",
			"Magnolia settings",
			"Magnolia tagging"
			};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);

		ListView list = (ListView) findViewById(R.id.activities);
		list.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, activitylist));
		list.setOnItemClickListener(this);
		
		// Watch for button clicks.
        ImageButton aboutButton = (ImageButton)findViewById(R.id.about);
        aboutButton.setOnClickListener(mAboutListener);
	}

	public void onItemClick(AdapterView adapterView, View view, int position,
			long id) {
		switch (position) {
		case 0:
			Intent intent = new Intent(this, LocationsView.class);
			startActivity(intent);
			break;
			
		case 1:
			intent = new Intent(this, TagsView.class);
			startActivity(intent);
			break;
			
		case 2:
			intent = new Intent(this, ShoppingView.class);
			startActivity(intent);
			break;
			
		case 3:
			intent = new Intent(this, SensorSimulatorView.class);
			startActivity(intent);
			break;
		
		case 4:
			intent = new Intent(this, ContentBrowserView.class);
			startActivity(intent);
			break;
			
		case 5:
			intent = new Intent(this, MagnoliaSettings.class);
			startActivity(intent);
			break;
			
		case 6:
			intent = new Intent(this, MagnoliaTagging.class);
			startActivity(intent);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown position " + position);
		}

	}
	
	private OnClickListener mAboutListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(OpenIntents.this, About.class);
			startActivity(intent);
	    }
	};

}
