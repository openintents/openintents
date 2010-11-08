package org.openintents.calendarpicker.activity;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.openintents.calendarpicker.IntentConstants;
import org.openintents.calendarpicker.MiniMonthDrawable;
import org.openintents.calendarpicker.IntentConstants.CalendarEvent;
import org.openintents.calendarpicker.container.CalendarDay;
import org.openintents.calendarpicker.container.SimpleEvent;
import org.openintents.calendarpicker.view.DayView;
import org.openintents.calendarpicker.view.MonthLayout;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import org.openintents.calendarpicker.R;

public class CalendarPickerActivity extends Activity {


    final static public String TAG = "CalendarPickerActivity";

	static final int REQUEST_CODE_EVENT_SELECTION = 1;


    ImageView mini_calendar_prev, mini_calendar_curr, mini_calendar_next;
    
    
    public static final long INVALID_EVENT_ID = -1;
    public static final long INVALID_DATE = 0;
    
    List<SimpleEvent> events = new ArrayList<SimpleEvent>();
    

	final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    
    // ========================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.calendar_activity);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.titlebar_icon);

        TextView month_title = (TextView) findViewById(R.id.month_title);
        SimpleDateFormat formatter = new SimpleDateFormat("MMMM yyyy");

        Calendar current_month_calendar = new GregorianCalendar();
        month_title.setText( formatter.format(current_month_calendar.getTime()) );
        

        Uri intent_data = getIntent().getData();
        // Zip the events
        if (intent_data != null) {
        	// We have been passed a cursor to the data via a content provider.
        	
        	Log.d(TAG, "Querying content provider for: " + intent_data);

        	String key_event_title = IntentConstants.CalendarEvent.COLUMN_EVENT_TITLE;
 			Cursor cursor = managedQuery(intent_data,
 					new String[] {BaseColumns._ID, IntentConstants.CalendarEvent.COLUMN_EVENT_TIMESTAMP, CalendarEvent.COLUMN_EVENT_TITLE},
 					null, null, null);

 			int id_column = cursor.getColumnIndex(BaseColumns._ID);
 			int timestamp_column = cursor.getColumnIndex(IntentConstants.CalendarEvent.COLUMN_EVENT_TIMESTAMP);
 			
 			if (cursor.moveToFirst()) {
	 			do {
	 				long timestamp = cursor.getLong(timestamp_column)*1000;
	 				Log.d(TAG, "Adding event with timestamp: " + timestamp);
	 				Log.d(TAG, "Timestamp date is: " + new Date(timestamp));
	
		        	events.add(
		        		new SimpleEvent(
		        			cursor.getLong(id_column),
		        			timestamp) );
		        	
	 			} while (cursor.moveToNext());
 			}
        }

        DateFormatSymbols dfs = new DateFormatSymbols();
//        String weekdays[] = dfs.getWeekdays();
        String weekdays[] = dfs.getShortWeekdays();
        LayoutParams lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        LinearLayout weekday_header_layout = (LinearLayout) findViewById(R.id.weekdays_header);
		for (int i=Calendar.getInstance().getFirstDayOfWeek(); i<weekdays.length; i++) {
	        TextView tv = new TextView(this);
	        tv.setGravity(Gravity.CENTER);
	        tv.setText(weekdays[i]);
	        weekday_header_layout.addView(tv, lp);
		}
        
        
        
        MonthLayout month_layout = (MonthLayout) findViewById(R.id.full_month);
        month_layout.setMonthAndEvents(current_month_calendar, events);

        month_layout.setOnDayClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
//				startActivity(new Intent());
				
				DayView dv = (DayView) v;
				CalendarDay cd = dv.getCalendarDay();

				Log.d(TAG, "Clicked on a day: " + dv.getCalendarDay().date);

				Uri data = getIntent().getData();
				if (data != null && cd.day_events.size() > 0) {

					Intent i = new Intent(CalendarPickerActivity.this, EventListActivity.class);

					i.setData(data);
					i.putExtra(IntentConstants.CalendarPicker.INTENT_EXTRA_DATETIME, dv.getCalendarDay().date.getTime());
					startActivityForResult(i, REQUEST_CODE_EVENT_SELECTION);

				} else {
					// If there are no events, just return the day.
					Intent i = new Intent();
					i.putExtra(IntentConstants.CalendarPicker.INTENT_EXTRA_DATETIME, sdf.format(dv.getCalendarDay().date));
					
					setResult(Activity.RESULT_OK, i);
					finish();
				}
			}
        });
        
        
        this.mini_calendar_prev = (ImageView) findViewById(R.id.mini_calendar_prev);
        this.mini_calendar_curr = (ImageView) findViewById(R.id.mini_calendar_curr);
        this.mini_calendar_next = (ImageView) findViewById(R.id.mini_calendar_next);

        

		GregorianCalendar cal_prev = new GregorianCalendar();
		cal_prev.add(GregorianCalendar.MONTH, -1);
		
		GregorianCalendar cal_curr = new GregorianCalendar();
		
		GregorianCalendar cal_next = new GregorianCalendar();
		cal_next.add(GregorianCalendar.MONTH, 1);
		
//		Log.d(TAG, "Previous month...");
        mini_calendar_prev.setImageDrawable(new MiniMonthDrawable(this, mini_calendar_prev, cal_prev));
        
//        Log.d(TAG, "Current month...");
        mini_calendar_curr.setImageDrawable(new MiniMonthDrawable(this, mini_calendar_curr, cal_curr));
        
//        Log.d(TAG, "Next month...");
        mini_calendar_next.setImageDrawable(new MiniMonthDrawable(this, mini_calendar_next, cal_next));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != RESULT_OK) {
            Log.i(TAG, "==> result " + resultCode + " from subactivity!  Ignoring...");
            return;
        }
        
  	   	switch (requestCode) {
   		case REQUEST_CODE_EVENT_SELECTION:
   		{
   			long id = data.getLongExtra(IntentConstants.INTENT_EXTRA_CALENDAR_EVENT_ID, INVALID_EVENT_ID);

			Intent i = new Intent();
			i.putExtra(IntentConstants.INTENT_EXTRA_CALENDAR_EVENT_ID, id);
	        setResult(Activity.RESULT_OK, i);
			finish();

			Toast.makeText(this, TAG + " finshing w/result: " + id, Toast.LENGTH_LONG).show();
            break;
        }
  	   	}
    }
}
