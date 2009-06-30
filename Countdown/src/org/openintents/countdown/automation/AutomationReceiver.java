package org.openintents.countdown.automation;

import org.openintents.countdown.LogConstants;
import org.openintents.intents.CountdownIntents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

public class AutomationReceiver extends BroadcastReceiver {

	private final static String TAG = "AutomationReceiver";
    private static final boolean debug = LogConstants.debug;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (debug) Log.i(TAG, "Receive intent: " + intent.toString());
		
		final String action = intent.getStringExtra(CountdownIntents.EXTRA_ACTION);
		final String dataString = intent.getStringExtra(CountdownIntents.EXTRA_DATA);
		Uri data = null;
		if (dataString != null) {
			data = Uri.parse(dataString);
		}
		if (debug) Log.i(TAG, "action: " + action + ", data: " + dataString);
		
		if (CountdownIntents.TASK_START_COUNTDOWN.equals(action)) {
			// Start countdown.
			if (data != null) {
				// Launch that countdown:
				if (debug) Log.i(TAG, "Launch countdown " + data);
				AutomationActions.startCountdown(context, data);
			}
		}

		if (CountdownIntents.TASK_STOP_COUNTDOWN.equals(action)) {
			// Start countdown.
			if (data != null) {
				// Launch that countdown:
				if (debug) Log.i(TAG, "Stop countdown " + data);
				AutomationActions.stopCountdown(context, data);
			}
		}
		
	}

}
