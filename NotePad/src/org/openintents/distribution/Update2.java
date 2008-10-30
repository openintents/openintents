package org.openintents.distribution;

import org.openintents.distribution.Update.UpdateCheckerClient;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.Html.TagHandler;
import android.util.Log;

public class Update2 {
	final static String ACTION_CHECK_VERSION = "org.openintents.intents.CHECK_VERSION";

	private static final String PREFERENCES_LAST_VERSION_CHECK = "org.openintents.update.last_version_check";
	private static final String PREFERENCES_LAST_VERSION_CODE_DENIED = "org.openintents.update.last_version_code_denied";

	private static final String TAG = "Update2";


	/**
	 * Period between checks. Currently set to 24 hours (in milliseconds).
	 */
	private static long CHECK_PERIOD = 24 * 3600 * 1000;

	public static void check(Context context) {
		Intent service = new Intent();
		service.setAction(ACTION_CHECK_VERSION);
		String link = "http://www.openintents.org/apks/"
			+ context.getPackageName() + ".txt";
		
		service.setData(Uri.parse(link));
		service.putExtra("package_name", context.getPackageName());
		service.putExtra("app_name", context.getString(org.openintents.notepad.R.string.app_name));
		
		int currentVersion = -1;
		try {
			PackageInfo pi = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			currentVersion = pi.versionCode;
			Log.d(TAG, "Package name: " + pi.packageName);
			Log.d(TAG, "Version name: " + pi.versionName);
			Log.d(TAG, "Version code: " + pi.versionCode);
			Log.d(TAG, "Current version: " + currentVersion);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "NameNotFoundException", e);
		}
		
		service.putExtra("current_version", currentVersion);
		context.startService(service );
	}
	
	public static void checkForUpdate(final Context context) {

		// Get last time checked from shared preferences:
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		if (false && skipCheck(context, prefs)) {
			// Don't need to check too often.
			Log.i(TAG, "Skipping update test.");
			return;
		}

		check(context);
	}
	

	/**
	 * Test when last check was done, and remember time of last check in shared
	 * preferences.
	 * 
	 * @param context
	 * @return true if checking can be skipped.
	 */
	private static boolean skipCheck(final Context context,
			SharedPreferences prefs) {
		long currentTime = System.currentTimeMillis();

		long lastCheckTime = prefs.getLong(PREFERENCES_LAST_VERSION_CHECK, -1);

		if (lastCheckTime > 0 && currentTime - lastCheckTime < CHECK_PERIOD) {
			// can skip Update test
			return true;
		}

		// Remember the current time in shared preferences.
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(PREFERENCES_LAST_VERSION_CHECK, currentTime);
		editor.commit();

		// Do the check (don't skip).
		return false;
	}
}
