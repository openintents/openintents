package org.openintents.sensorgesturedetector;

import org.openintents.sensorgesturedetector.SensorGestureDetector.OnSensorGestureListener;

import android.hardware.SensorManager;
import android.os.SystemClock;

public class ShakeDropDetector {

	private OnSensorGestureListener mListener;
	
    /** 
     * Shake detection threshold.
     */
    private float mShakeThreshold;

    private float mDropThreshold;
    
    //////////////////////////////////////////////////////
    // Internal constants.
    
    /**
     * Internal conversion factor for faster processing.
     */
    static final int FLOAT_TO_INT = 1024;

    //////////////////////////////////////////////////////
    // Internal variables.
    
    private int mSHAKE_THRESHOLD_SQUARE;
    private int mDROP_THRESHOLD_SQUARE;
    
    public ShakeDropDetector(OnSensorGestureListener listener) {
    	mListener = listener;
    	
    	setShakeThreshold(1.2f); // Initial value
    	setDropThreshold(0.8f); // Initial value
    }
    
    public void setShakeThreshold(float threshold) {
    	mShakeThreshold = threshold;
    	
    	float ti = threshold * FLOAT_TO_INT 
    				* SensorManager.GRAVITY_EARTH; // threshold internal
    	mSHAKE_THRESHOLD_SQUARE = (int) (ti * ti);
    }
    
    public float getShakeThreshold() {
    	return mShakeThreshold;
    }
    

    public void setDropThreshold(float threshold) {
    	mDropThreshold = threshold;
    	
    	float ti = threshold * FLOAT_TO_INT 
    				* SensorManager.GRAVITY_EARTH; // threshold internal
    	mDROP_THRESHOLD_SQUARE = (int) (ti * ti);
    }
    
    public float getDropThreshold() {
    	return mDropThreshold;
    }
    
        
    public void onSensorChanged(int sensor, float[] values) {
    	if (sensor != SensorManager.SENSOR_ACCELEROMETER) {
    		// Can only analyze accelerometer.
    		return;
    	}
        
        int ax = (int)(FLOAT_TO_INT * values[0]);
        int ay = (int)(FLOAT_TO_INT * values[1]);
        int az = (int)(FLOAT_TO_INT * values[2]);
        
        int len2 = ax * ax + ay * ay + az * az;
        
        // We compare the squares. In this way we avoid calculating
        // the square root.
        if (len2 > mSHAKE_THRESHOLD_SQUARE) {
			
			// Shaking
        	long eventTime = SystemClock.uptimeMillis();
        	SensorEvent event = SensorEvent.obtain(sensor, values, eventTime);
			
			mListener.onShake(event);
        }
        
        if (len2 < mDROP_THRESHOLD_SQUARE) {
			
			// Dropping
        	long eventTime = SystemClock.uptimeMillis();
        	SensorEvent event = SensorEvent.obtain(sensor, values, eventTime);
			
			mListener.onDrop(event);
        }
    }
    
}
