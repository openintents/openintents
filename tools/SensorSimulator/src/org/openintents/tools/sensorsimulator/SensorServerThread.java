package org.openintents.tools.sensorsimulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SensorServerThread implements Runnable {

	public SensorSimulator mSensorSimulator;
	
	/**
	 * linked list of all successors, so that we can destroy them 
	 * when needed.
	 */
	public Thread mThread;
	public SensorServerThread nextThread;
	public SensorServerThread previousThread;
	
	public Socket mClientSocket;
	
	
	
	boolean talking; // whether thread is supposed to be continuing work.
	/*
	String[] mSupportedSensors = {
			SensorSimulator.ACCELEROMETER,
			SensorSimulator.COMPASS,
			SensorSimulator.ORIENTATION
		};
	*/
	/**
	 * Constructor to start as thread.
	 * @param newSensorSimulator
	 */
	public SensorServerThread(SensorSimulator newSensorSimulator,
			Socket newClientSocket) {
		mSensorSimulator = newSensorSimulator;
		nextThread = null;
		previousThread = null;
		mClientSocket = newClientSocket;
		talking = true;
		
		// start ourselves:
		mThread = new Thread(this);
		mThread.start();
	}
	
	// Thread is called exactly once.
	public void run() {
		listenThread();
	}
	

    public void listenThread() {
        try {
	        PrintWriter out = new PrintWriter(mClientSocket.getOutputStream(), true);
	        BufferedReader in = new BufferedReader(
					new InputStreamReader(
					mClientSocket.getInputStream()));
	        String inputLine, outputLine;
	        
	        outputLine = "SensorSimulator";
	        out.println(outputLine);
	        
	        mSensorSimulator.addMessage("Incoming connection opened.");
	    	
	
	        while ((inputLine = in.readLine()) != null) {
	             //outputLine = kkp.processInput(inputLine);
	        	if (inputLine.compareTo("getSupportedSensors()") == 0) {
	        		String[] supportedSensors = getSupportedSensors();
	        		out.println("" + supportedSensors.length);
	        		for (int i=0; i<supportedSensors.length; i++) {
	        			out.println(supportedSensors[i]);
	        		}
	        		
	        	} else if (inputLine.compareTo("disableSensor()") == 0 ||
	        			inputLine.compareTo("enableSensor()") == 0) {
	        		boolean enable = (inputLine.compareTo("enableSensor()") == 0);
	        		
	        		inputLine = in.readLine();
	        		// Test which sensor is meant and whether that sensor is supported.
	        		if (inputLine.compareTo(SensorSimulator.ACCELEROMETER) == 0
	        				&& mSensorSimulator.mSupportedAccelerometer.isSelected()) {
	        			out.println("" + mSensorSimulator.mEnabledAccelerometer.isSelected());
	        			mSensorSimulator.mEnabledAccelerometer.setSelected(enable);
	        		} else if (inputLine.compareTo(SensorSimulator.COMPASS) == 0
	        				&& mSensorSimulator.mSupportedCompass.isSelected()) {
	        			out.println("" + mSensorSimulator.mEnabledCompass.isSelected());
	        			mSensorSimulator.mEnabledCompass.setSelected(enable);
	        		} else if (inputLine.compareTo(SensorSimulator.ORIENTATION) == 0
	        				&& mSensorSimulator.mSupportedOrientation.isSelected()) {
	        			out.println("" + mSensorSimulator.mEnabledOrientation.isSelected());
	        			mSensorSimulator.mEnabledOrientation.setSelected(enable);
	        		} else if (inputLine.compareTo(SensorSimulator.THERMOMETER) == 0
	        				&& mSensorSimulator.mSupportedThermometer.isSelected()) {
	        			out.println("" + mSensorSimulator.mEnabledThermometer.isSelected());
	        			mSensorSimulator.mEnabledThermometer.setSelected(enable);
	        		} else {
	        			// This sensor is not supported
	        			out.println("throw IllegalArgumentException");
	        		}
	        	} else if (inputLine.compareTo("getNumSensorValues()") == 0) {
	    	        inputLine = in.readLine();
	        		if (inputLine.compareTo(SensorSimulator.ACCELEROMETER) == 0
	        				&& mSensorSimulator.mSupportedAccelerometer.isSelected()) {
	        			out.println("3");
	        		} else if (inputLine.compareTo(SensorSimulator.COMPASS) == 0
	        				&& mSensorSimulator.mSupportedCompass.isSelected()) {
	        			out.println("3");
	        		} else if (inputLine.compareTo(SensorSimulator.ORIENTATION) == 0
	        				&& mSensorSimulator.mSupportedOrientation.isSelected()) {
	        			out.println("3");
	        		} else if (inputLine.compareTo(SensorSimulator.THERMOMETER) == 0
	        				&& mSensorSimulator.mSupportedThermometer.isSelected()) {
	        			out.println("1");
	        		} else {
	        			// This sensor is not supported
	        			out.println("throw IllegalArgumentException");
	        		}
	        	} else if (inputLine.compareTo("readSensor()") == 0) {
	        		inputLine = in.readLine();
	        		if (inputLine.compareTo(SensorSimulator.ACCELEROMETER) == 0
	        				&& mSensorSimulator.mSupportedAccelerometer.isSelected()) {
	        			if (mSensorSimulator.mEnabledAccelerometer.isSelected()) {
		        			out.println("3"); // number of data following
		        			out.println(mSensorSimulator.mobile.accelx);
		        			out.println(mSensorSimulator.mobile.accely);
		        			out.println(mSensorSimulator.mobile.accelz);
	        			} else {
	        				// This sensor is currently disabled
	        				out.println("throw IllegalStateException");
	        			}
	        		} else if (inputLine.compareTo(SensorSimulator.COMPASS) == 0
	        				&& mSensorSimulator.mSupportedCompass.isSelected()) {
	        			if (mSensorSimulator.mEnabledCompass.isSelected()) {
		        			out.println("3"); // number of data following
		        			out.println(mSensorSimulator.mobile.compassx);
		        			out.println(mSensorSimulator.mobile.compassy);
		        			out.println(mSensorSimulator.mobile.compassz);
	        			} else {
	        				// This sensor is currently disabled
	        				out.println("throw IllegalStateException");
	        			}
	        		} else if (inputLine.compareTo(SensorSimulator.ORIENTATION) == 0
	        				&& mSensorSimulator.mSupportedOrientation.isSelected()) {
	        			if (mSensorSimulator.mEnabledOrientation.isSelected()) {
			        		out.println("3"); // number of data following
		        			out.println(mSensorSimulator.mobile.yaw);
		        			out.println(mSensorSimulator.mobile.pitch);
		        			out.println(mSensorSimulator.mobile.roll);
	        			} else {
	        				// This sensor is currently disabled
	        				out.println("throw IllegalStateException");
	        			}
	        		} else if (inputLine.compareTo(SensorSimulator.THERMOMETER) == 0
	        				&& mSensorSimulator.mSupportedThermometer.isSelected()) {
	        			if (mSensorSimulator.mEnabledThermometer.isSelected()) {
				        	out.println("1"); // number of data following
				        	out.println(mSensorSimulator.mobile.temperature);
	        			} else {
	        				// This sensor is currently disabled
	        				out.println("throw IllegalStateException");
	        			}
	        		} else {
	        			// This sensor is not supported
	        			out.println("throw IllegalArgumentException");
	        		}
	        	}
	        	//outputLine = inputLine;
	        	//mSensorSimulator.yawSlider.setValue(Integer.parseInt(inputLine));
	             //out.println(outputLine);
	             //if (outputLine.equals("Bye."))
	                //break;
	        }
	        out.close();
	        in.close();
	        mClientSocket.close();
	        
        } catch (IOException e) {
        	if (talking) {
	        	System.err.println("IOException in SensorServerThread.");
	            // System.exit(1);
	        	try {
	        		if (mClientSocket != null) mClientSocket.close();
	            } catch (IOException e2) {
	                System.err.println("Close failed as well.");
	                // System.exit(1);
	            }
        	} else {
        		// everything fine. Our mouth was shut deliberately.
        	}
        }
        
        //  Here we finish program execution and we take ourselves out of the chained list:
        if (previousThread != null) {
        	previousThread.nextThread = nextThread;
        }
        if (nextThread != null) {
        	nextThread.previousThread = previousThread;
        }
        mSensorSimulator.addMessage("Incoming connection closed.");
    	
    }

    public String[] getSupportedSensors() {
    	String[] sensorList = new String[4]; // currently max. 4 possible!
		int sensorMax = 0;
		if (mSensorSimulator.mSupportedAccelerometer.isSelected()) {
			sensorList[sensorMax] = SensorSimulator.ACCELEROMETER;
			sensorMax++;
		}
		if (mSensorSimulator.mSupportedCompass.isSelected()) {
			sensorList[sensorMax] = SensorSimulator.COMPASS;
			sensorMax++;
		}
		if (mSensorSimulator.mSupportedOrientation.isSelected()) {
			sensorList[sensorMax] = SensorSimulator.ORIENTATION;
			sensorMax++;
		}
		if (mSensorSimulator.mSupportedThermometer.isSelected()) {
			sensorList[sensorMax] = SensorSimulator.THERMOMETER;
			sensorMax++;
		}
		String[] returnSensorList = new String[sensorMax];
		for (int i=0; i<sensorMax; i++)
			returnSensorList[i] = sensorList[i];
		
		return returnSensorList;
    }
    
    public void stop() {
    	// close the socket
    	try {
    		talking = false;
    		mClientSocket.close();
        } catch (IOException e) {
            System.err.println("Close failed.");
            System.exit(1);
        }
    }
}
