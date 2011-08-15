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

package org.openintents.timescape.api.service;

import org.openintents.timescape.api.provider.EventStreamHelper;
import org.openintents.timescape.api.provider.EventStreamHelper.EventsTable;

import android.app.IntentService;
import android.content.Intent;
import android.net.Uri;

public class TimescapeCompatibilityService extends IntentService {

	public static final String N = "TimescapeCompatibilityService";
	
	public TimescapeCompatibilityService() {
		super(N);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
		//delete plugin data
		
		int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
		if(uid!=-1) {
			
			String where = EventsTable.UID + " = "+uid;
			
			Uri url = EventStreamHelper.getUri(EventStreamHelper.EVENTS_PATH);
			getContentResolver().delete(url, where, null);
			
			url = EventStreamHelper.getUri(EventStreamHelper.FRIENDS_PATH);
			getContentResolver().delete(url, where, null);
			
			url = EventStreamHelper.getUri(EventStreamHelper.SOURCES_PATH);
			getContentResolver().delete(url, where, null);
			
			url = EventStreamHelper.getUri(EventStreamHelper.PLUGINS_PATH);
			getContentResolver().delete(url, where, null);
		}
	}

}
