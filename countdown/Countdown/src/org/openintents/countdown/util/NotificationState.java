package org.openintents.countdown.util;

import java.util.HashMap;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class NotificationState {

	public static final String ACTION_NOTIFICATION_STATE_CHANGED = "org.openintents.countdown.intent.NOTIFICATION_STATE_CHANGED";
	
	static HashMap<String, Boolean> mNotification = new HashMap<String, Boolean>();
	
	public static void start(Context context, Uri uri) {
		mNotification.put(uri.toString(), true);
		
		Intent intent = new Intent(NotificationState.ACTION_NOTIFICATION_STATE_CHANGED);
		context.sendBroadcast(intent);
	}
	
	public static void stop(Uri uri) {
		mNotification.remove(uri.toString());
	}
	
	public static boolean isActive(Uri uri) {
		return mNotification.containsKey(uri.toString());
	}

}
