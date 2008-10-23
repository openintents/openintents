package org.openintents.pocketplay;

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

import org.openintents.pocketplay.R;

import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class PocketplaySettings extends PreferenceActivity implements
		OnPreferenceChangeListener {


	private static final String _TAG = "PocketplaySettings";

	// private NotificationManager mNM;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.pocketplay_preferences);


	}

	public boolean onPreferenceChange(Preference preference, Object obj) {
		return true;
	}

}/* eoc */