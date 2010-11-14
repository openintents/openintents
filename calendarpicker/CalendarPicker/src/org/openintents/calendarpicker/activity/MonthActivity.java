package org.openintents.calendarpicker.activity;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.container.SimpleEvent;
import org.openintents.calendarpicker.contract.IntentConstants;
import org.openintents.calendarpicker.contract.IntentConstants.CalendarEventPicker;
import org.openintents.calendarpicker.view.FlingableMonthView;
import org.openintents.calendarpicker.view.OnDateUpdateListener;
import org.openintents.calendarpicker.view.TimelineViewHorizontal;
import org.openintents.calendarpicker.view.FlingableMonthView.MonthContextMenuInfo;
import org.openintents.calendarpicker.view.FlingableMonthView.MonthUpdateCallback;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

public class MonthActivity extends Activity {

    final static public String TAG = "MonthActivity";

	static final int REQUEST_CODE_EVENT_SELECTION = 1;
	static final int REQUEST_CODE_MONTH_YEAR_SELECTION = 2;


    final static String BUNDLE_CALENDAR_EPOCH = "BUNDLE_CALENDAR_EPOCH";
    
	
    public static final long INVALID_EVENT_ID = -1;
    public static final long INVALID_DATE = 0;
    
    List<SimpleEvent> events = new ArrayList<SimpleEvent>();
    

	final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	TextView month_title;
	FlingableMonthView month_view;
	TimelineViewHorizontal tiny_timeline;
    LinearLayout weekday_header_layout;
	
    Toast timeline_date_toast;
    final static SimpleDateFormat toast_date_formatter = new SimpleDateFormat("MMMM d, yyyy");
    
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
        setContentView(R.layout.months_view);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.titlebar_icon);

        this.month_title = (TextView) findViewById(R.id.month_title);


        Uri intent_data = getIntent().getData();
        // Zip the events
        if (intent_data != null) {
        	// We have been passed a cursor to the data via a content provider.
			this.events = getEventsFromUri(intent_data);
        } else {
        	Log.d(TAG, "No URI was passed, checking for Intent extras instead...");
			this.events = getEventsFromIntent(this.getIntent());
		}
        
        DateFormatSymbols dfs = new DateFormatSymbols();
        String weekdays[] = dfs.getShortWeekdays();
        LayoutParams lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        this.weekday_header_layout = (LinearLayout) findViewById(R.id.weekdays_header);
		for (int i=Calendar.getInstance().getFirstDayOfWeek(); i<weekdays.length; i++) {
	        TextView tv = new TextView(this);
	        tv.setGravity(Gravity.CENTER);
	        tv.setText(weekdays[i]);
	        this.weekday_header_layout.addView(tv, lp);
		}
        
		this.timeline_date_toast = Toast.makeText(this, "Date", Toast.LENGTH_SHORT);
		this.tiny_timeline = (TimelineViewHorizontal) findViewById(R.id.tiny_timeline);
		this.tiny_timeline.setOnDateUpdateListener(new OnDateUpdateListener() {
			@Override
			public void updateDate(Date date) {
				timeline_date_toast.setText(toast_date_formatter.format(date));
				timeline_date_toast.show();
			}
		});
		this.tiny_timeline.setOnDateSelectionListener(new OnDateUpdateListener() {
			@Override
			public void updateDate(Date date) {
				month_view.setMonthAndHighlight(date);
			}
		});
		
		this.month_view = (FlingableMonthView) findViewById(R.id.full_month);
		this.month_view.setMonthUpdateCallback(new MonthUpdateCallback() {
        	@Override
			public void updateMonth(Calendar cal) {
				updateMonthHeader(cal);
				tiny_timeline.setDate(cal.getTime());
			}
        });
        
		this.month_view.setOnDayTouchListener(new OnDateUpdateListener() {

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
        

		this.month_view.setOnScrollListener(new OnDateUpdateListener() {
			@Override
			public void updateDate(Date date) {
				tiny_timeline.setDate(date);
			}
        });
		this.month_view.setOnDayClickListener(new OnDateUpdateListener() {

			@Override
			public void updateDate(Date date) {

				updateWeekHeaderBar(date);
				
				Uri data = getIntent().getData();
				if (data != null) {
					launchDayEvents(data, date);
				} else {
					finishWithDate(date);
				}
			}
        });
        

        registerForContextMenu(this.month_view);

        
        Calendar current_month_calendar = new GregorianCalendar();
        
        if (savedInstanceState != null) {
        	long saved_millis = savedInstanceState.getLong(BUNDLE_CALENDAR_EPOCH);
        	current_month_calendar.setTimeInMillis(saved_millis);
        }
        
        updateMonthHeader(current_month_calendar);
        this.tiny_timeline.setDate(current_month_calendar.getTime());
        this.month_view.setMonthAndEvents(current_month_calendar, this.events);
    }

    // ========================================================================
    @Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

    	// If the key is not the DPAD, then let it be handled by default.
    	switch (keyCode) {
    	case KeyEvent.KEYCODE_DPAD_UP:
    	case KeyEvent.KEYCODE_DPAD_LEFT:
    	case KeyEvent.KEYCODE_DPAD_DOWN:
    	case KeyEvent.KEYCODE_DPAD_RIGHT:
    	case KeyEvent.KEYCODE_DPAD_CENTER:
    		break;
    	default:
    		return super.onKeyDown(keyCode, event);
    	}
    	

    	Date highlighted_day = month_view.getHighlightedDay();
		if (highlighted_day == null) {
			month_view.highlightDay(month_view.getCalendar().getTime());
			return true;
		}
		
		Calendar cal = new GregorianCalendar();
		cal.setTime(highlighted_day);
						
    	switch (keyCode) {
    	case KeyEvent.KEYCODE_DPAD_UP:
    	{
    		cal.add(Calendar.DATE, -FlingableMonthView.DAYS_PER_WEEK);
    		month_view.setMonthAndHighlight(cal.getTime());
    		break;
    	}
    	case KeyEvent.KEYCODE_DPAD_LEFT:
    	{
    		cal.add(Calendar.DATE, -1);
    		month_view.setMonthAndHighlight(cal.getTime());
    		break;
    	}
    	case KeyEvent.KEYCODE_DPAD_DOWN:
    	{
    		cal.add(Calendar.DATE, FlingableMonthView.DAYS_PER_WEEK);
    		month_view.setMonthAndHighlight(cal.getTime());
    		break;
    	}
    	case KeyEvent.KEYCODE_DPAD_RIGHT:
    	{
    		cal.add(Calendar.DATE, 1);
    		month_view.setMonthAndHighlight(cal.getTime());
    		break;
    	}
    	case KeyEvent.KEYCODE_DPAD_CENTER:
    	{
    		if (highlighted_day != null) {
    			month_view.executeDay(highlighted_day);
    		}
    		break;
    	}
    	default:
    		return false;
    	}
        return true;
    }
    
    // ========================================================================
    void finishWithDate(Date date) {
		// If there are no events, just return the day.
		Intent i = new Intent();
		
		if (date != null) {
			i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_EPOCH, date.getTime());
			i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_DATETIME, sdf.format(date));
		}
		
		setResult(Activity.RESULT_OK, i);
		finish();
    }
    
    // ========================================================================
    void updateWeekHeaderBar(Date date) {
		// Highlight the day of the week in the header bar
		Calendar c = new GregorianCalendar();
		c.setTime(date);
		int child_idx = c.get(Calendar.DAY_OF_WEEK) - c.getMinimum(Calendar.DAY_OF_WEEK);
		for (int i=0; i<weekday_header_layout.getChildCount(); i++) {
			TextView tv = (TextView) weekday_header_layout.getChildAt(child_idx);
			tv.setTextColor(i == child_idx ? Color.RED : getResources().getColor(android.R.color.primary_text_dark));
		}
    }

    // ========================================================================
    void launchDayEvents(Uri data, Date date) {
		Intent i = new Intent(MonthActivity.this, DayEventsListActivity.class);

		i.setData(data);
		if (date != null) {
			i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_EPOCH, date.getTime());
			i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_DATETIME, sdf.format(date));
		}
		startActivityForResult(i, REQUEST_CODE_EVENT_SELECTION);
    }
    
    // ========================================================================
    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	
    	outState.putLong(BUNDLE_CALENDAR_EPOCH, this.month_view.getCalendar().getTimeInMillis());
    }
    
    // ========================================================================
    @Override
    protected void onRestoreInstanceState (Bundle savedInstanceState) {
    	super.onRestoreInstanceState(savedInstanceState);

    }

    // ========================================================================
	/** We have been passed the data directly. */
    List<SimpleEvent> getEventsFromIntent(Intent intent) {
    	
    	List<SimpleEvent> events = new ArrayList<SimpleEvent>();

     	Log.d(TAG, "We have been passed the data directly.");

    	long[] event_ids = getIntent().getLongArrayExtra(IntentConstants.CalendarDatePicker.EXTRA_EVENT_IDS);
    	long[] event_timestamps = getIntent().getLongArrayExtra(IntentConstants.CalendarDatePicker.EXTRA_EVENT_TIMESTAMPS);

    	Collections.sort(events);
    	if (event_ids != null && event_timestamps != null) {
	    	for (int i=0; i<event_timestamps.length; i++)
	    		events.add( new SimpleEvent(event_ids[i], event_timestamps[i]) );

		    Log.d(TAG, "Added " + event_timestamps.length + " timestamps.");
    	}

    	return events;
    }

    // ========================================================================
    List<SimpleEvent> getEventsFromUri(Uri uri) {

    	List<SimpleEvent> events = new ArrayList<SimpleEvent>();

    	Log.d(TAG, "Querying content provider for: " + uri);
    	String selection = null;
    	if (getIntent().hasExtra(IntentConstants.CalendarEventPicker.COLUMN_EVENT_CALENDAR_ID)) {
        	long cal_id = getIntent().getLongExtra(IntentConstants.CalendarEventPicker.COLUMN_EVENT_CALENDAR_ID, -1);
    		selection = IntentConstants.CalendarEventPicker.COLUMN_EVENT_CALENDAR_ID + "=" + cal_id;
    	}

    	Cursor cursor = managedQuery(uri,
			new String[] {BaseColumns._ID, IntentConstants.CalendarEventPicker.COLUMN_EVENT_TIMESTAMP, CalendarEventPicker.COLUMN_EVENT_TITLE},
			selection,
			null,
			IntentConstants.CalendarEventPicker.COLUMN_EVENT_TIMESTAMP + " ASC");
    	
    	if (cursor != null && cursor.moveToFirst()) {

    		int id_column = cursor.getColumnIndex(BaseColumns._ID);
    		int timestamp_column = cursor.getColumnIndex(IntentConstants.CalendarEventPicker.COLUMN_EVENT_TIMESTAMP);

    		do {
    			long timestamp = cursor.getLong(timestamp_column);
    			Log.d(TAG, "Adding event with timestamp: " + timestamp);
    			Log.d(TAG, "Timestamp date is: " + new Date(timestamp));

    			events.add(
    					new SimpleEvent(
    							cursor.getLong(id_column),
    							timestamp) );

    		} while (cursor.moveToNext());
    	} else {
    		Log.e(TAG, "There were no rows in this Cursor: " + cursor);
    	}

    	return events;
    }

    // ========================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_month_view, menu);

        return true;
    }

    // ========================================================================
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_all_events).setVisible(getIntent().getData() != null);
        
        return true;
    }

    // ========================================================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_year_view:
        {
        	startActivityForResult(new Intent(this, YearsActivity.class), REQUEST_CODE_MONTH_YEAR_SELECTION);
            return true;
        }
        case R.id.menu_week_view:
        {
        	Intent intent = new Intent(this, WeekActivity.class);
        	intent.setData(getIntent().getData());
        	startActivityForResult(intent, REQUEST_CODE_MONTH_YEAR_SELECTION);
            return true;
        }
        case R.id.menu_all_events:
        {
        	Intent intent = new Intent(this, AllEventsListActivity.class);
        	intent.setData(getIntent().getData());
        	
        	if (getIntent().hasExtra(IntentConstants.CalendarEventPicker.COLUMN_EVENT_CALENDAR_ID))
        		intent.putExtra(
        				IntentConstants.CalendarEventPicker.COLUMN_EVENT_CALENDAR_ID,
        				getIntent().getLongExtra(IntentConstants.CalendarEventPicker.COLUMN_EVENT_CALENDAR_ID, -1));
        	
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
			long event_epoch = data.getLongExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_EPOCH, INVALID_DATE);

			Intent i = new Intent();
			i.putExtra(BaseColumns._ID, id);
			
			if (event_epoch != INVALID_DATE) {
				i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_DATETIME, sdf.format(new Date(event_epoch)));
				i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_EPOCH, event_epoch);
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
    
    

    // ========================================================================
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_month_view, menu);
        
        menu.setHeaderIcon(android.R.drawable.ic_dialog_alert);
        menu.setHeaderTitle("Day action:");
	}

    
    // ========================================================================
    @Override
	public boolean onContextItemSelected(MenuItem item) {

    	MonthContextMenuInfo info = (MonthContextMenuInfo) item.getMenuInfo();
		
		switch (item.getItemId()) {
		case R.id.menu_show_day_events:
		{
			launchDayEvents(getIntent().getData(), info.getDate());
	    	break;
		}
		default:
			break;
		}
		return super.onContextItemSelected(item);
	}

}
