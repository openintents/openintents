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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

/**
 * Update checker based on simple text file showing notifications
 * 
 * @author muef
 * 
 */
public class UpdateCheckerWithNotification extends UpdateChecker {

	Context mContext;
	NotificationManager mNm;
	protected String mPackageName;
	private String mAppName;
	protected int mCurrentVersion;
	protected String mCurrentVersionName;
	protected Intent mIntent;
	protected Intent mUpdateIntent;

	public UpdateCheckerWithNotification(Context context, String packageName,
			String appName, int currentVersionCode, String currentVersionName) {
		mContext = context;
		mPackageName = packageName;
		mAppName = appName;
		mCurrentVersion = currentVersionCode;
		mCurrentVersionName = currentVersionName;

		if (mNm == null) {
			mNm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
		}
	}

	public void checkForUpdateWithNotification(String uri) {
		Log.v(TAG, "update with notification");
		checkForUpdate(uri);
		setMarketUpdateIntent();
		showNotificationIfRequired();
		updateLastCheck(uri);
	}
	

	public void checkForUpdateWithOutNotification(String uri) {
		Log.v(TAG, "update without notification");
		checkForUpdate(uri);
		setMarketUpdateIntent();
		showNotificationIfRequired();
		updateLastCheck(uri);
		
	}

	private void updateLastCheck(String uri) {
		ContentValues values = new ContentValues();
		values.put(UpdateInfo.LAST_CHECK, System.currentTimeMillis());
		values.put(UpdateInfo.UPDATE_URL, uri);
		values.put(UpdateInfo.LAST_CHECK_VERSION_CODE, getLatestVersion());
		values.put(UpdateInfo.LAST_CHECK_VERSION_NAME, getLatestVersionName());
		mContext.getContentResolver().update(UpdateInfo.CONTENT_URI, values,
				UpdateInfo.PACKAGE_NAME + " = ? ",
				new String[] { mPackageName });
	}

	protected void showNotificationIfRequired() {
		if ((getLatestVersion() > mCurrentVersion && mCurrentVersion > 0)
				|| (getLatestVersionName() != null
						&& mCurrentVersionName != null && getLatestVersionName()
						.equals(mCurrentVersionName))) {
			// maybe start intent immediately
			showNotification();
		} else {
			Log.v(TAG, "up-to-date or no version: " + mPackageName + " ("
					+ mCurrentVersion + ")");
			showNotification();
		}
	}

	private void showNotification() {
		CharSequence text = mContext.getString(R.string.update_available,
				mAppName);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.icon_update, text,
				System.currentTimeMillis());

		mIntent = new Intent(mContext, UpdateCheckerActivity.class);
		mIntent
				.putExtra(UpdateChecker.EXTRA_LATEST_VERSION,
						getLatestVersion());
		mIntent.putExtra(UpdateChecker.EXTRA_LATEST_VERSION_NAME,
				getLatestVersionName());
		mIntent.putExtra(UpdateChecker.EXTRA_COMMENT, getComment());
		mIntent.putExtra(UpdateChecker.EXTRA_PACKAGE_NAME, mPackageName);
		mIntent.putExtra(UpdateChecker.EXTRA_APP_NAME, mAppName);
		mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_NEW_TASK);
		mIntent.putExtra(UpdateChecker.EXTRA_UPDATE_INTENT, mUpdateIntent);

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				mIntent, 0);

		// Set the info for the views that show in the notification panel.
		notification.setLatestEventInfo(mContext, mContext
				.getText(R.string.app_name), text, contentIntent);

		// Send the notification.
		// We use the package name hash because it is a unique number for each
		// package. We use it later to
		// cancel.
		mNm.notify(mPackageName.hashCode(), notification);
	}

	private void setMarketUpdateIntent() {
		if (mUpdateIntent == null) {
			mUpdateIntent = new Intent(Intent.ACTION_VIEW);
			if (getApplicationId() != null) {
				mUpdateIntent.setData(Uri.parse("market://details?id="
						+ getApplicationId()));
			} else if (mAppName != null) {
				mUpdateIntent.setData(Uri.parse("market://search?q="
						+ mContext.getString(R.string.market_search_term)));
			} else {
				// TODO
			}
		}
	}

	public Intent getUpdateIntent() {
		return mUpdateIntent;
	}

}
