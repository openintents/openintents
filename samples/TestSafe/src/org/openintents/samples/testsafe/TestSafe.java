package org.openintents.samples.testsafe;

import org.openintents.intents.CryptoIntents;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class TestSafe extends Activity {
	public final String TAG="SERVICE_TEST";
	public final Integer ENCRYPT_REQUEST = 1;
	public final Integer DECRYPT_REQUEST = 2;
	public final Integer GET_PASSWORD_REQUEST = 3;
	public final Integer SET_PASSWORD_REQUEST = 4;
	public final String desc = "opensocial";
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

		EditText inputText = (EditText) findViewById(R.id.input_entry);
		inputText.setText(desc,
			android.widget.TextView.BufferType.EDITABLE);
		
// ---------------- clicky
		Button encryptIntentButton = (Button) findViewById(R.id.encrypti);
		Button decryptIntentButton = (Button) findViewById(R.id.decrypti);
		Button getButton           = (Button) findViewById(R.id.get);
		Button setButton           = (Button) findViewById(R.id.set);
		Button outToInButton       = (Button) findViewById(R.id.outToIn);
		
		encryptIntentButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				clickMaster (ENCRYPT_REQUEST);
			}});
		decryptIntentButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				clickMaster (DECRYPT_REQUEST);
			}});
		getButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				clickMaster (GET_PASSWORD_REQUEST);
			}});
		setButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				clickMaster (SET_PASSWORD_REQUEST);
			}});
		//move output text box to input:
		outToInButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				EditText outputText = (EditText) findViewById(R.id.output_entry);
				EditText inputText = (EditText) findViewById(R.id.input_entry);
				String newInputStr = outputText.getText().toString();
				inputText.setText(newInputStr, android.widget.TextView.BufferType.EDITABLE);
				
			}});	
		
    }//oncreate
    
    private void clickMaster (Integer request) {
		EditText inputText = (EditText) findViewById(R.id.input_entry);
		String inputStr = inputText.getText().toString();
		
		Intent i = new Intent();
		i.putExtra(CryptoIntents.EXTRA_TEXT, inputStr);
		
    	if (request == ENCRYPT_REQUEST) {
            i.setAction(CryptoIntents.ACTION_ENCRYPT);
    	} else if (request == DECRYPT_REQUEST) {
            i.setAction(CryptoIntents.ACTION_DECRYPT);
    	} else if (request == GET_PASSWORD_REQUEST) {
    		i.putExtra(CryptoIntents.EXTRA_UNIQUE_NAME, inputStr);
    		i.setAction (CryptoIntents.ACTION_GET_PASSWORD);
    	} else if (request == SET_PASSWORD_REQUEST) {
    		String uniqueNameStr = ((EditText) findViewById(R.id.unique_name_entry)).getText().toString();
    		String passwordStr = ((EditText) findViewById(R.id.password_entry)).getText().toString();
    		String usernameStr = ((EditText) findViewById(R.id.username_entry)).getText().toString();


    		i.putExtra(CryptoIntents.EXTRA_UNIQUE_NAME, uniqueNameStr);
    		i.putExtra(CryptoIntents.EXTRA_PASSWORD, passwordStr);
    		i.putExtra(CryptoIntents.EXTRA_USERNAME, usernameStr);

    		i.setAction (CryptoIntents.ACTION_SET_PASSWORD);
    	}
        try {
        	startActivityForResult(i, request);
        } catch (ActivityNotFoundException e) {
			Log.e(TAG, "failed to invoke intent: " + e.toString());
        }
    }
    
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	String resultText = "";
    	if (resultCode != RESULT_OK) {
    		resultText = "An error occured while contacting apws.";
    	} else {
    		if (requestCode == ENCRYPT_REQUEST || requestCode == DECRYPT_REQUEST) {
    			resultText = data.getStringExtra (CryptoIntents.EXTRA_TEXT);
    		} else if (requestCode == SET_PASSWORD_REQUEST) {
    			resultText = "Request to set password sent.";
    		} else {
    			String uname = data.getStringExtra (CryptoIntents.EXTRA_USERNAME);
    			String pwd = data.getStringExtra (CryptoIntents.EXTRA_PASSWORD);
    			resultText = uname + ":" + pwd;
    		}
    	}
		EditText outputText = (EditText) findViewById(R.id.output_entry);
		outputText.setText(resultText, android.widget.TextView.BufferType.EDITABLE);
    }
}
