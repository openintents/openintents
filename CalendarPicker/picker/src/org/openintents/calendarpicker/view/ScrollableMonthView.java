package org.openintents.calendarpicker.view;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.container.CalendarDay;
import org.openintents.calendarpicker.container.SimpleEvent;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;

public class ScrollableMonthView extends View {

	static final String TAG = "ScrollableMonthView";
	
	final int DAYS_PER_WEEK = 7;

    boolean is_holding_longpress = false;
    long longpress_start_time;
//    int LONGPRESS_DURATION = 3*ViewConfiguration.getLongPressTimeout();
    int LONGPRESS_DURATION = ViewConfiguration.getLongPressTimeout();
	
    
    float horizontal_spacing = 2;
    float vertical_spacing = 2;

    Context context;
	Paint my_paint;
    Calendar month_calendar;
    
    List<CalendarDay> day_list;
    CalendarDay highlighted_day = null;
    
    MonthUpdateCallback month_update_callback = null;
    OnDayClickListener day_click_callback = null;

    // ========================================================================
    public ScrollableMonthView(Context context, AttributeSet attrs) {
    	super(context, attrs);

        this.context = context;
    	setMonth(new GregorianCalendar());
    	
        my_paint = new Paint();
		my_paint.setAntiAlias(true);
		my_paint.setColor(Color.WHITE);
		my_paint.setTextAlign(Align.CENTER);

        this.setFocusable(true);
        
        final GestureDetector gestureDetector = new GestureDetector(new MonthGestureDetector());
        setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

	        	Log.i(TAG, "Got MONTH touch event...");
				
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
			}
        });
        
        setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {

				if (event.getAction() != KeyEvent.ACTION_DOWN)
					return false;
				
				int idx = day_list.indexOf(highlighted_day);
								
		    	switch (keyCode) {
		    	case KeyEvent.KEYCODE_DPAD_DOWN:
		    	case KeyEvent.KEYCODE_DPAD_LEFT:
		    	{
		    		if (idx < 0) idx = 0;
		    		idx = (idx + day_list.size() - 1) % day_list.size();
		    	    highlighted_day = day_list.get(idx);
		    	    invalidate();
		    		break;
		    	}
		    	case KeyEvent.KEYCODE_DPAD_UP:
		    	case KeyEvent.KEYCODE_DPAD_RIGHT:
		    	{
		    		idx = (idx + 1) % day_list.size();
		    	    highlighted_day = day_list.get(idx);
		    	    invalidate();
		    		break;
		    	}
		    	case KeyEvent.KEYCODE_DPAD_CENTER:
		    		if (idx >= 0) {
		            	executeDay(highlighted_day);
		    		}
		    		break;
		    	default:
		    		return false;
		    	}
		    	return true;
			}
		});
    }
    
    
    // ========================================================================
    public interface MonthUpdateCallback {
    	void updateMonth(Calendar cal);
    }
    
    // ========================================================================
    public interface OnDayClickListener {
    	void clickDay(CalendarDay day);
    }
    
    // ========================================================================
    public void setOnDayClickListener(OnDayClickListener callback) {
    	this.day_click_callback = callback;
    }
    
    // ========================================================================
    public void setMonthUpdateCallback(MonthUpdateCallback callback) {
    	this.month_update_callback = callback;
    }
    
    
    
    
    // ========================================================================
    public void setMonth(Calendar calendar) {
    	this.month_calendar = calendar;
    	init(new ArrayList<SimpleEvent>());
    }
    
    // ========================================================================
    public void setMonthAndEvents(Calendar calendar, List<SimpleEvent> events) {
    	this.month_calendar = calendar;

    	Log.d(TAG, "We must process " + events.size() + " events.");
    	init(events);
    }

    // ========================================================================
    void init() {
    	init(new ArrayList<SimpleEvent>());
    }
    
    // ========================================================================
    void init(List<SimpleEvent> events) {

    	if (this.month_calendar != null) {
    		this.day_list = generateChildren(this.month_calendar, events);
    	} else {
    		this.day_list = new ArrayList<CalendarDay>();
    	}
    }

    // ========================================================================
    List<CalendarDay> generateChildren(Calendar month_cal, List<SimpleEvent> events) {

    	List<CalendarDay> day_list = new ArrayList<CalendarDay>();

    	int month_index = month_cal.get(Calendar.MONTH);
    	
    	Calendar working_calendar = new GregorianCalendar();
    	working_calendar.clear();
    	
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
		
		return day_list;
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
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        visitDayViewports(new ViewportVisitor() {
			@Override
			public void visitViewport(RectF viewport, CalendarDay child) {
	            canvas.save();
	            canvas.translate(viewport.left, viewport.top);
	            drawDay(canvas, viewport, child);
	            canvas.restore();
			}
        });
    }

    // ========================================================================
    interface ViewportVisitor {
    	void visitViewport(RectF viewport, CalendarDay child);
    }

    // ========================================================================
    void visitDayViewports(ViewportVisitor visitor) {
    	
        int usable_width = getWidth() - getPaddingLeft() - getPaddingRight();
        int usable_height = getHeight() - getPaddingTop() - getPaddingBottom();
        
        float inter_day_horizontal_padding = this.horizontal_spacing;
        float width_per_day = (usable_width - (DAYS_PER_WEEK - 1)*inter_day_horizontal_padding) / DAYS_PER_WEEK;
        
        
        int weeks_per_month = (int) Math.ceil(this.day_list.size() / (float) DAYS_PER_WEEK);
        
        float inter_day_vertical_padding = this.vertical_spacing;
        float height_per_day = (usable_height - (weeks_per_month - 1)*inter_day_vertical_padding) / weeks_per_month;
        
    	
        for (int i = 0; i < this.day_list.size(); i++) {
            final CalendarDay child = this.day_list.get(i);
            
            int left = getPaddingLeft() + (int) ((width_per_day + inter_day_horizontal_padding) * (i % DAYS_PER_WEEK));
            int top = getPaddingTop() + (int) ((height_per_day + inter_day_vertical_padding) * (i / DAYS_PER_WEEK));

            RectF viewport = new RectF(left, top, left + width_per_day, top + height_per_day);
            
            visitor.visitViewport(viewport, child);
        }
    }

    // ========================================================================
    protected void drawDay(Canvas canvas, RectF viewport, CalendarDay day) {

    	Calendar daycal = new GregorianCalendar();
    	daycal.setTime(day.date);
    	
    	boolean dim = month_calendar.get(Calendar.MONTH) == daycal.get(Calendar.MONTH);
        
        Resources resources = this.context.getResources();
        
        boolean day_highlighted = day == this.highlighted_day;
        int background_color = day_highlighted ?
        		resources.getColor(
        				dim ? R.color.calendar_date_background_passive_selected : R.color.calendar_date_background_active_selected)
        		: resources.getColor(
        				dim ? R.color.calendar_date_background_passive : R.color.calendar_date_background_active);
        				
        if (this.is_holding_longpress) {

        	long now = SystemClock.uptimeMillis();
        	float alpha = (now - this.longpress_start_time) / (float) LONGPRESS_DURATION;

        	alpha = Math.min(1, alpha);
        	
        	int target_color = resources.getColor(R.color.calendar_date_background_longpress);
        	background_color = interpolateColor(background_color, target_color, alpha);
        }
        				
		// Draw the background
        my_paint.setColor(background_color);
        RectF bg = new RectF(0, 0, viewport.width(), viewport.height());
        canvas.drawRect(bg, my_paint);
		

        my_paint.setStyle(Style.FILL);
        
        float usable_size = Math.min(viewport.width(), viewport.height());


		
		
		

        drawEventCount(canvas, resources, viewport, day, usable_size);
        drawCornerBox(canvas, resources, viewport, day, usable_size);
        
        
        // We animate the view by repeatedly invalidating it.
        if (is_holding_longpress)
        	this.invalidate();
    }

    // ========================================================================
    void drawEventCount(Canvas canvas, Resources resources, RectF viewport, CalendarDay calendar_day, float usable_size) {

        canvas.save();
        canvas.translate(viewport.width()/2f, viewport.height()/2f);
        
		
		int event_count = calendar_day.day_events.size();

		if (event_count > 0) {

	        // Draw decorative circle
	        my_paint.setColor(Color.CYAN);
			canvas.drawCircle(0, 0, usable_size/3, my_paint);
			
			
	
	        float corner_box_side = usable_size/2f;
			my_paint.setTextSize(corner_box_side*0.8f);
			float text_height = my_paint.getFontMetrics().ascent + my_paint.getFontMetrics().descent;
	
	
	        int event_count_number = resources.getColor(R.color.event_count_number);
	        my_paint.setColor(event_count_number);
			String text = Integer.toString( event_count );
			canvas.drawText(text, 0, -text_height/2, my_paint);
		}
		
		canvas.restore();
    }
    
    // ========================================================================
    void drawCornerBox(Canvas canvas, Resources resources, RectF viewport, CalendarDay calendar_day, float usable_size) {
    	
        float corner_box_side = usable_size/2f;
        RectF rect = new RectF(
        		0,
        		0,
        		corner_box_side,
        		corner_box_side);

        int cornerbox_color = resources.getColor(R.color.cornerbox_color);
        my_paint.setColor(cornerbox_color);
        canvas.drawRect(rect, my_paint);
        canvas.save();
        canvas.translate(rect.centerX(), rect.centerY());
        
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(calendar_day.date);
		String text = Integer.toString( cal.get(Calendar.DAY_OF_MONTH) );
//		Rect text_bounds = new Rect();
//		my_paint.getTextBounds(text, 0, text.length(), text_bounds);
		my_paint.setTextSize(corner_box_side*0.8f);
		float text_height = my_paint.getFontMetrics().ascent + my_paint.getFontMetrics().descent;

		
        int text_color = resources.getColor(R.color.calendar_date_number);
        my_paint.setColor(text_color);
		canvas.drawText(text, 0, -text_height/2, my_paint);
		
		canvas.restore();
    }

    // ========================================================================
    class ClosestViewportVisitor implements ViewportVisitor {
    	
    	private PointF point = null;
    	private float min_distance = Float.MAX_VALUE;
    	private CalendarDay closest_day = null;

    	ClosestViewportVisitor(PointF point) {
    		this.point = point;
    	}
    	    	
		@Override
		public void visitViewport(RectF viewport, CalendarDay child) {
			float dx = viewport.centerX() - this.point.x;
			float dy = viewport.centerY() - this.point.y;
			float squared_dist = dx*dx + dy*dy;
            if (squared_dist < this.min_distance) {
            	this.min_distance = squared_dist;
            	this.closest_day = child;
            }
		}
		
		CalendarDay getClosest() {
			return this.closest_day;
		}
    }
    
    // ========================================================================
    CalendarDay getDayFromPoint(PointF point) {
    	ClosestViewportVisitor visitor = new ClosestViewportVisitor(point);
    	visitDayViewports(visitor);
    	return visitor.getClosest();
    }
    
    // ========================================================================
	int interpolateInt(int src, int dst, float alpha) {
		return (int) ((dst - src)*alpha) + src;
	}

    // ========================================================================
	int interpolateColor(int src_color, int dst_color, float alpha) {
		int red = interpolateInt(Color.red(src_color), Color.red(dst_color), alpha);
		int green = interpolateInt(Color.green(src_color), Color.green(dst_color), alpha);
		int blue = interpolateInt(Color.blue(src_color), Color.blue(dst_color), alpha);
		return Color.rgb(red, green, blue);
	}

    // ========================================================================
	void moveMonth(boolean forward) {
		
		int inc_value = forward ? 1 : -1;
        this.month_calendar.add(Calendar.MONTH, inc_value);
    	init();
        invalidate();
        
        if (this.month_update_callback != null) {
        	this.month_update_callback.updateMonth(this.month_calendar);
        }
	}
	
    // ========================================================================
    private static final int SWIPE_MIN_DISTANCE = 60;
    private static final int SWIPE_MAX_OFF_PATH = 300;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;
    class MonthGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	
        	Log.d(TAG, "Intercepted fling gesture...");
        	
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {

                	moveMonth(true);
                    
                }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    
                	moveMonth(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        
        @Override
        public boolean onSingleTapConfirmed (MotionEvent e) {
        	CalendarDay day = getDayFromPoint(new PointF(e.getX(), e.getY()));
        	highlighted_day = day;
        	
        	executeDay(day);
        	
			return true;
        }
        
        @Override
        public boolean onDown(MotionEvent e) {
        	
        	CalendarDay day = getDayFromPoint(new PointF(e.getX(), e.getY()));
        	highlighted_day = day;
        	
        	invalidate();
        	return true;
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        	Log.d(TAG, "Scrolled verically by " + distanceY);
        	return true;
        }
    }
    
    // ==========================================================
    void executeDay(CalendarDay day) {
    	Log.d(TAG, "Chosen day: " + day.date);
    	this.day_click_callback.clickDay(day);
    }
}
