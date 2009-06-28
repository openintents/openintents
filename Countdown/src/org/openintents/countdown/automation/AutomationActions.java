package org.openintents.countdown.automation;

import org.openintents.countdown.AlarmReceiver;
import org.openintents.countdown.LogConstants;
import org.openintents.countdown.activity.CountdownEditorActivity;
import org.openintents.countdown.db.Countdown;
import org.openintents.countdown.db.Countdown.Durations;
import org.openintents.countdown.util.CountdownUtils;
import org.openintents.countdown.util.NotificationState;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class AutomationActions {
	private static final String TAG = LogConstants.TAG;
	private static final boolean debug = LogConstants.debug;

	public static void startCountdown(Context context, Uri uri) {
		// Stop any alarms
    	CountdownUtils.cancelAlarm(context, uri);
    	AlarmReceiver.cancelAlarmCancel(context, uri);
    	
		// Stop any notifications
		CountdownEditorActivity.cancelNotification(context, uri);
		
        NotificationState.stop(uri);
        
        // Now start again
		ContentResolver cr = context.getContentResolver();
		
	    Cursor c = cr.query(uri, Durations.PROJECTION, null, null,
	            Countdown.Durations.DEFAULT_SORT_ORDER);
		
		long now = System.currentTimeMillis();
		
		long duration = 0;
		long userdeadline = 0;
		
		if (c != null && c.moveToFirst()) {
	    	duration = c.getLong(c.getColumnIndexOrThrow(Durations.DURATION));
	    	userdeadline = c.getLong(c.getColumnIndexOrThrow(Durations.USER_DEADLINE_DATE));
		} else {
			// TODO
			// Problem retrieving what to do.
			Log.d(TAG, "No Countdown at URI - maybe deleted? " + uri);
			return;
		}
		
		long deadline = now + duration;
		
		if (userdeadline > 0) {
			deadline = userdeadline;
		}
		
		CountdownUtils.setAlarm(context, uri, deadline);
		
		// Write back modification
		ContentValues values = new ContentValues();
		values.put(Durations.DEADLINE_DATE, deadline);
		
		cr.update(uri, values, null, null);
	}

	public static void stopCountdown(Context context, Uri uri) {

    	CountdownUtils.cancelAlarm(context, uri);
    	AlarmReceiver.cancelAlarmCancel(context, uri);
    	
		CountdownEditorActivity.cancelNotification(context, uri);
		
        NotificationState.stop(uri);

		ContentResolver cr = context.getContentResolver();
		
		// Write back modification
		ContentValues values = new ContentValues();
		values.put(Durations.DEADLINE_DATE, 0);
		
		cr.update(uri, values, null, null);
	}
}
