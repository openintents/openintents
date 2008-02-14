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
	}
}
