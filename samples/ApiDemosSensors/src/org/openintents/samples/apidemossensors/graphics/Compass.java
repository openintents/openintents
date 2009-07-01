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
import android.util.Config;
import android.util.Log;
import android.view.View;

public class Compass extends GraphicsActivity {

    private static final String TAG = "Compass";

	//private SensorManager mSensorManager;
	private SensorManagerSimulator mSensorManager;
    private SampleView mView;
    private float[] mValues;
    
    private final SensorListener mListener = new SensorListener() {

		public void onAccuracyChanged(int sensor, int accuracy) {
			
		}
		
        public void onSensorChanged(int sensor, float[] values) {
            if (Config.LOGD) Log.d(TAG, "sensorChanged (" + values[0] + ", " + values[1] + ", " + values[2] + ")");
            mValues = values;
            if (mView != null) {
                mView.invalidate();
            }
        }

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
    }

    @Override
    protected void onResume()
    {
        if (Config.LOGD) Log.d(TAG, "onResume");
        super.onResume();
        mSensorManager.registerListener(mListener, 
        		SensorManager.SENSOR_ORIENTATION,
        		SensorManager.SENSOR_DELAY_GAME);
    }
    
    @Override
    protected void onStop()
    {
        if (Config.LOGD) Log.d(TAG, "onStop");
        mSensorManager.unregisterListener(mListener);
        super.onStop();
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

