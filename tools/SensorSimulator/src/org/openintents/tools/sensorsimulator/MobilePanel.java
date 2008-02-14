/* 
 * Copyright (C) 2008 OpenIntents.org
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

package org.openintents.tools.sensorsimulator;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JSlider;

class MobilePanel extends JPanel {
	
	SensorSimulator mSensorSimulator;

	// accelerometer
	double accelx;
	double accely;
	double accelz;
	
	// compass
	double compassx;
	double compassy;
	double compassz;
	
	// orientation (in degree)
	double yaw;
	double pitch;
	double roll;
	
	// thermometer
	double temperature;
	
	// orientation sensor raw data (in degree)
	double yawDegree;
	double pitchDegree;
	double rollDegree;
	
	int movex;
	int movez;
	
	int oldx;
	int oldz;
	double vx; // velocity
	double vz;
	double oldvx;
	double oldvz;
	double ax; // acceleration
	double az;
	
	double dt; // time step size
	double meterperpixel;
	double g;
	
	int mousedownx;
	int mousedowny;
	int mousedownyaw;
	int mousedownpitch;
	int mousedownroll;
	int mousedownmovex;
	int mousedownmovez;
	
	JSlider yawSlider;
	JSlider pitchSlider;
	JSlider rollSlider;
	
	Random r;
	
	
	/*
	 * http://code.google.com/android/reference/android/hardware/Sensors.html
	 * 
	 * With the device lying flat on a horizontal surface in front of the user, 
	 * oriented so the screen is readable by the user in the normal fashion, 
	 * the X axis goes from left to right, the Y axis goes from the user 
	 * toward the device, and the Z axis goes upwards perpendicular to the 
	 * surface. 
	 */
	// Mobile size
	final double sx = 15; // size x
	final double sy = 40; // size y
	final double sz = 5; // size z
	
	// Display size
	final double dx = 12; // size x
	final double dy1 = 33; // size y
	final double dy2 = -15;
	
	double[][] phone = {
			// bottom shape
			{ sx, sy, -sz}, {-sx, sy, -sz},
			{-sx, sy, -sz}, {-sx,-sy, -sz},
			{-sx,-sy, -sz}, { sx,-sy, -sz},
			{ sx,-sy, -sz}, { sx, sy, -sz},
			// top shape
			{ sx, sy, sz}, {-sx, sy, sz},
			{-sx, sy, sz}, {-sx,-sy, sz},
			{-sx,-sy, sz}, { sx,-sy, sz},
			{ sx,-sy, sz}, { sx, sy, sz},
			// connectint top and bottom
			{ sx, sy, -sz}, { sx, sy, sz},
			{-sx, sy, -sz}, {-sx, sy, sz},
			{-sx,-sy, -sz}, {-sx,-sy, sz},
			{ sx,-sy, -sz}, { sx,-sy, sz},
			// display
			{ dx, dy1, sz}, {-dx, dy1, sz},
			{-dx, dy1, sz}, {-dx, dy2, sz},
			{-dx, dy2, sz}, { dx, dy2, sz},
			{ dx, dy2, sz}, { dx, dy1, sz},
		};
	
	public MobilePanel(SensorSimulator newSensorSimulator) {
		mSensorSimulator = newSensorSimulator;
		
		// setBorder(BorderFactory.createLineBorder(Color.black));
	    
		yawDegree = 0;
		pitchDegree = 0;
		rollDegree = 0;
		
		movex = 0;
		movez = 0;
		oldx = 0;
		oldz = 0;
		oldvx = 0;
		oldvz = 0;
		
		dt = 0.1;
		meterperpixel = 1/3000.; // meter per pixel
		g = 9.8; // meter per second^2
		
		r = new Random();
		
		//this.setDoubleBuffered(true);
		
	    addMouseListener(new MouseAdapter() {
	        public void mousePressed(MouseEvent e) {
	            //moveSquare(e.getX(),e.getY());
	        	mousedownx = e.getX();
	        	mousedowny = e.getY();
	        	mousedownyaw = yawSlider.getValue();
	        	mousedownpitch = pitchSlider.getValue();
	        	mousedownroll = rollSlider.getValue();
	        	mousedownmovex = movex;
	        	mousedownmovez = movez;
	        }
	    });
	
	    addMouseMotionListener(new MouseAdapter() {
	        public void mouseDragged(MouseEvent e) {
	            //moveSquare(e.getX(),e.getY());
	        	if (mSensorSimulator.mouseMode 
	        			== SensorSimulator.mouseYawPitch) {
		        	// Control yawDegree
		        	int newyaw = mousedownyaw - (e.getX() - mousedownx);
		        	while (newyaw > 180) newyaw -= 360;
		        	while (newyaw < -180) newyaw += 360;
		        	yawSlider.setValue((int) newyaw);
		        	yawDegree = newyaw;
		        	
		        	// Control pitch
		        	int newpitch = mousedownpitch + (e.getY() - mousedowny);
		        	while (newpitch > 180) newpitch -= 360;
		        	while (newpitch < -180) newpitch += 360;
		        	pitchSlider.setValue((int) newpitch);
		        	pitchDegree = newpitch;
	        	} else if (mSensorSimulator.mouseMode 
	        			== SensorSimulator.mouseRollPitch) {
		        	// Control roll
		        	int newroll = mousedownroll + (e.getX() - mousedownx);
		        	while (newroll > 180) newroll -= 360;
		        	while (newroll < -180) newroll += 360;
		        	rollSlider.setValue((int) newroll);
		        	rollDegree = newroll;
		        	
		        	// Control pitch
		        	int newpitch = mousedownpitch + (e.getY() - mousedowny);
		        	while (newpitch > 180) newpitch -= 360;
		        	while (newpitch < -180) newpitch += 360;
		        	pitchSlider.setValue((int) newpitch);
		        	pitchDegree = newpitch;
	        	} else if (mSensorSimulator.mouseMode 
	        			== SensorSimulator.mouseMove) {
		        	// Control roll
		        	int newmovex = mousedownmovex + (e.getX() - mousedownx);
		        	movex = newmovex;
		        	
		        	// Control pitch
		        	int newmovez = mousedownmovez - (e.getY() - mousedowny);
		        	movez = newmovez;
	        	}
	        	
	        	repaint();
	        }
	    });
	}
	
	public void updateMouseAcceleration() {
		Vector vec;
		double random;

		// Update the timer if necessary:
		double newdelay;
		newdelay = mSensorSimulator.getSafeDouble(mSensorSimulator.mUpdateText);
		if (newdelay > 0) {
			mSensorSimulator.delay = (int) newdelay;
			mSensorSimulator.timer.setDelay(mSensorSimulator.delay);
		}
		
		dt = 0.001 * mSensorSimulator.delay; // from ms to s
		g = mSensorSimulator.getSafeDouble(mSensorSimulator.mGravityConstantText, 9.82);
		meterperpixel = 1 / mSensorSimulator.getSafeDouble(mSensorSimulator.mPixelPerMeterText, 3000);
		
		// Calculate velocity induced by mouse:
		double f = meterperpixel / g;
		vx = f * ((double) (movex - oldx)) / dt;
		vz = f * ((double) (movez - oldz)) / dt;
		
		// Calculate acceleration induced by mouse:
		ax = (vx - oldvx) / dt;
		az = (vz - oldvz) / dt;
		
		
		// Old values:
		oldx = movex;
		oldz = movez;
		oldvx = vx;
		oldvz = vz;
		
		// Calculate acceleration by gravity:
		double gravityax;
		double gravityay;
		double gravityaz;
		
		gravityax = mSensorSimulator.getSafeDouble(mSensorSimulator.mGravityXText);
		gravityay = mSensorSimulator.getSafeDouble(mSensorSimulator.mGravityYText);
		gravityaz = mSensorSimulator.getSafeDouble(mSensorSimulator.mGravityZText);
		
		
		////
		// Now calculate this into mobile phone acceleration:
		// ! Mobile phone's acceleration is just opposite to 
		// lab frame acceleration !
		vec = new Vector(-ax + gravityax, gravityay, -az + gravityaz);
		
		// we reverse roll, pitch, and yawDegree,
		// as this is how the mobile phone sees the coordinate system.
		vec.reverserollpitchyaw(rollDegree, pitchDegree, yawDegree);
		
		if (mSensorSimulator.mEnabledAccelerometer.isSelected()) {
			accelx = vec.x;
			accely = vec.y;
			accelz = vec.z;
			
			// Add random component:
			random = mSensorSimulator.getSafeDouble(mSensorSimulator.mRandomAccelerometerText);
			if (random > 0) {
				accelx += getRandom(random);
				accely += getRandom(random);
				accelz += getRandom(random);
			}
			
			// Add accelerometer limit:
			double limit = mSensorSimulator.getSafeDouble(mSensorSimulator.mAccelerometerLimitText);
			if (limit > 0) {
				// limit on each component separately, as each is
				// a separate sensor.
				if (accelx > limit) accelx = limit;
				if (accelx < -limit) accelx = -limit;
				if (accely > limit) accely = limit;
				if (accely < -limit) accely = -limit;
				if (accelz > limit) accelz = limit;
				if (accelz < -limit) accelz = -limit;
			}
		} else {
			accelx = 0;
			accely = 0;
			accelz = 0;
		}
		
		// Calculate magnetic field:
		// Calculate acceleration by gravity:
		double magneticnorth;
		double magneticeast;
		double magneticvertical;
		
		if (mSensorSimulator.mEnabledCompass.isSelected()) {
			magneticnorth = mSensorSimulator.getSafeDouble(mSensorSimulator.mMagneticFieldNorthText);
			magneticeast = mSensorSimulator.getSafeDouble(mSensorSimulator.mMagneticFieldEastText);
			magneticvertical = mSensorSimulator.getSafeDouble(mSensorSimulator.mMagneticFieldVerticalText);

			// Add random component:
			random = mSensorSimulator.getSafeDouble(mSensorSimulator.mRandomCompassText);
			if (random > 0) {
				magneticnorth += getRandom(random);
				magneticeast += getRandom(random);
				magneticvertical += getRandom(random);
			}
			
			// Magnetic vector in phone coordinates:
			vec = new Vector(magneticeast, magneticnorth, -magneticvertical);
			vec.scale(0.001); // convert from nT (nano-Tesla) to µT (micro-Tesla)
			
			// we reverse roll, pitch, and yawDegree,
			// as this is how the mobile phone sees the coordinate system.
			vec.reverserollpitchyaw(rollDegree, pitchDegree, yawDegree);
			
			compassx = vec.x;
			compassy = vec.y;
			compassz = vec.z;
		} else {
			compassx = 0;
			compassy = 0;
			compassz = 0;
		}
		
		// Orientation is currently not affected:
		if (mSensorSimulator.mEnabledOrientation.isSelected()) {
			//yaw = Math.toRadians(yawDegree);
			//pitch = Math.toRadians(pitchDegree);
			//roll = Math.toRadians(rollDegree);
			// Since OpenGL uses degree as input,
			// and it seems also more user-friendly,
			// let us stick to degree.
			// (it seems, professional sensors also use
			//  degree output.)
			yaw = yawDegree;
			pitch = pitchDegree;
			roll = rollDegree;
			
			// Add random component:
			random = mSensorSimulator.getSafeDouble(mSensorSimulator.mRandomOrientationText);
			if (random > 0) {
				yaw += getRandom(random);
				pitch += getRandom(random);
				roll += getRandom(random);
			}
		} else {
			yaw = 0;
			pitch = 0;
			roll = 0;
		}
		
		// Thermometer
		if (mSensorSimulator.mEnabledThermometer.isSelected()) {
			temperature = mSensorSimulator.getSafeDouble(mSensorSimulator.mTemperatureText);
		
			// Add random component:
			random = mSensorSimulator.getSafeDouble(mSensorSimulator.mRandomTemperatureText);
			if (random > 0) {
				temperature += getRandom(random);
			}
		} else {
			temperature = 0;
		}
	}
	
	/** 
	 * get a random number in the range -random to +random
	 * 
	 * @param random range of random number
	 * @return random number
	 */
	public double getRandom(double random) {
		double val;
		val = r.nextDouble();
		return (2*val - 1) * random;
	}
	
    public Dimension getPreferredSize() {
        return new Dimension(250,200);
    }
    
    /**
     *  yaws a vector (rotate around z-axis)
     */
    
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);       
        // g.drawString("This is my custom Panel!",(int)yawDegree,(int)pitch);
        
        Graphics2D g2 = (Graphics2D) g;
        // draw Line2D.Double
        
        double centerx = 100;
        double centery = 100;
        double centerz = -150;
        for (int i=0; i<phone.length; i+=2) {
        	if (i==0) g2.setColor(Color.RED);
        	if (i==24) g2.setColor(Color.BLUE);
        	
        	Vector v1 = new Vector(phone[i]);
        	Vector v2 = new Vector(phone[i+1]);
        	v1.rollpitchyaw(rollDegree, pitchDegree, yawDegree);
        	v2.rollpitchyaw(rollDegree, pitchDegree, yawDegree);
            g2.draw(new Line2D.Double(
            		centerx + (v1.x + movex) * centerz / (centerz - v1.y), 
            		centery - (v1.z + movez) * centerz / (centerz - v1.y), 
            		centerx + (v2.x + movex) * centerz / (centerz - v2.y), 
            		centery - (v2.z + movez) * centerz / (centerz - v2.y)));
        }
        
    }  

}