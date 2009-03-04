package org.openintents.sensordemo;

import org.openintents.hardware.SensorManagerSimulator;
import org.openintents.provider.Hardware;

import android.app.Activity;
import android.content.Intent;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Toast;

public class SensorDemo extends Activity implements SensorListener {

	SensorManager sensorManager;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* start open intents code */

		Hardware.mContentResolver = getContentResolver();
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Hardware.Preferences.CONTENT_URI);
		startActivity(intent);
		sensorManager.unregisterListener(this);
		SensorManagerSimulator.connectSimulator();

		/* end open intents code */

		sensorManager.registerListener(this, SensorManager.SENSOR_ACCELEROMETER
				| SensorManager.SENSOR_MAGNETIC_FIELD
				| SensorManager.SENSOR_ORIENTATION,
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	protected void onResume() {
		super.onResume();
		sensorManager.registerListener(this, SensorManager.SENSOR_ACCELEROMETER
				| SensorManager.SENSOR_MAGNETIC_FIELD
				| SensorManager.SENSOR_ORIENTATION,
				SensorManager.SENSOR_DELAY_FASTEST);
	}

	@Override
	protected void onStop() {
		sensorManager.unregisterListener(this);
		super.onStop();
	}

	public void onAccuracyChanged(int sensor, int accuracy) {
	}

	public void onSensorChanged(int sensor, float[] values) {
		Toast.makeText(
				this,
				Float.toString(values[0]) + "::" + Float.toString(values[1])
						+ "::" + Float.toString(values[2]), Toast.LENGTH_SHORT)
				.show();
	}
}
