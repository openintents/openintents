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
import org.openintents.updatechecker.util.CompareVersions;

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
	private String mIgnoreVersionName;
	private int mIgnoreVersion;
	private long mLastCheck;
	private boolean mNoNotifications;

	public UpdateCheckerWithNotification(Context context, String packageName,
			String appName, int currentVersionCode, String currentVersionName,
			String uri, boolean tempUri, String ignoreVersionName,
			int ignoreVersion, long lastCheck, boolean noNotifications) {
		mContext = context;
		mPackageName = packageName;
		mAppName = appName;
		mCurrentVersion = currentVersionCode;
		mCurrentVersionName = currentVersionName;
		mUri = uri;
		mTempUri = tempUri;
		mIgnoreVersionName = ignoreVersionName;
		mIgnoreVersion = ignoreVersion;
		mLastCheck = lastCheck;
		mNoNotifications = noNotifications;

		if (mNm == null) {
			mNm = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
		}
		mChecker = new UpdateChecker(context, mPackageName, mCurrentVersion, mCurrentVersionName);
	}

	public void checkForUpdateWithNotification() {
		Log.v(TAG, "update with notification");
		if (mLastCheck + UpdateInfo.CHECK_INTERVAL < System.currentTimeMillis()) {
			mChecker.checkForUpdate(mUri);
			mChecker.setMarketUpdateIntent(mPackageName, mAppName);
			if (!mNoNotifications) {
				showNotificationIfRequired();
			}
			updateLastCheck(mUri);
		}
	}

	/**
	 * ignores last check
	 * 
	 * @return
	 */
	public boolean checkForUpdateWithOutNotification() {
		Log.v(TAG, "update without notification");
		mChecker.checkForUpdate(mUri);
		mChecker.setMarketUpdateIntent(mPackageName, mAppName);
		updateLastCheck(mUri);
		return isUpdateRequired();

	}

	private void updateLastCheck(String uri) {
		//Log.d(TAG, "update table: " + mChecker.getComment());
		
		ContentValues values = new ContentValues();
		values.put(UpdateInfo.LAST_CHECK, System.currentTimeMillis());
		values.put(UpdateInfo.LATEST_VERSION_CODE, mChecker.getLatestVersion());
		values.put(UpdateInfo.LATEST_VERSION_NAME, mChecker.getLatestVersionName());
		values.put(UpdateInfo.LATEST_COMMENT, mChecker.getComment());
		if (!mTempUri) {
			// only update uri if requested
			values.put(UpdateInfo.UPDATE_URL, uri);
		}
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
		}
	}

	/**
	 * Whether update is required:
	 *  - either larger version code
	 *  - or larger version name
	 *  - and not ignored.
	 * @return
	 */
	private boolean isUpdateRequired() {
		/*
		Log.d(TAG, "Application: " + mAppName);
		Log.d(TAG, " - current version: " + mCurrentVersion);
		Log.d(TAG, " - latest version: " + mChecker.getLatestVersion());
		Log.d(TAG, " - current version name: " + mCurrentVersionName);
		Log.d(TAG, " - latest version name: " + mChecker.getLatestVersionName());
		*/
		
		/*
		boolean currentDiffer = (mChecker.getLatestVersion() > mCurrentVersion && mCurrentVersion > 0)
				|| (mChecker.getLatestVersionName() != null
						&& mCurrentVersionName != null && !mChecker
						.getLatestVersionName().equals(mCurrentVersionName));
		*/
		boolean newerVersionAvailable = CompareVersions.isNewerVersionAvailable(
				mCurrentVersion, mChecker.getLatestVersion(),
				mCurrentVersionName, mChecker.getLatestVersionName());
		
		boolean ignore = (mIgnoreVersion >= mChecker.getLatestVersion() && mChecker
				.getLatestVersion() > 0)
				|| (mIgnoreVersionName != null
						&& mChecker.getLatestVersionName() != null && mIgnoreVersionName
						.equals(mChecker.getLatestVersionName()));
		return newerVersionAvailable && !ignore;
	}

	private void showNotification() {
		CharSequence text = mContext.getString(R.string.update_available,
				mAppName);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.icon_update,
				text, System.currentTimeMillis());

		mIntent = createUpdateActivityIntent(true);

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				mIntent, PendingIntent.FLAG_ONE_SHOT);

		// Set the info for the views that show in the notification panel.
		notification
				.setLatestEventInfo(mContext, mAppName, text, contentIntent);

		// Send the notification.
		// We use the package name hash because it is a unique number for each
		// package. We use it later to
		// cancel.
		mNm.notify(mPackageName.hashCode(), notification);
	}

	public Intent getUpdateIntent() {
		return mChecker.getUpdateIntent();
	}

	public Intent createUpdateActivityIntent(boolean setNewFlag) {
		return UpdateInfo.createUpdateActivityIntent(mContext, mChecker,
				mPackageName, mAppName, setNewFlag);
	}

	public String getLatestVersionName() {
		return mChecker.getLatestVersionName();
	}

	public String getComment() {
		return mChecker.getComment();
	}

	public int getLatestVersion() {
		return mChecker.getLatestVersion();
	}

	
}
