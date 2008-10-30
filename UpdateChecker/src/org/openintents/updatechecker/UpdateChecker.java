package org.openintents.updatechecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.DeadObjectException;
import android.os.RemoteException;
import android.util.Log;

public class UpdateChecker implements Runnable {
	private static final String TAG = "UpdateChecker";
	public static final String EXTRA_LATEST_VERSION = "latest_version";
	public static final String EXTRA_COMMENT = "comment";

	private String mLink;
	private int mLatestVersion;
	private String mComment;
	private IUpdateCheckerServiceCallback mCallback;
	private String mNewApplicationId;

	public UpdateChecker(String link, IUpdateCheckerServiceCallback cb) {

		mLink = link;
		mCallback = cb;
	}

	public void run() {
		checkForUpdate(mLink);

		sendResult();

	}

	private void sendResult() {
		Log.v(TAG, "send result");
		try {

			mCallback
					.onVersionChecked(mLatestVersion, mNewApplicationId, mComment);
		} catch (DeadObjectException e) {
			// The IUpdateCheckerServiceCallback will take care of
			// removing
			// the dead object for us.
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	public void checkForUpdate(String link) {

		mLatestVersion = -1;
		mComment = null;
		mNewApplicationId = null;

		try {
			link = "http://www.openintents.org/apks/"
				+ "org.openintents.news" + ".txt";
			Log.d(TAG, "Looking for version at " + link);
			URL u = new URL(link);
			Object content = u.openConnection().getContent();
			if (content instanceof InputStream) {
				InputStream is = (InputStream) content;
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is));
				mLatestVersion = Integer.parseInt(reader.readLine());
				Log.d(TAG, "Lastest version available: " + mLatestVersion);

				mNewApplicationId = reader.readLine();
				Log.d(TAG, "New version application ID: " + mNewApplicationId);

				mComment = reader.readLine();
				Log.d(TAG, "comment: " + mComment);

			} else {
				Log.d(TAG, "Unknown server format: "
						+ ((String) content).substring(0, 100));
			}
		} catch (MalformedURLException e) {
			Log.e(TAG, "MalformedURLException", e);
		} catch (IOException e) {
			Log.e(TAG, "IOException", e);
		}

	}
}