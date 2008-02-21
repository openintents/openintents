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

import java.text.DecimalFormat;

import org.openintents.R;
import org.openintents.provider.Hardware;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SensorSimulatorView extends Activity implements OnFocusChangeListener {
	/**
	 * TAG for logging.
	 */
	private static final String TAG = "Hardware";

	/**
	 * Build the menu
	 */
	private static final int MENU_UPDATE_INTERVAL = Menu.FIRST;

	/** 
	 * Constant for message handling.
	 */
	private static final int UPDATE_SENSOR_DATA = 1;
	
	private EditText mEditText;
	private EditText mEditTextIP;
	private EditText mEditTextSocket;
	
	// Indicators whether real device or sensor simulator is connected.
	private TextView mTextSensorType;
	private ImageView mImageSensorType;
	
	private Button mButtonConnect;
	private Button mButtonDisconnect;
	
	private CheckBox mCheckBoxEnable;
	//private CheckBox mCheckBoxAutoUpdate;
	
	private LinearLayout mSettingsBackground;
	private LinearLayout mTestingBackground;
	
	private int mUpdateInterval;
	DecimalFormat mDecimalFormat;
	
	String[] mSupportedSensors;
    
	/**
	 * Dialog: setRefreshDelayDialog.
	 */
	private Dialog mDialog;
	
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
				
				enableAllSensors(isChecked);
				
				readAllSensors();
				
				Log.i(TAG, "start" + isChecked);
				if (isChecked) {
					// start the timer for automatic update:
					mHandler.sendMessageDelayed(mHandler.obtainMessage(UPDATE_SENSOR_DATA), mUpdateInterval);
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
		
		mTextSensorType = (TextView) findViewById(R.id.datatype);
		mImageSensorType = (ImageView) findViewById(R.id.imagetype);
		
		mSettingsBackground = (LinearLayout) findViewById(R.id.settings_background);
		mTestingBackground = (LinearLayout) findViewById(R.id.testing_background);
		
		// Format for output of data
		mDecimalFormat = new DecimalFormat("#0.00");
		
		// Get current sensors
		mSupportedSensors = Sensors.getSupportedSensors();
		
		// Default timer interval
		mUpdateInterval = 100;
		
		readAllSensors(); // initial reading
		
		// Register all possible focus changes:
		mEditText.setOnFocusChangeListener(this);
		mEditTextIP.setOnFocusChangeListener(this);
		mEditTextSocket.setOnFocusChangeListener(this);
		mButtonConnect.setOnFocusChangeListener(this);
		mButtonDisconnect.setOnFocusChangeListener(this);
		mCheckBoxEnable.setOnFocusChangeListener(this);
	}
	
	/**
	 * Implement the OnFocusChangeListener interface
	 */
	public void onFocusChanged(View v, boolean hasFocus) {
		int id = v.getId();
		if (id == R.id.ipaddress
				|| id == R.id.socket) {
			// Focus in settings area:
			mSettingsBackground.setBackground(R.drawable.border_orange_01b);
			mTestingBackground.setBackground(R.drawable.border_gray_01b);
		} else {
			// Focus in testing area:
			mSettingsBackground.setBackground(R.drawable.border_gray_01b);
			mTestingBackground.setBackground(R.drawable.border_orange_01b);
		}
	}
	
    ////////////////////////////////////////////////////////
    // The menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		// Standard menu
		menu.add(0, MENU_UPDATE_INTERVAL, R.string.set_update_interval)
			.setShortcut('0', 'r');

		return true;
	}
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
				
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(Item item) {
		switch (item.getId()) {
		case MENU_UPDATE_INTERVAL:
			setUpdateIntervalDialog();
			return true;	
		}
		return super.onOptionsItemSelected(item);		
	}

	///////////////////////////////////////
	// Menu related functions

	/**
	 * Opens a dialog to set the update interval.
	 */
	private void setUpdateIntervalDialog() {
		
		// TODO Shall we implement this as action?
		// Then other applications may call this as well.

		mDialog = new Dialog(SensorSimulatorView.this);
		
		mDialog.setContentView(R.layout.input_box);
		
		mDialog.setTitle(getString(R.string.ask_update_interval));
		
		EditText et = (EditText) mDialog.findViewById(R.id.edittext);
		et.setText("" + mUpdateInterval);
		et.selectAll();
		
		// Accept OK also when user hits "Enter"
		et.setOnKeyListener(new OnKeyListener() {
			
			public boolean onKey(final View v, final int keyCode, 
					final KeyEvent key) {
				//Log.i(TAG, "KeyCode: " + keyCode);
				EditText editText = (EditText) mDialog.findViewById(R.id.edittext);
				/*
				if (key.isDown()) {
					// undo red marking of wrong number (if existed)
					editText.setBackground(Color.WHITE);					
				}
				*/
				
				if (key.isDown() && keyCode == Integer
							.parseInt(getString(R.string.key_return))) {
					// User pressed "Enter" 
					int value;
					try {
						value = Integer.parseInt(editText.getText().toString());
					} catch (NumberFormatException e) {
						// wrong user input in box - take default values.
						//value = defaultValue;
						//editText.setBackground(Color.RED);
						//editText.setBackgroundColor(Color.RED);
						return true; // no dismiss
					}
					mUpdateInterval = value;
					mDialog.dismiss();
					return true;	
				}
				return false;
			}
			
		});
		
		
		Button bOk = (Button) mDialog.findViewById(R.id.ok);
		bOk.setOnClickListener(new OnClickListener() {
			public void onClick(final View v) {
				EditText editText = (EditText) mDialog.findViewById(R.id.edittext);
				int value;
				try {
					value = Integer.parseInt(editText.getText().toString());
				} catch (NumberFormatException e) {
					// wrong user input in box - take default values.
					//value = defaultValue;
					editText.setBackground(Color.RED);
					return; // no dismiss
				}
				mUpdateInterval = value;
					
				mDialog.dismiss();
			}
		});
		
		Button bCancel = (Button) mDialog.findViewById(R.id.cancel);
		bCancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				mDialog.cancel();
			}
		});
		
		mDialog.show();
	}

	///////////////////////////////////////
	
	public void connect() {
		
		String newIP = mEditTextIP.getText().toString();
		String newSocket = mEditTextSocket.getText().toString();
		String oldIP = Hardware.getPreference(Hardware.IPADDRESS);
		String oldSocket = Hardware.getPreference(Hardware.SOCKET);
		
		boolean disableEnable = false; // first disable, then enable
		if (mCheckBoxEnable.isChecked()) disableEnable = true;
		
		if (disableEnable) enableAllSensors(false);
		
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
		
		if (disableEnable) enableAllSensors(true);
		
		readAllSensors();
		
		// Hardware.mContentResolver = getContentResolver();
		// Hardware.setPreference("remote IP", "1.2.3.4");
		
		setButtonState();
	}
	
	public void disconnect() {
		boolean disableEnable = false; // first disable, then enable
		if (mCheckBoxEnable.isChecked()) disableEnable = true;
		
		if (disableEnable) enableAllSensors(false);
		
		Sensors.mClient.disconnect();

		if (disableEnable) enableAllSensors(true);
		
		// Get current sensors
		mSupportedSensors = Sensors.getSupportedSensors();
		
		readAllSensors();
		
		setButtonState();
	}
	
	public void setButtonState() {
		boolean connected = Sensors.mClient.connected;
		mButtonConnect.setEnabled(!connected);
		
		mButtonDisconnect.setEnabled(connected);
		
		mButtonConnect.invalidate();
		mButtonDisconnect.invalidate();
	}

    public void enableAllSensors(boolean newEnabledState) {
		// Enable all sensors:
		for (String sensor : mSupportedSensors) {
			if (newEnabledState) {
				Sensors.enableSensor(sensor);
			} else {
				Sensors.disableSensor(sensor);
			}
		};
	}

	public void readAllSensors() {
		Log.i(TAG, "readAllSensors()");
		String data = "";
		if (Sensors.mClient.connected) {
			//data += getString(R.string.sensor_simulator_data) + "\n";
			mTextSensorType.setText(R.string.sensor_simulator_data);
			mImageSensorType.setImageResource(R.drawable.sensorsimulator01b);
			
		} else {
			//data += getString(R.string.real_device_data) + "\n";
			mTextSensorType.setText(R.string.real_device_data);
			mImageSensorType.setImageResource(R.drawable.realdevice01a);
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
		
		/*  /// COMMENTED OUT: May cause confusion...
		// only update field if user is not trying to do anything in the text field.
		if (!mEditText.isFocused()) {
			// update info.
			mEditText.setText(data);
		}
		*/
		
		mEditText.setText(data);
		
	}

	/*/*
	 * Notification about corrupt table
	 *//*
	private void notifyCorruptTable() {
		AlertDialog.show(SensorSimulatorView.this, 
			getString(R.string.delete_list),
			getString(R.string.confirm_delete_list), 
			getString(R.string.ok),
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface di, int whichDialog) {
				}
			},
			getString(R.string.cancel),
			new DialogInterface.OnClickListener() {
				
				public void onClick(DialogInterface di, int whichDialog) {
				}
			},
			true, 
			new DialogInterface.OnCancelListener() {
				public void onCancel(DialogInterface di) {
				}				
			});
	}
	*/
	
	// Handle the process of automatically updating enabled sensors:
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == UPDATE_SENSOR_DATA) {
                readAllSensors();
            	
                if (mCheckBoxEnable.isChecked()) {
                	// Autoupdate
                	sendMessageDelayed(obtainMessage(UPDATE_SENSOR_DATA), mUpdateInterval);
                }
            }
        }
    };

	
}
