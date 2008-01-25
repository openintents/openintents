package org.openintents.hardware;

import java.text.DecimalFormat;

import org.openintents.R;
import org.openintents.provider.Hardware;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SensorSimulatorView extends Activity {
	/**
	 * TAG for logging.
	 */
	private static final String TAG = "Hardware";
	
	private static final int UPDATE_SENSOR_DATA = 1;
	
	private EditText mEditText;
	private EditText mEditTextIP;
	private EditText mEditTextSocket;
	
	private Button mButtonConnect;
	private Button mButtonDisconnect;
	
	private CheckBox mCheckBoxEnable;
	//private CheckBox mCheckBoxAutoUpdate;
	
	private int updateInterval;
	DecimalFormat mDecimalFormat;
	
	String[] mSupportedSensors;
    

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		
		setContentView(R.layout.sensorsimulator);
		Hardware.mContentResolver = getContentResolver();
		
		mEditText = (EditText) findViewById(R.id.edittext);
		mEditTextIP = (EditText) findViewById(R.id.ipaddress);
		mEditTextSocket = (EditText) findViewById(R.id.socket);
		
		mButtonConnect = (Button) findViewById(R.id.buttonconnect);
		mButtonConnect.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				connect();
			}
		});

		mButtonDisconnect = (Button) findViewById(R.id.buttondisconnect);
		mButtonDisconnect.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				disconnect();
			}
		});
		
		setButtonState();
		
		mCheckBoxEnable = (CheckBox) findViewById(R.id.checkenable);
		mCheckBoxEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// Enable all sensors:
				for (String sensor : mSupportedSensors) {
					if (isChecked) {
						Sensors.enableSensor(sensor);
					} else {
						Sensors.disableSensor(sensor);
					}
				};
				
				readAllSensors();
				
				Log.i(TAG, "start" + isChecked);
				if (isChecked) {
					// start the timer for automatic update:
					mHandler.sendMessageDelayed(mHandler.obtainMessage(UPDATE_SENSOR_DATA), updateInterval);
				}
			}
		});
		
		/*
		mCheckBoxAutoUpdate = (CheckBox) findViewById(R.id.checkautoupdate);
		// default is on:
		mCheckBoxAutoUpdate.setSelected(true);
		mCheckBoxAutoUpdate.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					readAllSensors();
					
					// Start timer again:
					mHandler.sendMessageDelayed(mHandler.obtainMessage(UPDATE_SENSOR_DATA), 50);
					
					// Check some exceptions here:
					//Sensors.disableSensor(Sensors.SENSOR_ACCELEROMETER);
					//Sensors.getNumSensorValues(Sensors.SENSOR_ACCELEROMETER);
					//Sensors.getNumSensorValues("foo");
				}
			}
		});
		*/
		
		mEditTextIP.setText(Hardware.getPreference(Hardware.IPADDRESS));
		String s = Hardware.getPreference(Hardware.SOCKET);
		if (s.contentEquals("")) {
			s = Hardware.DEFAULT_SOCKET;
		}
		mEditTextSocket.setText(s);
		
		// Format for output of data
		mDecimalFormat = new DecimalFormat("#0.00");
		
		// Get current sensors
		mSupportedSensors = Sensors.getSupportedSensors();
		
		// Default timer interval
		updateInterval = 100;
		
		
	}
	
	public void connect() {
		
		String newIP = mEditTextIP.getText().toString();
		String newSocket = mEditTextSocket.getText().toString();
		String oldIP = Hardware.getPreference(Hardware.IPADDRESS);
		String oldSocket = Hardware.getPreference(Hardware.SOCKET);
		
		if (! (newIP.contentEquals(oldIP) && newSocket.contentEquals(oldSocket)) ) {
			// new values
			Sensors.mClient.disconnect();
			
			// Save the values
			Hardware.setPreference(Hardware.IPADDRESS, newIP);
			Hardware.setPreference(Hardware.SOCKET, newSocket);
		}
		
		if (! Sensors.mClient.connected )
			Sensors.mClient.connect();
		
		// Get current sensors
		mSupportedSensors = Sensors.getSupportedSensors();
		
		readAllSensors();
		
		// Hardware.mContentResolver = getContentResolver();
		// Hardware.setPreference("remote IP", "1.2.3.4");
		
		setButtonState();
	}
	
	public void disconnect() {
		Sensors.mClient.disconnect();

		// Get current sensors
		mSupportedSensors = Sensors.getSupportedSensors();
		
		setButtonState();
	}
	
	public void setButtonState() {
		boolean connected = Sensors.mClient.connected;
		mButtonConnect.setEnabled(!connected);
		
		mButtonDisconnect.setEnabled(connected);
	}
	
	public void readAllSensors() {
		Log.i(TAG, "readAllSensors()");
		String data = "";
		if (Sensors.mClient.connected) {
			data += getString(R.string.sensor_simulator_data) + "\n";
		} else {
			data += getString(R.string.real_device_data) + "\n";
		}
		
		for (String sensor : mSupportedSensors) {
			data += sensor + ":";
		
			if (mCheckBoxEnable.isChecked()) {
				int num = Sensors.getNumSensorValues(sensor);
				float[] val = new float[num];
				Sensors.readSensor(sensor, val);
				for (int j=0; j<num; j++) {
					data += " " + mDecimalFormat.format(val[j]);
					if (j < num-1) data += ",";
				}
			} else {
				data += " " + getString(R.string.disabled);
			}
			data += "\n";
		}
		
		// only update field if user is not trying to do anything in the text field.
		if (!mEditText.isFocused()) {
			// update info.
			mEditText.setText(data);
		}
		
	}


    // Handle the process of searching for suitable present:
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_SENSOR_DATA) {
                readAllSensors();
            	
                if (mCheckBoxEnable.isChecked()) {
                	// Autoupdate
                	sendMessageDelayed(obtainMessage(UPDATE_SENSOR_DATA), updateInterval);
                }
            }
        }
    };

	
}
