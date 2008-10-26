package org.openintents.widget;

import org.openintents.flashlight.ColorPickerDialog.OnColorChangedListener;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * ColorCircle.
 * 
 * @author Peli, based on API demo code.
 * 
 */
public class ColorCircle extends View {

	/** Default widget width */
	public int defaultWidth;

	/** Default widget height */
	public int defaultHeight;
	
    private Paint mPaint;
    private Paint mCenterPaint;
    private int[] mColors;
    private OnColorChangedListener mListener;
    
    private static final int CENTER_X = 100;
    private static final int CENTER_Y = 100;
    private static final int CENTER_RADIUS = 32;

	/**
	 * Constructor. This version is only needed for instantiating the object
	 * manually (not from a layout XML file).
	 * 
	 * @param context
	 */
	public ColorCircle(Context context) {
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
	public ColorCircle(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO what happens with inflateParams
		init();
	}

	/**
	 * Initializes variables.
	 */
	void init() {
		defaultWidth = CENTER_X * 2;
		defaultHeight = CENTER_Y * 2;
		
        mColors = new int[] {
            0xFFFF0000, 0xFFFF00FF, 0xFF0000FF, 0xFF00FFFF, 0xFF00FF00,
            0xFFFFFF00, 0xFFFF0000
        };
        Shader s = new SweepGradient(0, 0, mColors, null);
        
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setShader(s);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(32);
        
        mCenterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mCenterPaint.setStrokeWidth(5);
	}
    
    private boolean mTrackingCenter;
    private boolean mHighlightCenter;

    @Override 
    protected void onDraw(Canvas canvas) {
        float r = CENTER_X - mPaint.getStrokeWidth()*0.5f;
        
        canvas.translate(CENTER_X, CENTER_X);
        
        canvas.drawOval(new RectF(-r, -r, r, r), mPaint);            
        canvas.drawCircle(0, 0, CENTER_RADIUS, mCenterPaint);
        
        if (mTrackingCenter) {
            int c = mCenterPaint.getColor();
            mCenterPaint.setStyle(Paint.Style.STROKE);
            
            if (mHighlightCenter) {
                mCenterPaint.setAlpha(0xFF);
            } else {
                mCenterPaint.setAlpha(0x80);
            }
            canvas.drawCircle(0, 0,
                              CENTER_RADIUS + mCenterPaint.getStrokeWidth(),
                              mCenterPaint);
            
            mCenterPaint.setStyle(Paint.Style.FILL);
            mCenterPaint.setColor(c);
        }
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
    
	public void setColor(int color) {
        mCenterPaint.setColor(color);
	}

	public void setOnColorChangedListener(
			OnColorChangedListener colorListener) {
		mListener = colorListener;
	}
	
    private int floatToByte(float x) {
        int n = java.lang.Math.round(x);
        return n;
    }
    private int pinToByte(int n) {
        if (n < 0) {
            n = 0;
        } else if (n > 255) {
            n = 255;
        }
        return n;
    }
    
    private int ave(int s, int d, float p) {
        return s + java.lang.Math.round(p * (d - s));
    }
    
    private int interpColor(int colors[], float unit) {
        if (unit <= 0) {
            return colors[0];
        }
        if (unit >= 1) {
            return colors[colors.length - 1];
        }
        
        float p = unit * (colors.length - 1);
        int i = (int)p;
        p -= i;

        // now p is just the fractional part [0...1) and i is the index
        int c0 = colors[i];
        int c1 = colors[i+1];
        int a = ave(Color.alpha(c0), Color.alpha(c1), p);
        int r = ave(Color.red(c0), Color.red(c1), p);
        int g = ave(Color.green(c0), Color.green(c1), p);
        int b = ave(Color.blue(c0), Color.blue(c1), p);
        
        return Color.argb(a, r, g, b);
    }
    
    private int rotateColor(int color, float rad) {
        float deg = rad * 180 / 3.1415927f;
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        
        ColorMatrix cm = new ColorMatrix();
        ColorMatrix tmp = new ColorMatrix();

        cm.setRGB2YUV();
        tmp.setRotate(0, deg);
        cm.postConcat(tmp);
        tmp.setYUV2RGB();
        cm.postConcat(tmp);
        
        final float[] a = cm.getArray();

        int ir = floatToByte(a[0] * r +  a[1] * g +  a[2] * b);
        int ig = floatToByte(a[5] * r +  a[6] * g +  a[7] * b);
        int ib = floatToByte(a[10] * r + a[11] * g + a[12] * b);
        
        return Color.argb(Color.alpha(color), pinToByte(ir),
                          pinToByte(ig), pinToByte(ib));
    }
    
    private static final float PI = 3.1415926f;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX() - CENTER_X;
        float y = event.getY() - CENTER_Y;
        boolean inCenter = java.lang.Math.sqrt(x*x + y*y) <= CENTER_RADIUS;
        
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTrackingCenter = inCenter;
                if (inCenter) {
                    mHighlightCenter = true;
                    invalidate();
                    break;
                }
            case MotionEvent.ACTION_MOVE:
                if (mTrackingCenter) {
                    if (mHighlightCenter != inCenter) {
                        mHighlightCenter = inCenter;
                        invalidate();
                    }
                } else {
                    float angle = (float)java.lang.Math.atan2(y, x);
                    // need to turn angle [-PI ... PI] into unit [0....1]
                    float unit = angle/(2*PI);
                    if (unit < 0) {
                        unit += 1;
                    }
                    int newcolor = interpColor(mColors, unit);
                    mCenterPaint.setColor(newcolor);

                	if (mListener != null) {
                		mListener.onColorChanged(this, newcolor);
                	}
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTrackingCenter) {
                    if (inCenter) {
                    	if (mListener != null) {
                    		mListener.onColorPicked(this, mCenterPaint.getColor());
                    	}
                    }
                    mTrackingCenter = false;    // so we draw w/o halo
                    invalidate();
                }
                break;
        }
        return true;
    }
    
	/**
	 * Interface for notifications of position change of slider.
	 * 
	 */
	public static interface OnColorChangedListener {

		/**
		 * This method is called when the user changed the color.
		 * 
		 * This works in touch mode, by dragging the along the 
		 * color circle with the finger.
		 */
		void onColorChanged(ColorCircle colorcircle, int newColor);
		
		/**
		 * This method is called when the user clickes the center button.
		 * 
		 * @param colorcircle
		 * @param newColor
		 */
		void onColorPicked(ColorCircle colorcircle, int newColor);
	}

}
