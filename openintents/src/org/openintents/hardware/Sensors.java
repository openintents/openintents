package org.openintents.hardware;

public class Sensors extends android.hardware.Sensors {

	/**
	 * TAG for logging.
	 */
	private static final String TAG = "Hardware";
	
	public static final String SENSOR_ACCELEROMETER 
		= android.hardware.Sensors.SENSOR_ACCELEROMETER;
	public static final String SENSOR_COMPASS 
		= android.hardware.Sensors.SENSOR_COMPASS;
	public static final String SENSOR_ORIENTATION 
		= android.hardware.Sensors.SENSOR_ORIENTATION;
	
	// Extensions
	public static SensorSimulatorClient mClient = new SensorSimulatorClient();
	
	public Sensors() {
		super();
	}

	public static void disableSensor(String sensor) {
		if (mClient.connected) {
			mClient.disableSensor(sensor);
		} else {
			android.hardware.Sensors.disableSensor(sensor);
		}
	}

	public static void enableSensor(String sensor) {
		if (mClient.connected) {
			mClient.enableSensor(sensor);
		} else {
			android.hardware.Sensors.enableSensor(sensor);
		}
	}

	public static int getNumSensorValues(String sensor) {
		if (mClient.connected) {
			return mClient.getNumSensorValues(sensor);
		} else {
			return android.hardware.Sensors.getNumSensorValues(sensor);
		}
	}

	public static String[] getSupportedSensors() {
		if (mClient.connected) {
			return mClient.getSupportedSensors();
		} else {
			return android.hardware.Sensors.getSupportedSensors();
		}
	}

	public static void readSensor(String sensor, float[] sensorValues) {
		if (mClient.connected) {
			mClient.readSensor(sensor, sensorValues);
		} else {
			android.hardware.Sensors.readSensor(sensor, sensorValues);
		}
				
	}
	
	//  Member function extensions:
	/**
	 * Connect to the Sensor Simulator.
	 * (All the settings should have been set already.)
	 */
	public static void connectSimulator() {
		mClient.connect();
	};
	
	/**
	 * Disconnect from the Sensor Simulator.
	 */
	public static void disconnectSimulator() {
		mClient.disconnect();
	};
	
}
