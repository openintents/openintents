package org.openintents.sensorcheck;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SensorCheck extends Activity {
	TextView mTextView;
	Button mButton;
	
	TextView mTextView1;
	TextView mTextView2;
	TextView mTextView3;

	int mStep = -1;
	
	String mSendText = "";

    private SensorManager mSensorManager;
    
    private float[] mAccelerometerValues;
    private float[] mMagneticFieldValues;
    private float[] mOrientationValues;
		
	String mSensorOrientationDescription = "No Orientation sensor";
	String mSensorAccelerometerDescription = "No Accelerometer sensor";
	String mSensorMagneticFieldDescription = "No Magnetic field sensor";
	
	
	String[] mInstruction = new String[] {
			"Hold your phone in landscape mode and point the camera towards North (as if you were taking a picture of something far away at the horizon)."
			+ " Keep the position and press the button below.",
			"Now point towards East (where the sun rises).",
			"Now towards South (where the sun is at noon).",
			"Finally towards West (where the sun sets).",
			"Now point towards the sky above you.",
			"And now below towards the ground.",
			"Thank you! Press the button to send the collected information to OpenIntents support.",
			"Thanks for participating."
	};
	

	String[] mButtonText = new String[] {
			"North",
			"East",
			"South",
			"West",
			"Up",
			"Down",
			"Send data",
			"Start again"
	};
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mTextView = (TextView) findViewById(R.id.Text01);
        mTextView1 = (TextView) findViewById(R.id.TextView01);
        mTextView2 = (TextView) findViewById(R.id.TextView02);
        mTextView3 = (TextView) findViewById(R.id.TextView03);
        
        mButton = (Button)findViewById(R.id.Button01);
        
        mButton.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				toNextStep();
			}
        	
        });
        

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        
        updateBuildInfo();
        
        toNextStep();
    }

	@Override
	protected void onResume() {
		super.onResume();
		
		// Register sensors

		int delay = SensorManager.SENSOR_DELAY_UI;
		List<Sensor> sensorlist;
		
		sensorlist = mSensorManager.getSensorList(
        		Sensor.TYPE_ORIENTATION);
        		//TYPE_ALL, TYPE_TEMPERATURE, TYPE_ORIENTATION
        for (Sensor sensor : sensorlist) {
        	if (mSensorOrientationDescription.startsWith("No")) {
        		mSensorOrientationDescription = "";
        	} else {
        		mSensorOrientationDescription += "\n";
        	}
        	mSensorOrientationDescription += "Sensor name: " + sensor.getName() + ", vendor: " + sensor.getVendor();
        }
        
        if (sensorlist.size() > 0) {
        	mSensorManager.registerListener(mOrientationListener, sensorlist.get(0), 
        			delay);
        }
        
		sensorlist = mSensorManager.getSensorList(
        		Sensor.TYPE_ACCELEROMETER);
        for (Sensor sensor : sensorlist) {
        	if (mSensorAccelerometerDescription.startsWith("No")) {
        		mSensorAccelerometerDescription = "";
        	} else {
        		mSensorAccelerometerDescription += "\n";
        	}
        	mSensorAccelerometerDescription += "Sensor name: " + sensor.getName() + ", vendor: " + sensor.getVendor();
        }
        
        if (sensorlist.size() > 0) {
        	mSensorManager.registerListener(mAccelerometerListener, sensorlist.get(0), 
        			delay);
        }
        
        sensorlist = mSensorManager.getSensorList(
        		Sensor.TYPE_MAGNETIC_FIELD);
        		//TYPE_ALL, TYPE_TEMPERATURE, TYPE_ORIENTATION
        
        for (Sensor sensor : sensorlist) {
        	if (mSensorMagneticFieldDescription.startsWith("No")) {
        		mSensorMagneticFieldDescription = "";
        	} else {
        		mSensorMagneticFieldDescription += "\n";
        	}
        	mSensorMagneticFieldDescription += "Sensor name: " + sensor.getName() + ", vendor: " + sensor.getVendor();
        }
        
        if (sensorlist.size() > 0) {
        	mSensorManager.registerListener(mMagneticFieldListener, sensorlist.get(0), 
        			delay);
        }
        
        updateSensorInfo();
	}
	
    @Override
	protected void onPause() {
		super.onPause();
		
		// Unregister sensors
		mSensorManager.unregisterListener(mOrientationListener);
		mSensorManager.unregisterListener(mAccelerometerListener);
		mSensorManager.unregisterListener(mMagneticFieldListener);
	}

	void toNextStep() {
    	mStep++;
    	if (mStep >= mInstruction.length) {
    		mStep = 0;
    	}
    	mTextView.setText(mInstruction[mStep]);
    	mButton.setText(mButtonText[mStep]);
    	
    	if (mStep == 1) {
    		mSendText = "Dear OpenIntents support team,\n\n";
    		mSendText += "Please find below the sensor data in various orientations ";
    		mSendText += "together with device-specific information.\n\n";
    		mSendText += "<insert additional comments>\n\n";
    		mSendText += "Best,\n<insert your name here>\n\n";
    		mSendText += "-----\nDevice information:\n";
    		mSendText += mTextView3.getText().toString();
    		mSendText += "\n-----\nSensor information:\n";
    		mSendText += mTextView2.getText().toString();
    		mSendText += "\n";
    	}
    	if (mStep >= 1 && mStep <= 6) {
    		mSendText += "-----\n";
    		mSendText += mButtonText[mStep-1];
    		mSendText += ":\n";
    		mSendText += mTextView1.getText().toString();
    		mSendText += "\n";
    	}
    	if (mStep == 7) {
    		// Send
    		mSendText += "-----\n";
    		Intent sendIntent = new Intent(Intent.ACTION_SEND);
    		//sendIntent.setData(Uri.parse("mailto:support@openintents.org"));
    		sendIntent.putExtra(Intent.EXTRA_TEXT, mSendText);
    		sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Sensor data for " + Build.MODEL);
    		sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[] {"support@openintents.org"});
    		sendIntent.setType("message/rfc822");
    		startActivity(Intent.createChooser(sendIntent, "Send message:"));
    	}
    }
    
    
    void updateBuildInfo() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("Model: ");
    	sb.append(Build.MODEL);
    	sb.append("\nBrand: ");
    	sb.append(Build.BRAND);
    	sb.append("\nProduct: ");
    	sb.append(Build.PRODUCT);
    	sb.append("\nDevice: ");
    	sb.append(Build.DEVICE);
    	sb.append("\nBoard: ");
    	sb.append(Build.BOARD);
    	sb.append("\nDisplay: ");
    	sb.append(Build.DISPLAY);
    	sb.append("\nID: ");
    	sb.append(Build.ID);
    	sb.append("\nVersion: ");
    	sb.append(Build.VERSION.RELEASE);
    	sb.append("\nVersion incremental: ");
    	sb.append(Build.VERSION.INCREMENTAL);
    	sb.append("\nSDK version: ");
    	sb.append(Build.VERSION.SDK);
    	sb.append("\nFingerprint: ");
    	sb.append(Build.FINGERPRINT);
    	//sb.append("\nHost: ");
    	//sb.append(Build.HOST);
    	//sb.append("\nTags: ");
    	//sb.append(Build.TAGS);
    	//sb.append("\nTime: ");
    	//sb.append(Build.TIME);
    	//sb.append("\nType: ");
    	//sb.append(Build.TYPE);
    	//sb.append("\nUser: ");
    	//sb.append(Build.USER);
    	mTextView3.setText(sb.toString());
    }
    
    void updateSensorReadout() {
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append("Orientation: ");
    	appendFloats(sb, mOrientationValues);
    	sb.append("\nAccelerometer: ");
    	appendFloats(sb, mAccelerometerValues);
    	sb.append("\nMagnetic field: ");
    	appendFloats(sb, mMagneticFieldValues);

    	mTextView1.setText(sb.toString());
    }

    void updateSensorInfo() {
    	StringBuilder sb = new StringBuilder();

    	sb.append(mSensorOrientationDescription);
    	sb.append("\n");
    	sb.append(mSensorAccelerometerDescription);
    	sb.append("\n");
    	sb.append(mSensorMagneticFieldDescription);
    	mTextView2.setText(sb.toString());
    }
    
    void appendFloats(StringBuilder sb, float[] values) {
    	if (values != null) {
	    	for (float val : values) {
	    		sb.append(val);
	    		sb.append(", ");
	    	}
	    	if (sb.length() >= 2) {
	    		sb.delete(sb.length() - 2, sb.length());
	    	}
    	} else {
    		sb.append("-");
    	}
    }

    SensorEventListener mOrientationListener = new SensorEventListener() {

//		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

//		@Override
		public void onSensorChanged(SensorEvent event) {
			mOrientationValues = event.values;
			
			updateSensorReadout();
		}
    };
    
    SensorEventListener mAccelerometerListener = new SensorEventListener() {

//		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

//		@Override
		public void onSensorChanged(SensorEvent event) {
			mAccelerometerValues = event.values;
			
			updateSensorReadout();
		}
    };

    SensorEventListener mMagneticFieldListener = new SensorEventListener() {

//		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
		
//		@Override
		public void onSensorChanged(SensorEvent event) {
			mMagneticFieldValues = event.values;
			
			updateSensorReadout();
		}
    };
 
}