package org.openintents.distribution;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class GetFromMarket {
	private static final String TAG = "StartSaveActivity";

	/**
	 * Start an activity but prompt a toast if activity is not found
	 * (instead of crashing).
	 * 
	 * @param context
	 * @param intent
	 */
	public static void startSaveActivity(Context context, Intent intent) {
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context,
					RD.string.update_error,
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Error starting activity.", e);
		}
	}
}
