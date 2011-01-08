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
import org.openintents.countdown.R;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class CountdownUtils {

	public static String textDay;
	public static String textDays;
	
	static StringBuilder sb = new StringBuilder();
	
	public static void setLocalizedStrings(Context context) {
		textDay = context.getString(R.string.day);
		textDays = context.getString(R.string.days);
	}
	
	/**
	 * Given a duration in milliseconds, returns a string in
	 * days, hours, minutes, and seconds.
	 * 
	 * @param duration in milliseconds
	 * @return
	 */
	public static String getDurationString(long duration) {
		long seconds = (long) (duration / 1000);
		long minutes = seconds / 60;
		seconds = seconds % 60;
		long hours = minutes / 60;
		minutes = minutes % 60;
		long days = hours / 24;
		hours = hours % 24;
		
		sb.setLength(0);
		
		if (days > 1) {
			sb.append(days);
			sb.append(" ");
			sb.append(textDays);
			sb.append(" ");
		} else if (days == 1) {
			sb.append("1 ");
			sb.append(textDay);
			sb.append(" ");
		}
		
		sb.append(hours);
		sb.append(":");
		if (minutes < 10) sb.append("0");
		sb.append(minutes);
		sb.append(":");
		if (seconds < 10) sb.append("0");
		sb.append(seconds);
		
		return sb.toString();
		
		//return "" + hours + ":" 
		//	+ (minutes < 10 ? "0" : "")
		//	+ minutes + ":"
		//	+ (seconds < 10 ? "0" : "")
		//	+ seconds;
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
	 * Get the pending intent for setting or canceling the alarm.
	 * @param context
	 * @param uri
	 * @return
	 */
	public static PendingIntent getAlarmPendingIntent(Context context, Uri uri) {
		Intent intent = new Intent(context, AlarmReceiver.class);
	    
	    intent.setData(uri);
	    
	    PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
	            0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	
	    return pendingIntent;
	}

}
