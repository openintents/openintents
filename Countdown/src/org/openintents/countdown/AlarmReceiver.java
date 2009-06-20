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

package org.openintents.countdown;

import java.net.URISyntaxException;

import org.openintents.countdown.db.Countdown;
import org.openintents.countdown.db.Countdown.Durations;
import org.openintents.countdown.util.CountdownUtils;
import org.openintents.countdown.util.NotificationState;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import org.openintents.countdown.util.AlarmAlertWakeLock;

/**
 * Alarm receiver for countdown events.
 * 
 * @author Peli
 *
 */public class AlarmReceiver extends BroadcastReceiver
{
	private final static String TAG = "AlarmReceiver";
	
	public final static int ALARM_TIMEOUT_SECONDS = 10 * 60; //5; // 300;
	private Handler mTimeout;
	
	final static boolean RING_AND_VIBRATE = true;
	final static boolean SILENT = false;

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
        
    	showNotification(context, mUri, RING_AND_VIBRATE, time);
        
        // We don't use the following, as it also cancels the notification.
        
        setAlarmCancel(mUri);
        
        // start a service for the duration of the lock:
        Intent serviceIntent = new Intent(mContext, AlarmService.class);
        mContext.startService(serviceIntent);
    }
    

    /**
     * The notification is the icon and associated expanded entry in the
     * status bar.
     */
    public static void showNotification(Context context, Uri uri, boolean ringAndVibrate, long time) {
    	
        // look up the notification manager service
        NotificationManager nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);


        //Intent intent = new Intent(mContext, NotificationReceiverActivity.class);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);

        // Get the data

        Cursor c = context.getContentResolver().query(uri, Durations.PROJECTION, null, null,
                Countdown.Durations.DEFAULT_SORT_ORDER);
        
        String title = "";
        String text = "";
        if (c != null) {
        	c.moveToFirst();
        	title = c.getString(c.getColumnIndexOrThrow(Durations.TITLE));
        	
        	// TODO: something fishy here:
        	text = CountdownUtils.getDurationString(c.getColumnIndexOrThrow(Durations.DURATION));
        }
        
        if (TextUtils.isEmpty(title)) {
        	title = context.getString(R.string.app_name);
        }
        
        long ring = 0;
        Uri ringtone = null;
        long vibrate = 0;
        long automate = 0;
        Intent automateIntent = null;
        if (c != null) {
        	c.moveToFirst();
        	ring = c.getLong(c.getColumnIndexOrThrow(Durations.RING));
        	String ringstring = c.getString(c.getColumnIndexOrThrow(Durations.RINGTONE));
        	if (ringstring != null) {
        		ringtone = Uri.parse(ringstring);
        	}
        	vibrate = c.getLong(c.getColumnIndexOrThrow(Durations.VIBRATE));
        	automate = c.getLong(c.getColumnIndexOrThrow(Durations.AUTOMATE));
        	String automateIntentUri = c.getString(c.getColumnIndexOrThrow(Durations.AUTOMATE_INTENT));
        	Log.v(TAG, "automateUri: " + automateIntentUri);
        	if (automateIntentUri != null) {
        		try {
					automateIntent = Intent.getIntent(automateIntentUri);
				} catch (URISyntaxException e) {
					e.printStackTrace();
					automateIntent = null;
				}
        	}
        }

        if (automate != 0) {
        	automateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        	Log.v(TAG, "Launching intent " + automateIntent.getAction());
        	Log.v(TAG, "Launching intent data " + automateIntent.getData());
        	context.startActivity(automateIntent);
        }
        
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
                intent, 0);

        // The ticker text, this uses a formatted string so our message could be localized
        //String tickerText = mContext.getString(R.string.countdown_ended);
        String tickerText = title;
        
        
        // construct the Notification object.
        Notification notif = new Notification(R.drawable.icon_hourglass, tickerText,
                time);

        // Set the info for the views that show in the notification panel.
        notif.setLatestEventInfo(context, title, text, contentIntent);

        if (ringAndVibrate && ring != 0) {
        	Log.i(TAG, "Notification: Set ringtone " + ringtone.toString());
        	notif.sound = ringtone;

        	notif.audioStreamType = AudioManager.STREAM_RING;//AudioManager.STREAM_ALARM;
        	notif.flags |= Notification.FLAG_INSISTENT;
        }
        
        if (ringAndVibrate && vibrate != 0) {
        	Log.i(TAG, "Notification: Set vibration");
            // after a 100ms delay, vibrate for 250ms, pause for 100 ms and
            // then vibrate for 500ms.
            notif.vibrate = new long[] { 100, 250, 100, 500};
            notif.flags |= Notification.FLAG_INSISTENT;
        }
        
        // notif.flags |= Notification.FLAG_SHOW_LIGHTS;
        
        int notification_id = Integer.parseInt(uri.getLastPathSegment());

        nm.notify(notification_id, notif);
        NotificationState.start(context, uri);
        
    }
    
    /**
     * Kills alarm audio after ALARM_TIMEOUT_SECONDS, so the alarm
     * won't run all day.
     */
    public void setAlarmCancel(Uri uri) {
    	long now = System.currentTimeMillis();
    	long time = now + 1000 * ALARM_TIMEOUT_SECONDS;
    	
    	// When the alarm goes off, we want to broadcast an Intent to our
        // BroadcastReceiver.  Here we make an Intent with an explicit class
        // name to have our own receiver (which has been published in
        // AndroidManifest.xml) instantiated and called, and then create an
        // IntentSender to have the intent executed as a broadcast.
        Intent intent = new Intent(mContext, AlarmCancelReceiver.class);
        
        intent.setData(uri);
        intent.putExtra(EXTRA_TIME, now);
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                0, intent, 0);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

    }

}

