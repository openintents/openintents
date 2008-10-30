package org.openintents.updatechecker;

import org.openintents.updatechecker.activity.UpdateCheckerActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class UpdateCheckerWithNotification extends UpdateChecker {

	Context mContext;
	NotificationManager mNm;
	private String mPackageName;
	private String mAppName;
	private int mCurrentVersion;

	public UpdateCheckerWithNotification(Context context, String packageName,
			String appName, int currentVersionCode) {
		mContext = context;
		mPackageName = packageName;
		mAppName = appName;
		mCurrentVersion = currentVersionCode;
		if (mNm == null) {
			mNm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		}
	}

	public void checkForUpdateWithNotification(String link) {
		checkForUpdate(link);

		if (getLatestVersion() > 0 && getLatestVersion() > mCurrentVersion) {
			showNotification();
		} else {
			Log.v(TAG, "up-to-date: " + mPackageName + " (" + mCurrentVersion
					+ ")");
		}

	}

	private void showNotification() {
		CharSequence text = mContext.getString(R.string.update_available,
				mAppName);

		// Set the icon, scrolling text and timestamp
		Notification notification = new Notification(R.drawable.icon, text,
				System.currentTimeMillis());

		Intent intent = new Intent(mContext, UpdateCheckerActivity.class);
		intent.putExtra(UpdateChecker.EXTRA_LATEST_VERSION, getLatestVersion());
		intent.putExtra(UpdateChecker.EXTRA_COMMENT, getComment());
		intent.putExtra(UpdateChecker.EXTRA_PACKAGE_NAME, mPackageName);
		intent.putExtra(UpdateChecker.EXTRA_APP_NAME, mAppName);

		// The PendingIntent to launch our activity if the user selects this
		// notification
		PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
				intent, 0);

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
