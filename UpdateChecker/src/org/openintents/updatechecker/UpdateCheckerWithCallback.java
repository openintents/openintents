package org.openintents.updatechecker;

import android.os.DeadObjectException;
import android.os.RemoteException;
import android.util.Log;

public class UpdateCheckerWithCallback extends UpdateChecker implements Runnable {
	private static final String TAG = "UpdateCheckerWithCallback";
	

	private String mLink;	
	private IUpdateCheckerServiceCallback mCallback;

	public UpdateCheckerWithCallback(String link, IUpdateCheckerServiceCallback cb) {

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

			mCallback.onVersionChecked(getLatestVersion(), getApplicationId(), getComment(), getLatestVersionName());
		} catch (DeadObjectException e) {
			// The IUpdateCheckerServiceCallback will take care of
			// removing
			// the dead object for us.
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

}