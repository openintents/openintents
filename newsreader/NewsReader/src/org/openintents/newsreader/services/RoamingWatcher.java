package org.openintents.newsreader.services;
/*
*	This file is part of OI Newsreader.
*	Copyright (C) 2007-2009 OpenIntents.org
*	OI Newsreader is free software: you can redistribute it and/or modify
*	it under the terms of the GNU General Public License as published by
*	the Free Software Foundation, either version 3 of the License, or
*	(at your option) any later version.
*
*	OI Newsreader is distributed in the hope that it will be useful,
*	but WITHOUT ANY WARRANTY; without even the implied warranty of
*	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*	GNU General Public License for more details.
*
*	You should have received a copy of the GNU General Public License
*	along with OI Newsreader.  If not, see <http://www.gnu.org/licenses/>.
*/



import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

public class RoamingWatcher extends BroadcastReceiver{

	private boolean useWhileRoaming=false;
	private boolean isRoaming=false;

	private static boolean didStop=false;

	private static final String _TAG="org.openintents.news.services.RoamingWatcher";

	public void onReceive(Context context, Intent intent) {

		//get prefs and check if we should do anthing
		SharedPreferences settings = context.getSharedPreferences(NewsreaderService.PREFS_NAME, 0);
		useWhileRoaming		=settings.getBoolean(NewsreaderService.DO_ROAMING,false);

		Log.d(_TAG,"useWhileRoaming:"+useWhileRoaming);

		String action = intent.getAction();
		Log.d(_TAG, action );
		if( action == null )
		{
			Log.e(_TAG,"Action==null!" );
			return;
		}else if( android.content.Intent.ACTION_AIRPLANE_MODE_CHANGED.equals( action ) ) 
		{
			// TODO find appropriate action
			// was  android.content.Intent.ACTION_SERVICE_STATE_CHANGED
			
			Bundle b=intent.getExtras();
			//isRoaming=b.getBoolean("roaming");
			isRoaming= intent.getBooleanExtra("roaming",false);
			Log.d(_TAG,"current roaming state of phone/data is >>"+isRoaming+"<<");
			Log.d(_TAG,"did we stop? >>"+didStop+"<<");
		//	android.telephony.ServiceState state=android.telephony.ServiceState.getServiceState();
			if (isRoaming && ! useWhileRoaming)
			{
				Log.d(_TAG,"phone is in roaming mode, service is not allowed. will shut down");
				context.stopService(new Intent(
						context,
						NewsreaderService.class)
					);
				didStop=true;

			}else if (! isRoaming && ! useWhileRoaming && didStop)
			{
				didStop=false;
				Log.d(_TAG,"phone left roaming mode, service is restarted");
				context.startService(new Intent(
						context,
						NewsreaderService.class)						
					);
			}

		}

	}




}