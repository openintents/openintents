package org.openintents.updatechecker;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class UpdateCheckService extends Service {

	private static final String TAG = "UpdateCheckerService";


	@Override
	public void onStart(Intent intent, int startId) {		
		Log.d(TAG, "started");
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "stopped");

	}
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	// No need to import IUpdateCheckerService if it's in the same project.
	private final IUpdateCheckerService.Stub mBinder = new IUpdateCheckerService.Stub() {

		public void checkForUpdate(String link,
				IUpdateCheckerServiceCallback cb) {
			new Thread(new UpdateChecker(link, cb)).start();
		}

	};

}
