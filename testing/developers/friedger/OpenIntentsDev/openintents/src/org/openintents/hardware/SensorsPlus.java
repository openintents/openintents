package org.openintents.hardware;

/**
 *  SensorsPlus extends the Sensors class by useful functions.
 * 
 */
public class SensorsPlus extends Sensors {

	public SensorsPlus() {
		super();
		// TODO Auto-generated constructor stub
	}

	// General useful functions that may be used with Sensors:
	/**
	 *  Check whether a specific sensor is supported.
	 *  @param sensor Sensor to be probed.
	 *  @return Whether that sensor is supported.
	 */
	public static boolean isSupportedSensor(String sensor) {
		String[] sensors = Sensors.getSupportedSensors();
		for (String s : sensors) {
			if (s.contentEquals(sensor)) return true;
		};
		return false;
	}
	
	/**
	 * Check whether a specific sensor is enabled.
	 * This is done by trying to read out a value and catch the exception
	 * if the sensor is not enabled.
	 * WARNING: If the sensor readout influences the subsequent readout result
	 * (e.g. for a pedometer that returns the number of steps since the last 
	 *  readout), this function shall not be used.
	 * @param sensor Sensor to be probed.
	 * @return Whether that sensor is enabled.
	 */
	public static boolean isEnabledSensor(String sensor) {
		try {
			// we do this by reading out a value.
			int num = getNumSensorValues(sensor);
			float[] val = new float[num];
			readSensor(sensor, val);
		} catch (IllegalStateException e) {
			// IllegalStateException occurs if the sensor has not been enabled:
			return false;
		}
		
		// everything went fine, so the sensor must be enabled:
		return true;
	}
	
	// Regarding SensorSimulator
	/**
	 * Is the Sensor Simulator connected?
	 */
	public static boolean isConnectedSimulator() {
		return Sensors.mClient.connected;
	}

}
