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
	 * Sets the alarm if desired by the preferences.
	 * 
	 * @param context
	 * @param firstinterval (if set to -1, then standard interval is taken)
	 */
	public static void refreshUpdateAlarm(Context context) {
	
		// Look up preferences
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean autoupdate = prefs.getBoolean(PreferencesActivity.PREFERENCE_AUTO_UPDATE, true);
		long updateinterval = Long.parseLong(prefs.getString(PreferencesActivity.PREFERENCE_UPDATE_INTERVAL, "172800000"));
		long lastupdate = prefs.getLong(PreferencesActivity.PREFERENCE_LAST_UPDATE, 0);
	
		
		long firstinterval = getFirstInterval(context, lastupdate, updateinterval);
		
		Log.d(TAG, "Autoupdate preference: " + autoupdate);
		
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
	}

	///////////////////////////////////////////////////////////////
	// Private members
	
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
