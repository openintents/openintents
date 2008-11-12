package org.openintents.updatechecker.util;

import org.openintents.updatechecker.UpdateCheckService;
import org.openintents.updatechecker.activity.PreferencesActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Class for handling setting and unsetting repeated alarms.
 * 
 * @author Peli
 *
 */
public class AlarmUtils {
	public static final String TAG = "AlarmUtils";

	/**
	 * Minimum interval: one day.
	 */
	private static final long MINIMUM_INTERVAL = 24 * 3600 * 1000;
		
	/**
	 * Sets the alarm if desired by the preferences.
	 * 
	 * @param context
	 * @param firstinterval (if set to -1, then standard interval is taken)
	 */
	public static void refreshUpdateAlarm(Context context) {
	
		// Look up preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean autoupdate = prefs.getBoolean(PreferencesActivity.PREFERENCE_AUTO_UPDATE, true);
		long updateinterval = Long.parseLong(prefs.getString(PreferencesActivity.PREFERENCE_UPDATE_INTERVAL, "0"));
		long lastupdate = prefs.getLong(PreferencesActivity.PREFERENCE_LAST_UPDATE, 0);
	
		if (updateinterval <= 0) {
			// Set to minimum interval
			updateinterval = MINIMUM_INTERVAL;
			SharedPreferences.Editor editor = prefs.edit();
			Log.d(TAG, "Setting new update interval: " + updateinterval);
			editor.putBoolean(PreferencesActivity.PREFERENCE_AUTO_UPDATE, true);
			editor.putString(PreferencesActivity.PREFERENCE_UPDATE_INTERVAL, "" + updateinterval);
			editor.commit();
		}
		
		long firstinterval = getFirstInterval(context, lastupdate, updateinterval);
		
		Log.d(TAG, "Autoupdate preference: " + autoupdate + ", interval: " + updateinterval + ", last update: " + lastupdate);
		
		if (autoupdate) {
			setAlarm(context, firstinterval, updateinterval);
		} else {
			unsetAlarm(context);
		}
	}

	/**
	 * Sets the update timestamp to now.
	 */
	public static void setUpdateTimestamp(Context context) {
		long now = System.currentTimeMillis();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(PreferencesActivity.PREFERENCE_LAST_UPDATE, now);
		editor.commit();
		
		checkMinimumInterval(context);
	}

	///////////////////////////////////////////////////////////////
	// Private members
	
	/**
	 * This is a safety measure - if the timestamp is set too low,
	 * it will be doubled until MINIMUM_INTERVAL is reached.
	 * If it is less than a second, it will be set to a second.
	 */
	private static void checkMinimumInterval(Context context) {

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		long updateInterval = Long.parseLong(prefs.getString(PreferencesActivity.PREFERENCE_UPDATE_INTERVAL, "0"));
		
		if (updateInterval < MINIMUM_INTERVAL) {
			Log.d(TAG, "Update interval too short: " + updateInterval);
			SharedPreferences.Editor editor = prefs.edit();
			updateInterval *= 2;
			if (updateInterval < 1000) {
				updateInterval = 1000;
			}
			Log.d(TAG, "Setting new update interval: " + updateInterval);
			editor.putString(PreferencesActivity.PREFERENCE_UPDATE_INTERVAL, "" + updateInterval);
			editor.commit();
			
			refreshUpdateAlarm(context);
		}
	}
	
	/**
	 * Gets the first interval for setting an alarm.
	 * 
	 * It is calculated using the last time of a successful update.
	 * 
	 * @param context
	 * @return
	 */
	private static long getFirstInterval(Context context, long lastupdate, long updateinterval) {
		long now = System.currentTimeMillis();
		
		long firstinterval = lastupdate - now + updateinterval;
		
		if (firstinterval < 0) {
			Log.d(TAG, "Limit first interval from " + firstinterval + " to " + 0);
			firstinterval = 0;
		} else if (firstinterval > updateinterval) {
			Log.d(TAG, "Limit first interval from " + firstinterval + " to " + updateinterval);
			firstinterval = updateinterval;
		}
		
		return firstinterval;
	}

	/**
	 * 
	 */
	private static void setAlarm(Context context, long firstinterval, long interval) {
		Intent intent = new Intent(context, UpdateCheckService.class);
		intent.setAction(UpdateCheckService.ACTION_SET_ALARM);
		intent.putExtra(UpdateCheckService.EXTRA_INTERVAL, interval);
		intent.putExtra(UpdateCheckService.EXTRA_FIRST_INTERVAL, firstinterval);
		context.startService(intent);
	}

	/**
	 * 
	 */
	private static void unsetAlarm(Context context) {
		Intent intent = new Intent(context, UpdateCheckService.class);
		intent.setAction(UpdateCheckService.ACTION_UNSET_ALARM);
		context.startService(intent);
	}

}
