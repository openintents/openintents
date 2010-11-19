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



import java.util.Calendar;
import java.util.GregorianCalendar;

import org.openintents.calendarpicker.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

public class ColormapView extends View {

	static final String TAG = "ColormapView";
	
    private Paint mLinePaint;
	int[] color_stops = new int[] {Color.BLACK, Color.RED};
	Orientation orientation = Orientation.VERTICAL;
	
	enum Orientation {
		HORIZONTAL, VERTICAL
	}
	
    
    /**
     * Constructor.  This version is only needed if you will be instantiating
     * the object manually (not from a layout XML file).
     * @param context
     */
    public ColormapView(Context context) {
        super(context);
        initColormapView();
    }

    /**
     * Construct object, initializing with any attributes we understand from a
     * layout file. These attributes are defined in
     * SDK/assets/res/any/classes.xml.
     * 
     * @see android.view.View#View(android.content.Context, android.util.AttributeSet)
     */
    public ColormapView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initColormapView();
        
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ColormapView);
        
        String orientation_attribute = a.getString(R.styleable.ColormapView_orientation);
       	this.orientation = Orientation.HORIZONTAL.name().toLowerCase().equals(orientation_attribute) ? Orientation.HORIZONTAL : Orientation.VERTICAL;
        
    	a.recycle();
    }


    public void setColors(int[] colors) {
    	this.color_stops = colors;
    }
    
    // ========================================================================
    private final void initColormapView() {
        
        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        
        setPadding(3, 3, 3, 3);	// XXX Irrelevant
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

    int DEFAULT_HEIGHT = 50;
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
            result = DEFAULT_HEIGHT;
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }
    
    Shader gradient;
    // ==========================================================
    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
    	gradient = new LinearGradient (0, 0, 0, h, this.color_stops, null, Shader.TileMode.CLAMP);
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

        this.mLinePaint.setShader(this.gradient);
        canvas.drawPaint(this.mLinePaint);
    }
}
