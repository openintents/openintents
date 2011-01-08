package org.openintents.countdown.util;

import java.util.List;

import org.openintents.countdown.LogConstants;
import org.openintents.intents.AutomationIntents;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

public class AutomationUtils {
	private static final String TAG = LogConstants.TAG;
	private static final boolean debug = LogConstants.debug;

//	public static String EXTRA_EDIT_AUTOMATION_COMPONENT = "org.openintents.internal.extra.EDIT_AUTOMATION_COMPONENT";
	public static String EXTRA_RUN_AUTOMATION_COMPONENT = "org.openintents.internal.extra.RUN_AUTOMATION_COMPONENT";
	public static String EXTRA_LAUNCH_THROUGH_STATUS_BAR = "org.openintents.internal.extra.LAUNCH_THROUGH_STATUS_BAR";

//	public static String ACTION_EDIT_AUTOMATION = AutomationIntents.ACTION_EDIT_AUTOMATION;
	public static String ACTION_RUN_AUTOMATION = AutomationIntents.ACTION_RUN_AUTOMATION;
	
	/**
	 * Adds the component used for running the automation to the existing intent.
	 * 
	 * @param context
	 * @param intent
	 * @return
	 */
	public static void setRunAutomationComponent(Context context, Intent intent, ComponentName component) {
	//	intent.putExtra(EXTRA_EDIT_AUTOMATION_COMPONENT, component.flattenToString());
		
		String packageName = component.getPackageName();
		
		if (packageName != null) {
			
			PackageManager pm = context.getPackageManager();
			Intent runAutomationIntent = new Intent(ACTION_RUN_AUTOMATION);
			
			List<ResolveInfo> rilist = pm.queryBroadcastReceivers(runAutomationIntent, 0);
			for (ResolveInfo ri : rilist) {
				//Log.i(TAG, "name: " + ri.loadLabel(pm));
				//Log.i(TAG, "Package name: " + ri.activityInfo.packageName);
				//Log.i(TAG, "Package name: " + ri.activityInfo.name);
				//Log.i(TAG, "more " + ri.serviceInfo);
				if (packageName.equals(ri.activityInfo.packageName)) {
					// Let's use this intent
					ComponentName cn = new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name);
					
					intent.putExtra(EXTRA_RUN_AUTOMATION_COMPONENT, cn.flattenToString());
					return;
				}
			}
		}
		
		// No automation target found.
		intent.putExtra(EXTRA_RUN_AUTOMATION_COMPONENT, (String) null);
		return;
		
	}
	
	/**
	 * Clears the internal extra.
	 * 
	 * @param intent
	 */
	public static final void clearInternalExtras(Intent intent) {
	//	intent.putExtra(EXTRA_EDIT_AUTOMATION_COMPONENT, (String) null);
		intent.putExtra(EXTRA_RUN_AUTOMATION_COMPONENT, (String) null);
		intent.putExtra(EXTRA_LAUNCH_THROUGH_STATUS_BAR, (long) 0);
	}
	
	/**
	 * Returns the activity intent whose component has been stored previously.
	 * 
	 * @param intent
	 * @return
	 *//*
	public static final Intent getEditAutomationIntent(Intent intent) {
		Intent editIntent = null;
		
		if (intent.hasExtra(EXTRA_EDIT_AUTOMATION_COMPONENT)) {
			editIntent = new Intent(intent);
	
			ComponentName component = ComponentName.unflattenFromString(intent.getStringExtra(EXTRA_EDIT_AUTOMATION_COMPONENT));
			editIntent.setAction(ACTION_EDIT_AUTOMATION);
			editIntent.setComponent(component);
			editIntent.setData(null);
			clearInternalExtras(editIntent);
			
		}
		return editIntent;
	}*/
	
	/**
	 * Returns the broadcast intent whose component has been stored previously.
	 * 
	 * @param intent
	 * @return
	 */
	public static final Intent getRunAutomationIntent(Intent intent) {
		Intent runIntent = null;
		
		if (intent.hasExtra(EXTRA_RUN_AUTOMATION_COMPONENT)) {
			runIntent = new Intent(intent);
	
			ComponentName component = ComponentName.unflattenFromString(intent.getStringExtra(EXTRA_RUN_AUTOMATION_COMPONENT));
			runIntent.setAction(ACTION_RUN_AUTOMATION);
			runIntent.setComponent(component);
			runIntent.setData(null);
			clearInternalExtras(runIntent);
			
		}
		return runIntent;
	}
	
	/**
	 * Whether a valid component to run the automation has been set.
	 * 
	 * @param intent
	 * @return
	 */
	public static final boolean isRunAutomationIntent(Intent intent) {
		return intent.hasExtra(EXTRA_RUN_AUTOMATION_COMPONENT);
	}
	
	public static final void setLaunchThroughStatusBar(Intent intent, long launchThroughStatusBar) {
		intent.putExtra(EXTRA_LAUNCH_THROUGH_STATUS_BAR, launchThroughStatusBar);
	}
	
	public static final long getLaunchThroughStatusBar(Intent intent) {
		return intent.getLongExtra(EXTRA_LAUNCH_THROUGH_STATUS_BAR, 0);
	}
}
