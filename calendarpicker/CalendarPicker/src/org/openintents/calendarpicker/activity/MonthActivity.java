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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.activity.prefs.CalendarDisplayPreferences;
import org.openintents.calendarpicker.container.SimpleEvent;
import org.openintents.calendarpicker.contract.CalendarPickerConstants;
import org.openintents.calendarpicker.provider.CachedEventContentProvider;
import org.openintents.calendarpicker.provider.CachedEventDatabase;
import org.openintents.calendarpicker.view.FlingableMonthView;
import org.openintents.calendarpicker.view.OnDateUpdateListener;
import org.openintents.calendarpicker.view.TimelineViewHorizontal;
import org.openintents.calendarpicker.view.FlingableMonthView.MonthContextMenuInfo;
import org.openintents.calendarpicker.view.FlingableMonthView.MonthUpdateCallback;
import org.openintents.distribution.AboutDialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
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

public class MonthActivity extends PeriodBrowsingActivity {

    final static public String TAG = "MonthActivity";

	static final int REQUEST_CODE_EVENT_SELECTION = 1;
	static final int REQUEST_CODE_MONTH_YEAR_SELECTION = 2;


    final static String BUNDLE_CALENDAR_EPOCH = "BUNDLE_CALENDAR_EPOCH";
    
	
    public static final long INVALID_EVENT_ID = -1;
    public static final long INVALID_DATE = 0;
    
    public static final int DIALOG_ABOUT = 1;


	TextView month_title;
	FlingableMonthView month_view;
	TimelineViewHorizontal tiny_timeline;
    LinearLayout weekday_header_layout;
	
    Toast timeline_date_toast;
    final static SimpleDateFormat YMD_FORMATTER = new SimpleDateFormat("MMMM d, yyyy");
    final static SimpleDateFormat FULL_MONTH_AND_YEAR_FORMATTER = new SimpleDateFormat("MMMM yyyy");
    
    // ========================================================================
	void updateMonthHeader(Calendar calendar) {
        month_title.setText( FULL_MONTH_AND_YEAR_FORMATTER.format(calendar.getTime()) );
	}
	
    // ========================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.months_view);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.titlebar_icon);

        this.month_title = (TextView) findViewById(R.id.month_title);

        DailyEventMaximums maximums = new DailyEventMaximums();
        List<SimpleEvent> events;
        Uri intent_data = getIntent().getData();
        // Zip the events
        if (intent_data != null) {
        	// We have been passed a cursor to the data via a content provider.
			events = getEventsFromUri(intent_data, getIntent(), maximums);
        } else if (getIntent().hasExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_EVENT_IDS)
        		&& getIntent().hasExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_EVENT_TIMESTAMPS)) {
        	Log.d(TAG, "No URI was passed, checking for Intent extras instead...");
			events = getEventsFromIntent(getIntent());
			CachedEventDatabase database = new CachedEventDatabase(this);
			long calendar_id = database.populateEvents(events);
			getIntent().setData(CachedEventContentProvider.constructUri(calendar_id));
			
			Log.d(TAG, "Sartup, set data URI: " + getIntent().getData());
			
		} else {
			Log.e(TAG, "No data provided!");
			events = new ArrayList<SimpleEvent>();
		}
        

        LayoutParams lp = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1);
        this.weekday_header_layout = (LinearLayout) findViewById(R.id.weekdays_header);
        Calendar weekdays_cal = Calendar.getInstance();
        weekdays_cal.set(Calendar.DAY_OF_WEEK, weekdays_cal.getFirstDayOfWeek());
		for (int i=0; i<FlingableMonthView.DAYS_PER_WEEK; i++) {
	        TextView tv = new TextView(this);
	        tv.setGravity(Gravity.CENTER);
	        tv.setText(DAY_OF_WEEK_FORMATTER.format(weekdays_cal.getTime()));
	        this.weekday_header_layout.addView(tv, lp);
	        weekdays_cal.roll(Calendar.DAY_OF_WEEK, 1);
		}
        
		this.timeline_date_toast = Toast.makeText(this, "Date", Toast.LENGTH_SHORT);
		this.tiny_timeline = (TimelineViewHorizontal) findViewById(R.id.tiny_timeline);
		this.tiny_timeline.setOnDateUpdateListener(new OnDateUpdateListener() {
			@Override
			public void updateDate(Date date) {
				updateTransientDate(date);
			}
		});
		this.tiny_timeline.setOnDateSelectionListener(new OnDateUpdateListener() {
			@Override
			public void updateDate(Date date) {
				month_view.setMonthAndHighlight(date);
			}
		});
		
		this.month_view = (FlingableMonthView) findViewById(R.id.full_month);
		
		this.month_view.calendar_drawing.enable_event_count = getIntent().getBooleanExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_SHOW_EVENT_COUNT, true);
		this.month_view.setVisualizeQuantities(getIntent().getBooleanExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_VISUALIZE_QUANTITIES, false));
		this.month_view.calendar_drawing.color_mapping.setMaximums(maximums);
		
		if (getIntent().hasExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_BACKGROUND_COLORMAP_QUANTITY_INDEX)) {
			int extra_quantity_index = getIntent().getIntExtra(CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_BACKGROUND_COLORMAP_QUANTITY_INDEX, -1);
			this.month_view.calendar_drawing.color_mapping.setColormapSource(extra_quantity_index);
		}
		
		
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
		this.month_view.setTransientListener(new OnDateUpdateListener() {
			@Override
			public void updateDate(Date date) {
				updateTransientDate(date);
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
					if (Intent.ACTION_PICK.equals(getIntent().getAction()))
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
        this.month_view.setMonthAndEvents(current_month_calendar, events);
    }

    // ========================================================================
    void setEventCountVisibility(boolean visible) {

    	this.month_view.calendar_drawing.enable_event_count = visible;
    	this.month_view.invalidate();

    	if (this.options_menu != null) {
	        this.options_menu.findItem(R.id.menu_toggle_event_counts).setIcon(
	        		visible ? R.drawable.ic_menu_checked : R.drawable.ic_menu_unchecked);
    	}
    }
    
    // ========================================================================
    void updateTransientDate(Date date) {
		timeline_date_toast.setText(FULL_MONTH_AND_YEAR_FORMATTER.format(date));
		timeline_date_toast.show();
    }

    // ========================================================================
    void launchDayEvents(Uri data, Date date) {
		Intent i = new Intent(MonthActivity.this, DayEventsListActivity.class);
		i.setAction(getIntent().getAction());
		i.setData(data);
		if (date != null) {
			i.putExtra(CalendarPickerConstants.CalendarDatePicker.IntentExtras.INTENT_EXTRA_EPOCH, date.getTime());
			i.putExtra(CalendarPickerConstants.CalendarDatePicker.IntentExtras.INTENT_EXTRA_DATETIME, HYPEHENATED_ISO_DATE_FORMATTER.format(date));
		}
		startActivityForResult(i, REQUEST_CODE_EVENT_SELECTION);
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
    Menu options_menu = null;
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        this.options_menu = menu;
        
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_month_view, menu);

		setEventCountVisibility(this.month_view.calendar_drawing.enable_event_count);

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
        case R.id.menu_toggle_event_counts:
        {
        	setEventCountVisibility(!this.month_view.calendar_drawing.enable_event_count);
            return true;
        }
        case R.id.menu_settings:
        {
        	startActivity(new Intent(this, CalendarDisplayPreferences.class));
            return true;
        }
        case R.id.menu_about:
        {
        	showAboutBox();
            return true;
        }
        case R.id.menu_week_view:
        {
        	Intent intent = new Intent(this, WeekActivity.class);
        	intent.setData(getIntent().getData());
        	intent.setAction(getIntent().getAction());
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

        	intent.setAction(getIntent().getAction());
        	startActivityForResult(intent, REQUEST_CODE_EVENT_SELECTION);
            return true;
        }
        }

        return super.onOptionsItemSelected(item);
    }

    // ========================================================================
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_month_view, menu);
        
        menu.setHeaderIcon(android.R.drawable.ic_dialog_alert);
        menu.setHeaderTitle("Day action:");
        
        menu.findItem(R.id.menu_show_day_events).setVisible(getIntent().getData() != null);
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
				i.putExtra(CalendarPickerConstants.CalendarDatePicker.IntentExtras.INTENT_EXTRA_DATETIME, HYPEHENATED_ISO_DATE_FORMATTER.format(new Date(event_epoch)));
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

    // ========================================================================
	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_ABOUT:
			return new AboutDialog(this);
		}
		return null;
	}

    // ========================================================================
	private void showAboutBox() {
		AboutDialog.showDialogOrStartActivity(this, DIALOG_ABOUT);
	}
}
