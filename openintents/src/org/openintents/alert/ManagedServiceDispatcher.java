package org.openintents.alert;

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
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.content.ContentValues;

import org.openintents.provider.Alert;

 public class ManagedServiceDispatcher extends IntentReceiver{

	public static final String _TAG="ManagedServiceDispatcher";

	Cursor c;
	public void onReceiveIntent(Context context,Intent intent){
		long lastTime=0;
		long timeIntervall=0;
		String className="";
		long now=System.currentTimeMillis();
		if (intent.getAction().equals(org.openintents.OpenIntents.SERVICE_MANAGER))
		{
			
			Alert.init(context);
			c=Alert.mContentResolver.query(
				Alert.ManagedService.CONTENT_URI,
				Alert.ManagedService.PROJECTION,
				null,
				null,
				null
				);

			if (c!=null && c.count()>0)
			{
				c.first();


				while (!c.isAfterLast())
				{
					lastTime=c.getLong(c.getColumnIndex(Alert.ManagedService.LAST_TIME));
					timeIntervall=c.getLong(c.getColumnIndex(Alert.ManagedService.TIME_INTERVALL));
					className=c.getString(c.getColumnIndex(Alert.ManagedService.SERVICE_CLASS));
					if (now+timeIntervall>lastTime)
					{

						try
						{
							context.startService(new Intent(
								context,
								Class.forName(className))
								,null);
						}
						catch (Exception e)
						{
							Log.e(_TAG,"couldnt start service >>"+className+"<<, reason>>"+e.getMessage()+"<<");
						}



						c.updateString(c.getColumnIndex(Alert.ManagedService.LAST_TIME),Long.toString(now));
						c.commitUpdates();
					}

					c.next();
				}
				c.close();
			}




		}
	}



 }/*eoc*/