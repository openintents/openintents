/*
 * Port of OpenIntents simulator to Android 2.1, extension to multi
 * emulator support, and GPS and battery simulation is developed as a
 * diploma thesis of Josip Balic at the University of Zagreb, Faculty of
 * Electrical Engineering and Computing.
 * 
 * Copyright (C) 2008-2010 OpenIntents.org
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

package org.openintents.tools.simulator.main;

import hr.fer.tel.simulator.Global;
import hr.fer.tel.simulator.SensorSimulator;
import hr.fer.tel.simulator.SimulatorInstances;
import hr.fer.tel.simulator.TelnetSimulator;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Main class of our sensor simulator. This class creates a Frame that is filled
 * by SensorSimulator class, creates a menu bar and panel for tabs.
 * 
 * @author Josip Balic
 */
public class SensorSimulatorMain extends JPanel implements ActionListener,
		WindowListener, ChangeListener, ItemListener {

	private static final long serialVersionUID = -5990997086225010821L;
	// command strings
	static String new_Tab = "New Tab";
	static String close_Tab = "Close Tab";
	static String exit = "Exit";

	// variable that holds running instances of Sensor Simulators
	public static SimulatorInstances simulatorInstances = new SimulatorInstances();

	/**
	 * Create the GUI and show it. For thread safety, this method should be
	 * invoked from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("SensorSimulator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// Create the menu bar.
		JMenuBar myMenuBar = new JMenuBar();
		myMenuBar.setPreferredSize(new Dimension(200, 30));

		// Add menu items
		JMenu menu;
		menu = new JMenu("File");
		myMenuBar.add(menu);

		// Create a group of JMenuItems
		JMenuItem menuItem1;
		JMenuItem menuItem2;
		JMenuItem menuItem4;

		// Create TabbedPane
		JTabbedPane tabbedPane = new JTabbedPane();

		// Create MenuListener
		MenuListener menuListener = new MenuListener(tabbedPane,
				simulatorInstances);
		// Create MenuItems
		menuItem1 = new JMenuItem("New Tab");
		menuItem2 = new JMenuItem("Close Tab");
		menuItem4 = new JMenuItem("Exit");
		// SetActionCommands for MenuItems
		menuItem1.setActionCommand(new_Tab);
		menuItem2.setActionCommand(close_Tab);
		menuItem4.setActionCommand(exit);
		// Set ActionListeners for MenuItems
		menuItem1.addActionListener(menuListener);
		menuItem2.addActionListener(menuListener);
		menuItem4.addActionListener(menuListener);
		// Add MenuItems to Menu
		menu.add(menuItem1);
		menu.add(menuItem2);
		menu.add(menuItem4);

		tabbedPane.setPreferredSize(new Dimension(Global.WIDTH, Global.HEIGHT));
		// Create tab pane and add first simulator tab to it
		SensorSimulator simulator = new SensorSimulator();
		TelnetSimulator telnet = new TelnetSimulator();

		// add instance of this simulator to SensorSimulatorInstances
		setFirstSimulatorInstance(simulator);
		JScrollPane simulatorScroll = new JScrollPane(simulator.view);
		simulatorScroll
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		JScrollPane telnetScroll = new JScrollPane(telnet.view);
		telnetScroll
				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		tabbedPane.addTab("Sensor Simulator", simulatorScroll);

		tabbedPane.addTab("Telnet Simulator", telnetScroll);

		frame.pack();
		frame.add(tabbedPane);

		// Create a yellow label to put in the content pane.
		JLabel yellowLabel = new JLabel();
		yellowLabel.setPreferredSize(new Dimension(400, 180));

		// Start creating and adding components.
		JCheckBox changeButton = new JCheckBox("Glass pane \"visible\"");
		changeButton.setSelected(false);

		// Set the menu bar and add the label to the content pane.
		frame.setJMenuBar(myMenuBar);

		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Method that adds instance of our first simulator tab to
	 * SimulatorInstances
	 * 
	 * @param sensorSimulator
	 *            , SensorSimulator instance we want to add.
	 */
	private static void setFirstSimulatorInstance(SensorSimulator simulator) {
		simulatorInstances.addSimulator(simulator);
	}

	/** Add a listener for window events. */
	void addWindowListener(Window w) {
		w.addWindowListener(this);
	}

	// React to window events.
	public void windowOpened(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowDeactivated(WindowEvent e) {
	}

	/**
	 * Main method of SensorSimulatorMain class.
	 * 
	 * @param args
	 *            , String[] arguments used to run this GUI.
	 */
	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager
							.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				} catch (InstantiationException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				} catch (UnsupportedLookAndFeelException e) {
					e.printStackTrace();
				}
				createAndShowGUI();
			}
		});
	}

	/**
	 * This method is invoked when action happens.
	 * 
	 * @param e
	 *            , ActionEvent that generated action.
	 */
	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();

		if (action.equals(exit)) {
			System.exit(0);
		}

	}

	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void stateChanged(ChangeEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void itemStateChanged(ItemEvent arg0) {
		// TODO Auto-generated method stub

	}
}

/**
 * MenuListener is inner class that implements ActionListener and allows us to
 * set what should happen on string commands.
 * 
 * @author Josip Balic
 */
class MenuListener implements ActionListener {
	private JTabbedPane tabbedPanel;
	private SimulatorInstances sensorSimulatorInstance;

	/**
	 * Constructor.
	 * 
	 * @param tabbedPane
	 *            , JTabbedPane we pass to MenuListener
	 * @param simulatorInstance
	 *            , SensorSimulatorInstances object that holds all of our sensor
	 *            simulators instances
	 */
	public MenuListener(JTabbedPane tabbedPane,
			SimulatorInstances simulatorInstance) {
		tabbedPanel = tabbedPane;
		sensorSimulatorInstance = simulatorInstance;
	}

	/**
	 * In this method we implement what should happen for specific command
	 * string.
	 * 
	 * @param e
	 *            , ActionEvent
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == "Exit") {
			System.exit(0);
		} else if (e.getActionCommand() == "New Tab") {
			String st = JOptionPane.showInputDialog(null, "Enter Tab Name.");
			if (!st.equals("")) {
				SensorSimulator simulator = new SensorSimulator();
				JComponent panel = simulator.view;
				sensorSimulatorInstance.addSimulator(simulator);
				tabbedPanel.addTab(st, panel);
			} else {
				JOptionPane.showMessageDialog(null, "Please name your Tab");
			}
		} else if (e.getActionCommand() == "Close Tab") {
			tabbedPanel.remove(tabbedPanel.getSelectedComponent());
		}

	}
}