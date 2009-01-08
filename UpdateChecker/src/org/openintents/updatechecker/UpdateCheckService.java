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

import org.openintents.updatechecker.db.UpdateInfo;
import org.openintents.updatechecker.util.AlarmUtils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

public class UpdateCheckService extends Service {

	private static final String TAG = "UpdateCheckerService";

	/**
	 * Regular interval.
	 */
	public static final String EXTRA_INTERVAL = "interval";
	
	/**
	 * First interval.
	 * Used after reboot to set a shorter first interval.
	 * e.g. 1 day, 7 days, 7 days, 7 days, etc.
	 * 
	 * Otherwise many reboots can prevent the alarm from ever starting.
	 */
	public static final String EXTRA_FIRST_INTERVAL = "firstinterval";
	public static final String ACTION_SET_ALARM = "org.openintents.updatechecker.SET_ALARM";
	public static final String ACTION_UNSET_ALARM = "org.openintents.updatechecker.UNSET_ALARM";
	static final String ACTION_CHECK_ALL = "org.openintents.updatechecker.CHECK_ALL";

	@Override
	public void onStart(final Intent intent, int startId) {
		Log.d(TAG, "started");

		if (ACTION_SET_ALARM.equals(intent.getAction())) {
			setAlarm(intent, startId);
		} else if (ACTION_UNSET_ALARM.equals(intent.getAction())) {
			unsetAlarm(startId);
		} else if (ACTION_CHECK_ALL.equals(intent.getAction())) {
			performAllUpdates(startId);
		} else {
			// we don't want to allow arbitrary intents to manipulate the update
			// urls
			// 
			// performCheckForUpdates(intent);
		}

	}

	private void performAllUpdates(final int startId) {
		new Thread() {
			@Override
			public void run() {
				for (PackageInfo pi : getPackageManager().getInstalledPackages(
						0)) {

					CharSequence name = getPackageManager()
							.getApplicationLabel(pi.applicationInfo);
					String versionName = pi.versionName;

					// ignore apps from black list
					if (UpdateInfo.isBlackListed(UpdateCheckService.this, pi)) {
						continue;
					}

					Cursor cursor = getContentResolver().query(
							UpdateInfo.CONTENT_URI,
							new String[] { UpdateInfo.UPDATE_URL,
									UpdateInfo.IGNORE_VERSION_NAME,
									UpdateInfo.IGNORE_VERSION_CODE,
									UpdateInfo.LAST_CHECK,
									UpdateInfo.NO_NOTIFICATIONS },
							UpdateInfo.PACKAGE_NAME + " = ?",
							new String[] { pi.packageName }, null);

					String updateUrl = null;
					String ignoreVersionName = null;
					int ignoreVersion = 0;
					long lastCheck = 0;
					boolean noNotifications = false;
					
					// we always use the meta data
					updateUrl = UpdateInfo
					.determineUpdateUrlFromPackageName(
							UpdateCheckService.this, pi);

					if (cursor.moveToFirst()) {	
						if (updateUrl == null){
							updateUrl = cursor.getString(0);
						}
						ignoreVersionName = cursor.getString(1);
						ignoreVersion = cursor.getInt(2);
						lastCheck = cursor.getLong(3);
						noNotifications = cursor.getInt(4) > 0;
					} else {
						
						UpdateInfo.insertUpdateInfo(UpdateCheckService.this,
								pi.packageName);
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
								versionName, updateUrl, false,
								ignoreVersionName, ignoreVersion, lastCheck,
								noNotifications);
						updateChecker.checkForUpdateWithNotification();
					}

				}
				
				AlarmUtils.setUpdateTimestamp(UpdateCheckService.this);
				
				stopSelfResult(startId);
			}
		}.start();

	}


	private void unsetAlarm(int startId) {
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
		Intent i = new Intent(this, UpdateCheckService.class);
		i.setAction(ACTION_CHECK_ALL);
		PendingIntent pi = PendingIntent.getService(this, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT);
		am.cancel(pi);
		Log.v(TAG, "Unset alarm.");
		stopSelfResult(startId);
	}

	private void setAlarm(Intent intent, int startId) {
		long interval = intent.getLongExtra(EXTRA_INTERVAL, -1);
		long firstinterval = intent.getLongExtra(EXTRA_FIRST_INTERVAL, -1);
		if (firstinterval < 0) {
			firstinterval = interval;
		}
		if (interval > 0) {
			long time = System.currentTimeMillis();

			Intent i = new Intent(this, UpdateCheckService.class);
			i.setAction(ACTION_CHECK_ALL);
			PendingIntent pi = PendingIntent.getService(this, 0, i,
					PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
			am.setRepeating(AlarmManager.RTC, time + firstinterval, interval, pi);
			Log.v(TAG, "Set repeating alarm. First interval: " + firstinterval + ", repeated interval: " + interval);
		}
		stopSelfResult(startId);

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
