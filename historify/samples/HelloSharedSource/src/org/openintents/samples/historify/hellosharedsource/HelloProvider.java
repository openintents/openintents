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

package org.openintents.samples.historify.hellosharedsource;

import org.openintents.historify.data.providers.Events;
import org.openintents.historify.data.providers.EventsProvider;

import android.database.Cursor;
import android.database.MatrixCursor;

public class HelloProvider extends EventsProvider{

	public static final String AUTHORITY = "org.openintents.samples.historify.hellosharedsource.provider";
	
	@Override
	protected String getAuthority() {
		return AUTHORITY;
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
		
		mc.addRow(new Object[] {
					1l, 
					lookupKey, 
					null,
					"Hello, SharedSource!",
					Events.Originator.contact,
					System.currentTimeMillis()
			});	

		return mc;
	}

	@Override
	protected Cursor queryEvent(String eventKey) {
		return null;
	}


}
