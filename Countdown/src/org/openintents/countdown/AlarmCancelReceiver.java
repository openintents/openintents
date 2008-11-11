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
import android.util.Log;

/**
 * Cancels playing the sound.
 * 
 * This prevents that the sound plays all day long.
 */
public class AlarmCancelReceiver extends BroadcastReceiver
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
        
        cancelNotification(mUri);
        
    }
    

    /**
     * The notification is the icon and associated expanded entry in the
     * status bar.
     */
    public void cancelNotification(Uri uri) {
    	
        int notification_id = Integer.parseInt(uri.getLastPathSegment());

        // look up the notification manager service
        NotificationManager nm = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        nm.cancel(notification_id);
    }

}

