/*
 * Copyright (C) 2010 Karl Ostmo
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

package org.openintents.calendarpicker.activity;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.contract.IntentConstants;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;


public class AllEventsListActivity extends AbstractEventsListActivity {

	static final String TAG = "AllEventsListActivity"; 


    // ========================================================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    
    // ========================================================================
	Cursor requery() {
		
        Uri intent_data = getIntent().getData();
        if (intent_data == null) return null;

    	Log.d(TAG, "Querying content provider for: " + intent_data);
    	

    	String selection = null;
    	if (getIntent().hasExtra(IntentConstants.CalendarEventPicker.ContentProviderColumns.COLUMN_EVENT_CALENDAR_ID)) {
        	long cal_id = getIntent().getLongExtra(IntentConstants.CalendarEventPicker.ContentProviderColumns.COLUMN_EVENT_CALENDAR_ID, -1);
    		selection = IntentConstants.CalendarEventPicker.ContentProviderColumns.COLUMN_EVENT_CALENDAR_ID + "=" + cal_id;
    	}
    	
		Cursor cursor = managedQuery(intent_data,
				new String[] {
					KEY_ROWID,
					KEY_EVENT_TIMESTAMP,
					KEY_EVENT_TITLE},
				selection,
				null,
				constructOrderByString());

		String header_text = cursor.getCount() + " event(s)";
		((TextView) findViewById(R.id.list_header)).setText(header_text);
		
		return cursor;
	}
}