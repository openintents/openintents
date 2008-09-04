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

import org.openintents.OpenIntents;
import org.openintents.hardware.SensorManagerSimulator;
import org.openintents.provider.Hardware;
import org.openintents.samples.apidemossensors.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.*;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Config;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class SensorTest extends GraphicsActivity {

	private SensorManager mSensorManager;
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
        
        ////////////////////////////////////////////////////////
        // Test if OpenIntents is present (for sensor settings)
        OpenIntents.requiresOpenIntents(this);

        // !! Very important !!
        // Before calling any of the Simulator data,
        // the Content resolver has to be set !!
        Hardware.mContentResolver = getContentResolver();
        
        // Link sensor manager to OpenIntents Sensor simulator
        // mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager = (SensorManager) new SensorManagerSimulator((SensorManager) getSystemService(SENSOR_SERVICE));
		////////////////////////////////////////////////////////
        
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
    

    ////////////////////////////////////////////////////////
    // Add some menus for connecting to sensor simulator

	private static final int MENU_SETTINGS = Menu.FIRST;
	private static final int MENU_CONNECT_SIMULATOR = Menu.FIRST + 1;
	private boolean mConnected = false;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Standard menu
		menu.add(0, MENU_SETTINGS, 0, "Settings")
			.setIcon(R.drawable.mobile_shake_settings001a)
			.setShortcut('0', 's');
		menu.add(1, MENU_CONNECT_SIMULATOR, 0, "Connect")
			.setIcon(R.drawable.mobile_shake001a)
			.setShortcut('1', 'c');

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
        
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS:
			Intent intent = new Intent(Intent.ACTION_VIEW, Hardware.Preferences.CONTENT_URI);
			startActivity(intent);
			return true;
			
		case MENU_CONNECT_SIMULATOR:
			
			// first disable the current sensors:
			unregisterSensorListener();
			
			if (!mConnected) {
				// now connect to simulator
				SensorManagerSimulator.connectSimulator();
			} else {
				// or disconnect to simulator
				SensorManagerSimulator.disconnectSimulator();				
			}
			
			// now enable the new sensors
			registerSensorListener();
	        
			mConnected = ! mConnected;
			return true;
		}
		return super.onOptionsItemSelected(item);
		
	}
}

