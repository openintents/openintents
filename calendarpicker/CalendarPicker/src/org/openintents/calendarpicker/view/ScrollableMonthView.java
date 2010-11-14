package org.openintents.calendarpicker.view;


import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.container.SimpleCalendarDay;
import org.openintents.calendarpicker.container.SimpleEvent;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.os.SystemClock;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;

public class ScrollableMonthView extends View {

	static final String TAG = "ScrollableMonthView";
	

	

	
	public static final long MILLISECONDS_PER_DAY = 1000L*60*60*24;
	static final int DAYS_PER_WEEK = 7;
	static final long MILLISECONDS_PER_WEEK = DAYS_PER_WEEK*MILLISECONDS_PER_DAY;
	


    float horizontal_spacing = 2;
    float vertical_spacing = 2;
    
    

//    Context context;
    Resources resources;
	Paint month_shapes_paint;
	TextPaint month_watermark_text_paint;
    Calendar month_calendar;


    List<SimpleEvent> sorted_events;
    Date highlighted_day = null;

    // Cached computed values
    int spanned_weeks;
	float max_month_width;
    
    
    MonthUpdateCallback month_update_callback = null;
    OnDaySelectionListener day_click_callback, day_touch_callback, scroll_callback;
    

    
    // Animation-related values
    float MONTH_TEXT_FADER_MILLISECONDS = 500;
	TimedAnimation month_text_fader = null;
	
	
	
    boolean is_holding_longpress = false;
    long longpress_start_time;
    int LONGPRESS_DURATION = ViewConfiguration.getLongPressTimeout();
    
	

//  float SNAP_BACK_ANIMATION_ACCELERATION = 100;	// 100 px/s^2
	float SNAP_BACK_MILLISECONDS = 500;
	TimedAnimation snap_back_animation = null;
	
    float VERTICAL_SCROLL_TOLERANCE = 75;
    
	float snap_back_start_offset = 0;
    float vertical_offset = 0;
    
    
    
    
    // FLINGING STATE
    static final float MINIMUM_SUSTAINED_FLINGING_VELOCITY = 20;	// in px/sec
    static final float FLINGING_DECELERATION = 750;	// in px/sec/sec
    float current_flinging_velocity = 0;
	
    long last_frame_time_for_fling;
    
    // ========================================================================
    public ScrollableMonthView(Context context, AttributeSet attrs) {
    	super(context, attrs);

        this.resources = context.getResources();
    	setMonth(new GregorianCalendar());
    	
        month_shapes_paint = new Paint();
		month_shapes_paint.setAntiAlias(true);
		month_shapes_paint.setColor(Color.WHITE);

        month_watermark_text_paint = new TextPaint();
		month_watermark_text_paint.setAntiAlias(true);
		month_watermark_text_paint.setColor(this.resources.getColor(R.color.background_month_text));
    	month_watermark_text_paint.setTextAlign(Align.RIGHT);
		
		max_month_width = getMaxMonthWidth(month_watermark_text_paint);

        this.setFocusable(true);
        
        final GestureDetector gestureDetector = new GestureDetector(new MonthGestureDetector());
        setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
	        	
	        	if (event.getAction() == MotionEvent.ACTION_UP) {

	            	is_holding_longpress = false;
	        		
	        		if (Math.abs(vertical_offset) > 0) {
	        			
	        			// FIXME
	        			// Cause the snap-back to be triggered when the fling
	        			// velocity drops below the minimum threshold
	        			
	        			/*
	        			snap_back_animation = new TimedAnimation(SystemClock.uptimeMillis(), MONTH_TEXT_FADER_MILLISECONDS);
	        			snap_back_start_offset = vertical_offset;

        				invalidate();
        				*/
	        		}
	        	} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
	        		snap_back_animation = null;
	        	}
	        	
				
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
				
				Calendar cal = new GregorianCalendar();
				cal.setTime(highlighted_day != null ? highlighted_day : month_calendar.getTime());
								
		    	switch (keyCode) {
		    	case KeyEvent.KEYCODE_DPAD_UP:
		    	{
		    		cal.add(Calendar.DATE, -DAYS_PER_WEEK);
		    	    highlighted_day = cal.getTime();
		    	    touchDay(highlighted_day);
		    		break;
		    	}
		    	case KeyEvent.KEYCODE_DPAD_LEFT:
		    	{
		    		cal.add(Calendar.DATE, -1);
		    	    highlighted_day = cal.getTime();
		    	    touchDay(highlighted_day);
		    		break;
		    	}
		    	case KeyEvent.KEYCODE_DPAD_DOWN:
		    	{
		    		cal.add(Calendar.DATE, DAYS_PER_WEEK);
		    	    highlighted_day = cal.getTime();
		    	    touchDay(highlighted_day);
		    		break;
		    	}
		    	case KeyEvent.KEYCODE_DPAD_RIGHT:
		    	{
		    		cal.add(Calendar.DATE, 1);
		    	    highlighted_day = cal.getTime();
					touchDay(highlighted_day);
		    		break;
		    	}
		    	case KeyEvent.KEYCODE_DPAD_CENTER:
		    		if (highlighted_day != null) {
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
	private static float getMaxMonthWidth(Paint paint) {
	
        DateFormatSymbols dfs = new DateFormatSymbols();
        float max_month_width = Float.MIN_VALUE;
    	Rect bounds = new Rect();
    	for (String month : dfs.getMonths()) {
    		paint.getTextBounds(month, 0, month.length(), bounds);
    		if (bounds.width() > max_month_width)
    			max_month_width = bounds.width();
    	}
    	
    	return max_month_width;
	}

    // ========================================================================
	public Calendar getCalendar() {
		return this.month_calendar;
	}
    
    // ========================================================================
    public interface MonthUpdateCallback {
    	void updateMonth(Calendar cal);
    }
    
    // ========================================================================
    public interface OnDaySelectionListener {
    	void updateDate(Date date);
    }

    // ========================================================================
    interface ViewportVisitor {
    	void visitViewport(RectF daybox, SimpleCalendarDay child);
    }
    
    // ========================================================================
    public void setOnDayClickListener(OnDaySelectionListener callback) {
    	this.day_click_callback = callback;
    }
    
    // ========================================================================
    public void setOnScrollListener(OnDaySelectionListener callback) {
    	this.scroll_callback = callback;
    }

    // ========================================================================
    public void setOnDayTouchListener(OnDaySelectionListener callback) {
    	this.day_touch_callback = callback;
    }
    
    // ========================================================================
    public void setMonthUpdateCallback(MonthUpdateCallback callback) {
    	this.month_update_callback = callback;
    }

    // ========================================================================
    /** After this routine, the calendar is guaranteed to be at the first
     * of the month.
     */
    public void setMonth(Calendar calendar) {
    	this.month_calendar = calendar;
    	setCalendarToFirstDayOfMonth(this.month_calendar);
    	this.vertical_offset = 0;
    	this.spanned_weeks = calcSpannedWeeksForMonth();
    }

    // ========================================================================
    public void setMonthAndEvents(Calendar calendar, List<SimpleEvent> sorted_events) {
    	setMonth(calendar);
    	this.sorted_events = sorted_events;
    }

    // ========================================================================
    /** Rolls the date back to the beginning of the week of the first week of the month.
     * Expects the calendar date to already be set to the first day of the month. */
    void setMonthWeekBeginning(Calendar working_calendar) {

    	while (true) {
    		if (working_calendar.get(Calendar.DAY_OF_WEEK) == working_calendar.getFirstDayOfWeek())
    			break;
    		working_calendar.add(Calendar.DAY_OF_MONTH, -1);
    	}
    }

    // ========================================================================
    @Override
    protected void onDraw(final Canvas canvas) {
        super.onDraw(canvas);

        canvas.save();
        canvas.translate(0, this.vertical_offset);
        
        drawMonthWatermarkText(canvas);
        
        // Draw all of the visible days
        visitDayViewports(new ViewportVisitor() {
			@Override
			public void visitViewport(RectF daybox, SimpleCalendarDay child) {
	            canvas.save();
	            canvas.translate(daybox.left, daybox.top);
	            drawDay(canvas, daybox, child);
	            canvas.restore();
			}
        });
        
        canvas.restore();
        
        handleViewAnimation(canvas);
    }

    // ========================================================================
    void handleViewAnimation(Canvas canvas) {
        
        // We animate the view by repeatedly invalidating it.
        if (is_holding_longpress) {
        	this.invalidate();
        }

        
        if (Math.abs(current_flinging_velocity) > 0) {
        	
        	long now = SystemClock.uptimeMillis();
        	long fling_frame_millis_delta = now - last_frame_time_for_fling;
        	last_frame_time_for_fling = now;

        	// Update velocity
        	float velocity_delta_magnitude = FLINGING_DECELERATION*fling_frame_millis_delta/1000;

        	// Protect the velocity from increasing in the opposite direction after
        	// crossing zero.
        	velocity_delta_magnitude = Math.min(velocity_delta_magnitude, Math.abs(current_flinging_velocity));
        	
        	// If the velocity is positive, we decelerate it by adding a negative delta.
        	// Otherwise, leave the delta as positive, which will bring a negative velocity
        	// closer to zero.
        	if (current_flinging_velocity > 0)
        		velocity_delta_magnitude = -velocity_delta_magnitude;

            current_flinging_velocity += velocity_delta_magnitude;
            
            // Velocity is in units of pixels/second.
        	float position_delta = current_flinging_velocity*fling_frame_millis_delta/1000;
        	vertical_offset += position_delta;
        	


        	if (scroll_callback != null)
        		scroll_callback.updateDate(getScrollOffsetDate());
        	
        	invalidate();
        	
        } else if (snap_back_animation != null) {
        	
        	long now = SystemClock.uptimeMillis();
        	if (snap_back_animation.isFinished(now)) {
        		vertical_offset = 0;
        		snap_back_animation = null;
        	} else {
        		float fraction = snap_back_animation.getFraction(now);
        		vertical_offset = (1 - fraction) * snap_back_start_offset;
        	}
        	
        	invalidate();
        }
        
        if (month_text_fader != null) {
            long now = SystemClock.uptimeMillis();
            if (month_text_fader.isFinished(now))
            	month_text_fader = null;
			
        	invalidate();
        }
    }
    
    // ========================================================================
    Date getScrollOffsetDate() {
        float height_per_week = getDayBoxHeight() + this.vertical_spacing;
        float weeks_offset = this.vertical_offset/height_per_week;
        long milliseconds_offset = (long) (MILLISECONDS_PER_WEEK*weeks_offset);
        
        return new Date(this.month_calendar.getTimeInMillis() - milliseconds_offset);
    }
    
    // ========================================================================
    void drawMonthWatermarkText(Canvas canvas) {

		// Set the scale to the widest month    	
    	float scale = getHeight() / this.max_month_width;

    	SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
    	String month_string = sdf.format(this.month_calendar.getTime());

        long now = SystemClock.uptimeMillis();
        float fraction = 1;
        if (this.month_text_fader != null)
        	fraction = this.month_text_fader.getFraction(now);

    	int target_color = this.resources.getColor(R.color.background_month_text);
    	int text_color = interpolateColor(Color.WHITE, target_color, fraction);
    	this.month_watermark_text_paint.setColor(text_color);
    	
    	
		canvas.save();
		canvas.translate(getWidth(), 0);
		canvas.rotate(-90);
		canvas.scale(scale, scale);
		// XXX The month names look more stylish if we align
		// the baseline with the edge of the screen, but this cuts
		// of the capital "J"s and the "y"s.
//		canvas.translate(0, -month_bg_paint.getFontMetrics().descent);

		canvas.drawText(month_string, 0, 0, this.month_watermark_text_paint);
		canvas.restore();	
    }

    // ========================================================================
    int getNextMonthIndex(Calendar calendar) {
    	// Get index of following month
    	Calendar dupe2 = (Calendar) calendar.clone();
    	dupe2.add(Calendar.MONTH, 1);
    	return dupe2.get(Calendar.MONTH);
    }
    
    // ========================================================================
    void setCalendarToFirstDayOfMonth(Calendar calendar) {

    	int month = calendar.get(Calendar.MONTH);
    	int year = calendar.get(Calendar.YEAR);
    	calendar.clear();
    	calendar.set(year, month, calendar.getMinimum(Calendar.DAY_OF_MONTH));
    }
    
    // ========================================================================
    int calcSpannedWeeksForMonth() {

    	// Set working calendar to first day of the month.
    	Calendar working_calendar = (Calendar) this.month_calendar.clone();
    	setMonthWeekBeginning(working_calendar);

    	// Count the weeks spanned by this month
    	int spanned_weeks = 0;
    	int next_month_index = getNextMonthIndex(this.month_calendar);
    	while (working_calendar.get(Calendar.MONTH) != next_month_index) {
    		working_calendar.add(Calendar.DAY_OF_MONTH, DAYS_PER_WEEK);
    		spanned_weeks++;
    	}
    	
    	return spanned_weeks;
    }

    // ========================================================================
    /** Calculate vertical dimensions */
    float getDayBoxHeight() {

    	int usable_height = getHeight() - (getPaddingTop() + getPaddingBottom());
        float day_box_height = (usable_height - (this.spanned_weeks - 1)*this.vertical_spacing) / this.spanned_weeks;
        return day_box_height;
    }
    
    // ========================================================================
    /** Implementation of the visitor pattern that iterates through
     * each of the days that are visible on screen.
     */
    void visitDayViewports(ViewportVisitor visitor) {
    	
        // Calculate horizontal dimensions
        int usable_width = getWidth() - (getPaddingLeft() + getPaddingRight());
        float day_box_width = (usable_width - (DAYS_PER_WEEK - 1)*this.horizontal_spacing) / DAYS_PER_WEEK;
        float width_per_day = day_box_width + this.horizontal_spacing;
    	
    	
        float day_box_height = getDayBoxHeight();
        float height_per_week = day_box_height + this.vertical_spacing;


        
        // Reset calendar to beginning of first week
    	Calendar working_calendar = (Calendar) this.month_calendar.clone();
    	setMonthWeekBeginning(working_calendar);

    	// Reposition calendar according to the vertical scroll offset
    	int weeks_offset = (int) Math.floor(-vertical_offset/height_per_week);
		working_calendar.add(Calendar.DAY_OF_MONTH, weeks_offset*DAYS_PER_WEEK);

		// Skip all events before the given calendar date
        int event_idx=0;
        while (event_idx < this.sorted_events.size() && this.sorted_events.get(event_idx).timestamp.before(working_calendar.getTime())) event_idx++;


        // The "<=" as opposed to the typical "<" allows the entire screen to be
        // covered while scrolling.
        RectF viewport = new RectF();
    	SimpleCalendarDay scd = new SimpleCalendarDay();
        for (int i=0; i <= spanned_weeks; i++) {

            float top = getPaddingTop() + height_per_week * (i + weeks_offset);
        	
            for (int j=0; j < DAYS_PER_WEEK; j++) {

            	scd.reset(working_calendar.getTime());

            	// We advance the calendar to the next day *after* recording
            	// the date for the current day.
        		working_calendar.add(Calendar.DAY_OF_MONTH, 1);
        		
                // Consume all events up until the next day
                while (event_idx < this.sorted_events.size() && this.sorted_events.get(event_idx).timestamp.before(working_calendar.getTime())) {
                	scd.incrementEventCount();
//                	child.day_events.add(this.sorted_events.get(event_idx));
                	event_idx++;
                }

                float left = getPaddingLeft() + ((width_per_day) * j);
                viewport.set(left, top, left + day_box_width, top + day_box_height);
                
                visitor.visitViewport(viewport, scd);
            }
        }
    }
    
    // ========================================================================
    protected void drawDay(Canvas canvas, RectF daybox, SimpleCalendarDay day) {

    	Calendar daycal = new GregorianCalendar();
    	daycal.setTime(day.getDate());
    	
    	int daycal_month_idx = daycal.get(Calendar.MONTH);
    	boolean month_active = this.month_calendar.get(Calendar.MONTH) == daycal_month_idx;
    	boolean daycal_month_odd = daycal_month_idx % 2 != 0;
        boolean day_highlighted = day.getDate().equals(this.highlighted_day);
        
        int background_color = day_highlighted ?
        		this.resources.getColor(
        				month_active ?
        						R.color.calendar_date_background_active_selected
        						: (daycal_month_odd ? R.color.calendar_date_background_passive_odd_selected : R.color.calendar_date_background_passive_even_selected))
        		: resources.getColor(
        				month_active ?
        						R.color.calendar_date_background_active
        						: (daycal_month_odd ? R.color.calendar_date_background_passive_odd : R.color.calendar_date_background_passive_even)				
        		);
        				
        if (this.is_holding_longpress && day.getDate().equals(this.highlighted_day)) {

        	long now = SystemClock.uptimeMillis();
        	
        	// Before you start to animate, check whether the tap time has been exceeded.
        	long tap_timeout = ViewConfiguration.getTapTimeout();
        	if (now > this.longpress_start_time + tap_timeout) {
	        	
	        	float alpha = (now - (this.longpress_start_time + tap_timeout)) / (float) LONGPRESS_DURATION;
	
	        	alpha = Math.min(1, alpha);
	        	
	        	int target_color = this.resources.getColor(R.color.calendar_date_background_longpress);
	        	background_color = interpolateColor(background_color, target_color, alpha);
        	}
        }
        				
		// Draw the background
        this.month_shapes_paint.setColor(background_color);
        canvas.drawRect(0, 0, daybox.width(), daybox.height(), month_shapes_paint);
        
        float usable_size = Math.min(daybox.width(), daybox.height());
        drawEventCount(canvas, daybox, day, usable_size);
        drawCornerBox(canvas, daybox, day, usable_size, month_active);
    }

    // ========================================================================
    void drawEventCount(Canvas canvas, RectF daybox, SimpleCalendarDay calendar_day, float usable_size) {

        canvas.save();
        canvas.translate(daybox.width()/2f, daybox.height()/2f);
        
		
		int event_count = calendar_day.getEventCount();

		if (event_count > 0) {

	        // Draw decorative circle
	        month_shapes_paint.setColor(Color.CYAN);
	        month_shapes_paint.setAlpha(0xff/2);
			canvas.drawCircle(0, 0, usable_size/3, month_shapes_paint);
			
			
			// Draw the number of events inside the circle
	        float corner_box_side = usable_size/2f;
			month_shapes_paint.setTextSize(corner_box_side*0.8f);
			float text_height = month_shapes_paint.getFontMetrics().ascent + month_shapes_paint.getFontMetrics().descent;
	
	        int event_count_number = resources.getColor(R.color.event_count_number);
	        month_shapes_paint.setColor(event_count_number);
			String text = Integer.toString( event_count );
	    	month_shapes_paint.setTextAlign(Align.CENTER);
			canvas.drawText(text, 0, -text_height/2, month_shapes_paint);
		}
		
		canvas.restore();
    }
    
    // ========================================================================
    void drawCornerBox(Canvas canvas, RectF viewport, SimpleCalendarDay calendar_day, float usable_size, boolean month_active) {
    	
        float corner_box_side = usable_size/2f;

        int cornerbox_color = resources.getColor(R.color.cornerbox_color);
        month_shapes_paint.setColor(cornerbox_color);
        canvas.drawRect(0, 0, corner_box_side, corner_box_side, month_shapes_paint);
        canvas.save();
        canvas.translate(corner_box_side/2f, corner_box_side/2f);
        
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(calendar_day.getDate());
		String text = Integer.toString( cal.get(Calendar.DAY_OF_MONTH) );

		month_shapes_paint.setTextSize(corner_box_side*0.8f);
		float text_height = month_shapes_paint.getFontMetrics().ascent + month_shapes_paint.getFontMetrics().descent;

        int text_color = resources.getColor(month_active ? R.color.calendar_date_number : R.color.calendar_date_number_passive);
        month_shapes_paint.setColor(text_color);
        
    	month_shapes_paint.setTextAlign(Align.CENTER);
		canvas.drawText(text, 0, -text_height/2, month_shapes_paint);
		
		canvas.restore();
    }
    
    // ========================================================================
    Date getDayFromPoint(PointF point) {

    	// Use horizontal position to determine weekday
        int usable_width = getWidth() - (getPaddingLeft() + getPaddingRight());
    	int weekday_index = (int) (DAYS_PER_WEEK*(point.x - getPaddingLeft()) / usable_width);
    	Log.d(TAG, "Weekday index: " + weekday_index);
    	
    	// Use vertical position to determine week multiplier
    	int usable_height = getHeight() - (getPaddingTop() + getPaddingBottom());
        int week_offset = (int) (this.spanned_weeks*(point.y - getPaddingTop() - this.vertical_offset) / usable_height);
        
		Calendar offset_calendar_date = (Calendar) this.month_calendar.clone();
    	setMonthWeekBeginning(offset_calendar_date);

		offset_calendar_date.add(Calendar.DATE, DAYS_PER_WEEK*week_offset + weekday_index);
    	return offset_calendar_date.getTime();
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
        setMonth(this.month_calendar);
        
        this.month_text_fader = new TimedAnimation(SystemClock.uptimeMillis(), MONTH_TEXT_FADER_MILLISECONDS);
        invalidate();
        
        if (this.month_update_callback != null) {
        	this.month_update_callback.updateMonth(this.month_calendar);
        }
        
        if (this.day_touch_callback != null) {
        	this.day_touch_callback.updateDate(null);
        }
	}
	
    // ========================================================================
    private static final int SWIPE_MIN_DISTANCE = 75;
    private static final int SWIPE_MAX_OFF_HORIZONTAL_PATH = 300;
    private static final int SWIPE_MAX_OFF_VERTICAL_PATH = 300;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;
    class MonthGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	
            try {

            	boolean horizontal_fling_possible = true;
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_HORIZONTAL_PATH)
                	horizontal_fling_possible = false;
                
            	boolean vertical_fling_possible = true;
                if (Math.abs(e1.getX() - e2.getX()) > SWIPE_MAX_OFF_VERTICAL_PATH)
                	vertical_fling_possible = false;
                

                
                if (horizontal_fling_possible) {
                	float x_delta = e1.getX() - e2.getX();
	                if(x_delta > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	                	moveMonth(true);
	                }  else if (-x_delta > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	                	moveMonth(false);
	                }
                } else if (vertical_fling_possible) {
                	
                	Log.i(TAG, "Flung with vertical velocity: " + velocityY);
                	
                	float y_delta = e1.getY() - e2.getY();
	                if(y_delta > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
	                	
	                	Log.d(TAG, "Achieved vertical swipe DOWN");
	                	current_flinging_velocity = velocityY;
	                	last_frame_time_for_fling = SystemClock.uptimeMillis();
	                	invalidate();
	                	
	                }  else if (-y_delta > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
	                	
	                	Log.d(TAG, "Achieved vertical swipe UP");
	                	current_flinging_velocity = velocityY;
	                	last_frame_time_for_fling = SystemClock.uptimeMillis();
	                	invalidate();
	                }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return false;
        }
        
        @Override
        public boolean onSingleTapConfirmed (MotionEvent e) {
        	highlighted_day = getDayFromPoint(new PointF(e.getX(), e.getY()));
        	executeDay(highlighted_day);
        	
			return true;
        }
        
		@Override
		public void onLongPress(MotionEvent e) {
        	is_holding_longpress = false;
	    	showContextMenu();
		}
        
        @Override
        public boolean onDown(MotionEvent e) {
        	
        	is_holding_longpress = true;
        	longpress_start_time = SystemClock.uptimeMillis();
        	// Note: invalidate() is called below from within touchDay()
        	
        	// Stop a fling
        	current_flinging_velocity = 0;
        	
        	highlighted_day = getDayFromPoint(new PointF(e.getX(), e.getY()));
        	touchDay(highlighted_day);

        	return true;
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        	is_holding_longpress = false;
        	
			float dy = e2.getY() - e1.getY();
			if (Math.abs(dy) >= VERTICAL_SCROLL_TOLERANCE) {
//	        	vertical_offset = dy;
				vertical_offset -= distanceY;

	        	if (scroll_callback != null)
	        		scroll_callback.updateDate(getScrollOffsetDate());
	        	
	        	invalidate();
	        }
        	return true;
        }
    }

    // ==========================================================
    public static class MonthContextMenuInfo implements ContextMenu.ContextMenuInfo {
    	Date date;
    	MonthContextMenuInfo(Date date) {
    		this.date = date;
    	}
    	
    	public Date getDate() {
    		return this.date;
    	}
    }
    
    // ==========================================================
    protected ContextMenu.ContextMenuInfo getContextMenuInfo() {
		return new MonthContextMenuInfo(this.highlighted_day);
    }
    
    // ==========================================================
    void executeDay(Date date) {
    	Log.d(TAG, "Chosen day: " + date);
    	this.day_click_callback.updateDate(date);
    }
        
    // ==========================================================
    void touchDay(Date date) {
    	Log.d(TAG, "Chosen day: " + date);
    	this.day_touch_callback.updateDate(date);
        	invalidate();
    }
}
