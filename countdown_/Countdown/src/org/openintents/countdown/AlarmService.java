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

// Code partly based on 
// Copyright (C) 2007 The Android Open Source Project
// licensed under the same license.

package org.openintents.countdown;

import org.openintents.countdown.util.AlarmAlertWakeLock;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.util.Log;

/**
 * This is an example of implementing an application service that will run in
 * response to an alarm, allowing us to move long duration work out of an
 * intent receiver.
 * 
 * @see AlarmService
 * @see AlarmService_Alarm
 */
public class AlarmService extends Service {
	private static final String TAG = LogConstants.TAG;
	private static final boolean debug = LogConstants.debug;
    
    Thread mThread;
    long mEndTime;
    
    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        mThread = new Thread(null, mTask, "AlarmService");
        mThread.start();
    }

    @Override
    public void onDestroy() {

    	// Release the wake lock
    	AlarmAlertWakeLock.release();
    	
    	mEndTime = 0;
    	mThread.interrupt();
    }

    /**
     * The function that runs in our worker thread
     */
    Runnable mTask = new Runnable() {
        public void run() {
            // Normally we would do some work here...  for our sample, we will
            // just sleep for 30 seconds.
        	if (debug) Log.v(TAG, "Starting service...");

        	long ALARM_TIMEOUT_SECONDS = PreferenceActivity.getNotificationTimeoutFromPrefs(AlarmService.this);
            mEndTime = System.currentTimeMillis() + ALARM_TIMEOUT_SECONDS * 1000;
            while (System.currentTimeMillis() < mEndTime) {
                synchronized (mBinder) {
                    try {
                        mBinder.wait(mEndTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
            
            if (debug) Log.v(TAG, "Stopping service.");

            // Done with our work...  stop the service!
            AlarmService.this.stopSelf();
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * This is the object that receives interactions from clients.  See RemoteService
     * for a more complete example.
     */
    private final IBinder mBinder = new Binder() {
        @Override
		protected boolean onTransact(int code, Parcel data, Parcel reply,
		        int flags) throws RemoteException {
            return super.onTransact(code, data, reply, flags);
        }
    };
}

