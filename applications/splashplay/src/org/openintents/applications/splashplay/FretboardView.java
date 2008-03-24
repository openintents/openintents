package org.openintents.applications.splashplay;

import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 *  Displays a guitar fretboard.
 *  
 * @author Peli
 *  
 */
public class FretboardView extends View {
	private static final String TAG = "SplashPlay"; 
	
	/** Indicates void string */
	public static final int MARKER_VOID = -1;
	
	/** Contains graphics for background wood. */
	private Drawable[] mDrawables;
	
	public static final int WOOD = 0;
	public static final int NUT = 1;
	public static final int FRET = 2;
	public static final int MARKER = 3;
	public static final int SPOT = 4;
	public static final int mDrawablesMax = 5;
	
	private Paint mPaint; 
	private Matrix  mMatrix;
	private RectF mRectF;
    
	/** Position of frets.
	 * Position along the x-axis from 0 to 1.*/
	private float[] fretPos;
	/** Position of strings.
	 * Position along the y-axis from 0 to 1. */
	private float[] stringPos;
	/** Position of markers.
	 * Position along the x-axis from 0 to 1. */
	private float[] markerPos;
	
	/** Maximum number of frets */
	private final int fretMax = 13;
	/** Maximum number of strings */
	private final int stringMax = 6;
	
	/** Number of frets displayed */
	public int fretNum;
	/** Number of strings displayed */
	public int stringNum;
	/** Maximum position of fretboard displayed */
	public float fretPosMax;
	
	private Paint[] mStringPaint;
	
	/** Markers on the fretboard that show position
	 * where to place fingers.
	 * 
	 * 1, 2, 3, ...: Finger at fret 1, 2, 3...
	 * 0: no finger
	 * MARKER_VOID: String should not sound.
	 */
	private int[] mMarkerList;
	
	/** Empty marker list.
	 * 	Empty means no frets are touched.
	 */
	private final int[] MARKERLIST_EMPTY
		= new int[] {0,1,2,3,4,5}; //{0,0,0,0,0,0};
	
	private float mNutWidth;
	private float mFretWidth;
	
	/* Radius of marker */
	private float mMarkerRadius;
	
	private float mMarkerOffsetX;
	
	/** Overall scale.
	 * This sets the maximum length and width
	 * of the fretboard. A large scale is necessary, 
	 * because Drawable currently only allows integer input.
	 * 
	 */
	private final int SCALE = 1024;
	
	/**
     * Constructor for manual instantiation.
     * @param context
     */
    public FretboardView(Context context) {
		super(context);
		//setFocusable(true);
		
		initFretboard();
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
    public FretboardView(Context context, AttributeSet attrs, Map inflateParams) {
		super(context, attrs, inflateParams);
		// TODO Auto-generated constructor stub

		initFretboard();
	}

	private void initFretboard() {
		fretPos = new float[fretMax];
		stringPos = new float[stringMax];
		markerPos = new float[fretMax];
		
		// multiplying factor 12 times by itself
		// gives 1/2.
		// factor is in musical terms the
		// geometric distance of a minor second
		// (half step).
		double factor = 1 / Math.pow(2., 1./12.);
		double pos = 1;
		
		// Calculate position of frets
		for (int i=0; i<fretMax; i++) {
			fretPos[i] = (float) (SCALE * (1 - pos));
			pos = pos * factor;
		}
		
		// Calculate position of markers
		for (int i=1; i<fretMax; i++) {
			markerPos[i] = 0.5f * (fretPos[i-1] + fretPos[i]);
		}
		
		// Calculate position of strings
		for (int i=0; i<stringMax; i++) {
			stringPos[i] = SCALE * (i + 0.5f) / stringMax;
		}
		
		stringNum = stringMax;
		fretNum = fretMax;
		fretPosMax = SCALE * 0.27f;
		
		mNutWidth = SCALE * 0.015f;
		mFretWidth = SCALE * 0.006f;
		mMarkerRadius = SCALE * 0.07f;
		mMarkerOffsetX = SCALE * 0.003f;
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor( Color.RED );
        mPaint.setStrokeWidth(4);
        
        mMatrix = new Matrix();
        
        mRectF = new RectF();
        
        mMarkerList = MARKERLIST_EMPTY;
        
        mDrawables = new Drawable[mDrawablesMax];
        mStringPaint = new Paint[stringMax];
        for (int i=0; i<stringMax; i++) {
        	mStringPaint[i] = new Paint(Paint.ANTI_ALIAS_FLAG);
        }
        float stringwidth = SCALE * 0.006f;
        int lightString = 0xffffffff;
        int darkString = 0xffbb7733;
        mStringPaint[0].setColor(lightString);
        mStringPaint[0].setStrokeWidth(stringwidth);
        mStringPaint[1].setColor(lightString);
        mStringPaint[1].setStrokeWidth(stringwidth);
        mStringPaint[2].setColor(lightString);
        mStringPaint[2].setStrokeWidth(stringwidth);
        mStringPaint[3].setColor(darkString);
        mStringPaint[3].setStrokeWidth(2*stringwidth);
        mStringPaint[4].setColor(darkString);
        mStringPaint[4].setStrokeWidth(3*stringwidth);
        mStringPaint[5].setColor(darkString);
        mStringPaint[5].setStrokeWidth(4*stringwidth);
        
        
	}

	/** 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		Drawable d;
		
		//int x = canvas.getBitmapWidth();
		//int y = canvas.getBitmapHeight();
		
		//Rect r = canvas.getClipBounds();
	
		//Log.i(TAG, "onDraw: " + x + ", " + y);
		//Log.i(TAG, "onDraw: " + " (" + r.left + ", " + r.top + ") - (" + r.right + ", " + r.bottom + ") ");
		//int width = r.width();
		//int height = r.height();
		int width = getWidth();
		int height = getHeight();
		/*
		mPaint.setColor(Color.RED);
		canvas.drawCircle(x / 2, y / 2, x / 2, mPaint);
		
		mPaint.setColor(Color.BLUE);
		canvas.drawCircle(x / 2, y / 2, y / 2, mPaint);
		*/
		
		
		// Set transformation
		canvas.save();
		float[] src = new float[] {-mNutWidth, 0, -mNutWidth, SCALE, fretPosMax, SCALE}; // fretboard coordinates
		float[] dst = new float[] {0, 0, 0, height, width, height}; // view coordinates
		mMatrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1);
		canvas.concat(mMatrix);
		float aspectratio = (fretPosMax * height) / (width * SCALE);
		
		// draw background:
		d = mDrawables[WOOD];
		if (d != null) {
			d.setBounds(0, 0, SCALE, SCALE);
			d.draw(canvas);
		} else {
			mPaint.setColor(0xFF885500);
			canvas.drawRect(0, 0, SCALE, SCALE, mPaint);
		}
		
		// Draw nut
		d = mDrawables[NUT];
		if (d != null) {
			d.setBounds((int) (fretPos[0] - mNutWidth), 0, (int) (fretPos[0]), SCALE);
			d.draw(canvas);
		} else {
			mPaint.setColor(Color.WHITE);
			canvas.drawLine(fretPos[0]-mNutWidth, 0,fretPos[0], SCALE, mPaint);
		}
		
		// Draw frets
		d = mDrawables[FRET];
		if (d != null) {
			for (int i = 1; i<fretNum; i++) {
				d.setBounds((int) ((fretPos[i] - mFretWidth)), 0, (int) ((fretPos[i])), SCALE);
				d.draw(canvas);
			}
		} else {
			mPaint.setColor(Color.WHITE);
			mPaint.setStrokeWidth(0);
			for (int i = 0; i<fretNum; i++) {
				canvas.drawLine(fretPos[i], 0, fretPos[i], SCALE, mPaint);
			}
		}
		
		// draw markers
		d = mDrawables[MARKER];
		if (d != null) {
			int middle = SCALE / 2;
			// Size of markers, taking into account
			// aspect ratio through transformation matrix:
			float dy = mMarkerRadius;
			float dx = dy * aspectratio;
				
			float x = markerPos[3] - mMarkerOffsetX;
			d.setBounds((int) (x - dx), (int) (middle - dy),
					(int) (x + dx), (int) (middle + dy));
			d.draw(canvas);
			x = markerPos[5] - mMarkerOffsetX;
			d.setBounds((int) (x - dx), (int) (middle - dy),
					(int) (x + dx), (int) (middle + dy));
			d.draw(canvas);
		}
		
		// draw strings
		//mPaint.setColor(Color.LTGRAY);
		for (int i = 0; i<stringNum; i++) {
			canvas.drawLine(-mNutWidth, stringPos[i], SCALE, stringPos[i], mStringPaint[i]);
		}
		
		// draw markers
		mPaint.setColor(Color.BLUE);
		
		// Size of markers, taking into account
		// aspect ratio through transformation matrix:
		float dy = mMarkerRadius;
		float dx = dy * aspectratio;
		
		// draw markers
		d = mDrawables[SPOT];
		if (d != null) {
			for (int i = 0; i<stringNum; i++) {
				int m = mMarkerList[i];
				if (m > 0) {
					// draw a filled marker
					//canvas.drawCircle(markerPos[m], stringPos[i], 0.1f, mPaint);
					float x = markerPos[m] - mMarkerOffsetX;
					float y = stringPos[i];
					
					d.setBounds((int) (x - dx), (int) (y - dy), 
							(int) (x + dx), (int) (y + dy));
					d.draw(canvas);
				}
			}
		} else {
			for (int i = 0; i<stringNum; i++) {
				int m = mMarkerList[i];
				if (m > 0) {
					// draw a filled marker
					//canvas.drawCircle(markerPos[m], stringPos[i], 0.1f, mPaint);
					float x = markerPos[m];
					float y = stringPos[i];
					mRectF.set(x - dx, y - dy, x + dx, y + dy);
					canvas.drawOval(mRectF, mPaint);
				}
			}
		}
		
		canvas.restore();
		
		//canvas.draw
	}
	
	/**
	 * Sets markers to new positions.
	 * 
	 * @param markerList Values that indicate positions of markers
	 */
	void setMarkerList(int[] markerList) {
		mMarkerList = markerList;
		
		// For now, invalidated the whole view.
		// Later this can be optimized to only invalidate
		// the regions that change.
		invalidate();
	}
	
	/**
	 * Sets markers to new positions given by chord.
	 * 
	 * @param chord Chord that contains positions of markers
	 */
	void setChord(Chord chord) {
		mMarkerList = chord.markerList;
		
		// For now, invalidated the whole view.
		// Later this can be optimized to only invalidate
		// the regions that change.
		invalidate();
	}
	
	/** 
	 * Sets a drawable according to the parameters.
	 * @param kind One of the types WOOD, NUT, FRET, MARKER.
	 * @param drawable The drawable that represents that type.
	 */
	public void setDrawable(int type, Drawable drawable) {
		mDrawables[type] = drawable;
	}
	
	/** 
	 * Sets a drawable according to the parameters.
	 * @param kind One of the types WOOD, NUT, FRET, MARKER.
	 * @param drawable_id The resource ID of the drawable that represents that type.
	 */
	public void setDrawable(int type, int drawable_id) {
		mDrawables[type] = mContext.getResources().getDrawable(drawable_id);
	}
}
