package org.openintents.sensorgesturedetector.main;

import org.openintents.sensorgesturedetector.R;
import org.openintents.sensorgesturedetector.SensorEvent;
import org.openintents.sensorgesturedetector.SensorGestureDetector;
import org.openintents.sensorgesturedetector.SensorGestureDetector.OnSensorGestureListener;

import android.app.Activity;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SensorGestureDetectorActivity extends Activity
	implements SensorListener, OnSensorGestureListener {
	
	SensorManager mSensorManager;
	SensorGestureDetector mSensorGestureDetector;
	
	TextView mText;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mText = (TextView) findViewById(R.id.text); 
        
        Button b = (Button) findViewById(R.id.clear);
        
        b.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				mText.setText("");
			}
        	
        });
        
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        
        mSensorGestureDetector = new SensorGestureDetector(this);
    }

	@Override
	protected void onResume() {
		super.onResume();

        mSensorManager.registerListener(this, 
                SensorManager.SENSOR_ACCELEROMETER | 
                SensorManager.SENSOR_MAGNETIC_FIELD | 
                SensorManager.SENSOR_ORIENTATION,
                SensorManager.SENSOR_DELAY_FASTEST);
	}

    @Override
    protected void onStop() {
        mSensorManager.unregisterListener(this);
        super.onStop();
    }
    
    //////////////////////////////////
    // SensorListener
    
	@Override
	public void onSensorChanged(int sensor, float[] values) {
		mSensorGestureDetector.onSensorChanged(sensor, values);
	}
    
	@Override
	public void onAccuracyChanged(int sensor, int accuracy) {
		mSensorGestureDetector.onAccuracyChanged(sensor, accuracy);
	}
	
	//////////////////////////////////
    // OnSensorGestureListener
    
	@Override
	public boolean onShake(SensorEvent event) {
		
		mText.append("\nShake " + event.getValueLength());
		
		return false;
	}
    

	@Override
	public boolean onDrop(SensorEvent event) {
		
		mText.append("\nDrop" + event.getValueLength());
		
		return false;
	}
	
}