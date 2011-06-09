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

import android.app.AlarmManager;
import android.database.Cursor;
import android.database.MatrixCursor;

/**
 * 
 * Content Provider for test purposes.
 * 
 * @author berke.andras
 */
public class FactoryTestProvider extends EventsProvider {

	private static final int TEST_SET_SIZE = 10;
	private static final long EVENT_INTERVAL = AlarmManager.INTERVAL_DAY;
	
	@Override
	public boolean onCreate() {
		return super.onCreate();
	}

	@Override
	protected String getAuthority() {
		return FactoryTest.FACTORY_TEST_AUTHORITY;
	}

	@Override
	protected Cursor queryEvent(long eventId) {
		// not supported yet
		return null;
	}

	@Override
	protected Cursor queryEvents(String lookupKey) {

		MatrixCursor mc = new MatrixCursor(new String[] {
				Events._ID,
				Events.CONTACT_KEY,
				Events.EVENT_KEY,
				Events.MESSAGE,
				Events.ORIGINATOR,
				Events.PUBLISHED_TIME
				
		});
		
		String[] testMessages = getContext().getResources().getStringArray(R.array.factory_test);
		
		for(int i=0;i<TEST_SET_SIZE;i++) {
			mc.addRow(new Object[] {
					Long.valueOf(i+1), 
					lookupKey, 
					null,
					testMessages[i % testMessages.length],
					i % 2 == 0 ? Events.Originator.user : Events.Originator.contact,
					System.currentTimeMillis()-EVENT_INTERVAL*(i+1)
			});	
		}

		return mc;
		
	}
}
