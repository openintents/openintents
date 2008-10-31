package org.openintents.updatechecker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import android.content.Intent;
import android.util.Log;

public class UpdateChecker {
	protected static final String TAG = "UpdateChecker";
	public static final String EXTRA_LATEST_VERSION = "latest_version";
	public static final String EXTRA_COMMENT = "comment";
	public static final String EXTRA_PACKAGE_NAME = "package_name";
	public static final String EXTRA_APP_NAME = "app_name";
	public static final String EXTRA_CURRENT_VERSION = "currrent_version";
	public static final String EXTRA_CURRENT_VERSION_NAME = "current_version_name";
	public static final String EXTRA_VEECHECK = "veecheck";

	protected int mLatestVersion;
	protected String mComment;
	protected String mNewApplicationId;

	public void checkForUpdate(String link) {

		mLatestVersion = -1;
		mComment = null;
		mNewApplicationId = null;

		try {
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

	public int getLatestVersion() {
		return mLatestVersion;
	}

	public String getApplicationId() {
		return mNewApplicationId;
	}

	public String getComment() {
		return mComment;
	}
}