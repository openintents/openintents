package org.openintents.samples.cachingcontentprovider;
/*
<!-- 
 * Copyright (C) 2010 OpenIntents UG
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
 --> 
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class DataService extends Service{

	private static final String TAG = "DataService";
	@Override
	public IBinder onBind(Intent arg0) {
		// we don't allow binding to this service so returning null is okay.
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		Log.d(TAG,"started");
		
		//normally all of this should be loaded off to another thread
		
		//a ~30% chance of deleteing all data in the cursor
		if((int)Math.floor((Math.random() *100))>66)
		{
			getContentResolver().delete(Customers.CONTENT_URI, null,null);
		}
		//write some random data
		for (int i=0;i<10;i++)
		{
			ContentValues cv=getImportantDataFromTheInternet();
			getContentResolver().insert(Customers.CONTENT_URI, cv);
		}
		
		//make sure we run again.
		scheduleNextRun();
		//done.
		stopSelf();
	}
	
	
	/**
	 * Creates a Random Customer with a Name and an Email Address
	 * @return ContentValues with a random data set
	 */
	private ContentValues getImportantDataFromTheInternet(){
		ContentValues cv= new ContentValues();
		//get us three random numbers to create a customer
		int r1=(int)Math.floor((Math.random() *8));
		int r2=(int)Math.floor((Math.random() *4));
		int r3=(int)Math.floor((Math.random() *4));
		String name=FIRST_NAMES[r1]+" "+LAST_NAMES[2];
		String email=name+DOMAINS[r3];
		
		cv.put(Customers.NAME,name);
		cv.put(Customers.EMAIL,email);
		
		return cv;
	}

	
	/**
	 * Set an alarm that will restart out service
	 */
	private void scheduleNextRun(){
		Intent intent=new Intent();
		intent.setClass(getApplicationContext(), DataService.class);
		PendingIntent pendingIntent=PendingIntent.getService(getApplicationContext(), 1, intent, PendingIntent.FLAG_ONE_SHOT);
		AlarmManager alarmManager=(AlarmManager)getSystemService(ALARM_SERVICE);
		long now= System.currentTimeMillis();
		//start after 1.5 minutes/ 90 seconds
		alarmManager.set(AlarmManager.RTC_WAKEUP, (now+ 9000), pendingIntent);
	}
	
	
	private String[] FIRST_NAMES={"John","Jack","Jim","Robert","Jane","Sally","Henrietta","Juliet"};
	private String[] LAST_NAMES={"Smith","Jones","Mueller","Doe"};
	private String[] DOMAINS={"@nowhere.com","@hell.com","@heaven.com","@randomland.it"};
	
}
