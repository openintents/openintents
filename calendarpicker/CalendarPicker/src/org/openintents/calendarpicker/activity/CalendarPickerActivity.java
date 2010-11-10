package org.openintents.calendarpicker.activity;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.container.CalendarDay;
import org.openintents.calendarpicker.container.SimpleEvent;
import org.openintents.calendarpicker.contract.IntentConstants;
import org.openintents.calendarpicker.contract.IntentConstants.CalendarEventPicker;
import org.openintents.calendarpicker.view.ScrollableMonthView;
import org.openintents.calendarpicker.view.ScrollableMonthView.MonthUpdateCallback;
import org.openintents.calendarpicker.view.ScrollableMonthView.OnDayClickListener;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
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
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class CalendarPickerActivity extends Activity {


    final static public String TAG = "CalendarPickerActivity";

	static final int REQUEST_CODE_EVENT_SELECTION = 1;
	static final int REQUEST_CODE_MONTH_YEAR_SELECTION = 2;


    public static final long INVALID_EVENT_ID = -1;
    public static final long INVALID_DATE = 0;
    
    List<SimpleEvent> events = new ArrayList<SimpleEvent>();
    

	final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	TextView month_title;
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
        setContentView(R.layout.months_main);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.titlebar_icon);

        this.month_title = (TextView) findViewById(R.id.month_title);
        Calendar current_month_calendar = new GregorianCalendar();
        updateMonthHeader(current_month_calendar);

        Uri intent_data = getIntent().getData();
        // Zip the events
        if (intent_data != null) {
        	// We have been passed a cursor to the data via a content provider.
        	
        	Log.d(TAG, "Querying content provider for: " + intent_data);

        	String key_event_title = IntentConstants.CalendarEventPicker.COLUMN_EVENT_TITLE;
 			Cursor cursor = managedQuery(intent_data,
 					new String[] {BaseColumns._ID, IntentConstants.CalendarEventPicker.COLUMN_EVENT_TIMESTAMP, CalendarEventPicker.COLUMN_EVENT_TITLE},
 					null, null, null);

 			this.events = getEventsFromCursor(cursor);
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
        
		ScrollableMonthView month_layout = (ScrollableMonthView) findViewById(R.id.full_month);
        month_layout.setMonthAndEvents(current_month_calendar, events);
        
        month_layout.setMonthUpdateCallback(new MonthUpdateCallback() {
        	@Override
			public void updateMonth(Calendar cal) {
				updateMonthHeader(cal);
			}
        });
        
        month_layout.setOnDayClickListener(new OnDayClickListener() {

			@Override
			public void clickDay(CalendarDay cd) {

				Uri data = getIntent().getData();
				if (data != null && cd.day_events.size() > 0) {

					Intent i = new Intent(CalendarPickerActivity.this, EventListActivity.class);

					i.setData(data);
					i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_DATETIME, cd.date.getTime());
					startActivityForResult(i, REQUEST_CODE_EVENT_SELECTION);

				} else {
					// If there are no events, just return the day.
					Intent i = new Intent();
					i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_DATETIME, sdf.format(cd.date));
					
					setResult(Activity.RESULT_OK, i);
					finish();
				}
			}
        });
    }

    // ========================================================================
    List<SimpleEvent> getEventsFromCursor(Cursor cursor) {

        List<SimpleEvent> events = new ArrayList<SimpleEvent>();
    	
			if (cursor.moveToFirst()) {

	 			int id_column = cursor.getColumnIndex(BaseColumns._ID);
	 			int timestamp_column = cursor.getColumnIndex(IntentConstants.CalendarEventPicker.COLUMN_EVENT_TIMESTAMP);
	 			
				
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
			
			return events;
    }

    

    // ========================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_main, menu);

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
        case R.id.menu_year_view:
        {
        	startActivityForResult(new Intent(this, YearsActivity.class), REQUEST_CODE_MONTH_YEAR_SELECTION);
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
   			long id = data.getLongExtra(IntentConstants.INTENT_EXTRA_CALENDAR_EVENT_ID, INVALID_EVENT_ID);

			Intent i = new Intent();
			i.putExtra(IntentConstants.INTENT_EXTRA_CALENDAR_EVENT_ID, id);
	        setResult(Activity.RESULT_OK, i);
			finish();

			Toast.makeText(this, TAG + " finshing w/result: " + id, Toast.LENGTH_LONG).show();
            break;
        }
   		case REQUEST_CODE_MONTH_YEAR_SELECTION:
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
