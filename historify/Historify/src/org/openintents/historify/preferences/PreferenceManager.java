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

package org.openintents.historify.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

	public static final String FILE_NAME = "preferences";
	
	private static PreferenceManager instance;
	
	public static synchronized PreferenceManager getInstance(Context context) {
		if(instance==null)
			instance = new PreferenceManager(context.getApplicationContext().getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE));
		return instance;
	}
	
	private SharedPreferences mSharedPreferences;
	
	public PreferenceManager(SharedPreferences sharedPreferences) {
		mSharedPreferences = sharedPreferences;
	}
	
	public synchronized boolean getBooleanPreference(String key, boolean defaultValue) {
		return mSharedPreferences.getBoolean(key, defaultValue);
	}
	
	public synchronized void setPreference(String key, boolean value) {
		mSharedPreferences.edit().putBoolean(key, value).commit();
	}
}
