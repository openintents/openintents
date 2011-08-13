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

import org.openintents.samples.timescapeextension.actions.AbstractAction;
import org.openintents.samples.timescapeextension.actions.RefreshAction;
import org.openintents.samples.timescapeextension.actions.RegisterPluginAction;

import android.app.IntentService;
import android.content.Intent;

import com.sonyericsson.eventstream.PluginConstants.ServiceIntentCmd;

/**
 * Service component for handling Xperia Events' requests.
 *
 */
public class PluginService extends IntentService {
	
	private static final String N = "PluginService";
    public static final int PLUGIN_VERSION = 1; 

	
	public PluginService() {
		super(N);
	
	}
	@Override
	protected void onHandleIntent(Intent intent) {
		
		String intentAction = intent.getAction();
		AbstractAction action = null;
		if(ServiceIntentCmd.ACTION_REGISTER_PLUGIN.equals(intentAction)) {
			action = new RegisterPluginAction(this);
		} else if(ServiceIntentCmd.ACTION_REFRESH_REQUEST.equals(intentAction)) {
			action = new RefreshAction(this);
		}
		
		if(action!=null) {
			action.run();
		}
	}

}
