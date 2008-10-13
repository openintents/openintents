package org.openintents.sensorgesturedetector;

import android.hardware.SensorManager;
import android.util.Log;

/**
 * Object used to report sensor events.
 * 
 * @author Peli
 *
 */
public class SensorEvent {
    private static final String TAG = "SensorEvent";

	public static final int DIRECTION_UNKNOWN = 0;
	public static final int DIRECTION_UP = 1;
	public static final int DIRECTION_DOWN = 2;
	public static final int DIRECTION_LEFT = 3;
	public static final int DIRECTION_RIGHT = 4;
	public static final int DIRECTION_FORWARD = 5;
	public static final int DIRECTION_BACKWARD = 6;

    private int mSensor;
    private float[] mValues;
    private long mEventTime;
    

    //private static Object gRecyclerLock = new Object();
    private static int gRecyclerUsed = 0;
    private static SensorEvent gRecyclerTop = null;

    private SensorEvent mNext;
    private boolean mRecycled;
    
    
    private SensorEvent() {
    	mRecycled = false;
    }
    
    public static SensorEvent obtain() {
    	if (gRecyclerTop == null) {
    		return new SensorEvent();
    	}
    	SensorEvent ev = gRecyclerTop;
    	gRecyclerTop = ev.mNext;
    	gRecyclerUsed--;
    	ev.mRecycled = false;
    	return ev;
    }
    
    public static SensorEvent obtain(int sensor, float[] values, long eventTime) {
    	
    	// TODO: obtain sensor event from garbage can.
    	SensorEvent ev = SensorEvent.obtain();
    	ev.mSensor = sensor;
        ev.copyValues(values);
        ev.mEventTime = eventTime;
        
    	return ev;
    }

    public static SensorEvent obtain(SensorEvent e) {
    	
    	// TODO: obtain sensor event from garbage can.
    	SensorEvent ev = SensorEvent.obtain();
    	ev.mSensor = e.mSensor;
        ev.copyValues(e.mValues);
        ev.mEventTime = e.mEventTime;
        
    	return ev;
    }
    
    public void recycle() {
    	if (mRecycled) {
    		throw new RuntimeException("recycled twice!");
    	}
    	
    	mRecycled = true;
    	gRecyclerUsed++;
    	mNext = gRecyclerTop;
    	gRecyclerTop = this;
    }
    
    private void copyValues(float[] values) {
    	if ((mValues != null) && (mValues.length != values.length)) {
    		// dimensions don't fit, so let's drop
    		mValues = null;
    	}
    	
    	if (mValues == null) {
    		mValues = new float[values.length];
    	}
   
    	System.arraycopy(values, 0, mValues, 0, values.length);
    }
    
    /**
     * Return the kind of action being performed  
     */
    public final int getSensor() {
            return mSensor;
    }
    
    /**
     * Returns the time (in ms) when this specific event was generated.
     */
    public final long getEventTime() {
            return mEventTime;
    }
    
    /**
     * Returns the sensor values of this event. 
     */
    public final float[] getValues() {
            return mValues;
    }
    
    /**
     * Sets this event's sensor. 
     * @param action
     */
    public final void setSensor(int sensor) {
            mSensor = sensor;
    }
     
     
    /**
     * Set this event's sensor values. 
     * @param x
     * @param y
     */
    public final void setValues(float[] values) {
            mValues = values;
    }
     
    /**
     * Obtain the length of the values vector.
     * This only makes sense of values are from the accelerometer.
     * 
     * @return Square root of the sum of squares of values.
     */
    public final float getValueLength() {
    	float sum2 = 0;
    	//int max = mValues.length;
    	int max = 3;  // Only use the first 3 values.
    	for (int i = 0; i < max; i++) {
    		sum2 += mValues[i] * mValues[i];
    	}
    	float length = (float) Math.sqrt(sum2);
    	return length;
    }

    /**
     * Obtain the rough direction.
     * The return value is one of the constants DIRECTION_UP,
     * DIRECTION_DOWN, ...
     * 
     * 
     * @param idleEvent Event when the device was last idle, otherwise null.
     * @return
     */
    public final int getRoughDirection(SensorEvent idleEvent) {
    	int direction = DIRECTION_UNKNOWN;
    	
    	if (mSensor == SensorManager.SENSOR_ACCELEROMETER) {
	    	float ax = mValues[0];
	    	float ay = mValues[1];
	    	float az = mValues[2];
	    	if (idleEvent != null) {
	    		float[] v = idleEvent.getValues();
	    		ax -= v[0];
	    		ay -= v[1];
	    		az -= v[2];
	    	}
	    	
	    	float absx = Math.abs(ax);
	    	float absy = Math.abs(ay);
	    	float absz = Math.abs(az);
	    	
	    	// Pick the largest value, this is the direction.
	    	if (absx > absy && absx > absz) {
	    		// x is largest value
    			if (ax > 0) {
    				direction = DIRECTION_RIGHT;
    			} else {
    				direction = DIRECTION_LEFT;
    			}
	    	} else if (absy > absx && absy > absz) {
	    		// y is largest value
    			if (ay > 0) {
    				direction = DIRECTION_FORWARD;
    			} else {
    				direction = DIRECTION_BACKWARD;
    			}
	    	} else if (absz > absx && absz > absy) {
	    		// z is largest value
    			if (az > 0) {
    				direction = DIRECTION_UP;
    			} else {
    				direction = DIRECTION_DOWN;
    			}
	    	} else {
	    		direction = DIRECTION_UNKNOWN;
	    		Log.i(TAG, "directions. " + ax + ", " + ay + ", " + az);
	    	}
    	}
    	return direction;
    }
}