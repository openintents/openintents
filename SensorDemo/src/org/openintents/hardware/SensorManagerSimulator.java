package org.openintents.hardware;

import android.content.Context;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Looper;
import android.text.style.SuperscriptSpan;
import android.widget.Toast;

public class SensorManagerSimulator {

	private static SensorManagerSimulator instance;
	
	/**
	 * TAG for logging.
	 */
	private static final String TAG = "SensorManagerSimulator";
	
	/**
	 * Client that communicates with the SensorSimulator application.
	 */
	public static SensorSimulatorClient mClient = new SensorSimulatorClient();
	
	private SensorManager mSensorManager = null;
	
	private SensorManagerSimulator(SensorManager systemsensormanager) {
		mSensorManager = systemsensormanager;
	}
	
	public static SensorManagerSimulator getSystemService(Context context, String sensorManager) {
		if (instance == null) {
			if (sensorManager.equals(Context.SENSOR_SERVICE)) {
				if (SensorManagerSimulator.isRealSensorsAvailable()) {
					instance = new SensorManagerSimulator((SensorManager)context.getSystemService(sensorManager));
				}
				else {
					instance = new SensorManagerSimulator(null);
					Toast.makeText(
							context, "Android SensorManager disabled, 1.5 SDK emulator crashes when using it... Make sure to connect SensorSimulator", Toast.LENGTH_LONG).show();	
				
				}
				
			}
		}
		return instance;
	}
	
	
	public int getSensors() {
		if (mClient.connected) {
			return mClient.getSensors();
		} else {
			if (mSensorManager != null) {
				return mSensorManager.getSensors();
			}
			return 0;
		}
	}
	
    // Method that checks for the 1.5 SDK Emulator bug...
	private static boolean isRealSensorsAvailable() {
		if (Build.VERSION.SDK.equals("3")) {
			// We are on 1.5 SDK
			if (Build.MODEL.contains("sdk")) {
				// We are on Emulator
				return false;
			}
		}
		return true;
	}

	public boolean registerListener(SensorListener listener, int sensors, int rate) {
		if (mClient.connected) {
			return mClient.registerListener(listener, sensors, rate);
		} else {
			if (mSensorManager == null) {
				return false;
			}
			return mSensorManager.registerListener(listener, sensors, rate);
		}
	}


	public boolean registerListener(SensorListener listener, int sensors) {
		if (mClient.connected) {
			return mClient.registerListener(listener, sensors);
		} else {
			if (mSensorManager == null) {
				return false;
			}
			return mSensorManager.registerListener(listener, sensors);
		}
	}


	public void unregisterListener(SensorListener listener, int sensors) {
		if (mClient.connected) {
			mClient.unregisterListener(listener, sensors);
		} else {
			if (mSensorManager == null) {
				mSensorManager.unregisterListener(listener, sensors);
			}
		}
	}


	public void unregisterListener(SensorListener listener) {
		if (mClient.connected) {
			mClient.unregisterListener(listener);
		} else {
			if (mSensorManager != null) {
				mSensorManager.unregisterListener(listener);
			}
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
