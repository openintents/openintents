package org.openintents.localebridge;

import java.net.URISyntaxException;

import org.openintents.intents.AutomationIntents;
import org.openintents.intents.LocaleBridgeIntents;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class LocaleReceiver extends BroadcastReceiver {
	private static final String TAG = LogConstants.TAG;
	private static final boolean debug = LogConstants.debug;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (debug) Log.i(TAG, "LocaleReceiver - onReceive()");
		
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
			
			localeIntent.setAction(AutomationIntents.ACTION_RUN_AUTOMATION);

			if (debug) Log.i(TAG, "into Locale intent: " + localeIntent.toURI());
			
			// Launch this intent
			context.sendBroadcast(localeIntent);
		}
	}

}
