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

package org.openintents.historify.services.bridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * Receiving broadcast intents for added and removed application packages. If
 * the added / removed application contains a Historify SharedSource, it is
 * necessary to register / unregister it in Historify.
 * 
 * @author berke.andras
 */
public class PackageChangesReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (Intent.ACTION_PACKAGE_ADDED.equals(intent.getAction())) {

			Log.v(BridgeService.N, "Received broadcast for PACKAGE_ADDED");
			// a new application has been installed
			// fire broadcast intent to let the application
			// register its SharedSource if has any.
			int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
			new SourceRegistrationHelper().requestRegisterSource(context, uid);

		}

		else if (Intent.ACTION_PACKAGE_REMOVED.equals(intent.getAction())) {
			if (!intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {

				Log
						.v(BridgeService.N,
								"Received broadcast for PACKAGE_REMOVED");
				// package is removed
				// delete registered SharedSource if there is any.
				int uid = intent.getIntExtra(Intent.EXTRA_UID, 0);
				new SourceRegistrationHelper().unregisterSource(context, uid);
			}
		}
	}

}
