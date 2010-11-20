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

package org.openintents.calendarpicker.view;


import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.activity.PeriodBrowsingActivity.DailyEventMaximums;
import org.openintents.calendarpicker.activity.prefs.CalendarDisplayPreferences;
import org.openintents.calendarpicker.container.CalendarDayAggregator;
import org.openintents.calendarpicker.container.SimpleEvent;
import org.openintents.calendarpicker.contract.CalendarPickerConstants;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DrawFilter;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.os.SystemClock;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.ContextMenu;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.GestureDetector.SimpleOnGestureListener;

/**
 * Animation process:
 * When the user "scrolls" the calendar vertically then releases,
 * the calendar will "snap" into place at the nearest month.
 * The snapping animation begins on ACTION_UP, and once the animation
 * is complete (and the calendar is at rest), the active month
 * will be updated (using setMonth()).
 * 
 * The post-scroll snapping animation starts from zero velocity.
 * It accelerates towards the snap target and stops suddenly
 * when reached.
 * The flinging animation on the other hand, must take initial
 * velocity into account.
 * 
 * The "snapping" phase acts different rules than the
 * "flinging" phase to avoid oscillations around the
 * snap target.  A separate variable for "snapping" velocity
 * takes on the value of the "flinging" velocity when the deceleration
 * threshold is reached.
 * 
 * @author kostmo
 *
 */


public class FlingableMonthView extends View implements SharedPreferences.OnSharedPreferenceChangeListener {

	static final String TAG = "FlingableMonthView";

	
	public static final long MILLISECONDS_PER_DAY = 1000L*60*60*24;
	public static final int DAYS_PER_WEEK = 7;
	static final long MILLISECONDS_PER_WEEK = DAYS_PER_WEEK*MILLISECONDS_PER_DAY;
	static final int MONTHS_PER_YEAR = 12;

	static final SimpleDateFormat FULL_MONTH_NAME_FORMATTER = new SimpleDateFormat("MMMM");

    
	
    float VERTICAL_SCROLL_TOLERANCE = 75;
    
    
    float vertical_offset = 0;
    // The vertical offset is relative to the Sunday (upper-left corner)
    // before the 1st of the active month.
    Date active_month_northwest_corner_date = new Date();
    
    Calendar active_month_calendar = new GregorianCalendar();
    Date highlighted_day = null;
    List<SimpleEvent> sorted_events = new ArrayList<SimpleEvent>();

    // Cached computed values
    int spanned_weeks;
    
    // Callbacks
    MonthUpdateCallback month_update_callback = null;
    OnDateUpdateListener day_click_callback, day_touch_callback, scroll_callback, transient_callback;

	// Longpress animation
    boolean is_holding_longpress = false;
    long longpress_start_time;
	

    // ========================================================================
    public CalendarRenderer calendar_drawing;
	public void setColors(int[] stops) {
		if (stops != null && stops.length > 1)
			this.calendar_drawing.color_mapping.color_stops = stops;
		
		invalidate();
	}
	
    // ========================================================================
    public static class ColorMappingConfiguration {

    	enum ColorMappingSource {
    		EVENT_COUNT, EXTRA_QUANTITY
    	}

        public boolean showing_monthwide_daily_maximums = false;
        DailyEventMaximums monthwide_daily_maximums = new DailyEventMaximums();


    	public boolean enabled = false;
        
        
        DailyEventMaximums overall_daily_maximums;
        public void setMaximums(DailyEventMaximums maximums) {
        	this.overall_daily_maximums = maximums;
        }
    	

    	ColorMappingSource mapping_source = ColorMappingSource.EVENT_COUNT;
    	public int extra_quantity_index = 0;
    	public void setColormapSource(int extra_quantity_index) {
    		if (extra_quantity_index < CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES.length) {
        		if (extra_quantity_index >= 0) {
        			this.mapping_source = ColorMappingSource.EXTRA_QUANTITY;
        			this.extra_quantity_index = extra_quantity_index;
        		} else
        			this.mapping_source = ColorMappingSource.EVENT_COUNT;
    		} else
    			Log.e(TAG, "Color mapping source is out of range: " + extra_quantity_index);
    	}
    	
    	

    	int[] color_stops = new int[] {Color.BLACK, Color.MAGENTA};
    	
    	/** Assign equidistant color stops.  Validates the input */
    	private int interpolateColorStops(float fraction) {
    		
    		int max_color_index = this.color_stops.length - 1;
    		float high_fractional_value = fraction * max_color_index;
    		int low_index = (int) high_fractional_value;
    		int high_index = (int) Math.ceil(fraction * max_color_index);
    		
    		float partial_fraction = high_fractional_value - low_index;
    		
    		return interpolateColor(this.color_stops[low_index], this.color_stops[high_index], partial_fraction);
    	}

    	int getTileColor(CalendarDayAggregator day) {
    		DailyEventMaximums maxes = this.showing_monthwide_daily_maximums ?
    				this.monthwide_daily_maximums : this.overall_daily_maximums;

    		float fraction = 0;
    		switch (this.mapping_source) {
    		case EVENT_COUNT:
    			fraction = maxes.max_event_count_per_day == 0 ? 0 : day.getEventCount() / (float) maxes.max_event_count_per_day;
    			break;
    		case EXTRA_QUANTITY:
    			float max = maxes.max_quantities_per_day[this.extra_quantity_index];
    			fraction = max == 0 ? 0 : day.getAggregateQuantity(this.extra_quantity_index) / max;
    			break;
    		}
    		
    		return interpolateColorStops(fraction);
    	}
    }
    
    // ========================================================================
    public FlingableMonthView(Context context, AttributeSet attrs) {
    	super(context, attrs);

    	this.calendar_drawing = new CalendarRenderer(context);
    	
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.FlingableMonthView);
    	this.calendar_drawing.horizontal_spacing = a.getDimensionPixelOffset(R.styleable.FlingableMonthView_horizontal_spacing, 1);
    	this.calendar_drawing.vertical_spacing = a.getDimensionPixelOffset(R.styleable.FlingableMonthView_vertical_spacing, 1);
    	a.recycle();
    	
    	setMonth(new GregorianCalendar());
    	

        final GestureDetector gestureDetector = new GestureDetector(new MonthGestureDetector());
        setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
	        	
	        	if (event.getAction() == MotionEvent.ACTION_UP) {

	            	is_holding_longpress = false;
	        		
	        		if (Math.abs(vertical_offset) > 0) {
	        			calendar_drawing.beginSnappingBackAnimation(0);
	        		}
	        	} else if (event.getAction() == MotionEvent.ACTION_DOWN) {
	        		calendar_drawing.is_snapping = false;
	        	}
	        	
				
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
			}
        });
        
        
        
        SharedPreferences prefs = context.getSharedPreferences(CalendarDisplayPreferences.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
		prefs.registerOnSharedPreferenceChangeListener(this);
		
		this.calendar_drawing.dark_watermark = prefs.getBoolean(CalendarDisplayPreferences.PREFKEY_DARK_WATERMARK, false);
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
    	if (!cal.equals(this.active_month_calendar))
    		setMonth(cal);

		Calendar cal2 = (Calendar) cal.clone();
    	// Set the precision of the calendar to the Day
		cal2.set(Calendar.DATE, day);
        highlightDay(cal2.getTime());
    }

    // ========================================================================
	public Calendar getCalendar() {
		return this.active_month_calendar;
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
    	
    	abstract void visitViewport(RectF daybox, CalendarDayAggregator child);
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
    public void setTransientListener(OnDateUpdateListener callback) {
    	this.transient_callback = callback;
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
     * This function gets called infrequently enough (less than once per frame)
     * so it is OK to do memory allocations within.
     */
    
    public void setMonth(Calendar calendar) {
    	
    	this.highlighted_day = null;
    	
    	this.active_month_calendar.setTime(calendar.getTime());
    	setCalendarToFirstDayOfMonth(this.active_month_calendar);
    	
    	Calendar temp_calendar = new GregorianCalendar();
    	temp_calendar.setTime(this.active_month_calendar.getTime());
    	setMonthWeekBeginning(temp_calendar);
    	this.active_month_northwest_corner_date.setTime(temp_calendar.getTimeInMillis());
    	this.vertical_offset = 0;
    	

    	// The order of these two functions is vital.
    	this.spanned_weeks = calcSpannedWeeksForMonth(this.active_month_calendar);
    	this.calendar_drawing.prepareForMonth();

    	
    	
    	
        if (this.month_update_callback != null) {
        	this.month_update_callback.updateMonth(this.active_month_calendar);
        }
        
        if (this.day_touch_callback != null) {
        	this.day_touch_callback.updateDate(null);
        }
    	
        this.calendar_drawing.month_text_fader = new TimedAnimation(SystemClock.uptimeMillis(), CalendarRenderer.MONTH_TEXT_FADER_MILLISECONDS);
    	invalidate();
    }

    // ========================================================================
    public void highlightDay(Date date) {

    	this.highlighted_day = date;
    	this.day_touch_callback.updateDate(date);
    	invalidate();
    }
    
    // ========================================================================
    public void setEvents(List<SimpleEvent> sorted_events) {
    	this.sorted_events = sorted_events;
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
    	while (calendar.get(Calendar.DAY_OF_WEEK) != calendar.getFirstDayOfWeek())
    		calendar.add(Calendar.DAY_OF_MONTH, -1);
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
    public void setVisualizeQuantities(boolean should_visualize) {
    	this.calendar_drawing.color_mapping.enabled = should_visualize;
    }
    
    // ========================================================================
    /** Handles all view rendering responsibilities, including render-specific data */
    public class CalendarRenderer {
    	
    	static final String MONTH_WATERMARK_FONT_PATH = "BerlinSmallCaps.ttf";

    	Paint day_tile_paint, month_watermark_text_paint, triangle_paint;
    	
        Resources resources;
        FontMetrics day_tile_paint_font_metrics;

    	float max_month_width;

    	float horizontal_spacing = 1;
        float vertical_spacing = 1;
        
        final static int CHECKER_PATTERN_TILE_SIZE = 8;
        
    	public boolean enable_event_count = true;

        // Animation-related values
        static final float MONTH_TEXT_FADER_MILLISECONDS = 500;
    	TimedAnimation month_text_fader = null;
        
    	

        public ColorMappingConfiguration color_mapping = new ColorMappingConfiguration();
        
        // FLINGING STATE
        static final float MINIMUM_SUSTAINED_FLINGING_VELOCITY = 400;	// in px/sec
        static final float FLINGING_DECELERATION = 750;	// in px/sec/sec
        float current_flinging_velocity = 0;
        long last_frame_time_for_fling;
    	
    	
        
        
        // SNAPPING STATE
        static final float SNAPPING_ACCELERATION = 750;	// in px/sec/sec
        float current_snapping_velocity = 0;	// in px/sec
    	boolean is_snapping = false;
    	long last_frame_time_for_snap;
    	Calendar snap_target_month = new GregorianCalendar();
    	Calendar snap_target_day = new GregorianCalendar();
    	float snap_target_offset = 0;
        
        

    	Calendar dummy_calendar = new GregorianCalendar();
    	Date dummy_date = new Date();

    	
    	
    	
    	
    	
    	private DrawFilter mFastDF = new PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG |
    			Paint.DITHER_FLAG,
    			0);

    	private Shader mShader1 = new BitmapShader(makeCheckeredBitmap(), Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
    	private Bitmap makeCheckeredBitmap() {
    		int tile_size = CHECKER_PATTERN_TILE_SIZE;
    		int dimension = tile_size*2;
    		Bitmap bm = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888);
    		Canvas c = new Canvas(bm);
    		c.drawColor(Color.TRANSPARENT);
    		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    		paint.setColor(Color.BLACK);

    		for (int i=0; i<2; i++)
    			c.drawRect(tile_size*i, tile_size*i, tile_size*(i+1), tile_size*(i+1), paint);
    		return bm;
    	}



    	
    	
    	
    	
    	
    	
    	int color_month_watermark_text_dark,
    	color_month_watermark_text_light,
    	color_calendar_date_background_longpress,
    	color_event_count_background,
    	color_event_count_number,
    	color_cornerbox_background_color,
    	color_cornerbox_date_number,
    	color_cornerbox_date_number_passive;

    	ColorStateList month_color_states;
    	
    	boolean dark_watermark = false;
        // ========================================================================
    	/** We initialize the colors once here, as opposed to looking them up from
    	 * the Resources object every frame (or worse, once per calendar day).
    	 */
    	void initColors(Resources resources) {
    		this.color_month_watermark_text_dark = resources.getColor(R.color.month_watermark_text_dark);
    		this.color_month_watermark_text_light = resources.getColor(R.color.month_watermark_text_light);
    		this.color_calendar_date_background_longpress = resources.getColor(R.color.calendar_date_background_longpress);
    		this.color_event_count_background = resources.getColor(R.color.event_count_background);
    		this.color_event_count_number = resources.getColor(R.color.event_count_number);
    		this.color_cornerbox_background_color = resources.getColor(R.color.cornerbox_background_color);
    		this.color_cornerbox_date_number = resources.getColor(R.color.cornerbox_date_number);
    		this.color_cornerbox_date_number_passive = resources.getColor(R.color.cornerbox_date_number_passive);
    		
    		this.month_color_states = resources.getColorStateList(R.color.days);
    	}
    	

        boolean transparency_enabled = CalendarDisplayPreferences.DEFAULT_ENABLE_TRANSPARENCY;

        // ========================================================================
    	CalendarRenderer(Context context) {
    		
            SharedPreferences prefs = context.getSharedPreferences(CalendarDisplayPreferences.SHARED_PREFS_NAME, Context.MODE_PRIVATE);
        	this.transparency_enabled = prefs.getBoolean(
        			CalendarDisplayPreferences.PREFKEY_ENABLE_TRANSPARENCY, CalendarDisplayPreferences.DEFAULT_ENABLE_TRANSPARENCY);
        	
    		this.resources = context.getResources();
    		initColors(this.resources);

    		
    		
        	this.triangle_paint = new Paint();
        	this.triangle_paint.setShader(mShader1);
    		
    		
    		
        	this.day_tile_paint = new TextPaint();
        	this.day_tile_paint.setAntiAlias(true);
        	this.day_tile_paint.setColor(Color.WHITE);
        	
    		this.month_watermark_text_paint = new TextPaint();
    		Typeface face = Typeface.createFromAsset(context.getAssets(), MONTH_WATERMARK_FONT_PATH);
    		this.month_watermark_text_paint.setTypeface(face);
            this.month_watermark_text_paint.setAntiAlias(true);
    		this.month_watermark_text_paint.setColor(color_month_watermark_text_dark);
    		this.month_watermark_text_paint.setTextAlign(Align.RIGHT);

        	this.max_month_width = getMaxMonthWidth(this.month_watermark_text_paint);
        	
        	
        	this.northwest_triangle.setFillType(Path.FillType.EVEN_ODD);
        	this.southeast_triangle.setFillType(Path.FillType.EVEN_ODD);
    	}

        // ========================================================================
		// Identifies the closest snap_target
    	Date dummy_earlier_first_of_month = new Date();
    	Date dummy_earlier_week_beginning = new Date();
    	Date dummy_later_first_of_month = new Date();
    	Date dummy_later_week_beginning = new Date();
    	Date getNearestMonthBeginningDate() {
    		Date scroll_offset_date = getOffsetDateFromOffsetPixels();
    		this.dummy_calendar.setTime(scroll_offset_date);
    		
    		setCalendarToFirstDayOfMonth(this.dummy_calendar);
    		dummy_earlier_first_of_month.setTime(this.dummy_calendar.getTimeInMillis());
    		setMonthWeekBeginning(this.dummy_calendar);
    		dummy_earlier_week_beginning.setTime(this.dummy_calendar.getTimeInMillis());
    		long backwards_millis_delta = scroll_offset_date.getTime() - dummy_earlier_week_beginning.getTime();

    		this.dummy_calendar.setTime(dummy_earlier_first_of_month);
    		this.dummy_calendar.add(Calendar.MONTH, 1);
    		dummy_later_first_of_month.setTime(this.dummy_calendar.getTimeInMillis());
    		setMonthWeekBeginning(this.dummy_calendar);
    		dummy_later_week_beginning.setTime(this.dummy_calendar.getTimeInMillis());
    		long forwards_millis_delta = dummy_later_week_beginning.getTime() - scroll_offset_date.getTime();
    		
    		
    		if (backwards_millis_delta < forwards_millis_delta) {
        		return dummy_earlier_first_of_month;
    		} else {
        		return dummy_later_first_of_month;
    		}
    	}
    	
        // ========================================================================
    	void beginSnappingBackAnimation(float initial_velocity) {
    		this.is_snapping = true;
    		this.current_snapping_velocity = initial_velocity;
    		this.last_frame_time_for_snap = SystemClock.uptimeMillis();

    		
    		// Identify the closest snap_target
    		Date scroll_offset_date = getOffsetDateFromOffsetPixels();
    		this.dummy_calendar.setTime(scroll_offset_date);
    		
    		setCalendarToFirstDayOfMonth(this.dummy_calendar);
    		Date earlier_first_of_month = this.dummy_calendar.getTime();
    		setMonthWeekBeginning(this.dummy_calendar);
    		Date earlier_week_beginning = this.dummy_calendar.getTime();
    		long backwards_millis_delta = scroll_offset_date.getTime() - earlier_week_beginning.getTime();

    		this.dummy_calendar.setTime(earlier_first_of_month);
    		this.dummy_calendar.add(Calendar.MONTH, 1);
    		Date later_first_of_month = this.dummy_calendar.getTime();
    		setMonthWeekBeginning(this.dummy_calendar);
    		Date later_week_beginning = this.dummy_calendar.getTime();
    		long forwards_millis_delta = later_week_beginning.getTime() - scroll_offset_date.getTime();
    		
    		
    		if (backwards_millis_delta < forwards_millis_delta) {
        		this.snap_target_month.setTime(earlier_first_of_month);
        		this.snap_target_day.setTime(earlier_week_beginning);
    		} else {
    			this.snap_target_month.setTime(later_first_of_month);
        		this.snap_target_day.setTime(later_week_beginning);
    		}
    		
//    		Log.d(TAG, "Target snap month: " + FULL_MONTH_NAME_FORMATTER.format(this.snap_target_month.getTime()));    		

    		this.snap_target_offset = getOffsetPixelsForDate(
    				this.snap_target_day.getTime());
    		
    		// Set the redraw chain in motion
			invalidate();
    	}
        
        // ========================================================================
    	private float getMaxMonthWidth(Paint paint) {
    	
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
        Date getOffsetDateFromOffsetPixels() {
            float height_per_week = getDayBoxHeight() + this.vertical_spacing;
            float weeks_offset = vertical_offset/height_per_week;
            long milliseconds_offset = (long) (MILLISECONDS_PER_WEEK*weeks_offset);
            
            this.dummy_date.setTime(active_month_northwest_corner_date.getTime() - milliseconds_offset);
            return this.dummy_date;
        }

        // ========================================================================
        /** The inverse operation of getOffsetDateFromOffsetPixels() */
        float getOffsetPixelsForDate(Date offset_date) {

        	long millis_delta = active_month_northwest_corner_date.getTime() - offset_date.getTime();
        	
            float height_per_week = getDayBoxHeight() + this.vertical_spacing;
            
            float weeks_offset = millis_delta / MILLISECONDS_PER_WEEK;
            float pixel_offset = weeks_offset*height_per_week;

            return pixel_offset;
        }

        // ========================================================================
        /** Calculate vertical dimensions */
        float getDayBoxHeight() {

        	int usable_height = getHeight() - (getPaddingTop() + getPaddingBottom());
            float day_box_height = (usable_height - (spanned_weeks - 1)*this.vertical_spacing) / spanned_weeks;
            return day_box_height;
        }
        
        // ========================================================================
        /** Calculate vertical dimensions */
        float getDayBoxWidth() {

            int usable_width = getWidth() - (getPaddingLeft() + getPaddingRight());
            float day_box_width = (usable_width - (DAYS_PER_WEEK - 1)*this.horizontal_spacing) / DAYS_PER_WEEK;
            return day_box_width;
        }

        // ========================================================================
        ViewportVisitor day_event_drawing_visitor = new ViewportVisitor() {
			@Override
			public void visitViewport(RectF daybox, CalendarDayAggregator child) {
	            this.canvas.save();
	            this.canvas.translate(daybox.left, daybox.top);
	            drawDayEvents(canvas, daybox, child);
	            this.canvas.restore();
			}
        };
        
        // ========================================================================
        ViewportVisitor day_background_drawing_visitor = new ViewportVisitor() {
			@Override
			public void visitViewport(RectF daybox, CalendarDayAggregator child) {
				this.canvas.save();
				this.canvas.translate(daybox.left, daybox.top);
	            drawDayHolder(canvas, daybox, child);
	            this.canvas.restore();
			}
        };

        // ========================================================================
    	void prepareForMonth() {
        	reestablishCornerBoxDimensions();
        	updateMonthMaximums();
    	}

        // ========================================================================
    	/** Update the month maximums */
    	void updateMonthMaximums() {
        	int event_index = findFirstEventAfterDate(active_month_calendar);
        	this.day_iterator_calendar.setTime(active_month_calendar.getTime());
        	int month = this.day_iterator_calendar.get(Calendar.MONTH);
        	this.color_mapping.monthwide_daily_maximums.clear();
        	while (month == this.day_iterator_calendar.get(Calendar.MONTH)) {
	        	this.dummy_scd.reset(this.day_iterator_calendar.getTime());
	        	this.day_iterator_calendar.add(Calendar.DAY_OF_MONTH, 1);
	    		
	        	event_index = aggregateEventsForDay(this.dummy_scd, this.day_iterator_calendar, event_index);
	        	this.color_mapping.monthwide_daily_maximums.updateMax(this.dummy_scd);
        	}
    	}

    	// XXX These are no longer used
    	private Path northwest_triangle = new Path();
    	private Path southeast_triangle = new Path();
        Shader day_highlighter_gradient;
        // ========================================================================
        void reestablishCornerBoxDimensions() {
        	this.day_box_dimensions.set(
        			this.getDayBoxWidth(),
        			this.getDayBoxHeight());
            float usable_size = Math.min(
            		this.day_box_dimensions.x,
            		this.day_box_dimensions.y);

            this.northwest_triangle.reset();
            this.northwest_triangle.moveTo(0, 0);
            this.northwest_triangle.lineTo(this.day_box_dimensions.x, 0);
            this.northwest_triangle.lineTo(0, this.day_box_dimensions.y);
            this.northwest_triangle.close();

            this.southeast_triangle.reset();
            this.southeast_triangle.moveTo(this.day_box_dimensions.x, this.day_box_dimensions.y);
            this.southeast_triangle.lineTo(this.day_box_dimensions.x, 0);
            this.southeast_triangle.lineTo(0, this.day_box_dimensions.y);
            this.southeast_triangle.close();
            
            float half_x = this.day_box_dimensions.x/2;
            float half_y = this.day_box_dimensions.y/2;
            float radius = FloatMath.sqrt(half_x*half_x + half_y*half_y);
            if (radius >0)
            	this.day_highlighter_gradient = new RadialGradient(half_x, half_y, radius, new int[] {Color.TRANSPARENT, Color.TRANSPARENT, Color.WHITE, Color.BLACK}, null, Shader.TileMode.CLAMP);
            
            float corner_box_side = usable_size/2f;
            this.day_tile_paint.setTextSize(corner_box_side*0.8f);
        	this.day_tile_paint_font_metrics = this.day_tile_paint.getFontMetrics();
        }
        
        // ========================================================================
    	void draw(final Canvas canvas) {
    		canvas.setDrawFilter(this.mFastDF);
    	

            canvas.save();
            canvas.translate(0, vertical_offset);
            
            // Draw backgrounds for all of the visible days
            this.day_background_drawing_visitor.setCanvas(canvas);
            visitDayViewports(this.day_background_drawing_visitor);
            	
            
            drawMonthWatermarkText(canvas);
            
            // Draw details for all of the visible days
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
            
            if (Math.abs(this.current_flinging_velocity) > 0) {
            	processFlingState();
            } else if (this.is_snapping) {
            	processSnapState();
            }            	
            
            if (this.month_text_fader != null) {
                long now = SystemClock.uptimeMillis();
                if (this.month_text_fader.isFinished(now))
                	this.month_text_fader = null;
    			
            	invalidate();
            }
        }

        // ========================================================================
        void processSnapState() {

        	long now = SystemClock.uptimeMillis();
        	long snap_frame_millis_delta = now - this.last_frame_time_for_snap;
        	this.last_frame_time_for_snap = now;

        	// Update velocity
        	float velocity_delta_magnitude = SNAPPING_ACCELERATION*snap_frame_millis_delta/1000;

        	
        	// The default velocity delta sign is positive, but 
        	// We must accelerate in the direction of the snap target.
        	// Therefore, if the snap target position is less than than
        	// current position, the velocity should become more negative.
        	float distance_remaining = this.snap_target_offset - vertical_offset;

        	if (distance_remaining < 0)
        		velocity_delta_magnitude = -velocity_delta_magnitude;

        	this.current_snapping_velocity += velocity_delta_magnitude;
            
        	
            // Velocity is in units of pixels/second.
        	float position_delta = this.current_snapping_velocity*snap_frame_millis_delta/1000;
        	
        	if (Math.abs(position_delta) > Math.abs(distance_remaining)) {
        		
        		this.is_snapping = false;
        		this.current_snapping_velocity = 0;

        		setMonth(this.snap_target_month);
        		
        	} else {

        		vertical_offset += position_delta;
	        	invalidate();
        	}
        }

        // ========================================================================
        void processFlingState() {

        	long now = SystemClock.uptimeMillis();
        	long fling_frame_millis_delta = now - this.last_frame_time_for_fling;
        	this.last_frame_time_for_fling = now;

        	// Update velocity
        	float velocity_delta_magnitude = FLINGING_DECELERATION*fling_frame_millis_delta/1000;

        	// Protect the velocity from increasing in the opposite direction after
        	// crossing zero.
        	velocity_delta_magnitude = Math.min(velocity_delta_magnitude, Math.abs(this.current_flinging_velocity));
        	
        	// If the velocity is positive, we decelerate it by adding a negative delta.
        	// Otherwise, leave the delta as positive, which will bring a negative velocity
        	// closer to zero.
        	if (this.current_flinging_velocity > 0)
        		velocity_delta_magnitude = -velocity_delta_magnitude;

        	this.current_flinging_velocity += velocity_delta_magnitude;

        	if (Math.abs(this.current_flinging_velocity) < MINIMUM_SUSTAINED_FLINGING_VELOCITY) {
        		
        		beginSnappingBackAnimation(this.current_flinging_velocity);
        		this.current_flinging_velocity = 0;

        	} else {
	            // Velocity is in units of pixels/second.
	        	float position_delta = this.current_flinging_velocity*fling_frame_millis_delta/1000;
	        	vertical_offset += position_delta;

	        	if (scroll_callback != null)
	        		scroll_callback.updateDate(getOffsetDateFromOffsetPixels());

	        	if (transient_callback != null)
	        		transient_callback.updateDate(getNearestMonthBeginningDate());
	        		
	        	
	        	invalidate();
        	}
        }
        
        // ========================================================================
        void drawMonthWatermarkText(Canvas canvas) {

    		// Set the scale to the widest month    	
        	float scale = getHeight() / this.max_month_width;

        	String month_string = FULL_MONTH_NAME_FORMATTER.format(active_month_calendar.getTime());

            long now = SystemClock.uptimeMillis();
            float fraction = 1;
            if (this.month_text_fader != null)
            	fraction = this.month_text_fader.getFraction(now);

            
        	int text_color = this.dark_watermark ?
        			interpolateColor(Color.BLACK, this.color_month_watermark_text_dark, fraction)
        			: interpolateColor(Color.WHITE, this.color_month_watermark_text_light, fraction);
        	this.month_watermark_text_paint.setColor(text_color);
        	
        	
    		canvas.save();
    		canvas.translate(getWidth(), 0);
    		canvas.rotate(-90);
    		canvas.scale(scale, scale);
    		
    		// The month names look more stylish if we align
    		// the baseline with the edge of the screen, but this can cut
    		// off the capital "J"s and the "y"s.
//    		canvas.translate(0, -month_bg_paint.getFontMetrics().descent);

    		canvas.drawText(month_string, 0, 0, this.month_watermark_text_paint);
    		canvas.restore();	
        }

        // ========================================================================
        protected void drawDayEvents(Canvas canvas, RectF daybox, CalendarDayAggregator day) {
            float usable_size = Math.min(daybox.width(), daybox.height());
            
            if (this.enable_event_count)
            	drawEventCount(canvas, daybox, day, usable_size);
            
        	this.dummy_calendar.setTime(day.getDate());
        	
        	int daycal_month_idx = this.dummy_calendar.get(Calendar.MONTH);
        	boolean month_active = active_month_calendar.get(Calendar.MONTH) == daycal_month_idx;
            drawCornerBox(canvas, day, month_active);
        }

        // ========================================================================
        int[] getProperViewState(boolean month_focused, boolean daycal_month_odd, boolean day_highlighted) {
        	if (month_focused) {
        		if (day_highlighted)
        			return View.ENABLED_FOCUSED_SELECTED_STATE_SET;
    			else
        			return View.ENABLED_FOCUSED_STATE_SET;
        	} else {
        		if (daycal_month_odd) {
        			if (day_highlighted)
            			return View.ENABLED_SELECTED_STATE_SET;
        			else
            			return View.ENABLED_STATE_SET;
        		} else {
        			if (day_highlighted)
            			return View.SELECTED_STATE_SET;
        			else
            			return View.EMPTY_STATE_SET;
        		}
        	}
        }
        
        // ========================================================================
        protected void drawDayHolder(Canvas canvas, RectF daybox, CalendarDayAggregator day) {

        	this.dummy_calendar.setTime(day.getDate());

        	int months_away = getMonthDifference(active_month_calendar, this.dummy_calendar);
        	boolean month_focused = months_away == 0;
        	boolean daycal_month_odd = months_away % 2 != 0;
        	
            boolean day_highlighted = day.getDate().equals(highlighted_day);

            int background_color;
            if (this.color_mapping.enabled) {
            	
//            	if (day_highlighted) {
            	if (false) {
            		background_color = this.month_color_states.getColorForState(getProperViewState(month_focused, daycal_month_odd, day_highlighted), Color.YELLOW);
            		
            	} else {
	            	if (this.color_mapping.showing_monthwide_daily_maximums && !month_focused)
	            		background_color = this.color_mapping.color_stops[0];
	            	else
	            		background_color = this.color_mapping.getTileColor(day);
            	}
            } else {
            	background_color = this.month_color_states.getColorForState(getProperViewState(month_focused, daycal_month_odd, day_highlighted), Color.YELLOW);
            }
            				
            if (is_holding_longpress && day.getDate().equals(highlighted_day)) {

            	long now = SystemClock.uptimeMillis();
            	
            	// Before you start to animate, check whether the tap time has been exceeded.
            	long tap_timeout = ViewConfiguration.getTapTimeout();
            	if (now > longpress_start_time + tap_timeout) {
            		
    	        	float alpha = (now - (longpress_start_time + tap_timeout)) / (float) ViewConfiguration.getLongPressTimeout();
    	
    	        	alpha = Math.min(1, alpha);

    	        	background_color = interpolateColor(background_color, color_calendar_date_background_longpress, alpha);
            	}
            }
            				
    		// Draw the background
            this.day_tile_paint.setColor(background_color);
            canvas.drawRect(0, 0, daybox.width(), daybox.height(), this.day_tile_paint);
            
            if (this.color_mapping.enabled && day_highlighted) {
            	this.day_tile_paint.setShader(this.day_highlighter_gradient);
                canvas.drawRect(0, 0, daybox.width(), daybox.height(), this.day_tile_paint);
                this.day_tile_paint.setShader(null);
            }
            
            
            // Draw bordering month day indicators
            if (this.color_mapping.enabled) {

            	/*
            	if (months_away < 0) {
                    canvas.drawPath(this.southeast_triangle, this.triangle_paint);
            	} else if (months_away > 0) {
                    canvas.drawPath(this.northwest_triangle, this.triangle_paint);
            	}
            	*/

            	if (!month_focused)
            		canvas.drawRect(0, 0, daybox.width(), daybox.height(), this.triangle_paint);
            }
        }

        // ========================================================================
        void drawEventCount(Canvas canvas, RectF daybox, CalendarDayAggregator calendar_day, float usable_size) {

            canvas.save();
            canvas.translate(daybox.width()/2f, daybox.height()/2f);
    		
    		int event_count = calendar_day.getEventCount();

            if (event_count > 0) {

    	        // Draw decorative circle
    			
    	        this.day_tile_paint.setColor(color_event_count_background);
                if (transparency_enabled)
                	this.day_tile_paint.setAlpha(0xff/2);

    			canvas.drawCircle(0, 0, usable_size/3, day_tile_paint);
    			
    			
    			// Draw the number of events inside the circle
    	        float corner_box_side = usable_size/2f;
    	        this.day_tile_paint.setTextSize(corner_box_side*0.8f);
    			float text_height = this.day_tile_paint_font_metrics.ascent + this.day_tile_paint_font_metrics.descent;
    	
    	        this.day_tile_paint.setColor(color_event_count_number);
    			String text = Integer.toString( event_count );
    			this.day_tile_paint.setTextAlign(Align.CENTER);
    			canvas.drawText(text, 0, -text_height/2, this.day_tile_paint);
    		}
    		
    		canvas.restore();
        }
        
        // ========================================================================
        void drawCornerBox(Canvas canvas, CalendarDayAggregator calendar_day, boolean month_active) {
        	
            float usable_size = Math.min(
            		this.day_box_dimensions.x,
            		this.day_box_dimensions.y);
        	
            float corner_box_side = usable_size/2f;


            this.day_tile_paint.setColor(color_cornerbox_background_color);
            if (transparency_enabled)
            	this.day_tile_paint.setAlpha(0xFF/2);
            
            canvas.drawRect(0, 0, corner_box_side, corner_box_side, this.day_tile_paint);
            canvas.save();
            canvas.translate(corner_box_side/2f, corner_box_side/2f);
            
    		this.dummy_calendar.setTime(calendar_day.getDate());
    		String text = Integer.toString( this.dummy_calendar.get(Calendar.DAY_OF_MONTH) );

    		float text_height = this.day_tile_paint_font_metrics.ascent + this.day_tile_paint_font_metrics.descent;

            int text_color = month_active ? color_cornerbox_date_number : color_cornerbox_date_number_passive;
            this.day_tile_paint.setColor(text_color);
            
            this.day_tile_paint.setTextAlign(Align.CENTER);
    		canvas.drawText(text, 0, -text_height/2, this.day_tile_paint);
    		
    		canvas.restore();
        }

        // ========================================================================
        RectF dummy_rect = new RectF();
    	CalendarDayAggregator dummy_scd = new CalendarDayAggregator();
    	Calendar day_iterator_calendar = new GregorianCalendar();
        
    	PointF day_box_dimensions = new PointF();
    	
        // ========================================================================
        /** Implementation of the visitor pattern that iterates through
         * each of the days that are visible on screen.
         */
    	SimpleEvent dummy_simple_event = new SimpleEvent(-1, 0, null);
        void visitDayViewports(ViewportVisitor visitor) {

            float day_box_width = getDayBoxWidth();
            float width_per_day = day_box_width + this.horizontal_spacing;

            float day_box_height = getDayBoxHeight();
            float height_per_week = day_box_height + this.vertical_spacing;


            // Reset calendar to beginning of first week
        	this.day_iterator_calendar.setTime(active_month_calendar.getTime());
        	setMonthWeekBeginning(this.day_iterator_calendar);

        	// Reposition calendar according to the vertical scroll offset
        	int weeks_offset = (int) Math.floor(-vertical_offset/height_per_week);
        	this.day_iterator_calendar.add(Calendar.DAY_OF_MONTH, weeks_offset*DAYS_PER_WEEK);

            int event_idx = findFirstEventAfterDate(this.day_iterator_calendar);
            
            // The "<=" as opposed to the typical "<" allows the entire screen to be
            // covered while scrolling.
            for (int i=0; i <= spanned_weeks; i++) {

                float top = getPaddingTop() + height_per_week * (i + weeks_offset);
            	
                for (int j=0; j < DAYS_PER_WEEK; j++) {

                	// We advance the calendar to the next day *after* recording
                	// the date for the current day.
                	this.dummy_scd.reset(this.day_iterator_calendar.getTime());
                	this.day_iterator_calendar.add(Calendar.DAY_OF_MONTH, 1);
            		
                	event_idx = aggregateEventsForDay(this.dummy_scd, this.day_iterator_calendar, event_idx);

                    float left = getPaddingLeft() + ((width_per_day) * j);
                    this.dummy_rect.set(left, top, left + day_box_width, top + day_box_height);
                    
                    visitor.visitViewport(this.dummy_rect, this.dummy_scd);
                }
            }
        }

        // ========================================================================
        /** Consume all events up until the next day.  The values are zeroed,
         * except for the date, which has already been set.
         * Returns the index of the next event to be processed. */
        int aggregateEventsForDay(CalendarDayAggregator aggregator, Calendar limit, int starting_index) {
        	
        	int event_idx = starting_index;
            while (event_idx < sorted_events.size() && sorted_events.get(event_idx).timestamp.getTime() < limit.getTimeInMillis()) {
            	aggregator.incrementEventCount();
            	SimpleEvent se = sorted_events.get(event_idx);
            	for (int q=0; q<se.quantities.length; q++)
            		aggregator.addAggregateQuantity(q, se.quantities[q]);
//            	child.day_events.add(this.sorted_events.get(event_idx));
            	event_idx++;
            }
        	
        	return event_idx;
        }

        // ========================================================================
        /** Skip all events before the given calendar date */
        int findFirstEventAfterDate(Calendar calendar) {
        	
            int event_idx = 0;
        	// This is the naive, O(N) approach.
//			while (event_idx < sorted_events.size() && sorted_events.get(event_idx).timestamp.getTime() < calendar.getTimeInMillis()) event_idx++;
//			int backup_event_idx = event_idx;

            // The binary search will avoid iterating through many events; it is O(log(N))
            this.dummy_simple_event.timestamp.setTime( calendar.getTimeInMillis() );
            int search_index = Collections.binarySearch(sorted_events, this.dummy_simple_event);
            
            // If positive, the binary search index is guaranteed to be
            // a valid index in the list.
            if (search_index >= 0) {
                // If the binary search index is non-negative, that means at least one of
            	// the event dates was exactly equal to our first visible calendar date
            	// (that is, exactly midnight).  Since binary search makes no guarantee
            	// which one of multiple equal values will be found, we iterate backwards
            	// in the list until we get a non-equal value.

            	Date matching_date = sorted_events.get(search_index).timestamp;
            	do {
            		event_idx = search_index;
            		search_index--;

            	} while (search_index >= 0 && matching_date.equals(sorted_events.get(search_index).timestamp));
            	
            } else {
                // If the binary search index is negative, we have a formula
            	// for the "insertion point":
            	// search_index = (-(insertion point) - 1). The insertion point is
            	// defined as the point at which the key would be inserted into the
            	// list: the index of the first element greater than the key, or
            	// list.size(), if all elements in the list are less than the specified key.

            	// Therefore:
            	event_idx = Math.abs(search_index + 1);
            }
            
//			Log.d(TAG, "Binary search starting point: " + event_idx + "; Actual starting point: " + backup_event_idx);
//			event_idx = backup_event_idx;
            
            return event_idx;
        }
    }

    // ========================================================================
    static int getMonthDifference(Calendar cal1, Calendar cal2) {
    	
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
        
		Calendar offset_calendar_date = (Calendar) this.active_month_calendar.clone();
    	setMonthWeekBeginning(offset_calendar_date);

		offset_calendar_date.add(Calendar.DATE, DAYS_PER_WEEK*week_offset + weekday_index);
    	return offset_calendar_date.getTime();
    }
    
    // ========================================================================
    static int interpolateInt(int src, int dst, float alpha) {
		return (int) ((dst - src)*alpha) + src;
	}

    // ========================================================================
	static int interpolateColor(int src_color, int dst_color, float fraction) {
		int red = interpolateInt(Color.red(src_color), Color.red(dst_color), fraction);
		int green = interpolateInt(Color.green(src_color), Color.green(dst_color), fraction);
		int blue = interpolateInt(Color.blue(src_color), Color.blue(dst_color), fraction);
		int alpha = interpolateInt(Color.alpha(src_color), Color.alpha(dst_color), fraction);
		return Color.argb(alpha, red, green, blue);
	}
	
    // ========================================================================
	static int invertColor(int src_color) {
		return Color.argb(
				Color.alpha(src_color),
				0xFF - Color.red(src_color),
				0xFF - Color.green(src_color),
				0xFF - Color.blue(src_color));
	}
	

    // ========================================================================
	void moveMonth(boolean forward) {
        this.active_month_calendar.add(Calendar.MONTH, forward ? 1 : -1);
        setMonth(this.active_month_calendar);
	}
	
    // ========================================================================
    private static final int FLING_MIN_DISTANCE = 75;
    private static final int FLING_MAX_OFF_HORIZONTAL_PATH = 300;
    private static final int FLING_MAX_OFF_VERTICAL_PATH = 300;
    private static final int FLING_THRESHOLD_VELOCITY = 500;
    
    class MonthGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	
            try {

            	boolean horizontal_fling_possible = true;
                if (Math.abs(e1.getY() - e2.getY()) > FLING_MAX_OFF_HORIZONTAL_PATH)
                	horizontal_fling_possible = false;
                
            	boolean vertical_fling_possible = true;
                if (Math.abs(e1.getX() - e2.getX()) > FLING_MAX_OFF_VERTICAL_PATH)
                	vertical_fling_possible = false;


                
                if (horizontal_fling_possible) {
                	float x_delta = e1.getX() - e2.getX();
	                if(x_delta > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_THRESHOLD_VELOCITY) {
	                	moveMonth(true);
	                }  else if (-x_delta > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_THRESHOLD_VELOCITY) {
	                	moveMonth(false);
	                }
                } else if (vertical_fling_possible) {
                	
                	Log.i(TAG, "Flung with vertical velocity: " + velocityY);
                	
                	float y_delta = e1.getY() - e2.getY();
	                if(y_delta > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_THRESHOLD_VELOCITY) {
	                	
	                	Log.d(TAG, "Achieved vertical swipe DOWN");
	                	calendar_drawing.current_flinging_velocity = velocityY;
	                	calendar_drawing.last_frame_time_for_fling = SystemClock.uptimeMillis();
	                	invalidate();
	                	
	                }  else if (-y_delta > FLING_MIN_DISTANCE && Math.abs(velocityY) > FLING_THRESHOLD_VELOCITY) {
	                	
	                	Log.d(TAG, "Achieved vertical swipe UP");
	                	calendar_drawing.current_flinging_velocity = velocityY;
	                	calendar_drawing.last_frame_time_for_fling = SystemClock.uptimeMillis();
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
        	calendar_drawing.current_flinging_velocity = 0;
        	
        	highlighted_day = getDayFromPoint(new PointF(e.getX(), e.getY()));
        	highlightDay(highlighted_day);

        	return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        	is_holding_longpress = false;
        	
			float dy = e2.getY() - e1.getY();
			if (Math.abs(dy) >= VERTICAL_SCROLL_TOLERANCE) {

				vertical_offset -= distanceY;

	        	if (scroll_callback != null)
	        		scroll_callback.updateDate(calendar_drawing.getOffsetDateFromOffsetPixels());


	        	if (transient_callback != null)
	        		transient_callback.updateDate(calendar_drawing.getNearestMonthBeginningDate());
	        	
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
    
    // ==========================================================
    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
    	this.calendar_drawing.reestablishCornerBoxDimensions();
    }

    // ==========================================================
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		this.calendar_drawing.transparency_enabled = sharedPreferences.getBoolean(CalendarDisplayPreferences.PREFKEY_ENABLE_TRANSPARENCY, CalendarDisplayPreferences.DEFAULT_ENABLE_TRANSPARENCY);
		
		this.calendar_drawing.dark_watermark = sharedPreferences.getBoolean(CalendarDisplayPreferences.PREFKEY_DARK_WATERMARK, false);
		invalidate();
	}
}
