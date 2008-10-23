package org.openintents.pocketplay;

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
import org.openintents.pocketplay.playback.*;
import org.openintents.pocketplay.playlists.PlaylistBrowser;
import org.openintents.pocketplay.playlists.PlaylistGeneratorService;
import org.openintents.widget.Slider;
import org.openintents.widget.Slider.OnPositionChangedListener;
import org.openintents.widget.textticker.TextTickerView;
import org.openintents.widget.textticker.AutoTextTickerView;

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
import android.widget.Toast;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.os.IBinder;


public class MediaPlayerActivity extends Activity implements 
	OnBufferingUpdateListener, OnCompletionListener, 
	MediaPlayer.OnPreparedListener, SurfaceHolder.Callback{
	
	/** TAG for log messages. */
	private static final String TAG = "PlayerActivity"; 
	
	private static final int MENU_OPEN_AUDIO = Menu.FIRST;
	private static final int MENU_OPEN_VIDEO = Menu.FIRST + 1;
	private static final int MENU_INFO = Menu.FIRST + 2;
	
	/**
	 * One of the different states this activity can run.
	 */
	private static final int STATE_MAIN = 0;
	private static final int STATE_VIEW = 1;
	private static final int STATE_VIEW_FILE = 2;
	private static final int STATE_VIEW_PLAYLIST = 3;
	
	/* Definition of the requestCode for the subactivity. */
    static final private int SUBACTIVITY_MEDIA_BROWSER = 1;
    static final private int SUBACTIVITY_PICK_PLAYLIST = 2;


	/** Current state */
	private int mState;
	
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
	
	/**
	 * One of the different views being presented.
	 */
	private static final int MEDIA_VIEW_VIDEO = 1;
	private static final int MEDIA_VIEW_INFO = 2;
	
	
    /** Specifies the relevant columns. */
    String[] mProjection = new String[] {
        android.provider.BaseColumns._ID,
        android.provider.MediaStore.MediaColumns.TITLE,
        android.provider.MediaStore.Audio.AudioColumns.ARTIST,
        android.provider.MediaStore.MediaColumns.DATA
    };

    /** Specifies the relevant columns. */
    String[] mPlaylistProjection = new String[] {
        android.provider.BaseColumns._ID,
        android.provider.MediaStore.Audio.Playlists.Members.AUDIO_ID,
        android.provider.MediaStore.Audio.Playlists.Members.PLAY_ORDER
    };
    
	String[] playlistBaseProjection= new String[]{
		android.provider.BaseColumns._ID,
		PlaylistsColumns.NAME,
		PlaylistsColumns.DATA
	};

	/** URI of current media file. */
	private Uri mURI;

	/** Uri to playlist info*/
	private Uri mPlaylistBaseURI;

	/** Uri to playlist members (data to read)*/
	private Uri mPlaylistURI;
	
	/** Location of the media file. */
	private String mFilename;
	
	/** Title of the media file. */
	private String mTitle;
    
	private String mCurrentTitle;
	private String mCurrentArtist;
	private String mCurrentPlaylist;

	private Cursor mCursor;
	private Cursor mPlaylistCursor;
	private int mPlaylistPosition;
	
	/** Message for message handler. */
	private static final int UPDATE_POSITION = 1;
    
	private MediaPlayer mp; 
	private SurfaceView mPreview; 
    private SurfaceHolder mHolder; 
    private boolean mSurfaceCreated;
	
	private Button mPlay; 
	private Button mSettings; 
	private Button mPlaylists; 

	private Button mTrackInfo;
	private Button mHelp;

	private Button mUpRight;
	private Button mDownLeft;

	//TODO: throw em out
	private Button mPause; 
	private Button mStop; 
	private Button mReset; 
	//----------------------

	private TextView mListPosition;
	private TextView mPositionText;
	private Slider mSlider;
	
    /** The primary interface we will be calling on the service. */
    IAudioPlayerService mService = null;
    private boolean mIsBound;

	/**
	 * Widgets corresponding to info layout.
	 * 
	 * Info layout shows song name, artist, album, etc.
	 */
	private LinearLayout mInfoView;
	private AutoTextTickerView mNameField;
	private TextView mPlaylistPos;
	
	private AutoTextTickerView mDownLeftSong;

	private AutoTextTickerView mUpRightSong;

	private SharedPreferences mPreferences;

	private static final String PREFS_NAME="pocketplay_preferences";
	private static final String PREFS_LAST_PLAYLIST	="prefs_last_playlist";
	private static final String PREFS_LOAD_LAST_LIST="prefs_load_last_list";
	private String DEFAULT_PLAYLIST="all"; //name is language dependent

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        /* TODO The following does not seem to work. 
         * 
         * 
        // Before we can inflate the view, we have to set
        // the correct R-values:
        Slider.R.styleable.Slider = R.styleable.Slider;
        Slider.R.styleable.Slider_max = R.styleable.Slider_max;
        Slider.R.styleable.Slider_min = R.styleable.Slider_min;
        Slider.R.styleable.Slider_pos = R.styleable.Slider_pos;
        Slider.R.styleable.Slider_background = R.styleable.Slider_background;
        Slider.R.styleable.Slider_knob = R.styleable.Slider_knob;
        */
        
        setContentView(R.layout.mediaplayer);
        
        mp = null;
        mSurfaceCreated = false;
        mMediaType = MEDIA_TYPE_VOID;
        mPlaying = false;
        mPlayIfSurfaceCreated = false;

        
		Intent lintent=new Intent();
		lintent.setClass(MediaPlayerActivity.this,PlaylistGeneratorService.class);
		startService(lintent);

        // Handle the calling intent
        final Intent intent = getIntent();
        String action = intent.getAction();
		checkEULA();
		Log.d(TAG,"onCREATE: starting Service");
        startService(new Intent(
                    "org.openintents.pocketplay.playback.REMOTE_SERVICE"));
		Log.d(TAG,"onCREATE: checking connection");
        checkConnection();


		if (action==null)
		{
			action=Intent.ACTION_MAIN;
		}
        if (action.equals(Intent.ACTION_MAIN)) {
            mState = STATE_MAIN;
            mURI = null;
            
            // TODO: Here we could load a fild
            // that the user watched last time.
        } else if (action.equals(Intent.ACTION_VIEW)) {
            mState = STATE_VIEW;
			Log.d(TAG,"uri-ssp>"+intent.getData().getSchemeSpecificPart());
			Log.d(TAG,"pl-ext-ssp>"+Playlists.EXTERNAL_CONTENT_URI.getSchemeSpecificPart());
			if (intent.getData().getSchemeSpecificPart().indexOf(
				Playlists.EXTERNAL_CONTENT_URI.getSchemeSpecificPart())>-1)
			{
				mPlaylistBaseURI=intent.getData();
            	mPlaylistURI = Uri.withAppendedPath(mPlaylistBaseURI,"members");
				mState=STATE_VIEW_PLAYLIST;
			}else{
				mURI = intent.getData();
				mState=STATE_VIEW_FILE;
			}
          //  setMediaTypeFromUri(mURI);
        } else {
            // Unknown action.
            Log.e(TAG, "Player: Unknown action, exiting");
            finish();
            return;
        }
                
        ////////////////////////////////////////////
        // Set up widgets

        
        // Set a size for the video screen 
        //mHolder = mPreview.getHolder(); 
        //mHolder.addCallback(this); 
        //mHolder.setFixedSize(200, 150); 
        
        mPlay = (Button) findViewById(R.id.playpause); 
        mPlay.setOnClickListener(new View.OnClickListener() { 
            public void onClick(View view) { 
				if (checkConnection())
				{
					try
					{
						Log.i(TAG,"deciding play/pause");
						if (mService.isPlaying())
						{
							pauseMedia();
							switchToPauseMode();							
						}else{
							playMedia(); 
							switchToPlayMode();
						}            
					}
					catch (RemoteException re)
					{
						Log.e(TAG,"got remote exception, did checkCOnnection fail ??");
					}
					
				}
			}

        }); 
        
		mPlaylists	=	(Button) findViewById(R.id.playlist);

		mSettings	=	(Button) findViewById(R.id.settings);
		
		mTrackInfo	=	(Button) findViewById(R.id.trackinfo);

		mHelp		=	(Button) findViewById(R.id.help);
		mUpRight	=	(Button) findViewById(R.id.upright);
		mDownLeft	=	(Button) findViewById(R.id.downleft);

	
		mPlaylists.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				Intent intent=new Intent();
				intent.setAction(Intent.ACTION_PICK);
				intent.setData(android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI);
				//force our browser, the default one is useless
				//intent.setClassName("org.openintents.pocketplay.playlists","org.openintents.pocketplay.playlists.PlaylistBrowser");
				intent.setClass(MediaPlayerActivity.this,PlaylistBrowser.class);
				startActivityForResult(intent,SUBACTIVITY_PICK_PLAYLIST);
			}
		});

		mSettings.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				Intent intent = new Intent();
				intent.setAction("org.openintents.preferences");
				intent.addCategory(Intent.CATEGORY_DEFAULT);
				// intent.putExtras(b);
				startActivity(intent);
			}
		});


		mUpRight.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				nextTrack();
			}
		});

	
		mDownLeft.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				lastTrack();
			}
		});

		mHelp.setOnClickListener(new View.OnClickListener(){
			public void onClick(View view){
				mUpRightSong.triggerAutoScrolling();
			}
		});


        mPositionText = (TextView) findViewById(R.id.position); 
        mPositionText.setText("00:00");
        //mPositionText.setTextColor(0xff000088);
        
        mNameField = (AutoTextTickerView) findViewById(R.id.name_field); 
        mNameField.setText("");
        mNameField.setTextSize(20);
        mNameField.setLines(1);

		mListPosition=(TextView) findViewById(R.id.listpos);
		mListPosition.setText("");
        /*
        mSlider = (Slider) findViewById(R.id.slider);
        mSlider.setBackground(getResources().getDrawable(R.drawable.shiny_slider_background001c));
        mSlider.setKnob(getResources().getDrawable(R.drawable.shiny_slider_knob001a));
        mSlider.setPosition(0);
        mSlider.setOnPositionChangedListener(
        		new OnPositionChangedListener() {

					/**
					 * Changed slider to new position.
					 * @see org.openintents.widget.Slider.OnPositionChangedListener#onPositionChangeCompleted()
					 *//*
					public void onPositionChangeCompleted() {
						int newPos = mSlider.pos;
						if (mp != null) {
							mp.seekTo(newPos);
						}
					}

					/* (non-Javadoc)
					 * @see org.openintents.widget.Slider.OnPositionChangedListener#onPositionChanged(org.openintents.widget.Slider, int, int)
					 *//*
					public void onPositionChanged(Slider slider,
							int oldPosition, int newPosition) {
						// Update text field:
						if (mp != null) {
							int timeMax = mp.getDuration();
							mPositionText.setText("" 
			            			+ formatTime(newPosition) + " / " 
			            			+ formatTime(timeMax));		
						}
					}
        			
        		});
			*/
        

        // 

		mDownLeftSong=(AutoTextTickerView)findViewById(R.id.downleft_song);
		mDownLeftSong.setTextSize(20);
		mDownLeftSong.setTextColor(android.graphics.Color.WHITE);
		mDownLeftSong.setLines(1);
		
		mDownLeftSong.setText("");
		
		mUpRightSong=(AutoTextTickerView)findViewById(R.id.upright_song);
		mUpRightSong.setTextSize(20);
		mUpRightSong.setTextColor(android.graphics.Color.WHITE);
		mUpRightSong.setLines(1);
		
		mUpRightSong.setText("");

		//for now, only audio support
		mMediaType=MEDIA_TYPE_AUDIO;


    }
    

	private void init(){
		//called after connection is establised
		if (mState==STATE_VIEW_PLAYLIST)
		{
			if (checkConnection())
			{
				try
				{
					Log.d(TAG,"uri>"+mPlaylistBaseURI);
					Log.d(TAG,"uriS>"+mPlaylistBaseURI.toString());
					mService.loadPlaylist(mPlaylistBaseURI.toString());	
				}
				catch (RemoteException re)
				{
					Log.e(TAG,"got remote exception, did checkCOnnection fail ??");
				}
				
			}
		}else if (mState==STATE_VIEW_FILE)
		{

			if (checkConnection())
			{
				try
				{
					mService.loadFile(mURI.toString());	
				}
				catch (RemoteException re)
				{
					Log.e(TAG,"got remote exception, did checkCOnnection fail ??");
				}
				
			}

		}else if (mState==STATE_MAIN)
		{
			Log.d(TAG,"onCreate:final State is MAIN");
			try
			{
				if (mService!=null &&mService.isPlaying())
				{
					Log.d(TAG,"service is playing something");
					switchToPlayMode();
				}else if (mService!=null)
				{
					Log.d(TAG,"service is idle, looking up last list & starting");
					
					SharedPreferences prefs=getSharedPreferences(PREFS_NAME,0);

					if (prefs.getBoolean(PREFS_LOAD_LAST_LIST,true))
					{							
						String plname=prefs.getString(PREFS_LAST_PLAYLIST,DEFAULT_PLAYLIST);
						Cursor nameCursor=getContentResolver().query(
							Playlists.EXTERNAL_CONTENT_URI,
							playlistBaseProjection,
							PlaylistsColumns.NAME+" like '"+plname+"'",
							null,
							null
						);
						if (nameCursor!=null && nameCursor.getCount()>0)
						{

							nameCursor.moveToFirst();
							long playlistID=nameCursor.getLong(
								nameCursor.getColumnIndexOrThrow(android.provider.BaseColumns._ID)
								);


							mPlaylistBaseURI=Playlists.EXTERNAL_CONTENT_URI.buildUpon().appendPath(String
											.valueOf(playlistID)).build();

							mService.loadPlaylist(mPlaylistBaseURI.toString());
							mService.pause();
						}
					}
				
				}else if(mService==null) {

					Log.e(TAG,"service is NULL! CONNECTION DIED AFTER STARTING!");
				}
			}
			catch (RemoteException re)
			{
				Log.e(TAG,"REMOTE EXCPETOIN WHILE TALKING TO SERVICE");
			}

		}
		updateDisplay();
	}


	/**
	 * Test whether EULA has been accepted. Otherwise display EULA.
	 */
	private void checkEULA() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		boolean accepted = sp.getBoolean(EulaActivity.PREFERENCES_EULA_ACCEPTED, false);
		
		if (accepted) {
			Log.i(TAG, "Eula has been accepted.");
		} else {
			Log.i(TAG, "Eula has not been accepted yet.");
			Intent i = new Intent(this, EulaActivity.class);
			startActivity(i);
			finish();
		}
	}






	/** Sets the media type from an intent. */
	private void setMediaTypeFromUri(Uri data) {
		// Chop off last part (that contains the row number in the database)
		String uriString = data.toString();
		int split_at = uriString.lastIndexOf('/');
		uriString = uriString.substring(0, split_at);
		data = Uri.parse(uriString);
		
		if (data.compareTo(
				android.provider.MediaStore.Audio.Media
				.INTERNAL_CONTENT_URI) == 0) {
			mMediaType = MEDIA_TYPE_AUDIO;
		} else if (data.compareTo(
		        android.provider.MediaStore.Audio.Media
		        .EXTERNAL_CONTENT_URI) == 0) {
			// We pick video data:
			mMediaType = MEDIA_TYPE_AUDIO;
		} else if (data.compareTo(
				android.provider.MediaStore.Video.Media
				.INTERNAL_CONTENT_URI) == 0) {
			mMediaType = MEDIA_TYPE_VIDEO;
		} else if (data.compareTo(
		        android.provider.MediaStore.Video.Media
		        .EXTERNAL_CONTENT_URI) == 0) {
			// We pick video data:
			mMediaType = MEDIA_TYPE_VIDEO;
		}
	}

	private void switchToPauseMode(){
		mPlay.setBackgroundResource(R.drawable.grey_button_play);					
		mSettings.setVisibility(View.VISIBLE);
		mPlaylists.setVisibility(View.VISIBLE);
		mTrackInfo.setVisibility(View.VISIBLE);
		mHelp.setVisibility(View.VISIBLE);

	}

	private void switchToPlayMode(){
		mPlay.setBackgroundResource(R.drawable.grey_button_pause);					
		mSettings.setVisibility(View.INVISIBLE);
		mPlaylists.setVisibility(View.INVISIBLE);
		mTrackInfo.setVisibility(View.INVISIBLE);
		mHelp.setVisibility(View.INVISIBLE);

	}


	public void onPause(){
		super.onPause();
		Log.d(TAG,"onPause");

	try
	{
		unbindService(mConnection);
	}
	catch (Exception e)
	{
		Log.e(TAG,e.getMessage()+" IGNORED");
	}

		

		
	}

	public void onDestroy(){
		super.onDestroy();
		SharedPreferences prefs=getSharedPreferences(PREFS_NAME,0);
		SharedPreferences.Editor editor=prefs.edit();
		editor.putString(PREFS_LAST_PLAYLIST,mCurrentPlaylist);
		editor.commit();
	}

	public void onSaveInstanceState(Bundle outstate){

		if (mPlaylistBaseURI!=null)
		{
			outstate.putString("playlist_uri",mPlaylistBaseURI.toString());
		}
		
		outstate.putString("playlist",mCurrentPlaylist);
		outstate.putString("title",mCurrentTitle);
		outstate.putString("artist",mCurrentArtist);
		outstate.putInt("playlist_pos",mPlaylistPosition);
		super.onSaveInstanceState(outstate);



	}

	public void onRestoreInstanceState(Bundle instate){
		super.onRestoreInstanceState(instate);

		mCurrentPlaylist=instate.getString("playlist");
		mCurrentTitle=instate.getString("title");
		mCurrentArtist=instate.getString("artist");
		mPlaylistPosition=instate.getInt("playlist_pos");
		String sUri=instate.getString("playlist_uri");
		if (sUri!=null)
		{
			mPlaylistBaseURI=Uri.parse(sUri);
		}else{
			mPlaylistBaseURI=null;
		}
		//updateDisplay();


	}
	private void nextTrack(){
		if (checkConnection())
		{
			try
			{
	    		Log.i(TAG,"switching to next");
				mService.nextTrack();	
			}
			catch (RemoteException re)
			{
				Log.e(TAG,"got remote exception, did checkCOnnection fail ??");
			}
			
		}
	}


	private void lastTrack(){

		if (checkConnection())
		{
			try
			{
	    		Log.i(TAG,"switching to previous");
				mService.previousTrack();	
			}
			catch (RemoteException re)
			{
				Log.e(TAG,"got remote exception, did checkCOnnection fail ??");
			}
			
		}
	}

    private void playMedia() { 

		if (checkConnection())
		{
			try
			{
	    		Log.i(TAG,"Starting music");
				mService.play();	
			}
			catch (RemoteException re)
			{
				Log.e(TAG,"got remote exception, did checkCOnnection fail ??");
			}
			
		}
    } 

    public void pauseMedia() {

		if (checkConnection())
		{
			try
			{
		    	Log.i(TAG,"pausing music");
				mService.pause();	
			}
			catch (RemoteException re)
			{
				Log.e(TAG,"got remote exception, did checkCOnnection fail ??");
			}
			
		}

    }
    
    public void stopMedia() {

    }
    
    public void resetMedia() {
    	if (mp != null) {
    		//mp.reset();
    		mp.seekTo(0);
    	}
    }
    

	private void updateDisplay(){
			Log.d(TAG,"updateDisplay: entering");
		
		if (mPlaylistBaseURI!=null)
		{
			Log.d(TAG,"updateDisplay: Playlist loaded");
			//using a playlist
			
			setTitle(mCurrentPlaylist + " : " + mCurrentTitle);
			
			// Set information in info view:
			mNameField.setText(mCurrentArtist+" - "+ mCurrentTitle);
			//humans love 1 based indeces ;)
			mListPosition.setText(String.valueOf(mPlaylistPosition+1));

			String[] next5=null;
			String[] last5=null;
			if (checkConnection())
			{
				try
				{
					Log.i(TAG,"asking for list");
					next5=mService.getNext5();
					last5=mService.getPrevious5();
				}
				catch (RemoteException re)
				{
					Log.e(TAG,"got remote exception, did checkCOnnection fail ??");
				}catch (NullPointerException ne){
					Log.e(TAG,"updateDisplay: connection was down, re-trying");

					forceCheckConnection();
				
				
					try
					{
						Thread.sleep(250);
						Log.i(TAG,"asking for list");
						next5=mService.getNext5();
						last5=mService.getPrevious5();
					}
					catch (InterruptedException ie)
					{
						Log.e(TAG,"sleep disturbed");
					}
					catch (RemoteException re)
					{
						Log.e(TAG,"got remote exception, did checkCOnnection fail ??");
					}
				}
				
			}
			if (next5!=null && next5.length>0)
			{
				Log.d(TAG,"nexxt5>");
				dumpArrayToLog(next5);
				mUpRightSong.setText(next5[0]);
			}else{
				mUpRightSong.setText("");
			}

			if (last5!=null && last5.length>0)
			{
				Log.d(TAG,"last5>");
				dumpArrayToLog(last5);
				mDownLeftSong.setText(last5[0]);
			}else{
				mDownLeftSong.setText("");
			}


			mUpRight.setVisibility(View.VISIBLE);
			mUpRightSong.setPreventScrolling(false);
			//mUpRightSong.triggerAutoScrolling();

			mDownLeft.setVisibility(View.VISIBLE);
			mDownLeftSong.setPreventScrolling(false);
			//mDownLeftSong.triggerAutoScrolling();


			//dirty trick for now.
		}else if (mPlaylistBaseURI==null)
		{
			//single track playing
			Log.d(TAG,"updateDisplay: single Track mode");

			setTitle(mCurrentTitle);
			
			// Set information in info view:
			mNameField.setText(mCurrentArtist+" - "+ mCurrentTitle);
			mUpRightSong.setText("");
			mDownLeftSong.setText("");
			mUpRight.setVisibility(View.INVISIBLE);
			mDownLeft.setVisibility(View.INVISIBLE);

		}

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
		//this will move to next track if playlist is loaded
		nextTrack();
    } 


    public void onPrepared(MediaPlayer mediaplayer) { 
	    Log.d(TAG, "onPrepared called"); 
	    mediaplayer.start(); 
    } 
    

    public void surfaceChanged(SurfaceHolder surfaceholder, 
    		int format, int width, int height) { 
        Log.d(TAG, "surfaceChanged called"); 
    } 
    
    public void surfaceCreated(SurfaceHolder holder) { 
      Log.d(TAG, "surfaceCreated called");
      // Start playing as soon as surface is ready.
		mSurfaceCreated = true;
		if (mPlayIfSurfaceCreated) {
			// A playMedia() command had to wait for surface,
			// so now we can start playing.
			mPlayIfSurfaceCreated = false;
			playMedia();			
		}
    } 
    
    public void surfaceDestroyed(SurfaceHolder surfaceholder) { 
        Log.d(TAG, "surfaceDestroyed called"); 
        mSurfaceCreated = false;
    } 

    /** Handler for timing messages. */
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_POSITION) {
            	if (mp == null) {
					/*
            		mPositionText.setText("00:00 / 00:00");
            		mSlider.min = 0;
	            	mSlider.max = 100;
	            	mSlider.setPosition(0);
					*/
	            	mPlaying = false;
            	} else {
					/*
	            	int time = mp.getCurrentPosition();
	            	int timeMax = mp.getDuration();
	            	if (mSlider.mTouchState == Slider.STATE_RELEASED) {
		            	mPositionText.setText("" 
		            			+ formatTime(time) + " / " 
		            			+ formatTime(timeMax));
	            	}
	            	
	            	mSlider.min = 0;
	            	mSlider.max = timeMax;
	            	mSlider.setPosition(time);
	            	*/
	            	if (mPlaying) {
	            		sendMessageDelayed(obtainMessage(UPDATE_POSITION), 200);
	            	}
            	}
            }
        }
    };
    
    static DecimalFormat mTimeDecimalFormat = new DecimalFormat("00");
	
    public String formatTime(int ms) {
    	int s = ms / 1000; // seconds
    	int m = s / 60;
    	s = s - 60 * m;
    	int h = m / 60;
    	m = m - 60 * h;
    	String m_s = mTimeDecimalFormat.format(m) + ":" 
    		+ mTimeDecimalFormat.format(s);
    	if (h > 0) {
    		// show also hour
    		return "" + h + ":" + m_s;
    	} else {
    		// Show only minute:second
    		return m_s;
    	}
    }

	///////////////////////////////////////////////////////
	//
	// Menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Standard menu
		menu.add(0, MENU_OPEN_AUDIO, 0, R.string.open_audio)
			.setIcon(R.drawable.music001a)
			.setShortcut('0', 'o');

		menu.add(0, MENU_INFO, 0, R.string.about).setIcon(
				android.R.drawable.ic_menu_info_details);
/*
		menu.add(0, MENU_OPEN_VIDEO, 0, R.string.open_video)
			.setIcon(R.drawable.video002a)
		.setShortcut('0', 'o');
*/		
	
		// Generate any additional actions that can be performed on the
        // overall list.  This allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);

        
		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_OPEN_AUDIO:
			// Start subactivity for media browser:
			
			return startSubActivityFromCurrentUri(
					android.provider.MediaStore
					.Audio.Media.INTERNAL_CONTENT_URI);
			
		case MENU_OPEN_VIDEO:
			// Start subactivity for media browser:
			
			return startSubActivityFromCurrentUri(
					android.provider.MediaStore
					.Video.Media.INTERNAL_CONTENT_URI);
		case MENU_INFO:
			showInfo();
			break;

		}
		return super.onOptionsItemSelected(item);
	}


	private void showInfo() {
		startActivity(new Intent(this, About.class));
	}



	private boolean startSubActivityFromCurrentUri(Uri defaultUri) {
		if ((mState == STATE_VIEW) && (mURI != null)) {
			// Pick the same type that we had
			
			// Split off last piece containing the data row number
			String uriString = mURI.toString();
			int split_at = uriString.lastIndexOf('/');
			uriString = uriString.substring(0, split_at);
			
			Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse(uriString));
			startActivityForResult(intent, SUBACTIVITY_MEDIA_BROWSER);
		} else {
			// Let us pick a generic type given through the parameter.
			// TODO: If user used EXTERNAL_CONTENT_URI last time,
			// one may provide external content; same with Video.
			Intent intent = new Intent(Intent.ACTION_PICK, 
					defaultUri);
			startActivityForResult(intent, SUBACTIVITY_MEDIA_BROWSER);
		}
		
		return true;
	}


	/**
     * This method is called when the sending activity has finished, with the
     * result it supplied.
     * 
     * @param requestCode The original request code as given to
     *                    startActivity().
     * @param resultCode From sending activity as per setResult().
     * @param data From sending activity as per setResult().
     * @param extras From sending activity as per setResult().
     * 
     * @see android.app.Activity#onActivityResult(int, int, java.lang.String, android.os.Bundle)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent resultIntent) {

        if (requestCode == SUBACTIVITY_MEDIA_BROWSER) {

        	if (resultCode == RESULT_CANCELED) {
                // Don't do anything.

        		// Except for:
            	// Set view back to info view
        		// (Otherwise the video background
        		//  may not be available anymore
        		//  and some random remaining background
        		//  displayed).
            	getWindow().setFormat(PixelFormat.OPAQUE); 
            	//mPreview.setVisibility(View.GONE);
 //           	mInfoView.setVisibility(View.VISIBLE);
            } else if (resultCode==RESULT_OK)
			{
                // Start playing the file:
            	invalidatePlaylist();
            	mURI = resultIntent.getData();
                if (checkConnection())
                {
					try
					{
						Log.d(TAG,"uri>"+mURI);	
						mService.stop();
						mService.invalidatePlaylist();
						mService.loadFile(mURI.toString());	
					}
					catch (RemoteException re)
					{
						Log.e(TAG,"got remote exception, did checkCOnnection fail ??");
					}
					
                } 
                
            }

        }else if (requestCode == SUBACTIVITY_PICK_PLAYLIST)
        {

			
        	if (resultCode == RESULT_CANCELED) {
                // Don't do anything.

        		// Except for:
            	// Set view back to info view
        		// (Otherwise the video background
        		//  may not be available anymore
        		//  and some random remaining background
        		//  displayed).
            	getWindow().setFormat(PixelFormat.OPAQUE); 
            //	mPreview.setVisibility(View.GONE);
 //           	mInfoView.setVisibility(View.VISIBLE);
            } else {
                // Start playing the file:
            	
            	// First stop the old one
            	//stopMedia();
            	
            	mPlaylistBaseURI = resultIntent.getData();
            	mPlaylistURI = Uri.withAppendedPath(resultIntent.getData(),"members");
            	
            	// Set video settings
            	//setMediaTypeFromUri(mURI);

                if (checkConnection())
                {
					try
					{
						Log.d(TAG,"uri>"+mPlaylistBaseURI);
						Log.d(TAG,"uriS>"+mPlaylistBaseURI.toString());
						if (mService.isPlaying())
						{
							mService.stop();
						}
						mService.loadPlaylist(mPlaylistBaseURI.toString());	
					}
					catch (RemoteException re)
					{
						Log.e(TAG,"got remote exception, did checkCOnnection fail ??");
					}
					
                }
            	//loadPlaylistFromUri();
                
            }
        }
	}

	private void invalidatePlaylist(){

       	mPlaylistBaseURI = null;
      	mPlaylistURI = null;
		mPlaylistCursor=null;     
	}


	
	/* *******************callback Methods from IAudioPlayerCallback (see aidl file)***********/



    /**
     * This implementation is used to receive callbacks from the remote
     * service.
     */
    private IAudioPlayerCallback mServiceCallback = new IAudioPlayerCallback.Stub() {
        /**
         * This is called by the remote service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */

		public void onAudioPlay(){
			mServiceHandler.sendMessage(mServiceHandler.obtainMessage(TRACK_PLAY,0,0));		
		}

		public void onAudioPause(){		
			mServiceHandler.sendMessage(mServiceHandler.obtainMessage(TRACK_PAUSE,0,0));				
		}

		public void onAudioStop(){
			Log.d(TAG,":mServiceCallback::onAudioStop: entering------------------");
		}

		public void onTrackChange(String trackUri,String artist, String title,String playlist, int playlistPosition){
			Log.d(TAG,":mServiceCallback::onTrackChange: entering------------------");

			String[] args=new String[]{
				trackUri,
				artist,
				title,
				playlist,
				Integer.toString(playlistPosition)
			};
			mServiceHandler.sendMessage(mServiceHandler.obtainMessage(TRACK_CHANGE, args));
			Log.d(TAG,":mServiceCallback::onTrackChange: leaving_____________");
		}

		public void onPositionChange(){}


    };
    
    private static final int TRACK_CHANGE		= 1;
    private static final int TRACK_PLAY			= 2;
    private static final int TRACK_PAUSE		= 3;
    private static final int TRACK_STOP			= 4;
    private static final int TRACK_POS_CHANGE	= 5;
    
    private Handler mServiceHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case TRACK_CHANGE:
					String[] args=(String[])msg.obj;
					if (mPlaylistCursor!=null)
					{
						int playlistPosition=-1;


						mPlaylistCursor.moveToPosition(playlistPosition);
					}
					mCurrentArtist=args[1];
					mCurrentTitle=args[2];
					mCurrentPlaylist=args[3];
					mPlaylistPosition=Integer.parseInt(args[4]);

					updateDisplay();

                    
                    break;
				case TRACK_PLAY:
					switchToPlayMode();
					break;
				case TRACK_PAUSE:
					switchToPauseMode();
					break;
                default:
                    super.handleMessage(msg);
            }
        }
        
    };



	
	private boolean checkConnection(){
		if (!mIsBound||mService==null)
		{
			Log.d(TAG,"checkConnection: binding Service");
			bindService(new Intent(IAudioPlayerService.class.getName()),
								mConnection, Context.BIND_AUTO_CREATE);
			mIsBound=true;
		}
		return mIsBound;
	}
           
	private boolean forceCheckConnection(){
		mIsBound=false;
		Log.d(TAG,"mconncetions is >>"+mConnection+"<< mService>>"+mService);
		return checkConnection();
	}			

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
			Log.d(TAG,"---------------mConnection:onServiceConnected: entering------------------------");
            mService = IAudioPlayerService.Stub.asInterface(service);
			Log.d(TAG,"mConnection:onServiceConnected: connection established, registering callback");
            // We want to monitor the service for as long as we are
            // connected to it.
            try {
                mService.registerCallback(mServiceCallback);
            } catch (RemoteException e) {
				Log.e(TAG,"mConnection:onServiceConnected: registering callback FAILED. Service may be down");
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }
            
            // As part of the sample, tell the user what happened.
            Toast.makeText(MediaPlayerActivity.this, "started audioengine",
                    Toast.LENGTH_SHORT).show();
			init();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;

            // As part of the sample, tell the user what happened.
            Toast.makeText(MediaPlayerActivity.this, "Disconnected.",
                    Toast.LENGTH_SHORT).show();
        }
    };


	private void dumpArrayToLog(String[] array){
		StringBuffer b=new StringBuffer("---------------------dumping array:-------------\n");
		for (int i=0;i<array.length ;i++ )
		{
			b.append("["+i+"] >>"+array[i]+"<<\n");
		}
		b.append("------------------------------------\n");
		Log.d(TAG,b.toString());
	}

}
