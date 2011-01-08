/*
 * Copyright (C) 2008 The Android Open Source Project
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

// package com.android.alarmclock;
package org.openintents.countdown.util;

import org.openintents.countdown.LogConstants;

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

/**
 * Hold a wakelock that can be acquired in the AlarmReceiver and
 * released in the AlarmAlert activity
 */
public class AlarmAlertWakeLock {
	private static final String TAG = LogConstants.TAG;
	private static final boolean debug = LogConstants.debug;
	
    private static PowerManager.WakeLock sWakeLock;

    public static void acquire(Context context) {
    	if (debug) Log.v(TAG, "Acquiring wake lock");
        if (sWakeLock != null) {
        	if (debug) Log.v(TAG, " - releaseing first old wake lock");
            sWakeLock.release();
        }

        PowerManager pm =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        sWakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE,
                TAG
                );
        sWakeLock.acquire();
    }

    public static void release() {
    	if (debug)  Log.v(TAG, "Releasing wake lock");
        if (sWakeLock != null) {
        	if (debug) Log.v(TAG, " - releasing wake lock now");
            sWakeLock.release();
            sWakeLock = null;
        }
    }
}
