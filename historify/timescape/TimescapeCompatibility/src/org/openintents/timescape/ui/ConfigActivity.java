/* 
 * Copyright (C) 2011 OpenIntents.org
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

package org.openintents.timescape.ui;

import org.openintents.timescape.R;
import org.openintents.timescape.api.data.PluginsAdapter;
import org.openintents.timescape.api.provider.EventStreamHelper;
import org.openintents.timescape.api.requestscheduling.RequestSender;
import org.openintents.timescape.api.requestscheduling.Settings;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class ConfigActivity extends Activity {
	
	private Spinner spinnerInterval;
	private ListView lstPlugins;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new RequestSender().requestRefresh(this);
        
        Cursor c = getContentResolver().query(EventStreamHelper.getUri(EventStreamHelper.FRIENDS_PATH),null, null, null, null);
        while(c.moveToNext()) {
        	for(String s : c.getColumnNames()) {
        		Log.v(s," "+c.getString(c.getColumnIndex(s)));
        	}
        }
        
		if (savedInstanceState == null) {
			FirstStartTasks.onStart(this);	
		}

        setContentView(R.layout.activity_config);

		ActionBar actionBar = new ActionBar((ViewGroup) findViewById(R.id.actionbar), R.string.config_title);
		actionBar.setup();
		
		initIntervalSpinner();
		initLstPlugins();
		
    }

    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	((PluginsAdapter)lstPlugins.getAdapter()).release();
    }
    
	private void initLstPlugins() {
		
		lstPlugins = (ListView) findViewById(R.id.config_lstPlugins);
		lstPlugins.setAdapter(new PluginsAdapter(this));
         
		// init list empty view
		View lstEmptyView = getLayoutInflater().inflate(R.layout.list_empty_view,
				null);
		((TextView) lstEmptyView).setText(R.string.config_no_services);
		((ViewGroup) lstPlugins.getParent()).addView(lstEmptyView);
		lstPlugins.setEmptyView(lstEmptyView);
		
		lstPlugins.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int pos,
					long id) {
				if(view.getTag()!=null) {
					Intent intent = new Intent();
					intent.setComponent((ComponentName) view.getTag());
					startActivity(intent);
				}
			}
		});

	}

	private void initIntervalSpinner() {
		
		spinnerInterval = (Spinner)findViewById(R.id.config_spinnerInterval);
		String[] items = getResources().getStringArray(R.array.scheduling_intervals);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item,items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerInterval.setAdapter(adapter);
		
		//load current value
		int index = new Settings().getSchedulingIntervalIndex(this);
		spinnerInterval.setSelection(index);
		
		spinnerInterval.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view,
					int pos, long id) {
				new Settings().setSchedulingIntervalIndex(ConfigActivity.this, pos);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
		});
	}
    
}