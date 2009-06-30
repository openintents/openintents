/* 
 * Copyright (C) 2009 OpenIntents.org
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

// Code partly based on 
// Copyright (C) 2007 The Android Open Source Project
// licensed under the same license.

//package com.android.alarmclock;

package org.openintents.countdown;

import org.openintents.countdown.db.Countdown;
import org.openintents.countdown.db.Countdown.Durations;
import org.openintents.countdown.util.CountdownUtils;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class AlarmInitReceiver extends BroadcastReceiver {
	private static final String TAG = "AlarmInitReceiver";
	private static final boolean debug = true;
	
	
    /**
     * Sets alarm on ACTION_BOOT_COMPLETED.  Resets alarm on
     * TIME_SET, TIMEZONE_CHANGED
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (debug) Log.i(TAG, "AlarmInitReceiver" + action);

        if (context.getContentResolver() == null) {
            Log.e(TAG, "AlarmInitReceiver: FAILURE unable to get content resolver.  Alarms inactive.");
            return;
        }
        /*
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Alarms.disableSnoozeAlert(context);
            Alarms.disableExpiredAlarms(context);
        }
        Alarms.setNextAlert(context);
        */
        
        // Set all alarms again
        
        // Go through all alarms and set alarm if it should still be running
        long now = System.currentTimeMillis();
		
        ContentResolver cr = context.getContentResolver();
        Cursor c = cr.query(Durations.CONTENT_URI, Durations.PROJECTION, null, null,
	            Countdown.Durations.DEFAULT_SORT_ORDER);
        if (c != null) {
        	while(c.moveToNext()) {
        		long id = c.getLong(c.getColumnIndexOrThrow(Durations._ID));
        		long duration = c.getLong(c.getColumnIndexOrThrow(Durations.DURATION));
        		long userdeadline = c.getLong(c.getColumnIndexOrThrow(Durations.USER_DEADLINE_DATE));
    	    	long deadline = c.getLong(c.getColumnIndexOrThrow(Durations.DEADLINE_DATE));
    	    	
    	    	if (deadline > 0) {
    	    		Uri uri = Uri.withAppendedPath(Durations.CONTENT_URI, 
    	    				Long.toString(id));
    	    		// This alarm is active

    	    		// if deadline is in the past, 
    	    		// AlarmManager will fire the intent immediately.
	    			CountdownUtils.setAlarm(context, uri, deadline);
    	    		/*
    	    		if (deadline > now) {
    	    			// alarm is in the future. Refresh the
    	    			// alarm notification

    	    			CountdownUtils.setAlarm(context, uri, deadline);
    	    		} else {
    	    			// Show notificaion NOW - alarm is overdue

    	    			CountdownUtils.setAlarm(context, uri, deadline);
    	    		}
    	    		*/
    	    	}
        	}
        	c.close();
        }
    }
}
