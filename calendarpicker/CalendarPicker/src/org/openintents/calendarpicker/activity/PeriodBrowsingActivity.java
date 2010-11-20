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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.openintents.calendarpicker.container.TimespanEventAggregator;
import org.openintents.calendarpicker.container.SimpleEvent;
import org.openintents.calendarpicker.contract.CalendarPickerConstants;
import org.openintents.calendarpicker.contract.CalendarPickerConstants.CalendarEventPicker;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class PeriodBrowsingActivity extends Activity {

    final static public String TAG = "PeriodBrowsingActivity";

	final static SimpleDateFormat HYPEHENATED_ISO_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
	final static SimpleDateFormat DAY_OF_WEEK_FORMATTER = new SimpleDateFormat("EEE");
	
	
    
    // ========================================================================
    void finishWithDate(Date date) {
		// If there are no events, just return the day.
		Intent i = new Intent();
		
		if (date != null) {
			i.putExtra(CalendarPickerConstants.CalendarDatePicker.IntentExtras.INTENT_EXTRA_EPOCH, date.getTime());
			i.putExtra(CalendarPickerConstants.CalendarDatePicker.IntentExtras.INTENT_EXTRA_DATETIME, HYPEHENATED_ISO_DATE_FORMATTER.format(date));
		}
		
		setResult(Activity.RESULT_OK, i);
		finish();
    }

    // ========================================================================
	/** We have been passed the data directly. */
    public static List<SimpleEvent> getEventsFromIntent(Intent intent) {
    	
    	List<SimpleEvent> events = new ArrayList<SimpleEvent>();

     	Log.d(TAG, "We have been passed the data directly.");

     	// Mandatory fields
    	long[] event_ids = intent.getLongArrayExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_EVENT_IDS);
    	long[] event_timestamps = intent.getLongArrayExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_EVENT_TIMESTAMPS);
    	
    	// Optional fields
    	String[] event_titles = intent.getStringArrayExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_EVENT_TITLES);

    	if (event_ids != null && event_timestamps != null) {
	    	for (int i=0; i<event_timestamps.length; i++) {
	    		
	    		// Optional fields
	    		String event_title = null;
	    		if (event_titles != null)
	    			event_title = event_titles[i];
	    		
	    		events.add( new SimpleEvent(event_ids[i], event_timestamps[i], event_title) );
	    	}

		    Log.d(TAG, "Added " + event_timestamps.length + " timestamps.");
    	}

    	Collections.sort(events);
    	return events;
    }

    // ========================================================================
    public static class TimespanEventMaximums {
    	// Holds the maximums
    	public int max_event_count_per_day = 0;
    	public float[] max_quantities_per_day = new float[CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES.length];
    	
    	public void clear() {
    		this.max_event_count_per_day = 0;
    		for (int i=0; i<this.max_quantities_per_day.length; i++)
    			this.max_quantities_per_day[i] = 0;
    	}
    	
    	public void updateMax(TimespanEventAggregator day) {
    		if (day.getEventCount() > this.max_event_count_per_day)
    			this.max_event_count_per_day = day.getEventCount();
			
    		for (int i=0; i<this.max_quantities_per_day.length; i++)
        		if (day.getAggregateQuantity(i) > this.max_quantities_per_day[i])
        			this.max_quantities_per_day[i] = day.getAggregateQuantity(i);
    	}
    }
    
    // ========================================================================    
    public static String[] getAugmentedProjection(Intent intent) {
    	
    	List<String> projection = new ArrayList<String>(Arrays.asList(new String[] {BaseColumns._ID, CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.TIMESTAMP, CalendarEventPicker.ContentProviderColumns.TITLE}));
    	for (String extra : CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES)
    		if (intent.hasExtra(extra))
    			projection.add(intent.getStringExtra(extra));	// Adds column name
    	
    	return projection.toArray(new String[] {});
    }
    
    // ========================================================================
    public List<SimpleEvent> getEventsFromUri(Uri uri, Intent intent, TimespanEventMaximums maximums) {

    	List<SimpleEvent> events = new ArrayList<SimpleEvent>();

    	Log.d(TAG, "Querying content provider for: " + uri);
    	String selection = null;
    	if (intent.hasExtra(CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.CALENDAR_ID)) {
        	long cal_id = intent.getLongExtra(CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.CALENDAR_ID, -1);
    		selection = CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.CALENDAR_ID + "=" + cal_id;
    	}


    	Cursor cursor = managedQuery(uri,
			getAugmentedProjection(intent),
			selection,
			null,
			CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.TIMESTAMP + " ASC");
    	
    	if (cursor != null && cursor.moveToFirst()) {

    		int id_column = cursor.getColumnIndex(BaseColumns._ID);
    		int timestamp_column = cursor.getColumnIndex(CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.TIMESTAMP);
    		int title_column = cursor.getColumnIndex(CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.TITLE);
    		int[] quantity_column_indices = new int[CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES.length];
        	for (int i=0; i<CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES.length; i++)
        		quantity_column_indices[i] = intent.hasExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES[i]) ?
        				cursor.getColumnIndex(intent.getStringExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES[i]))
        				: -1;

        				
        	// SLURP EVENTS WHILE FINDING MIN/MAX DAY QUANTITIES

        	// We know there is at east one event, since moveToFirst() returned success.
        	// Set calendar to the first day with an event.
        				
        	Calendar day_iterator_calendar = new GregorianCalendar();
        	day_iterator_calendar.clear();
        	

        	// Holds the running counts for a day
        	TimespanEventAggregator day_aggregator = new TimespanEventAggregator();
        	
        	do {

            	long timestamp_millis = cursor.getLong(timestamp_column);

            	if (timestamp_millis >= day_iterator_calendar.getTimeInMillis()) {
            		// Our current event has fallen on a later day, so we
            		// push back the calendar.
            		
                	roundCalendarToNextDay(day_iterator_calendar, timestamp_millis);
                	
                	// Reset the running totals
                	day_aggregator.reset(null);
            	}


    			SimpleEvent event = new SimpleEvent(
    					cursor.getLong(id_column),
    					timestamp_millis,
    					cursor.getString(title_column));

    			events.add(event);
    			for (int i=0; i<quantity_column_indices.length; i++) {
    				if (quantity_column_indices[i] >= 0) {
    					event.quantities[i] = cursor.getFloat(quantity_column_indices[i]);
    					day_aggregator.addAggregateQuantity(i, event.quantities[i]);
    				}
    			}
    			day_aggregator.incrementEventCount();

            	// Update the maximums if need be
    			maximums.updateMax(day_aggregator);
    			
        	} while (cursor.moveToNext());


        	// This is the old version of the while loop that is above.
        	// It doesn't record daily maximums.
        	/*
    		do {
    			
    			long timestamp = cursor.getLong(timestamp_column);
    			String title = cursor.getString(title_column);

    			

    			SimpleEvent event = new SimpleEvent(cursor.getLong(id_column), timestamp, title);
    			for (int i=0; i<quantity_column_indices.length; i++)
    				if (quantity_column_indices[i] >= 0)
    					event.quantities[i] = cursor.getFloat(quantity_column_indices[i]);
    			
    			events.add(event);

    		} while (cursor.moveToNext());
    		*/

    	} else {
    		Log.e(TAG, "There were no rows in this Cursor: " + cursor);
    	}

    	return events;
    }
    
    // ========================================================================
    static void roundCalendarToNextDay(Calendar calendar, long millis) {
    	calendar.setTimeInMillis(millis);
    	int day = calendar.get(Calendar.DATE);
    	int month = calendar.get(Calendar.MONTH);
    	int year = calendar.get(Calendar.YEAR);
    	calendar.clear();
    	calendar.set(year, month, day);
    	calendar.add(Calendar.DAY_OF_MONTH, 1);
    }
    
}
