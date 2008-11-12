package org.openintents.updatechecker.activity;

import org.openintents.updatechecker.R;
import org.openintents.updatechecker.UpdateCheckService;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

public class PreferencesActivity extends PreferenceActivity implements
		OnPreferenceChangeListener {
	
	private static final String TAG = "PreferencesActivity";

	private CheckBoxPreference mAutoUpdate;
	private ListPreference mInterval;
	
	public final static String PREFERENCE_AUTO_UPDATE = "auto_update";
	public final static String PREFERENCE_UPDATE_INTERVAL = "update_interval";
	
	/**
	 * Timestamp of last update.
	 */
	public final static String PREFERENCE_LAST_UPDATE = "last_update";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.preferences);

		mAutoUpdate = (CheckBoxPreference) getPreferenceScreen()
				.findPreference(PREFERENCE_AUTO_UPDATE);
		mInterval = (ListPreference) getPreferenceScreen().findPreference(
				PREFERENCE_UPDATE_INTERVAL);
		mAutoUpdate.setOnPreferenceChangeListener(this);
	}

	public boolean onPreferenceChange(Preference preference, Object obj) {
		
		// Todo: Check if it is PREFERENCE_AUTO_UPDATE
		
		if (obj instanceof Boolean) {
			if (((Boolean) obj).booleanValue()) {
				int interval = Integer.parseInt(mInterval.getValue());
				setAlarm(this, interval, interval);
			} else {
				unsetAlarm(this);
			}
		}
		return true;
	}

	
	/**
	 * 
	 */
	public static void setAlarm(Context context, int firstinterval, int interval) {
		Intent intent = new Intent(context, UpdateCheckService.class);
		intent.setAction(UpdateCheckService.ACTION_SET_ALARM);
		intent.putExtra(UpdateCheckService.EXTRA_INTERVAL, interval);
		intent.putExtra(UpdateCheckService.EXTRA_FIRST_INTERVAL, firstinterval);
		context.startService(intent);
	}
	
	/**
	 * 
	 */
	public static void unsetAlarm(Context context) {
		Intent intent = new Intent(context, UpdateCheckService.class);
		intent.setAction(UpdateCheckService.ACTION_UNSET_ALARM);
		context.startService(intent);
	}
	
	/**
	 * Sets the alarm if desired by the preferences.
	 * 
	 * @param context
	 * @param firstinterval (if set to -1, then standard interval is taken)
	 */
	public static void refreshUpdateAlarm(Context context) {

    	// Look up preferences
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    	boolean autoupdate = prefs.getBoolean(PREFERENCE_AUTO_UPDATE, true);
    	int updateinterval = Integer.parseInt(prefs.getString(PREFERENCE_UPDATE_INTERVAL, "172800000"));
    	long lastupdate = prefs.getLong(PREFERENCE_LAST_UPDATE, 0);

		long now = System.currentTimeMillis();
		
    	long firstinterval = lastupdate - now + updateinterval;
    	
    	if (firstinterval < 5000) {
    		Log.d(TAG, "Limit first interval from " + firstinterval + " to " + 5000);
    		firstinterval = 5000;
    	} else if (firstinterval > updateinterval) {
    		Log.d(TAG, "Limit first interval from " + firstinterval + " to " + updateinterval);
    		firstinterval = updateinterval;
    	}
    	Log.d(TAG, "Autoupdate preference: " + autoupdate);
    	
    	if (autoupdate) {
    		setAlarm(context, (int) firstinterval, updateinterval);
    	}
	}
	
	/**
	 * Sets the update timestamp to now.
	 */
	public static void setUpdateTimestamp(Context context) {
		long now = System.currentTimeMillis();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(PREFERENCE_LAST_UPDATE, now);
		editor.commit();
	}

}
