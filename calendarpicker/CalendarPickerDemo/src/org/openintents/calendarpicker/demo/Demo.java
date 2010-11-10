package org.openintents.calendarpicker.demo;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import org.openintents.calendarpicker.contract.IntentConstants;
import org.openintents.calendarpicker.demo.provider.EventContentProvider;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Demo extends Activity implements View.OnClickListener {

	static final String TAG = "Demo";
	

	final int REQUEST_CODE_DATE_SELECTION = 1;
	final int REQUEST_CODE_EVENT_SELECTION = 2;
	
	
	// ========================================================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        for (int view : new int[] {
        		R.id.button_pick_date_no_events,
        		R.id.button_pick_date_with_events,
        		R.id.button_pick_event_intent_extras,
        		R.id.button_pick_event_content_provider
        		}) {
        	findViewById(view).setOnClickListener(this);
        }
    }
    
	// ========================================================================
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_pick_date_no_events:
		{
			Intent i = new Intent(Intent.ACTION_PICK);
			i.setType(IntentConstants.CalendarDatePicker.CONTENT_TYPE_DATETIME);
            startActivityForResult(i, REQUEST_CODE_DATE_SELECTION);
			break;
		}
		case R.id.button_pick_date_with_events:
		{
			Intent i = new Intent(Intent.ACTION_PICK);
			i.setType(IntentConstants.CalendarDatePicker.CONTENT_TYPE_DATETIME);
            startActivityForResult(i, REQUEST_CODE_DATE_SELECTION);
			break;
		}
		case R.id.button_pick_event_intent_extras:
		{
	    	Uri u = EventContentProvider.constructUri(12345);
			Intent i = new Intent(Intent.ACTION_PICK, u);
            startActivityForResult(i, REQUEST_CODE_EVENT_SELECTION);
			break;
		}
		case R.id.button_pick_event_content_provider:
		{
	    	Uri u = EventContentProvider.constructUri(12345);
			Intent i = new Intent(Intent.ACTION_PICK, u);
            startActivityForResult(i, REQUEST_CODE_EVENT_SELECTION);
			break;
		}
		}
	}
	
	// ========================================================================
    public static class EventWrapper {
    	public long id, timestamp;
    	public String title;
    }
    
	// ========================================================================
    public static List<EventWrapper> generateRandomEvents(int event_count) {
    	
    	List<EventWrapper> events = new ArrayList<EventWrapper>();
		GregorianCalendar cal = new GregorianCalendar();
		Random r = new Random();

		for (int event_id = 0; event_id < event_count; event_id++) {
			EventWrapper event = new EventWrapper();
    		cal.roll(GregorianCalendar.DATE, r.nextInt(3));
    		event.timestamp = cal.getTimeInMillis();
			event.id = event_id;
    		events.add(event);
		}
		
		return events;
    }
    
	// ========================================================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            Log.i(TAG, "==> result " + resultCode + " from subactivity!  Ignoring...");
//            Toast t = Toast.makeText(this, "Action cancelled!", Toast.LENGTH_SHORT);
//            t.show();
            return;
        }
        
  	   	switch (requestCode) {
   		case REQUEST_CODE_DATE_SELECTION:
   		{
   			String iso_date = data.getStringExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_DATETIME);
   			Toast.makeText(this, "Result: " + iso_date, Toast.LENGTH_SHORT).show();
   			((TextView) findViewById(R.id.date_picker_result)).setText( iso_date );
            break;
        }
   		case REQUEST_CODE_EVENT_SELECTION:
   		{
   			long id = data.getLongExtra(IntentConstants.INTENT_EXTRA_CALENDAR_EVENT_ID, -1);
   			Toast.makeText(this, "Result: " + id, Toast.LENGTH_SHORT).show();
   			((TextView) findViewById(R.id.event_picker_result)).setText( "" + id );
            break;
        }
  	   	}
    }
}