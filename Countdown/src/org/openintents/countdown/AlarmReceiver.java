/* 
 * Copyright (C) 2007 Google Inc.
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
import org.openintents.countdown.list.CountdownListItemView;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

/**
 * This is an example of implement an {@link BroadcastReceiver} for an alarm that
 * should occur once.
 * <p>
 * When the alarm goes off, we show a <i>Toast</i>, a quick message.
 */
public class AlarmReceiver extends BroadcastReceiver
{
	Context mContext;
	
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	mContext = context;
    	
    	Uri mUri = intent.getData();
    	
        // Toast.makeText(context, "R.string.alarm_received", Toast.LENGTH_SHORT).show();
        
        showNotification(mUri);
    }
    

    /**
     * The notification is the icon and associated expanded entry in the
     * status bar.
     */
    public void showNotification(Uri uri) {
    	
        // look up the notification manager service
        NotificationManager nm = (NotificationManager)mContext.getSystemService(mContext.NOTIFICATION_SERVICE);


        //Intent intent = new Intent(mContext, NotificationReceiverActivity.class);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setData(uri);

        // Get the data

        Cursor c = mContext.getContentResolver().query(uri, new String[]{Countdown.Durations.TITLE, Countdown.Durations.DURATION}, null, null,
                Countdown.Durations.DEFAULT_SORT_ORDER);
        
        String title = "";
        String text = "";
        if (c != null) {
        	c.moveToFirst();
        	title = c.getString(0);
        	text = CountdownListItemView.getDurationString(c.getLong(1));
        }
        
        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0,
                intent, 0);

        // The ticker text, this uses a formatted string so our message could be localized
        String tickerText = mContext.getString(R.string.countdown_ended);

        // construct the Notification object.
        Notification notif = new Notification(R.drawable.icon_hourglass, tickerText,
                System.currentTimeMillis());

        // Set the info for the views that show in the notification panel.
        notif.setLatestEventInfo(mContext, title, text, contentIntent);

        // after a 100ms delay, vibrate for 250ms, pause for 100 ms and
        // then vibrate for 500ms.
        notif.vibrate = new long[] { 100, 250, 100, 500};
        
        int notification_id = Integer.parseInt(uri.getLastPathSegment());

        // Note that we use R.layout.incoming_message_panel as the ID for
        // the notification.  It could be any integer you want, but we use
        // the convention of using a resource id for a string related to
        // the notification.  It will always be a unique number within your
        // application.
        nm.notify(notification_id, notif);
    }
}

