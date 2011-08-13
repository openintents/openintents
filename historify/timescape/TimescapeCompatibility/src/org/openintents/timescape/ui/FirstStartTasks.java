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
package org.openintents.timescape.ui;

import org.openintents.timescape.api.requestscheduling.RequestSender;

import android.content.Context;

/**
 * 
 * Tasks to run at first application startup.
 * 
 * @author berke.andras
 */
public class FirstStartTasks {
	
	public static void onStart(Context context) {
		
		//check if application has been started before
		if(isFirstStart(context)) {
			
			//do tasks
			new RequestSender().requestRegisterPlugin(context);
			commit(context);
		}
	}

	private static boolean isFirstStart(Context context) {
		return context.getSharedPreferences("start_tasks", Context.MODE_PRIVATE).getBoolean("first", true);
	}
	
	private static void commit(Context context) {
		context.getSharedPreferences("start_tasks", Context.MODE_PRIVATE).edit().putBoolean("first", false).commit();
	}
}
