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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import org.openintents.provider.Hardware;

import android.util.Log;

public class SensorSimulatorClient {
	
	/**
	 * TAG for logging.
	 */
	private static final String TAG = "Hardware";
	private static final String TAG2 = "Hardware2";
	
	public boolean connected;
	
	Socket mSocket;
    PrintWriter mOut;
    BufferedReader mIn;
	
	public SensorSimulatorClient() {
		connected = false;
	}
	
	public void connect() {
		
		// if (connected) return;

        mSocket = null;
        mOut = null;
        mIn = null;

        Log.i(TAG, "Starting connection...");
        
        // get Info from ContentProvider
        
        String ipaddress = Hardware.getPreference(Hardware.IPADDRESS);
        String socket = Hardware.getPreference(Hardware.SOCKET);
        
        try {
            mSocket = new Socket(ipaddress, Integer.parseInt(socket));
        	/*
        	 * !!!!! Socket with timeout does not work due to bug in Android SDK:
        	 * http://groups.google.com/group/android-developers/browse_thread/thread/bd9a4057713d9f50/c76645b31078445a
            
            // socket with timeout:
        	mSocket = new Socket();
        	SocketAddress sockaddr = new InetSocketAddress(ipaddress, Integer.parseInt(socket));
            
        	int timeoutMs = 3000; // 3 seconds
        	mSocket.connect(sockaddr, timeoutMs);
        	*/
        	
            mOut = new PrintWriter(mSocket.getOutputStream(), true);
            mIn = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
        } catch (UnknownHostException e) {
            //System.err.println("Don't know about host: ");
            //System.exit(1);
        	Log.e(TAG, "Don't know about host: " + ipaddress + " : " + socket);
        	return;
        } catch (SocketTimeoutException e) {
        	Log.e(TAG, "Connection time out: " + ipaddress + " : " + socket);
        	return;
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: x.x.x.x.");
            System.exit(1);
        }

        Log.i(TAG, "Read line...");
        
        String fromServer = "";
        try {
        	fromServer = mIn.readLine();
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: x.x.x.x.");
            System.exit(1);
		}
		Log.i(TAG, "Received: " + fromServer);
        
		if (fromServer.equals("SensorSimulator")) {
			// OK
			connected = true;
			Log.i(TAG, "Connected");
		} else {
			// Who is that???
			Log.i(TAG, "Problem connecting: Wrong string sent.");
			disconnect();
		}
               
	}
	
	public void disconnect() {
		if (connected) {
			Log.i(TAG, "Disconnect()");
	        
			try {
				mOut.close();
		        mIn.close();
		        mSocket.close();
			} catch (IOException e) {
				System.err.println("Couldn't get I/O for the connection to: x.x.x.x.");
	            System.exit(1);
			}
			
			connected = false;
		} else {
			// aldready disconnected. Nothing to do.
		}
	}
	
	public void disableSensor(String sensor) {
		Log.i(TAG2, "disableSensor()");
		mOut.println("disableSensor()");
		Log.i(TAG2, "Send: " + sensor);
        mOut.println(sensor);
        
		try {
			String answer = mIn.readLine();
			if (answer.compareTo("throw IllegalArgumentException") == 0) {
				throw new IllegalArgumentException(
						"Sensor '" + sensor
						+ "' is currently not supported.");
			}
			Log.i(TAG2, "Received: " + answer);
			
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: x.x.x.x.");
            System.exit(1);
		}
	}

	public void enableSensor(String sensor) {
		Log.i(TAG2, "enableSensor()");
		mOut.println("enableSensor()");
		Log.i(TAG2, "Send: " + sensor);
        mOut.println(sensor);
		
		try {
			String answer = mIn.readLine();
			if (answer.compareTo("throw IllegalArgumentException") == 0) {
				throw new IllegalArgumentException(
						"Sensor '" + sensor
						+ "' is currently not supported.");
			}
			Log.i(TAG2, "Received: " + answer);
			
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: x.x.x.x.");
            System.exit(1);
		}
	}
	
	public String[] getSupportedSensors() {
		Log.i(TAG, "getSupportedSensors()");
        
		mOut.println("getSupportedSensors()");
		String[] sensors = {""};
		int num = 0;
		
		try {
			String numstr = mIn.readLine();
			Log.i(TAG, "Received: " + numstr);
	        
			num = Integer.parseInt(numstr);
			
			sensors = new String[num];
			for (int i=0; i<num; i++) {
				sensors[i] = mIn.readLine();
				Log.i(TAG, "Received: " + sensors[i]);
		        
			}
			
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: x.x.x.x.");
            System.exit(1);
		}
		
		return sensors;
	}
	
	public int getNumSensorValues(String sensor) {
		Log.i(TAG, "Send: getNumSensorValues()");
		mOut.println("getNumSensorValues()");
		
		Log.i(TAG, "Send: " + sensor);
        mOut.println(sensor);
        
		int num = 0;
		
		try {
			String numstr = mIn.readLine();
			if (numstr.compareTo("throw IllegalArgumentException") == 0) {
				throw new IllegalArgumentException(
						"Sensor '" + sensor
						+ "' is currently not supported.");
			}
			Log.i(TAG, "Received: " + numstr);
	        
			num = Integer.parseInt(numstr);
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: x.x.x.x.");
            System.exit(1);
		}
		
		return num;
	}
	
	public void readSensor(String sensor, float[] sensorValues) {
		if (sensorValues == null) {
			throw new NullPointerException (
					"readSensor for '" + sensor
					+ "' called with sensorValues == null.");
		}
		Log.i(TAG, "Send: getNumSensorValues()");
		mOut.println("readSensor()");
		Log.i(TAG, "Send: " + sensor);
        mOut.println(sensor);
		int num = 0;
		
		try {
			String numstr = mIn.readLine();
			if (numstr.compareTo("throw IllegalArgumentException") == 0) {
				throw new IllegalArgumentException(
						"Sensor '" + sensor
						+ "' is currently not supported.");
			} else if (numstr.compareTo("throw IllegalStateException") == 0){
				throw new IllegalStateException(
						"Sensor '" + sensor
						+ "' is currently not enabled.");
			}
			Log.i(TAG, "Received: " + numstr);
	        num = Integer.parseInt(numstr);
	        
	        if (sensorValues.length < num) {
	        	throw new ArrayIndexOutOfBoundsException (
						"readSensor for '" + sensor
						+ "' called with sensorValues having too few elements ("
						+ sensorValues.length + ") to hold the sensor values ("
						+ num + ").");
	        }
			
			//sensorValues = new float[num];
			for (int i=0; i<num; i++) {
				String val = mIn.readLine();
				Log.i(TAG, "Received: " + val);
		        
				sensorValues[i] = Float.parseFloat(val);
			}
		} catch (IOException e) {
			System.err.println("Couldn't get I/O for the connection to: x.x.x.x.");
            System.exit(1);
		}
	}
}
