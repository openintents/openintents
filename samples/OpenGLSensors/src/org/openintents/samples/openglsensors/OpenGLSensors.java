
/**
 * This application is based on Google's ApiDemos
 * com.google.android.samples.graphics.GLView1.java
 * 
 * which is licensed under the same license (Apache License, Version 2.0
 */

package org.openintents.samples.openglsensors;

/*
 * YOU HAVE TO MANUALLY INCLUDE THE OPENINTENTS-LIB-n.n.n.JAR FILE:
 * 
 * In the Eclipse Package Explorer, right-click on the imported 
 * project OpenGLSensors, select "Properties", then "Java Build Path" 
 * and tab "Libraries". There "Add External JARs..." and select 
 * lib/openintents-lib-n.n.n.jar. 
 */

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.openintents.hardware.Sensors;
import org.openintents.provider.Hardware;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.OpenGLContext;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.Menu.Item;


/**
 * Example of how to use OpenGL|ES in a custom view
 *
 */

public class OpenGLSensors extends Activity {

	private static final int MENU_SETTINGS = Menu.FIRST;
	private static final int MENU_CONNECT_SIMULATOR = Menu.FIRST + 1;
	
	private static final int MENU_SENSOR = Menu.FIRST + 100;
	private static final int MENU_SENSOR_NOT_AVAILABLE = Menu.FIRST + 101;
	private static final int MENU_SENSOR_ACCELEROMETER = Menu.FIRST + 102;
	private static final int MENU_SENSOR_COMPASS = Menu.FIRST + 103;
	private static final int MENU_SENSOR_ACCELEROMETER_COMPASS = Menu.FIRST + 104;
	private static final int MENU_SENSOR_ORIENTATION = Menu.FIRST + 105;
	private static final int MENU_SENSOR_ORIENTATION_COMPASS = Menu.FIRST + 106;
	
	private static final int MENU_SHAPE = Menu.FIRST + 200;
	private static final int MENU_SHAPE_CUBE = Menu.FIRST + 201;
	private static final int MENU_SHAPE_PYRAMID = Menu.FIRST + 202;
	private static final int MENU_SHAPE_MAGNET = Menu.FIRST + 203;
	
	
	private boolean mConnected;
	
	private boolean mAccelerometerSupported;
	private boolean mCompassSupported;
	private boolean mOrientationSupported;
	
	 // sensors to be used
	public boolean mUseAccelerometer;
	public boolean mUseCompass;
	public boolean mUseOrientation;
	
    @Override
	protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);     
        setContentView(new GLView( getApplication() , this));
        
        // !! Very important !!
        // Before calling any of the Simulator data,
        // the Content resolver has to be set !!
        Hardware.mContentResolver = getContentResolver();
		
        // TODO: Sensors.isSimulatorConnected() should be implemented 
        // and used here.
        mConnected = false;
        
        findSupportedSensors();
        
        useBestAvailableSensors();

        enableAllSensors();
    }
    
    @Override
	protected void onResume()
    {
        super.onResume();
    	//android.os.Debug.startMethodTracing("/tmp/trace/GLView1.dmtrace",
        //  8 * 1024 * 1024);
    }
    
    @Override
	protected void onStop()
    {
        super.onStop();
        //android.os.Debug.stopMethodTracing();
    }
    
    ////////////////////////////////////////////////////////
    // The menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Standard menu
		menu.add(0, MENU_SETTINGS, R.string.settings)
			.setShortcut(KeyEvent.KEYCODE_0, 0, KeyEvent.KEYCODE_S);
		menu.add(0, MENU_CONNECT_SIMULATOR, R.string.connect_simulator)
			.setShortcut(KeyEvent.KEYCODE_1, 0, KeyEvent.KEYCODE_C);
		
		
		SubMenu menuSensor;
		menuSensor = menu.addSubMenu(0, MENU_SENSOR, R.string.sensor);
		menuSensor.add(0, MENU_SENSOR_NOT_AVAILABLE, R.string.not_available);
		menuSensor.add(0, MENU_SENSOR_ACCELEROMETER, R.string.accelerometer);
		menuSensor.add(0, MENU_SENSOR_COMPASS, R.string.compass);
		menuSensor.add(0, MENU_SENSOR_ACCELEROMETER_COMPASS, 
				R.string.accelerometer_compass);
		menuSensor.add(0, MENU_SENSOR_ORIENTATION, R.string.orientation);
		//menuSensor.add(0, MENU_SENSOR_ORIENTATION_COMPASS, R.string.orientation_compass);
		
		// Generate any additional actions that can be performed on the
        // overall list.  This allows other applications to extend
        // our menu with their own actions.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.ALTERNATIVE_CATEGORY);
        menu.addIntentOptions(
            Menu.ALTERNATIVE, 0, new ComponentName(this, OpenGLSensors.class),
            null, intent, 0, null);
        
        // Set checkable items:
        menu.setItemCheckable(MENU_CONNECT_SIMULATOR, true);

		menu.setItemCheckable(MENU_SENSOR_ACCELEROMETER, true);
		menu.setItemCheckable(MENU_SENSOR_COMPASS, true);
		menu.setItemCheckable(MENU_SENSOR_ACCELEROMETER_COMPASS,  true);
		menu.setItemCheckable(MENU_SENSOR_ORIENTATION, true);
		menu.setItemCheckable(MENU_SENSOR_ORIENTATION_COMPASS, true);

		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		
		// TODO: Add item-specific menu items (see NotesList.java example)
		// like edit, strike-through, delete.
		
		// Delete list is possible, if we have more than one list:
		//menu.setItemShown(MENU_CONNECT, !mConnected);
		//menu.setItemShown(MENU_DISCONNECT, mConnected);
		
        menu.setItemChecked(MENU_CONNECT_SIMULATOR, mConnected);

        menu.setItemShown(MENU_SENSOR_NOT_AVAILABLE, 
        		!(mAccelerometerSupported
        			|| mCompassSupported
        			|| mOrientationSupported));
		menu.setItemShown(MENU_SENSOR_ACCELEROMETER, mAccelerometerSupported);
		menu.setItemShown(MENU_SENSOR_COMPASS, mCompassSupported);
		menu.setItemShown(MENU_SENSOR_ACCELEROMETER_COMPASS, 
				mAccelerometerSupported && mCompassSupported);
		menu.setItemShown(MENU_SENSOR_ORIENTATION, mOrientationSupported);
		//menu.setItemShown(MENU_SENSOR_ORIENTATION_COMPASS, 
		//		mOrientationSupported && mCompassSupported);
        
		menu.setItemChecked(MENU_SENSOR_ACCELEROMETER, 
				mUseAccelerometer && !mUseCompass);
		menu.setItemChecked(MENU_SENSOR_COMPASS, 
				mUseCompass && !mUseAccelerometer);
		menu.setItemChecked(MENU_SENSOR_ACCELEROMETER_COMPASS,  
				mUseAccelerometer && mUseCompass);
		menu.setItemChecked(MENU_SENSOR_ORIENTATION, 
				mUseOrientation);
		//menu.setItemChecked(MENU_SENSOR_ORIENTATION_COMPASS,  
		//		mUseOrientation && mUseCompass);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(Item item) {
		switch (item.getId()) {
		case MENU_SETTINGS:
			Intent intent = new Intent(Intent.MAIN_ACTION, Hardware.Preferences.CONTENT_URI);
			startActivity(intent);
			return true;
			
		case MENU_CONNECT_SIMULATOR:
			// first disable the current sensors:
			disableAllSensors();
			
			if (!mConnected) {
				// now connect to simulator
				Sensors.connectSimulator();
			} else {
				// or disconnect to simulator
				Sensors.disconnectSimulator();				
			}
			
			// check again which sensors are now supported:
	        findSupportedSensors();
	        
	        // check whether the sensors we just used
	        // are still supported 
	        // (if not, choose again the best sensors
	        //  from those that are available now)
	        updateUseBestAvailableSensors();
			
	        // now enable the sensors again:
			enableAllSensors();
			
			mConnected = ! mConnected;
			return true;
			
		case MENU_SENSOR_ACCELEROMETER:
			disableAllSensors();
			useSensorsReset();
			mUseAccelerometer = true;
			enableAllSensors();
			return true;

		case MENU_SENSOR_COMPASS:
			disableAllSensors();
			useSensorsReset();
			mUseCompass = true;
			enableAllSensors();
			return true;

		case MENU_SENSOR_ACCELEROMETER_COMPASS:
			disableAllSensors();
			useSensorsReset();
			mUseAccelerometer = true;
			mUseCompass = true;
			enableAllSensors();
			return true;

		case MENU_SENSOR_ORIENTATION:
			disableAllSensors();
			useSensorsReset();
			mUseOrientation = true;
			enableAllSensors();
			return true;
		}
		return super.onOptionsItemSelected(item);
		
	}
	
	/* 
	 * Get sensor capabilities
	 */
	public void findSupportedSensors() {
		mAccelerometerSupported
			= isSupportedSensor(Sensors.SENSOR_ACCELEROMETER);
		mCompassSupported
			= isSupportedSensor(Sensors.SENSOR_COMPASS);
		mOrientationSupported
			= isSupportedSensor(Sensors.SENSOR_ORIENTATION);
	}
	
	/**
	 *  Check whether a specific sensor is supported.
	 */
	public boolean isSupportedSensor(String sensor) {
		String[] sensors = Sensors.getSupportedSensors();
		for (String s : sensors) {
			if (s.contentEquals(sensor)) return true;
		};
		return false;
	}

	/**
	 * Disable all sensors that we want to use.
	 */
	public void disableAllSensors() {
		if (mUseAccelerometer)
			Sensors.disableSensor(Sensors.SENSOR_ACCELEROMETER);
		if (mUseCompass)
			Sensors.disableSensor(Sensors.SENSOR_COMPASS);
		if (mUseOrientation)
			Sensors.disableSensor(Sensors.SENSOR_ORIENTATION);
	}
	
	/**
	 * Enable all sensors that we want to use.
	 */
	public void enableAllSensors() {
		if (mUseAccelerometer)
			Sensors.enableSensor(Sensors.SENSOR_ACCELEROMETER);
		if (mUseCompass)
			Sensors.enableSensor(Sensors.SENSOR_COMPASS);
		if (mUseOrientation)
			Sensors.enableSensor(Sensors.SENSOR_ORIENTATION);		
	}
	
	/**
	 * Selects the best supported sensors currently available.
	 * This is orientation if supported. Otherwise a combination
	 * of accelerometer and compass is used (if supported).
	 */
    public void useBestAvailableSensors() {
	    // use the best sensor available:
	    useSensorsReset();
	    
	    if (mOrientationSupported) {
	    	// the best is orientation
	    	mUseOrientation = true;
	    } else {
	    	// otherwise use any of accelerometer or compass
	    	// or both together.
	    	if (mAccelerometerSupported) mUseAccelerometer = true;
	       	if (mCompassSupported) mUseCompass = true;
	    }
    }
    
    /** 
     * See if the currently selected sensor(s) is (are) still available.
     * If not, useBestAvailableSensors() is called.
     */
    public void updateUseBestAvailableSensors() {
    	if ((mUseAccelerometer && !mAccelerometerSupported) 
    			|| (mUseCompass && !mCompassSupported)
    			|| (mUseOrientation && !mOrientationSupported)) {
    		useBestAvailableSensors();
    	}
    }
    
    /**
     * Resets which sensors we want to use.
     */
    public void useSensorsReset() {
    	mUseAccelerometer = false;
    	mUseCompass = false;
    	mUseOrientation = false;
    }
}

class GLView extends View
{
	private OpenGLSensors mOpenGLAccelerator; // needed to access sensor settings
	
    /**
     * The View constructor is a good place to allocate our OpenGL context
     */
    public GLView(Context context, OpenGLSensors newOpenGLAccelerator)
    {
        super(context);
        
        /* 
         * Create an OpenGL|ES context. This must be done only once, an
         * OpenGL contex is a somewhat heavy object.
         */
        mGLContext = new OpenGLContext( OpenGLContext.DEPTH_BUFFER );
        mCube = new Cube();
        mPyramid = new Pyramid();
        mAnimate = false;
        mOpenGLAccelerator = newOpenGLAccelerator;
        
        /*
         * First, we need to get to the appropriate GL interface.
         * This is simply done by casting the GL context to either
         * GL10 or GL11.
         */
        GL10 gl = (GL10)(mGLContext.getGL());

        /*
         * Some one-time OpenGL initialization can be made here
         * probably based on features of this particular context
         */
         gl.glHint(gl.GL_PERSPECTIVE_CORRECTION_HINT, gl.GL_FASTEST);

    }
    
    /*
     * Start the animation only once we're attached to a window
     * @see android.view.View#onAttachedToWindow()
     */
    @Override
    protected void onAttachedToWindow() {
        mAnimate = true;
        Message msg = mHandler.obtainMessage(INVALIDATE);
        mNextTime = SystemClock.uptimeMillis();
        mHandler.sendMessageAtTime(msg, mNextTime);
        super.onAttachedToWindow();
    }
    
    /*
     * Make sure to stop the animation when we're no longer on screen,
     * failing to do so will cause most of the view hierarchy to be
     * leaked until the current process dies.
     * @see android.view.View#onDetachedFromWindow()
     */
    @Override
    protected void onDetachedFromWindow() {
        mAnimate = false;
        super.onDetachedFromWindow();
    }

    /**
     * Draw the view content
     * 
     * @see android.view.View#onDraw(android.graphics.Canvas)
     */
    @Override
    protected void onDraw(Canvas canvas) {
    	if (true) {
        /*
         * First, we need to get to the appropriate GL interface.
         * This is simply done by casting the GL context to either
         * GL10 or GL11.
         */
        GL10 gl = (GL10)(mGLContext.getGL());
        
        /*
         * Before we can issue GL commands, we need to make sure all
         * native drawing commands are completed. Simply call
         * waitNative() to accomplish this. Once this is done, no native
         * calls should be issued.
         */
        mGLContext.waitNative(canvas, this);
        
            int w = getWidth();
            int h = getHeight();

            /*
             * Set the viewport. This doesn't have to be done each time
             * draw() is called. Typically this is called when the view
             * is resized.
             */


            gl.glViewport(0, 0, w, h);
        
            /*
             * Set our projection matrix. This doesn't have to be done
             * each time we draw, but usualy a new projection needs to be set
             * when the viewport is resized.
             */
             
            float ratio = (float)w / h;
            gl.glMatrixMode(gl.GL_PROJECTION);
            gl.glLoadIdentity();
            gl.glFrustumf(-ratio, ratio, -1, 1, 2, 12);

            /*
             * dithering is enabled by default in OpenGL, unfortunattely
             * it has a significant impact on performace in software
             * implementation. Often, it's better to just turn it off.
             */
             gl.glDisable(gl.GL_DITHER);

            /*
             * Usually, the first thing one might want to do is to clear
             * the screen. The most efficient way of doing this is to use
             * glClear(). However we must make sure to set the scissor
             * correctly first. The scissor is always specified in window
             * coordinates:
             */

            gl.glClearColor(1,1,1,1);
            gl.glEnable(gl.GL_SCISSOR_TEST);
            gl.glScissor(0, 0, w, h);
            gl.glClear(gl.GL_COLOR_BUFFER_BIT | gl.GL_DEPTH_BUFFER_BIT);


            /*
             * Now we're ready to draw some 3D object
             */

            gl.glMatrixMode(gl.GL_MODELVIEW);
            gl.glLoadIdentity();
            
            gl.glTranslatef(0, 0, -3.0f);
            gl.glScalef(0.5f, 0.5f, 0.5f);
            
            gl.glColor4f(0.7f, 0.7f, 0.7f, 1.0f);
            gl.glEnableClientState(gl.GL_VERTEX_ARRAY);
            gl.glEnableClientState(gl.GL_COLOR_ARRAY);
            gl.glEnable(gl.GL_CULL_FACE);
            gl.glEnable(gl.GL_DEPTH_TEST);

            //gl.glRotatef(mAngle,        0, 1, 0);
            //gl.glRotatef(mAngle*0.25f,  1, 0, 0);
            
            float yaw = 0;
            float pitch = 0;
            float roll = 0;
            
            float compass_yaw = 0;
            float compass_pitch = 0;
            float compass_roll = 0;
            
            if (mOpenGLAccelerator.mUseOrientation) {
            	// Using orientation sensor definitely is the 
            	// easiest way to rotate things:
            	
            	// First get the sensor values
	            //int num = Sensors.getNumSensorValues(Sensors.SENSOR_ORIENTATION);
	            final int num = 3; // we known that there are 3 values.
				float[] val = new float[num];
				Sensors.readSensor(Sensors.SENSOR_ORIENTATION, val);

				// Assign yaw, pitch, and roll.
				// Negative angles are used, because we have to
				// "undo" the rotations that brought the mobile phone
				// into its current position.
				yaw = -val[0];
				pitch = -val[1];
				roll = -val[2];
            }
            
            if (mOpenGLAccelerator.mUseAccelerometer) {
            	// We can only let the pyramid point up,
            	// but we can not obtain information about the yaw.
            	
            	// (strictly speaking, we can only adjust two of the three
            	//  variables (yaw, pitch, and roll). Since the
            	//  standard orientation is to point down (-z), we
            	//  choose to calculate pitch and roll as the 
            	//  deviation from that standard position.)
            	
            	// First get the sensor values
	            //int num = Sensors.getNumSensorValues(Sensors.SENSOR_ACCELEROMETER);
	            final int num = 3; // we known that there are 3 values.
				float[] val = new float[num];
				Sensors.readSensor(Sensors.SENSOR_ACCELEROMETER, val);
				
				// we can only adjust pitch and roll:
				double r = Math.sqrt(val[0]*val[0] + val[2]*val[2]);
				pitch = (float) - Math.toDegrees(Math.atan2(-val[1], r));
				roll = (float) - Math.toDegrees(Math.atan2(val[0], -val[2]));	
            }
            
            if (mOpenGLAccelerator.mUseCompass) {
            	// We can only adjust the compass to point
            	// along the magnetic field, but we can not
            	// say where "up" is.
            	// Since the expected standard orientation is
            	// to point north (that is in +y direction),
            	// we use the information to adjust 
            	// compass_yaw and compass_pitch,
            	// but we don't know compass_roll.
            	
            	// First get the sensor values
	            //int num = Sensors.getNumSensorValues(Sensors.SENSOR_COMPASS);
	            final int num = 3; // we known that there are 3 values.
				float[] val = new float[num];
				Sensors.readSensor(Sensors.SENSOR_COMPASS, val);
					
				// we can only adjust yaw and pitch:
				//double r = Math.sqrt(val[0]*val[0] + val[1]*val[1]);
				//compass_pitch = (float) - Math.toDegrees(Math.atan2(-val[2], r));
				//compass_yaw = (float) - Math.toDegrees(Math.atan2(-val[0], val[1]));	
				double r = Math.sqrt(val[1]*val[1] + val[2]*val[2]);
				compass_yaw = (float) - Math.toDegrees(Math.atan2(-val[0], r));	
	            compass_pitch = (float) - Math.toDegrees(Math.atan2(-val[2], val[1]));
			}
            
            if (mOpenGLAccelerator.mUseAccelerometer 
            		&& mOpenGLAccelerator.mUseCompass) {
				// If we use both sensors, we could use the 
            	// compass orientation to fix the yaw of the accelerometer
            	// information.
            	
            	// TODO Fix accelerometer yaw using compass direction.
            }
            
			// Now perform the rotation:
			gl.glRotatef((int) roll, 0, 1, 0);
			gl.glRotatef((int) pitch, 1, 0, 0);
			gl.glRotatef((int) yaw, 0, 0, -1);

			//mCube.draw(gl);
            
			if (mOpenGLAccelerator.mUseAccelerometer
					|| mOpenGLAccelerator.mUseOrientation) {
				// draw the pyramid
				mPyramid.draw(gl);
			}
			
            if (mOpenGLAccelerator.mUseCompass) {
            	// Plot the compass on top of the pyramid:
                gl.glTranslatef(0, 0, 1.0f);
                
                // Compass coordinates are given with respect
                // to the phone, not with respect to the pyramid.
                // So we first have to undo the rotations from above:
                gl.glRotatef((int) -yaw, 0, 0, -1);
                gl.glRotatef((int) -pitch, 1, 0, 0);
    			gl.glRotatef((int) -roll, 0, 1, 0);
    			
                // Now we perform the compass rotations:
                gl.glRotatef((int) compass_roll, 0, 1, 0); // should be 0
                gl.glRotatef((int) compass_pitch, 1, 0, 0);
                gl.glRotatef((int) compass_yaw, 0, 0, -1);
                
                gl.glScalef(0.1f, 0.25f, 0.1f);
                
	            // Draw the compass
	            gl.glTranslatef(0, 1f, 0);
	            mCube.drawColor(gl, 1,0,0);
	            gl.glTranslatef(0, -2f, 0);
	            mCube.drawColor(gl, 1,1,1);
            }
            //mAngle += 1.2f;
            //mAngle += 12f;

        /*
         * Once we're done with GL, we need to flush all GL commands and
         * make sure they complete before we can issue more native
         * drawing commands. This is done by calling waitGL().
         */
        mGLContext.waitGL();
    	}
    }
    

    // ------------------------------------------------------------------------

    private static final int INVALIDATE = 1;

    private final Handler mHandler = new Handler() {
        @Override
		public void handleMessage(Message msg) {
            if (mAnimate && msg.what == INVALIDATE) {
                invalidate();
                msg = obtainMessage(INVALIDATE);
                long current = SystemClock.uptimeMillis();
                if (mNextTime < current) {
                    mNextTime = current + 20;
                }
                //sendMessageAtTime(msg, mNextTime);
                mNextTime += 20;
                sendMessageDelayed(msg, 50);
            }
        }
    };

    private OpenGLContext   mGLContext;
    private Cube            mCube;
    private Pyramid         mPyramid;
    private float           mAngle;
    private long            mNextTime;
    private boolean         mAnimate;
}


class Cube
{
    public Cube()
    {
        int one = 0x10000;
        int vertices[] = {
               -one, -one, -one,
                one, -one, -one,
                one,  one, -one,
               -one,  one, -one,
               -one, -one,  one,
                one, -one,  one,
                one,  one,  one,
               -one,  one,  one,
            };
            
        int colors[] = {
                  0,    0,    0,  one,
                one,    0,    0,  one,
                one,  one,    0,  one,
                  0,  one,    0,  one,
                  0,    0,  one,  one,
                one,    0,  one,  one,
                one,  one,  one,  one,
                  0,  one,  one,  one,
            };

        byte indices[] = {
                0, 4, 5,    0, 5, 1,
                1, 5, 6,    1, 6, 2,
                2, 6, 7,    2, 7, 3,
                3, 7, 4,    3, 4, 0,
                4, 7, 6,    4, 6, 5,
                3, 0, 1,    3, 1, 2
        };

	// Buffers to be passed to gl*Pointer() functions
	// must be direct, i.e., they must be placed on the
	// native heap where the garbage collector cannot
	// move them.
    //
    // Buffers with multi-byte datatypes (e.g., short, int, float)
    // must have their byte order set to native order

    ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length*4);
    vbb.order(ByteOrder.nativeOrder());
    mVertexBuffer = vbb.asIntBuffer();
	mVertexBuffer.put(vertices);
	mVertexBuffer.position(0);

    ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);
    cbb.order(ByteOrder.nativeOrder());
	mColorBuffer = cbb.asIntBuffer();
	mColorBuffer.put(colors);
	mColorBuffer.position(0);

	mIndexBuffer = ByteBuffer.allocateDirect(indices.length);
	mIndexBuffer.put(indices);
	mIndexBuffer.position(0);
    }
    
    public void draw(GL10 gl)
    {
        gl.glFrontFace(gl.GL_CW);
        gl.glVertexPointer(3, gl.GL_FIXED, 0, mVertexBuffer);
        gl.glColorPointer(4, gl.GL_FIXED, 0, mColorBuffer);
        gl.glDrawElements(gl.GL_TRIANGLES, 36, gl.GL_UNSIGNED_BYTE, mIndexBuffer);
    } 
    
    public void drawColor(GL10 gl, int red, int green, int blue)
    {
    	int one = 0x10000;
    	one = one * 3/4;
        int colors[] = {
        	  red*one, green*one ,  blue*one,  one,
        	  red*one*2/4, green*one*2/4,  blue*one*2/4,  one,
        	  red*one, green*one, blue*one,  one,
        	  red*one*2/4, green*one*2/4,blue*one*2/4,  one,
        	  red*one, green*one,blue*one,  one,
        	  red*one*6/8,  green*one*6/8,  blue*one*6/8,  one,
        	  red*one, green*one, blue*one,  one,
        	  red*one*6/8,green*one*6/8, blue*one*6/8,  one,
           };

        ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length*4);
        cbb.order(ByteOrder.nativeOrder());
    	mColorBuffer = cbb.asIntBuffer();
    	mColorBuffer.put(colors);
    	mColorBuffer.position(0);
    	
        gl.glFrontFace(gl.GL_CW);
        gl.glVertexPointer(3, gl.GL_FIXED, 0, mVertexBuffer);
        gl.glColorPointer(4, gl.GL_FIXED, 0, mColorBuffer);
        gl.glDrawElements(gl.GL_TRIANGLES, 36, gl.GL_UNSIGNED_BYTE, mIndexBuffer);
    } 
    
    
    private IntBuffer   mVertexBuffer;
    private IntBuffer   mColorBuffer;
    private ByteBuffer  mIndexBuffer;
}

// The pyramid is from the anddev.org tutorial.
class Pyramid { 
      
     private IntBuffer mVertexBuffer; 
     private IntBuffer mColorBuffer; 
     private ByteBuffer mIndexBuffer; 

     public Pyramid() { 
                     
          int one = 0x10000; 
          /* Every vertex got 3 values, for 
          * x / y / z position in the kartesian space. 
          */ 
          int vertices[] = { -one, -one, -one, // The four floor vertices of the pyramid 
               one, -one, -one, 
               one, one, -one, 
               -one, one, -one, 
               0, 0, one, };  // The top of the pyramid 

          /* Every vertex has got its own color, described by 4 values 
          * R(ed) 
          * G(green) 
          * B(blue) 
          * A(lpha) <-- Opticacy 
          */ 
          int colors[] = { 0, 0, one, one, 
               one, 0, 0, one, 
               one, one, 0, one, 
               0, one, 0, one, 
               one, 0, one, one, }; 

           /* The last thing is that we need to describe some Triangles. 
           * A triangle got 3 vertices. 
           * The confusing thing is, that it is important in which order 
           * the vertices of each triangle are described. 
           * So describing a triangle through the vertices: "0, 3, 4" 
           * will not result in the same triangle as: "0, 4, 3" 
           * You probably ask: Why the hell isn't that the same ??? 
           * The reason for that is the call of: "gl.glFrontFace(gl.GL_CW);" 
           * which means, that we have to describe the "visible" side of the 
           * triangles by naming its vertices in a ClockWise order! 
           * From the other side, the triangle will be 100% lookthru! 
           * You can create a kind of magic mirror with that . 
           */ 
          byte indices[] = { 0, 4, 1, // The four side-triangles 
               1, 4, 2, 
               2, 4, 3, 
               3, 4, 0, 
               1, 2, 0, // The two bottom-triangles 
               0, 2, 3}; 

          // Buffers to be passed to gl*Pointer() functions 
          // must be direct, i.e., they must be placed on the 
          // native heap where the garbage collector cannot 
          // move them. 
          // 
          // Buffers with multi-byte datatypes (e.g., short, int, float) 
          // must have their byte order set to native order 

          ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);  // * 4 becuase of int 
          vbb.order(ByteOrder.nativeOrder()); 
          mVertexBuffer = vbb.asIntBuffer(); 
          mVertexBuffer.put(vertices); 
          mVertexBuffer.position(0); 

          ByteBuffer cbb = ByteBuffer.allocateDirect(colors.length * 4); // * 4 becuase of int 
          cbb.order(ByteOrder.nativeOrder()); 
          mColorBuffer = cbb.asIntBuffer(); 
          mColorBuffer.put(colors); 
          mColorBuffer.position(0); 

          mIndexBuffer = ByteBuffer.allocateDirect(indices.length); 
          mIndexBuffer.put(indices); 
          mIndexBuffer.position(0); 
     } 

     public void draw(GL10 gl) { 
          gl.glFrontFace(gl.GL_CW); 
          gl.glVertexPointer(3, gl.GL_FIXED, 0, mVertexBuffer); 
          gl.glColorPointer(4, gl.GL_FIXED, 0, mColorBuffer); 
          gl.glDrawElements(gl.GL_TRIANGLES, 18, gl.GL_UNSIGNED_BYTE, mIndexBuffer); 
     } 

}