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



import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.activity.PeriodBrowsingActivity.TimespanEventMaximums;
import org.openintents.calendarpicker.container.SimpleEvent;
import org.openintents.calendarpicker.container.TimespanEventAggregator;
import org.openintents.calendarpicker.container.ColorMappingConfiguration.ColorMappingHost;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;

public class TimelineViewHorizontal extends View {

	static final String TAG = "TimelineViewHorizontal";
	
	
    private TextPaint mTextPaint;
    private Paint mLinePaint, mEventPaint;
    private int mAscent;
    
    boolean is_touching = false;
    float last_touch_x;
    
    private Date date = new Date();

    static final long MILLISECONDS_PER_YEAR = FlingableMonthView.MILLISECONDS_PER_DAY*365;
    float timeline_years_span = 3.5f;
    float pixels_per_bin = 5;
    
    // ========================================================================
    /**
     * Constructor.  This version is only needed if you will be instantiating
     * the object manually (not from a layout XML file).
     * @param context
     */
    public TimelineViewHorizontal(Context context) {
        super(context);
        initTimelineView();
    }

    // ========================================================================
    /**
     * Construct object, initializing with any attributes we understand from a
     * layout file. These attributes are defined in
     * SDK/assets/res/any/classes.xml.
     * 
     * @see android.view.View#View(android.content.Context, android.util.AttributeSet)
     */
    public TimelineViewHorizontal(Context context, AttributeSet attrs) {
        super(context, attrs);
        initTimelineView();

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.TinyTimelineViewHorizontal);

        // Retrieve the color(s) to be used for this view and apply them.
        // Note, if you only care about supporting a single color, that you
        // can instead call a.getColor() and pass that to setTextColor().
        setTextColor(a.getColor(R.styleable.TinyTimelineViewHorizontal_textColor, 0xFF000000));

        int textSize = a.getDimensionPixelOffset(R.styleable.TinyTimelineViewHorizontal_textSize, 0);
        if (textSize > 0) {
            setTextSize(textSize);
        }

        a.recycle();
    }

    // ========================================================================
    private final void initTimelineView() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(16);
        mTextPaint.setTextAlign(Align.CENTER);
        mTextPaint.setColor(Color.WHITE);
        
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setStrokeCap(Cap.ROUND);
        
        mEventPaint = new Paint();
        mEventPaint.setAntiAlias(true);
        mEventPaint.setColor(Color.YELLOW);
        mEventPaint.setStyle(Style.FILL);
        
        setPadding(3, 3, 3, 3);
        
        final GestureDetector gestureDetector = new GestureDetector(new TimelineGestureDetector());
        setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
     	
	        	if (event.getAction() == MotionEvent.ACTION_UP) {
					is_touching	= false;
					invalidate();
					Date touched_date = new Date(getTouchDateMillis(last_touch_x));
					date_selection_callback.updateDate(touched_date);
	        	}
				
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
			}
        });
    }
    
    // ========================================================================
    OnDateUpdateListener date_update_callback;
    public void setOnDateUpdateListener(OnDateUpdateListener callback) {
    	this.date_update_callback = callback;
    }
    
    // ========================================================================
    OnDateUpdateListener date_selection_callback;
    public void setOnDateSelectionListener(OnDateUpdateListener callback) {
    	this.date_selection_callback = callback;
    }

    // ========================================================================
    public void setDate(Date date) {
        this.date = date;
        invalidate();
    }

    // ========================================================================
    /**
     * Sets the text size for this label
     * @param size Font size
     */
    public void setTextSize(int size) {
        mTextPaint.setTextSize(size);
        requestLayout();
        invalidate();
    }

    // ========================================================================
    /**
     * Sets the text color for this label.
     * @param color ARGB value for the text
     */
    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    // ========================================================================
    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

    int DEFAULT_WIDTH = 50;
    // ========================================================================
    /**
     * Determines the width of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text
            result = DEFAULT_WIDTH;
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

    // ========================================================================
    /**
     * Determines the height of this view
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        mAscent = (int) mTextPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (-mAscent + mTextPaint.descent()) + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    // ========================================================================
    Calendar dummy_calendar = new GregorianCalendar();
    // ========================================================================
    /**
     * Render the text
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float smaller_dimension = Math.min(getHeight(), getWidth());
        float line_width = smaller_dimension/10;
        float hash_width = line_width/2;
        float marker_radius = 1.5f*line_width;
        
        // Center vertically
        canvas.translate(0, getHeight()/2f);

        this.mLinePaint.setColor(Color.GRAY);
        this.mLinePaint.setStrokeWidth(line_width);
        canvas.drawLine(0, 0, getWidth(), 0, this.mLinePaint);


        if (this.is_touching) {
        	this.mLinePaint.setColor(Color.GREEN);
	        canvas.drawLine(this.last_touch_x, getHeight()/4f, this.last_touch_x, -getHeight()/4f, this.mLinePaint);
        }
    	
        
        // Center horizontally
        canvas.translate(getWidth()/2f, 0);        


        if (this.colormapping_host.getColorMappingConfig().enabled)
        	drawEventsHistogram(canvas);
        
        this.mLinePaint.setColor(Color.RED);
        this.mLinePaint.setStrokeWidth(hash_width);
        canvas.drawLine(0, getHeight()/4f, 0, -getHeight()/4f, this.mLinePaint);
        

        drawYearNodes(canvas, marker_radius);

    }


    Date dummy_left_edge_date = new Date();
    Date dummy_right_edge_date = new Date();
    // ========================================================================
    void drawEventsHistogram(Canvas canvas) {

    	this.dummy_left_edge_date.setTime(getLeftEdgeDateMillis());
        this.dummy_right_edge_date.setTime(this.dummy_left_edge_date.getTime() + (long) (MILLISECONDS_PER_YEAR*this.timeline_years_span));
    	
    	float max_height = getHeight()/2;
    	for (TimespanEventAggregator aggregation : aggregated_events) {
    		
    		// We skip through pieces that come before the leftmost visible
    		// edge of the timeline.
    		
    		if (aggregation.getDate().before(this.dummy_left_edge_date)) {
    			continue;
    		} else if (!aggregation.getDate().before(this.dummy_right_edge_date)) {
    			break;
    		}
    		
    		
        	
        	float fraction = this.colormapping_host.getColorMappingConfig().getFraction(aggregation, this.timespan_maximums);
    		float height = max_height*fraction;
        	float horizontal_position = getScreenPositionOfDateMillis(aggregation.getDate().getTime());
        	
        	int color = this.colormapping_host.getColorMappingConfig().interpolateColorStops(fraction);
        	this.mEventPaint.setColor(color);
    		canvas.drawRect(horizontal_position, -height, horizontal_position + this.pixels_per_bin, 0, this.mEventPaint);
    	}
    }
    
    // ========================================================================
    long getLeftEdgeDateMillis() {
    	return this.date.getTime() - (long) (MILLISECONDS_PER_YEAR*this.timeline_years_span/2);
    }

    // ========================================================================
    float getScreenPositionOfDateMillis(long millis) {
        long millis_delta = millis - this.date.getTime();
    	float fraction = millis_delta/(MILLISECONDS_PER_YEAR*this.timeline_years_span);

    	float horizontal_position = fraction*getWidth();
    	return horizontal_position;
    }
    
    // ========================================================================
    void drawYearNodes(Canvas canvas, float marker_radius) {
    	
        if (this.date != null) {
        	
            this.mLinePaint.setColor(Color.WHITE);
        	
            this.dummy_calendar.setTimeInMillis(getLeftEdgeDateMillis());
            int y = this.dummy_calendar.get(Calendar.YEAR);
            this.dummy_calendar.clear();
            this.dummy_calendar.set(Calendar.YEAR, y);

            for (int i=0; i<this.timeline_years_span; i++) {

            	this.dummy_calendar.add(Calendar.YEAR, 1);

            	
            	float horizontal_position = getScreenPositionOfDateMillis(this.dummy_calendar.getTimeInMillis());
            	canvas.drawCircle(horizontal_position, 0, marker_radius, this.mLinePaint);

                int year = this.dummy_calendar.get(Calendar.YEAR);
           		canvas.drawText(Integer.toString(year), horizontal_position, -this.mAscent + marker_radius, this.mTextPaint);
            }
        }
    }

    // ========================================================================
    long getTouchDateMillis(float horizontal_position) {
    	long ms_delta = (long) ((getWidth()/2f - horizontal_position)*(MILLISECONDS_PER_YEAR*timeline_years_span) / getWidth());
    	return this.date.getTime() - ms_delta;
    }
	
    // ========================================================================
    class TimelineGestureDetector extends SimpleOnGestureListener {
        
        @Override
        public boolean onDown(MotionEvent e) {
        	
        	is_touching = true;
        	last_touch_x = e.getX();
        	
        	return true;
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {

        	last_touch_x = e2.getX();
        	
        	Date touched_date = new Date(getTouchDateMillis(e2.getX()));
        	date_update_callback.updateDate(touched_date);
        	
        	invalidate();
        	return true;
        }
    }
    
    // ==========================================================
    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
    	
    	float screenspace_pixel_milliseconds = MILLISECONDS_PER_YEAR*timeline_years_span / w;
    	aggregateEvents(screenspace_pixel_milliseconds);
    }

    // ==========================================================
    // We only hold onto this variable until the dimensions of this view are set,
    // at which time we aggregate them into screen-space-determined bins.
    List<SimpleEvent> sorted_events = new ArrayList<SimpleEvent>();
    public void setEvents(List<SimpleEvent> sorted_events) {
    	this.sorted_events = sorted_events;
    }

    private List<TimespanEventAggregator> aggregated_events = new ArrayList<TimespanEventAggregator>();
    TimespanEventMaximums timespan_maximums = new TimespanEventMaximums();
    
    // ========================================================================
    /** Aggregates the events into weeks */
    public void aggregateEvents(float screenspace_pixel_milliseconds) {
    	this.aggregated_events.clear();
    	
    	
    	if (sorted_events.size() > 0) {

    		
    		long bin_duration_milliseconds = (long) (pixels_per_bin*screenspace_pixel_milliseconds);
    		
	    	TimespanEventAggregator timespan_aggregator = null;
	    	
	    	
	    	Date aggregation_stopping_point = new Date();
	    	SimpleEvent first_event = sorted_events.get(0);
	    	aggregation_stopping_point.setTime(first_event.timestamp.getTime());
	    	
	    	int event_index = 0;
	    	while (event_index < sorted_events.size()) {
	
		    	SimpleEvent event = sorted_events.get(event_index);
		    	
		    	while (event.timestamp.getTime() >= aggregation_stopping_point.getTime()) {
		    		timespan_aggregator = new TimespanEventAggregator();
		    		timespan_aggregator.reset((Date) aggregation_stopping_point.clone());
		    		this.aggregated_events.add(timespan_aggregator);
		    		
		    		aggregation_stopping_point.setTime(aggregation_stopping_point.getTime() + bin_duration_milliseconds);
		    	}
	    		
	        	event_index = FlingableMonthView.aggregateEventsUntilTime(sorted_events, timespan_aggregator, aggregation_stopping_point.getTime(), event_index);
	        	timespan_maximums.updateMax(timespan_aggregator);
	    	}
    	}
    }
    
    
    
    ColorMappingHost colormapping_host;
    // ==========================================================
    public void setColorMappingHost(ColorMappingHost host) {
    	this.colormapping_host = host;
    }
}
