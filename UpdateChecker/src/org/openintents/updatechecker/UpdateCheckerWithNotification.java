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
		checkForUpdate(uri);
		showNotificationIfRequired();
		updateLastCheck();
	}

	private void updateLastCheck() {
		ContentValues values = new ContentValues();
		values.put(UpdateInfo.LAST_CHECK, System.currentTimeMillis());
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
		}
	}

	private void showNotification() {
		CharSequence text = mContext.getString(R.string.update_available,
				mAppName);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.icon, text,
				System.currentTimeMillis());

		if (mUpdateIntent == null) {
			mUpdateIntent = new Intent(Intent.ACTION_VIEW);
			if (getApplicationId() != null) {
				mUpdateIntent.setData(Uri.parse("market://details?id="
						+ getApplicationId()));
			} else if (mAppName != null) {
				mUpdateIntent.setData(Uri.parse("market://search?q="
						+ mContext.getString(R.string.market_search_term)));
			} else {
				//TODO
			}
		}

		mIntent = new Intent(mContext, UpdateCheckerActivity.class);
		mIntent
				.putExtra(UpdateChecker.EXTRA_LATEST_VERSION,
						getLatestVersion());
		mIntent
		.putExtra(UpdateChecker.EXTRA_LATEST_VERSION_NAME,
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
}
