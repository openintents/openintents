package org.openintents.calendarpicker.activity;

import java.text.DateFormatSymbols;
import java.util.Date;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.contract.IntentConstants;
import org.openintents.calendarpicker.view.FlingableMonthView;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;


public class DayEventsListActivity extends AbstractEventsListActivity {

	static final String TAG = "DayEventsListActivity";
	
    // ========================================================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        

        View main = findViewById(R.id.main);
        main.setBackgroundResource(R.drawable.panel_background);
    }
    
    // ========================================================================
	Cursor requery() {

        Uri intent_data = getIntent().getData();
    	Log.d(TAG, "Querying content provider for: " + intent_data);
    	
        Date d = new Date(getIntent().getLongExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_EPOCH, 0));
        

        Log.e(TAG, "Received date: " + d.getDate());
        long day_begin = d.getTime();
        long day_end = day_begin + FlingableMonthView.MILLISECONDS_PER_DAY;
        
		
        
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
				KEY_EVENT_TIMESTAMP + ">=? AND " + KEY_EVENT_TIMESTAMP + "<?"
				+ (selection == null ? "" : " AND " + selection),
				new String[] {Long.toString(day_begin), Long.toString(day_end)},
				constructOrderByString());

		Log.d(TAG, "Uri queried: " + intent_data);
		Log.d(TAG, "Cursor: " + cursor);
		
		int cursor_row_count = cursor.getCount();
		
		String header_text = cursor_row_count + " event(s) on " + d.getDate();
		((TextView) findViewById(R.id.list_header)).setText(header_text);
		
		return cursor;
	}

}

