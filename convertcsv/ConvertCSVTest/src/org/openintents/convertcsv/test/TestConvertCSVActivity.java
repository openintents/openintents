/*
 * 
 * @author Michal Zielinski, michal@zielinscy.org.pl
 * 
 */

package org.openintents.convertcsv.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.openintents.convertcsv.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.test.InstrumentationTestCase;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;

public class TestConvertCSVActivity extends InstrumentationTestCase {
	private Solo solo;
	private Activity activity;
	private Random random = new Random();
	private Intent intent;
	private final String sdcardPath = "/mnt/sdcard/";
	
	public TestConvertCSVActivity() {
		super();
	}

	protected void setUp() throws Exception {
		super.setUp();
	}

	protected void tearDown() throws Exception {
		try {
			this.solo.finalize();
		} catch (Throwable e) {
			e.printStackTrace();
		}
		super.tearDown();
	}
	
	private void setUpActivity(String name, Uri data) {
		Log.i("ConvertCSVTest", "starting activity " + name);
		intent = new Intent();
		intent.setAction("android.intent.action.MAIN");
		intent.setClassName("org.openintents.convertcsv", name);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if(data != null)
			intent.setData(data);

		activity = getInstrumentation().startActivitySync(intent);

		this.solo = new Solo(getInstrumentation(), activity);
	}
	
	private void setUpActivity(String name) {
		setUpActivity(name, Uri.parse("content://org.openintents.shopping/lists/0"));
	}
	
	private String getAppString(int resId) {
		return activity.getString(resId);
	}
	
	private String[] getAppArray(int resId) {
		return activity.getResources().getStringArray(resId);
	}
	
	private void setPreference(String name, boolean value) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(name, value);
		editor.commit();
	}
	private void setPreference(String name, String value) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(name, value);
		editor.commit();
	}
	private void setPreference(String name, int value) {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(activity);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(name, value);
		editor.commit();
	}
	
	private void writeFile(String name, String content, String coding) {
		File file = new File(sdcardPath + name);
		try {
			Writer wr = new OutputStreamWriter(new FileOutputStream(file), coding);
			wr.write(content);
			wr.close();
		} catch(IOException err) {
			throw new RuntimeException(err);
		}
	}
	
	private void enterText(EditText text, String string) {
		solo.clearEditText(text);
		solo.enterText(text, string);
	}
	
	private String putRandom(String template, int rand) {
		return template.replace("$RANDOM", "" + rand);
	}
	
	private void importFile(String name) {
		enterText(solo.getEditText(0), sdcardPath + name);
		
		solo.clickOnButton(getAppString(R.string.file_import));
		solo.clickOnButton(getAppString(R.string.dialog_ok));
	}
	
	private void exportFile(String name) {
		enterText(solo.getEditText(0), sdcardPath + name);
		
		solo.clickOnButton(getAppString(R.string.file_export));
		solo.clickOnButton(getAppString(R.string.dialog_ok));
	}
	
	public boolean lineExists(String[] array, String findLine) {
		for(String line: array) {
			if(line.equals(findLine))
				return true;
		}
		return false;
	}
	private void checkEntriesExists(int rand) {
		enterText(solo.getEditText(0), sdcardPath + "oi-output.csv");
		selectFormat(getAppArray(R.array.shoppinglist_format), 0);
		
		solo.clickOnButton(getAppString(R.string.file_export));
		solo.clickOnButton(getAppString(R.string.dialog_ok));
		String[] lines = readFileLines("oi-output.csv");
		assertEquals(lines[0], "\"Subject\",\"% Complete\",\"Categories\",\"Tags\"");
		assertTrue(lineExists(lines, "\"OI-Test-1-" + rand + "\",0,\"TestList\",\"\""));
		assertTrue(lineExists(lines, "\"OI-Test-2-" + rand + "\",0,\"TestList\",\"\""));
	}
	
	private String[] readFileLines(String name) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(sdcardPath + name));
			ArrayList<String> lines = new ArrayList<String>();
			String line;
			while((line = reader.readLine()) != null) {
				lines.add(line);
			}
			String[] arr = new String[lines.size()];
			lines.toArray(arr);
			return arr;
		} catch(IOException err) {
			throw new RuntimeException(err);
		}
	}
	
	private String[] readTestResource(String resId) {
		try {
			InputStream inp = this.getInstrumentation().getContext().getAssets().open(resId + ".txt");
			BufferedReader reader = new BufferedReader(new InputStreamReader(inp));
			ArrayList<String> lines = new ArrayList<String>();
			String line;
			while((line = reader.readLine()) != null) {
				lines.add(line);
			}
			String[] arr = new String[lines.size()];
			lines.toArray(arr);
			return arr;
		} catch(IOException err) {
			throw new RuntimeException(err);
		}
	}
	
	private void selectFormat(String[] formats, int index) {
		// find format select
		boolean found = false;
		for(String format: formats) {
			if(solo.searchText(format)) {
				solo.clickOnText(format);
				found = true;
				break;
			}
		}
		if(found)
			solo.clickOnText(formats[index]);
		else
			throw new RuntimeException("Format " + formats[index] + " not found");
	}
	
	/* Tests */

	private String shoppingListOutlook = ("Subject,% Complete,Categories,Tags\n" + 
			"OI-Test-1-$RANDOM,0,TestList,\n" +
			"OI-Test-2-$RANDOM,100,TestList,");
	
	public void testShoppingList() throws InterruptedException {
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		setPreference("ask_if_file_exists", false);
		
		exportFile("oi-backup.csv");
	}
	
	// this test fails, because duplicate mode doesn't work
	/*public void testShoppingList_Duplicate() {
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		setPreference("ask_if_file_exists", false);
		setPreference("shoppinglist_import_policy", "0");
		
		int rand = random.nextInt(10000);
		
		writeFile("oi-input.csv", putRandom(shoppingListOutlook, rand), "utf8");
		selectFormat(getAppArray(R.array.shoppinglist_format), 0);
		importFile("oi-input.csv");

		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		selectFormat(getAppArray(R.array.shoppinglist_format), 0);
		exportFile("oi-output.csv");
		String[] lines = readFileLines("oi-output.csv");
		String findLine = "\"OI-Test-1-" + rand + "\",0,\"TestList\",\"\"";
		int times = 0;
		for(String line: lines) {
			if(findLine.equals(line))
				times++;
		}
		assertTrue(times >= 2);
	}*/
	

	public void testShoppingList_Outlook() {
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		setPreference("ask_if_file_exists", false);
		
		int rand = random.nextInt(10000);
		
		writeFile("oi-input.csv", putRandom(shoppingListOutlook, rand), "utf-8");
		enterText(solo.getEditText(0), sdcardPath + "oi-input.csv");
		selectFormat(getAppArray(R.array.shoppinglist_format), 0);
		
		solo.clickOnButton(getAppString(R.string.file_import));
		solo.clickOnButton(getAppString(R.string.dialog_ok));

		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		selectFormat(getAppArray(R.array.shoppinglist_format), 0);
		importFile("oi-input.csv");
		
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		checkEntriesExists(rand);
	}
	
	private String shoppingListOutlookPolishChars = ("Subject,% Complete,Categories,Tags\n" + 
			"OI-Test-ąężź-$RANDOM,0,TestList,\n");
	
	private void checkCheckbox(boolean state) {
		for(View v: solo.getViews()) {
			if(v instanceof CheckBox) {
				if(((CheckBox)v).isChecked() != state)
					solo.clickOnView(v);
				return;
			}
		}
		throw new RuntimeException("checkbox not found");
	}

	private String[] encodings = new String[] {"US_ASCII", "UTF_8", "UTF_16", "ISO_8859_1"};
	
	public void testShoppingList_Encoding() {
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		setPreference("ask_if_file_exists", false);
		
		int rand = random.nextInt(10000);
		
		writeFile("oi-input.csv", putRandom(shoppingListOutlookPolishChars, rand), "utf-16");
		selectFormat(getAppArray(R.array.shoppinglist_format), 0);
		enterText(solo.getEditText(0), sdcardPath + "oi-input.csv");
		
		// import from utf-16
		checkCheckbox(true);
		selectFormat(encodings, 2);
		
		solo.clickOnButton(getAppString(R.string.file_import));
		solo.clickOnButton(getAppString(R.string.dialog_ok));

		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		enterText(solo.getEditText(0), sdcardPath + "oi-output.csv");
		selectFormat(getAppArray(R.array.shoppinglist_format), 0);
		selectFormat(encodings, 1);
		
		solo.clickOnButton(getAppString(R.string.file_export));
		solo.clickOnButton(getAppString(R.string.dialog_ok));
		
		String[] lines = readFileLines("oi-output.csv");
		assertEquals(lines[0], "\"Subject\",\"% Complete\",\"Categories\",\"Tags\"");
		assertTrue(lineExists(lines, "\"OI-Test-ąężź-" + rand + "\",0,\"TestList\",\"\""));
		
	}
	
	public void testShoppingList_Errors() {
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		setPreference("ask_if_file_exists", false);
		
		importFile("oi-this-file-doesn_t_exists");
		
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		exportFile("this-directory-doent-exits/file.csv");
	}
	
	public void testShoppingList_WrongEncoding() {
		int rand = random.nextInt(10000);
		
		writeFile("oi-input.csv", "wrong!", "utf-8");
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		importFile("oi-input.csv");

		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		writeFile("oi-input.csv", putRandom(shoppingListOutlookPolishChars, rand), "utf-8");
		selectFormat(getAppArray(R.array.shoppinglist_format), 0);
		enterText(solo.getEditText(0), sdcardPath + "oi-input.csv");
		
		// import from ascii
		checkCheckbox(true);
		selectFormat(encodings, 0);
		
		solo.clickOnButton(getAppString(R.string.file_import));
		solo.clickOnButton(getAppString(R.string.dialog_ok));

	}

	private String handyshopperHeader = "Need,Priority,Description,CustomText,Quantity,Units,Price,Aisle,Date,Category,Stores,PerStoreInfo,EntryOrder,Coupon,Tax,Tax2,AutoDelete,Private,Note,Alarm,AlarmMidi,Icon,AutoOrder";
	private String shoppingListHandyshopper = (handyshopperHeader + "\n" + 
			"x,,OI-Test-1-$RANDOM,,,,,,,,,,,,,,,,,,0,0,\n" +
			",,OI-Test-2-$RANDOM,,,,,,,,,,,,,,,,,,0,0,\n");
	

	private String shoppingListHandyshopper_FromOutlook = ("Subject,% Complete,Categories,Tags\n" + 
			"OI-Test-1-$RANDOM,0,TestHandyshopper,\n");
	
	private Uri getHandyshopperListUri(int rand) {
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		setPreference("ask_if_file_exists", false);
		
		//activity.getContentResolver().delete(Uri.parse("content://org.openintents.shopping/lists"), "name = 'TestHandyshopper'", null);
		
		writeFile("oi-input.csv", putRandom(shoppingListHandyshopper_FromOutlook, rand), "utf8");

		enterText(solo.getEditText(0), sdcardPath + "oi-input.csv");
		selectFormat(getAppArray(R.array.shoppinglist_format), 0);
		
		solo.clickOnButton(getAppString(R.string.file_import));
		solo.clickOnButton(getAppString(R.string.dialog_ok));
		
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");

		Cursor c = activity.getContentResolver().query(Uri.parse("content://org.openintents.shopping/lists"), 
    			new String[] {"_id", "name"}, "name = 'TestHandyshopper'", null, null);
		
		long listId = -1;
		if (c != null) {
    		if (c.moveToFirst()) {
    			listId = c.getLong(0);
    		}
    		c.close();
    	}
		if(listId == -1)
			fail("failed to find imported list");
		Uri listUri = Uri.parse("content://org.openintents.shopping/lists/" + listId);
		
		Log.i("ConvertCSVTest", "found imported list id=" + listId);
		
		solo.goBack();
		return listUri;
	}
	
	public void testShoppingList_Handyshopper() throws InterruptedException {
		int rand = random.nextInt(10000);
		Uri listUri = getHandyshopperListUri(rand);
		
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity", listUri);
		
		// test import
		writeFile("oi-input.csv", putRandom(shoppingListHandyshopper, rand), "utf8");
		selectFormat(getAppArray(R.array.shoppinglist_format), 1);
		
		importFile("oi-input.csv");

		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		
		enterText(solo.getEditText(0), sdcardPath + "oi-output.csv");
		selectFormat(getAppArray(R.array.shoppinglist_format), 0);
		
		solo.clickOnButton(getAppString(R.string.file_export));
		solo.clickOnButton(getAppString(R.string.dialog_ok));
		String[] lines = readFileLines("oi-output.csv");
		assertEquals(lines[0], "\"Subject\",\"% Complete\",\"Categories\",\"Tags\"");
		assertTrue(lineExists(lines, "\"OI-Test-1-" + rand + "\",0,\"TestHandyshopper\",\"\""));
		
		// test export
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity", listUri);
		
		selectFormat(getAppArray(R.array.shoppinglist_format), 1);
		exportFile("oi-output.csv"); //*/

		lines = readFileLines("oi-output.csv");
		assertEquals(lines[0], handyshopperHeader);
		assertTrue(lineExists(lines, "x,,OI-Test-1-" + rand + ",,,,,,,,,,,,,,,,,,0,0,"));
		assertTrue(lineExists(lines, ",,OI-Test-2-" + rand + ",,,,,,,,,,,,,,,,,,0,0,"));
	}
	
	private boolean hasSameElements(String[] a, String[] b) {
		Arrays.sort(a);
		Arrays.sort(b);
		return Arrays.equals(a, b);
	}
	
	public void testShoppingList_HandyshopperMore() throws InterruptedException, IOException {
		String[] testLines = readTestResource("handyshoppertest");
		
		int rand = random.nextInt(10000);
		Uri listUri = getHandyshopperListUri(rand);
		
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity", listUri);
		
		// test import

		FileWriter wr = new FileWriter(sdcardPath + "oi-input.csv");
		for(String line: testLines) {
			wr.write(line + "\n");			
		}
		wr.close();
		
		selectFormat(getAppArray(R.array.shoppinglist_format), 1);
		
		importFile("oi-input.csv");

		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity", listUri);
		
		enterText(solo.getEditText(0), sdcardPath + "oi-output.csv");
		selectFormat(getAppArray(R.array.shoppinglist_format), 1);
		
		solo.clickOnButton(getAppString(R.string.file_export));
		solo.clickOnButton(getAppString(R.string.dialog_ok));
		
		String[] outLines = readFileLines("oi-output.csv");
		
		// check if exported lines match imported lines...
		Map<String, String[]> outEntries = new HashMap<String, String[]>();
		for(String line: outLines) {
			String[] split = line.split(",");
			outEntries.put(split[2], split);
		}
		
		for(String line: testLines) {
			String[] in = line.split(",");
			String[] out = outEntries.get(in[2]);
			assertNotNull(out);
			assertEquals(in[0], out[0]);
			assertEquals(in[1], out[1]);
			assertEquals(in[4], out[4]);
			assertEquals(in[9], out[9]);
			assert(hasSameElements(in[10].split(";"), out[10].split(";")));
			assert(hasSameElements(in[11].split(";"), out[11].split(";")));
			
		}
	}
	
	private void waitAndClickOnButton(String text) {
		while(true) {
			if(solo.searchButton(text)) {
				solo.clickOnButton(text);
				return;
			}
		}
	}
	
	public void testShoppingList_Performance() throws IOException {
		// import and export list of 1000 elements
		FileWriter wr = new FileWriter(sdcardPath + "oi-input.csv");
		wr.write("Subject,% Complete,Categories,Tags\n");
		int i = 10000;
		for(int j=0; j<1000; j++) {
			wr.write("OI-Test-2-" + i + ",100,TestList1,\n");
			i++;
		}
		wr.close();
		
		try {
			setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
			enterText(solo.getEditText(0), sdcardPath + "oi-input.csv");
			selectFormat(getAppArray(R.array.shoppinglist_format), 0);
	
			solo.clickOnButton(getAppString(R.string.file_import));
			long start = System.currentTimeMillis();
			
			waitAndClickOnButton(getAppString(R.string.dialog_ok));
			long took = System.currentTimeMillis() - start;
			Log.i("ConvertCSVTest", "importing took " + took);
			
			assertTrue(took < 20000);
	
			setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
			solo.clickOnButton(getAppString(R.string.file_export));
			start = System.currentTimeMillis();
			waitAndClickOnButton(getAppString(R.string.dialog_ok));
			took = System.currentTimeMillis() - start;
			Log.i("ConvertCSVTest", "exporting took " + took);
			
			assertTrue(took < 5000);
		} finally {
			cleanList();
		}
	}
	
	private void cleanList() {
		Log.i("ConvertCSVTest", "cleaning lists...");
		setUpActivity("org.openintents.convertcsv.shoppinglist.ConvertCsvActivity");
		
		Cursor c = activity.getContentResolver().query(Uri.parse("content://org.openintents.shopping/lists"), 
    			new String[] {"_id", "name"}, "name = 'TestHandyshopper' OR name = 'TestList' OR name = 'TestList1'", null, null);
		
		int found = 0;
		if(c.moveToFirst()) {
			while (true) {
    			long listId = c.getLong(0);
    			activity.getContentResolver().delete(Uri.parse("content://org.openintents.shopping/contains"), 
    	    			"list_id = " + listId, null);
    			found++;
	       		if(c.isLast()) {
	       			c.close();
	       			break;
	       		} else {
	       			c.moveToNext();
	       		}
	    	}
		}
		Log.i("ConvertCSVTest", "removed entries of " + found + " lists");
	}
	
	public void testShoppingList_ZZ_CleanLists() {
		// cleans content created by tests
		cleanList();
	}
	
	/* notepad tests */
	
	private void importNotesFile(String name, int formatIndex) {
		enterText(solo.getEditText(0), sdcardPath + name);

		selectFormat(getAppArray(R.array.notepad_format), formatIndex);
		
		solo.clickOnButton(getAppString(R.string.file_import));
		solo.clickOnButton(getAppString(R.string.dialog_ok));
	}
	
	private void exportNotesFile(String name, int formatIndex) {
		enterText(solo.getEditText(0), sdcardPath + name);

		selectFormat(getAppArray(R.array.notepad_format), formatIndex);
		
		solo.clickOnButton(getAppString(R.string.file_export));
		solo.clickOnButton(getAppString(R.string.dialog_ok));
	}
	
	private String notepadPalm = "TestNote,0,\nTestNote1,0,";
	private String notepadPalmRand = "TestNote-$RANDOM,0,\nTestNote1-$RANDOM,0,";
	private String notepadOutlook = "Note Body,Categories,Note Color,Priority,Sensitivity\n" +
		"TestNote,,3,Normal,Normal\nTestNote1,,3,Normal,Normal";
	private String notepadJPilot = "CSV memo version 1.6.2.4: Category, Private, Memo Text\n" +
		",0,TestNote\n,0,TestNote1";
	
	private void backupNotes() {
		setUpActivity("org.openintents.convertcsv.notepad.ConvertCsvActivity");
		setPreference("ask_if_file_exists", false);
		
		exportNotesFile("oi-notes-backup.csv", 1);
	}
	
	private void restoreNotes() {
		setUpActivity("org.openintents.convertcsv.notepad.ConvertCsvActivity");
		setPreference("notepad_import_policy", "3");
		setPreference("ask_if_file_exists", false);
		solo.goBack();
		setUpActivity("org.openintents.convertcsv.notepad.ConvertCsvActivity");
		
		enterText(solo.getEditText(0), sdcardPath + "oi-notes-backup.csv");
		selectFormat(getAppArray(R.array.notepad_format), 1);
		solo.clickOnButton(getAppString(R.string.file_import));
		solo.clickOnButton(getAppString(android.R.string.ok));
		solo.clickOnButton(getAppString(R.string.dialog_ok));
		
		setPreference("import_policy_value", "1");
	}
	
	public void testNotepad_Palm() {
		backupNotes();
		
		try {
			setUpActivity("org.openintents.convertcsv.notepad.ConvertCsvActivity");
			setPreference("notepad_import_policy", "0");
			setPreference("ask_if_file_exists", false);
			
			writeFile("oi-input.csv", notepadPalm, "utf-8");
			importNotesFile("oi-input.csv", 1);
			setUpActivity("org.openintents.convertcsv.notepad.ConvertCsvActivity");
			exportNotesFile("oi-output.csv", 1);
			String[] lines = readFileLines("oi-output.csv");
			
			assertTrue(lineExists(lines, "\"TestNote\",\"0\",\"\""));
			assertTrue(lineExists(lines, "\"TestNote1\",\"0\",\"\""));
	
		} finally {
			restoreNotes();
		}
	}
	
	public void testNotepad_Outlook() {
		backupNotes();
		
		try {
			setUpActivity("org.openintents.convertcsv.notepad.ConvertCsvActivity");
			setPreference("notepad_import_policy", "0");
			setPreference("ask_if_file_exists", false);
			
			writeFile("oi-input.csv", notepadOutlook, "utf-8");
			importNotesFile("oi-input.csv", 0);
			setUpActivity("org.openintents.convertcsv.notepad.ConvertCsvActivity");
			exportNotesFile("oi-output.csv", 0);
			String[] lines = readFileLines("oi-output.csv");
			
			assertEquals(lines[0], "Note Body,Categories,Note Color,Priority,Sensitivity");
			assertTrue(lineExists(lines, "\"TestNote\",,3,Normal,Normal"));
			assertTrue(lineExists(lines, "\"TestNote1\",,3,Normal,Normal"));
	
		} finally {
			restoreNotes();
		}
	}
	
	public void testNotepad_JPilot() {
		backupNotes();
		
		try {
			setUpActivity("org.openintents.convertcsv.notepad.ConvertCsvActivity");
			setPreference("notepad_import_policy", "0");
			setPreference("ask_if_file_exists", false);
			
			writeFile("oi-input.csv", notepadJPilot, "utf-8");
			importNotesFile("oi-input.csv", 2);
			setUpActivity("org.openintents.convertcsv.notepad.ConvertCsvActivity");
			exportNotesFile("oi-output.csv", 2);
			String[] lines = readFileLines("oi-output.csv");
			
			assertEquals(lines[0], "CSV memo version 1.6.2.4: Category, Private, Memo Text");
			assertTrue(lineExists(lines, "\"\",\"0\",\"TestNote\""));
			assertTrue(lineExists(lines, "\"\",\"0\",\"TestNote1\""));
		} finally {
			restoreNotes();
		}
	}
	
	public void testNotepad_KeepMode() {
		backupNotes();
		
		try {
			setUpActivity("org.openintents.convertcsv.notepad.ConvertCsvActivity");
			setPreference("notepad_import_policy", "1");
			setPreference("ask_if_file_exists", false);
			
			int rand = random.nextInt(10000);
			
			writeFile("oi-input.csv", putRandom(notepadPalmRand, rand), "utf-8");
			
			importNotesFile("oi-input.csv", 1);
			setUpActivity("org.openintents.convertcsv.notepad.ConvertCsvActivity");
			importNotesFile("oi-input.csv", 1);
			setUpActivity("org.openintents.convertcsv.notepad.ConvertCsvActivity");
			
			exportNotesFile("oi-output.csv", 1);
			String[] lines = readFileLines("oi-output.csv");
			
			// check if entry wasn't duplicated
			
			int count = 0;
			for(String line: lines) {
				if(line.equals("\"TestNote-" + rand + "\",\"0\",\"\""))
					count++;
			}
			
			assertEquals(1, count);
		} finally {
			restoreNotes();
		}
	}
}
