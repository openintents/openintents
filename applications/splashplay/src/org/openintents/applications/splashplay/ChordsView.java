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
import android.view.MotionEvent;
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
	
	SplashPlay mSplashPlay; //Needed for repeat AB
	
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
		mPaintBackground.setColor(0xFFFFFFFF);
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
		
		// Draw all previous chords fainter
		mPaintText.setColor(0xFFaaaaaa);
		
		while (cur < mSong.mMax) {
			if (mSplashPlay != null) {
				// Mark repeat positions:
				if (mSplashPlay.mRepeatState == SplashPlay.REPEAT_B) {
					// In loop mode B, highlight marked songs
					// up to current position (that we read from slider):
					int timeRepeatStop = mSplashPlay.mSlider.pos;
					
					// extra treatment if user is touching region:
					if (mDownCreateLoop) timeRepeatStop = mSplashPlay.mRepeatStop;
					
					int time = mSong.times[cur];
					int next = cur + 1;
					if (next >= mSong.mMax) next = mSong.mMax - 1;
					int nextTime = mSong.times[next];
					if (nextTime >= mSplashPlay.mRepeatStart 
							&& time < timeRepeatStop) {
						// Draw the background yellow:
						mPaintBackground.setColor(0xFFFFFF88);
						canvas.drawRect(x, y, 
								x + mTileWidth - 1,  y + mTileHeight - 1, 
								mPaintBackground);
					}
				}
				if (mSplashPlay.mRepeatState == SplashPlay.REPEAT_LOOP) {
					// In loop mode, highlight marked songs:
					int time = mSong.times[cur];
					int next = cur + 1;
					if (next >= mSong.mMax) next = mSong.mMax - 1;
					int nextTime = mSong.times[next];
					if (nextTime >= mSplashPlay.mRepeatStart 
							&& time <= mSplashPlay.mRepeatStop) {
						// Draw the background green:
						mPaintBackground.setColor(0xFF88FF88);
						canvas.drawRect(x, y, 
								x + mTileWidth - 1,  y + mTileHeight - 1, 
								mPaintBackground);
					}
				}
			}
			
			// Highlight current chord
			if (cur == mSong.mCur) {
				// Highlight the current chord:
				//canvas.drawRect(x, y, x + mTileWidth, y + mTileHeight, mPaintHighlight);
				
				mHighlight.setBounds(x, y, x + mTileWidth - 1, y + mTileHeight - 1);
				mHighlight.draw(canvas);   
				
				// draw all following chords black:
				mPaintText.setColor(0xFF000000);
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

	private int mDownPos;
	private int mDownRepeatState;
	private int mDownRepeatStart;
	private int mDownRepeatStop;
	private boolean mDownCreateLoop;
	
	/* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			int newSongPos = getSongPos(x, y);
			
			// Set new song position:
			int newPos = mSong.times[newSongPos];
			mSplashPlay.seekToMusic(newPos);
			mSplashPlay.updateViews(newPos);
			mSplashPlay.mSlider.setPosition(newPos);
			mDownPos = newSongPos;
			mDownRepeatState = mSplashPlay.mRepeatState;
			mDownRepeatStart = mSplashPlay.mRepeatStart;
			mDownRepeatStop = mSplashPlay.mRepeatStop;
			mDownCreateLoop = false;
			
			break;
		case MotionEvent.ACTION_MOVE:
			newSongPos = getSongPos(x, y);
			
			/*
			 // Set new song position:
			newPos = mSong.times[newSongPos];
			mSplashPlay.seekToMusic(newPos);
			mSplashPlay.updateViews(newPos);
			mSplashPlay.mSlider.setPosition(newPos);
			*/
			
			if (newSongPos != mDownPos) {
				// Let us create a loop:
				mSplashPlay.mRepeatState = SplashPlay.REPEAT_B;
				int p1 = mDownPos;
				int p2 = newSongPos;
				if (p2 < p1) {
					// always keep smaller position the starting pos.
					p2 = mDownPos;
					p1 = newSongPos;
				}
				p2 = p2 + 1;
				if (p2 >= mSong.mMax) p2 = mSong.mMax - 1;
				mSplashPlay.mRepeatStart = mSong.times[p1] + 1;
				mSplashPlay.mRepeatStop = mSong.times[p2] - 1;
				mDownCreateLoop = true;
				mSplashPlay.updateRepeatButton();
			} else {
				mSplashPlay.mRepeatState = mDownRepeatState;
				mSplashPlay.mRepeatStart = mDownRepeatStart;
				mSplashPlay.mRepeatStop = mDownRepeatStop;
				mDownCreateLoop = false;
				mSplashPlay.updateRepeatButton();
			}
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			if (mDownCreateLoop) {
				// Let us create a loop:
				mSplashPlay.mRepeatState = SplashPlay.REPEAT_LOOP;
				mSplashPlay.updateRepeatButton();
				
				// Start playing automatically:
				mSplashPlay.playMusic(false);
				mDownCreateLoop = false;
			}
			invalidate();
			break;
		} 
		return true;
	}
	
	/** Get song position from touched position */
	int getSongPos(float x, float y) {
		int width = getWidth();
		int height = getHeight();
		
		// Set Song position to currently clicked item:
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
		int yoffset = (height - tilesPerHeight * mTileHeight) / 2;
		
		// Click position in tiles:
		int tileColumn = (int) x / mTileWidth;
		int tileRow = (int) (y - yoffset) / mTileHeight;
		
		int newSongPos = cur + tilesPerLine * tileRow + tileColumn;
		
		// Safety checks:
		if (newSongPos < 0) newSongPos = 0;
		if (newSongPos >= mSong.mMax) newSongPos = mSong.mMax - 1;
		
		return newSongPos;
	}
	
}
