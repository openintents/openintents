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

package org.openintents.samples.timescapeextension;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.sonyericsson.eventstream.PluginConstants.EventStream;
import com.sonyericsson.eventstream.PluginConstants.ServiceIntentCmd;

/**
 * Broadcast listener for processing Intents sent by Xperia Events.
 * 
 */
public class EventStreamListener extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		String action = intent.getAction();
		Intent serviceIntent = new Intent();
		serviceIntent.setComponent(new ComponentName(context,
				PluginService.class));

		if (EventStream.Intents.REGISTER_PLUGINS_REQUEST_INTENT.equals(action)) {

			serviceIntent.setAction(ServiceIntentCmd.ACTION_REGISTER_PLUGIN);
			context.startService(serviceIntent);

		} else if (EventStream.Intents.REFRESH_REQUEST_INTENT.equals(action)) {

			serviceIntent.setAction(ServiceIntentCmd.ACTION_REFRESH_REQUEST);
			context.startService(serviceIntent);
		}
	}

}
