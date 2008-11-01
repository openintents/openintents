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

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.IBinder;
import android.util.Log;

public class UpdateCheckService extends Service {

	private static final String TAG = "UpdateCheckerService";
	private static final long CHECK_INTERVAL = 86400000; // 24 hours

	@Override
	public void onStart(final Intent intent, int startId) {
		Log.d(TAG, "started");

		// get extras from intent
		String packageName = intent
				.getStringExtra(UpdateChecker.EXTRA_PACKAGE_NAME);
		String appName = intent.getStringExtra(UpdateChecker.EXTRA_APP_NAME);
		int currVersion = intent.getIntExtra(
				UpdateChecker.EXTRA_CURRENT_VERSION, 0);
		String versionName = intent
				.getStringExtra(UpdateChecker.EXTRA_CURRENT_VERSION_NAME);
		boolean veecheck = intent.getBooleanExtra(UpdateChecker.EXTRA_VEECHECK,
				false);

		long lastCheck;
		lastCheck = getUpdateInfo(packageName);

		Log.v(TAG, "last check for " + packageName + ": "
				+ new java.util.Date(lastCheck));
		if (true || lastCheck + CHECK_INTERVAL < System.currentTimeMillis()) {

			// create update checker
			final UpdateCheckerWithNotification updateChecker;

			if (!veecheck) {
				updateChecker = new UpdateCheckerWithNotification(this,
						packageName, appName, currVersion, versionName);

			} else {
				updateChecker = new UpdateCheckerWithNotificationVeecheck(this,
						packageName, appName, currVersion, versionName);

			}

			// start in thread
			new Thread() {
				@Override
				public void run() {
					updateChecker.checkForUpdateWithNotification(intent
							.getDataString());
				}
			}.start();
		} else {
			Log.v(TAG, "last check less than 24 hours ago");
		}

	}

	private long getUpdateInfo(String packageName) {
		long lastCheck;
		Cursor cursor = getContentResolver().query(UpdateInfo.CONTENT_URI,
				new String[] { UpdateInfo._ID, UpdateInfo.LAST_CHECK },
				UpdateInfo.PACKAGE_NAME + " = ?", new String[] { packageName },
				null);
		if (cursor != null) {

			if (cursor.moveToFirst()) {
				lastCheck = cursor.getLong(1);
			} else {
				insertUpdateInfo(packageName);
				lastCheck = 0;

			}
			cursor.close();
		} else {
			insertUpdateInfo(packageName);
			lastCheck = 0;
		}
		return lastCheck;
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
