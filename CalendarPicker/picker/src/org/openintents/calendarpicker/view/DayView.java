package org.openintents.calendarpicker.view;


import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.openintents.calendarpicker.container.CalendarDay;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.util.Log;
import android.view.View;

import org.openintents.calendarpicker.R;

public class DayView extends View {

	static final String TAG = "DayView"; 
	
    int intrinsic_width = 50;
    int intrinsic_height = 50;
  
    CalendarDay calendar_day;
    boolean dim;
	Paint my_paint;
	Context context;
	DayChoiceHandler day_choice_handler;
	
	public interface DayChoiceHandler {
		void onDaySelected(Date date);
	}

    // ========================================================================
	public CalendarDay getCalendarDay() {
		return this.calendar_day;
	}
	
    // ========================================================================
    public DayView(Context context, DayChoiceHandler day_choice_handler, CalendarDay calendar_day, boolean dim) {
        super(context);
        this.context = context;
        this.day_choice_handler = day_choice_handler;
        init(calendar_day, dim);
    }

    // ========================================================================
    void init(final CalendarDay calendar_day, boolean dim) {
    	this.calendar_day = calendar_day;
    	this.dim = dim;
    	
        my_paint = new Paint();
		my_paint.setAntiAlias(true);
		my_paint.setColor(Color.WHITE);
//		my_paint.setStyle(Style.STROKE);
//		my_paint.setStrokeJoin(Join.MITER);
//		my_paint.setStrokeWidth(2);
		my_paint.setTextAlign(Align.CENTER);
		

        this.setFocusable(true);
        this.setClickable(true);
        this.setFocusableInTouchMode(true);
        
        this.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				Calendar cal = new GregorianCalendar();
				cal.setTime(calendar_day.date);

				Log.d(TAG, "Focus was changed to day " + cal.get(Calendar.DAY_OF_MONTH));
			}
        });
    }

    // ========================================================================
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec),
                measureHeight(heightMeasureSpec));
    }

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
            // Measure the text (beware: ascent is a negative number)
            result = intrinsic_width + getPaddingLeft()
                    + getPaddingRight();
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

        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = intrinsic_height + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

    // ========================================================================
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        
        Resources resources = this.context.getResources();
        
        int background_color = this.isFocused() ?
        		resources.getColor(
        				this.dim ? R.color.calendar_date_background_passive_selected : R.color.calendar_date_background_active_selected)
        		: resources.getColor(
        				this.dim ? R.color.calendar_date_background_passive : R.color.calendar_date_background_active);
		// Draw the background
		canvas.drawColor(background_color);

        my_paint.setStyle(Style.FILL);
        
        int usable_size = Math.min(getWidth() - (getPaddingLeft() + getPaddingRight()),
        		getHeight() - (getPaddingTop() + getPaddingBottom()));


		
		
		

        drawEventCount(canvas, resources, usable_size);
        drawCornerBox(canvas, resources, usable_size);
    }

    // ========================================================================
    void drawEventCount(Canvas canvas, Resources resources, int usable_size) {

        canvas.save();
        canvas.translate(getWidth()/2f, getHeight()/2f);
        
		
		
		int event_count = this.calendar_day.day_events.size();

		if (event_count > 0) {
			


	        // Draw decorative circle
	        my_paint.setColor(Color.CYAN);
			canvas.drawCircle(0, 0, usable_size/3, my_paint);
			
			
	
	        float corner_box_side = usable_size/2f;
			String text = Integer.toString( event_count );
	//		Rect text_bounds = new Rect();
	//		my_paint.getTextBounds(text, 0, text.length(), text_bounds);
			my_paint.setTextSize(corner_box_side*0.8f);
			float text_height = my_paint.getFontMetrics().ascent + my_paint.getFontMetrics().descent;
	
	
	        int event_count_number = resources.getColor(R.color.event_count_number);
	        my_paint.setColor(event_count_number);
			canvas.drawText(text, 0, -text_height/2, my_paint);
		}
		
		canvas.restore();
    }
    
    // ========================================================================
    void drawCornerBox(Canvas canvas, Resources resources, int usable_size) {
    	
        float corner_box_side = usable_size/2f;
        RectF rect = new RectF(
        		getPaddingLeft(),
        		getPaddingTop(),
        		getPaddingLeft() + corner_box_side,
        		getPaddingTop() + corner_box_side);

        int cornerbox_color = resources.getColor(R.color.cornerbox_color);
        my_paint.setColor(cornerbox_color);
        canvas.drawRect(rect, my_paint);

        canvas.save();
        canvas.translate(rect.centerX(), rect.centerY());
        
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(this.calendar_day.date);
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
    public Date getDate() {
    	return calendar_day.date;
    }
}