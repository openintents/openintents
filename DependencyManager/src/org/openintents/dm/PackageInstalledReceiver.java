package org.openintents.dm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PackageInstalledReceiver extends BroadcastReceiver{


	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.v(this.getClass().getSimpleName(), "received " + intent.toString());
		
		// retrieve package name from Intent.ACTION_PACKAGE_ADDED broadcast intent
		String packageName = intent.getData().getSchemeSpecificPart();
		
		// prepare intent to start DependencyManagerService
		Intent dmServiceIntent = new Intent();
		dmServiceIntent.setClass(context, DependencyManagerService.class);
		dmServiceIntent.putExtra(DependencyManagerService.EXTRA_PACKAGE_NAME, packageName);
		
		// start service
		context.startService(dmServiceIntent);
	}

}
