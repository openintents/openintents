package org.openintents.calendarpicker.view;



import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.openintents.calendarpicker.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;

public class TimelineViewHorizontal extends View {

	static final String TAG = "TinyTimelineViewHorizontal";
	
	
    private TextPaint mTextPaint;
    private Paint mLinePaint;
    private String mText;
    private int mAscent;
    
    private Date date;

    static final long MILLISECONDS_PER_YEAR = ScrollableMonthView.MILLISECONDS_PER_DAY*365;
    float timeline_years_span = 3.5f;
    
    /**
     * Constructor.  This version is only needed if you will be instantiating
     * the object manually (not from a layout XML file).
     * @param context
     */
    public TimelineViewHorizontal(Context context) {
        super(context);
        initTimelineView();
    }

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

        CharSequence s = a.getString(R.styleable.TinyTimelineViewHorizontal_text);
        if (s != null) {
            setText(s.toString());
        }

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

    private final void initTimelineView() {
        mTextPaint = new TextPaint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(16);
        mTextPaint.setTextAlign(Align.CENTER);
        mTextPaint.setColor(0xFF000000);
        
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(Color.WHITE);
        mLinePaint.setStrokeCap(Cap.ROUND);
        
        setPadding(3, 3, 3, 3);
        
        this.setFocusable(true);
        
        final GestureDetector gestureDetector = new GestureDetector(new TimelineGestureDetector());
        setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
			}
        });
    }

    /**
     * Sets the text to display in this label
     * @param text The text to display. This will be drawn as one line.
     */
    public void setText(String text) {
        mText = text;
        requestLayout();
        invalidate();
    }
    
    
    public void setDate(Date date) {
        this.date = date;
        invalidate();
    }
    

    /**
     * Sets the text size for this label
     * @param size Font size
     */
    public void setTextSize(int size) {
        mTextPaint.setTextSize(size);
        requestLayout();
        invalidate();
    }

    /**
     * Sets the text color for this label.
     * @param color ARGB value for the text
     */
    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        invalidate();
    }

    /**
     * @see android.view.View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

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
            result = (int) mTextPaint.measureText(mText) + getPaddingLeft()
                    + getPaddingRight();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }

        return result;
    }

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

        mLinePaint.setColor(Color.GRAY);
        mLinePaint.setStrokeWidth(line_width);
        canvas.drawLine(0, 0, getWidth(), 0, mLinePaint);

        
        // Center horizontally
        canvas.translate(getWidth()/2f, 0);        

        mLinePaint.setColor(Color.RED);
        mLinePaint.setStrokeWidth(hash_width);
        canvas.drawLine(0, getHeight()/4f, 0, -getHeight()/4f, mLinePaint);
        

        mLinePaint.setColor(Color.WHITE);
    	Calendar calendar = new GregorianCalendar();        
        if (this.date != null) {
        	
        	Date d = new Date(this.date.getTime() - (long) (MILLISECONDS_PER_YEAR*timeline_years_span/2));

            calendar.setTime(d);
            int y = calendar.get(Calendar.YEAR);
            calendar.clear();
            calendar.set(Calendar.YEAR, y);

            for (int i=0; i<timeline_years_span; i++) {

                calendar.add(Calendar.YEAR, 1);

                long millis_delta = calendar.getTimeInMillis() - this.date.getTime();
            	float fraction = millis_delta/(MILLISECONDS_PER_YEAR*timeline_years_span);

            	float horizontal_position = fraction*getWidth();
            	canvas.drawCircle(horizontal_position, 0, marker_radius, mLinePaint);

                int year = calendar.get(Calendar.YEAR);
           		canvas.drawText(Integer.toString(year), horizontal_position, -mAscent + marker_radius, mTextPaint);
            }
        }
    }

    // ========================================================================
    Date getTouchDate(float horizontal_position) {
    	long ms_delta = (long) ((getWidth()/2f - horizontal_position)*(MILLISECONDS_PER_YEAR*timeline_years_span) / getWidth());
    	return new Date(date.getTime() - ms_delta);
    }
	
    // ========================================================================
    private static final int SWIPE_MIN_DISTANCE = 60;
    private static final int SWIPE_MAX_OFF_PATH = 300;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;
    class TimelineGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        	
        	Log.d(TAG, "Intercepted timeline fling gesture...");
        	
        	/*
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
            */
            return false;
        }
        
        @Override
        public boolean onSingleTapConfirmed (MotionEvent e) {
			return false;
        }
        
        @Override
        public boolean onDown(MotionEvent e) {
        	
        	Date touched_date = getTouchDate(e.getX());
        	Log.d(TAG, "Touched timeline at " + touched_date);
        	
        	return true;
        }
        
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        	return false;
        }
    }
}
