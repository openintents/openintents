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
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.os.SystemClock;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;

public class FlingableMonthView extends View {

	static final String TAG = "FlingableMonthView";

	
	public static final long MILLISECONDS_PER_DAY = 1000L*60*60*24;
	public static final int DAYS_PER_WEEK = 7;
	static final long MILLISECONDS_PER_WEEK = DAYS_PER_WEEK*MILLISECONDS_PER_DAY;
	static final int MONTHS_PER_YEAR = 12;

	static final SimpleDateFormat FULL_MONTH_FORMATTER = new SimpleDateFormat("MMMM");

	static final String MONTH_WATERMARK_FONT_PATH = "BerlinSmallCaps.ttf";
    
    
    Calendar month_calendar = new GregorianCalendar();
    Date highlighted_day = null;
    List<SimpleEvent> sorted_events;

    // Cached computed values
    int spanned_weeks;
    
    // Callbacks
    MonthUpdateCallback month_update_callback = null;
    OnDateUpdateListener day_click_callback, day_touch_callback, scroll_callback;
    
    // Animation-related values
    float MONTH_TEXT_FADER_MILLISECONDS = 500;
	TimedAnimation month_text_fader = null;
	

	// Longpress animation
    boolean is_holding_longpress = false;
    long longpress_start_time;
    
	

//  float SNAP_BACK_ANIMATION_ACCELERATION = 100;	// 100 px/s^2
	float SNAP_BACK_MILLISECONDS = 500;
	TimedAnimation snap_back_animation = null;
	
    float VERTICAL_SCROLL_TOLERANCE = 75;
    
	float snap_back_start_offset = 0;
    float vertical_offset = 0;
    
    
    
    
    // FLINGING STATE
    static final float MINIMUM_SUSTAINED_FLINGING_VELOCITY = 400;	// in px/sec
    static final float FLINGING_DECELERATION = 750;	// in px/sec/sec
    float current_flinging_velocity = 0;
    long last_frame_time_for_fling;
    
    
    CalendarRenderer calendar_drawing;

    // ========================================================================
    public FlingableMonthView(Context context, AttributeSet attrs) {
    	super(context, attrs);

    	setMonth(new GregorianCalendar());
    	calendar_drawing = new CalendarRenderer(context);
    	

        final GestureDetector gestureDetector = new GestureDetector(new MonthGestureDetector());
        setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
	        	
	        	if (event.getAction() == MotionEvent.ACTION_UP) {

	            	is_holding_longpress = false;
	        		
	        		if (Math.abs(vertical_offset) > 0) {
	        			
	        			// TODO
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
    }

    // ========================================================================
    public Date getHighlightedDay() {
    	return this.highlighted_day;
    }
    
    // ========================================================================
    public void setMonthAndHighlight(Date date) {
    	Calendar cal = new GregorianCalendar();
		cal.setTime(date);
    	int day = cal.get(Calendar.DATE);
    	FlingableMonthView.setCalendarToFirstDayOfMonth(cal);
    	if (!cal.equals(this.month_calendar))
    		setMonth(cal);

		Calendar cal2 = (Calendar) cal.clone();
    	// Set the precision of the calendar to the Day
		cal2.set(Calendar.DATE, day);
        highlightDay(cal2.getTime());
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
    abstract class ViewportVisitor {
    	
    	Canvas canvas;
    	void setCanvas(Canvas canvas) {
    		this.canvas = canvas;
    	}
    	
    	abstract void visitViewport(RectF daybox, SimpleCalendarDay child);
    }
    
    // ========================================================================
    public void setOnDayClickListener(OnDateUpdateListener callback) {
    	this.day_click_callback = callback;
    }
    
    // ========================================================================
    public void setOnScrollListener(OnDateUpdateListener callback) {
    	this.scroll_callback = callback;
    }

    // ========================================================================
    public void setOnDayTouchListener(OnDateUpdateListener callback) {
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
    	
    	this.highlighted_day = null;
    	
    	this.month_calendar.setTime(calendar.getTime());
    	setCalendarToFirstDayOfMonth(this.month_calendar);
    	this.vertical_offset = 0;
    	this.spanned_weeks = calcSpannedWeeksForMonth(this.month_calendar);
    	
        if (this.month_update_callback != null) {
        	this.month_update_callback.updateMonth(this.month_calendar);
        }
        
        if (this.day_touch_callback != null) {
        	this.day_touch_callback.updateDate(null);
        }
    	
        this.month_text_fader = new TimedAnimation(SystemClock.uptimeMillis(), MONTH_TEXT_FADER_MILLISECONDS);
    	invalidate();
    }

    // ========================================================================
    public void highlightDay(Date date) {

    	this.highlighted_day = date;
    	this.day_touch_callback.updateDate(date);
    	invalidate();
    }
    
    // ========================================================================
    public void setMonthAndEvents(Calendar calendar, List<SimpleEvent> sorted_events) {
    	this.sorted_events = sorted_events;
    	setMonth(calendar);
    }

    // ========================================================================
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        this.calendar_drawing.draw(canvas);
    }

    // ========================================================================
    /** Rolls the date back to the beginning of the week of the first week of the month.
     * Expects the calendar date to already be set to the first day of the month. */
    static void setMonthWeekBeginning(Calendar calendar) {
    	while (true) {
    		if (calendar.get(Calendar.DAY_OF_WEEK) == calendar.getFirstDayOfWeek())
    			break;
    		calendar.add(Calendar.DAY_OF_MONTH, -1);
    	}
    }
    
    // ========================================================================
    static int getNextMonthIndex(Calendar source_calendar, Calendar dummy_calendar) {
    	// Get index of following month
    	dummy_calendar.setTime(source_calendar.getTime());
    	dummy_calendar.add(Calendar.MONTH, 1);
    	return dummy_calendar.get(Calendar.MONTH);
    }
    
    // ========================================================================
    public static void setCalendarToFirstDayOfMonth(Calendar calendar) {

    	int month = calendar.get(Calendar.MONTH);
    	int year = calendar.get(Calendar.YEAR);
    	calendar.clear();
    	calendar.set(year, month, calendar.getMinimum(Calendar.DAY_OF_MONTH));
    }
    
    // ========================================================================
    static int calcSpannedWeeksForMonth(Calendar calendar) {

    	// Set working calendar to first day of the month.
    	Calendar working_calendar = (Calendar) calendar.clone();
    	setMonthWeekBeginning(working_calendar);

    	// Count the weeks spanned by this month
    	int spanned_weeks = 0;
    	int next_month_index = getNextMonthIndex(calendar, new GregorianCalendar());
    	while (working_calendar.get(Calendar.MONTH) != next_month_index) {
    		working_calendar.add(Calendar.DAY_OF_MONTH, DAYS_PER_WEEK);
    		spanned_weeks++;
    	}
    	
    	return spanned_weeks;
    }

    // ========================================================================
    class CalendarRenderer {

    	TextPaint day_tile_paint;
    	TextPaint month_watermark_text_paint;
        Resources resources;
        FontMetrics day_tile_paint_font_metrics;

    	float max_month_width;

    	float horizontal_spacing = 2;
        float vertical_spacing = 2;
        
    	

    	Calendar dummy_calendar = new GregorianCalendar();
        
    	CalendarRenderer(Context context) {
    		
    		this.resources = context.getResources();
    		
        	this.day_tile_paint = new TextPaint();
        	this.day_tile_paint.setAntiAlias(true);
        	this.day_tile_paint.setColor(Color.WHITE);

        	this.day_tile_paint_font_metrics = this.day_tile_paint.getFontMetrics();
        	
    		this.month_watermark_text_paint = new TextPaint();
    		

    		Typeface face = Typeface.createFromAsset(context.getAssets(), MONTH_WATERMARK_FONT_PATH);
    		this.month_watermark_text_paint.setTypeface(face);
            this.month_watermark_text_paint.setAntiAlias(true);
    		this.month_watermark_text_paint.setColor(this.resources.getColor(R.color.month_watermark_text));
    		this.month_watermark_text_paint.setTextAlign(Align.RIGHT);

        	this.max_month_width = getMaxMonthWidth(this.month_watermark_text_paint);
    	}
        
    	Date dummy_date = new Date();
        // ========================================================================
        Date getScrollOffsetDate() {
            float height_per_week = getDayBoxHeight() + this.vertical_spacing;
            float weeks_offset = vertical_offset/height_per_week;
            long milliseconds_offset = (long) (MILLISECONDS_PER_WEEK*weeks_offset);
            
            this.dummy_date.setTime(month_calendar.getTimeInMillis() - milliseconds_offset);
            return this.dummy_date;
        }
        
        // ========================================================================
        /** Calculate vertical dimensions */
        float getDayBoxHeight() {

        	int usable_height = getHeight() - (getPaddingTop() + getPaddingBottom());
            float day_box_height = (usable_height - (spanned_weeks - 1)*this.vertical_spacing) / spanned_weeks;
            return day_box_height;
        }

        // ========================================================================
        ViewportVisitor day_event_drawing_visitor = new ViewportVisitor() {
			@Override
			public void visitViewport(RectF daybox, SimpleCalendarDay child) {
	            this.canvas.save();
	            this.canvas.translate(daybox.left, daybox.top);
	            drawDayEvents(canvas, daybox, child);
	            this.canvas.restore();
			}
        };
        
        // ========================================================================
        ViewportVisitor day_background_drawing_visitor = new ViewportVisitor() {
			@Override
			public void visitViewport(RectF daybox, SimpleCalendarDay child) {
				this.canvas.save();
				this.canvas.translate(daybox.left, daybox.top);
	            drawDayHolder(canvas, daybox, child);
	            this.canvas.restore();
			}
        };
        
        // ========================================================================
    	void draw(final Canvas canvas) {

            canvas.save();
            canvas.translate(0, vertical_offset);
            
            // Draw all of the visible days
            this.day_background_drawing_visitor.setCanvas(canvas);
            visitDayViewports(this.day_background_drawing_visitor);
            	
            
            drawMonthWatermarkText(canvas);
            
            // Draw all of the visible days
            this.day_event_drawing_visitor.setCanvas(canvas);
            visitDayViewports(this.day_event_drawing_visitor);
            
            canvas.restore();
            
            handleViewAnimation(canvas);
        }
    	
        // ========================================================================
        void handleViewAnimation(Canvas canvas) {
            
            // We animate the view by repeatedly invalidating it.
            if (is_holding_longpress) {
            	invalidate();
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
                
            	
            	if (current_flinging_velocity == 0) {

            		this.dummy_calendar.setTime(getScrollOffsetDate());
            		setCalendarToFirstDayOfMonth(this.dummy_calendar);
            		setMonth(this.dummy_calendar);
            		
            	} else {
    	            // Velocity is in units of pixels/second.
    	        	float position_delta = current_flinging_velocity*fling_frame_millis_delta/1000;
    	        	vertical_offset += position_delta;
    	        	
    	        	// TODO Use this for debugging
//    	        	Log.d(TAG, "Velocity: " + this.current_flinging_velocity);
    	
    	
    	        	if (scroll_callback != null)
    	        		scroll_callback.updateDate(getScrollOffsetDate());
    	        	
            	
    	        	invalidate();
            	}
            	
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
        void drawMonthWatermarkText(Canvas canvas) {

    		// Set the scale to the widest month    	
        	float scale = getHeight() / this.max_month_width;

        	String month_string = FULL_MONTH_FORMATTER.format(month_calendar.getTime());

            long now = SystemClock.uptimeMillis();
            float fraction = 1;
            if (month_text_fader != null)
            	fraction = month_text_fader.getFraction(now);

        	int target_color = this.resources.getColor(R.color.month_watermark_text);
        	int text_color = interpolateColor(Color.WHITE, target_color, fraction);
        	this.month_watermark_text_paint.setColor(text_color);
        	
        	
    		canvas.save();
    		canvas.translate(getWidth(), 0);
    		canvas.rotate(-90);
    		canvas.scale(scale, scale);
    		// XXX The month names look more stylish if we align
    		// the baseline with the edge of the screen, but this can cut
    		// off the capital "J"s and the "y"s.
//    		canvas.translate(0, -month_bg_paint.getFontMetrics().descent);

    		canvas.drawText(month_string, 0, 0, this.month_watermark_text_paint);
    		canvas.restore();	
        }

        // ========================================================================
        protected void drawDayEvents(Canvas canvas, RectF daybox, SimpleCalendarDay day) {
            float usable_size = Math.min(daybox.width(), daybox.height());
            drawEventCount(canvas, daybox, day, usable_size);
            
        	this.dummy_calendar.setTime(day.getDate());
        	
        	int daycal_month_idx = this.dummy_calendar.get(Calendar.MONTH);
        	boolean month_active = month_calendar.get(Calendar.MONTH) == daycal_month_idx;
            drawCornerBox(canvas, daybox, day, usable_size, month_active);
        }
        
        // ========================================================================
        protected void drawDayHolder(Canvas canvas, RectF daybox, SimpleCalendarDay day) {

        	this.dummy_calendar.setTime(day.getDate());

//        	int daycal_month_idx = daycal.get(Calendar.MONTH);
//        	boolean month_active = this.month_calendar.get(Calendar.MONTH) == daycal_month_idx;
//        	boolean daycal_month_odd = daycal_month_idx % 2 != 0;
        	
        	int months_away = getMonthDifference(month_calendar, this.dummy_calendar);
        	boolean month_active = months_away == 0;
        	boolean daycal_month_even = months_away % 2 == 0;
        	
            boolean day_highlighted = day.getDate().equals(highlighted_day);
            
            int background_color = day_highlighted ?
            		this.resources.getColor(
            				month_active ?
            						R.color.calendar_date_background_active_selected
            						: (daycal_month_even ? R.color.calendar_date_background_passive_odd_selected : R.color.calendar_date_background_passive_even_selected))
            		: this.resources.getColor(
            				month_active ?
            						R.color.calendar_date_background_active
            						: (daycal_month_even ? R.color.calendar_date_background_passive_odd : R.color.calendar_date_background_passive_even)				
            		);
            				
            if (is_holding_longpress && day.getDate().equals(highlighted_day)) {

            	long now = SystemClock.uptimeMillis();
            	
            	// Before you start to animate, check whether the tap time has been exceeded.
            	long tap_timeout = ViewConfiguration.getTapTimeout();
            	if (now > longpress_start_time + tap_timeout) {
            		
    	        	float alpha = (now - (longpress_start_time + tap_timeout)) / (float) ViewConfiguration.getLongPressTimeout();
    	
    	        	alpha = Math.min(1, alpha);
    	        	
    	        	int target_color = resources.getColor(R.color.calendar_date_background_longpress);
    	        	background_color = interpolateColor(background_color, target_color, alpha);
            	}
            }
            				
    		// Draw the background
            this.day_tile_paint.setColor(background_color);
            canvas.drawRect(0, 0, daybox.width(), daybox.height(), this.day_tile_paint);
        }

        // ========================================================================
        void drawEventCount(Canvas canvas, RectF daybox, SimpleCalendarDay calendar_day, float usable_size) {

            canvas.save();
            canvas.translate(daybox.width()/2f, daybox.height()/2f);
            
    		
    		int event_count = calendar_day.getEventCount();

    		if (event_count > 0) {

    	        // Draw decorative circle
    	        day_tile_paint.setColor(Color.CYAN);
    	        day_tile_paint.setAlpha(0xff/2);
    			canvas.drawCircle(0, 0, usable_size/3, day_tile_paint);
    			
    			
    			// Draw the number of events inside the circle
    	        float corner_box_side = usable_size/2f;
    	        this.day_tile_paint.setTextSize(corner_box_side*0.8f);
    			float text_height = this.day_tile_paint_font_metrics.ascent + this.day_tile_paint_font_metrics.descent;
    	
    	        int event_count_number = this.resources.getColor(R.color.event_count_number);
    	        this.day_tile_paint.setColor(event_count_number);
    			String text = Integer.toString( event_count );
    			this.day_tile_paint.setTextAlign(Align.CENTER);
    			canvas.drawText(text, 0, -text_height/2, this.day_tile_paint);
    		}
    		
    		canvas.restore();
        }
        
        // ========================================================================
        void drawCornerBox(Canvas canvas, RectF viewport, SimpleCalendarDay calendar_day, float usable_size, boolean month_active) {
        	
            float corner_box_side = usable_size/2f;

            int cornerbox_color = this.resources.getColor(R.color.cornerbox_color);
            this.day_tile_paint.setColor(cornerbox_color);
            canvas.drawRect(0, 0, corner_box_side, corner_box_side, this.day_tile_paint);
            canvas.save();
            canvas.translate(corner_box_side/2f, corner_box_side/2f);
            
    		this.dummy_calendar.setTime(calendar_day.getDate());
    		String text = Integer.toString( this.dummy_calendar.get(Calendar.DAY_OF_MONTH) );

    		this.day_tile_paint.setTextSize(corner_box_side*0.8f);
    		float text_height = this.day_tile_paint_font_metrics.ascent + this.day_tile_paint_font_metrics.descent;

            int text_color = this.resources.getColor(month_active ? R.color.calendar_date_number : R.color.calendar_date_number_passive);
            this.day_tile_paint.setColor(text_color);
            
            this.day_tile_paint.setTextAlign(Align.CENTER);
    		canvas.drawText(text, 0, -text_height/2, this.day_tile_paint);
    		
    		canvas.restore();
        }


        // ========================================================================
        RectF dummy_rect = new RectF();
    	SimpleCalendarDay dummy_scd = new SimpleCalendarDay();
    	Calendar day_iterator_calendar = new GregorianCalendar();
        
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
        	this.day_iterator_calendar.setTime(month_calendar.getTime());
        	setMonthWeekBeginning(this.day_iterator_calendar);

        	// Reposition calendar according to the vertical scroll offset
        	int weeks_offset = (int) Math.floor(-vertical_offset/height_per_week);
        	this.day_iterator_calendar.add(Calendar.DAY_OF_MONTH, weeks_offset*DAYS_PER_WEEK);

    		// Skip all events before the given calendar date
            int event_idx=0;
            while (event_idx < sorted_events.size() && sorted_events.get(event_idx).timestamp.before(this.day_iterator_calendar.getTime())) event_idx++;


            // The "<=" as opposed to the typical "<" allows the entire screen to be
            // covered while scrolling.
            for (int i=0; i <= spanned_weeks; i++) {

                float top = getPaddingTop() + height_per_week * (i + weeks_offset);
            	
                for (int j=0; j < DAYS_PER_WEEK; j++) {

                	this.dummy_scd.reset(this.day_iterator_calendar.getTime());

                	// We advance the calendar to the next day *after* recording
                	// the date for the current day.
                	this.day_iterator_calendar.add(Calendar.DAY_OF_MONTH, 1);
            		
                    // Consume all events up until the next day
                    while (event_idx < sorted_events.size() && sorted_events.get(event_idx).timestamp.before(this.day_iterator_calendar.getTime())) {
                    	this.dummy_scd.incrementEventCount();
//                    	child.day_events.add(this.sorted_events.get(event_idx));
                    	event_idx++;
                    }

                    float left = getPaddingLeft() + ((width_per_day) * j);
                    this.dummy_rect.set(left, top, left + day_box_width, top + day_box_height);
                    
                    visitor.visitViewport(this.dummy_rect, this.dummy_scd);
                }
            }
        }
    }

    // ========================================================================
    int getMonthDifference(Calendar cal1, Calendar cal2) {
    	
    	int y1 = cal1.get(Calendar.YEAR);
    	int y2 = cal2.get(Calendar.YEAR);
    	
    	int m1 = cal1.get(Calendar.MONTH);
    	int m2 = cal2.get(Calendar.MONTH);

    	return (y2 - y1) * MONTHS_PER_YEAR + (m2 - m1);
    }
    
    // ========================================================================
    Date getDayFromPoint(PointF point) {

    	// Use horizontal position to determine weekday
        int usable_width = getWidth() - (getPaddingLeft() + getPaddingRight());
    	int weekday_index = (int) (DAYS_PER_WEEK*(point.x - getPaddingLeft()) / usable_width);
    	
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
	int interpolateColor(int src_color, int dst_color, float fraction) {
		int red = interpolateInt(Color.red(src_color), Color.red(dst_color), fraction);
		int green = interpolateInt(Color.green(src_color), Color.green(dst_color), fraction);
		int blue = interpolateInt(Color.blue(src_color), Color.blue(dst_color), fraction);
		int alpha = interpolateInt(Color.alpha(src_color), Color.alpha(dst_color), fraction);
		return Color.argb(alpha, red, green, blue);
	}

    // ========================================================================
	void moveMonth(boolean forward) {
        this.month_calendar.add(Calendar.MONTH, forward ? 1 : -1);
        setMonth(this.month_calendar);
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
        	highlightDay(highlighted_day);

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
	        		scroll_callback.updateDate(calendar_drawing.getScrollOffsetDate());
	        	
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
    public void executeDay(Date date) {
    	Log.d(TAG, "Chosen day: " + date);
    	this.day_click_callback.updateDate(date);
    }
}
