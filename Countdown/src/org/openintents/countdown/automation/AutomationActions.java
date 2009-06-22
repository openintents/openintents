package org.openintents.countdown.automation;

import org.openintents.countdown.db.Countdown;
import org.openintents.countdown.db.Countdown.Durations;
import org.openintents.countdown.util.CountdownUtils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class AutomationActions {

	public static void startCountdown(Context context, Uri uri) {
	
		ContentResolver cr = context.getContentResolver();
		
	    Cursor c = cr.query(uri, Durations.PROJECTION, null, null,
	            Countdown.Durations.DEFAULT_SORT_ORDER);
		
		long now = System.currentTimeMillis();
		
		long duration = 0;
		long userdeadline = 0;
		
		if (c != null) {
	    	c.moveToFirst();
	    	duration = c.getLong(c.getColumnIndexOrThrow(Durations.DURATION));
	    	userdeadline = c.getLong(c.getColumnIndexOrThrow(Durations.USER_DEADLINE_DATE));
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

}
