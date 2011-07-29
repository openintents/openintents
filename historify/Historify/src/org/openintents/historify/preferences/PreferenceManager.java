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
