package org.openintents.calendarpicker.activity;

import org.openintents.calendarpicker.R;

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
    	Log.d(TAG, "Querying content provider for: " + intent_data);
    	
		Cursor cursor = managedQuery(intent_data,
				new String[] {
					KEY_ROWID,
					KEY_EVENT_TIMESTAMP,
					KEY_EVENT_TITLE},
				null,
				null,
				constructOrderByString());

		String header_text = cursor.getCount() + " event(s)";
		((TextView) findViewById(R.id.list_header)).setText(header_text);
		
		return cursor;
	}
}