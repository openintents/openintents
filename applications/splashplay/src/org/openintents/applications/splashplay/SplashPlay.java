package org.openintents.applications.splashplay;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;

import org.openintents.widget.Slider;
import org.openintents.widget.Slider.OnPositionChangedListener;

import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SplashPlay extends Activity implements 
	OnBufferingUpdateListener, OnCompletionListener, 
	MediaPlayer.OnPreparedListener {
    
	/** TAG for log messages. */
	private static final String TAG = "SplashPlay"; 
	
	private static final int PROGRESS = 1;
    
	private static final int MENU_BLUETOOTH = Menu.FIRST;
	private static final int MENU_ABOUT = Menu.FIRST + 1;
	
	
	private static final String mAppFileFolder = 
		"/data/data/org.openintents.applications.splashplay/files/";
	
	/** Message for message handler. */
	private static final int UPDATE_POSITION = 1;
    
	private MediaPlayer mp; 
	
	/** 
	 * Whether a media file is being played.
	 * 
	 *  This helps to stop mHandler from being called
	 *  when there is no music playing and no slider 
	 *  moving. Only the playMedia() routine sets
	 *  mPlaying = true. 
	 */
	private boolean mPlaying;
	
	private LinearLayout mLayout;
	private Button mPlay; 
	private Button mPause; 
	private Button mStop;
	private Button mReset; 
	private TextView mPositionText;
	private Slider mSlider;
	
	private FretboardView mFretboard;
	//private TextView mChordText;
	private ChordsView mChords;
	
	private Song mSong;
	
	/** Time of next event */
	private int mNextTime;
	
	private Dialog mBluetoothDialog;
	private ProgressDialog mProgressDialog;
    private boolean mCancelled;
    private int mProgress;
    private NotificationManager mNotificationManager;
    // Use our layout id for a unique identifier
    private static final int BLUETOOTH_NOTIFICATIONS = R.layout.bluetooth;
    private boolean mBluetoothConnected;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        // Variable initialization
        mp = null;
        mPlaying = false;
        mBluetoothConnected = false;
        
        // Hook up widgets
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
        
        mStop = (Button) findViewById(R.id.stop); 
        mStop.setOnClickListener(new View.OnClickListener() { 
            public void onClick(View view) { 
                stopMedia(); 
            } 
        }); 
        
        mReset = (Button) findViewById(R.id.reset); 
        mReset.setOnClickListener(new View.OnClickListener() { 
            public void onClick(View view) { 
                resetMusic(); 
            } 
        }); 
        

        mPositionText = (TextView) findViewById(R.id.position); 
        mPositionText.setText("00:00 / 00:00");
        mPositionText.setTextColor(0xff000088);
        
        mSlider = (Slider) findViewById(R.id.slider);
        mSlider.setBackground(getResources().getDrawable(R.drawable.shiny_slider_background001c));
        mSlider.setKnob(getResources().getDrawable(R.drawable.shiny_slider_knob001a));
        mSlider.setPosition(0);
        mSlider.setOnPositionChangedListener(
        		new OnPositionChangedListener() {

					/**
					 * Changed slider to new position.
					 * @see org.openintents.widget.Slider.OnPositionChangedListener#onPositionChangeCompleted()
					 */
					@Override
					public void onPositionChangeCompleted() {
						int newPos = mSlider.pos;
						if (mp != null) {
							mp.seekTo(newPos);

							// Force update of song position at next event.
							mNextTime = 0;
						}
					}

					/* (non-Javadoc)
					 * @see org.openintents.widget.Slider.OnPositionChangedListener#onPositionChanged(org.openintents.widget.Slider, int, int)
					 */
					@Override
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
        

        
        mLayout = (LinearLayout) findViewById(R.id.layout);
        
        mFretboard = (FretboardView) findViewById(R.id.fretboard);
        
        // Now assign the graphics elements.
        mFretboard.setDrawable(FretboardView.WOOD, R.drawable.wood001a);
        mFretboard.setDrawable(FretboardView.NUT, R.drawable.nut001a);
        mFretboard.setDrawable(FretboardView.FRET, R.drawable.fret001a);
        mFretboard.setDrawable(FretboardView.MARKER, R.drawable.marker001a);
        mFretboard.setDrawable(FretboardView.SPOT, R.drawable.spot001a);
        
        mChords = (ChordsView) findViewById(R.id.chords);
        
        
        /*mFretboard = new FretboardView(this);
        
        
        //int cc = mLayout.getChildCount();
        Log.i(TAG, "Fretboard call: ");
		mLayout.addView(mFretboard, 
				new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 200));
		Log.i(TAG, "Fretboard done: ");
		*/
        /*
		mChordText = new TextView(this);
		mLayout.addView(mChordText, 
				new LinearLayout.LayoutParams(200, 20));
		mChordText.setText("Chord");
		*/
		
		mFretboard.invalidate();
		mSong = new Song();
		mSong.setTime(0);
		mNextTime = 0;
		
		mChords.mSong = mSong;
        
        
        // We install the sample applications
        installSampleFiles();
        

    	// Reset all views to initial song position
		mNextTime = updateViews(0);
    }
    
    /**
     * Installs sample files from the assets folder into the user's data folder.
     */
    void installSampleFiles() {
    	// Load the sample file and put them into our data directory:
    	
    	// Here we check whether they already exist:
    	
    	
    	try {
    		InputStream is = getAssets().open("OpenIntents01f.mid");
    		FileOutputStream fos = openFileOutput("OpenIntents01f.mid", MODE_WORLD_READABLE);
    		int size = is.available();
    		byte[] buffer = new byte[size];
    		is.read(buffer);
    		fos.write(buffer, 0, size);
    		fos.close();
    		is.close();
    	} catch (IOException e) {
    		// Should never happen
            throw new RuntimeException(e);
    	}
    }

    private void playMusic() { 
	    try { 
	    	Log.i(TAG,"Starting music");
	        // If the path has not changed, just start the media player 
	        if (mp != null) { 
	        	Log.i(TAG,"Re-start music");
	            
	            mp.start(); 
	            if (! mPlaying ) {
		            mPlaying = true;
			        mHandler.sendMessage(mHandler.obtainMessage(UPDATE_POSITION));
	            }
	            return; 
	        } 
	          // Create a new media player and set the listeners 
	        mp = new MediaPlayer(); 
	        //mp.setOnErrorListener(this); 
	        mp.setOnBufferingUpdateListener(this); 
	        mp.setOnCompletionListener(this); 
	        mp.setOnPreparedListener(this); 
	        mp.setAudioStreamType(2); 
	        
	        try { 
	        	mp.setDataSource(mAppFileFolder + "OpenIntents01f.mid");
	        	//mp.setDataSource("/sdcard/OpenIntentsBluesAudio01f.mp3"); 
	        	//mp.setDataSource("/system/media/audio/ringtones/ringer.mp3"); 
				//mp.setDataSource("/sdcard/OpenIntentsBluesAudio01f.mid"); 
				//mp.setDataSource("/sdcard/OpenIntentsBluesAudio01c.mp3"); 
				//mp.setDataSource("/sdcard/OpenIntentsBlues01f.MID"); 
				
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
	        mHandler.sendMessage(mHandler.obtainMessage(UPDATE_POSITION));
	
	    } catch (Exception e) { 
	        Log.e(TAG, "error: " + e.getMessage(), e); 
	    } 
    } 

    public void pauseMusic() {
    	if (mp != null) {
    		mp.pause();
    	}
    	mPlaying = false;
    }
    
    public void stopMedia() {
    	if (mp != null) {
    		//mp.reset();
    		mp.stop();
    		mp.release();
    		mp = null;
    	}
    	mPlaying = false;

    	// Reset all views to initial song position
		mNextTime = updateViews(0);
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
    	
		// Reset all views to initial song position
		mNextTime = updateViews(0);		
    } 


    public void onPrepared(MediaPlayer mediaplayer) { 
	    Log.d(TAG, "onPrepared called"); 
	    mediaplayer.start(); 
    } 
    
    // Handle the process of searching for suitable present:
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_POSITION) {
            	if (mp == null) {
            		mPositionText.setText("00:00 / 00:00");
            		mSlider.min = 0;
	            	mSlider.max = 100;
	            	mSlider.setPosition(0);
	            	mPlaying = false;
            	} else {
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
	            	
	            	// Now check for music updates:
	            	if (time >= mNextTime) {
	            		// Time to update the chord:
	            		mNextTime = updateViews(time);
	            	}
	            	
	            	if (mPlaying) {
	            		sendMessageDelayed(obtainMessage(UPDATE_POSITION), 200);
	            	}
            	}
            }
        }
    };


    /**
     * Update all views to the current time.
     * @param time Song position in ms.
     * @return Next time when update is necessary (e.g. chord change).
     */
    public int updateViews(int time) {
    	mSong.setTime(time);
		Event e = mSong.getEvent();
		mFretboard.setChord(e.chord);
		//mChordText.setText(e.chord.name);
		mChords.invalidate();
		return mSong.getNextTime();
    }
    
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

    // The menu

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		super.onCreateOptionsMenu(menu);
		
		// Standard menu
		menu.add(0, MENU_BLUETOOTH, R.string.bluetooth, R.drawable.bluetooth_icon1)
			.setShortcut('0', 'b');
		/*menu.add(1, MENU_ABOUT, R.string.about)
		.setShortcut('1', 'a');*/
		
		return true;
	}


	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.Menu.Item)
	 */
	@Override
	public boolean onOptionsItemSelected(Item item) {
		switch (item.getId()) {
		case MENU_BLUETOOTH:
			showBluetoothBox();
			return true;
	/*	case MENU_ABOUT:
			showAboutBox();
			return true;
	*/	}
		return super.onOptionsItemSelected(item);
	}
    
    /**
     * Show the "Bluetooth connection" box.
     */
    private void showBluetoothBox() {
    	mBluetoothDialog = new Dialog(SplashPlay.this);
		
    	mBluetoothDialog.setContentView(R.layout.bluetooth);
		
    	mBluetoothDialog.setTitle(getString(R.string.connect_bluetooth));
    	
    	if (mBluetoothConnected) {
    		TextView tv = (TextView) mBluetoothDialog.findViewById(R.id.disconnect);
    		tv.setText("Disconnect");
    	}
    	
    	// Open a web page upon clicking
		((ImageButton) mBluetoothDialog.findViewById(R.id.splashpod))
			.setOnClickListener(new OnClickListener() {
				public void onClick(final View v) {
					Intent i = new Intent(Intent.VIEW_ACTION, 
						Uri.parse("http://www.splashplay.co.uk"));
					startActivity(i);
				}
			});
		
		// Connect button
		LinearLayout bOk = (LinearLayout) mBluetoothDialog.findViewById(R.id.connect);
		bOk.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				if (! mBluetoothConnected) {
					mCancelled = false;
	                mProgress = 0;
	                OnCancelListener cancelListener = new OnCancelListener() {
	                    public void onCancel(DialogInterface dialog) {
	                        mCancelled = true;
	                        //TODO: remove before submitting
	//                        Log.v("ProgressBarTest", "Canceled the progress bar.");
	                    }
	                };
	                
	                mProgressDialog = ProgressDialog.show(SplashPlay.this,
	                        null, "Connecting to SplashPod...", false,
	                        true, cancelListener);
	                
	                mBluetoothHandler.sendMessage(mBluetoothHandler.obtainMessage(PROGRESS));
				} else {
					// Disconnect
					mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			    	Toast.makeText(SplashPlay.this, "Disconnected.",
			    			Toast.LENGTH_SHORT).show();
                	mNotificationManager.cancel(BLUETOOTH_NOTIFICATIONS);
                	mBluetoothConnected = false;
				}
				mBluetoothDialog.dismiss();
			}
		});
		
		mBluetoothDialog.show();
	
    }
    
    // Handle the process of searching for suitable present:
    private Handler mBluetoothHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == PROGRESS && !mCancelled) {
                mProgress += 200;
               
                if (mProgress > 10000) {
                	Toast.makeText(SplashPlay.this, "Connection OK.",
                            Toast.LENGTH_SHORT).show();
                	
                	// we are done
                    mProgress = 0;
                    mProgressDialog.cancel();
                    setNotification();
                    return;
                }
                mProgressDialog.setProgress(mProgress);
                sendMessageDelayed(obtainMessage(PROGRESS), 50);
            }
        }
    };


    private void setNotification() {
    	mBluetoothConnected = true;
    	
    	// Get the notification manager serivce.
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    	
        // This is who should be launched if the user selects our notification.
        Intent contentIntent = new Intent(this, SplashPlay.class);

        // This is who should be launched if the user selects the app icon in the notification.
        Intent appIntent = new Intent(this, SplashPlay.class);

        mNotificationManager.notify(
                   BLUETOOTH_NOTIFICATIONS, // we use a layout id because it is a unique
                                          // number.  we use it later to cancel the
                                          // notification
                   new Notification(
                       this,                        // our context
                       R.drawable.bluetooth_icon1_small_16,                      // the icon for the status bar
                       null,                  // the text to display in the ticker
                       System.currentTimeMillis(),  // the timestamp for the notification
                       getText(R.string.connect_bluetooth),
                                                    // the title for the notification
                       "Connection to SplashPod",          // the details to display in the notification
                       contentIntent,               // the contentIntent (see above)
                       R.drawable.splashplay_application001a,  // the app icon
                       getText(R.string.app_name), // the name of the app
                       appIntent));                 // the appIntent (see above)
    }

}
