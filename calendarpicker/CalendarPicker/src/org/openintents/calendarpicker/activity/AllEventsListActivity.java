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
    	if (getIntent().hasExtra(IntentConstants.CalendarEventPicker.COLUMN_EVENT_CALENDAR_ID)) {
        	long cal_id = getIntent().getLongExtra(IntentConstants.CalendarEventPicker.COLUMN_EVENT_CALENDAR_ID, -1);
    		selection = IntentConstants.CalendarEventPicker.COLUMN_EVENT_CALENDAR_ID + "=" + cal_id;
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