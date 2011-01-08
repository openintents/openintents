/* 
 * Copyright (C) 2007-2009 OpenIntents.org
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

package org.openintents.countdown.automation;

import org.openintents.countdown.LogConstants;
import org.openintents.countdown.R;
import org.openintents.countdown.db.Countdown.Durations;
import org.openintents.intents.AutomationIntents;
import org.openintents.intents.CountdownIntents;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class EditAutomationActivity extends Activity {
	private static final String TAG = LogConstants.TAG;
	private static final boolean debug = LogConstants.debug;
	
	private static final int REQUEST_CODE_PICK_COUNTDOWN = 1;
	
	private static final String BUNDLE_ACTION = "action";
	private static final String BUNDLE_COUNTDOWN_URI = "countdown";
	
	TextView mTextCommand;
	TextView mTextSelectAction;
	TextView mTextSelectCountdown;
	Spinner mSpinnerAction;
	Button mButtonOk;
	Button mButtonCountdown;
	
	String mDescriptionAction;
	String mDescriptionCountdown;
	
	private Uri mUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.automation_settings);
		
		mUri = null;
		mDescriptionCountdown = "?";
		
        mSpinnerAction = (Spinner) findViewById(R.id.spinner_action);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.automation_actions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerAction.setAdapter(adapter);
        
        mSpinnerAction.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mDescriptionAction = getResources().getStringArray(R.array.automation_actions)[position];
				
				updateTextViews();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
        	
        });
        
        mButtonCountdown = (Button) findViewById(R.id.button_countdown);
        mButtonCountdown.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pickCountdown();
			}
        	
        });
        
        mButtonOk = (Button) findViewById(R.id.button_ok);
        mButtonOk.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doOk();
			}
        	
        });
        
        mButtonOk.setEnabled(false);
        
        Button b = (Button) findViewById(R.id.button_cancel);
        b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doCancel();
			}
        	
        });
        
        mTextCommand = (TextView) findViewById(R.id.command);
        mTextSelectAction = (TextView) findViewById(R.id.select_action);
        mTextSelectCountdown = (TextView) findViewById(R.id.select_countdown);

        if (savedInstanceState != null) {
        	if (savedInstanceState.containsKey(BUNDLE_ACTION)) {
        		int i = savedInstanceState.getInt(BUNDLE_ACTION);
        		mSpinnerAction.setSelection(i);
        	}
        	if (savedInstanceState.containsKey(BUNDLE_COUNTDOWN_URI)) {
        		mUri = Uri.parse(savedInstanceState.getString(BUNDLE_COUNTDOWN_URI));
        		setCountdownFromUri();
        	}
        	
        } else {
			final Intent intent = getIntent();
			
			if (intent != null) {
				String action = intent.getStringExtra(CountdownIntents.EXTRA_ACTION);
				
				if (CountdownIntents.TASK_START_COUNTDOWN.equals(action)) {
					mSpinnerAction.setSelection(0);
				} else if  (CountdownIntents.TASK_STOP_COUNTDOWN.equals(action)) {
					mSpinnerAction.setSelection(1);
				} else {
					// set default
					mSpinnerAction.setSelection(0);
				}
				
				// Get countdown:
	
				final String dataString = intent.getStringExtra(CountdownIntents.EXTRA_DATA);
				if (dataString != null) {
					mUri = Uri.parse(dataString);
				}
				setCountdownFromUri();
			}
        }
        
        updateTextViews();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if (debug) Log.i(TAG, "onPause");
		
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(BUNDLE_ACTION, mSpinnerAction.getSelectedItemPosition());
		if (mUri != null) {
			outState.putString(BUNDLE_COUNTDOWN_URI, mUri.toString());
		}
	}

	void pickCountdown() {
		Intent i = new Intent(Intent.ACTION_PICK);
		i.setData(Durations.CONTENT_URI);
		
		startActivityForResult(i, REQUEST_CODE_PICK_COUNTDOWN);
	}
	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		

		if (requestCode == REQUEST_CODE_PICK_COUNTDOWN
				&& resultCode == RESULT_OK) {
			mUri = intent.getData();
			
			setCountdownFromUri();
		}
		
		updateTextViews();
	}

	private void setCountdownFromUri() {
		mDescriptionCountdown = "";
		
		if (mUri != null) {
			mButtonOk.setEnabled(true);
			
			// Get name of countdown from content provider
			Cursor c = getContentResolver().query(mUri, new String[] {Durations._ID, Durations.TITLE, Durations.AUTOMATE_TEXT}, null, null, null);
			
			if (c != null && c.moveToFirst()) {
				mDescriptionCountdown = c.getString(1);
				
				if (TextUtils.isEmpty(mDescriptionCountdown)) {
					// Use automation command as alternative title
					mDescriptionCountdown = c.getString(2);
				}
			}
			if (c != null) {
				c.close();
			}
		} 

		if (TextUtils.isEmpty(mDescriptionCountdown)) {
			mDescriptionCountdown = getString(android.R.string.untitled);
		}
	}
	
	void doOk() {
		updateResult();
		finish();
	}

	void doCancel() {
		setResult(RESULT_CANCELED);
		finish();
	}
	
	void updateResult() {
		Intent intent = new Intent();
		
		long id = mSpinnerAction.getSelectedItemId();
		if (id == 0) {
			intent.putExtra(CountdownIntents.EXTRA_ACTION, CountdownIntents.TASK_START_COUNTDOWN);
		} else if (id == 1) {
			intent.putExtra(CountdownIntents.EXTRA_ACTION, CountdownIntents.TASK_STOP_COUNTDOWN);
		}
		intent.putExtra(CountdownIntents.EXTRA_DATA, mUri.toString());
		
		String description = mDescriptionAction + ": " + mDescriptionCountdown;
		intent.putExtra(AutomationIntents.EXTRA_DESCRIPTION, description);
		
		if (debug) Log.i(TAG, "Created intent (URI)   : " + intent.toURI());
		if (debug) Log.i(TAG, "Created intent (String): " + intent.toString());
		
		setResult(RESULT_OK, intent);
	}
	
	void updateTextViews() {
		mTextCommand.setText(mDescriptionAction + ": " + mDescriptionCountdown);
		//mTextSelectAction.setText(getString(R.string.select_action, mDescriptionAction));
		//mTextSelectCountdown.setText(getString(R.string.select_countdown, mDescriptionCountdown));
		mTextSelectAction.setText(getString(R.string.select_action, ""));
		mTextSelectCountdown.setText(getString(R.string.select_countdown, ""));
		mButtonCountdown.setText(mDescriptionCountdown);
	}
}
