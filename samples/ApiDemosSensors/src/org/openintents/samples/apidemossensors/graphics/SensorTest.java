/* 
 * Copyright (C) 2007 Google Inc.
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

package org.openintents.samples.apidemossensors.graphics;

import org.openintents.sensorsimulator.hardware.SensorManagerSimulator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;

public class SensorTest extends GraphicsActivity {

	//private SensorManager mSensorManager;
	private SensorManagerSimulator mSensorManager;
    private SampleView mView;
    private float[] mValues;
    
    private static class RunAve {
        private final float[] mWeights;
        private final float mWeightScale;
        private final float[] mSamples;
        private final int mDepth;
        private int mCurr;

        public RunAve(float[] weights) {
            mWeights = weights;
            
            float sum = 0;
            for (int i = 0; i < weights.length; i++) {
                sum += weights[i];
            }
            mWeightScale = 1 / sum;

            mDepth = weights.length;
            mSamples = new float[mDepth];
            mCurr = 0;
        }
        
        public void addSample(float value) {
            mSamples[mCurr] = value;
            mCurr = (mCurr + 1) % mDepth;
        }
        
        public float computeAve() {
            final int depth = mDepth;
            int index = mCurr;
            float sum = 0;
            for (int i = 0; i < depth; i++) {
                sum += mWeights[i] * mSamples[index];
                index -= 1;
                if (index < 0) {
                    index = depth - 1;
                }
            }
            return sum * mWeightScale;
        }
    };

    private final SensorListener mListener = new SensorListener() {

        private final float[] mScale = new float[] { 2, 2.5f, 0.5f };   // accel

        private float[] mPrev = new float[3];

    	public void onAccuracyChanged(int sensor, int accuracy) {
    		
    	}

        public void onSensorChanged(int sensor, float[] values) {
            boolean show = false;
            float[] diff = new float[3];

            for (int i = 0; i < 3; i++) {
                diff[i] = Math.round(mScale[i] * (values[i] - mPrev[i]) * 0.45f);
                if (Math.abs(diff[i]) > 0) {
                    show = true;
                }
                mPrev[i] = values[i];
            }
            
            if (show) {
                // only shows if we think the delta is big enough, in an attempt
                // to detect "serious" moves left/right or up/down
                android.util.Log.e("test", "sensorChanged " + sensor + " (" + values[0] + ", " + values[1] + ", " + values[2] + ")"
                                   + " diff(" + diff[0] + " " + diff[1] + " " + diff[2] + ")");
            }
            
            long now = android.os.SystemClock.uptimeMillis();
            if (now - mLastGestureTime > 1000) {
                mLastGestureTime = 0;
                
                float x = diff[0];
                float y = diff[1];
                boolean gestX = Math.abs(x) > 3;
                boolean gestY = Math.abs(y) > 3;

                if ((gestX || gestY) && !(gestX && gestY)) {
                    if (gestX) {
                        if (x < 0) {
                            android.util.Log.e("test", "<<<<<<<< LEFT <<<<<<<<<<<<");
                        } else {
                            android.util.Log.e("test", ">>>>>>>>> RITE >>>>>>>>>>>");
                        }
                    } else {
                        if (y < -2) {
                            android.util.Log.e("test", "<<<<<<<< UP <<<<<<<<<<<<");
                        } else {
                            android.util.Log.e("test", ">>>>>>>>> DOWN >>>>>>>>>>>");
                        }
                    }
                    mLastGestureTime = now;
                }
            }
        }
        
        private long mLastGestureTime;
    };

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        ////////////////////////////////////////////////////////////////
        // INSTRUCTIONS
        // ============

        // 1) Use the separate application SensorSimulatorSettings
        //    to enter the correct IP address of the SensorSimulator.
        //    This should work before you proceed, because the same
        //    settings are used for your custom sensor application.

        // 2) Include sensorsimulator-lib.jar in your project.
        //    Put that file into the 'lib' folder.
        //    In Eclipse, right-click on your project in the 
        //    Package Explorer, select
        //    Properties > Java Build Path > (tab) Libraries
        //    then click Add JARs to add this jar.

        // 3) You need the permission
        //    <uses-permission android:name="android.permission.INTERNET"/>
        //    in your Manifest file!

        // 4) Instead of calling the system service to obtain the Sensor manager,
        //    you should obtain it from the SensorManagerSimulator:

        //mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager = SensorManagerSimulator.getSystemService(this, SENSOR_SERVICE);

        // 5) Connect to the sensor simulator, using the settings
        //    that have been set previously with SensorSimulatorSettings
        mSensorManager.connectSimulator();

        // The rest of your application can stay unmodified.
        ////////////////////////////////////////////////////////////////

        mView = new SampleView(this);
        setContentView(mView);
//        android.util.Log.d("skia", "create " + mSensorManager);
    }

    @Override
    protected void onResume() {
        super.onResume();
        
        registerSensorListener();
//        android.util.Log.d("skia", "resume " + mSensorManager);
    }

	private void registerSensorListener() {
		int mask = 0;
//        mask |= SensorManager.SENSOR_ORIENTATION;
        mask |= SensorManager.SENSOR_ACCELEROMETER;
        
        mSensorManager.registerListener(mListener, mask, SensorManager.SENSOR_DELAY_FASTEST);
	}
    
    @Override
    protected void onStop() {
        unregisterSensorListener();
        super.onStop();
//        android.util.Log.d("skia", "stop " + mSensorManager);
    }

	private void unregisterSensorListener() {
		mSensorManager.unregisterListener(mListener);
	}

    private class SampleView extends View {
        private Paint   mPaint = new Paint();
        private Path    mPath = new Path();
        private boolean mAnimate;
        private long    mNextTime;

        public SampleView(Context context) {
            super(context);

            // Construct a wedge-shaped path
            mPath.moveTo(0, -50);
            mPath.lineTo(-20, 60);
            mPath.lineTo(0, 50);
            mPath.lineTo(20, 60);
            mPath.close();
        }
    
        @Override protected void onDraw(Canvas canvas) {
            Paint paint = mPaint;

            canvas.drawColor(Color.WHITE);
            
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);

            int w = canvas.getWidth();
            int h = canvas.getHeight();
            int cx = w / 2;
            int cy = h / 2;

            canvas.translate(cx, cy);
            if (mValues != null) {            
                canvas.rotate(-mValues[0]);
            }
            canvas.drawPath(mPath, mPaint);
        }
    
        @Override
        protected void onAttachedToWindow() {
            mAnimate = true;
            super.onAttachedToWindow();
        }
        
        @Override
        protected void onDetachedFromWindow() {
            mAnimate = false;
            super.onDetachedFromWindow();
        }
    }
    
}

