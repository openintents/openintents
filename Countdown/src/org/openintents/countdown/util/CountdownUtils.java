package org.openintents.countdown.util;

import org.openintents.countdown.AlarmReceiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class CountdownUtils {

	/**
	 * Given a duration in milliseconds, returns a string in
	 * hours, minutes, and seconds.
	 * 
	 * @param duration in milliseconds
	 * @return
	 */
	public static String getDurationString(long duration) {
		int seconds = (int) (duration / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;
		int hours = minutes / 60;
		minutes = minutes % 60;
		
		return "" + hours + ":" 
			+ (minutes < 10 ? "0" : "")
			+ minutes + ":"
			+ (seconds < 10 ? "0" : "")
			+ seconds;
	}

	/**
	 * Sets an alarm for a speficied time.
	 * 
	 * @param context
	 * @param uri
	 * @param time
	 * @return
	 */
	public static PendingIntent setAlarm(Context context, Uri uri, long time) {
		// When the alarm goes off, we want to broadcast an Intent to our
	    // BroadcastReceiver.  Here we make an Intent with an explicit class
	    // name to have our own receiver (which has been published in
	    // AndroidManifest.xml) instantiated and called, and then create an
	    // IntentSender to have the intent executed as a broadcast.
	    Intent intent = new Intent(context, AlarmReceiver.class);
	    
	    intent.setData(uri);
	    
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
	            0, intent, 0);
	
	    // Schedule the alarm!
	    AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	    am.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
	    
	    return pendingIntent;
	}

}
