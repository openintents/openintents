/*
 * Copyright (C) 2008  OpenIntents.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.openintents.updatechecker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

public class UpdateCheckService extends Service {

	private static final String TAG = "UpdateCheckerService";
	private static final long CHECK_INTERVAL = 86400000; // 24 hours

	public static final String EXTRA_INTERVAL = "interval";
	public static final String ACTION_SET_ALARM = "org.openintents.updatechecker.SET_ALARM";
	public static final String ACTION_UNSET_ALARM = "org.openintents.updatechecker.UNSET_ALARM";
	static final String ACTION_CHECK_ALL = "org.openintents.updatechecker.CHECK_ALL";

	@Override
	public void onStart(final Intent intent, int startId) {
		Log.d(TAG, "started");

		if (ACTION_SET_ALARM.equals(intent.getAction())) {
			setAlarm(intent);
		} else if (ACTION_UNSET_ALARM.equals(intent.getAction())) {
			unsetAlarm();
		} else if (ACTION_CHECK_ALL.equals(intent.getAction())) {
			performAllUpdates();
		} else {

			performCheckForUpdates(intent);
		}

	}

	private void performAllUpdates() {
		new Thread() {
			@Override
			public void run() {
				for (PackageInfo pi : getPackageManager().getInstalledPackages(
						0)) {

					CharSequence name = getPackageManager()
							.getApplicationLabel(pi.applicationInfo);
					String versionName = pi.versionName;

					// ignore apps from black list
					if (UpdateInfo.isBlackListed(pi)) {
						continue;
					}

					Cursor cursor = getContentResolver().query(
							UpdateInfo.CONTENT_URI,
							new String[] { UpdateInfo.UPDATE_URL,
									UpdateInfo.IGNORE_VERSION_NAME,
									UpdateInfo.IGNORE_VERSION_CODE },
							UpdateInfo.PACKAGE_NAME + " = ?",
							new String[] { pi.packageName }, null);

					String updateUrl = null;
					String ignoreVersionName = null;
					int ignoreVersion = 0;
					if (cursor.moveToFirst()) {
						updateUrl = cursor.getString(0);
						ignoreVersionName = cursor.getString(1);
						ignoreVersion = cursor.getInt(2);
					} else {
						if (pi.packageName
								.startsWith(UpdateInfo.ORG_OPENINTENTS)) {
							updateUrl = "http://www.openintents.org/apks/"
									+ pi.packageName + ".txt";
						}
					}
					cursor.close();

					if (updateUrl != null) {
						try {
							sleep(250);
						} catch (InterruptedException e) {
							// ignore
						}

						UpdateCheckerWithNotification updateChecker = new UpdateCheckerWithNotification(
								UpdateCheckService.this, pi.packageName, name
										.toString(), pi.versionCode,
								versionName, updateUrl, false, ignoreVersionName, ignoreVersion);
						updateChecker.checkForUpdateWithNotification();
					}

				}
			}
		}.start();

	}

	private void performCheckForUpdates(final Intent intent) {
		// get extras from intent
		String packageName = intent
				.getStringExtra(UpdateChecker.EXTRA_PACKAGE_NAME);
		String appName = intent.getStringExtra(UpdateChecker.EXTRA_APP_NAME);
		int currVersion = intent.getIntExtra(
				UpdateChecker.EXTRA_CURRENT_VERSION, 0);
		String versionName = intent
				.getStringExtra(UpdateChecker.EXTRA_CURRENT_VERSION_NAME);

		
		long lastCheck = 0;
		String ignoreVersionName = null;
		int ignoreVersion = 0;
		
		// get update info from db
		Cursor cursor = getContentResolver().query(UpdateInfo.CONTENT_URI,
				new String[] { UpdateInfo._ID, UpdateInfo.LAST_CHECK, UpdateInfo.IGNORE_VERSION_NAME , UpdateInfo.IGNORE_VERSION_CODE  },
				UpdateInfo.PACKAGE_NAME + " = ?", new String[] { packageName },
				null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				lastCheck = cursor.getLong(1);
				ignoreVersionName = cursor.getString(2);
				ignoreVersion = cursor.getInt(3);
			} else {
				insertUpdateInfo(packageName);
				lastCheck = 0;

			}
			cursor.close();
		} else {
			insertUpdateInfo(packageName);
			lastCheck = 0;
		}
				

		Log.v(TAG, "last check for " + packageName + ": "
				+ new java.util.Date(lastCheck));
		if (lastCheck + CHECK_INTERVAL < System.currentTimeMillis()) {

			// create update checker
			final UpdateCheckerWithNotification updateChecker;

			updateChecker = new UpdateCheckerWithNotification(this,
					packageName, appName, currVersion, versionName, intent
							.getDataString(), false, ignoreVersionName, ignoreVersion);

			// start in thread
			new Thread() {
				@Override
				public void run() {
					try {
						sleep(250);
					} catch (InterruptedException e) {
						// ignore
					}
					updateChecker.checkForUpdateWithNotification();
				}
			}.start();
		} else {
			Log.v(TAG, "last check less than 24 hours ago");
		}
	}

	private void unsetAlarm() {
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent i = new Intent(this, UpdateCheckService.class);
		i.setAction(ACTION_CHECK_ALL);
		PendingIntent pi = PendingIntent.getService(this, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(pi);
		Log.v(TAG, "Unset alarm.");
	}

	private void setAlarm(Intent intent) {
		int interval = intent.getIntExtra(EXTRA_INTERVAL, -1);
		if (interval > 0) {
			long time = System.currentTimeMillis();

			Intent i = new Intent(this, UpdateCheckService.class);
			i.setAction(ACTION_CHECK_ALL);
			PendingIntent pi = PendingIntent.getService(this, 0, i,
					PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			am.setRepeating(AlarmManager.RTC, time + interval, interval, pi);
			Log.v(TAG, "Set alarm.");
		}

	}

	
	private void insertUpdateInfo(String packageName) {
		ContentValues values = new ContentValues();
		values.put(UpdateInfo.PACKAGE_NAME, packageName);
		values.put(UpdateInfo.LAST_CHECK, 0);

		getContentResolver().insert(UpdateInfo.CONTENT_URI, values);

	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "stopped");

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	// No need to import IUpdateCheckerService if it's in the same project.
	private final IUpdateCheckerService.Stub mBinder = new IUpdateCheckerService.Stub() {

		public void checkForUpdate(String link, IUpdateCheckerServiceCallback cb) {
			new Thread(new UpdateCheckerWithCallback(link, cb)).start();
		}

	};

}
