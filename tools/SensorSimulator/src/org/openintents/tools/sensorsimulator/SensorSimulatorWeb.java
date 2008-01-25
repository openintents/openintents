package org.openintents.tools.sensorsimulator;

import javax.swing.JApplet;
import javax.swing.SwingUtilities;

public class SensorSimulatorWeb extends JApplet {
	
	public SensorSimulator mSensorSimulator;
	
	public void init() {
        //Execute a job on the event-dispatching thread:
        //creating this applet's GUI.
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    createGUI();
                }
            });
        } catch (Exception e) {
            System.err.println("createGUI didn't successfully complete");
        }
    }
	
    private void createGUI() {        
        //Create the text field and make it uneditable.
        mSensorSimulator = new SensorSimulator();

        //Set the layout manager so that the text field will be
        //as wide as possible.
        setLayout(new java.awt.GridLayout(1,0));

        //Add the text field to the applet.
        add(mSensorSimulator);
    }

    public void start() {
        
    }

    public void stop() {
        
    }

    public void destroy() {
        
    }
    
    private void cleanUp() {
        //Execute a job on the event-dispatching thread:
        //taking the text field out of this applet.
    	
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    remove(mSensorSimulator);
                }
            });
        } catch (Exception e) {
            System.err.println("cleanUp didn't successfully complete");
        }
        mSensorSimulator = null;
        
    }

}
