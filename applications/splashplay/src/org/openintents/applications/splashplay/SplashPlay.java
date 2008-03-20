package org.openintents.applications.splashplay;

import java.io.IOException;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SplashPlay extends Activity implements 
	OnBufferingUpdateListener, OnCompletionListener, 
	MediaPlayer.OnPreparedListener {
    
	/** TAG for log messages. */
	private static final String TAG = "SplashPlay"; 
	
	/** Message for message handler. */
	private static final int UPDATE_POSITION = 1;
    
	private MediaPlayer mp; 
	
	private LinearLayout mLayout;
	private Button mPlay; 
	private Button mPause; 
	private Button mReset; 
	private TextView mPositionText;
	
	private FretboardView mFretboard;
	private TextView mChordText;
	
	private Song mSong;
	
	/** Time of next event */
	private int mNextTime;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        mPlay = (Button) findViewById(R.id.play); 
        mPlay.setOnClickListener(new View.OnClickListener() { 
            public void onClick(View view) { 
                playMusic(); 
            } 
        }); 
        
        mPause = (Button) findViewById(R.id.pause); 
        mPause.setOnClickListener(new View.OnClickListener() { 
            public void onClick(View view) { 
                pauseMusic(); 
            } 
        }); 
        
        mReset = (Button) findViewById(R.id.reset); 
        mReset.setOnClickListener(new View.OnClickListener() { 
            public void onClick(View view) { 
                resetMusic(); 
            } 
        }); 
        

        mPositionText = (TextView) findViewById(R.id.position); 
        mPositionText.setText("0");
        
        mLayout = (LinearLayout) findViewById(R.id.layout);
        
        mFretboard = new FretboardView(this);
        
        //int cc = mLayout.getChildCount();
        Log.i(TAG, "Fretboard call: ");
		mLayout.addView(mFretboard, 
				new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 200));
		Log.i(TAG, "Fretboard done: ");
		mChordText = new TextView(this);
		mLayout.addView(mChordText, 
				new LinearLayout.LayoutParams(200, 200));
		mChordText.setText("Chord");
		
		mFretboard.invalidate();
		mSong = new Song();
		mSong.setTime(0);
		mNextTime = 0;
        
        mp = null;
    }
    

    private void playMusic() { 
	    try { 
	    	Log.i(TAG,"Starting music");
	        // If the path has not changed, just start the media player 
	        if (mp != null) { 
	        	Log.i(TAG,"Re-start music");
	            
	            mp.start(); 
	            return; 
	        } 
	          // Create a new media player and set the listeners 
	        mp = new MediaPlayer(); 
	        //mp.setOnErrorListener(this); 
	        mp.setOnBufferingUpdateListener(this); 
	        mp.setOnCompletionListener(this); 
	        mp.setOnPreparedListener(this); 
	        mp.setAudioStreamType(2); 
	
	        Log.i(TAG,"setOn... ok");
	        
	        try { 
	        	   mp.setDataSource("/sdcard/OpenIntentsBluesAudio01f.mp3"); 
	        	   //mp.setDataSource("/system/media/audio/ringtones/ringer.mp3"); 
	        	   //mp.setDataSource("/sdcard/OpenIntentsBluesAudio01f.mid"); 
	        	   //mp.setDataSource("/sdcard/OpenIntentsBluesAudio01c.mp3"); 
	        	   //mp.setDataSource("/sdcard/OpenIntentsBlues01f.MID"); 
	            
	               Log.i(TAG,"setDataSource OK");
	               } catch (IOException e) { 
	                   Log.e(TAG, e.getMessage(), e);} 
	
	
	        try{ 
	               mp.prepare(); 
	               Log.i(TAG,"prepare OK");
	        }catch(Exception e){ 
	          Log.e("\n\nprepare",e.toString()); 
	        } 
	        
	        mp.start(); 
	        Log.i(TAG,"start OK");
	        
	        mHandler.sendMessage(mHandler.obtainMessage(UPDATE_POSITION));
	
	    } catch (Exception e) { 
	        Log.e(TAG, "error: " + e.getMessage(), e); 
	    } 
    } 

    public void pauseMusic() {
    	if (mp != null) {
    		mp.pause();
    	}
    }
    
    public void resetMusic() {
    	if (mp != null) {
    		//mp.reset();
    		mp.seekTo(0);
    		mNextTime = 0;
            
    	}
    }
    
    public void onBufferingUpdate(MediaPlayer arg0, int percent) { 
    	Log.d(TAG, "onBufferingUpdate percent:" + percent); 
    } 


    public void onCompletion(MediaPlayer arg0) { 
    	Log.d(TAG, "onCompletion called"); 
    } 


    public void onPrepared(MediaPlayer mediaplayer) { 
	    Log.d(TAG, "onPrepared called"); 
	    mediaplayer.start(); 
    } 
    
    // Handle the process of searching for suitable present:
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_POSITION && mp != null) {
            	int time = mp.getCurrentPosition();
            	mPositionText.setText("" + time + " / " 
            			+ mp.getDuration());
            	
            	
            	// Now check for music updates:
            	if (time >= mNextTime) {
            		// Time to update the chord:
            		mSong.setTime(time);
            		Event e = mSong.getEvent();
            		mFretboard.setChord(e.chord);
            		mChordText.setText(e.chord.name);
            		mNextTime = mSong.getNextTime();
            		
            	}
            	sendMessageDelayed(obtainMessage(UPDATE_POSITION), 200);
            }
        }
    };

}
