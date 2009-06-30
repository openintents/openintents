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

package org.openintents.localebridge;

import java.net.URISyntaxException;

import org.openintents.intents.LocaleBridgeIntents;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AutomationReceiver extends BroadcastReceiver {
	private static final String TAG = LogConstants.TAG;
	private static final boolean debug = LogConstants.debug;

	@Override
	public void onReceive(Context context, Intent intent) {
		
		// Fire the intent as specified.
		if (intent != null) {
			// Extract the original Locale intent:
			String encodedIntent = intent.getStringExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_INTENT);
			if (debug) Log.i(TAG, "Converting automation intent: " + encodedIntent);
			
			Intent localeIntent;
			try {
				localeIntent = Intent.getIntent(encodedIntent);
			} catch (URISyntaxException e) {
				// Error decoding the original intent
				return;
			}
			
			String runComponent = intent.getStringExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_RUN_COMPONENT);
			if (runComponent == null) {
				// Error with component
				return;
			}
			ComponentName component = ComponentName.unflattenFromString(runComponent);
			
			// Set this component in the newly created intent:
			localeIntent.setComponent(component);
			
			if (localeIntent.hasExtra(com.twofortyfouram.Intent.EXTRA_STRING_ACTION_FIRE)) {
				String action = localeIntent.getStringExtra(com.twofortyfouram.Intent.EXTRA_STRING_ACTION_FIRE);
				localeIntent.setAction(action);
			} else {
				localeIntent.setAction(com.twofortyfouram.Intent.ACTION_FIRE_SETTING);
			}

			if (debug) Log.i(TAG, "into Locale intent: " + localeIntent.toURI());
			
			// Launch this intent
			context.sendBroadcast(localeIntent);
		}
		
	}

}
