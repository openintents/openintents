/* 
 * Copyright (C) 2008-2009 OpenIntents.org
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

package org.openintents.countdown;

import java.net.URISyntaxException;

import org.openintents.countdown.db.Countdown;
import org.openintents.countdown.db.Countdown.Durations;
import org.openintents.countdown.util.AlarmAlertWakeLock;
import org.openintents.countdown.util.AutomationUtils;
import org.openintents.countdown.util.CountdownUtils;
import org.openintents.countdown.util.NotificationState;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

/**
 * Alarm receiver for countdown events.
 * 
 * @author Peli
 *
 */public class AlarmReceiver extends BroadcastReceiver
{
	private final static String TAG = LogConstants.TAG;
	private final static boolean debug = LogConstants.debug;
	
	//public final static int ALARM_TIMEOUT_SECONDS = 10 * 60; //5; // 300;
	private Handler mTimeout;
	
	final static boolean START_NOTIFICATION = true;
	final static boolean CANCEL_NOTIFICATION = false;

	/**
	 * Time of the original notification.
	 */
	public final static String EXTRA_TIME = "time";
	
	Context mContext;
	
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	mContext = context;
    	AlarmAlertWakeLock.acquire(context);

		CountdownUtils.setLocalizedStrings(context);
    	
    	Uri mUri = intent.getData();
    	
        // Toast.makeText(context, "R.string.alarm_received", Toast.LENGTH_SHORT).show();
        
    	long time = System.currentTimeMillis();
        
    	showNotification(context, mUri, START_NOTIFICATION, time);
        
    	// This starts a second alarm that turns off the first alarm after a
    	// timeout
        setAlarmCancel(context, mUri);
        
        // start a service for the duration of the lock:
        Intent serviceIntent = new Intent(mContext, AlarmService.class);
        mContext.startService(serviceIntent);
    }
    

    /**
     * The notification is the icon and associated expanded entry in the
     * status bar.
     * 
     * @param startNotification true to start a notification, false to cancel
     *                          a notification's sound & vibrate (but with
     *                          showing the text again)
     */
    public static void showNotification(Context context, Uri uri, boolean startNotification, long time) {
    	
        // look up the notification manager service
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);


        Intent intent = new Intent(context, NotificationReceiver.class);
        intent.setData(uri);
        
        Intent launchIntent = new Intent();
        launchIntent.setAction(Intent.ACTION_EDIT);
        launchIntent.setData(uri);
        
        // launchIntent is optionally over-written by an automation intent, 
        // and attached further below to intent.

        // Get the data

        Cursor c = context.getContentResolver().query(uri, Durations.PROJECTION, null, null,
                Countdown.Durations.DEFAULT_SORT_ORDER);
        
        String title = "";
        String text = "";
        if (c != null && c.moveToFirst()) {
        	title = c.getString(c.getColumnIndexOrThrow(Durations.TITLE));
        	
        	// TODO: something fishy here:
        	text = CountdownUtils.getDurationString(c.getColumnIndexOrThrow(Durations.DURATION));
        }
        
        long notification = 0;
        long ring = 0;
        Uri ringtone = null;
        long vibrate = 0;
        long light = 0;
        long automate = 0;
        Intent automateIntent = null;
        String automateDescription = null;
        if (c != null && c.moveToFirst()) {
        	notification = c.getLong(c.getColumnIndexOrThrow(Durations.NOTIFICATION));
        	ring = c.getLong(c.getColumnIndexOrThrow(Durations.RING));
        	String ringstring = c.getString(c.getColumnIndexOrThrow(Durations.RINGTONE));
        	if (ringstring != null) {
        		ringtone = Uri.parse(ringstring);
        	}
        	vibrate = c.getLong(c.getColumnIndexOrThrow(Durations.VIBRATE));
        	light = c.getLong(c.getColumnIndexOrThrow(Durations.LIGHT));
        	automate = c.getLong(c.getColumnIndexOrThrow(Durations.AUTOMATE));
        	String automateIntentUri = c.getString(c.getColumnIndexOrThrow(Durations.AUTOMATE_INTENT));
        	if (debug) Log.v(TAG, "automateUri: " + automateIntentUri);
        	if (automateIntentUri != null) {
        		try {
					automateIntent = Intent.getIntent(automateIntentUri);
				} catch (URISyntaxException e) {
					e.printStackTrace();
					automateIntent = null;
				}
        	}
        	automateDescription = c.getString(c.getColumnIndexOrThrow(Durations.AUTOMATE_TEXT));
        } else {
        	Log.w(TAG, "Error retrieving data");
        }
        if (c != null) {
        	c.close();
        }
        
        if (TextUtils.isEmpty(title)) {
    		title = automateDescription;
    	}

        if (TextUtils.isEmpty(title)) {
        	title = context.getString(R.string.app_name);
        }
        
        if (automate != 0 && automateIntent != null) {
        	
        	Intent runIntent = AutomationUtils.getRunAutomationIntent(automateIntent);
        	
        	if (runIntent != null) {
        		// Send broadcast intent (automation task)
        		// Only send this the first time (startNotification == true)
        		if (startNotification) {
        			if (debug) Log.v(TAG, "Run automation " + runIntent.getComponent());
        			context.sendBroadcast(runIntent);
        		}
        	} else {
        		// Send activity intent (application or shortcut)

            	if (AutomationUtils.getLaunchThroughStatusBar(automateIntent) != 0
            			&& notification != 0) {
            		// Launch intent through status bar
            		// Do this also if startNotification == false
            		launchIntent = automateIntent;
            		
            	} else {
            		// Launch intent directly
            		if (startNotification) {
                		// Only launch activity the first time (startNotification == true)
	    				try {
	    					if (debug) Log.v(TAG, "Launching intent " + automateIntent.getAction());
	    					if (debug) Log.v(TAG, "Launching intent data " + automateIntent.getData());
	    	            	
	    					AutomationUtils.clearInternalExtras(automateIntent);
	    	            	automateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	    	            	context.startActivity(automateIntent);
	    				} catch (ActivityNotFoundException e) {
	    					// Error launching intent
	    	            	Log.d(TAG, "Error launching activity intent.");
	    				}
            		}
            	}
        	}
        	
        }
        
        intent.putExtra(NotificationReceiver.EXTRA_LAUNCH_INTENT, launchIntent.toURI());
        
        if (debug) Log.i(TAG, "Launch intent " + launchIntent.toURI());
        
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // construct the Notification object.
        Notification notif = new Notification(R.drawable.ic_launcher_countdown, title,
                time);

        // Set the info for the views that show in the notification panel.
        notif.setLatestEventInfo(context, title, text, contentIntent);

        if (startNotification && ring != 0) {
        	if (debug) Log.i(TAG, "Notification: Set ringtone " + ringtone.toString());
        	notif.sound = ringtone;

        	notif.audioStreamType = AudioManager.STREAM_RING;//AudioManager.STREAM_ALARM;
        	notif.flags |= Notification.FLAG_INSISTENT;
        }
        
        if (startNotification && vibrate != 0) {
        	if (debug) Log.i(TAG, "Notification: Set vibration");
            // after a 100ms delay, vibrate for 250ms, pause for 100 ms and
            // then vibrate for 500ms.
            notif.vibrate = new long[] { 100, 250, 100, 500};
            notif.flags |= Notification.FLAG_INSISTENT;
        }
        
        if (startNotification && light != 0) {
        	//notif.ledARGB = 0xFFFFFFFF;
        	//notif.ledOffMS = 500;
        	//notif.ledOnMS = 500;
        	//notif.flags |= Notification.FLAG_SHOW_LIGHTS;
	        //notif.flags = Notification.FLAG_AUTO_CANCEL |   	Notification.FLAG_SHOW_LIGHTS;
	        //notif.ledARGB = 0;
	        //notif.ledOffMS = 400;
	        //notif.ledOnMS = 300;
        	notif.defaults = Notification.DEFAULT_LIGHTS;
        }
        
        int notification_id = Integer.parseInt(uri.getLastPathSegment());
        
        if (notification != 0) {
        	// Show countdown notification
        	nm.notify(notification_id, notif);
        	NotificationState.start(context, uri);
        }
        
    }
    
    /**
     * Kills alarm audio after ALARM_TIMEOUT_SECONDS, so the alarm
     * won't run all day.
     */
    public static void setAlarmCancel(Context context, Uri uri) {
    	long now = System.currentTimeMillis();
    	long ALARM_TIMEOUT_SECONDS = PreferenceActivity.getNotificationTimeoutFromPrefs(context);
    	if (debug) Log.i(TAG, "Timeout in seconds: " + ALARM_TIMEOUT_SECONDS);
    	long time = now + 1000 * ALARM_TIMEOUT_SECONDS;
    	
    	// When the alarm goes off, we want to broadcast an Intent to our
        // BroadcastReceiver.  Here we make an Intent with an explicit class
        // name to have our own receiver (which has been published in
        // AndroidManifest.xml) instantiated and called, and then create an
        // IntentSender to have the intent executed as a broadcast.
        Intent intent = new Intent(context, AlarmCancelReceiver.class);
        
        intent.setData(uri);
        intent.putExtra(EXTRA_TIME, now);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

    }
    
    public static void cancelAlarmCancel(Context context, Uri uri) {
        Intent intent = new Intent(context, AlarmCancelReceiver.class);
        intent.setData(uri);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Cancel the alarm!
        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

}

