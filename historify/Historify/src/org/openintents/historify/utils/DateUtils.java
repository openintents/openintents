/* 
 * Copyright (C) 2011 OpenIntents.org
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

package org.openintents.historify.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlarmManager;

import com.ocpsoft.pretty.time.PrettyTime;

/**
 * 
 * Helper class for formatting dates.
 * 
 * @author berke.andras
 */
public class DateUtils {

	private static final SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd. MMM HH:mm",Locale.ENGLISH);
	private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm",Locale.ENGLISH);
	
	private static PrettyTime prettyTime = new PrettyTime(Locale.ENGLISH);
	private static long INTERVAL_WEEK = AlarmManager.INTERVAL_DAY * 7;
		
	public static String formatTimelineDate(Date date) {
		if(isInADay(date))
			return prettyTime.format(date);
		else if(isInAWeek(date))		
			return prettyTime.format(date) + " " + timeFormat.format(date);
		else
			return fullDateFormat.format(date);
	}
	
	public static String formatPrettyDate(Date date) {
		return prettyTime.format(date);
	}
	
	public static String formatDate(Date date) {
		return fullDateFormat.format(date);
	}
	
	private static boolean isInADay(Date date) {
		return System.currentTimeMillis() - date.getTime() < AlarmManager.INTERVAL_DAY;
	}

	private static boolean isInAWeek(Date date) {
		return System.currentTimeMillis() - date.getTime() < INTERVAL_WEEK;
	}
}
