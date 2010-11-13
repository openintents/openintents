package org.openintents.calendarpicker.demo;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

import org.openintents.calendarpicker.contract.IntentConstants;
import org.openintents.calendarpicker.demo.provider.EventContentProvider;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Demo extends Activity implements View.OnClickListener {

	static final String TAG = "Demo";

	private static final int REQUEST_CODE_DATE_SELECTION = 1;
	private static final int REQUEST_CODE_EVENT_SELECTION = 2;
	
	private static final int DIALOG_CALENDARPICKER_DOWNLOAD = 1;
	private static final int DIALOG_GOOGLE_CALENDAR_SELECTION = 2;
	
	// ========================================================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.main);
        
        for (int view : new int[] {
        		R.id.button_pick_date_no_events,
        		R.id.button_pick_date_with_events,
        		R.id.button_pick_event_intent_extras,
        		R.id.button_pick_event_content_provider,
        		R.id.button_pick_event_google_calendar
        		}) {
        	findViewById(view).setOnClickListener(this);
        }
    }
    
    // ========================================================================
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	outState.putLong("selected_google_calendar_id", selected_google_calendar_id);
    }
    
    // ========================================================================
    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);

    	selected_google_calendar_id = savedInstanceState.getLong("selected_google_calendar_id");
    }
    
	// ========================================================================
    
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_pick_date_no_events:
		{
			Intent i = new Intent(Intent.ACTION_PICK);
			i.setType(IntentConstants.CalendarDatePicker.CONTENT_TYPE_DATETIME);
			downloadLaunchCheck(i, REQUEST_CODE_DATE_SELECTION);
			break;
		}
		case R.id.button_pick_date_with_events:
		{
	    	Uri u = EventContentProvider.constructUri(12345);
			Intent i = new Intent(Intent.ACTION_PICK, u);

			Log.e(TAG, "I specified a URI for the date-picker Intent!");
			
			// XXX Specifying the "mime-type" manually has the effect of
			// nullifying the data URI!
//			i.setType(IntentConstants.CalendarDatePicker.CONTENT_TYPE_DATETIME);
			downloadLaunchCheck(i, REQUEST_CODE_DATE_SELECTION);
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

			downloadLaunchCheck(intent, REQUEST_CODE_EVENT_SELECTION);

			break;
		}
		case R.id.button_pick_event_content_provider:
		{
	    	Uri u = EventContentProvider.constructUri(12345);
			Intent i = new Intent(Intent.ACTION_PICK, u);
			downloadLaunchCheck(i, REQUEST_CODE_EVENT_SELECTION);
			break;
		}
		
		case R.id.button_pick_event_google_calendar:
		{
	    	showDialog(DIALOG_GOOGLE_CALENDAR_SELECTION);
			break;
		}
		}
	}

    // ========================================================================
	void downloadLaunchCheck(Intent intent, int request_code) {
		
		Log.d(TAG, "Checking whether event is available: " + intent);
		
		boolean is_available = Market.isIntentAvailable(this, intent);

		
		
		
		if (is_available) {
			startActivityForResult(intent, request_code);
		} else {
			showDialog(DIALOG_CALENDARPICKER_DOWNLOAD);
		}
	}
	
    // ========================================================================
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
		case DIALOG_GOOGLE_CALENDAR_SELECTION:
		{
			final Cursor cursor = getCalendarManagedCursor(null, null, "calendars");
			
			return new AlertDialog.Builder(this)
			.setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle("Choose calendar")
			.setSingleChoiceItems(cursor, -1, "name", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cursor.moveToPosition(which);
					selected_google_calendar_id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
				}
			})
			.setPositiveButton("Select", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "Selected calendar ID: " + selected_google_calendar_id);
					
//					Uri uri = CalendarWrapperContentProvider.constructUri(selected_google_calendar_id);
					Uri uri = new Uri.Builder()
						.scheme(ContentResolver.SCHEME_CONTENT)
						.authority( IntentConstants.ANDROID_CALENDAR_AUTHORITY_2_0)
						.appendPath("events").build();

					Log.d(TAG, "Sending content provider Uri: " + uri);
					
					Intent intent = new Intent(Intent.ACTION_PICK, uri);
					
					

					ContentResolver cr = getContentResolver();
					String resolved_type = intent.resolveType(cr);
					Log.i(TAG, "Resolved type: " + resolved_type);
					
					
					downloadLaunchCheck(intent, REQUEST_CODE_EVENT_SELECTION);

//					Log.d(TAG, "Querying content provider Uri in Demo: " + uri);
//					Cursor cursor = managedQuery(uri, null, null, null, null);
//					slurpGoogleCalendarEventsFromCursor(cursor);
				}
			})
			.create();
		}
		
		}

		return null;
	}
	
	long selected_google_calendar_id = -1;
	
	
	
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
    void slurpGoogleCalendarEventsFromCursor(Cursor managedCursor) {
    


    	if (managedCursor != null && managedCursor.moveToFirst()) {

    		Log.i(TAG, "Listing Calendar Event Details");

    		do {

    			Log.i(TAG, "**START Calendar Event Description**");

    			for (int i = 0; i < managedCursor.getColumnCount(); i++) {
    				Log.i(TAG, managedCursor.getColumnName(i) + "="
    						+ managedCursor.getString(i));
    			}
    			Log.i(TAG, "**END Calendar Event Description**");
    		} while (managedCursor.moveToNext());
    	} else {
    		Log.i(TAG, "No Calendars");
    	}
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
   			long epoch_date = data.getLongExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_EPOCH, 0);
   			
   			((TextView) findViewById(R.id.date_picker_result)).setText( iso_date + "; " + epoch_date );
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
    
    
    
    
    /**
     * @param projection
     * @param selection
     * @param path
     * @return
     */
    private Cursor getCalendarManagedCursor(String[] projection,
            String selection, String path) {
    	
		Uri calendars = new Uri.Builder()
		.scheme(ContentResolver.SCHEME_CONTENT)
		.authority( IntentConstants.ANDROID_CALENDAR_AUTHORITY_2_0)
		.appendPath("calendars").build();

		Cursor managedCursor = null;
        try {
            managedCursor = managedQuery(calendars, projection, selection,
                    null, null);
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Failed to get provider at ["
                    + calendars.toString() + "]");
        }

        return managedCursor;
    }
}