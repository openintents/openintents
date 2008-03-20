package org.openintents.applications.splashplay;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.util.Log;
import android.view.View;

/**
 *  Displays a guitar fretboard.
 *  
 */
public class FretboardView extends View {
	private static final String TAG = "SplashPlay"; 
	
	/** Indicates void string */
	public static final int MARKER_VOID = -1;
	
	private ShapeDrawable[] mDrawables;
	
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
	
	/** Markers on the fretboard that show position
	 * where to place fingers.
	 * 
	 * 1, 2, 3, ...: Finger at fret 1, 2, 3...
	 * 0: no finger
	 * MARKER_VOID: String should not sound.
	 */
	private int[] mMarkerList;
	
	/** Empty marker list */
	private final int[] MARKERLIST_EMPTY;
    
	public FretboardView(Context context) {
		super(context);
		setFocusable(true);
		
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
			fretPos[i] = (float) (1 - pos);
			pos = pos * factor;
		}
		
		// Calculate position of markers
		for (int i=1; i<fretMax; i++) {
			markerPos[i] = 0.5f * (fretPos[i-1] + fretPos[i]);
		}
		
		// Calculate position of strings
		for (int i=0; i<stringMax; i++) {
			stringPos[i] = (i + 0.5f) / stringMax;
		}
		
		stringNum = stringMax;
		fretNum = fretMax;
		fretPosMax = 0.25f;
		
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor( Color.RED );
        mPaint.setStrokeWidth(4);
        
        mMatrix = new Matrix();
        
        mRectF = new RectF();
        
        // Empty: no frets touched
        MARKERLIST_EMPTY = new int[] {0,1,2,3,4,5}; //{0,0,0,0,0,0};
        
        mMarkerList = MARKERLIST_EMPTY;
	}

	/** 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		//int x = canvas.getBitmapWidth();
		//int y = canvas.getBitmapHeight();
		
		Rect r = canvas.getClipBounds();
	
		//Log.i(TAG, "onDraw: " + x + ", " + y);
		//Log.i(TAG, "onDraw: " + " (" + r.left + ", " + r.top + ") - (" + r.right + ", " + r.bottom + ") ");
		int width = r.width();
		int height = r.height();
		/*
		mPaint.setColor(Color.RED);
		canvas.drawCircle(x / 2, y / 2, x / 2, mPaint);
		
		mPaint.setColor(Color.BLUE);
		canvas.drawCircle(x / 2, y / 2, y / 2, mPaint);
		*/
		// draw background:
		mPaint.setColor(0xFF885500);
		canvas.drawRect(0, 0, width, height, mPaint);
		
		// draw frets
		mPaint.setColor(Color.WHITE);
		mPaint.setStrokeWidth(0);
		canvas.save();
		float[] src = new float[] {0, 0, 0, 1,      fretPosMax,     1}; // fretboard coordinates
		float[] dst = new float[] {0, 0, 0, height, width, height}; // view coordinates
		mMatrix.setPolyToPoly(src, 0, dst, 0, src.length >> 1);
		canvas.concat(mMatrix);
		for (int i = 0; i<fretNum; i++) {
			canvas.drawLine(fretPos[i], 0, fretPos[i], 1, mPaint);
		}
		mPaint.setColor(Color.LTGRAY);
		for (int i = 0; i<stringNum; i++) {
			canvas.drawLine(0, stringPos[i], 1, stringPos[i], mPaint);
		}
		
		// draw markers
		mPaint.setColor(Color.BLUE);
		
		// Size of markers, taking into account
		// aspect ratio through transformation matrix:
		float dy = 0.05f;
		float dx = dy * (fretPosMax * height) / width;
		
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
	
}
