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

import org.openintents.updatechecker.activity.UpdateCheckerActivity;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;

public class UpdateInfo implements BaseColumns {

	public static final String LAST_CHECK = "last_check";
	public static final String PACKAGE_NAME = "package_name";
	public static final String UPDATE_URL = "update_url";

	public static final String DEFAULT_SORT_ORDER = LAST_CHECK;

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.openintents.updates";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.openintents.updates";

	public static final String AUTHORITY = "org.openintents.updateinfo";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/info");
	public static final String LAST_CHECK_VERSION_CODE = "last_check_version_code";
	public static final String LAST_CHECK_VERSION_NAME = "last_check_version_name";

	public static final String ORG_OPENINTENTS = "org.openintents";

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
				|| pi.packageName.startsWith("com.android");
	}

	public static void checkAlarm(Context context) {
		Intent i = new Intent(context, UpdateCheckService.class);
		i.setAction(UpdateCheckService.ACTION_CHECK_ALL);
		PendingIntent pi = PendingIntent.getService(context, 0, i,
				PendingIntent.FLAG_NO_CREATE);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		Intent intent = new Intent(context, UpdateCheckService.class);
		if (prefs.getBoolean(
				"auto_update", true)) {

			if (pi == null) {
				intent.setAction(UpdateCheckService.ACTION_SET_ALARM);
				intent.putExtra(UpdateCheckService.EXTRA_INTERVAL, Integer.parseInt(prefs.getString("update_interval", "604800000")));
				context.startService(intent);
			}
		} else {
			if (pi != null) {
				intent.setAction(UpdateCheckService.ACTION_UNSET_ALARM);
				context.startService(intent);
			}
		}

	}
}
