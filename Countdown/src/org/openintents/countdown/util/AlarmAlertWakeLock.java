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

import android.content.Context;
import android.os.PowerManager;
import android.util.Log;

/**
 * Hold a wakelock that can be acquired in the AlarmReceiver and
 * released in the AlarmAlert activity
 */
public class AlarmAlertWakeLock {
	public final static String LOGTAG = "AlarmAlertWakeLock";
	
    private static PowerManager.WakeLock sWakeLock;

    public static void acquire(Context context) {
        Log.v(LOGTAG, "Acquiring wake lock");
        if (sWakeLock != null) {
            Log.v(LOGTAG, " - releaseing first old wake lock");
            sWakeLock.release();
        }

        PowerManager pm =
                (PowerManager) context.getSystemService(Context.POWER_SERVICE);

        sWakeLock = pm.newWakeLock(
                PowerManager.FULL_WAKE_LOCK |
                PowerManager.ACQUIRE_CAUSES_WAKEUP |
                PowerManager.ON_AFTER_RELEASE,
                LOGTAG
                );
        sWakeLock.acquire();
    }

    public static void release() {
        Log.v(LOGTAG, "Releasing wake lock");
        if (sWakeLock != null) {
            Log.v(LOGTAG, " - releaseing wake lock now");
            sWakeLock.release();
            sWakeLock = null;
        }
    }
}
