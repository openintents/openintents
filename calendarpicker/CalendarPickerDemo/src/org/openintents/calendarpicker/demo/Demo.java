package org.openintents.calendarpicker.demo;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import org.openintents.calendarpicker.contract.IntentConstants;
import org.openintents.calendarpicker.demo.provider.EventContentProvider;

import com.googlecode.chartdroid.demo.R;
import com.kostmo.charbuilder.Market;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Demo extends Activity implements View.OnClickListener {

	static final String TAG = "Demo";
	

	private static final int REQUEST_CODE_DATE_SELECTION = 1;
	private static final int REQUEST_CODE_EVENT_SELECTION = 2;
	
	private static final int DIALOG_CALENDARPICKER_DOWNLOAD = 1;
	
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

			Intent intent = new Intent(Intent.ACTION_PICK);
			intent.setType(IntentConstants.CalendarEventPicker.CONTENT_TYPE_CALENDAR_EVENT);

			List<EventWrapper> generated_events = generateRandomEvents(5);
			int event_count = generated_events.size();
			long[] event_ids = new long[event_count];
			long[] event_times = new long[event_count];
			for (int i = 0; i < event_count; i++) {
				EventWrapper event = generated_events.get(i);
				event_ids[i] = event.id;
				event_times[i] = event.timestamp;
			}


			intent.putExtra(IntentConstants.CalendarDatePicker.EXTRA_EVENT_IDS, event_ids);
			intent.putExtra(IntentConstants.CalendarDatePicker.EXTRA_EVENT_TIMESTAMPS, event_times);

			if (Market.isIntentAvailable(this, intent)) {
				startActivityForResult(intent, REQUEST_CODE_EVENT_SELECTION);
			} else {
				showDialog(DIALOG_CALENDARPICKER_DOWNLOAD);
			}
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
	

    
    // =============================================
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {

        switch (id) {
		case DIALOG_CALENDARPICKER_DOWNLOAD:
		{
			boolean has_android_market = Market.isIntentAvailable(this,
					Market.getMarketDownloadIntent(Market.PACKAGE_NAME_CALENDAR_PICKER));

			Log.d(TAG, "has_android_market? " + has_android_market);
			
			dialog.findViewById(android.R.id.button1).setVisibility(
					has_android_market ? View.VISIBLE : View.GONE);
			break;
		}
        default:
        	break;
        }
    }
    
	// ========================================================
	@Override
	protected Dialog onCreateDialog(int id) {

        LayoutInflater factory = LayoutInflater.from(this);
        
		switch (id) {
		case DIALOG_CALENDARPICKER_DOWNLOAD:
		{
			return new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.download_calendar_picker)
			.setMessage(R.string.calendar_picker_modularization_explanation)
			.setPositiveButton(R.string.download_calendar_picker_market, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					startActivity(Market.getMarketDownloadIntent(Market.PACKAGE_NAME_CALENDAR_PICKER));
				}
			})
			.setNeutralButton(R.string.download_calendar_picker_web, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					startActivity(new Intent(Intent.ACTION_VIEW, Market.APK_DOWNLOAD_URI_COLOR_PICKER));
				}
			})
			.create();
		}
		}

		return null;
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