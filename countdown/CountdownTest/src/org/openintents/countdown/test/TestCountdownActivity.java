package org.openintents.countdown.test;

import java.util.Random;

import org.openintents.com.android.internal.widget.NumberPickerButton;
import org.openintents.countdown.R;

import android.app.Activity;
import android.content.Intent;
import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.Smoke;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;

import com.jayway.android.robotium.solo.Solo;

public class TestCountdownActivity extends InstrumentationTestCase {
	
	private static final String TAG = "TestCountdownActivity";
	
	private Solo solo;
	private Activity activity;
	private Random random = new Random();
	
	public TestCountdownActivity() {
		super();
	}
	
	protected void setUp() throws Exception {
		Intent i = new Intent();
		i.setAction("android.intent.action.MAIN");
		i.setClassName("org.openintents.countdown",
				"org.openintents.countdown.list.CountdownListActivity");
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

		activity = getInstrumentation().startActivitySync(i);

		this.solo = new Solo(getInstrumentation(), activity);
	}
	
	@Override
	public void tearDown() throws Exception {
		try {
			this.solo.finishOpenedActivities();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.tearDown();
	}

	private String getAppString(int resId) {
		return activity.getString(resId);
	}

	@Smoke
	public void test000Eula() {
		String accept = getAppString(org.openintents.distribution.R.string.oi_distribution_eula_accept);
		String cancel = getAppString(org.openintents.distribution.R.string.oi_distribution_eula_refuse);
		boolean existsAccept = solo.searchButton(accept);
		boolean existsCancel = solo.searchButton(cancel);
		
		if (existsAccept && existsCancel) {
			solo.clickOnButton(accept);
		}
	}

	@Smoke
	public void test001RecentChanges() {
		String recentChanges = getAppString(org.openintents.distribution.R.string.oi_distribution_newversion_recent_changes);
		String cont = getAppString(org.openintents.distribution.R.string.oi_distribution_newversion_continue);
		while(solo.scrollUp());
		boolean existsRecentChanges = solo.searchText(recentChanges);
		boolean existsCont = solo.searchButton(cont);
		
		if (existsRecentChanges && existsCont) {
			solo.clickOnButton(cont);
		}
	}

	/*
	 * Test adding a countdown
	 */
	public void testAddCountdownToList() {
		String itemname = "testitem_add_" + random.nextInt(10000);
		
		// Add the countdown and name it
		solo.clickOnMenuItem("Add countdown");
		solo.enterText(0, itemname);
		
		solo.goBack();
		
		// Verify that the countdown was added
		assertTrue(solo.searchText(itemname));
	}
	
	/*
	 * Test deleting a countdown using a longpress
	 */
	public void testDeleteCountdownFromList() {
		String itemname = "testitem_delete_" + random.nextInt(10000);
		
		// Add the item
		solo.clickOnMenuItem("Add countdown");
		solo.enterText(0,itemname);
		solo.goBack();
		assertTrue(solo.searchText(itemname));
		
		// Long press to open the context menu
		solo.clickLongOnText(itemname);
		
		// Make sure context menu opened:
		assertTrue(solo.searchText("Edit"));
		assertTrue(solo.searchText("Delete"));
		
		// Delete the item and verify it got deleted
		solo.clickOnText("Delete");
		assertFalse(solo.searchText(itemname));
	}
	
	/*
	 * Test adding and then running a countdown
	 */
	public void testAddAndRunCountdown() {
		String itemname = "testitem_add_and_run_" + random.nextInt(10000);
		
		solo.clickOnMenuItem("Add countdown");
		solo.enterText(0,itemname);
		
		// Increment everything in the duration picker by 1
		for(View v: solo.getViews()) {
			if(v instanceof NumberPickerButton) {
				if(v.getId() == R.id.increment) {
					solo.clickOnView(v);
					solo.sleep(3000);
				}
			}
		}
		
		// Start the countdown
		solo.clickOnButton("Start");
		
		// Verify that the time is correct and that they stop and modify buttons are present
		assertTrue(solo.searchText("1:0"));
		assertTrue(solo.searchButton("Stop"));
		assertTrue(solo.searchButton("Modify"));
		
		// Stop the countdown and verify the start button reappears
		solo.clickOnButton("Stop");
		assertTrue(solo.searchButton("Start"));
	}
	
	/*
	 * Test that the notification settings behave as they should
	 */
	public void testNotificationSettings() {
		String itemname = "testitem_notification_settings_" + random.nextInt(10000);
		
		solo.clickOnMenuItem("Add countdown");
		solo.enterText(0,itemname);
		
		// Click on the Ring checkbox and verify that the setting changes, and all others don't
		solo.clickOnCheckBox(1);
		assertFalse(solo.isCheckBoxChecked("Ring"));
		assertTrue(solo.isCheckBoxChecked("Vibrate"));
		assertTrue(solo.isCheckBoxChecked("Light"));
		assertTrue(solo.searchText("Light"));
		
		// Click on Notification checkbox and verify it's unchecked
		// Then verify all other settings disappear
		solo.clickOnCheckBox(0);
		assertFalse(solo.isCheckBoxChecked("Notification in status bar"));
		assertFalse(solo.searchText("Ring",true));
		assertFalse(solo.searchText("Vibrate",true));
		assertFalse(solo.searchText("Light",true));
	}
	
	/*
	 * Test the button which adds a day picker
	 */
	public void testDayCountdown() {
		String itemname = "testitem_day_picker_" + random.nextInt(10000);
		
		solo.clickOnMenuItem("Add countdown");
		solo.enterText(0,itemname);
		
		// Click on the + to add the day picker
		// Verify there are 4 increment buttons rather than 3
		solo.clickOnButton(0);
		int numIncrementButtons = 0;
		for(View v: solo.getViews()) {
			if(v instanceof NumberPickerButton) {
				if(v.getId() == R.id.increment) {
					numIncrementButtons++;
				}
			}
		}
		assertTrue(numIncrementButtons == 4);
	}
	
	/*
	 * Test setting the countdown using set date rather than set duration
	 */
	public void testSetDate() {
		String itemname = "testitem_date_picker_" + random.nextInt(10000);
		
		solo.clickOnMenuItem("Add countdown");
		solo.enterText(0,itemname);
		
		// Change the mode from Set duration to Set date
		solo.clickOnMenuItem("Set date");
		
		// Set the time of day
		solo.clickOnButton(1);
		TimePicker time;
		time = solo.getCurrentTimePickers().get(0);
		int hour = 10;
		int minute = 16;
		solo.setTimePicker(time, hour, minute);
		solo.clickOnButton("Set");
		
		// Verify the time of day that we set is displayed
		assertTrue(solo.searchButton(hour+":"+minute));
		
		// Set the date (Robotium numbers months starting from 0 so Dec. is 11)
		solo.clickOnButton(0);
		DatePicker date;
		date = solo.getCurrentDatePickers().get(0);
		int day = 21;
		int month = 11;
		int year = 2012;
		solo.setDatePicker(date, year, month, day);
		solo.clickOnButton("Set");
		
		// Verify the date that we set is displayed
		assertTrue(solo.searchButton((month+1)+"/"+day+"/"+year));
	}
	
	/*
	 * Test deleting a countdown from the menu rather than with a longpress
	 */
	public void testDeleteFromMenu() {
		String itemname = "testitem_delete_from_menu_" + random.nextInt(10000);
		
		solo.clickOnMenuItem("Add countdown");
		solo.enterText(0,itemname);
		
		solo.clickOnMenuItem("Delete");
		
		// Verify the item got deleted
		assertFalse(solo.searchText(itemname));
	}
	
	/*
	 * Test that the notification setting is stored like it should be.
	 */
	public void testNotificationSetting() {
		// Go to the preferences and set the Notification timeout
		solo.clickOnMenuItem("Settings");
		solo.clickOnText("Notification timeout");
		solo.clickOnText("1 minute");
		
		// Return to the main activity
		solo.goBack();
		
		// Go to preferences and verify the correct preference is checked
		solo.clickOnMenuItem("Settings");
		solo.clickOnText("Notification timeout");
		assertTrue(solo.isTextChecked("1 minute"));
	}
}
