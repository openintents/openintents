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

package org.openintents.samples.apidemossensors.os;

import org.openintents.OpenIntents;
import org.openintents.hardware.SensorManagerSimulator;
import org.openintents.provider.Hardware;
import org.openintents.samples.apidemossensors.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

/**
 * <h3>Application that displays the values of the acceleration sensor graphically.</h3>

<p>This demonstrates the {@link android.hardware.SensorManager android.hardware.SensorManager} class.

<h4>Demo</h4>
OS / Sensors
 
<h4>Source files</h4>
 * <table class="LinkTable">
 *         <tr>
 *             <td >src/com/android/samples/os/Sensors.java</td>
 *             <td >Sensors</td>
 *         </tr>
 * </table> 
 */
public class Sensors extends Activity {
    /** Tag string for our debug logs */
    private static final String TAG = "Sensors";

    private SensorManager mSensorManager;
    private GraphView mGraphView;

    private class GraphView extends View implements SensorListener
    {
        private Bitmap  mBitmap;
        private Paint   mPaint = new Paint();
        private Canvas  mCanvas = new Canvas();
        private Path    mPath = new Path();
        private RectF   mRect = new RectF();
        private float   mLastValues[] = new float[3*2];
        private float   mOrientationValues[] = new float[3];
        private int     mColors[] = new int[3*2];
        private float   mLastX;
        private float   mScale[] = new float[2];
        private float   mYOffset;
        private float   mMaxX;
        private float   mSpeed = 1.0f;
        private float   mWidth;
        private float   mHeight;
        
        public GraphView(Context context) {
            super(context);
            mColors[0] = Color.argb(192, 255, 64, 64);
            mColors[1] = Color.argb(192, 64, 128, 64);
            mColors[2] = Color.argb(192, 64, 64, 255);
            mColors[3] = Color.argb(192, 64, 255, 255);
            mColors[4] = Color.argb(192, 128, 64, 128);
            mColors[5] = Color.argb(192, 255, 255, 64);

            mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            mRect.set(-0.5f, -0.5f, 0.5f, 0.5f);
            mPath.arcTo(mRect, 0, 180);
        }
        
        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.RGB_565);
            mCanvas.setBitmap(mBitmap);
            mCanvas.drawColor(0xFFFFFFFF);
            mYOffset = h * 0.5f;
            mScale[0] = - (h * 0.5f * (1.0f / (SensorManager.STANDARD_GRAVITY * 2)));
            mScale[1] = - (h * 0.5f * (1.0f / (SensorManager.MAGNETIC_FIELD_EARTH_MAX)));
            mWidth = w;
            mHeight = h;
            if (mWidth < mHeight) {
                mMaxX = w;
            } else {
                mMaxX = w-50;
            }
            mLastX = mMaxX;
            super.onSizeChanged(w, h, oldw, oldh);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            synchronized (this) {
                if (mBitmap != null) {
                    final Paint paint = mPaint;
                    final Path path = mPath;
                    final int outer = 0xFFC0C0C0;
                    final int inner = 0xFFff7010;

                    if (mLastX >= mMaxX) {
                        mLastX = 0;
                        final Canvas cavas = mCanvas;
                        final float yoffset = mYOffset;
                        final float maxx = mMaxX;
                        final float oneG = SensorManager.STANDARD_GRAVITY * mScale[0];
                        paint.setColor(0xFFAAAAAA);
                        cavas.drawColor(0xFFFFFFFF);
                        cavas.drawLine(0, yoffset,      maxx, yoffset,      paint);
                        cavas.drawLine(0, yoffset+oneG, maxx, yoffset+oneG, paint);
                        cavas.drawLine(0, yoffset-oneG, maxx, yoffset-oneG, paint);
                    }
                    canvas.drawBitmap(mBitmap, 0, 0, null);

                    float[] values = mOrientationValues;
                    if (mWidth < mHeight) {
                        float w0 = mWidth * 0.333333f;
                        float w  = w0 - 32;
                        float x = w0*0.5f;
                        for (int i=0 ; i<3 ; i++) {
                            canvas.save(Canvas.MATRIX_SAVE_FLAG);
                            canvas.translate(x, w*0.5f + 4.0f);
                            canvas.save(Canvas.MATRIX_SAVE_FLAG);
                            paint.setColor(outer);
                            canvas.scale(w, w);
                            canvas.drawOval(mRect, paint);
                            canvas.restore();
                            canvas.scale(w-5, w-5);
                            paint.setColor(inner);
                            canvas.rotate(-values[i]);
                            canvas.drawPath(path, paint);
                            canvas.restore();
                            x += w0;
                        }
                    } else {
                        float h0 = mHeight * 0.333333f;
                        float h  = h0 - 32;
                        float y = h0*0.5f;
                        for (int i=0 ; i<3 ; i++) {
                            canvas.save(Canvas.MATRIX_SAVE_FLAG);
                            canvas.translate(mWidth - (h*0.5f + 4.0f), y);
                            canvas.save(Canvas.MATRIX_SAVE_FLAG);
                            paint.setColor(outer);
                            canvas.scale(h, h);
                            canvas.drawOval(mRect, paint);
                            canvas.restore();
                            canvas.scale(h-5, h-5);
                            paint.setColor(inner);
                            canvas.rotate(-values[i]);
                            canvas.drawPath(path, paint);
                            canvas.restore();
                            y += h0;
                        }
                    }

                }
            }
        }

        public void onSensorChanged(int sensor, float[] values) {
            //Log.d(TAG, "sensor: " + sensor + ", x: " + values[0] + ", y: " + values[1] + ", z: " + values[2]);
            synchronized (this) {
                if (mBitmap != null) {
                    final Canvas canvas = mCanvas;
                    final Paint paint = mPaint;
                    if (sensor == SensorManager.SENSOR_ORIENTATION) {
                        for (int i=0 ; i<3 ; i++) {
                            mOrientationValues[i] = values[i];
                        }
                    } else {
                        float deltaX = mSpeed;
                        float newX = mLastX + deltaX;

                        int j = (sensor == SensorManager.SENSOR_MAGNETIC_FIELD) ? 1 : 0;
                        for (int i=0 ; i<3 ; i++) {
                            int k = i+j*3;
                            final float v = mYOffset + values[i] * mScale[j];
                            paint.setColor(mColors[k]);
                            canvas.drawLine(mLastX, mLastValues[k], newX, v, paint);
                            mLastValues[k] = v;
                        }
                        if (sensor == SensorManager.SENSOR_MAGNETIC_FIELD)
                            mLastX += mSpeed;
                    }
                    invalidate();
                }
            }
        }
    }
    
    /**
     * Initialization of the Activity after it is first created.  Must at least
     * call {@link android.app.Activity#setContentView setContentView()} to
     * describe what is to be displayed in the screen.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Be sure to call the super class.
        super.onCreate(savedInstanceState);

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
        
        mGraphView = new GraphView(this);
        setContentView(mGraphView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(mGraphView, 
                SensorManager.SENSOR_ACCELEROMETER | 
                SensorManager.SENSOR_MAGNETIC_FIELD | 
                SensorManager.SENSOR_ORIENTATION,
                SensorManager.SENSOR_DELAY_FASTEST);
    }
    
    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(mGraphView);
        super.onStop();
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
	        mSensorManager.unregisterListener(mGraphView);
			
			if (!mConnected) {
				// now connect to simulator
				SensorManagerSimulator.connectSimulator();
			} else {
				// or disconnect to simulator
				SensorManagerSimulator.disconnectSimulator();				
			}
			
			// now enable the new sensors
	        mSensorManager.registerListener(mGraphView, 
	                SensorManager.SENSOR_ACCELEROMETER | 
	                SensorManager.SENSOR_MAGNETIC_FIELD | 
	                SensorManager.SENSOR_ORIENTATION,
	                SensorManager.SENSOR_DELAY_FASTEST);
	        
			mConnected = ! mConnected;
			return true;
		}
		return super.onOptionsItemSelected(item);
		
	}
}
