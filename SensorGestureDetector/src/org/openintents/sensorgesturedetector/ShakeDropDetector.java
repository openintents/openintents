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
    
    private float mRotateThreshold;
    
    /**
     * Timeout after which a gesture ends.
     */
    private long mGestureTimeout;
    
    private long mIdleTimeout;
    
    private int mState;
    
    private static final int STATE_IDLE_PREPARE = 1;
    private static final int STATE_IDLE = 2;
    private static final int STATE_SHAKING = 3;
    private static final int STATE_DROPPING = 4;
    
    
    //////////////////////////////////////////////////////
    // Internal constants.
    
    /**
     * Internal conversion factor for faster processing.
     */
    //static final int FLOAT_TO_INT = 1024;

    //////////////////////////////////////////////////////
    // Internal variables.
    
    private int mSHAKE_THRESHOLD_SQUARE;
    private int mDROP_THRESHOLD_SQUARE;
    private int mROTATE_THRESHOLD_SQUARE;
    
    public ShakeDropDetector(OnSensorGestureListener listener) {
    	mListener = listener;
    	
    	setShakeThreshold(1.1f); // Initial value
    	setDropThreshold(0.9f); // Initial value
    	setRotateThreshold(0.1f);
    	setGestureTimeout(300);
    	setIdleTimeout(300);
    	
    	mState = STATE_IDLE_PREPARE;
    	mIdleCandidateEvent = null;
    	mIdleEvent = null;
    }
    
    public void setShakeThreshold(float threshold) {
    	mShakeThreshold = threshold;
    	
    	float ti = threshold * SensorEvent.FLOAT_TO_INT 
    				* SensorManager.GRAVITY_EARTH; // threshold internal
    	mSHAKE_THRESHOLD_SQUARE = (int) (ti * ti);
    }
    
    public float getShakeThreshold() {
    	return mShakeThreshold;
    }
    

    public void setDropThreshold(float threshold) {
    	mDropThreshold = threshold;
    	
    	float ti = threshold * SensorEvent.FLOAT_TO_INT 
    				* SensorManager.GRAVITY_EARTH; // threshold internal
    	mDROP_THRESHOLD_SQUARE = (int) (ti * ti);
    }
    
    public float getDropThreshold() {
    	return mDropThreshold;
    }
    

    public void setRotateThreshold(float threshold) {
    	mRotateThreshold = threshold;
    	
    	float ti = threshold * SensorEvent.FLOAT_TO_INT 
    				* SensorManager.GRAVITY_EARTH; // threshold internal
    	mROTATE_THRESHOLD_SQUARE = (int) (ti * ti);
    }
    
    public float getRotateThreshold() {
    	return mRotateThreshold;
    }
    
    public void setGestureTimeout(long timeout) {
    	mGestureTimeout = timeout;
    }
    
    public long getGestureTimeout() {
    	return mGestureTimeout;
    }

    public void setIdleTimeout(long timeout) {
    	mIdleTimeout = timeout;
    }
    
    public long getIdleTimeout() {
    	return mIdleTimeout;
    }
    
    /**
     * Event shortly before current gesture started.
     */
    SensorEvent mIdleEvent;
    
    /**
     * Could potentially be the idle event, if the state does not change much.
     */
    SensorEvent mIdleCandidateEvent;
    long mIdleCandidateTimeout;
    
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
    
    SensorEvent mDelta;
        
    public void onSensorChanged(int sensor, float[] values) {
    	if (sensor != SensorManager.SENSOR_ACCELEROMETER) {
    		// Can only analyze accelerometer.
    		return;
    	}
    	
    	long eventTime = SystemClock.uptimeMillis();
    	SensorEvent event = SensorEvent.obtain(sensor, values, eventTime);
    	event.toIntegerValues();
        int len2 = event.getIntegerLen2();
        
        switch (mState) {
        case STATE_IDLE_PREPARE:
        case STATE_IDLE:
        	// 2 things need to be checked in order to stay idle:
        	// 1) There must be standard gravity
        	// 2) The device must not have been rotated too much.
        	
	        // We compare the squares. In this way we avoid calculating
	        // the square root.
	        if (len2 > mSHAKE_THRESHOLD_SQUARE) {
				
				// Start shaking
	        	mLastLen2 = len2;
	        	mLastPeakEvent = null;
	        	
	        	// The idle event is the previous one.
	        	mIdleEvent = SensorEvent.obtain(mLastEvent);
	        	
	        	mState = STATE_SHAKING;
	        } else if (len2 < mDROP_THRESHOLD_SQUARE) {
				
				// Dropping
				
				mListener.onDrop(mIdleEvent, event);
	        	mState = STATE_DROPPING;
	        } else {
	        	// Idle
	        	if (mIdleCandidateEvent == null) {
	        		mIdleCandidateEvent = SensorEvent.obtain(event);
	        		mIdleCandidateTimeout = eventTime + mIdleTimeout;
	        	} else {
	        		// Check that we are still within bounds.
	        		
	        		if (mDelta == null) {
	        			mDelta = SensorEvent.obtain();
	        		}
	        		
	        		mDelta.getIntegerDifference(event, mIdleCandidateEvent);
	        		int deltalen2 = mDelta.getIntegerLen2();
	        		if (deltalen2 > mROTATE_THRESHOLD_SQUARE) {
	        			// Rotated device too much
	        			
	        			mListener.onRotate(mIdleEvent, event);
	        			
	        			// pick this as new idle candidate
	        			mIdleCandidateEvent = SensorEvent.obtain(event);
		        		mIdleCandidateTimeout = eventTime + mIdleTimeout;
		        		
		        		mState = STATE_IDLE_PREPARE;
	        			
	        		} else {
	        			if (mState == STATE_IDLE_PREPARE) {
		        			if (eventTime >= mIdleCandidateTimeout) {
		        				mIdleEvent = mIdleCandidateEvent;
		        				
		        				// Send that idle event
		        				mListener.onIdle(mIdleEvent);
		        				
		        				mState = STATE_IDLE;
		        			}
	        			} else {
	        				// Already in idle state, and we stay there.
	        			}
	        		}
	        	}
	        }
	        break;
        case STATE_SHAKING:
        	if (len2 > mLastLen2) {
        		// Still accelerating
        		//mLastEvent = getSensorEvent(sensor, values);
	        	mLastLen2 = len2;
	        	/*
	        	mLastX = ax;
	        	mLastY = ay;
	        	mLastZ = az;
	        	*/
        	} else {
        		if (mLastPeakEvent == null) {
	        		// We reached maximum acceleration.
	        		// Let us report this with the event at maximum acceleration:
	        		mLastPeakEvent = mLastEvent;
	        		mLastPeakTime = mLastEvent.getEventTime();
	        		/*
	        		mLastPeakX = mLastX;
	        		mLastPeakY = mLastY;
	        		mLastPeakZ = mLastZ;
	        		*/
	    			mListener.onShake(mIdleEvent, mLastEvent);
        		} else {
        			// There was a last peak. Let us see when acceleration
        			// starts to point in the opposite direction:
        			//int angle = mLastPeakX * ax + mLastPeakY * ay + mLastPeakZ * az;
        			int angle = mLastPeakEvent.getIntegerDotProduct(event);
        			if (angle < 0) {
        				// We switched direction.
        				mLastPeakEvent = null;
        				mLastLen2 = len2;
        			}
        		}
        		
        		if (SystemClock.uptimeMillis() - mLastPeakTime > mGestureTimeout 
        				&& len2 < mSHAKE_THRESHOLD_SQUARE) {
        			// After gesture timeout, let's get back to idle state.
		        	mState = STATE_IDLE_PREPARE;
        		}
        	}
			
        	break;
        case STATE_DROPPING:
        	
        	if (len2 > mDROP_THRESHOLD_SQUARE) {
				mListener.onCatch(mIdleEvent, event);
	        	mState = STATE_IDLE_PREPARE;
        	}
        	break;
        }
        
        if (mLastEvent != null) {
        	mLastEvent.recycle();
        }
        mLastEvent = event;
    }
    
}
