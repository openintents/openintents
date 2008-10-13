package org.openintents.sensorgesturedetector;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.MotionEvent;

/**
 * Class to detect gestures through sensors.
 * 
 * @author Peli
 *
 */
public class SensorGestureDetector {

    private final Handler mHandler;
    private final OnSensorGestureListener mListener;
    
    private ShakeDropDetector mShakeDetector;

    /**
     * The listener that is used to notify when gestures occur. 
     * If you want to listen for all the different gestures 
     * then implement this interface. 
     * If you only want to listen for a subset it might be 
     * easier to extend 
     * SensorGestureDetector.SimpleOnSensorGestureListener.
     *
     */
    public static interface OnSensorGestureListener
    {

    	/**
    	 * Notified when the device is in idle position.
    	 * 
    	 * @param event Sensor Event with current idle position
    	 * @return true if the event is consumed, else false
    	 */
        public abstract boolean onIdle(SensorEvent event);

    	/**
    	 * Notified when the device is rotated, with the event that triggered the shake.
    	 * 
    	 * A rotation is slow enough so that the value of acceleration is always given 
    	 * by standard gravity.
    	 * 
    	 * @param idleEvent Sensor Event of the idle position before the shaking started.
    	 * @param event Sensor Event when rotation started.
    	 * @return true if the event is consumed, else false
    	 */
        public abstract boolean onRotate(SensorEvent idleEvent, SensorEvent event);

    	/**
    	 * Notified when the device is shaken, with the event that triggered the shake.
    	 * 
    	 * @param idleEvent Sensor Event of the idle position before the shaking started.
    	 * @param event Sensor Event when maximum acceleration was reached.
    	 * @return true if the event is consumed, else false
    	 */
        public abstract boolean onShake(SensorEvent idleEvent, SensorEvent event);
        

    	/**
    	 * Notified when the device is dropped, with the event that triggered the shake.
    	 * This event is followed by onCatch().
    	 * 
    	 * @param idleEvent Sensor Event of the idle position before the dropping started.
    	 * @param event Sensor Event when the threshold was reached.
    	 * @return true if the event is consumed, else false
    	 */
        public abstract boolean onDrop(SensorEvent idleEvent, SensorEvent event);
        

    	/**
    	 * Notified when the device is dropped, with the event that triggered the shake.
    	 * 
    	 * @param idleEvent Sensor Event of the idle position before the dropping started.
    	 * @param event Sensor Event when the threshold was reached.
    	 * @return true if the event is consumed, else false
    	 */
        public abstract boolean onCatch(SensorEvent idleEvent, SensorEvent event);
    }
    
    /**
     * A convenience class to extend when you only want to listen 
     * for a subset of all the gestures. 
     * This implements all methods in the 
     * SensorGestureDetector.OnSensorGestureListener 
     * but does nothing and return false for all applicable methods.
     *
     */
    public static class SimpleOnSensorGestureListener
        implements OnSensorGestureListener
    {

        public SimpleOnSensorGestureListener() {
        }

    	/**
    	 * Notified when the device is in idle position.
    	 * 
    	 * @param event Sensor Event with current idle position
    	 * @return true if the event is consumed, else false
    	 */
        public boolean onIdle(SensorEvent event) {
        	return false;
        }

    	/**
    	 * Notified when the device is rotated, with the event that triggered the shake.
    	 * 
    	 * A rotation is slow enough so that the value of acceleration is always given 
    	 * by standard gravity.
    	 * 
    	 * @param idleEvent Sensor Event of the idle position before the shaking started.
    	 * @param event Sensor Event when rotation started.
    	 * @return true if the event is consumed, else false
    	 */
        public boolean onRotate(SensorEvent idleEvent, SensorEvent event){
        	return false;
        }

        /**
    	 * Notified when the device is shaken, with the event that triggered the shake.
    	 * 
    	 * @param idleEvent Sensor Event of the idle position before the shaking started.
    	 * @param event Sensor Event when maximum acceleration was reached.
    	 * @return true if the event is consumed, else false
    	 */
        public boolean onShake(SensorEvent idleEvent, SensorEvent event) {
            return false;
        }

        /**
    	 * Notified when the device is dropped, with the event that triggered the shake.
    	 * This event is followed by onCatch().
    	 * 
    	 * @param idleEvent Sensor Event of the idle position before the dropping started.
    	 * @param event Sensor Event when the threshold was reached.
    	 * @return true if the event is consumed, else false
    	 */
        public boolean onDrop(SensorEvent idleEvent, SensorEvent event) {
            return false;
        }

        /**
    	 * Notified when the device is dropped, with the event that triggered the shake.
    	 * 
    	 * @param idleEvent Sensor Event of the idle position before the dropping started.
    	 * @param event Sensor Event when the threshold was reached.
    	 * @return true if the event is consumed, else false
    	 */
        public boolean onCatch(SensorEvent idleEvent, SensorEvent event) {
        	return false;
        }
    }
    

    private class SensorGestureHandler extends Handler
    {

        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
            
            default:
                throw new RuntimeException((new StringBuilder()).append("Unknown message ").append(msg).toString());
            }
        }

        final SensorGestureDetector mGestureDetector;

        SensorGestureHandler()
        {
            super();
                mGestureDetector = SensorGestureDetector.this;
        }

        SensorGestureHandler(Handler handler)
        {
            super(handler.getLooper());
                mGestureDetector = SensorGestureDetector.this;
        }
    }


    /**
     * Creates a GestureDetector with the supplied listener. 
     * 
     * This variant of the constructor should be used 
     * from a non-UI thread (as it allows specifying the Handler).
     * 
     * @param listener the listener invoked for all the callbacks, this must not be null.
     * @param handler the handler to use, this must not be null.
     */
    public SensorGestureDetector(OnSensorGestureListener listener, Handler handler)
    {
        mHandler = new SensorGestureHandler(handler);
        mListener = listener;
        init();
    }

    /**
     * Creates a GestureDetector with the supplied listener. 
     * 
     * You may only use this constructor from a UI thread 
     * (this is the usual situation).
     * 
     * @param listener the listener invoked for all the callbacks, 
     *                  this must not be null.
     */
    public SensorGestureDetector(OnSensorGestureListener listener)
    {
        mHandler = new SensorGestureHandler();
        mListener = listener;
        init();
    }

    private void init()
    {
        if(mListener == null)
        {
            throw new NullPointerException("OnGestureListener must not be null");
        }
        
        mShakeDetector = new ShakeDropDetector(mListener);
    }

    
    public void onSensorChanged(int sensor, float[] values) {
    	mShakeDetector.onSensorChanged(sensor, values);
    }
    

    public void onAccuracyChanged(int sensor, int accuracy) {
    
    }
        

}