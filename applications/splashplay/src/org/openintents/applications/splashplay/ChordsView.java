package org.openintents.applications.splashplay;

import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.util.AttributeSet;
import android.view.View;

/**
 * Displays a list of chords.
 * 
 * This list reflects the chords in the song.
 * 
 * @author Peli
 *
 */
public class ChordsView extends View {
	private static final String TAG = "SplashPlay"; 

	public Song mSong;
	
	public int mTileWidth;
	public int mTileHeight;
	
	private Paint mPaintBackground; 
	private Paint mPaintText; 
	private Paint mPaintHighlight; 
	ShapeDrawable mHighlight;
	
	/**
     * Constructor for manual instantiation.
     * @param context
     */
    public ChordsView(Context context) {
		super(context);
		initChords();
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
    public ChordsView(Context context, AttributeSet attrs, Map inflateParams) {
		super(context, attrs, inflateParams);
		// TODO Auto-generated constructor stub

		initChords();
	}

	private void initChords() {
		
		mTileWidth = 40;
		mTileHeight = 40;
		
		mPaintBackground = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintBackground.setColor(0xFFFFFFFF);
		
		mPaintText = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintText.setColor(0xFF000000);
		mPaintText.setTextSize(mTileHeight * 3 / 4);
		mPaintText.setTextAlign(Paint.Align.CENTER);
		
		
		mPaintHighlight = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaintHighlight.setColor(0xFFff0000);
		
		float[] outerR = new float[] { 8, 8, 8, 8, 8, 8, 8, 8 };
        RectF   inset = new RectF(4, 4, 4, 4);
        float[] innerR = new float[] { 4, 4, 4, 4, 4, 4, 4, 4 };
        mHighlight = new ShapeDrawable(new RoundRectShape(outerR, inset,
                innerR));
		Shader highlightShader = new LinearGradient(0, 0, mTileWidth, mTileHeight,
                new int[] { 0xFFCCCCFF, 0xFF0000FF },
                null, Shader.TileMode.CLAMP);
		mHighlight.setShader(highlightShader);
		
		
	}
	
	/** 
	 * @see android.view.View#onDraw(android.graphics.Canvas)
	 */
	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
		
		int width = getWidth();
		int height = getHeight();
		
		// Fill background white
		canvas.drawRect(0, 0, width, height, mPaintBackground);
		
		if (mSong == null) {
			canvas.drawText("(no song selected)", 20, 20, mPaintText);
			return;
		}
		
		// Draw chord symbols
		int x = 0;
		int y = 0;
		
		// test condition whether another tile is possible
		int testWidth = width - mTileWidth;
		int testHeight = height - mTileHeight;
		
		int cur = 0; // current position in song
		
		// x-alignment for centered text
		int textx = mTileWidth / 2 - 1;
		
		int texty = mTileHeight * 3 / 4;
		
		// Set first line containing current song position
		int tilesPerLine = width / mTileWidth;
		cur = (mSong.mCur / tilesPerLine) * tilesPerLine;
		// (this works because of the integer division)
		
		// Center vertically:
		int tilesPerHeight = height / mTileHeight;
		y = (height - tilesPerHeight * mTileHeight) / 2;
			
		while (cur < mSong.mMax) {
			// Highlight current chord
			if (cur == mSong.mCur) {
				// Highlight the current chord:
				//canvas.drawRect(x, y, x + mTileWidth, y + mTileHeight, mPaintHighlight);
				
				mHighlight.setBounds(x, y, x + mTileWidth - 1, y + mTileHeight - 1);
				mHighlight.draw(canvas);   
			}			
			
			canvas.drawText(mSong.events[cur].chord.name, x + textx, y + texty, mPaintText);
			
			
			// forward position
			cur++;
			x += mTileWidth;
			if (x > testWidth) {
				x = 0;
				y += mTileHeight;
				if (y > testHeight) {
					// Space full:
					break;
				}
			}
		}
		
		
	}
}
