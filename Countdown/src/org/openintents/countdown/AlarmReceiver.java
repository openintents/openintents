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

import org.openintents.countdown.db.Countdown;
import org.openintents.countdown.db.Countdown.Durations;
import org.openintents.countdown.util.CountdownUtils;

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

/**
 * Alarm receiver for countdown events.
 * 
 * @author Peli
 *
 */public class AlarmReceiver extends BroadcastReceiver
{
	private final static String TAG = "AlarmReceiver";
	
	private final static int ALARM_TIMEOUT_SECONDS = 5; // 300;
	private Handler mTimeout;
	
	Context mContext;
	
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	mContext = context;
    	
    	Uri mUri = intent.getData();
    	
        // Toast.makeText(context, "R.string.alarm_received", Toast.LENGTH_SHORT).show();
        
        showNotification(mUri);
        
        // We don't use the following, as it also cancels the notification.
        
        //setAlarmCancel(mUri);
    }
    

    /**
     * The notification is the icon and associated expanded entry in the
     * status bar.
     */
    public void showNotification(Uri uri) {
    	
        // look up the notification manager service
        NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);


        //Intent intent = new Intent(mContext, NotificationReceiverActivity.class);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);

        // Get the data

        Cursor c = mContext.getContentResolver().query(uri, Durations.PROJECTION, null, null,
                Countdown.Durations.DEFAULT_SORT_ORDER);
        
        String title = "";
        String text = "";
        if (c != null) {
        	c.moveToFirst();
        	title = c.getString(c.getColumnIndexOrThrow(Durations.TITLE));
        	text = CountdownUtils.getDurationString(c.getColumnIndexOrThrow(Durations.DURATION));
        }
        
        if (TextUtils.isEmpty(title)) {
        	title = mContext.getString(R.string.app_name);
        }
        
        long ring = 0;
        Uri ringtone = null;
        long vibrate = 0;
        if (c != null) {
        	c.moveToFirst();
        	ring = c.getLong(c.getColumnIndexOrThrow(Durations.RING));
        	String ringstring = c.getString(c.getColumnIndexOrThrow(Durations.RINGTONE));
        	if (ringstring != null) {
        		ringtone = Uri.parse(ringstring);
        	}
        	vibrate = c.getLong(c.getColumnIndexOrThrow(Durations.VIBRATE));
        }
        
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                intent, 0);

        // The ticker text, this uses a formatted string so our message could be localized
        //String tickerText = mContext.getString(R.string.countdown_ended);
        String tickerText = title;
        	
        // construct the Notification object.
        Notification notif = new Notification(R.drawable.icon_hourglass, tickerText,
                System.currentTimeMillis());

        // Set the info for the views that show in the notification panel.
        notif.setLatestEventInfo(mContext, title, text, contentIntent);

        if (ring != 0) {
        	Log.i(TAG, "Notification: Set ringtone " + ringtone.toString());
        	notif.sound = ringtone;

        	notif.audioStreamType = AudioManager.STREAM_ALARM;
        	notif.flags |= Notification.FLAG_INSISTENT;
        }
        
        if (vibrate != 0) {
        	Log.i(TAG, "Notification: Set vibration");
            // after a 100ms delay, vibrate for 250ms, pause for 100 ms and
            // then vibrate for 500ms.
            notif.vibrate = new long[] { 100, 250, 100, 500};
        }
        
        // notif.flags |= Notification.FLAG_SHOW_LIGHTS;
        
        int notification_id = Integer.parseInt(uri.getLastPathSegment());

        nm.notify(notification_id, notif);
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
        
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext,
                0, intent, 0);

        // Schedule the alarm!
        AlarmManager am = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.RTC_WAKEUP, time, pendingIntent);

    }

}

