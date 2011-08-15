/* 
 * Copyright (C) 2011 OpenIntents.org
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

package org.openintents.timescape.api.service;

import org.openintents.timescape.api.requestscheduling.RequestSender;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * Receiving broadcast intents for added and removed application packages.
 * 
 * @author berke.andras
 */
public class PackageChangesReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {

		Log.v("i",intent.getAction());
		
		if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {

			Log.v(TimescapeCompatibilityService.N, "Received broadcast for PACKAGE_ADDED");
			// a new application has been installed
			new RequestSender().requestRegisterPlugin(context);

		}

		else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
			//if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {

				Log
						.v(TimescapeCompatibilityService.N,
								"Received broadcast for PACKAGE_REMOVED");
				// package is removed
				int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
				
				// delete registered plugin if there is any.
				Intent i = new Intent(context, TimescapeCompatibilityService.class);
				i.setAction(Intent.ACTION_PACKAGE_REMOVED);
				i.putExtra(Intent.EXTRA_UID, uid);
				context.startService(i);

			//}
		}
	}

}
