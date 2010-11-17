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

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.container.SimpleEvent;
import org.openintents.calendarpicker.contract.CalendarPickerConstants;
import org.openintents.calendarpicker.view.FlingableWeekView;
import org.openintents.calendarpicker.view.TimelineViewHorizontal;
import org.openintents.calendarpicker.view.FlingableWeekView.MonthUpdateCallback;
import org.openintents.calendarpicker.view.FlingableWeekView.OnDaySelectionListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;


public class WeekActivity extends Activity {

    final static public String TAG = "WeekActivity";

	static final int REQUEST_CODE_EVENT_SELECTION = 1;
	static final int REQUEST_CODE_MONTH_YEAR_SELECTION = 2;


    final static String BUNDLE_CALENDAR_EPOCH = "BUNDLE_CALENDAR_EPOCH";
    
	
    public static final long INVALID_EVENT_ID = -1;
    public static final long INVALID_DATE = 0;
    
    List<SimpleEvent> events = new ArrayList<SimpleEvent>();
    

	final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	TextView month_title;
	FlingableWeekView week_view;
	TimelineViewHorizontal tiny_timeline;
	
    // ========================================================================
	void updateMonthHeader(Calendar calendar) {
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy");
        month_title.setText( formatter.format(calendar.getTime()) );
	}
	
    // ========================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.weeks_view);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.titlebar_icon);

        this.month_title = (TextView) findViewById(R.id.month_title);


        Uri intent_data = getIntent().getData();
        // Zip the events
        if (intent_data != null) {
        	// We have been passed a cursor to the data via a content provider.
			this.events = MonthActivity.getEventsFromUri(intent_data, getIntent(), this);

        } else {
        	Log.d(TAG, "No URI was passed, checking for Intent extras instead...");
			this.events = MonthActivity.getEventsFromIntent(getIntent());
		}

        DateFormatSymbols dfs = new DateFormatSymbols();
//        String weekdays[] = dfs.getWeekdays();
        String weekdays[] = dfs.getShortWeekdays();
        LayoutParams lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        final LinearLayout weekday_header_layout = (LinearLayout) findViewById(R.id.weekdays_header);
		for (int i=Calendar.getInstance().getFirstDayOfWeek(); i<weekdays.length; i++) {
	        TextView tv = new TextView(this);
	        tv.setGravity(Gravity.CENTER);
	        tv.setText(weekdays[i]);
	        weekday_header_layout.addView(tv, lp);
		}
        

		tiny_timeline = (TimelineViewHorizontal) findViewById(R.id.tiny_timeline);
		
		week_view = (FlingableWeekView) findViewById(R.id.full_week);
        week_view.setMonthUpdateCallback(new MonthUpdateCallback() {
        	@Override
			public void updateMonth(Calendar cal) {
				updateMonthHeader(cal);
				tiny_timeline.setDate(cal.getTime());
			}
        });
        
        week_view.setOnDayTouchListener(new OnDaySelectionListener() {

			@Override
			public void updateDate(Date date) {

				int child_idx = -1;
				if (date != null) {
					Calendar c = new GregorianCalendar();
					c.setTime(date);
					child_idx = c.get(Calendar.DAY_OF_WEEK) - c.getMinimum(Calendar.DAY_OF_WEEK);
				}
				
				for (int i=0; i<weekday_header_layout.getChildCount(); i++) {
					TextView tv = (TextView) weekday_header_layout.getChildAt(i);
					tv.setTextColor(i == child_idx ? Color.RED : getResources().getColor(android.R.color.secondary_text_dark));
				}
			}
        });
        

        week_view.setOnScrollListener(new OnDaySelectionListener() {
			@Override
			public void updateDate(Date date) {
				tiny_timeline.setDate(date);
			}
        });
				
        
        week_view.setOnDayClickListener(new OnDaySelectionListener() {

			@Override
			public void updateDate(Date date) {

				// Highlight the day of the week in the header bar
				Calendar c = new GregorianCalendar();
				c.setTime(date);
				int child_idx = c.get(Calendar.DAY_OF_WEEK) - c.getMinimum(Calendar.DAY_OF_WEEK);
				for (int i=0; i<weekday_header_layout.getChildCount(); i++) {
					TextView tv = (TextView) weekday_header_layout.getChildAt(child_idx);
					tv.setTextColor(i == child_idx ? Color.RED : getResources().getColor(android.R.color.primary_text_dark));
				}
				
				// FIXME
				/*
				Uri data = getIntent().getData();
				if (data != null) {

					Intent i = new Intent(WeekActivity.this, DayEventsListActivity.class);

					i.setData(data);
					if (date != null) {
						i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_EPOCH, date.getTime());
						i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_DATETIME, sdf.format(date));
					}
					startActivityForResult(i, REQUEST_CODE_EVENT_SELECTION);

				} else {
					// If there are no events, just return the day.
					Intent i = new Intent();
					
					if (date != null) {
						i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_EPOCH, date.getTime());
						i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_DATETIME, sdf.format(date));
					}
					
					setResult(Activity.RESULT_OK, i);
					finish();
				}
				*/
			}
        });

        
        Calendar current_month_calendar = new GregorianCalendar();
        
        if (savedInstanceState != null) {
        	long saved_millis = savedInstanceState.getLong(BUNDLE_CALENDAR_EPOCH);
        	current_month_calendar.setTimeInMillis(saved_millis);
        }
        
        updateMonthHeader(current_month_calendar);
		tiny_timeline.setDate(current_month_calendar.getTime());
        week_view.setMonthAndEvents(current_month_calendar, events);
    }
    
    // ========================================================================
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	outState.putLong(BUNDLE_CALENDAR_EPOCH, this.week_view.getCalendar().getTimeInMillis());
    }
    
    // ========================================================================
    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);

    }

    // ========================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_month_view, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_month_view:
        {
        	Intent intent = new Intent(this, MonthActivity.class);
        	intent.setData(getIntent().getData());
        	startActivityForResult(intent, REQUEST_CODE_MONTH_YEAR_SELECTION);
            return true;
        }
        case R.id.menu_all_events:
        {
        	Intent intent = new Intent(this, AllEventsListActivity.class);
        	intent.setData(getIntent().getData());
        	
        	if (getIntent().hasExtra(CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.COLUMN_EVENT_CALENDAR_ID))
        		intent.putExtra(
        				CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.COLUMN_EVENT_CALENDAR_ID,
        				getIntent().getLongExtra(CalendarPickerConstants.CalendarEventPicker.ContentProviderColumns.COLUMN_EVENT_CALENDAR_ID, -1));
        	
        	startActivityForResult(intent, REQUEST_CODE_EVENT_SELECTION);
            return true;
        }
        }

        return super.onOptionsItemSelected(item);
    }

    // ========================================================================
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            Log.i(TAG, "==> result " + resultCode + " from subactivity!  Ignoring...");
            return;
        }
        
  	   	switch (requestCode) {
   		case REQUEST_CODE_EVENT_SELECTION:
   		{
   			long id = data.getLongExtra(BaseColumns._ID, INVALID_EVENT_ID);
			long event_epoch = data.getLongExtra(CalendarPickerConstants.CalendarDatePicker.IntentExtras.INTENT_EXTRA_EPOCH, INVALID_DATE);

			Intent i = new Intent();
			i.putExtra(BaseColumns._ID, id);
			
			if (event_epoch != INVALID_DATE) {
				i.putExtra(CalendarPickerConstants.CalendarDatePicker.IntentExtras.INTENT_EXTRA_DATETIME, sdf.format(new Date(event_epoch)));
				i.putExtra(CalendarPickerConstants.CalendarDatePicker.IntentExtras.INTENT_EXTRA_EPOCH, event_epoch);
			}
			
	        setResult(Activity.RESULT_OK, i);
			finish();

            break;
        }
   		case REQUEST_CODE_MONTH_YEAR_SELECTION:
   		{
   			long id = data.getLongExtra(BaseColumns._ID, INVALID_EVENT_ID);

			Intent i = new Intent();
			i.putExtra(BaseColumns._ID, id);
	        setResult(Activity.RESULT_OK, i);
			finish();
            break;
        }
  	   	}
    }
}
