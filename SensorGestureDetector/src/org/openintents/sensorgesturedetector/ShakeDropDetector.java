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
    
    /**
     * Timeout after which a gesture ends.
     */
    private long mGestureTimeout;
    
    private int mState;
    
    private static final int STATE_IDLE = 1;
    private static final int STATE_SHAKING = 2;
    private static final int STATE_DROPPING = 3;
    
    
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
    	
    	setShakeThreshold(1.1f); // Initial value
    	setDropThreshold(0.9f); // Initial value
    	setGestureTimeout(300);
    	
    	mState = STATE_IDLE;
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
    
    public void setGestureTimeout(long timeout) {
    	mGestureTimeout = timeout;
    }
    
    public long getGestureTimeout() {
    	return mGestureTimeout;
    }
    
    /**
     * Event shortly before current gesture started.
     */
    SensorEvent mIdleEvent;
    
    /**
     * LastEvent is recycled frequently.
     * If you need to keep it, copy it through obtain().
     */
    SensorEvent mLastEvent;
    int mLastLen2;
    int mLastX;
    int mLastY;
    int mLastZ;

    SensorEvent mLastPeakEvent;
    long mLastPeakTime;
    int mLastPeakX;
    int mLastPeakY;
    int mLastPeakZ;
    
    SensorEvent mDropStart;
        
    public void onSensorChanged(int sensor, float[] values) {
    	if (sensor != SensorManager.SENSOR_ACCELEROMETER) {
    		// Can only analyze accelerometer.
    		return;
    	}
    	
    	long eventTime = SystemClock.uptimeMillis();
    	SensorEvent event = SensorEvent.obtain(sensor, values, eventTime);
        
        int ax = (int)(FLOAT_TO_INT * values[0]);
        int ay = (int)(FLOAT_TO_INT * values[1]);
        int az = (int)(FLOAT_TO_INT * values[2]);
        
        int len2 = ax * ax + ay * ay + az * az;
        
        switch (mState) {
        case STATE_IDLE:
	        // We compare the squares. In this way we avoid calculating
	        // the square root.
	        if (len2 > mSHAKE_THRESHOLD_SQUARE) {
				
				// Start shaking
	        	mLastLen2 = len2;
	        	mLastPeakEvent = null;
	        	
	        	// The idle event is the previous one.
	        	mIdleEvent = SensorEvent.obtain(mLastEvent);
	        	
	        	mState = STATE_SHAKING;
	        }
	        
	        if (len2 < mDROP_THRESHOLD_SQUARE) {
				
				// Dropping
				
				mListener.onDrop(mIdleEvent, event);
	        	mState = STATE_DROPPING;
	        }
	        break;
        case STATE_SHAKING:
        	if (len2 > mLastLen2) {
        		// Still accelerating
        		//mLastEvent = getSensorEvent(sensor, values);
	        	mLastLen2 = len2;
	        	mLastX = ax;
	        	mLastY = ay;
	        	mLastZ = az;
        	} else {
        		if (mLastPeakEvent == null) {
	        		// We reached maximum acceleration.
	        		// Let us report this with the event at maximum acceleration:
	        		mLastPeakEvent = mLastEvent;
	        		mLastPeakTime = mLastEvent.getEventTime();
	        		mLastPeakX = mLastX;
	        		mLastPeakY = mLastY;
	        		mLastPeakZ = mLastZ;
	    			mListener.onShake(mIdleEvent, mLastEvent);
        		} else {
        			// There was a last peak. Let us see when acceleration
        			// starts to point in the opposite direction:
        			int angle = mLastPeakX * ax + mLastPeakY * ay + mLastPeakZ * az;
        			if (angle < 0) {
        				// We switched direction.
        				mLastPeakEvent = null;
        				mLastLen2 = len2;
        			}
        		}
        		
        		if (SystemClock.uptimeMillis() - mLastPeakTime > mGestureTimeout 
        				&& len2 < mSHAKE_THRESHOLD_SQUARE) {
        			// After gesture timeout, let's get back to idle state.
		        	mState = STATE_IDLE;
        		}
        	}
			
        	break;
        case STATE_DROPPING:
        	
        	if (len2 > mDROP_THRESHOLD_SQUARE) {
				mListener.onCatch(mIdleEvent, event);
	        	mState = STATE_IDLE;
        	}
        	break;
        }
        
        if (mLastEvent != null) {
        	mLastEvent.recycle();
        }
        mLastEvent = event;
    }
    
}
