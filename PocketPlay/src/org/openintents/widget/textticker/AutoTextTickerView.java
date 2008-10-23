package org.openintents.widget.textticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.util.Log;

/**
 * Displays a list of chords.
 * 
 * This list reflects the chords in the song.
 * 
 * @author Peli
 *
 */
public class AutoTextTickerView extends View {
	private static final String TAG = "AutoTextTickerView"; 

	String mText;

	private int mTextWidth;
    private int mAscent;
	private Paint mPaint;
	private int mWidth;
	
	private int mTextPadding=2;

	private int mDownState;
	
	final static int STATE_UNTOUCHED = 0;
	final static int STATE_TOUCH = 1;
	final static int STATE_SCROLL = 4;

	/**
	 * Waiting time in milliseconds before ticker starts scrolling.
	 */
	public int mWaitingTime = 3000;
	
	/**
	 * Time between animation updates.
	 */
	public int mAnimationTime = 250;
	
	/**
	 * Scroll velocity in pixel per mAnimationTime.
	 */
	public float mScrollVelocity = 0.02f;
	
	/** Message for handler */
	/** Kinetic scrolling is when flinged by touch */
	public static final int MSG_KINETIC_SCROLLING = 0;
	
	/** Pause scrolling of display */
	public static final int MSG_PAUSE_SCROLLING = 1;
	
	/** Automatic scrolling of display */
	public static final int MSG_AUTO_SCROLLING = 2;
	
	float mOldX;
	long mOldTime;
	int mOldStartingOffset;
	
	float mKineticVelocityX;
	private float mKineticDampingFactor = 0.9f;
	
	/** Factor for averaging over several move events */
	float mKineticVelocityAdaptation = 0.4f;
	
	/** Time in milliseconds above which no adaptation is
	 * done but the velocity is calculated directly from this and 
	 * the previous event.
	 */
	long mNoAdaptationTime = 50;
	
	// Offset in pixel
	// (float for flinging)
	float mCurrentStartingOffset;
	
	int mCurrentTextSize;
	
	
	int mFaderange;
	
	/**
	 * Prevent scrolling, e.g. while the song is playing,
	 * to reserve CPU to other tasks than text display.
	 */
	boolean mPreventScrolling;
	
	private static final int[] mFadeout = new int[] { 0x00FFFFFF, 0xFFFFFFFF };
	private static final int[] mFadein = new int[] { 0xFFFFFFFF, 0x00FFFFFF };
    
	
	/**
     * Constructor for manual instantiation.
     * @param context
     */
    public AutoTextTickerView(Context context) {
		super(context);
		initTextTicker();
	}
    

    /**
     * Construct object through layout XML file.
     * 
     * Known attributes are initialized from a
     * layout file. 
     * 
     * These attributes are defined in res/values/attrs.xml .
     * 
     * @see android.view.View#View(android.content.Context, android.util.AttributeSet, java.util.Map)
     */
    public AutoTextTickerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub

		initTextTicker();
	}

	private void initTextTicker() {
		
		mCurrentStartingOffset = 0;
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		
		mText = "";
		
		mPreventScrolling = false;
		
		initFadeRegion();
	}

	
	
	public void resize(int newheight) {
		mCurrentTextSize = newheight * 3 / 4;
		mFaderange = newheight;
		
	}
	
	public void setTextColor(int color) {
		mPaint.setColor(color);
	}

	public void setTextPadding(int padding){
		mTextPadding=padding;
	}
	
	public void setTextSize(float size) {
		mPaint.setTextSize(size);
		requestLayout();
        invalidate();
	}
	
	public void setText(CharSequence text) {
		if (text==null)
		{
			text="";
		}		
		mText = text.toString();
		mCurrentStartingOffset = 0;
		
		removeAllMessages();
		
		//mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PAUSE_SCROLLING), mWaitingTime);
		triggerAutoScrolling();
	}
	
	public void triggerAutoScrolling(){
		Log.d(TAG,"-----------------------------------TRIGGER AUTO SCROLLING----------------------------");
		mHandler.sendMessage(mHandler.obtainMessage(MSG_AUTO_SCROLLING));
		mPreventScrolling=false;


	}
	public void setPreventScrolling(boolean preventscrolling) {
		mPreventScrolling = preventscrolling;
	}
	
	/**
	 * Does nothing, as the ticker only has 1 line.
	 * @param lines
	 */
	public void setLines(int lines) {
		
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
            mTextWidth = (int) mPaint.measureText(mText) + getPaddingLeft()
                    + getPaddingRight();
            result = mTextWidth;
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

        mAscent = (int) mPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be
            result = specSize;
        } else {
            // Measure the text (beware: ascent is a negative number)
            result = (int) (-mAscent + mPaint.descent()) + getPaddingTop()
                    + getPaddingBottom();
            if (specMode == MeasureSpec.AT_MOST) {
                // Respect AT_MOST value if that was what is called for by measureSpec
                result = Math.min(result, specSize);
            }
        }
        return result;
    }

	/** 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
//		Log.d(TAG, "in onDraw");
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		Drawable d;
		Paint p;
		
		int width = getWidth();
		int height = getHeight();
		
		mWidth = width;
		
		// Ensure valid range
		if (mCurrentStartingOffset > width) {
			mCurrentStartingOffset = -mTextWidth;
		} else if (mCurrentStartingOffset < -(width+mTextPadding)) {
			mCurrentStartingOffset = (width-mTextPadding);
		}
		/* */
		// Draw chord symbols
		int x = 0;
		int y = 0;
		
		// Add initial padding
		int paddingx = 0;
		x += paddingx;

		x += mCurrentStartingOffset;
//		Log.d(TAG,"onDraw: mText>"+mText+"< x>"+x+"< y>"+y+"< width>"+width+"< currentOffset>"+mCurrentStartingOffset);
		canvas.drawText(mText, x, y  - mAscent, mPaint);
		
		
		// Finally put fade out region on top of it:
		/*
		// fade out:
		fadeRegion(canvas, width, height, width - mFaderange, width, mFadeout);
        
		// fade in:
		int faderange = getLeftFaderange(mFaderange);
		fadeRegion(canvas, width, height, 0, faderange, mFadein);
        */
	}

	/**
	 * Return the fade range to the left.
	 * If all chords are far away to the left, then return maxrange.
	 * If the first chord is at the beginning, then return 0.
	 * Linearly interpolate inbetween.
	 * @param maxrange
	 * @return
	 */
	private final int getLeftFaderange(int maxrange) {
		int offset = (int) -mCurrentStartingOffset;
		if (offset >= maxrange) {
			return maxrange;
		} else {
			return offset;
		}
	}

	Paint mAlphaPaint;
	
	private final void initFadeRegion() {
		mAlphaPaint = new Paint();
        mAlphaPaint.setFilterBitmap(false);
        mAlphaPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
	}
	
	private final void fadeRegion(Canvas canvas, int width, int height, int x1,
			int x2, int[] colorlist) {
		Drawable d;
		int faderange = x2 - x1;
		if (faderange <= 0) {
			// Nothing to fade
			return;
		}
		int sc = canvas.saveLayer(x1, 0, x2, height, null, 
				Canvas.MATRIX_SAVE_FLAG |
                Canvas.CLIP_SAVE_FLAG |
                Canvas.HAS_ALPHA_LAYER_SAVE_FLAG |
                Canvas.FULL_COLOR_LAYER_SAVE_FLAG |
                Canvas.CLIP_TO_LAYER_SAVE_FLAG);
		/*
		// Draw default background
		d = mDrawables[BACKGROUND];
		d.setBounds(0, 0, width, height);
		canvas.clipRect(x1, 0, x2, height);
		d.draw(canvas);
		*/
		
		// Draw alpha filter
        Bitmap alphaFilter = makeAlphaFilter(faderange, height, colorlist);
        canvas.drawBitmap(alphaFilter, x1, 0, mAlphaPaint);
        
		canvas.restoreToCount(sc);
	}
	
	
	int mAlphaFilterWidth = -1;
	int mAlphaFilterHeight = -1;
	Bitmap mAlphaFilterBitmap;
    Canvas mAlphaFilterCanvas;
    Paint mAlphaFilterPaint;
    //Shader mAlphaFilterShader;
    LinearGradient mAlphaFilterShader;
    
	private void prepareAlphaFilter(int w, int h) {
		if (w > mAlphaFilterWidth || h > mAlphaFilterHeight) {
			mAlphaFilterWidth = w;
			mAlphaFilterHeight = h;
			
			mAlphaFilterBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
	        mAlphaFilterCanvas = new Canvas(mAlphaFilterBitmap);
	        mAlphaFilterPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		} else {
			mAlphaFilterBitmap.eraseColor(0x8888);
		}
	}
	
	int wOld = -1;
	int hOld = -1;
	int[] colorlistOld = null;
	
	private Bitmap makeAlphaFilter(int w, int h, int[] colorlist) {
		prepareAlphaFilter(w, h);
		if (w != wOld || h != hOld || colorlist != colorlistOld) {
			mAlphaFilterShader = new LinearGradient(0, 0, w, 0,
					colorlist, null,
					Shader.TileMode.CLAMP);
	        mAlphaFilterPaint.setShader(mAlphaFilterShader);
			wOld = w;
			hOld = h;
			colorlistOld = colorlist;
		}
		mAlphaFilterCanvas.drawRect(0, 0, w, h, mAlphaFilterPaint);
		return mAlphaFilterBitmap;
    }
	
	float mKineticAccelerationX;
	int mDesiredPosition = 80;
	
	
	/**
	 * Obtain the distance of the element at 'position' to the 
	 * left edge of the Chord View.
	 * @param position
	 * @param x
	 * @return
	 */
	private final int getDistance(int position) {
		// First get the distance from the left most screen element to position
		int totaloffset = (int) mCurrentStartingOffset;
		
		return totaloffset;
	}
	
	
	
	/* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		long time = event.getEventTime();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			touchDown(x, y, time);
			break;
		case MotionEvent.ACTION_MOVE:
			touchMove(x, y, time);
			break;
		case MotionEvent.ACTION_UP:
			touchUp(x, y, time);
			break;
		} 
		return true;
	}

	
	private final void touchDown(float x, float y, long time) {
		invalidate();
		mShowHighlightPosition = false;
		
		removeAllMessages();
		
		// Start scrolling
		startScrolling(x, time);
			
	}


	


	private final void startScrolling(float x, long time) {
		mOldX = x;
		mOldTime = time;
		mKineticVelocityX = 0;
		mDownState = STATE_SCROLL;
	}

	
	private final void touchMove(float x, float y, long time) {
		mShowHighlightPosition = false;
		
		if (mDownState == STATE_SCROLL) {
			mCurrentStartingOffset += x - mOldX;
			mKineticVelocityX = (x - mOldX) / (time - mOldTime) * mKineticVelocityAdaptation
				+ mKineticVelocityX * (1-mKineticVelocityAdaptation);
			
			if (time - mOldTime > mNoAdaptationTime) {
				mKineticVelocityX = 0;
				mKineticVelocityX = (x - mOldX) / (time - mOldTime);
				//Log.i(TAG, "x" + x + ", mOldX " + mOldX + ", time " + time + ", mOldTime " + mOldTime);
			}
			//mKineticVelocityX = (x - mOldX) / (time - mOldTime);
			
			mOldX = x;
			mOldTime = time;
			invalidate();
		} 
	}

	private final void touchUp(float x, float y, long time) {
		switch (mDownState) {
		case STATE_SCROLL:
			// Do we have some momentum for kinetic scrolling?
			//mKineticVelocityX = (x - mOldX) / (time - mOldTime);
			if (time - mOldTime > mNoAdaptationTime) {
				mKineticVelocityX = 0;
				mKineticVelocityX = (x - mOldX) / (time - mOldTime);
				//Log.i(TAG, "UP x" + x + ", mOldX " + mOldX + ", time " + time + ", mOldTime " + mOldTime);
			}
			
			// Start kinetic scrolling:
			mOldTime = System.currentTimeMillis();
			mHandler.sendMessage(mHandler.obtainMessage(MSG_KINETIC_SCROLLING));
			
			break;
		}
		mDownState = STATE_UNTOUCHED;
		invalidate();
	}

	
	
	/** Handle the process of kinetic scrolling */
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_KINETIC_SCROLLING) {
				
				// Adjust the chord view:
				long time = System.currentTimeMillis();
				
				mCurrentStartingOffset += mKineticVelocityX * (time - mOldTime);
				
				mKineticVelocityX *= mKineticDampingFactor;
				//Log.i(TAG, "Fling: " + mKineticVelocityX);
				
				invalidate();
				
				mOldTime = time;
				//Log.i(TAG, "minimum" + ViewConfiguration.getMinimumFlingVelocity());
				if (Math.abs(mKineticVelocityX) > ViewConfiguration.getMinimumFlingVelocity() * 0.001) {
					mHandler.removeMessages(MSG_AUTO_SCROLLING);
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_KINETIC_SCROLLING), mAnimationTime);
				} else {
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PAUSE_SCROLLING), mWaitingTime);
				}
				
			} else if (msg.what == MSG_PAUSE_SCROLLING) {
				// Adjust the chord view:
			//	Log.d(TAG,"in hnadler: MSG_PAUSE");

				mHandler.removeMessages(MSG_AUTO_SCROLLING);
				long time = System.currentTimeMillis();
				
				mOldTime = time;
				//Log.i(TAG, "measuredWidth" + mTextWidth + ", mWidth " + mWidth);
				if (mPreventScrolling) {
					// Don't scroll right now, let's wait again
	//				Log.d(TAG,"Handler: next MSG will PAUSE");
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_PAUSE_SCROLLING), mWaitingTime);
				} else if (mTextWidth > mWidth) {
	//				Log.d(TAG,"Handler: next MSG will AUTO_SCROLL, animTIme>"+mAnimationTime);
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_AUTO_SCROLLING), mAnimationTime);
				}
			} else if (msg.what == MSG_AUTO_SCROLLING) {
				// Adjust the chord view:
//				Log.d(TAG,"in hnadler: MSG_AUTO_SCROLLING");
				long time = System.currentTimeMillis();
				
				float oldOffset = mCurrentStartingOffset;
				mCurrentStartingOffset -= mScrollVelocity * (time - mOldTime);
//				Log.d(TAG,"in Handler: reduced offset to>"+mCurrentStartingOffset);
				invalidate();

				mOldTime = time;
				if (oldOffset > 0 && mCurrentStartingOffset <= mTextPadding) {
					// We just crossed the initial position. Stop for a moment.
					mCurrentStartingOffset = 0;
	//				Log.d(TAG,"Handler: next MSG will PAUSE");
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_AUTO_SCROLLING), mWaitingTime);
				} else {
//					Log.d(TAG,"Handler: next MSG will AUTO_SCROLL, animTIme>"+mAnimationTime);
					mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_AUTO_SCROLLING), mAnimationTime);
				}
			}
		}
		
	};
	
	void removeAllMessages() {
		mHandler.removeMessages(MSG_AUTO_SCROLLING);
		mHandler.removeMessages(MSG_PAUSE_SCROLLING);
		mHandler.removeMessages(MSG_KINETIC_SCROLLING);
	}
	
	
	boolean mShowHighlightPosition;
}
