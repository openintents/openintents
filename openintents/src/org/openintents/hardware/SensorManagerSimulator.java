package org.openintents.hardware;

import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Looper;

public class SensorManagerSimulator extends SensorManager {


	/**
	 * TAG for logging.
	 */
	private static final String TAG = "SensorManagerSimulator";
	
	/**
	 * Client that communicates with the SensorSimulator application.
	 */
	public static SensorSimulatorClient mClient = new SensorSimulatorClient();
	
	private SensorManager mSensorManager = null;
	
	SensorManagerSimulator(SensorManager systemsensormanager) {
		super((Looper) null);
		mSensorManager = systemsensormanager;
	}
	
	@Override
	public int getSensors() {
		if (mClient.connected) {
			return mClient.getSensors();
		} else {
			return mSensorManager.getSensors();
		}
	}

	@Override
	public boolean registerListener(SensorListener listener, int sensors, int rate) {
		if (mClient.connected) {
			return mClient.registerListener(listener, sensors, rate);
		} else {
			return mSensorManager.registerListener(listener, sensors, rate);
		}
	}

	@Override
	public boolean registerListener(SensorListener listener, int sensors) {
		if (mClient.connected) {
			return mClient.registerListener(listener, sensors);
		} else {
			return mSensorManager.registerListener(listener, sensors);
		}
	}

	@Override
	public void unregisterListener(SensorListener listener, int sensors) {
		if (mClient.connected) {
			mClient.unregisterListener(listener, sensors);
		} else {
			mSensorManager.unregisterListener(listener, sensors);
		}
	}

	@Override
	public void unregisterListener(SensorListener listener) {
		if (mClient.connected) {
			mClient.unregisterListener(listener);
		} else {
			mSensorManager.unregisterListener(listener);
		}
	}

}
