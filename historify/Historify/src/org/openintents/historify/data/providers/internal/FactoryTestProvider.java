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

package org.openintents.historify.data.providers.internal;

import org.openintents.historify.R;
import org.openintents.historify.data.providers.Events;
import org.openintents.historify.data.providers.EventsProvider;
import org.openintents.historify.uri.ContentUris;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;

/**
 * 
 * Content Provider for test purposes.
 * 
 * @author berke.andras
 */
public class FactoryTestProvider extends EventsProvider {

	/**
	 * Class for storing the parameters of the test set (size and event interval).
	 */
	public static class FactoryTestConfig {
		
		private static final int DEFAULT_TEST_SET_SIZE = 100;
		private static final long DEFAULT_EVENT_INTERVAL = AlarmManager.INTERVAL_DAY;
		
		private static final String PREF_FILE = "factorytest";
		private static final String PREF_TEST_SET_SIZE = "test_set_size";
		private static final String PREF_EVENT_INTERVAL = "event_interval";
		
		
		public final int testSetSize;
		public final long eventInterval;
		
		public FactoryTestConfig(int testSetSize, long eventInterval) {
			this.testSetSize = testSetSize;
			this.eventInterval = eventInterval;
		}
		
		public static FactoryTestConfig load(Context context) {
			
			SharedPreferences sp =  context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE);
			return new FactoryTestConfig(
					sp.getInt(PREF_TEST_SET_SIZE, DEFAULT_TEST_SET_SIZE), 
					sp.getLong(PREF_EVENT_INTERVAL, DEFAULT_EVENT_INTERVAL));
			
		}
		
		public void save(Context context) {
			
			context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)
			.edit()
			.putInt(PREF_TEST_SET_SIZE, testSetSize)
			.putLong(PREF_EVENT_INTERVAL, eventInterval)
			.commit();
			
			//parameters have been changed
			//notify ContentResolver to reload events
			context.getContentResolver().notifyChange(FactoryTest.EVENTS_URI, null);
		}
	}
	
	
	@Override
	public boolean onCreate() {
		return super.onCreate();
	}

	@Override
	protected String getAuthority() {
		return FactoryTest.FACTORY_TEST_AUTHORITY;
	}

	@Override
	protected Cursor queryEventsForContact(String lookupKey) {

		FactoryTestConfig factoryTestConfig = FactoryTestConfig.load(getContext());
		
		MatrixCursor mc = new MatrixCursor(new String[] {
				Events._ID,
				Events.CONTACT_KEY,
				Events.EVENT_KEY,
				Events.MESSAGE,
				Events.ORIGINATOR,
				Events.PUBLISHED_TIME
				
		});
		
		String[] testMessages = getContext().getResources().getStringArray(R.array.factory_test);
		
		for(int i=0;i<factoryTestConfig.testSetSize;i++) {
			mc.addRow(new Object[] {
					Long.valueOf(i+1), 
					lookupKey, 
					null,
					testMessages[i % testMessages.length],
					i % 2 == 0 ? Events.Originator.user : Events.Originator.contact,
					System.currentTimeMillis()-factoryTestConfig.eventInterval*(i+1)
			});	
		}

		return mc;
		
	}

	@Override
	protected Cursor queryEvent(long eventId) {
		return null;
	}

	@Override
	protected Cursor queryEvents() {
		return null;
	}

	@Override
	protected Cursor queryEventsByKey(String eventKey) {
		return null;
	}
}
