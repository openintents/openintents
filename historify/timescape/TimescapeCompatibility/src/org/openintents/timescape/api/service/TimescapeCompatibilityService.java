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
