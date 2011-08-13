package org.openintents.timescape.api.service;

import android.app.IntentService;
import android.content.Intent;

public class TimescapeCompatibilityService extends IntentService {

	public static final String N = "TimescapeCompatibilityService";
	
	public TimescapeCompatibilityService() {
		super(N);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		
	}

}
