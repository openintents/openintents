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

import java.net.URL;

import org.openintents.updatechecker.activity.UpdateCheckerActivity;

import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;

public class UpdateInfo implements BaseColumns {

	public static final String LAST_CHECK = "last_check";
	public static final String PACKAGE_NAME = "package_name";
	public static final String UPDATE_URL = "update_url";
	public static final String IGNORE_VERSION_CODE = "ignore_version_code";
	public static final String IGNORE_VERSION_NAME = "ignore_version_name";
	public static final String NO_NOTIFICATIONS = "no_notifications";

	public static final String DEFAULT_SORT_ORDER = LAST_CHECK;

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.openintents.updates";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.openintents.updates";

	public static final String AUTHORITY = "org.openintents.updateinfo";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/info");

	public static final String ORG_OPENINTENTS = "org.openintents";
	public static final long CHECK_INTERVAL = 86400000; // 24 hours
	public static final String META_DATA_UPDATE_URL = "org.openintents.updatechecker.UPDATE_URL";
	private static final String TAG = "UpdateInfo";

	public static Intent createUpdateActivityIntent(Context mContext,
			UpdateChecker mChecker, String mPackageName, String mAppName) {
		Intent intent = new Intent(mContext, UpdateCheckerActivity.class);
		intent.putExtra(UpdateChecker.EXTRA_LATEST_VERSION, mChecker
				.getLatestVersion());
		intent.putExtra(UpdateChecker.EXTRA_LATEST_VERSION_NAME, mChecker
				.getLatestVersionName());
		intent.putExtra(UpdateChecker.EXTRA_COMMENT, mChecker.getComment());
		intent.putExtra(UpdateChecker.EXTRA_PACKAGE_NAME, mPackageName);
		intent.putExtra(UpdateChecker.EXTRA_APP_NAME, mAppName);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(UpdateChecker.EXTRA_UPDATE_INTENT, mChecker
				.getUpdateIntent());
		return intent;
	}

	public static Intent createUpdateActivityIntent(Context mContext,
			int latestVersion, String latestVersionName, String comment,
			String mPackageName, String mAppName, Intent updateIntent) {
		Intent intent = new Intent(mContext, UpdateCheckerActivity.class);
		intent.putExtra(UpdateChecker.EXTRA_LATEST_VERSION, latestVersion);
		intent.putExtra(UpdateChecker.EXTRA_LATEST_VERSION_NAME,
				latestVersionName);
		intent.putExtra(UpdateChecker.EXTRA_COMMENT, comment);
		intent.putExtra(UpdateChecker.EXTRA_PACKAGE_NAME, mPackageName);
		intent.putExtra(UpdateChecker.EXTRA_APP_NAME, mAppName);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra(UpdateChecker.EXTRA_UPDATE_INTENT, updateIntent);
		return intent;
	}

	public static boolean isBlackListed(PackageInfo pi) {
		return (pi.versionName == null && pi.versionCode == 0)
				|| pi.packageName.startsWith("com.android")
				|| pi.packageName.startsWith("android");
	}

	public static void checkAlarm(Context context) {
		Intent i = new Intent(context, UpdateCheckService.class);
		i.setAction(UpdateCheckService.ACTION_CHECK_ALL);
		PendingIntent pi = PendingIntent.getService(context, 0, i,
				PendingIntent.FLAG_NO_CREATE);

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);

		Intent intent = new Intent(context, UpdateCheckService.class);
		if (prefs.getBoolean("auto_update", true)) {

			if (pi == null) {
				intent.setAction(UpdateCheckService.ACTION_SET_ALARM);
				intent.putExtra(UpdateCheckService.EXTRA_INTERVAL, Integer
						.parseInt(prefs.getString("update_interval",
								"604800000")));
				context.startService(intent);
			}
		} else {
			if (pi != null) {
				intent.setAction(UpdateCheckService.ACTION_UNSET_ALARM);
				context.startService(intent);
			}
		}

	}

	public static void insertUpdateInfo(Context context, String packageName) {
		ContentValues values = new ContentValues();
		values.put(UpdateInfo.PACKAGE_NAME, packageName);
		values.put(UpdateInfo.LAST_CHECK, 0);
		context.getContentResolver().insert(UpdateInfo.CONTENT_URI, values);

	}

	public static String determineUpdateUrlFromPackageName(Context context,
			PackageInfo pi) {

		String updateUrl = null;

		// try meta data of package
		Bundle md = null;
		try {
			md = context.getPackageManager().getApplicationInfo(pi.packageName,
					PackageManager.GET_META_DATA).metaData;
		} catch (NameNotFoundException e) {
			// ignore
		}

		if (md != null) {
			updateUrl = md.getString(UpdateInfo.META_DATA_UPDATE_URL);
		}

		// try OI url
		if (updateUrl == null
				&& pi.packageName.startsWith(UpdateInfo.ORG_OPENINTENTS)) {
			updateUrl = "http://www.openintents.org/apks/" + pi.packageName
					+ ".txt";
		}

		// try andappstore url
		if (updateUrl == null) {
			updateUrl = "http://andappstore.com/AndroidPhoneApplications/updates/!veecheck?p="
					+ pi.packageName;
			try {
				// try connection
				new URL(updateUrl).openConnection();
			} catch (Exception e) {
				Log.v(TAG, "AndAppStore url:" + e.getMessage());
				updateUrl = null;
			}
		}

		return updateUrl;

	}

	public static void setNoUpdates(Context context, String packageName,
			boolean noUpdates) {

		ContentValues values = new ContentValues();
		values.put(UpdateInfo.NO_NOTIFICATIONS, noUpdates);
		context.getContentResolver().update(UpdateInfo.CONTENT_URI, values,
				UpdateInfo.PACKAGE_NAME + " = ? ",
				new String[] { packageName });

	}
}
