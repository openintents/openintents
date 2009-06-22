package org.openintents.countdown.automation;

import org.openintents.countdown.R;
import org.openintents.countdown.db.Countdown.Durations;
import org.openintents.intents.AutomationIntents;
import org.openintents.intents.CountdownIntents;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class AutomationSettingsActivity extends Activity {
	
	private static final String TAG = "AutomationSettings";
	
	private static final int REQUEST_CODE_PICK_COUNTDOWN = 1;
	
	TextView mText;
	Spinner mSpinnerAction;
	
	private Uri mUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.automation_settings);
		
        mSpinnerAction = (Spinner) findViewById(R.id.spinner_action);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.automation_actions, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerAction.setAdapter(adapter);
        
        Button b = (Button) findViewById(R.id.button_countdown);
        b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pickCountdown();
			}
        	
        });
        
        b = (Button) findViewById(R.id.button_ok);
        b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doOk();
			}
        	
        });
        
        b = (Button) findViewById(R.id.button_cancel);
        b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				doCancel();
			}
        	
        });
        
        mText = (TextView) findViewById(R.id.countdown);
        
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "onPause");
		
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
			
			// Set the name
			String title = "";
			Cursor c = getContentResolver().query(mUri, new String[] {Durations._ID, Durations.TITLE}, null, null, null);
			
			if (c != null & c.moveToFirst()) {
				title = c.getString(1);
			}
			
			mText.setText(title);
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
		// Call back exactly this class:
		Intent intent = new Intent(this, AutomationSettingsActivity.class);
		
		Intent broadcastIntent = new Intent();
		
		long id = mSpinnerAction.getSelectedItemId();
		if (id == 0) {
			broadcastIntent.setAction(CountdownIntents.ACTION_START_COUNTDOWN);
		} else if (id == 1) {
			broadcastIntent.setAction(CountdownIntents.ACTION_STOP_COUNTDOWN);
		}
		broadcastIntent.setData(mUri);
		
		intent.putExtra(AutomationIntents.EXTRA_BROADCAST_INTENT, broadcastIntent.toURI());
		
		intent.putExtra(AutomationIntents.EXTRA_DESCRIPTION, mText.getText().toString());
		
		Log.i(TAG, "Created intent (URI)   : " + intent.toURI());
		Log.i(TAG, "Created intent (String): " + intent.toString());
		
		setResult(RESULT_OK, intent);
	}
}
