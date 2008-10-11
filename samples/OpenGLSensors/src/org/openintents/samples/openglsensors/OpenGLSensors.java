/* 
 * Copyright (C) 2008 OpenIntents.org
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
 */

/*
 * This application is based on Google's ApiDemos
 * com.google.android.samples.graphics.GLView1.java
 * which is licensed under the same license (Apache License, Version 2.0)
 */

package org.openintents.samples.openglsensors;

/*
 * YOU HAVE TO MANUALLY INCLUDE THE OPENINTENTS-LIB-n.n.n.JAR FILE:
 * 
 * In the Eclipse Package Explorer, right-click on the imported 
 * project OpenGLSensors, select "Properties", then "Java Build Path" 
 * and tab "Libraries". There "Add External JARs..." and select 
 * lib/openintents-lib-n.n.n.jar. 
 */
import org.openintents.sensorsimulator.db.SensorSimulator;
import org.openintents.sensorsimulator.hardware.SensorManagerSimulator;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;


/**
 * Displays a pyramid that always points up, or a 
 * compass needle that always points towards North.
 * 
 * Directions are obtained from Android Sensors.
 * If no real sensors are available, one can connect
 * to the SensorSimulator.
 * 
 * Accelerometer, compass, and orientation sensors are supported.
 * 
 * @author Peli
 *
 */

public class OpenGLSensors extends Activity implements SensorListener {
	/**
	 * TAG for logging.
	 */
	private static final String TAG = "OpenGLSensors";

	private static final int MENU_SETTINGS = Menu.FIRST;
	private static final int MENU_CONNECT_SIMULATOR = Menu.FIRST + 1;
	
	private static final int MENU_SENSOR = Menu.FIRST + 100;
	private static final int MENU_SENSOR_NOT_AVAILABLE = Menu.FIRST + 101;
	private static final int MENU_SENSOR_ACCELEROMETER = Menu.FIRST + 102;
	private static final int MENU_SENSOR_COMPASS = Menu.FIRST + 103;
	private static final int MENU_SENSOR_ACCELEROMETER_COMPASS = Menu.FIRST + 104;
	private static final int MENU_SENSOR_ORIENTATION = Menu.FIRST + 105;
	private static final int MENU_SENSOR_ORIENTATION_COMPASS = Menu.FIRST + 106;
	
	private static final int MENU_SHAPE = Menu.FIRST + 200;
	private static final int MENU_SHAPE_CUBE = Menu.FIRST + 201;
	private static final int MENU_SHAPE_PYRAMID = Menu.FIRST + 202;
	private static final int MENU_SHAPE_MAGNET = Menu.FIRST + 203;
	
	/** 
	 * Constant for message handling.
	 */
	private static final int UPDATE_ANIMATION = 1;
	
    private SensorManagerSimulator mSensorManager;
    
	private boolean mConnected;
	
	private boolean mAccelerometerSupported;
	private boolean mCompassSupported;
	private boolean mOrientationSupported;
	
	 // sensors to be used
	public boolean mUseAccelerometer;
	public boolean mUseCompass;
	public boolean mUseOrientation;
	

	private int mUpdateInterval;
	
	/**
	 * Whether we currently automatically update the animation.
	 */
	boolean mUpdatingAnimation;
	
	//private GLSurfaceView mGLSurfaceView;
	private GLSurfaceViewNoThread mGLSurfaceView;
	
	/**
	 * Called when activity starts.
	 * 
	 * We do not automatically reconnect to the SensorSimulator,
	 * as it may not be available in the mean-time anymore or 
	 * the IP address may have changed.
	 * 
	 * (We do not know how long the activity had been dormant).
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);   
        
        // Default timer interval
   		mUpdateInterval = 100;
   		mUpdatingAnimation = false;

        //mGLSurfaceView = new GLSurfaceView( getApplication() , this);
        mGLSurfaceView = new GLSurfaceViewNoThread( getApplication() , this);
        
        mGLSurfaceView.stopUseSensors();
        
        setContentView(mGLSurfaceView);
        
        //mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mSensorManager = new SensorManagerSimulator(this);
		
        // TODO: Sensors.isSimulatorConnected() should be implemented 
        // and used here.
        mConnected = false;
        
        
        findSupportedSensors();
        
        useBestAvailableSensors();

        enableAllSensors();
        
        mGLSurfaceView.startUseSensors();
    }
    
    /**
	 * Called when activity comes to foreground.
	 */
    @Override
	protected void onResume()
    {
        super.onResume();
       	Log.i(TAG, "onResume()");
               
        mSensorManager.registerListener(this, 
                SensorManager.SENSOR_ACCELEROMETER | 
                SensorManager.SENSOR_MAGNETIC_FIELD | 
                SensorManager.SENSOR_ORIENTATION,
                SensorManager.SENSOR_DELAY_FASTEST);
        
        // Actually, the following should only be called
        // after holdAnimation(), but a quickfix allows
        // us to call this method once at start
        // without consequences.
        //mGLSurfaceView.resumeAnimation();
       	
       	// We do this through handlers:
       	mGLSurfaceView.init();
        kickAnimation();
    	
    }

    
    
	/** 
	 * Called when another activity is started.
	 * 
	 * @see android.app.Activity#onFreeze(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
    	Log.i(TAG, "onFreeze()");
		
		// Hold the animation to save CPU consumtion
		// when in background.
		//mGLSurfaceView.holdAnimation();
    	
    	// stop the animation:
    	mUpdatingAnimation = false;
    	
    	mGLSurfaceView.stopAnimation();
	}

	@Override
	protected void onStop()
    {
        mSensorManager.unregisterListener(this);
        super.onStop();
        
    }
    
    ////////////////////////////////////////////////////////
    // The menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Standard menu
		menu.add(0, MENU_SETTINGS, 0, R.string.openglsensors_settings)
			.setIcon(R.drawable.mobile_shake_settings001a)
			.setShortcut('0', 's');
		menu.add(1, MENU_CONNECT_SIMULATOR, 0, R.string.connect_simulator)
			.setIcon(R.drawable.mobile_shake001a)
			.setShortcut('1', 'c');
		
		
		SubMenu menuSensor;
		menuSensor = menu.addSubMenu(2, MENU_SENSOR, 0, R.string.sensor_type)
			.setIcon(R.drawable.mobile_shake001a);
		menuSensor.add(0, MENU_SENSOR_NOT_AVAILABLE, 0, R.string.not_available).setCheckable(true);
		menuSensor.add(0, MENU_SENSOR_ACCELEROMETER, 0, R.string.accelerometer).setCheckable(true);
		menuSensor.add(0, MENU_SENSOR_COMPASS, 0, R.string.compass).setCheckable(true);
		menuSensor.add(0, MENU_SENSOR_ACCELEROMETER_COMPASS, 0,
				R.string.accelerometer_compass).setCheckable(true);
		menuSensor.add(0, MENU_SENSOR_ORIENTATION, 0, R.string.orientation).setCheckable(true);
		//menuSensor.add(0, MENU_SENSOR_ORIENTATION_COMPASS, R.string.orientation_compass);
		
		// Generate any additional actions that can be performed on the
        // overall list.  This allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(
            Menu.CATEGORY_ALTERNATIVE, 0, 0, new ComponentName(this, OpenGLSensors.class),
            null, intent, 0, null);
        
        // Set checkable items:
        menu.setGroupCheckable(1, true, false);

		menu.setGroupCheckable(2, true, false);

		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
				
        //menu.setItemChecked(MENU_CONNECT_SIMULATOR, mConnected);
        if (mConnected) {
        	menu.findItem(MENU_CONNECT_SIMULATOR).setTitle("Disconnect");
        }else {
        	menu.findItem(MENU_CONNECT_SIMULATOR).setTitle("Connect");	
        }
        
        menu.findItem(MENU_SENSOR_NOT_AVAILABLE).setVisible(!(mAccelerometerSupported
    			|| mCompassSupported
    			|| mOrientationSupported));
        menu.findItem(MENU_SENSOR_ACCELEROMETER).setVisible(mAccelerometerSupported);
		menu.findItem(MENU_SENSOR_COMPASS).setVisible(mCompassSupported);
		menu.findItem(MENU_SENSOR_ACCELEROMETER_COMPASS)
				.setVisible(mAccelerometerSupported && mCompassSupported);
		menu.findItem(MENU_SENSOR_ORIENTATION).setVisible(mOrientationSupported);
        
		menu.findItem(MENU_SENSOR_ACCELEROMETER).setChecked(mUseAccelerometer && !mUseCompass);
		menu.findItem(MENU_SENSOR_COMPASS).setChecked(mUseCompass && !mUseAccelerometer);
		menu.findItem(MENU_SENSOR_ACCELEROMETER_COMPASS).setChecked(mUseAccelerometer && mUseCompass);
		menu.findItem(MENU_SENSOR_ORIENTATION).setChecked(mUseOrientation);
        
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS:
			Intent intent = new Intent(Intent.ACTION_VIEW, SensorSimulator.Settings.CONTENT_URI);
			startActivity(intent);
			return true;
			
		case MENU_CONNECT_SIMULATOR:
			// first disable the current sensors:
			disableAllSensors();
			
			if (!mConnected) {
				// now connect to simulator
				mSensorManager.connectSimulator();
			} else {
				// or disconnect to simulator
				mSensorManager.disconnectSimulator();				
			}
			
			// check again which sensors are now supported:
	        findSupportedSensors();
	        
	        // check whether the sensors we just used
	        // are still supported 
	        // (if not, choose again the best sensors
	        //  from those that are available now)
	        updateUseBestAvailableSensors();
			
	        // now enable the sensors again:
			enableAllSensors();
			
			mConnected = ! mConnected;
			kickAnimation();
			return true;
			
		case MENU_SENSOR_ACCELEROMETER:
			disableAllSensors();
			useSensorsReset();
			mUseAccelerometer = true;
			enableAllSensors();
			kickAnimation();
			return true;

		case MENU_SENSOR_COMPASS:
			disableAllSensors();
			useSensorsReset();
			mUseCompass = true;
			enableAllSensors();
			kickAnimation();
			return true;

		case MENU_SENSOR_ACCELEROMETER_COMPASS:
			disableAllSensors();
			useSensorsReset();
			mUseAccelerometer = true;
			mUseCompass = true;
			enableAllSensors();
			kickAnimation();
			return true;

		case MENU_SENSOR_ORIENTATION:
			disableAllSensors();
			useSensorsReset();
			mUseOrientation = true;
			enableAllSensors();
			kickAnimation();
			return true;
		}
		return super.onOptionsItemSelected(item);
		
	}
	
	/* 
	 * Get sensor capabilities
	 */
	public void findSupportedSensors() {
		
		int sensors = mSensorManager.getSensors();
		
		mOrientationSupported = ((sensors & SensorManager.SENSOR_ORIENTATION) != 0);
		mAccelerometerSupported = ((sensors & SensorManager.SENSOR_ACCELEROMETER) != 0);
		mCompassSupported = ((sensors & SensorManager.SENSOR_MAGNETIC_FIELD) != 0);
		
	}

	/**
	 * Disable all sensors that we want to use.
	 */
	public void disableAllSensors() {
		// First disable access to sensors 
		// by GLSurfaceView
		mGLSurfaceView.stopUseSensors();
        
        mSensorManager.unregisterListener(this);
	}
	
	/**
	 * Enable all sensors that we want to use.
	 */
	public void enableAllSensors() {

        mSensorManager.registerListener(this, 
                SensorManager.SENSOR_ACCELEROMETER | 
                SensorManager.SENSOR_MAGNETIC_FIELD | 
                SensorManager.SENSOR_ORIENTATION,
                SensorManager.SENSOR_DELAY_FASTEST);
		
		// Now enable access to sensors
		// by GLSurfaceView
		mGLSurfaceView.startUseSensors();
	}
	
	/**
	 * Selects the best supported sensors currently available.
	 * This is orientation if supported. Otherwise a combination
	 * of accelerometer and compass is used (if supported).
	 */
    public void useBestAvailableSensors() {
	    // use the best sensor available:
	    useSensorsReset();
	    
	    if (mOrientationSupported) {
	    	// the best is orientation
	    	mUseOrientation = true;
	    } else {
	    	// otherwise use any of accelerometer or compass
	    	// or both together.
	    	if (mAccelerometerSupported) mUseAccelerometer = true;
	       	if (mCompassSupported) mUseCompass = true;
	    }
	    
    }
    
    /** 
     * See if the currently selected sensor(s) is (are) still available.
     * If not, useBestAvailableSensors() is called.
     */
    public void updateUseBestAvailableSensors() {
    	if ((mUseAccelerometer && !mAccelerometerSupported) 
    			|| (mUseCompass && !mCompassSupported)
    			|| (mUseOrientation && !mOrientationSupported)) {
    		useBestAvailableSensors();
    	}
    	
    	// another possibility is that previously no sensor
    	// was available, and now they are available.
    	if (!mUseAccelerometer && !mUseCompass && !mUseOrientation) {
    		// we had not used anything, so let's check whether
    		// anything is possible now.
    		useBestAvailableSensors();
    	}
    }
    
    /**
     * Resets which sensors we want to use.
     */
    public void useSensorsReset() {
    	mUseAccelerometer = false;
    	mUseCompass = false;
    	mUseOrientation = false;
    }
    
    
    /**
     * Start the animation if it has not started yet:
     */
	void kickAnimation() {
		if (!mUpdatingAnimation) {
        	mUpdatingAnimation = true;
        	// Autoupdate
        	mHandler.sendMessageDelayed(mHandler.obtainMessage(UPDATE_ANIMATION), mUpdateInterval);
        }
	}
	
	// Handle the process of automatically updating enabled sensors:
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_ANIMATION) {
            	// Let us go through all sensors,
            	// and only read out the data if it is time for
            	// that particular sensor.
            	
            	//long current = SystemClock.uptimeMillis();
                				
            	boolean change = mGLSurfaceView.doAnimation();
            	
            	// Nothing changes: no need to waste CPU:
            	if (! change) mUpdatingAnimation = false;
            	
            	
                if (mUpdatingAnimation) {
                	// Autoupdate
                	sendMessageDelayed(obtainMessage(UPDATE_ANIMATION), mUpdateInterval);
                }
            }
        }
    };

	public void onSensorChanged(int sensor, float[] values) {
        //Log.d(TAG, "onSensorChanged: " + sensor + ", x: " + values[0] + ", y: " + values[1] + ", z: " + values[2]);
		

		// T-mobile G1 patch begins:
		if (sensor == SensorManager.SENSOR_ORIENTATION) {
			/*
			if (values[1] < -90 || values[1] > 90) {
				values[1] = -180 - values[1];
			}
			*/
			/*
			if (values[1] > 90 || values[1] < -90) {
				values[2] = - values[2];
				values[0] += 180 - 2 * values[1] + 2 * values[2];
				//values[0] += 2 * values[2];
				
				//values[0] = - values[0] + 360;
				//values[0] += 180;
				//if (values[0] > 360) {
				//	values[0] -= 360;
				//}
			}
			*/
		}
		// T-mobile G1 patch ends.

		
		//Log.d(TAG, "onSensorChanged: " + sensor + ", x: " + values[0] + ", y: " + values[1] + ", z: " + values[2]);
		
		boolean graphicsupdate = false;
		
		switch(sensor) {
		case SensorManager.SENSOR_ORIENTATION:
			// Assign mYaw, mPitch, and mRoll.
			// Negative angles are used, because we have to
			// "undo" the rotations that brought the mobile phone
			// into its current position.
			if (mUseOrientation) {
				mGLSurfaceView.mYaw = -values[0];
				mGLSurfaceView.mPitch = -values[1];
				mGLSurfaceView.mRoll = -values[2];
				graphicsupdate = true;
			}
			break;

		case SensorManager.SENSOR_ACCELEROMETER:
			// We can only let the pyramid point up,
        	// but we can not obtain information about the mYaw.
        	
        	// (strictly speaking, we can only adjust two of the three
        	//  variables (mYaw, mPitch, and mRoll). Since the
        	//  standard orientation is to point down (-z), we
        	//  choose to calculate mPitch and mRoll as the 
        	//  deviation from that standard position.)
			
			// we can only adjust mPitch and mRoll:
			if (mUseAccelerometer) {
				double r = Math.sqrt(values[0]*values[0] + values[2]*values[2]);
				mGLSurfaceView.mYaw = 0;
				mGLSurfaceView.mPitch = (float) + Math.toDegrees(Math.atan2(-values[1], r));
				mGLSurfaceView.mRoll = (float) - Math.toDegrees(Math.atan2(values[0], -values[2]));	
				graphicsupdate = true;
			}
			break;

		case SensorManager.SENSOR_MAGNETIC_FIELD:
			// We can only adjust the compass to point
        	// along the magnetic field, but we can not
        	// say where "up" is.
        	// Since the expected standard orientation is
        	// to point north (that is in +y direction),
        	// we use the information to adjust 
        	// mCompassYaw and mCompassPitch,
        	// but we don't know mCompassRoll.
			
			// we can only adjust mYaw and mPitch:
			if (mUseCompass) {
				double r = Math.sqrt(values[1]*values[1] + values[2]*values[2]);
				mGLSurfaceView.mCompassYaw = (float) - Math.toDegrees(Math.atan2(-values[0], r));	
				mGLSurfaceView.mCompassPitch = (float) - Math.toDegrees(Math.atan2(-values[2], values[1]));
				graphicsupdate = true;
			}
			break;
		}
		
		if (graphicsupdate) {
			boolean change = mGLSurfaceView.doAnimation();
		}
        
	}

	public void onAccuracyChanged(int sensor, int accuracy) {
		
	}


}




