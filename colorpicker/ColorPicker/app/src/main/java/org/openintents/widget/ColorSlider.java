/* 
 * Copyright (C) 2008 OpenIntents.org
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

package org.openintents.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * BrightnessSlider.
 * 
 * @author Peli, based on API demo code.
 * 
 */
public class ColorSlider extends View {

	/** Default widget width */
	public int defaultWidth;

	/** Default widget height */
	public int defaultHeight;
	
    private Paint mPaint;
    private int mColor1;
    private int mColor2;
    private OnColorChangedListener mListener;

	/**
	 * Constructor. This version is only needed for instantiating the object
	 * manually (not from a layout XML file).
	 * 
	 * @param context
	 */
	public ColorSlider(Context context) {
		super(context);
		init();
	}

	/**
	 * Construct object, initializing with any attributes we understand from a
	 * layout file.
	 * 
	 * These attributes are defined in res/values/attrs.xml .
	 * 
	 * @see android.view.View#View(android.content.Context,
	 *      android.util.AttributeSet, java.util.Map)
	 */
	public ColorSlider(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO what happens with inflateParams
		init();
	}

	/**
	 * Initializes variables.
	 */
	void init() {
		
		mColor1 = 0xFFFFFFFF;
		mColor2 = 0xFF000000;

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Paint.Style.FILL);
	}

    @Override 
    protected void onDraw(Canvas canvas) {
		Shader s = new LinearGradient(0, 0, 0, getHeight(), mColor1, mColor2, Shader.TileMode.CLAMP);
        mPaint.setShader(s);
    	
    	canvas.drawRect(0, 0, getWidth(), getHeight(), mPaint);
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
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
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
			// Default width:
			result = defaultWidth;
			if (specMode == MeasureSpec.AT_MOST) {
				// Respect AT_MOST value if that was what is called for by
				// measureSpec
				result = Math.min(result, specSize);
			}
		}
		return result;
	}

	/**
	 * Determines the height of this view
	 * 
	 * @param measureSpec
	 *            A measureSpec packed into an int
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
			// Default height
			result = defaultHeight;
			if (specMode == MeasureSpec.AT_MOST) {
				// Respect AT_MOST value if that was what is called for by
				// measureSpec
				result = Math.min(result, specSize);
			}
		}
		return result;
	}
    
	public void setColors(int color1, int color2) {
		mColor1 = color1;
		mColor2 = color2;
        
        invalidate();
	}

	public void setOnColorChangedListener(
			OnColorChangedListener colorListener) {
		mListener = colorListener;
	}
    
    private int ave(int s, int d, float p) {
        return s + java.lang.Math.round(p * (d - s));
    }

    private int interpColor(int color1, int color2, float unit) {
        if (unit <= 0) {
            return color1;
        }
        if (unit >= 1) {
            return color2;
        }
        
        float p = unit;

        // now p is just the fractional part [0...1) and i is the index
        int c0 = color1;
        int c1 = color2;
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);
        
        return Color.argb(a, r, g, b);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            	
            	float unit = (float) y / ((float) getHeight()); 
            	
                int newcolor = interpColor(mColor1, mColor2, unit);

            	if (mListener != null) {
            		mListener.onColorChanged(this, newcolor);
            	}
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                
                break;
        }
        return true;
    }
}
