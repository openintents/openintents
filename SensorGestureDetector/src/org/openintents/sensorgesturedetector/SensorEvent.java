package org.openintents.sensorgesturedetector;

import android.hardware.SensorManager;

/**
 * Object used to report sensor events.
 * 
 * @author Peli
 *
 */
public class SensorEvent {
        
        private int mSensor;
        float[] mValues;
        long mEventTime;
        
        SensorEvent() {
                
        }

        private SensorEvent(int sensor, float[] values, long eventTime) {
            mSensor = sensor;
            mValues = values;
            mEventTime = eventTime;
        }
        
        public static SensorEvent obtain(int sensor, float[] values, long eventTime) {
        	
        	// TODO: obtain sensor event from garbage can.
        	SensorEvent event = new SensorEvent(sensor, values, eventTime);
        	
        	return event;
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

}