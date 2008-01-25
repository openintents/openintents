package org.openintents.tools.sensorsimulator;


class Vector {
	public double x;
	public double y;
	public double z;
	
	public Vector() {
		x = 0;
		y = 0;
		z = 0;
	}
	public Vector(Vector v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}
	public Vector(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	public Vector(double[] vec) {
		x = vec[0];
		y = vec[1];
		z = vec[2];
	}
	
	/**
	 * Scale the vector by a factor.
	 * 
	 * @param factor Common factor.
	 */
	public void scale(double factor) {
		x = factor * x;
		y = factor * y;
		z = factor * z;
	}
	
	/**
	 * Yaw the vector (rotate around z-axis)
	 * 
	 * @param yaw yaw in Degree.
	 */
	public void yaw(double yaw) {
    	Vector v = new Vector(this); // temporary vector
    	double yawRad = Math.toRadians(yaw);
    	double cos = Math.cos(yawRad);
    	double sin = Math.sin(yawRad);
    	x = cos * v.x + sin * v.y;
    	y = -sin * v.x + cos * v.y;
    	z = v.z;
    }
	
	public void pitch(double pitch) {
    	Vector v = new Vector(this); // temporary vector
    	double pitchRad = -Math.toRadians(pitch); // negative sign!
    	double cos = Math.cos(pitchRad);
    	double sin = Math.sin(pitchRad);
    	x = v.x;
    	y = cos * v.y + sin * v.z;
    	z = -sin * v.y + cos * v.z;
    }
	
	public void roll(double roll) {
    	Vector v = new Vector(this); // temporary vector
    	double rollRad = Math.toRadians(roll);
    	double cos = Math.cos(rollRad);
    	double sin = Math.sin(rollRad);
    	x = cos * v.x + sin * v.z;
    	y = v.y;
    	z = -sin * v.x + cos * v.z;
    }
	
	public void rollpitchyaw(double roll, double pitch, double yaw) {
		roll(roll);
        pitch(pitch);
        yaw(yaw);
	}
	
	public void reverserollpitchyaw(double roll, double pitch, double yaw) {
		yaw(-yaw);
		pitch(-pitch);
		roll(-roll);
	}
}