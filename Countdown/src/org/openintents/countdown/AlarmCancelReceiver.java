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

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;

/**
 * Cancels playing the sound.
 * 
 * This prevents that the sound plays all day long.
 */
public class AlarmCancelReceiver extends BroadcastReceiver
{
	private final static String TAG = "AlarmCancelReceiver";
	
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
        
        long time = intent.getLongExtra(AlarmReceiver.EXTRA_TIME, 
        		System.currentTimeMillis());
        
        // Canceling sound and vibration also cancels the notification.
        // Too keep the notification there, we set the notification
        // again with the original time, but this time a silent version.
        AlarmReceiver.showNotification(context, mUri, AlarmReceiver.CANCEL_NOTIFICATION, time);
        

        // stop service for wake lock:
        Intent serviceIntent = new Intent(mContext, AlarmService.class);
        mContext.stopService(serviceIntent);
        
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

