package org.openintents.news.services;
/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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



import android.os.Bundle;
import android.content.IntentReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.util.Log;
import android.content.SharedPreferences;


import org.openintents.provider.News;
import org.openintents.provider.News;

public class AutostartService extends IntentReceiver{

	private boolean debugMode=false;
	private boolean startOnSystemBoot=false;

	private static final String _TAG="org.openintents.news.services.AutostartService";

	public void onReceiveIntent(Context context, Intent intent) {

		//get prefs and check if we should do anthing
		SharedPreferences settings = context.getSharedPreferences(NewsreaderService1.PREFS_NAME, 0);
		//useWhileRoaming		=settings.getBoolean(NewsreaderService.DO_ROAMING,false);
		startOnSystemBoot	=settings.getBoolean(NewsreaderService1.ON_BOOT_START,false);
		debugMode			=settings.getBoolean(NewsreaderService1.DEBUG_MODE,false);
	
		//startOnSystemBoot=true;
		Log.d(_TAG,"StartOnSystemBoot:"+startOnSystemBoot);
		if (!startOnSystemBoot)
		{//Nothing to do,go home
			return;
		}else{
			String action = intent.getAction();
			Log.d(_TAG, action );
			if( action == null )
			{
				Log.e(_TAG,"Action==null!" );
				return;
			}else if( "android.intent.action.BOOT_COMPLETED".equals( action ) ) 
			{
				
				context.startService(new Intent(
						context,
						NewsreaderService1.class)
				,null);

			}
		}
	}




}