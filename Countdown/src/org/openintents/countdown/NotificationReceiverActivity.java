/* 
 * Copyright (C) 2008 OpenIntents.org
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

import org.openintents.countdown.activity.CountdownEditorActivity;
import org.openintents.countdown.automation.AutomationActions;
import org.openintents.countdown.util.CountdownUtils;
import org.openintents.countdown.util.NotificationState;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class NotificationReceiverActivity extends Activity {
	private static final String TAG = LogConstants.TAG;
	private static final boolean debug = LogConstants.debug;
	
	
	public static final String EXTRA_LAUNCH_INTENT = "org.openintents.countdown.internal.LAUNCH_INTENT";
	
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.countdown_notificationreceiver);
        
        Intent i = getIntent();
        Uri uri = i.getData();
        
        Intent launchIntent = null;
        try {
			launchIntent = Intent.getIntent(i.getStringExtra(EXTRA_LAUNCH_INTENT));
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		if (debug) Log.i(TAG, "Launch intent: " + launchIntent.toURI());
		
        AutomationActions.stopCountdown(this, uri);

        
        if (launchIntent != null) {
        	try {
        		startActivity(launchIntent);
        	} catch (ActivityNotFoundException e) {
        		// Error launching activity
        		Log.e(TAG, "Error launching activity " + e);
        	}
        }
        
        finish();
    }
}
