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
import android.content.Context;
import android.content.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewInflate;
import android.view.Menu.Item;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TabHost.TabSpec;

/**
 * Lets user set connection settings to the SensorSimulator and test them.
 * 
 * The connection is outbound to the SensorSimulator.
 * 
 * @author Peli
 *
 */
public class SensorSimulatorView extends Activity {
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
	
	//private ImageView mImageSensorType;
	
	
	private Button mButtonConnect;
	private Button mButtonDisconnect;
	
	//private CheckBox mCheckBoxEnable;
	//private CheckBox mCheckBoxAutoUpdate;
	
	private ListView mSensorsList;
	private SensorListAdapter mSensorListAdapter;
	
	private LinearLayout mSettingsBackground;
	private LinearLayout mTestingBackground;
	
	private int mUpdateInterval;
	DecimalFormat mDecimalFormat;
	
	String[] mSupportedSensors;
	String[][] mSensorUpdateRates;
	
	/**
	 * Whether we currently automatically update sensors.
	 */
	boolean mUpdatingSensors;
	
	/**
	 * Number of supported sensors.
	 */
	int mNumSensors;
	
	/**
	 * The list of currently enabled sensors.
	 * (Needed to determine which of the sensors
	 *  have to be updated regularly by new sensor data).
	 */
	boolean[] mSensorEnabled;
	
	/**
	 * Current sensor update duration (inverse of rate).
	 * (required for determining when enabled
	 *  sensors have to be updated).
	 */
	float[] mSensorUpdateDuration;
	
	/**
	 * Next sensor update time for that sensor.
	 * (compared to SystemClock.uptimeMillis().)
	 */
	long [] mNextSensorUpdate;
	
	/**
	 * Keep pointers to SingleSensorView
	 * so that we can update sensor data regularly.
	 */
	SingleSensorView[] mSingleSensorView;
	
	/**
	 * If this is set true, then
	 * SensorData should be read out again.
	 */
	boolean[] mInvalidateSensorData;
	
    
	/**
	 * Dialog: setRefreshDelayDialog.
	 */
	private Dialog mDialog;
	
	private TabHost mTabHost;
    
	/**
	 * Called when activity starts.
	 * 
	 * This can either be the first time, or the user navigates back
	 * after the activity has been killed.
	 * 
	 * We do not automatically reconnect to the SensorSimulator,
	 * as it may not be available in the mean-time anymore or 
	 * the IP address may have changed.
	 * 
	 * (We do not know how long the activity had been dormant).
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		
		setContentView(R.layout.sensorsimulator);
		Hardware.mContentResolver = getContentResolver();
		
		Context context = this;
        // Get the Resources object from our context
        Resources res = context.getResources();
    
		mTabHost = (TabHost)findViewById(R.id.tabhost);
		mTabHost.setup();
		
		TabSpec tabspec = mTabHost.newTabSpec("settings");
		tabspec.setIndicator(res.getString(R.string.settings), res.getDrawable(R.drawable.settings001a_32));
		tabspec.setContent(R.id.content1);
		mTabHost.addTab(tabspec);
		
		tabspec = mTabHost.newTabSpec("testing");
		tabspec.setIndicator(res.getString(R.string.testing), res.getDrawable(R.drawable.mobile_shake001a_32));
		tabspec.setContent(R.id.content2);
		mTabHost.addTab(tabspec);
		
		mTabHost.setCurrentTab(0);
	
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
		
		/*
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
		*/
		
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
		/*
		mImageSensorType = (ImageView) findViewById(R.id.imagetype);
		*/
		
		/*
		mSettingsBackground = (LinearLayout) findViewById(R.id.settings_background);
		mTestingBackground = (LinearLayout) findViewById(R.id.testing_background);
		*/
		
		
		// Format for output of data
		mDecimalFormat = new DecimalFormat("#0.00");
		
		
		readAllSensors(); // Basic sensor information
		
		readAllSensorsUpdate(); // initial reading
		
		mSensorsList = (ListView) findViewById(R.id.sensordatalist);
		mSensorListAdapter = new SensorListAdapter(this);
		mSensorsList.setAdapter(mSensorListAdapter);
		
		// Default timer interval
		mUpdateInterval = 100;
		
		/*
		// Register all possible focus changes:
		mEditText.setOnFocusChangeListener(this);
		mEditTextIP.setOnFocusChangeListener(this);
		mEditTextSocket.setOnFocusChangeListener(this);
		mButtonConnect.setOnFocusChangeListener(this);
		mButtonDisconnect.setOnFocusChangeListener(this);
		mCheckBoxEnable.setOnFocusChangeListener(this);
		*/
	}
	
	/**
	 * Called when activity comes to foreground.
	 */
    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * Called when another activity is started.
     */
    @Override
    protected void onFreeze(Bundle outState) {
        super.onFreeze(outState);
    }

	/**
	 * Called when the user leaves.
	 * Here we store the IP address and port.
	 */
    @Override
    protected void onPause() {
        super.onPause();

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
	}

	
	/**
	 * Implement the OnFocusChangeListener interface
	 */
	/*
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
	*/
	
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
				
				// TODO: Upgrade deprecated function.
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
		
		//boolean disableEnable = false; // first disable, then enable
		// if (mCheckBoxEnable.isChecked()) disableEnable = true;
		
		//if (disableEnable) enableAllSensors(false);
		//disableAllSensors();
		
		if (! (newIP.contentEquals(oldIP) && newSocket.contentEquals(oldSocket)) ) {
			// new values
			Sensors.mClient.disconnect();
			
			// Save the values
			Hardware.setPreference(Hardware.IPADDRESS, newIP);
			Hardware.setPreference(Hardware.SOCKET, newSocket);
		}
		
		if (! Sensors.mClient.connected )
			Sensors.mClient.connect();
		
		readAllSensors();
		
		//if (disableEnable) 
		//enableAllSensors();
		
		readAllSensorsUpdate();
		
		// Hardware.mContentResolver = getContentResolver();
		// Hardware.setPreference("remote IP", "1.2.3.4");
		
		setButtonState();
		
 		if (Sensors.mClient.connected) {
			mTextSensorType.setText(R.string.sensor_simulator_data);
		} else {
			mTextSensorType.setText(R.string.real_device_data);
		}

 		
 		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
 		// TODO Is this an Android bug?? notifyDataSetChanged()
 		// call results in performance loss later.
 		// 
 		// I've asked about this here:
 		// http://groups.google.com/group/android-developers/browse_frm/thread/ad4a386116f2e915
 		//
 		// Keeping the line below works, but has really 
 		// slow performance, because for each small text change
 		// the whole row is recreated.
 		//
		// Now notify the ListAdapter of the changes:
		mSensorListAdapter.notifyDataSetChanged();
		//mSensorListAdapter.notifyDataSetInvalidated();
		// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		
	}
	
	public void disconnect() {
		boolean disableEnable = false; // first disable, then enable
		// if (mCheckBoxEnable.isChecked()) disableEnable = true;
		
		//if (disableEnable) 
		//enableAllSensors();
		
		Sensors.mClient.disconnect();

		//if (disableEnable)
		//disableAllSensors();
		
		readAllSensors();
		
		readAllSensorsUpdate();
		
		setButtonState();
		
 		if (Sensors.mClient.connected) {
			mTextSensorType.setText(R.string.sensor_simulator_data);
		} else {
			mTextSensorType.setText(R.string.real_device_data);
		}

 		/*
		// Now notify the ListAdapter of the changes:
		mSensorListAdapter.notifyDataSetChanged();
		//mSensorListAdapter.notifyDataSetInvalidated();
		 */
	}
	
	public void setButtonState() {
		boolean connected = Sensors.mClient.connected;
		mButtonConnect.setEnabled(!connected);
		
		mButtonDisconnect.setEnabled(connected);
		
		mButtonConnect.invalidate();
		mButtonDisconnect.invalidate();
	}
/*
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
    */
	
    public void enableAllSensors() {
		// Enable all sensors:
		for (String sensor : mSupportedSensors) {
			Sensors.enableSensor(sensor);
		};
	}
    
    public void disableAllSensors() {
		// Enable all sensors:
		for (String sensor : mSupportedSensors) {
			Sensors.disableSensor(sensor);
		};
	}
    
    /**
     * Reads information about which sensors are supported and what their rates and current rates are.
     */
	public void readAllSensors() {
		// Get current sensors
		mSupportedSensors = Sensors.getSupportedSensors();
		
		// Now obtain the sensor rates:
		int max = mSupportedSensors.length;
		mSensorUpdateRates = new String[max][];
		for (int i=0; i<max; i++) {
			String sensor = mSupportedSensors[i];
			float[] rate = Sensors.getSensorUpdateRates(sensor);
			
			if (rate == null) {
				mSensorUpdateRates[i] = new String[1];
				mSensorUpdateRates[i][0] = "N/A";  // TODO: from resource
			} else {
				// Convert the floats to strings:
				int maxj = rate.length;
				mSensorUpdateRates[i] = new String[maxj];
				for (int j=0; j<maxj; j++) {
					mSensorUpdateRates[i][j] = "" + rate[j];
				}
			}
		}
		
		// Now set values that are related to sensor updates:
		mNumSensors = mSupportedSensors.length;
		
		// Set all fields:
		// (Hmmm.. probably this could all be packed into a single class...)
		// (Maybe the SingleSensorView defined below?!?)
		mSensorEnabled = new boolean[mNumSensors];
		mSensorUpdateDuration = new float[mNumSensors];
		mNextSensorUpdate = new long[mNumSensors];
		mSingleSensorView = new SingleSensorView[mNumSensors];
		mInvalidateSensorData = new boolean[mNumSensors];
		
		// Some default values:
		for (int i=0; i < mNumSensors; i++) {
			mSensorEnabled[i] = false;
			mSensorUpdateDuration[i] = 0;
			mNextSensorUpdate[i] = 0;
			mSingleSensorView[i] = null;
			mInvalidateSensorData[i] = true;
		}
		
	}
	

	public String getSensorValuesString(String sensor, boolean isEnabled) {
		String data;
		if (isEnabled) {
			data = "";
			int num = Sensors.getNumSensorValues(sensor);
			float[] val = new float[num];
			Sensors.readSensor(sensor, val);
			for (int j=0; j<num; j++) {
				data += mDecimalFormat.format(val[j]);
				if (j < num-1) data += ", ";
			}
			//Log.i(TAG, "mTextView: " + mTextView.getText());
		} else {
			// Sensor disabled:
			// Mark it in the text:
			data = getString(R.string.disabled);
		}
		return data;
	}
	
	/**
	 * Updates the data read from the sensors.
	 */
	public void readAllSensorsUpdate() {
		Log.i(TAG, "readAllSensors()");
		String data = "";
/*
 		if (Sensors.mClient.connected) {
			//data += getString(R.string.sensor_simulator_data) + "\n";
			mTextSensorType.setText(R.string.sensor_simulator_data);
			//mImageSensorType.setImageResource(R.drawable.sensorsimulator01b);
			
		} else {
			//data += getString(R.string.real_device_data) + "\n";
			mTextSensorType.setText(R.string.real_device_data);
			//mImageSensorType.setImageResource(R.drawable.realdevice01a);
		}
*/
		/*
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
		*/
		
		/*  /// COMMENTED OUT: May cause confusion...
		// only update field if user is not trying to do anything in the text field.
		if (!mEditText.isFocused()) {
			// update info.
			mEditText.setText(data);
		}
		*/
		/*
		mEditText.setText(data);
		*/
		
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
            	// Let us go through all sensors,
            	// and only read out the data if it is time for
            	// that particular sensor.
            	
            	long current = SystemClock.uptimeMillis();
                
            	for (int i=0; i < mNumSensors; i++) {
            		if (mSensorEnabled[i] 
            		    && (current >= mNextSensorUpdate[i])) {
            			// do the update:
            			
            			// We first look for the correct child
            			//SingleSensorView ssv = (SingleSensorView) mSensorsList.findViewById(i);
            			//SingleSensorView ssv = 
            		//		(SingleSensorView) mSensorsList.findViewWithTag(mSupportedSensors[i]);
            			SingleSensorView ssv = 
            				(SingleSensorView) mSensorsList.getChildAt(i);
            			
            			if (ssv != null) {
            				// in the child we update the text view
                			TextView tv = (TextView) ssv.findViewById(R.id.sensordata);
                			if (tv != null) {
                				//tv.setText("OK" + current);
                				ssv.updateSensorValues();
                			}
            			}
            			
            			
            			if (mSingleSensorView[i] != null) {
            				//mSingleSensorView[i].updateSensorValues();
            				// Force a redraw of sensor data:
            				//mSensorListAdapter
            				//mSensorListAdapter.
            				//mSensorsList.invalidate();
            				//mSensorListAdapter.notifyDataSetChanged();
            				

                        	
            			}
            			
            			// Update all:
            			mInvalidateSensorData[i] = true;
        				
                    	//mSensorsList.invalidateViews();
            			
            			// Set next update time:
            			mNextSensorUpdate[i] += mSensorUpdateDuration[i];
            			//Log.i(TAG, "mSensorUpdateDuration[" + i + "] = " + mSensorUpdateDuration[i]);
            			
            			// Now in case we are already behind the schedule,
            			// so the following update as soon as possible
            			// (but don't drag behind schedule forever -
            			//  i.e. skip the updates that are already past.)
            			if (mNextSensorUpdate[i] < current) mNextSensorUpdate[i] = current;
            		}
            	}
            	
				
                // readAllSensorsUpdate();
            	
                if (mUpdatingSensors) {
                	// Autoupdate
                	sendMessageDelayed(obtainMessage(UPDATE_SENSOR_DATA), mUpdateInterval);
                }
            }
        }
    };

    /**
     * Adapter for displaying information about single sensors.
     * 
     */
    private class SensorListAdapter extends BaseAdapter {

        /**
         * Remember our context so we can use it when constructing views.
         */
        private Context mContext;
        
        public SensorListAdapter(Context context) {
            mContext = context;
        }

        public int getCount() {
        	Log.i(TAG, "SensorListAdapater - getCount()");
            return mSupportedSensors.length;
        }

        public Object getItem(int position) {
        	Log.i(TAG, "SensorListAdapater - getItem()");
            return position;
        }

        /**
         * We use the array index as a unique id.
         */
        public long getItemId(int position) {
        	Log.i(TAG, "SensorListAdapater - getItemId()");
            return position;
        }

        /**
         * 
         */
        public View getView(int position, View convertView, ViewGroup parent) {
        	Log.i(TAG, "SensorListAdapater - getView(" + position + ")");
        	SingleSensorView sv;
            if (convertView == null) {
                sv = new SingleSensorView(mContext, mSupportedSensors[position], mSensorUpdateRates[position], position);
            } else {
                sv = (SingleSensorView) convertView;
                sv.setSensor(mSupportedSensors[position],  mSensorUpdateRates[position], position);               
            }

            return sv;
        }
        
        
		@Override
		public void notifyDataSetChanged() {
			// TODO Auto-generated method stub
			Log.i(TAG,"notifyDataSetChanged called");
			super.notifyDataSetChanged();
			Log.i(TAG,"notifyDataSetChanged super called");
			/*
			// We can not call super as this will trigger
			// a strange mode.
			// We will update everything by hand:

			for (int i=0; i<mNumSensors; i++) {
				SingleSensorView ssv = 
					(SingleSensorView) mSensorsList.getChildAt(i);
				
				getView(i, ssv, mSensorsList);
				
				//if (ssv != null) {
					
					
					// in the child we update the views
	    			//TextView tv = (TextView) ssv.findViewById(R.id.sensordata);
	    			//if (tv != null) {
	    				//tv.setText("OK" + current);
	    			//	ssv.updateSensorValues();
	    			//}
	    			
				// }
			}
			*/
		}
		
    }
    
    /**
     * Layout for displaying single sensor.
     */
    private class SingleSensorView extends LinearLayout {

        private TextView mTitle;
        
        LinearLayout mL1;
        LinearLayout mL1a;
        LinearLayout mL1b;
        LinearLayout mL1c;
        
        CheckBox  mCheckBox;
        TextView mTextView;
        Spinner mSpinner;
        
        ArrayAdapter<String> mUpdateRateAdapter;
        
        Context mContext;
        
        int mSensorId;
        String mSensor;
        String[] mUpdateRates;
        
        /**
         * Index of the default value in the list (spinner) for the 
         * sensor update rate.
         * (-1 for no default index).
         */
        int mDefaultValueIndex;
		
    	public SingleSensorView(Context context, String sensor, String[] updateRates, int sensorId) {
    		super(context);
    		Log.i(TAG, "SingleSensorView - constructor");
    		
    		mContext = context;
    		mSensorId = sensorId;
    		mSensor = sensor;
    		mUpdateRates = updateRates;
    		mDefaultValueIndex = -1;  // -1 means there is no default index.
    		
    		// Build child view from resource:
    		ViewInflate inf = 
    			(ViewInflate)getSystemService(INFLATE_SERVICE); 
    		View rowView = inf.inflate(R.layout.sensorsimulator_row, null, null); 
    		//my_view.find
    		
    		// We set a tag, so that Handler can find this view
    		// rowView.setId(sensorId);
    		rowView.setTag(mSensor);
    		
    		// Assign widgets
    		mCheckBox = (CheckBox) rowView.findViewById(R.id.enabled);
    		mCheckBox.setText(sensor);
    		
    		mTextView = (TextView) rowView.findViewById(R.id.sensordata);
    		mTextView.setText(sensor);
    		
    		mSpinner = (Spinner) rowView.findViewById(R.id.updaterate);
    		mUpdateRateAdapter = new ArrayAdapter<String>(
                    context, android.R.layout.simple_spinner_item, updateRates);
            mUpdateRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinner.setAdapter(mUpdateRateAdapter);
            	        
    		addView(rowView, new LinearLayout.LayoutParams(
        			LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
    		
    		updateSensorStateInformation();   

    		updateSensorValues();
    	}
    	
    	public void setSensor(String sensor, String[] updateRates, int sensorId) {
    		Log.i(TAG, "SingleSensorView - setSensor(" + sensor + ", " + updateRates + ")");

    		if ((!mSensor.equals(sensor)) || mSensorId != sensorId) {
    			Log.i(TAG, "SensorListAdapter.setSensor() - update general information");
    			// Need to update general information:
	    		mSensorId = sensorId;
	    		mSensor = sensor;
	
	    		mCheckBox.setText(sensor);
	            mCheckBox.setChecked(SensorsPlus.isEnabledSensor(sensor));
    		}
    		
    		if (!mUpdateRates.equals(updateRates)) {
    			Log.i(TAG, "SensorListAdapter.setSensor() - update updateRates");
    			// Need to update updateRates:
	    		mUpdateRates = updateRates;
	
	    		/*
	            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
	                    context, android.R.layout.simple_spinner_item, updateRates);
	            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	            mSpinner.setAdapter(adapter);
	            */
	            
	           	mUpdateRateAdapter = new ArrayAdapter<String>(
	                    mContext, android.R.layout.simple_spinner_item, updateRates);
	            mUpdateRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	            mSpinner.setAdapter(mUpdateRateAdapter);
	
	             
	            /*
	            int max=updateRates.length;
	            for (int i=0; i<max; i++) {
	            	
	            	// TODO: How to adjust the adapter??
	            	// The following is brute force: Replace by a new adapter.
	            }
	            */
	            
	            updateSensorStateInformation();
    		}
    		
    		// The following should be done periodically anyway.
    		if (mInvalidateSensorData[mSensorId]) {
    			updateSensorValues();

    			// Data are now up-to-date:
    			mInvalidateSensorData[mSensorId] = false;
    		}
        }
    	
    	public void updateSensorStateInformation() {
    		boolean sensorEnabled = SensorsPlus.isEnabledSensor(mSensor);
    		mCheckBox.setChecked(sensorEnabled);
    		
    		if (sensorEnabled) {
    			// Refresh: start with no default value.
    			mDefaultValueIndex = -1;
    			
    			// We can get the default value and set this in the list:
    			float defaultValue =
    				SensorsPlus.getDefaultSensorUpdateRate(mSensor);
    			int max = mUpdateRates.length;
    			for (int i = 0; i < max; i++) {
    				if (mUpdateRates[i].equals("" + defaultValue)) {
    					// Now, this is the default value:
    					// Mark it:
    					mUpdateRates[i] += " (default)";
    					mDefaultValueIndex = i;
    					// Note that it can not be marked twice, even
    					// if updateSensorStateInformation() is called
    					// twice, because the string changes.
    				}
    			}
    			
    			// Now we mark the currently selected Sensor:
    			float currentValue =
    				SensorsPlus.getSensorUpdateRate(mSensor);
    			
    			for (int i = 0; i < max; i++) {
    				if (mUpdateRates[i].equals("" + currentValue)) {
    					// Now, this is the current value:
    					// Select it:
    					mSpinner.setSelection(i);
    				}
    			}
    			
    			// We have to treat the default value differently,
    			// as its string may have been changed above:
    			if ((currentValue == defaultValue) && 
    					(mDefaultValueIndex >= 0)) {
    				assert(mDefaultValueIndex >= 0);
    				mSpinner.setSelection(mDefaultValueIndex);
    			}
    			
    		}
    		
    		// Now we set a new onSelectListener:
    		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

				/**
				 * Select a new update rate for a sensor.
				 * @see android.widget.AdapterView$OnItemSelectedListener#onItemSelected(android.widget.AdapterView, android.view.View, int, long)
				 */
    			@Override
				public void onItemSelected(AdapterView parent, View v,
						int position, long id) {
    				if (id == mDefaultValueIndex) {
    					// The user selected the default value, so let's get
    					// that by unsetting:
    					Sensors.unsetSensorUpdateRate(mSensor);
    					setSensorUpdateDuration();
    				} else {
    					// User selected non-default value:
    					
        				// get again the float values
        				// (because currently only string values are stored).
        				// (I'm sure all this can be handled more efficiently
        				//  and cleanly...)
	    				float[] rate = Sensors.getSensorUpdateRates(mSensor);
	    				if (rate != null) {
	    					if (id >= rate.length) {
		    					/// uh uh, this should not happen.
		    					Log.e(TAG, "Inconsistent list behavior (id >= rate.length) (" + id + " >= " + rate.length + ")");
		    					Log.e(TAG, "in SensorSimulatorView - updateSensorStateInformation - onItemSelected");
		    					
		    					// We could refresh the list at this point,
		    					// but for now we simply do nothing...
		    				} else {
		    					assert(id < rate.length);
		    					
		    					Sensors.setSensorUpdateRate(mSensor, rate[(int) id]);
		    					setSensorUpdateDuration();
		    				}
	    				} else {
	    					// Nothing to be set as updateRates are not supported.
	    				}
    				}
    				
				}

				@Override
				public void onNothingSelected(AdapterView arg0) {
					// We don't have to do anything.
				}
    			
    		});
    		
    		// And we add a listener for the CheckBox:
    		mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
    			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    				
    				if (isChecked) {
    					Sensors.enableSensor(mSensor);
    				} else {
    					Sensors.disableSensor(mSensor);
    				}
    				
    				mSensorEnabled[mSensorId] = isChecked;

    				updateSensorValues();
    				
    				if (isChecked) {
    					// get the current Sensor update duration:
    					setSensorUpdateDuration();
    					
    					// start the timer for automatic update:
    					// (first update can be right now)
    					mNextSensorUpdate[mSensorId] = SystemClock.uptimeMillis();
    					
    					if (!mUpdatingSensors) {
    						Log.i(TAG,"Starting timer");
    						mUpdatingSensors = true;
    						mHandler.sendMessageDelayed(mHandler.obtainMessage(UPDATE_SENSOR_DATA), mUpdateInterval);
    					}
    				} else {
    					// If all sensors are turned off, we can also deactivate updating sensors:
    					// (Creating new local variable testUpdate to avoid possibly 
    					//  synchronization problems with Handler - although it should not happen
    					//  as we run currently only in a single thread).
    					boolean testUpdate = false;
    					for (int i = 0; i < mNumSensors; i++) {
    						if (mSensorEnabled[i]) {
    							testUpdate = true;
    							break;
    						}
    					}
    					mUpdatingSensors = testUpdate;
    					if (! mUpdatingSensors) Log.i(TAG,"Stopping timer");
    				}
    			
    			}    			
    		});
    		
    		// Now set information related to automatic sensor updates:
    		//mSingleSensorView[mSensorId] = SingleSensorView.this;
    		
    		
    	}

    	public void updateSensorValues() {
    		// Update the values for the sensor text:
			mTextView.setText(getSensorValuesString(mSensor, mCheckBox.isChecked()));
    	}
    	
    	/**
    	 * Determines the duration ( = 1000 ms /rate) to be used
    	 * in automatic data updates.
    	 */
    	void setSensorUpdateDuration() {
			// get the current Sensor rate:
    		float rate;
    		try {
    			rate = Sensors.getSensorUpdateRate(mSensor);
    		} catch (IllegalStateException e) {
    			// Sensor not enabled: just return 0.
    			rate = 0;
    		}
			if (rate > 0) {
				mSensorUpdateDuration[mSensorId] = 1000 / rate;
			} else
			{
				// No default update rate given:
				// we go for minimum rate
				mSensorUpdateDuration[mSensorId] = 0;
			}
			
    	}
    }
	
}
