package org.openintents.notepad.crypto;

import org.openintents.intents.CryptoIntents;
import org.openintents.notepad.NotePadIntents;
import org.openintents.notepad.R;
import org.openintents.notepad.NotePad.Notes;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class EncryptActivity extends Activity {

    private static final String TAG = "EncryptActivity";
    
	private static final int REQUEST_CODE_ENCRYPT = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Log.i(TAG, "EncryptActivity: onCreate");
		
		Intent i = getIntent();

		i.setComponent(null);
		i.setAction(CryptoIntents.ACTION_ENCRYPT);
		// Extras should have been set properly by the calling activity
		// and are not changed here.
        
        try {
    		Log.i(TAG, "EncryptActivity: startActivity");
        	startActivityForResult(i, REQUEST_CODE_ENCRYPT);
        } catch (ActivityNotFoundException e) {
			Toast.makeText(this,
					R.string.encryption_failed,
					Toast.LENGTH_SHORT).show();
			Log.e(TAG, "failed to invoke encrypt");
        }
        

		Log.i(TAG, "EncryptActivity: startActivity OK");
	}
	
	

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	Log.i(TAG, "EncryptActivity: Received requestCode " + requestCode + ", resultCode " + resultCode);
    	switch(requestCode) {
    	case REQUEST_CODE_ENCRYPT:
    		if (resultCode == RESULT_OK && data != null) {
    			String[] encryptedTextArray = data.getStringArrayExtra(CryptoIntents.EXTRA_TEXT_ARRAY);
    			String encryptedText = encryptedTextArray[0];
    			String encryptedTitle = encryptedTextArray[1];
    			
    			String uristring = data.getStringExtra(NotePadIntents.EXTRA_URI);
    			Uri uri = null;
    			if (uristring != null) {
    				uri = Uri.parse(uristring);
    			} else {
        	    	Log.i(TAG, "Wrong extra uri");
    				Toast.makeText(this,
        					"Encrypted information incomplete",
        					Toast.LENGTH_SHORT).show();
    				return;
    			}

    	    	Log.i(TAG, "Updating" + uri + ", encrypted text " + encryptedText);
    			// Write this to content provider:

                ContentValues values = new ContentValues();
                values.put(Notes.MODIFIED_DATE, System.currentTimeMillis());
                values.put(Notes.TITLE, encryptedTitle);
                values.put(Notes.NOTE, encryptedText);
                values.put(Notes.ENCRYPTED, 1);
                
                //Uri noteUri = ContentUris.withAppendedId(getIntent().getData(), id);
                Uri noteUri = getIntent().getData();
                
                getContentResolver().update(uri, values, null, null);
                
                // we are done
                finish();
                
    		} else {
    			Toast.makeText(this,
    					"Failed to invoke encrypt",
    					Toast.LENGTH_SHORT).show();
    		}
    		break;
    	}
    }

}
