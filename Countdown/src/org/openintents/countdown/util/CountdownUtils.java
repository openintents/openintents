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
	 * Sets an alarm for a specified time.
	 * 
	 * @param context
	 * @param uri
	 * @param time
	 * @return
	 */
	public static void setAlarm(Context context, Uri uri, long time) {
		PendingIntent pendingIntent = getAlarmPendingIntent(context, uri);
		
	    // Schedule the alarm!
	    AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	    am.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);
	}

	/**
	 * Cancels an alarm for a specified time.
	 * 
	 * @param context
	 * @param uri
	 * @param time
	 * @return
	 */
	public static void cancelAlarm(Context context, Uri uri) {
	    PendingIntent pendingIntent = getAlarmPendingIntent(context, uri);
	
	    // Cancel the alarm!
	    AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
	}
	
	/**
	 * Get the pending intent for setting or cancelling the alarm.
	 * @param context
	 * @param uri
	 * @return
	 */
	public static PendingIntent getAlarmPendingIntent(Context context, Uri uri) {
		Intent intent = new Intent(context, AlarmReceiver.class);
	    
	    intent.setData(uri);
	    
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
	            0, intent, 0);
	
	    return pendingIntent;
	}

}
