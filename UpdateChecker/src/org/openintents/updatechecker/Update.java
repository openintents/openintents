package org.openintents.updatechecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class Update {
	public static class CheckerThread extends Thread {

		private Context mContext;

		public CheckerThread(Context context) {
			mContext = context;
		}

		@Override
		public void run() {
			try {
				sleep(250);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			checkForUpdate(mContext);
		}

	}

	private static final String TAG = "Update";
	private static final String PREFERENCES_LAST_VERSION_CHECK = "last_version_check";
	protected static final String PREFERENCES_LAST_VERSION_CODE_DENIED = "last_version_code_denied";
	private static final String PREFERENCES_PENDING_UPDATE_VERSION = "pending_update_version";
	private static final String PREFERENCES_PENDING_UPDATE_APPLICATION_ID = "pending_update_application_id";
	private static final String PREFERENCES_PENDING_UPDATE_COMMENT = "pending_update_comment";

	/**
	 * Period between checks. Currently set to 24 hours (in milliseconds).
	 */
	private static long CHECK_PERIOD = 24 * 3600 * 1000;

	public static void checkForUpdateInThread(final Context context) {
		new CheckerThread(context).start();		
	}

	public static void checkForUpdate(final Context context) {

		// Get last time checked from shared preferences:
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		if (skipCheck(context, prefs)) {
			// Don't need to check too often.
			Log.i(TAG, "Skipping update test.");
			return;
		}

		int latestVersion = -1;
		String newApplicationId = null;
		String comment = null;

		try {
			String link = "http://www.openintents.org/apks/"
					+ context.getPackageName() + ".txt";
			Log.d(TAG, "Looking for version at " + link);
			URL u = new URL(link);
			Object content = u.openConnection().getContent();
			if (content instanceof InputStream) {
				InputStream is = (InputStream) content;
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));
				latestVersion = Integer.parseInt(reader.readLine());
				Log.d(TAG, "Lastest version available: " + latestVersion);

				newApplicationId = reader.readLine();
				Log.d(TAG, "New version application ID: " + newApplicationId);

				comment = reader.readLine();
				Log.d(TAG, "comment: " + comment);
				
				storePendingUpdate(prefs, latestVersion, newApplicationId,
						comment);

			} else {
				Log.d(TAG, "Unknown server format: "
						+ ((String) content).substring(0, 100));
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "MalformedURLException", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}

	}

	private static void storePendingUpdate(SharedPreferences prefs,
			int latestVersion, String newApplicationId, String comment) {
		Editor editor = prefs.edit();
		editor.putInt(PREFERENCES_PENDING_UPDATE_VERSION, latestVersion);
		editor.putString(PREFERENCES_PENDING_UPDATE_APPLICATION_ID,
				newApplicationId);
		editor.putString(PREFERENCES_PENDING_UPDATE_COMMENT, comment);
		editor.commit();
	}

	public static void checkForPendingUpdate(final Context context) {
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

		final SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		int latestVersion = prefs
				.getInt(PREFERENCES_PENDING_UPDATE_VERSION, -1);
		String newApplicationId = prefs.getString(
				PREFERENCES_PENDING_UPDATE_APPLICATION_ID, null);
		String comment = prefs.getString(PREFERENCES_PENDING_UPDATE_COMMENT,
				null);

		if (currentVersion > 0 && latestVersion > 0
				&& currentVersion < latestVersion) {

			// check whether upgrade was denied previously
			int lastVersionCodeDenied = prefs.getInt(
					PREFERENCES_LAST_VERSION_CODE_DENIED, -1);

			if (lastVersionCodeDenied >= latestVersion) {
				// don't ask user again, upgrade was canceled previously.
				Log.d(TAG, "upgrade canceled previously");
			} else {
				final int latestVersionFinal = latestVersion;
				final String applicationIdFinal = newApplicationId;

				String msg = context.getString(R.string.update_available,
						comment);

				(new AlertDialog.Builder(context).setMessage(msg).setIcon(
						android.R.drawable.ic_dialog_alert).setPositiveButton(
						android.R.string.yes, new OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {
								// update required
								Intent intent = new Intent(Intent.ACTION_VIEW);
								intent
										.setData(Uri
												.parse("market://search?q="
														+ context
																.getString(R.string.market_search_term)));
								
								//intent.setData(Uri.parse("market://details?id="
								// + applicationIdFinal));

								try {
									context.startActivity(intent);
								} catch (ActivityNotFoundException e) {
									Toast.makeText(context,
											R.string.market_not_available,
											Toast.LENGTH_SHORT).show();
									Log.e(TAG, "Market not installed");
								}

							}

						}).setNegativeButton(android.R.string.no,
						new OnClickListener() {

							public void onClick(DialogInterface arg0, int arg1) {
								// Remember the version that has been denied in
								// shared preferences.
								SharedPreferences.Editor editor = prefs.edit();
								editor.putInt(
										PREFERENCES_LAST_VERSION_CODE_DENIED,
										latestVersionFinal);
								editor.commit();

							}

						})).show();
			}

		} else {
			Log.d(TAG, "Version up-to-date");
		}
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
	
	/** 
	 * Convenience function to check for update in the background.
	 * @param context
	 */
	public static void check(final Context context) {
		Update.checkForPendingUpdate(context);
		Update.checkForUpdateInThread(context);
	}
}
