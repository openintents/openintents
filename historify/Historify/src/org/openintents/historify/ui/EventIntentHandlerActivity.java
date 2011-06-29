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

package org.openintents.historify.ui;

import org.openintents.historify.data.providers.Events;
import org.openintents.historify.data.providers.internal.QuickPosts;
import org.openintents.historify.data.providers.internal.QuickPosts.QuickPostSourcesTable;
import org.openintents.historify.uri.Actions;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * 
 * Activity for handling internal sources' event intents.
 * 
 * @author berke.andras
 */
public class EventIntentHandlerActivity extends Activity {

	private static final String N = "EventIntentHandler";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		Intent i = new Intent();
//        i.setAction(Intent.ACTION_VIEW);
//        i.setData(CallLog.Calls.CONTENT_URI);
//        i.setType(CallLog.Calls.CONTENT_TYPE);
//        startActivity(i); 
		
		setTheme(android.R.style.Theme_Dialog);
		TextView tv = new TextView(this);
		tv.setText(getIntent().getAction());
		setContentView(tv);
		
		String action = getIntent().getAction();
		
		if(Actions.ACTION_VIEW_MESSAGING_EVENT.equals(action)) {
			tv.setText("TODO: view messaging event");
		} else if(Actions.ACTION_VIEW_CALLOG_EVENT.equals(action)) {
			tv.setText("TODO: view callog event");
		} else if(Actions.ACTION_VIEW_QUICKPOST_EVENT.equals(action)) {
			handleQuickPostEventIntent();
			finish();
		} else {
			finish();
		}
		
		
	}

	private void handleQuickPostEventIntent() {
		
		long eventId = getIntent().getLongExtra(Actions.EXTRA_EVENT_ID, -1);
		if(eventId!=-1) {
			
			//query the event's EVENT_INTENT as registered by the QuickPost client app
			Uri eventUri = Uri.withAppendedPath(QuickPosts.SOURCE_URI,Events.EVENTS_PATH+"/"+eventId);
			
			String intentAction = null;
			Cursor c = getContentResolver().query(eventUri, null, null, null, null);
			if(c.moveToFirst()) {
				intentAction = c.getString(c.getColumnIndex(QuickPostSourcesTable.EVENT_INTENT));
			}
			c.close();
			
			//fire intent if action is not null
			if(intentAction!=null) {
				Intent i = new Intent();
				i.setAction(intentAction);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra(Actions.EXTRA_EVENT_KEY, getIntent().getStringExtra(Actions.EXTRA_EVENT_KEY));
				try {
					startActivity(i);	
				} catch(ActivityNotFoundException e) {
					Log.e(N, "Unable to launch Activity to handle action: "+intentAction);
				}
				
			}
		}
		 

	}
}
