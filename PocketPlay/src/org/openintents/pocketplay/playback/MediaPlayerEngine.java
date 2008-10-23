package org.openintents.pocketplay.playback;

/*
 <!-- 
 * Copyright (C) 2007-2008 OpenIntents.org
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
 -->*/

import java.io.IOException;
import java.text.DecimalFormat;

import org.openintents.pocketplay.R;
import org.openintents.widget.Slider;
import org.openintents.widget.Slider.OnPositionChangedListener;
import org.openintents.widget.textticker.TextTickerView;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ContentUris;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import android.database.Cursor;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.app.Activity;
import android.provider.MediaStore.Audio.*;
import android.provider.MediaStore.Audio;
import android.content.Context;

public class MediaPlayerEngine implements 
	OnBufferingUpdateListener, OnCompletionListener, 
	MediaPlayer.OnPreparedListener	{

			/**
	 * One of the different media types the user can play.
	 * VOID: no selection has been done yet.
	 */
	private static final int MEDIA_TYPE_VOID = 0;
	private static final int MEDIA_TYPE_AUDIO = 1;
	private static final int MEDIA_TYPE_VIDEO = 2;
	
	/** Current media type for browsing. */
	private int mMediaType;
	
	/** 
	 * Whether a media file is being played.
	 * 
	 *  This helps to stop mHandler from being called
	 *  when there is no music playing and no slider 
	 *  moving. Only the playMedia() routine sets
	 *  mPlaying = true. 
	 */
	private boolean mPlaying;
	
	/** 
	 * Play the file as soon as the video surface has been created.
	 * 
	 * 
	 */
	private boolean mPlayIfSurfaceCreated;


	/** TAG for log messages. */
	private static final String TAG = "MediaPlayerEngine"; 

	private Context context;

	private Uri mURI;
	private String mFilename="";
	private String mCurrentArtist;
	private String mCurrentTitle;
	private String mCurrentPlaylist;

	private Cursor mCursor;

	private boolean mSurfaceCreated=false;
	
	/** Message for message handler. */
	private static final int UPDATE_POSITION = 1;
    
	private MediaPlayer mp; 

	private PlayerEngineListener listener;

    /** Specifies the relevant columns. */
    String[] mProjection = new String[] {
        android.provider.BaseColumns._ID,
        android.provider.MediaStore.MediaColumns.TITLE,
        android.provider.MediaStore.Audio.AudioColumns.ARTIST,
        android.provider.MediaStore.MediaColumns.DATA
    };

	public MediaPlayerEngine(Context context){

		this.context=context;
		//for now, only audio support
		mMediaType=MEDIA_TYPE_AUDIO;
		if (this.context==null)
		{
			Log.e(TAG,"context was null, you'll be crashing soon!!");
		}

	}


    public void playMedia() { 
	    try { 
	    	Log.i(TAG,"Starting music");
	        // If the path has not changed, just start the media player 
	        if (mp != null) { 
	        	Log.i(TAG,"Re-start music");
	            
	            mp.start(); 
	            if (! mPlaying ) {
		            mPlaying = true;
	            }

				if (listener!=null)
				{
					listener.onPlayerPlay(mURI,mCurrentArtist,mCurrentTitle);
				}

	            return; 
	        } 
	        
	        if (mFilename.equals("")) {
	        	Log.i(TAG, "No file chosen yet");
	        	
	        	return;
	        }
	        
	        // Create a new media player and set the listeners 
	        mp = new MediaPlayer(); 
	        //mp.setOnErrorListener(this); 
	        mp.setOnBufferingUpdateListener(this); 
	        mp.setOnCompletionListener(this); 
	        mp.setOnPreparedListener(this); 
	        mp.setAudioStreamType(2); 
	        if (mSurfaceCreated && mMediaType == MEDIA_TYPE_VIDEO) {
	        	//mp.setDisplay(mHolder); 
	        }
	        
	        
	        try { 
	        	mp.setDataSource(mFilename); 
	        	
	        	Log.i(TAG,"setDataSource OK");
	        } catch (IOException e) { 
	        	Log.e(TAG, e.getMessage(), e);
	        }
	        try{ 
	               mp.prepare(); 
	               Log.i(TAG,"prepare OK");
	        } catch(Exception e) { 
	          Log.e("\n\nprepare",e.toString()); 
	        } 
	        
	        mp.start(); 
	        Log.i(TAG,"start OK");
	        
	        mPlaying = true;
			if (listener!=null)
			{
				listener.onPlayerPlay(mURI,mCurrentArtist,mCurrentTitle);
			}

	
	    } catch (Exception e) { 
	        Log.e(TAG, "error: " + e.getMessage(), e); 
	    } 
    } 

    public void pauseMedia() {
    	if (mp != null) {
    		mp.pause();
    	}
    	mPlaying = false;	     
		if (listener!=null)
		{
			listener.onPlayerPause();
		}		
    }
    
    public void stopMedia() {
    	if (mp != null) {
    		//mp.reset();
    		mp.stop();
    		mp.release();
    		mp = null;
    	}
    	mPlaying = false;
		if (listener!=null)
		{
			listener.onPlayerStop();
		}
    }
    
    public void resetMedia() {
    	if (mp != null) {
    		//mp.reset();
    		mp.seekTo(0);
    	}
		if (listener!=null)
		{
			listener.onPlayerReset();
		}
    }

    /**
     * Load file from URI if it has been set by the calling activity.
     */
	public void loadFileFromUri(Uri tURI) {
		this.mURI=tURI;
		// Get the media content
        if (this.mURI != null) {
	        mCursor = context.getContentResolver().query(mURI, mProjection, null, null,null);
	        if (mCursor != null && mCursor.moveToFirst()) {
		        
				int indexDATA = mCursor.getColumnIndex(android.provider.MediaStore.MediaColumns.DATA);
				int indexTitle = mCursor.getColumnIndex(android.provider.MediaStore.MediaColumns.TITLE);
				int indexArtist = mCursor.getColumnIndex(android.provider.MediaStore.Audio.AudioColumns.ARTIST);

				mFilename = mCursor.getString(indexDATA);
				mCurrentTitle = mCursor.getString(indexTitle);
				mCurrentArtist = mCursor.getString(indexArtist);
				
				if (mSurfaceCreated || mMediaType == MEDIA_TYPE_AUDIO) {
					// We have already created the surface.
					// Or we don't need it since we play Audio.
					// Let's play the file immediately:
					playMedia();
				} else {
					// Let us wait until the surface is created
					// and start playing then
					mPlayIfSurfaceCreated = true;
				}
				return;
	        }
        }
 
        // No result:
    	mFilename = "";
	}


    public void onBufferingUpdate(MediaPlayer arg0, int percent) { 
    	Log.d(TAG, "onBufferingUpdate percent:" + percent); 
    } 


    /**
     * Is called when the song reached is final position.
     */
    public void onCompletion(MediaPlayer arg0) { 
    	Log.d(TAG, "onCompletion called"); 
    	
    	// Let us clean up
    	if (mp != null) {
	    	mp.release();
	    	mp = null;
    	}
    	mPlaying = false;
		//inform listener. he might want to load next track ;))
		if (listener!=null)
		{
			listener.onPlayerCompletion();
		}
    } 


    public void onPrepared(MediaPlayer mediaplayer) { 
	    Log.d(TAG, "onPrepared called"); 
	    mediaplayer.start(); 
    } 


	public boolean isPlaying(){return mPlaying;}

	public void setPlayerEngineListener(PlayerEngineListener listener){
		this.listener=listener;
	}




	public interface PlayerEngineListener{

		public void onPlayerPlay(Uri mURI,String mCurrentArtist,String mCurrentTitle);
		public void onPlayerPause();
		public void onPlayerStop();
		public void onPlayerReset();
		public void onPlayerCompletion();


	};

}












