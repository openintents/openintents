// NOTE: Found in modified form at this URL:
// http://staticfree.info/clip/2009-10-20T132442

package org.openintents.calendarpicker.view;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.openintents.calendarpicker.container.CalendarDay;
import org.openintents.calendarpicker.container.SimpleEvent;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;


/**
 * ViewGroup that arranges child views in a similar way to text, with them laid
 * out one line at a time and "wrapping" to the next line as needed.
 */
public class MonthLayout extends ViewGroup {

	static final String TAG = "MonthLayout";
	
	
	final int DAYS_PER_WEEK = 7;

	// ========================================================================
	public static class DataSeriesAttributes {
		public String title;
		public int color;
	}
	
    // ========================================================================
	public static int[] DEFAULT_COLORS = {
		Color.CYAN, Color.GREEN, Color.MAGENTA, Color.YELLOW, Color.BLUE, Color.RED };
    
    // ========================================================================
    public static class LayoutParams extends ViewGroup.LayoutParams {
        public final int horizontal_spacing;
        public final int vertical_spacing;

        /**
         * @param horizontal_spacing Pixels between items, horizontally
         * @param vertical_spacing Pixels between items, vertically
         */
        public LayoutParams(int horizontal_spacing, int vertical_spacing) {
            super(0, 0);
            this.horizontal_spacing = horizontal_spacing;
            this.vertical_spacing = vertical_spacing;
        }
    }
    
    // ========================================================================

    LayoutParams reflow_layout_params;
    Context context;
    Calendar month_calendar;
    // ========================================================================
    public MonthLayout(Context context, Calendar month) {
        super(context);
        this.context = context;
    	setMonth(month);
    }
    
    // ========================================================================
    public MonthLayout(Context context, AttributeSet attrs) {
    	super(context, attrs);
    	
        this.context = context;
    	reflow_layout_params = (LayoutParams) generateDefaultLayoutParams();
    	setMonth(new GregorianCalendar());
    }
    
    public void setOnDayClickListener(OnClickListener day_click_listener) {

        final int child_count = getChildCount();
        for (int i = 0; i < child_count; i++) {
            final View child = getChildAt(i);
            child.setOnClickListener(day_click_listener);
        }
    }

    // ========================================================================
    public void setMonth(Calendar calendar) {
    	this.month_calendar = calendar;

    	this.removeAllViews();
    	init(context, new ArrayList<SimpleEvent>());
    	this.requestLayout();
    }
    
    // ========================================================================
    public void setMonthAndEvents(Calendar calendar, List<SimpleEvent> events) {
    	this.month_calendar = calendar;

    	Log.d(TAG, "We must process " + events.size() + " events.");
    	
    	this.removeAllViews();
    	init(context, events);
    	this.requestLayout();
    }
    
    // ========================================================================
    void init(Context context, List<SimpleEvent> events) {
    	this.context = context;

    	if (this.month_calendar != null)
    		generateChildren(this.month_calendar, events);
    }

    // ========================================================================
    void generateChildren(Calendar month_cal, List<SimpleEvent> events) {

    	List<CalendarDay> day_list = new ArrayList<CalendarDay>();

    	int month_index = month_cal.get(Calendar.MONTH);
    	
    	Calendar working_calendar = new GregorianCalendar();
    	working_calendar.clear();
    	
    	Log.d(TAG, "Minimum day of month: " + working_calendar.getMinimum(Calendar.DAY_OF_MONTH));
    	// Set working calendar to first day of the month.
    	working_calendar.set(month_cal.get(Calendar.YEAR), month_index, working_calendar.getMinimum(Calendar.DAY_OF_MONTH));
    	

    	// Get index of following month
    	Calendar dupe2 = (Calendar) working_calendar.clone();
    	dupe2.add(Calendar.MONTH, 1);
    	int next_month_index = dupe2.get(Calendar.MONTH);
    	
    	
    	// Roll the date back to the beginning of the week
    	while (true) {
    		if (working_calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)
    			break;
    		
    		working_calendar.add(Calendar.DAY_OF_MONTH, -1);
    	}
    	
    	// Get trailing days from previous month and leading days from following month
    	while (true) {
    		day_list.add(new CalendarDay(working_calendar.getTime()));
    		working_calendar.add(Calendar.DAY_OF_MONTH, 1);
    		
    		int current_month = working_calendar.get(Calendar.MONTH);
    		if (working_calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && current_month == next_month_index)
    			break;
    	}
    	
    	assignDayEvents(working_calendar.getTime(), day_list, events);

		populateCalendarView(this.context, month_cal, day_list);
    }

    // ========================================================================
    void assignDayEvents(Date cutoff, List<CalendarDay> day_list, List<SimpleEvent> events) {
		if (events.size() == 0) return;
		
		Log.e(TAG, "About to process the events...");

    	// Populate each calendar day with the right events
		List<SimpleEvent> reversed_events = new ArrayList<SimpleEvent>(events);
        Collections.sort(reversed_events);
        Collections.reverse(reversed_events);
        
		List<CalendarDay> reversed_days = new ArrayList<CalendarDay>(day_list);
        Collections.sort(reversed_days);
        Collections.reverse(reversed_days);        
        
        int i=0; 	// Event index
        
        // Advance the event index (backwards) until
        // we encounter an event within the final calendar day
        // Note: Our "working calendar" is set to the beginning of the
        // day after the last in our list.
        while (!cutoff.after(reversed_events.get(i).timestamp)) i++;
    	for (CalendarDay day : reversed_days) {
    		
    		// Consume events until we find an event
    		// that starts "before" the given day
            while (!day.date.after(reversed_events.get(i).timestamp)) {
            	day.day_events.add(reversed_events.get(i));

            	Log.d(TAG, "Added event " + i);
            	
    			i++;
    			if (i >= reversed_events.size())
    				return;
    		}
    	}
    }
    
    // ========================================================================
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(1, 1); // default of 1px spacing
    }

    // ========================================================================
    public void setFlowLayoutParams(LayoutParams lp) {
    	reflow_layout_params = lp;
    }

    // ========================================================================
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return (p instanceof LayoutParams);
    }

    // ========================================================================
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	
        final int child_count = getChildCount();
        if (child_count == 0) return;
        
        
        int usable_width = getWidth() - getPaddingLeft() - getPaddingRight();
        int usable_height = getHeight() - getPaddingTop() - getPaddingBottom();
        
        int inter_day_horizontal_padding = reflow_layout_params.horizontal_spacing;
        float width_per_day = (usable_width - (DAYS_PER_WEEK - 1)*inter_day_horizontal_padding) / DAYS_PER_WEEK;
        
        
        int weeks_per_month = (int) Math.ceil(getChildCount() / (float) DAYS_PER_WEEK);
        
        int inter_day_vertical_padding = reflow_layout_params.vertical_spacing;
        float height_per_day = (usable_height - (weeks_per_month - 1)*inter_day_vertical_padding) / weeks_per_month;
        
        Log.d(TAG, "Spacing: " + inter_day_horizontal_padding + ", " + inter_day_vertical_padding);
        
        
        for (int i = 0; i < child_count; i++) {
            final View child = getChildAt(i);
            
            int left = getPaddingLeft() + (int) ((width_per_day + inter_day_horizontal_padding) * (i % DAYS_PER_WEEK));
            int top = getPaddingTop() + (int) ((height_per_day + inter_day_vertical_padding) * (i / DAYS_PER_WEEK));
                
            child.layout(left, top, left + (int) width_per_day, top + (int) height_per_day);
        }
    }

	// ========================================================================
	public void populateCalendarView(Context context, Calendar calendar, List<CalendarDay> day_list) {
		
		int month = calendar.get(Calendar.MONTH);
		
		MonthLayout.LayoutParams lp = new MonthLayout.LayoutParams(2, 2);
		this.setFlowLayoutParams(lp);

		for (CalendarDay day : day_list) {
			Calendar cal = new GregorianCalendar();
			cal.setTime(day.date);
			boolean dim = month == cal.get(Calendar.MONTH);
			
			DayView b = new DayView(context, null, day, dim);
			b.setPadding(0, 0, 0, 0);
			this.addView(b);
		}
	}
}
