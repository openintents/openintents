package org.openintents.samples.openglsensors;


import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Holds the Open GL surface.
 * 
 * This class also contains the GLThread class 
 * that takes care of graphics updates.
 *
 */
public class GLSurfaceViewNoThread extends SurfaceView implements SurfaceHolder.Callback
{
	/**
	 * TAG for logging.
	 */
	private static final String TAG = "GLSurfaceViewNoThread";
	
	SurfaceHolder mHolder;
	//private GLStatic mGLStatic;
	
	boolean surfaceCreated;
	
	/*
    private OpenGLContext   mGLContext;
    */
    private Cube            mCube;
    private Pyramid         mPyramid;
    private float           mAngle;
    private long            mNextTime;
    private boolean         mAnimate;
    
	private OpenGLSensors mOpenGLAccelerator; // needed to access sensor settings
	
    /**
     * The View constructor is a good place to allocate our OpenGL context
     */
    public GLSurfaceViewNoThread(Context context, OpenGLSensors newOpenGLAccelerator)
    {
        super(context);
        surfaceCreated = false;
        //init();
        mOpenGLAccelerator = newOpenGLAccelerator;
        
    }
    
    public void init() {
        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed 
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, start our drawing thread.
    	/*
        mGLThread = new GLThread();
        
        // Increase interactivity of menu
        mGLThread.setPriority(Thread.MIN_PRIORITY);
        
        mGLThread.start();
        */
    	
    	// We don't do threads, we do update by external handler.
    	// But it is ok to permit these updates:
    	surfaceCreated = true;
    	

    	initAnimation();
    	prepareAnimation();
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return
    	/*
        mGLThread.requestExitAndWait();
        mGLThread = null;
        */
    	surfaceCreated = false;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
    	/*
        // Surface size or format has changed. This should not happen in this
        // example.
        mGLThread.onWindowResize(w, h);
        */
    	 mWidth = w;
         mHeight = h;
    }
    
    /**
     * Holds animation temporarily.
     * 
     * This can be called onPause() to save CPU power consumption.
     */
    public void holdAnimation() {
    	
    }
    
    /**
     * Resumes animation.
     * 
     * Only to be called after holdAnimation().
     */
    public void resumeAnimation() {
    	
    }
    
 // ----------------------------------------------------------------------

        //private boolean mDone;
        private boolean mSizeChanged = true;
        private int mWidth;
        private int mHeight;
        
        float mYaw = 0;
        float mPitch = 0;
        float mRoll = 0;
        
        float mCompassYaw = 0;
        float mCompassPitch = 0;
        float mCompassRoll = 0;
        
        private boolean mUseSensors;
        
        
        
        void initAnimation() {
            //mDone = false;
            mWidth = 0;
            mHeight = 0;
            mCube = new Cube();
            mPyramid = new Pyramid();
            mUseSensors = true;
            mSizeChanged = true;
           
        }
    
        //GL10 mGl;
        EGL10 egl;
        EGLDisplay dpy;
        EGLSurface surface;
        EGLConfig config;
        EGLContext context;
        GL10 gl;
        
        public void prepareAnimation() {
        	/*
             * Get an EGL instance
             */
            egl = (EGL10)EGLContext.getEGL();
            
            /*
             * Get to the default display.
             */
            dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

            /*
             * We can now initialize EGL for that display
             */
            int[] version = new int[2];
            egl.eglInitialize(dpy, version);

            /*
             * Specify a configuration for our opengl session
             * and grab the first configuration that matches is
             */
            int[] configSpec = {
                    EGL10.EGL_RED_SIZE,      5,
                    EGL10.EGL_GREEN_SIZE,    6,
                    EGL10.EGL_BLUE_SIZE,     5,
                    EGL10.EGL_DEPTH_SIZE,   16,
                    EGL10.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] num_config = new int[1];
            egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config);
            config = configs[0];


            /* 
             * Create an OpenGL ES context. This must be done only once, an
             * OpenGL context is a somewhat heavy object.
             */
            context = egl.eglCreateContext(dpy, config,
                    EGL10.EGL_NO_CONTEXT, null);
            
            surface = null;
            gl = null;

        }
        
        /**
         * 
         * @return true if something changed.
         */
        boolean doAnimation() {
            
        		if (!surfaceCreated) {
        			// Not yet ready, but check back.
        			return true;
        		}
        		
                // Update the asynchronous state (window size, key events)
                int w, h;
                boolean changed;
                changed = mSizeChanged;
                w = mWidth;
                h = mHeight;
                mSizeChanged = false;

                if (changed) {
                    recreateSurface(w, h);
                }
                
                /* draw a frame here */
                drawFrame(gl);

                /*
                 * Once we're done with GL, we need to call swapBuffers()
                 * to instruct the system to display the rendered frame
                 */
                egl.eglSwapBuffers(dpy, surface);

                /*
                 * Always check for EGL_CONTEXT_LOST, which means the context
                 * and all associated data were lost (For instance because
                 * the device went to sleep). We need to quit immediately.
                 */
                if (egl.eglGetError() == EGL11.EGL_CONTEXT_LOST) {
                    // we lost the gpu, quit immediately
                    Context c = getContext();
                    if (c instanceof Activity) {
                        ((Activity)c).finish();
                    }
                }

                
                //Log.i(TAG, "doAnimation()" + mRoll + " , " + mYaw + " , " + mPitch);
            	// If any of the sensors is not == 0 then we continue to update.
                //return mRoll != -180.f || mYaw != 0 || mPitch != 0;
                
                // No need to check back if successfully drawn once.
                // (OnSensorChange updates the view anyway).
                return false;
        }

		/**
		 * @param w
		 * @param h
		 */
		private void recreateSurface(int w, int h) {
			/*
			 *  The window size has changed, so we need to create a new
			 *  surface.
			 */
			if (surface != null) {
			    
			    /*
			     * Unbind and destroy the old EGL surface, if
			     * there is one.
			     */
			    egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE,
			            EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT);
			    egl.eglDestroySurface(dpy, surface);
			}
			
			/* 
			 * Create an EGL surface we can render into.
			 */
			surface = egl.eglCreateWindowSurface(dpy, config, mHolder,
			        null);
      
			/*
			 * Before we can issue GL commands, we need to make sure 
			 * the context is current and bound to a surface.
			 */
			egl.eglMakeCurrent(dpy, surface, surface, context);
			
			/*
			 * Get to the appropriate GL interface.
			 * This is simply done by casting the GL context to either
			 * GL10 or GL11.
			 */
			gl = (GL10)context.getGL();
               
			/*
			 * By default, OpenGL enables features that improve quality
			 * but reduce performance. One might want to tweak that
			 * especially on software renderer.
			 */
			gl.glDisable(GL10.GL_DITHER);
      
			/*
			 * Some one-time OpenGL initialization can be made here
			 * probably based on features of this particular context
			 */
			 gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
			         GL10.GL_FASTEST);
      
			 gl.glClearColor(1,1,1,1);
			 gl.glEnable(GL10.GL_CULL_FACE);
			 gl.glShadeModel(GL10.GL_SMOOTH);
			 gl.glEnable(GL10.GL_DEPTH_TEST);
 
			 gl.glViewport(0, 0, w, h);

			 /*
			  * Set our projection matrix. This doesn't have to be done
			  * each time we draw, but usually a new projection needs to
			  * be set when the viewport is resized.
			  */

			 float ratio = (float)w / h;
			 gl.glMatrixMode(GL10.GL_PROJECTION);
			 gl.glLoadIdentity();
			 gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);
		}
        
        void stopAnimation() {
            /*
             * clean-up everything...
             */
            egl.eglMakeCurrent(dpy, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE,
                    EGL10.EGL_NO_CONTEXT);
            egl.eglDestroySurface(dpy, surface);
            egl.eglDestroyContext(dpy, context);
            egl.eglTerminate(dpy);
        }
        
        private void drawFrame(GL10 gl) {
        	/*
             * Usually, the first thing one might want to do is to clear
             * the screen. The most efficient way of doing this is to use
             * glClear().
             */

            gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

            /*
             * Now we're ready to draw some 3D object
             */

            gl.glMatrixMode(GL10.GL_MODELVIEW);
            gl.glLoadIdentity();
            
            gl.glTranslatef(0, 0, -4.0f);
            //gl.glScalef(0.5f, 0.5f, 0.5f);
            
            gl.glColor4f(0.7f, 0.7f, 0.7f, 1.0f);
            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
            gl.glEnable(GL10.GL_CULL_FACE);
            gl.glEnable(GL10.GL_DEPTH_TEST);

            /*
            synchronized(this) {
            	if (mUseSensors)
            	{
            		readSensors();
            	}
            }
            */
            
			// Now perform the rotation:
			gl.glRotatef((int) mRoll, 0, 1, 0);
			gl.glRotatef((int) mPitch, -1, 0, 0);
			gl.glRotatef((int) mYaw, 0, 0, -1);

			if (!(mOpenGLAccelerator.mUseAccelerometer
					|| mOpenGLAccelerator.mUseOrientation
					|| mOpenGLAccelerator.mUseCompass)) {
				// No sensor used - give at least some graphical feedback:
				mCube.draw(gl);
			}
            
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
                gl.glRotatef((int) -mYaw, 0, 0, -1);
                gl.glRotatef((int) -mPitch, -1, 0, 0);
    			gl.glRotatef((int) -mRoll, 0, 1, 0);
    			
                // Now we perform the compass rotations:
                gl.glRotatef((int) mCompassRoll, 0, 1, 0); // should be 0
                gl.glRotatef((int) mCompassPitch, 1, 0, 0);
                gl.glRotatef((int) mCompassYaw, 0, 0, -1);
                
                gl.glScalef(0.1f, 0.25f, 0.1f);
                
	            // Draw the compass
	            gl.glTranslatef(0, 1f, 0);
	            mCube.drawColor(gl, 1,0,0);
	            gl.glTranslatef(0, -2f, 0);
	            mCube.drawColor(gl, 1,1,1);
            }

        }

        public void stopUseSensors() {
        		mUseSensors = false;
        }
        
        public void startUseSensors() {
        	synchronized (this) {
        		mUseSensors = true;
        	}
        }
        /*
		public void readSensors() {
			if (mOpenGLAccelerator.mUseOrientation) {
            	// Using orientation sensor definitely is the 
            	// easiest way to rotate things:
            	
            	// First get the sensor values
	            //int num = Sensors.getNumSensorValues(Sensors.SENSOR_ORIENTATION);
	            final int num = 3; // we known that there are 3 values.
				float[] val = new float[num];
				try {
//TODO					Sensors.readSensor(Sensors.SENSOR_ORIENTATION, val);
            	} catch (IllegalStateException e) {
					// Currently not enabled:
					val[0] = 0;
					val[1] = 0;
					val[2] = 0;
				}
				// Assign mYaw, mPitch, and mRoll.
				// Negative angles are used, because we have to
				// "undo" the rotations that brought the mobile phone
				// into its current position.
				mYaw = -val[0];
				mPitch = -val[1];
				mRoll = -val[2];
            }
            
            if (mOpenGLAccelerator.mUseAccelerometer) {
            	// We can only let the pyramid point up,
            	// but we can not obtain information about the mYaw.
            	
            	// (strictly speaking, we can only adjust two of the three
            	//  variables (mYaw, mPitch, and mRoll). Since the
            	//  standard orientation is to point down (-z), we
            	//  choose to calculate mPitch and mRoll as the 
            	//  deviation from that standard position.)
            	
            	// First get the sensor values
	            //int num = Sensors.getNumSensorValues(Sensors.SENSOR_ACCELEROMETER);
	            final int num = 3; // we known that there are 3 values.
				float[] val = new float[num];
				try {
//TODO					Sensors.readSensor(Sensors.SENSOR_ACCELEROMETER, val);
				} catch (IllegalStateException e) {
					// Currently not enabled:
					val[0] = 0;
					val[1] = 0;
					val[2] = 0;
				}
				
				// we can only adjust mPitch and mRoll:
				double r = Math.sqrt(val[0]*val[0] + val[2]*val[2]);
				mYaw = 0;
				mPitch = (float) - Math.toDegrees(Math.atan2(-val[1], r));
				mRoll = (float) - Math.toDegrees(Math.atan2(val[0], -val[2]));	
            }
            
            if (mOpenGLAccelerator.mUseCompass) {
            	// We can only adjust the compass to point
            	// along the magnetic field, but we can not
            	// say where "up" is.
            	// Since the expected standard orientation is
            	// to point north (that is in +y direction),
            	// we use the information to adjust 
            	// mCompassYaw and mCompassPitch,
            	// but we don't know mCompassRoll.
            	
            	// First get the sensor values
	            //int num = Sensors.getNumSensorValues(Sensors.SENSOR_COMPASS);
	            final int num = 3; // we known that there are 3 values.
				float[] val = new float[num];
				try {
//TODO					Sensors.readSensor(Sensors.SENSOR_COMPASS, val);
	            } catch (IllegalStateException e) {
					// Currently not enabled:
					val[0] = 0;
					val[1] = 0;
					val[2] = 0;
				}	
				// we can only adjust mYaw and mPitch:
				//double r = Math.sqrt(val[0]*val[0] + val[1]*val[1]);
				//mCompassPitch = (float) - Math.toDegrees(Math.atan2(-val[2], r));
				//mCompassYaw = (float) - Math.toDegrees(Math.atan2(-val[0], val[1]));	
				double r = Math.sqrt(val[1]*val[1] + val[2]*val[2]);
				mCompassYaw = (float) - Math.toDegrees(Math.atan2(-val[0], r));	
	            mCompassPitch = (float) - Math.toDegrees(Math.atan2(-val[2], val[1]));
			}
            
            if (mOpenGLAccelerator.mUseAccelerometer 
            		&& mOpenGLAccelerator.mUseCompass) {
				// If we use both sensors, we could use the 
            	// compass orientation to fix the mYaw of the accelerometer
            	// information.
            	
            	// TODO Fix accelerometer mYaw using compass direction.
            }
		}
		*/
    
}

