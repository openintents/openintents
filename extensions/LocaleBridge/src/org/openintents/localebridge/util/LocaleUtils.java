/* 
 * Copyright (C) 2008-2009 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.localebridge.util;

import java.util.List;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

public class LocaleUtils extends AutomationUtils {

	public static String EXTRA_RUN_AUTOMATION_COMPONENT = "org.openintents.localebridge.internal.extra.RUN_AUTOMATION_COMPONENT";

	public static String ACTION_RUN_AUTOMATION = com.twofortyfouram.Intent.ACTION_FIRE_SETTING;
	

	/**
	 * Adds the component used for running the automation to the existing intent.
	 * 
	 * @param context
	 * @param intent
	 * @return
	 */
	public static ComponentName getRunAutomationComponent(Context context, Intent intent, ComponentName component) {
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
					return cn;
				}
			}
		}
		
		// No automation target found.
		return null;
	}

	public static ComponentName getRunAutomationComponent(Context context, Intent intent, ComponentName component, String action) {
		ACTION_RUN_AUTOMATION = action;
		ComponentName cn = getRunAutomationComponent(context, intent, component);
		ACTION_RUN_AUTOMATION = com.twofortyfouram.Intent.ACTION_FIRE_SETTING;
		return cn;
	}
	
	public static final Intent getRunAutomationIntent(Intent intent, String action) {
		ACTION_RUN_AUTOMATION = action;
		Intent i = getRunAutomationIntent(intent);
		ACTION_RUN_AUTOMATION = com.twofortyfouram.Intent.ACTION_FIRE_SETTING;
		return i;
	}
	
}
