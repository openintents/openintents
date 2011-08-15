/* 
 * Copyright (C) 2011 OpenIntents.org
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

package org.openintents.timescape.api.requestscheduling;

import android.app.AlarmManager;
import android.content.Context;

public class Settings {

	private static final String SP_NAME = "scheduling";
	private static final String KEY_INTERVAL = "interval";
	public static final long DEF_INTERVAL = 0;
	
	public final long[] SCHEDULING_INTERVAL_VALUES = new long[] {
			0,
			AlarmManager.INTERVAL_FIFTEEN_MINUTES
	};
	
	public void setSchedulingIntervalIndex(Context context, int index) {
		
		long interval = SCHEDULING_INTERVAL_VALUES[index]; 
		
		context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
			.edit()
			.putLong(KEY_INTERVAL, interval)
			.commit();
		
		if(interval==DEF_INTERVAL) {
			new AlarmRegister().unregister(context);
		} else {
			new AlarmRegister().register(context, interval);
		}
	}
	
	public long getSchedulingInterval(Context context) {
		return context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE).getLong(KEY_INTERVAL, DEF_INTERVAL);
	}
	
	public int getSchedulingIntervalIndex(Context context) {
		
		long value = getSchedulingInterval(context);
		
		for(int i=0;i<SCHEDULING_INTERVAL_VALUES.length;i++) {
			if(value==SCHEDULING_INTERVAL_VALUES[i]) return i;
		}
		
		return 0;
	}
}
