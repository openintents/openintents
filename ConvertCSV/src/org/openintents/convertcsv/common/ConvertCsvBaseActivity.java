package org.openintents.convertcsv.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.openintents.convertcsv.PreferenceActivity;
import org.openintents.convertcsv.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class ConvertCsvBaseActivity extends Activity {
	
	private final static String TAG = "ConvertCsvBaseActivity";

	protected static final int MENU_SETTINGS = Menu.FIRST + 1;
	
	protected static final int DIALOG_ID_WARN_OVERWRITE = 1;

	protected EditText mEditText;

	protected String PREFERENCE_FILENAME;

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convert);
        
        setPreferencesUsed();
         
        mEditText = (EditText) findViewById(R.id.file_path);
        
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this);
        mEditText.setText(pm.getString(PREFERENCE_FILENAME, getString(R.string.default_path)));

        Button buttonImport = (Button) findViewById(R.id.file_import);
        
        buttonImport.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startImport();
			}
        });
        
        Button buttonExport = (Button) findViewById(R.id.file_export);
        
        buttonExport.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startExport();
			}
        });
    }
    
    public void setPreferencesUsed() {
    	
    }
    

    public void startImport() {
    	// First delete old lists
    	//getContentResolver().delete(Shopping.Contains.CONTENT_URI, null, null);
    	//getContentResolver().delete(Shopping.Items.CONTENT_URI, null, null);
    	//getContentResolver().delete(Shopping.Lists.CONTENT_URI, null, null);
    	

    	String fileName = getAndSaveFilename();
    	
    	Log.i(TAG, "Importing...");
    	
    	File file = new File(fileName);
		if (true) { // (!file.exists()) {
			try{
				FileReader reader = new FileReader(file);
				
				doImport(reader);
				
				reader.close();
				Toast.makeText(this, R.string.import_finished, Toast.LENGTH_SHORT).show();
				finish();
				
			} catch (FileNotFoundException e) {
				Toast.makeText(this, R.string.error_file_not_found, Toast.LENGTH_SHORT).show();
				Log.i(TAG, "File not found", e);
			} catch (IOException e) {
				Toast.makeText(this, R.string.error_reading_file, Toast.LENGTH_SHORT).show();
				Log.i(TAG, "IO exception", e);
				
			}
		}
    }
    
    /**
	 * @param reader
	 * @throws IOException
	 */
	public void doImport(FileReader reader) throws IOException {
	
	}
    
    public void startExport() {

		String fileName = mEditText.getText().toString();
    	
    	Log.i(TAG, "Exporting...");
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	boolean askIfExists = prefs.getBoolean(PreferenceActivity.PREFS_ASK_IF_FILE_EXISTS, true);

    	final File file = new File(fileName);
    	
		if (file.exists() && askIfExists) {
			showDialog(DIALOG_ID_WARN_OVERWRITE);
		} else {
			doExport();
			//finish();
		}
    }

	/**
	 * @param file
	 */
	public void doExport() {
		String fileName = getAndSaveFilename();
    	final File file = new File(fileName);
    	
		try{
			FileWriter writer = new FileWriter(file);
			
			doExport(writer);

			writer.close();

			Toast.makeText(this, R.string.export_finished, Toast.LENGTH_SHORT).show();
			finish();
		} catch (IOException e) {
			Toast.makeText(this, R.string.error_writing_file, Toast.LENGTH_SHORT).show();
			Log.i(TAG, "IO exception", e);
			
		}
	}
    
	/**
	 * @param writer
	 * @throws IOException
	 */
	public void doExport(FileWriter writer) throws IOException {
	
	}
	
	/**
	 * @return
	 */
	public String getAndSaveFilename() {

		String fileName = mEditText.getText().toString();
		
		SharedPreferences prefs = PreferenceManager
		.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putString(PREFERENCE_FILENAME, fileName);
		editor.commit();

		return mEditText.getText().toString();
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings).setShortcut(
				'1', 's').setIcon(android.R.drawable.ic_menu_preferences);

		return true;
	}
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_SETTINGS:
			Intent intent = new Intent(this, PreferenceActivity.class);
			startActivity(intent);
		}
		return super.onOptionsItemSelected(item);
	}
	

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_ID_WARN_OVERWRITE:
			LayoutInflater inflater = LayoutInflater.from(this);
			View view = inflater.inflate(R.layout.file_exists, null);
			final CheckBox cb = (CheckBox) view
					.findViewById(R.id.dont_ask_again);
			return new AlertDialog.Builder(this).setView(view).setPositiveButton(
					android.R.string.yes, new OnClickListener() {

						public void onClick(DialogInterface dialog, int which) {

							saveBooleanPreference(PreferenceActivity.PREFS_ASK_IF_FILE_EXISTS, !cb.isChecked());
							finish();

						}


					}).setNegativeButton(android.R.string.no, new OnClickListener() {

				public void onClick(DialogInterface dialog, int which) {
					// Cancel should not do anything.
					
					//saveBooleanPreference(PreferenceActivity.PREFS_ASK_IF_FILE_EXISTS, !cb.isChecked());
					//finish();
				}

			}).create();
		}
		return null;
	}


	/**
	 * @param preference
	 * @param value
	 */
	private void saveBooleanPreference(String preference, boolean value) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putBoolean(preference, value);
		editor.commit();
		doExport();
	}
}
