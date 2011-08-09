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

package org.openintents.historify.ui;


import org.openintents.historify.FirstStartTasks;
import org.openintents.historify.R;
import org.openintents.historify.preferences.Pref;
import org.openintents.historify.preferences.PreferenceManager;
import org.openintents.historify.ui.views.ActionBar;
import org.openintents.historify.uri.Actions;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ViewGroup;

/**
 * 
 * Historify's main view. Contains a fragment for displaying the main screen.
 * 
 * @author berke.andras
 */
public class MainActivity extends FragmentActivity {

	private ActionBar actionBar;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	
		if (savedInstanceState == null) {
			FirstStartTasks.onStart(this);	
		}
	
		//decide which startup action to run
		PreferenceManager pm = PreferenceManager.getInstance(this);
		String startUpActionSetting = pm.getStringPreference(Pref.STARTUP_ACTION, Pref.DEF_STARTUP_ACTION);
		if(!startUpActionSetting.equals(getString(R.string.preferences_startup_welcome))) {
		
			//user prefer to open a timeline on startup
			String contactToShow = pm.getContactToShow(this, startUpActionSetting);
			if(contactToShow!=null) {
				Intent intent = new Intent(this, TimeLineActivity.class);
				intent.putExtra(Actions.EXTRA_CONTACT_LOOKUP_KEY, contactToShow);
				startActivity(intent);
				finish();
				return;
			}
		}
			
		//normal behaviour is to show the welcome screen
		setContentView(R.layout.activity_main);
		setupActionBar();
	}

	private void setupActionBar() {
		
		actionBar = new ActionBar((ViewGroup) findViewById(R.id.actionbar), null);
		actionBar.setup();

	}
}