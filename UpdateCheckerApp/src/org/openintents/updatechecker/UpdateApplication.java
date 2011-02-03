package org.openintents.updatechecker;

import android.app.Application;

public class UpdateApplication extends Application {

	public static boolean AND_APP_STORE = false;

	@Override
	public void onCreate() {
		super.onCreate();
		AND_APP_STORE = getResources().getIdentifier("and_app_store",
				"string", getPackageName()) != 0;
	}
}
