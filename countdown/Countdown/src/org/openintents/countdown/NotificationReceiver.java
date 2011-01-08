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

package org.openintents.countdown;

import java.net.URISyntaxException;

import org.openintents.countdown.automation.AutomationActions;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class NotificationReceiver extends BroadcastReceiver {
	private static final String TAG = LogConstants.TAG;
	private static final boolean debug = LogConstants.debug;
	
	
	public static final String EXTRA_LAUNCH_INTENT = "org.openintents.countdown.internal.LAUNCH_INTENT";

	@Override
	public void onReceive(Context context, Intent intent) {
        Uri uri = intent.getData();
        
        Intent launchIntent = null;
        try {
			launchIntent = Intent.getIntent(intent.getStringExtra(EXTRA_LAUNCH_INTENT));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		if (debug) Log.i(TAG, "Launch intent: " + launchIntent.toURI());
		
        AutomationActions.stopCountdown(context, uri);

        launchIntent.addFlags(
        		//Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP 
        		//| Intent.FLAG_ACTIVITY_CLEAR_TOP 
        		Intent.FLAG_ACTIVITY_SINGLE_TOP   // Required so that not multiple instances of the current countdown are created.
        		//| Intent.FLAG_DEBUG_LOG_RESOLUTION
        		| Intent.FLAG_ACTIVITY_NEW_TASK   // Required since we start activity from outside activity.
        		);
        
        if (launchIntent != null) {
        	try {
        		context.startActivity(launchIntent);
        	} catch (ActivityNotFoundException e) {
        		// Error launching activity
        		Log.e(TAG, "Error launching activity " + e);
        	}
        }
    }

}
