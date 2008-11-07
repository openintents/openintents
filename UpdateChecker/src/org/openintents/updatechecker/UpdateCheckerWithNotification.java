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
import android.util.Log;

/**
 * Update checker based on simple text file showing notifications
 * 
 * @author muef
 * 
 */
public class UpdateCheckerWithNotification {

	private static final String TAG = "UpdateCheckerWithNotification";
	Context mContext;
	NotificationManager mNm;

	protected String mPackageName;
	private String mAppName;
	protected int mCurrentVersion;
	protected String mCurrentVersionName;
	protected Intent mIntent;
	protected UpdateChecker mChecker;
	private boolean mTempUri;
	private String mUri;

	public UpdateCheckerWithNotification(Context context, String packageName,
			String appName, int currentVersionCode, String currentVersionName,
			String uri, boolean tempUri) {
		mContext = context;
		mPackageName = packageName;
		mAppName = appName;
		mCurrentVersion = currentVersionCode;
		mCurrentVersionName = currentVersionName;
		mUri = uri;
		mTempUri = tempUri;

		if (mNm == null) {
			mNm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		mChecker = new UpdateChecker();
	}

	public void checkForUpdateWithNotification() {
		Log.v(TAG, "update with notification");
		mChecker.checkForUpdate(mUri);
		mChecker.setMarketUpdateIntent(mAppName);
		showNotificationIfRequired();
		updateLastCheck(mUri);
	}

	public boolean checkForUpdateWithOutNotification() {
		Log.v(TAG, "update without notification");
		mChecker.checkForUpdate(mUri);
		mChecker.setMarketUpdateIntent(mAppName);
		updateLastCheck(mUri);
		return isUpdateRequired();

	}

	private void updateLastCheck(String uri) {
		ContentValues values = new ContentValues();
		values.put(UpdateInfo.LAST_CHECK, System.currentTimeMillis());
		values.put(UpdateInfo.UPDATE_URL, uri);
		values.put(UpdateInfo.LAST_CHECK_VERSION_CODE, mChecker
				.getLatestVersion());
		values.put(UpdateInfo.LAST_CHECK_VERSION_NAME, mChecker
				.getLatestVersionName());
		mContext.getContentResolver().update(UpdateInfo.CONTENT_URI, values,
				UpdateInfo.PACKAGE_NAME + " = ? ",
				new String[] { mPackageName });
	}

	protected void showNotificationIfRequired() {
		if (isUpdateRequired()) {
			// maybe start intent immediately
			showNotification();
		} else {
			Log.v(TAG, "up-to-date or no version: " + mPackageName + " ("
					+ mCurrentVersion + ")");
			showNotification();
		}
	}

	private boolean isUpdateRequired() {
		return (mChecker.getLatestVersion() > mCurrentVersion && mCurrentVersion > 0)
				|| (mChecker.getLatestVersionName() != null
						&& mCurrentVersionName != null && mChecker
						.getLatestVersionName().equals(mCurrentVersionName));
	}

	private void showNotification() {
		CharSequence text = mContext.getString(R.string.update_available,
				mAppName);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.icon_update,
				text, System.currentTimeMillis());

		mIntent = createUpdateActivityIntent();

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

	public Intent getUpdateIntent() {
		return mChecker.getUpdateIntent();
	}

	public Intent createUpdateActivityIntent() {
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

	public String getLatestVersionName() {
		return mChecker.getLatestVersionName();
	}

	public String getComment() {
		return mChecker.getComment();
	}

}
