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

package org.openintents.updatechecker;

import org.openintents.updatechecker.util.AlarmUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Received when boot completed.
 */
public class BootCompletedReceiver extends BroadcastReceiver
{
	private final static String TAG = "BootCompletedReceiver";
	
    @Override
    public void onReceive(Context context, Intent intent)
    {
    	Log.d(TAG, "Received BootCompleted");
    	Log.i(TAG, "Refresh update alarm");
		AlarmUtils.refreshUpdateAlarm(context);
        
    }

}

