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

import org.openintents.OpenIntents;
import org.openintents.hardware.Sensors;
import org.openintents.provider.Hardware;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.SubMenu;
import android.view.Menu.Item;


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

public class OpenGLSensors extends Activity {
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
	
	
	private boolean mConnected;
	
	private boolean mAccelerometerSupported;
	private boolean mCompassSupported;
	private boolean mOrientationSupported;
	
	 // sensors to be used
	public boolean mUseAccelerometer;
	public boolean mUseCompass;
	public boolean mUseOrientation;
	
	private GLSurfaceView mGLSurfaceView;
	
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
        
        OpenIntents.requiresOpenIntents(this);
        
        mGLSurfaceView = new GLSurfaceView( getApplication() , this);
        
        mGLSurfaceView.stopUseSensors();
        
        setContentView(mGLSurfaceView);
        
        // !! Very important !!
        // Before calling any of the Simulator data,
        // the Content resolver has to be set !!
        Hardware.mContentResolver = getContentResolver();
		
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
               
        // Actually, the following should only be called
        // after holdAnimation(), but a quickfix allows
        // us to call this method once at start
        // without consequences.
        mGLSurfaceView.resumeAnimation();
    	
    }
    
	/** 
	 * Called when another activity is started.
	 * 
	 * @see android.app.Activity#onFreeze(android.os.Bundle)
	 */
	@Override
	protected void onFreeze(Bundle outState) {
		super.onFreeze(outState);
    	Log.i(TAG, "onFreeze()");
		
		// Hold the animation to save CPU consumtion
		// when in background.
		mGLSurfaceView.holdAnimation();
	}

	@Override
	protected void onStop()
    {
        super.onStop();
        
    }
    
    ////////////////////////////////////////////////////////
    // The menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Standard menu
		menu.add(0, MENU_SETTINGS, R.string.settings, R.drawable.mobile_shake_settings001a)
			.setShortcut('0', 's');
		menu.add(0, MENU_CONNECT_SIMULATOR, R.string.connect_simulator, R.drawable.mobile_shake001a)
			.setShortcut('1', 'c');
		
		
		SubMenu menuSensor;
		menuSensor = menu.addSubMenu(0, MENU_SENSOR, R.string.sensor_type, R.drawable.mobile_shake001a);
		menuSensor.add(0, MENU_SENSOR_NOT_AVAILABLE, R.string.not_available);
		menuSensor.add(0, MENU_SENSOR_ACCELEROMETER, R.string.accelerometer);
		menuSensor.add(0, MENU_SENSOR_COMPASS, R.string.compass);
		menuSensor.add(0, MENU_SENSOR_ACCELEROMETER_COMPASS, 
				R.string.accelerometer_compass);
		menuSensor.add(0, MENU_SENSOR_ORIENTATION, R.string.orientation);
		//menuSensor.add(0, MENU_SENSOR_ORIENTATION_COMPASS, R.string.orientation_compass);
		
		// Generate any additional actions that can be performed on the
        // overall list.  This allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.ALTERNATIVE_CATEGORY);
        menu.addIntentOptions(
            Menu.ALTERNATIVE, 0, new ComponentName(this, OpenGLSensors.class),
            null, intent, 0, null);
        
        // Set checkable items:
        menu.setItemCheckable(MENU_CONNECT_SIMULATOR, true);

		menu.setItemCheckable(MENU_SENSOR_ACCELEROMETER, true);
		menu.setItemCheckable(MENU_SENSOR_COMPASS, true);
		menu.setItemCheckable(MENU_SENSOR_ACCELEROMETER_COMPASS,  true);
		menu.setItemCheckable(MENU_SENSOR_ORIENTATION, true);
		menu.setItemCheckable(MENU_SENSOR_ORIENTATION_COMPASS, true);

		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
				
        menu.setItemChecked(MENU_CONNECT_SIMULATOR, mConnected);

        menu.setItemShown(MENU_SENSOR_NOT_AVAILABLE, 
        		!(mAccelerometerSupported
        			|| mCompassSupported
        			|| mOrientationSupported));
		menu.setItemShown(MENU_SENSOR_ACCELEROMETER, mAccelerometerSupported);
		menu.setItemShown(MENU_SENSOR_COMPASS, mCompassSupported);
		menu.setItemShown(MENU_SENSOR_ACCELEROMETER_COMPASS, 
				mAccelerometerSupported && mCompassSupported);
		menu.setItemShown(MENU_SENSOR_ORIENTATION, mOrientationSupported);
		//menu.setItemShown(MENU_SENSOR_ORIENTATION_COMPASS, 
		//		mOrientationSupported && mCompassSupported);
        
		menu.setItemChecked(MENU_SENSOR_ACCELEROMETER, 
				mUseAccelerometer && !mUseCompass);
		menu.setItemChecked(MENU_SENSOR_COMPASS, 
				mUseCompass && !mUseAccelerometer);
		menu.setItemChecked(MENU_SENSOR_ACCELEROMETER_COMPASS,  
				mUseAccelerometer && mUseCompass);
		menu.setItemChecked(MENU_SENSOR_ORIENTATION, 
				mUseOrientation);
		//menu.setItemChecked(MENU_SENSOR_ORIENTATION_COMPASS,  
		//		mUseOrientation && mUseCompass);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(Item item) {
		switch (item.getId()) {
		case MENU_SETTINGS:
			Intent intent = new Intent(Intent.VIEW_ACTION, Hardware.Preferences.CONTENT_URI);
			startActivity(intent);
			return true;
			
		case MENU_CONNECT_SIMULATOR:
			// first disable the current sensors:
			disableAllSensors();
			
			if (!mConnected) {
				// now connect to simulator
				Sensors.connectSimulator();
			} else {
				// or disconnect to simulator
				Sensors.disconnectSimulator();				
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
			return true;
			
		case MENU_SENSOR_ACCELEROMETER:
			disableAllSensors();
			useSensorsReset();
			mUseAccelerometer = true;
			enableAllSensors();
			return true;

		case MENU_SENSOR_COMPASS:
			disableAllSensors();
			useSensorsReset();
			mUseCompass = true;
			enableAllSensors();
			return true;

		case MENU_SENSOR_ACCELEROMETER_COMPASS:
			disableAllSensors();
			useSensorsReset();
			mUseAccelerometer = true;
			mUseCompass = true;
			enableAllSensors();
			return true;

		case MENU_SENSOR_ORIENTATION:
			disableAllSensors();
			useSensorsReset();
			mUseOrientation = true;
			enableAllSensors();
			return true;
		}
		return super.onOptionsItemSelected(item);
		
	}
	
	/* 
	 * Get sensor capabilities
	 */
	public void findSupportedSensors() {
		mAccelerometerSupported
			= isSupportedSensor(Sensors.SENSOR_ACCELEROMETER);
		mCompassSupported
			= isSupportedSensor(Sensors.SENSOR_COMPASS);
		mOrientationSupported
			= isSupportedSensor(Sensors.SENSOR_ORIENTATION);
	}
	
	// TODO: Use SensorPlus function
	/**
	 *  Check whether a specific sensor is supported.
	 */
	public boolean isSupportedSensor(String sensor) {
		String[] sensors = Sensors.getSupportedSensors();
		for (String s : sensors) {
			if (s.contentEquals(sensor)) return true;
		};
		return false;
	}

	/**
	 * Disable all sensors that we want to use.
	 */
	public void disableAllSensors() {
		// First disable access to sensors 
		// by GLSurfaceView
		mGLSurfaceView.stopUseSensors();
        
		if (mUseAccelerometer)
			Sensors.disableSensor(Sensors.SENSOR_ACCELEROMETER);
		if (mUseCompass)
			Sensors.disableSensor(Sensors.SENSOR_COMPASS);
		if (mUseOrientation)
			Sensors.disableSensor(Sensors.SENSOR_ORIENTATION);
	}
	
	/**
	 * Enable all sensors that we want to use.
	 */
	public void enableAllSensors() {
		if (mUseAccelerometer)
			Sensors.enableSensor(Sensors.SENSOR_ACCELEROMETER);
		if (mUseCompass)
			Sensors.enableSensor(Sensors.SENSOR_COMPASS);
		if (mUseOrientation)
			Sensors.enableSensor(Sensors.SENSOR_ORIENTATION);	
		
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
}




