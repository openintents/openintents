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
import android.widget.Toast;


public class AlertTest extends IntentReceiver{

	public void onReceiveIntent(Context context,Intent intent){
		
		if (intent.getAction().equals("org.openintents.action.ALERT_TEST"))
		{
		
			Uri u=intent.getData();
			Toast.makeText(context, "Alert Received", Toast.LENGTH_SHORT).show();
		}

	}

}