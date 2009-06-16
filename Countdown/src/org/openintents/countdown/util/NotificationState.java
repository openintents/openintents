package org.openintents.countdown.util;

import java.util.HashMap;

import org.openintents.countdown.CountdownEditorActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class NotificationState {

	static HashMap<String, Boolean> mNotification = new HashMap<String, Boolean>();
	
	public static void start(Context context, Uri uri) {
		mNotification.put(uri.toString(), true);
		
		Intent intent = new Intent(CountdownEditorActivity.ACTION_NOTIFICATION_STATE_CHANGED);
		context.sendBroadcast(intent);
	}
	
	public static void stop(Uri uri) {
		mNotification.remove(uri.toString());
	}
	
	public static boolean isActive(Uri uri) {
		return mNotification.containsKey(uri.toString());
	}
}
