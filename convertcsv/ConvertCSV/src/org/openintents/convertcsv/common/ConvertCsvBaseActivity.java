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

package org.openintents.convertcsv.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.openintents.convertcsv.PreferenceActivity;
import org.openintents.convertcsv.R;
import org.openintents.distribution.DistributionLibraryActivity;
import org.openintents.distribution.DownloadOIAppDialog;
import org.openintents.intents.FileManagerIntents;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml.Encoding;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

public class ConvertCsvBaseActivity extends DistributionLibraryActivity {
	
	private final static String TAG = "ConvertCsvBaseActivity";

	protected static final int MENU_SETTINGS = Menu.FIRST + 1;
	protected static final int MENU_DISTRIBUTION_START = Menu.FIRST + 100; // MUST BE LAST
	
	protected static final int DIALOG_ID_WARN_OVERWRITE = 1;
	protected static final int DIALOG_ID_NO_FILE_MANAGER_AVAILABLE = 2;
	protected static final int DIALOG_ID_WARN_RESTORE_POLICY = 3;
	protected static final int DIALOG_DISTRIBUTION_START = 100; // MUST BE LAST
	
	protected static final int REQUEST_CODE_PICK_FILE = 1;

	protected EditText mEditText;

	protected TextView mConvertInfo;
	protected Spinner mSpinner;

	protected String PREFERENCE_FILENAME;
	protected String DEFAULT_FILENAME;
	protected String PREFERENCE_FORMAT;
	protected String DEFAULT_FORMAT = null;
	protected String PREFERENCE_ENCODING;
	protected String PREFERENCE_USE_CUSTOM_ENCODING;
	
	protected int RES_STRING_FILEMANAGER_TITLE = 0;
	protected int RES_STRING_FILEMANAGER_BUTTON_TEXT = 0;
	protected int RES_ARRAY_CSV_FILE_FORMAT = 0;
	protected int RES_ARRAY_CSV_FILE_FORMAT_VALUE = 0;
	
	static final public int IMPORT_POLICY_DUPLICATE = 0;
	static final public int IMPORT_POLICY_KEEP = 1;
	static final public int IMPORT_POLICY_OVERWRITE = 2;
	static final public int IMPORT_POLICY_RESTORE = 3;
	static final public int IMPORT_POLICY_MAX = IMPORT_POLICY_RESTORE;
	
	
	String[] mFormatValues;
	
	// This is the activity's message handler that the worker thread can use to communicate
	// with the main thread. This may be null if the activity is paused and could change, so
	// it needs to be read and verified before every use.
	static protected Handler smCurrentHandler;
	
	// True if we have an active worker thread.
	static boolean smHasWorkerThread;
	
	// Max value for the progress bar.
	static int smProgressMax;

	static final public int MESSAGE_SET_PROGRESS = 1;	// Progress changed, arg1 = new status
	static final public int MESSAGE_SUCCESS = 2;		// Operation finished.
	static final public int MESSAGE_ERROR = 3;			// An error occured, arg1 = string ID of error
	static final public int MESSAGE_SET_MAX_PROGRESS = 4;	// Set maximum progress int, arg1 = new max value

	
	// Message handler that receives status messages from the
	// CSV import/export thread.
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_SET_PROGRESS:
				ConvertCsvBaseActivity.this.setConversionProgress(msg.arg1);
				break;
				
			case MESSAGE_SET_MAX_PROGRESS:
				ConvertCsvBaseActivity.this.setMaxProgress(msg.arg1);
				break;
				
				
			case MESSAGE_SUCCESS:
				ConvertCsvBaseActivity.this.displayMessage(msg.arg1, true);
				break;
				
			case MESSAGE_ERROR:
				ConvertCsvBaseActivity.this.displayMessage(msg.arg1, false);
				break;
			}			
		}
	};

	private Spinner mSpinnerEncoding;

	private CheckBox mCustomEncoding;

	private OnCheckedChangeListener mCustomEncodingListener = new OnCheckedChangeListener() {
		
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {		
			mSpinnerEncoding.setEnabled(isChecked);
		}
	};


	

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDistribution.setFirst(MENU_DISTRIBUTION_START, DIALOG_DISTRIBUTION_START);
        
        // Check whether EULA has been accepted
        // or information about new version can be presented.
        if (mDistribution.showEulaOrNewVersion()) {
            return;
        }
        
        // Always create the main layout first, since we need to populate the
        // variables with all the views.
    	switchToMainLayout();

    	if (smHasWorkerThread) {
        	switchToConvertLayout();
        }
    }
    
    private void switchToMainLayout() {
        setContentView(R.layout.convert);
        
        DEFAULT_FILENAME = getString(R.string.default_filename);
        
        setPreferencesUsed();
         
        mEditText = (EditText) findViewById(R.id.file_path);
        
        SharedPreferences pm = PreferenceManager.getDefaultSharedPreferences(this);
        String filepath = pm.getString(PREFERENCE_FILENAME, DEFAULT_FILENAME);
        if (filepath.equals(DEFAULT_FILENAME)) {
        	// prepend SD path
        	filepath = getSdCardFilename(DEFAULT_FILENAME);
        }
        mEditText.setText(filepath);

        ImageButton buttonFileManager = (ImageButton) findViewById(R.id.file_manager);
        
        buttonFileManager.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				openFileManager();
			}
        });
        
        mConvertInfo = (TextView) findViewById(R.id.convert_info);
        
        Button buttonImport = (Button) findViewById(R.id.file_import);
        
        buttonImport.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				startImport();
			}
        });
        
        Button buttonExport = (Button) findViewById(R.id.file_export);
        
        buttonExport.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				startExport();
			}
        });
        
        mSpinner = (Spinner) findViewById(R.id.spinner1);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, RES_ARRAY_CSV_FILE_FORMAT, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinner.setAdapter(adapter);
                
        mFormatValues = getResources().getStringArray(RES_ARRAY_CSV_FILE_FORMAT_VALUE);
        
        setSpinner(pm.getString(PREFERENCE_FORMAT, DEFAULT_FORMAT));
        
        // set encoding spinner
        mSpinnerEncoding = (Spinner) findViewById(R.id.spinner_encoding);
        EncodingAdapter adapterEncoding = new EncodingAdapter(this, android.R.layout.simple_spinner_item);
        adapterEncoding.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerEncoding.setAdapter(adapterEncoding);
        
        String encodingString = getDefaultEncoding().name();
        try {
        	encodingString = pm.getString(PREFERENCE_ENCODING, encodingString);
        } catch (ClassCastException e) {
        }
        
        Encoding encoding;
		try {
        	encoding = Encoding.valueOf(encodingString);           
		} catch (IllegalArgumentException e) {
			encoding = Encoding.UTF_8;
		}
        int encodingPosition = adapterEncoding.getPosition(encoding);
        if (encodingPosition != Spinner.INVALID_POSITION){
        	mSpinnerEncoding.setSelection(encodingPosition);
        }
     
        // set encoding checkbox
        mCustomEncoding = (CheckBox)findViewById(R.id.custom_encoding);
        mCustomEncoding.setOnCheckedChangeListener(mCustomEncodingListener);
        mCustomEncoding.setChecked(pm.getBoolean(PREFERENCE_USE_CUSTOM_ENCODING, false));
        
        Intent intent = getIntent();
        String type = intent.getType();
        if (type != null && type.equals("text/csv")) {
        	// Someone wants to import a CSV document through the file manager. 
        	// Set the path accordingly:
        	String path = getIntent().getDataString();
        	if (path != null) {
	        	if (path.startsWith("file://")) {
	        		path = path.substring(7);
	        	}
	        	mEditText.setText(path);
        	}
        }
    }
    
    private void switchToConvertLayout() {
       	setContentView(R.layout.convertprogress);
		((ProgressBar) findViewById(R.id.Progress)).setMax(smProgressMax);
        smCurrentHandler = mHandler;
    }

    @Override
    public void onDestroy() {
    	super.onDestroy();
    	
    	// The worker thread is on its own now.
    	smCurrentHandler = null;
    }

    
    public void setSpinner(String value) {
    	// get the ID:
    	int id = findString(mFormatValues, value);
    	
    	if (id != -1) {
    		mSpinner.setSelection(id);
    	}
    }
    
    private static int findString(String[] array, String string) {
    	int length = array.length;
    	for (int i = 0; i < length; i++) {
    		if (string.equals(array[i])) {
    			return i;
    		}
    	}
    	return -1;
    }
    
    public void setPreferencesUsed() {
    	
    }
    
    /**
     * Display the current import policy.
     * 
     * @param prefString The key in the shared preferences that contains
     * the import policy value.
     */
    public void displayImportPolicy() {
    	int importPolicy = getValidatedImportPolicy();
    	
    	String[] policyStrings = getResources().getStringArray(R.array.import_policy_detail);

    	TextView policyView = ((TextView) findViewById(R.id.import_policy_detail));
    	
    	if (policyView != null) {
    		policyView.setText(policyStrings[importPolicy]);
    	}
    }
    
    public int getValidatedImportPolicy() {
    	String prefKey = getImportPolicyPrefString();
    	
    	if (prefKey == null) {
    		// This activity does not support import policies.
    		return IMPORT_POLICY_DUPLICATE;
    	}
    	
    	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    	
    	String importPolicy = prefs.getString(prefKey, getDefaultImportPolicy());
    	
    	try
    	{
    		int policy = Integer.parseInt(importPolicy);
    		
    		if (policy < 0 || policy > IMPORT_POLICY_MAX)
    		{
    			return 0;
    		}
    		
    		return policy;
    	}
    	catch(NumberFormatException e)
    	{
    		// Invalid prefs.
    		return 0;
    	}
    }
    
    /**
     * @return The string that identifies the import policy for this importer.
     * null if this derived activity does not support import policies.
     */
    public String getImportPolicyPrefString() {
    	return null;
    }
    
    /**
     * @return The default import policy
     */
    public String getDefaultImportPolicy() {
    	return "" + IMPORT_POLICY_DUPLICATE;
    }

    public void startImport() {
		int importPolicy = getValidatedImportPolicy();
		
		if (importPolicy == IMPORT_POLICY_RESTORE) {
			showDialog(DIALOG_ID_WARN_RESTORE_POLICY);
		} else {
			startImportPostCheck();
		}
    }
    
    public void startImportPostCheck() {
    	// First delete old lists
    	//getContentResolver().delete(Shopping.Contains.CONTENT_URI, null, null);
    	//getContentResolver().delete(Shopping.Items.CONTENT_URI, null, null);
    	//getContentResolver().delete(Shopping.Lists.CONTENT_URI, null, null);
    	

    	String fileName = getFilenameAndSavePreferences();
    	
    	Log.i(TAG, "Importing...");
    	
    	final File file = new File(fileName);
		if (true) { // (!file.exists()) {
			
			// If this is the RESTORE policy, make sure we let the user know
			// what kind of trouble he's getting himself into.
			switchToConvertLayout();
			smHasWorkerThread = true;
			
			new Thread() {
				public void run() {
					try{						
						Reader reader;
						
						Encoding enc = getCurrentEncoding();
						if (enc == null){
							reader = new InputStreamReader(new FileInputStream(file));
						} else {
							reader = new InputStreamReader(new FileInputStream(file), enc.name());
						}

						smProgressMax = (int) file.length();
						((ProgressBar) findViewById(R.id.Progress)).setMax(smProgressMax);
						
						doImport(reader);
						
						reader.close();
						dispatchSuccess(R.string.import_finished);
//						finish();
						
					} catch (FileNotFoundException e) {
						dispatchError(R.string.error_file_not_found);
						Log.i(TAG, "File not found", e);
					} catch (IOException e) {
						dispatchError(R.string.error_reading_file);
						Log.i(TAG, "IO exception", e);
					} catch (WrongFormatException e) {
						dispatchError(R.string.wrong_csv_format);
						Log.i(TAG, "array index out of bounds", e);
					}

					smHasWorkerThread = false;
				}
			}.start();
		}
    }
    
    protected Encoding getCurrentEncoding() {
    	if (mCustomEncoding.isChecked()){	
    		return (Encoding)mSpinnerEncoding.getSelectedItem();    		
    	} else {
    		return getDefaultEncoding();
    	}
	}

	protected Encoding getDefaultEncoding() {
		return Encoding.UTF_8;
	}

	void displayMessage(int message, boolean success) {
    	// Just make a toast instead?
		//Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		//finish();

    	new AlertDialog.Builder(this)
    		.setIcon((success) ? android.R.drawable.ic_dialog_info : android.R.drawable.ic_dialog_alert)
    		.setMessage(message)
    		.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {
					finish();
    			}
    		})
    		.show();
    }
    
    void setConversionProgress(int newProgress) {
    	((ProgressBar) findViewById(R.id.Progress)).setProgress(newProgress);
    }
    
    void setMaxProgress(int maxProgress) {
    	((ProgressBar) findViewById(R.id.Progress)).setMax(maxProgress);
    }
    
    static public void dispatchSuccess(int successMsg) {
    	dispatchMessage(MESSAGE_SUCCESS, successMsg);
    }
    
    static public void dispatchError(int errorMsg) {
    	dispatchMessage(MESSAGE_ERROR, errorMsg);
    }
    
    static public void dispatchConversionProgress(int newProgress) {
    	dispatchMessage(MESSAGE_SET_PROGRESS, newProgress);
    }
    
    static public void dispatchSetMaxProgress(int maxProgress) {
    	dispatchMessage(MESSAGE_SET_MAX_PROGRESS, maxProgress);
    }
    
    static void dispatchMessage(int what, int argument) {
    	// Cache the handler since the other thread could modify it at any time.
    	Handler handler = smCurrentHandler;
    	
    	if (handler != null) {
    		Message msg = Message.obtain(handler, what, argument, 0);
    		handler.sendMessage(msg);
    	}
    }
    
    /**
	 * @param reader
	 * @throws IOException
	 */
	public void doImport(Reader reader) throws IOException,
				WrongFormatException {
	
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
		String fileName = getFilenameAndSavePreferences();
    	final File file = new File(fileName);

		switchToConvertLayout();
		smHasWorkerThread = true;
		
		new Thread() {
			public void run() {
				try{
					Writer writer;
					Encoding enc = getCurrentEncoding();
					if (enc == null){
						writer = new OutputStreamWriter(new FileOutputStream(file));
					} else {
						writer = new OutputStreamWriter(new FileOutputStream(file), enc.name());
					}


					doExport(writer);
					
					writer.close();
					dispatchSuccess(R.string.export_finished);
//					finish();
					
				} catch (IOException e) {
					dispatchError(R.string.error_writing_file);
					Log.i(TAG, "IO exception", e);
				}

				smHasWorkerThread = false;
			}
		}.start();
	}
    
	/**
	 * @param writer
	 * @throws IOException
	 */
	public void doExport(Writer writer) throws IOException {
	
	}
	
	/**
	 * @return
	 */
	public String getFilenameAndSavePreferences() {

		String fileName = mEditText.getText().toString();
		
		SharedPreferences prefs = PreferenceManager
		.getDefaultSharedPreferences(this);
		Editor editor = prefs.edit();
		editor.putString(PREFERENCE_FILENAME, fileName);
		editor.putString(PREFERENCE_FORMAT, getFormat());
		if (mCustomEncoding.isChecked()){
			editor.putString(PREFERENCE_ENCODING, ((Encoding)mSpinnerEncoding.getSelectedItem()).name());
		}
		editor.putBoolean(PREFERENCE_USE_CUSTOM_ENCODING, mCustomEncoding.isChecked());
		editor.commit();

		return fileName;
	}
	
	public String getFormat() {
		int id = mSpinner.getSelectedItemPosition();
		if (id != Spinner.INVALID_POSITION) {
			return mFormatValues[id];
		}
		return DEFAULT_FORMAT;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		// Let's not let the user mess around while we're busy.
		if (!smHasWorkerThread) {
			menu.add(0, MENU_SETTINGS, 0, R.string.menu_settings).setShortcut(
					'1', 's').setIcon(android.R.drawable.ic_menu_preferences);
			
	 		// Add distribution menu items last.
	 		mDistribution.onCreateOptionsMenu(menu);
		}
		
		return true;
	}
	
    @Override
    public void onResume() {
    	super.onResume();
    	
    	displayImportPolicy();
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
			
		case DIALOG_ID_WARN_RESTORE_POLICY:
			return new AlertDialog.Builder(this)
					.setTitle(R.string.warn_restore_policy_title)
					.setMessage(R.string.warn_restore_policy)
					.setPositiveButton(android.R.string.yes, new OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							startImportPostCheck();
						}
					})
					.setNegativeButton(android.R.string.no, null)
					.create();
					
		case DIALOG_ID_NO_FILE_MANAGER_AVAILABLE:
			return new DownloadOIAppDialog(this,
					DownloadOIAppDialog.OI_FILEMANAGER);
		}
		return super.onCreateDialog(id);
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
	
	private void openFileManager() {
		String fileName = mEditText.getText().toString();
		
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
		intent.setData(Uri.parse("file://" + fileName));
		
		if (RES_STRING_FILEMANAGER_TITLE != 0) {
			intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(RES_STRING_FILEMANAGER_TITLE));
		}
		if (RES_STRING_FILEMANAGER_BUTTON_TEXT != 0) {
			intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(RES_STRING_FILEMANAGER_BUTTON_TEXT));
		}
		
		
		try {
			startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
		} catch (ActivityNotFoundException e) {
			showDialog(DIALOG_ID_NO_FILE_MANAGER_AVAILABLE);
		}
	}

	/**
	 * Prepends the system's SD card path to the file name.
	 * @param filename
	 * @return
	 */
    protected String getSdCardFilename(String filename) {
    	String sdpath = android.os.Environment
				.getExternalStorageDirectory().getAbsolutePath();
    	String path;
    	if (sdpath.substring(sdpath.length() - 1, sdpath.length()).equals("/")) {
    		path = sdpath + filename;
    	} else {
    		path = sdpath + "/" + filename;
    	}
	   	return path;
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		Log.i(TAG, "onActivityResult");

		switch (requestCode) {
		case REQUEST_CODE_PICK_FILE:
			if (resultCode == RESULT_OK && data != null) {
				// obtain the filename
				String filename = data.getDataString();
				if (filename != null) {
					if (filename.startsWith("file://")) {
						filename = filename.substring(7);
					}
					
					mEditText.setText(filename);
				}				
				
			}
			break;
		}
	}
}
