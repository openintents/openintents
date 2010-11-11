package org.openintents.calendarpicker.view;


import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.os.SystemClock;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
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
	TextPaint month_bg_paint;
    Calendar month_calendar;
    
    CalendarDay highlighted_day = null;
    
    MonthUpdateCallback month_update_callback = null;
    OnDaySelectionListener day_click_callback = null, day_touch_callback = null;
    

    
	float max_month_width;
    
    boolean snapping_back = false;



    float MONTH_TEXT_FADER_MILLISECONDS = 500;
	TimedAnimation month_text_fader = null;
	

//  float SNAP_BACK_ANIMATION_ACCELERATION = 100;	// 100 px/s^2
	float SNAP_BACK_MILLISECONDS = 500;
	TimedAnimation snap_back_animation = null;
	
    float VERTICAL_SCROLL_TOLERANCE = 75;
    
	float snap_back_start_offset = 0;
    float vertical_offset = 0;
	
    // ========================================================================
	float getMaxMonthWidth(Paint paint) {
	
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
    public ScrollableMonthView(Context context, AttributeSet attrs) {
    	super(context, attrs);

        this.context = context;
    	setMonth(new GregorianCalendar());
    	
        my_paint = new Paint();
		my_paint.setAntiAlias(true);
		my_paint.setColor(Color.WHITE);

        month_bg_paint = new TextPaint();
		month_bg_paint.setAntiAlias(true);
		month_bg_paint.setColor(this.context.getResources().getColor(R.color.background_month_text));
    	month_bg_paint.setTextAlign(Align.RIGHT);
		
		max_month_width = getMaxMonthWidth(month_bg_paint);

        this.setFocusable(true);
        
        final GestureDetector gestureDetector = new GestureDetector(new MonthGestureDetector());
        setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
	        	
	        	if (event.getAction() == MotionEvent.ACTION_UP) {
	        		if (Math.abs(vertical_offset) > 0) {
	        			
	        			snap_back_animation = new TimedAnimation(SystemClock.uptimeMillis(), MONTH_TEXT_FADER_MILLISECONDS);
	        			snap_back_start_offset = vertical_offset;

        				invalidate();
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
        
        /*
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
		    	    touchDay(highlighted_day);
		    		break;
		    	}
		    	case KeyEvent.KEYCODE_DPAD_UP:
		    	case KeyEvent.KEYCODE_DPAD_RIGHT:
		    	{
		    		idx = (idx + 1) % day_list.size();
		    	    highlighted_day = day_list.get(idx);
					touchDay(highlighted_day);
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
		*/
    }
    
    
    // ========================================================================
    public interface MonthUpdateCallback {
    	void updateMonth(Calendar cal);
    }
    
    // ========================================================================
    public interface OnDaySelectionListener {
    	void clickDay(CalendarDay day);
    }
    
    // ========================================================================
    public void setOnDayClickListener(OnDaySelectionListener callback) {
    	this.day_click_callback = callback;
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
    public void setMonth(Calendar calendar) {
    	this.month_calendar = calendar;
    }
    
    List<SimpleEvent> sorted_events;
    
    // ========================================================================
    public void setMonthAndEvents(Calendar calendar, List<SimpleEvent> sorted_events) {
    	this.month_calendar = calendar;
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
        
        drawMonthText(canvas);
        
        visitDayViewports(new ViewportVisitor() {
			@Override
			public void visitViewport(RectF viewport, CalendarDay child) {
	            canvas.save();
	            canvas.translate(viewport.left, viewport.top);
	            drawDay(canvas, viewport, child);
	            canvas.restore();
			}
        });
        
        canvas.restore();
        
        
        
        // We animate the view by repeatedly invalidating it.
        if (is_holding_longpress) {
        	this.invalidate();
        }
         
        if (snap_back_animation != null) {
        	
        	long now = SystemClock.uptimeMillis();
        	if (snap_back_animation.isFinished(now)) {
        		vertical_offset = 0;
        		snap_back_animation = null;
        	} else {
        		float fraction = snap_back_animation.getFraction(now);
        		vertical_offset = (1 - fraction) * snap_back_start_offset;
        	}
        	
        	this.invalidate();
        }
        
        if (month_text_fader != null) {
            long now = SystemClock.uptimeMillis();
            if (month_text_fader.isFinished(now))
            	month_text_fader = null;
			
        	this.invalidate();
        }
    }
    
    // ========================================================================
    void drawMonthText(Canvas canvas) {

		// Set the scale to the widest month    	
    	float scale = getHeight() / this.max_month_width;

    	SimpleDateFormat sdf = new SimpleDateFormat("MMMM");
    	String month_string = sdf.format(this.month_calendar.getTime());

        long now = SystemClock.uptimeMillis();
        float fraction = 1;
        if (month_text_fader != null)
        	fraction = month_text_fader.getFraction(now);

    	int target_color = this.context.getResources().getColor(R.color.background_month_text);
    	int text_color = interpolateColor(Color.WHITE, target_color, fraction);
    	month_bg_paint.setColor(text_color);
    	
    	
		canvas.save();
		canvas.translate(getWidth(), 0);
		canvas.rotate(-90);
		canvas.scale(scale, scale);
		// XXX The month names look more stylish if we align
		// the baseline with the edge of the screen, but this cuts
		// of the capital "J"s and the "y"s.
//		canvas.translate(0, -month_bg_paint.getFontMetrics().descent);

		canvas.drawText(month_string, 0, 0, month_bg_paint);
		canvas.restore();	
    }

    // ========================================================================
    interface ViewportVisitor {
    	void visitViewport(RectF viewport, CalendarDay child);
    }

    // ========================================================================
    int getNextMonthIndex(Calendar calendar) {
    	// Get index of following month
    	Calendar dupe2 = (Calendar) calendar.clone();
    	dupe2.add(Calendar.MONTH, 1);
    	return dupe2.get(Calendar.MONTH);
    }
    
    // ========================================================================
    void visitDayViewports(ViewportVisitor visitor) {
    	
        int usable_width = getWidth() - getPaddingLeft() - getPaddingRight();
        int usable_height = getHeight() - getPaddingTop() - getPaddingBottom();
        
        // Calculate horizontal dimensions
        float day_box_width = (usable_width - (DAYS_PER_WEEK - 1)*this.horizontal_spacing) / DAYS_PER_WEEK;
        float width_per_day = day_box_width + this.horizontal_spacing;
        
    	int month_index = month_calendar.get(Calendar.MONTH);

    	// Set working calendar to first day of the month.
    	Calendar working_calendar = new GregorianCalendar();
    	working_calendar.clear();
    	working_calendar.set(month_calendar.get(Calendar.YEAR), month_index, working_calendar.getMinimum(Calendar.DAY_OF_MONTH));
    	int next_month_index = getNextMonthIndex(working_calendar);
    	setMonthWeekBeginning(working_calendar);

    	// Count the weeks spanned by this month
    	int spanned_weeks = 0;
    	while (working_calendar.get(Calendar.MONTH) != next_month_index) {
    		working_calendar.add(Calendar.DAY_OF_MONTH, DAYS_PER_WEEK);
    		spanned_weeks++;
    	}
    	
        // Calculate vertical dimensions
        float day_box_height = (usable_height - (spanned_weeks - 1)*this.vertical_spacing) / spanned_weeks;
        float height_per_week = day_box_height + this.vertical_spacing;
        
        // Reset calendar to beginning of first week
        working_calendar.set(month_calendar.get(Calendar.YEAR), month_index, working_calendar.getMinimum(Calendar.DAY_OF_MONTH));
    	setMonthWeekBeginning(working_calendar);

    	// Reposition calendar according to the vertical scroll offset
    	int weeks_offset = (int) Math.floor(-vertical_offset/height_per_week);
		working_calendar.add(Calendar.DAY_OF_MONTH, weeks_offset*DAYS_PER_WEEK);

		// Skip all events before the given calendar date
        int event_idx=0;
        while (event_idx < this.sorted_events.size() && this.sorted_events.get(event_idx).timestamp.before(working_calendar.getTime())) event_idx++;


        // The "<=" as opposed to the typical "<" allows the entire screen to be
        // covered while scrolling.
        for (int i=0; i <= spanned_weeks; i++) {

            float top = getPaddingTop() + height_per_week * (i + weeks_offset);
        	
            for (int j=0; j < DAYS_PER_WEEK; j++) {

                CalendarDay child = new CalendarDay(working_calendar.getTime());

        		working_calendar.add(Calendar.DAY_OF_MONTH, 1);
        		
                // Consume all events up until the next day
                while (event_idx < this.sorted_events.size() && this.sorted_events.get(event_idx).timestamp.before(working_calendar.getTime())) {
                	child.day_events.add(this.sorted_events.get(event_idx));
                	event_idx++;
                }

                float left = getPaddingLeft() + ((width_per_day) * j);
                RectF viewport = new RectF(left, top, left + day_box_width, top + day_box_height);
                
                visitor.visitViewport(viewport, child);
            }
        }
    }

    // ========================================================================
    protected void drawDay(Canvas canvas, RectF viewport, CalendarDay day) {

    	Calendar daycal = new GregorianCalendar();
    	daycal.setTime(day.date);
    	
    	boolean month_active = month_calendar.get(Calendar.MONTH) == daycal.get(Calendar.MONTH);
        
        Resources resources = this.context.getResources();
        
        boolean day_highlighted = day == this.highlighted_day;
        int background_color = day_highlighted ?
        		resources.getColor(
        				month_active ? R.color.calendar_date_background_passive_selected : R.color.calendar_date_background_active_selected)
        		: resources.getColor(
        				month_active ? R.color.calendar_date_background_passive : R.color.calendar_date_background_active);
        				
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

        
        float usable_size = Math.min(viewport.width(), viewport.height());
        drawEventCount(canvas, resources, viewport, day, usable_size);
        drawCornerBox(canvas, resources, viewport, day, usable_size, month_active);
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
	    	my_paint.setTextAlign(Align.CENTER);
			canvas.drawText(text, 0, -text_height/2, my_paint);
		}
		
		canvas.restore();
    }
    
    // ========================================================================
    void drawCornerBox(Canvas canvas, Resources resources, RectF viewport, CalendarDay calendar_day, float usable_size, boolean month_active) {
    	
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

        int text_color = resources.getColor(month_active ? R.color.calendar_date_number : R.color.calendar_date_number_passive);
        my_paint.setColor(text_color);
        
    	my_paint.setTextAlign(Align.CENTER);
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

    	month_text_fader = new TimedAnimation(SystemClock.uptimeMillis(), MONTH_TEXT_FADER_MILLISECONDS);
        invalidate();
        
        if (this.month_update_callback != null) {
        	this.month_update_callback.updateMonth(this.month_calendar);
        }
        
        if (this.day_touch_callback != null) {
        	this.day_touch_callback.clickDay(null);
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
        	
        	touchDay(day);

        	return true;
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

			float dy = e2.getY() - e1.getY();
			if (Math.abs(dy) >= VERTICAL_SCROLL_TOLERANCE) {
	        	vertical_offset = dy; 
	        	invalidate();
	        }
        	return true;
        }
    }
    
    // ==========================================================
    void executeDay(CalendarDay day) {
    	Log.d(TAG, "Chosen day: " + day.date);
    	this.day_click_callback.clickDay(day);
    }
        
    // ==========================================================
    void touchDay(CalendarDay day) {
    	Log.d(TAG, "Chosen day: " + day.date);
    	this.day_touch_callback.clickDay(day);
        	invalidate();
    }
}
