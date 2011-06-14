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

import org.openintents.historify.uri.Actions;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * 
 * The Bridge is an interface for other applications to interact with
 * Historify's core functionality. It could be used for registering
 * SharedSources, and posting single events through the QuickPost interface.
 * 
 * 3rd party apps may use the {@link HistorifyBridge} helper class to call the
 * service.
 * 
 * @author berke.andras
 */
public class BridgeService extends IntentService {

	public static final String N = "Historify.Bridge";

	public BridgeService() {
		super(N);
	}

	/** Called to a handle a task represented by an intent */
	@Override
	protected void onHandleIntent(Intent intent) {

		if (Actions.ACTION_REGISTER_SOURCE.equals(intent.getAction())) {

			String sourceName = intent
					.getStringExtra(Actions.EXTRA_SOURCE_NAME);
			if (sourceName != null) {
				Log.v(N, "Registering source: " + sourceName);
				try {
					new SourceRegistrationHelper().registerSource(this, intent
							.getExtras());
				} catch (Exception e) {
					Log.e("N", "Error while registering source.");
					e.printStackTrace();
				}
			} else {
				Log.e("N", intent.getAction().toString()
						+ " called with wrong parameters");
			}

		}
	}

}
