package org.openintents.notepad.crypto;

import org.openintents.distribution.GetFromMarketDialog;
import org.openintents.distribution.RD;
import org.openintents.intents.CryptoIntents;
import org.openintents.notepad.NotePadIntents;
import org.openintents.notepad.R;
import org.openintents.notepad.NotePad.Notes;
import org.openintents.notepad.filename.DialogHostingActivity;
import org.openintents.notepad.filename.FilenameDialog;
import org.openintents.util.IntentUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * Encrypt or unencrypt (i.e. remove encryption) a note.
 * 
 * @author Peli
 *
 */
public class EncryptActivity extends Activity {

    private static final String TAG = "EncryptActivity";
    
	public static final int DIALOG_ID_GET_FROM_MARKET = 1;
    
	private static final int REQUEST_CODE_ENCRYPT_OR_UNENCRYPT = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		Log.i(TAG, "EncryptActivity: onCreate");
		
		Intent i = getIntent();

		i.setComponent(null);
		String action = i.getStringExtra(NotePadIntents.EXTRA_ACTION);
		
		// action should be either ENCRYPT or DECRYPT
		if (!action.equals(CryptoIntents.ACTION_ENCRYPT) 
				&& !action.equals(CryptoIntents.ACTION_DECRYPT) ) {
			
			// Unknown action
			Log.i(TAG, "Unknown action supplied: " + action);
			finish();
			return;
		}
		
		i.setAction(action);
		// Extras should have been set properly by the calling activity
		// and are not changed here.
		
		if (IntentUtils.isIntentAvailable(this, i)) {
	        try {
	    		Log.i(TAG, "EncryptActivity: startActivity");
	        	startActivityForResult(i, REQUEST_CODE_ENCRYPT_OR_UNENCRYPT);
	        } catch (ActivityNotFoundException e) {
				Toast.makeText(this,
						R.string.encryption_failed,
						Toast.LENGTH_SHORT).show();
				Log.e(TAG, "failed to invoke encrypt");
	        }
		} else {
			// Intent does not exist.
        	showDialog(DIALOG_ID_GET_FROM_MARKET);
		}
        
        

		Log.i(TAG, "EncryptActivity: startActivity OK");
	}
	
	/**
	 * Returns an object to be used in EXTRA_TEXT_ARRAY in a well-defined order.
	 * 
	 * @param text
	 * @param title
	 * @param tags
	 * @return
	 */
	public static String[] getCryptoStringArray(String text, String title, String tags) {
		return new String[] {text, title, tags};
	}

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	Log.i(TAG, "EncryptActivity: Received requestCode " + requestCode + ", resultCode " + resultCode);
    	switch(requestCode) {
    	case REQUEST_CODE_ENCRYPT_OR_UNENCRYPT:
    		if (resultCode == RESULT_OK && data != null) {
    			// Depending on the action, textArray contains either encrypted or 
    			// decrypted information.
    			String[] textArray = data.getStringArrayExtra(CryptoIntents.EXTRA_TEXT_ARRAY);
    			String text = textArray[0];
    			String title = textArray[1];
    			String tags = textArray[2];
    			String action = data.getAction();
    			
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

    	    	Log.i(TAG, "Updating" + uri + ", encrypted text " + text + ", tags " + tags);
    			// Write this to content provider:

                ContentValues values = new ContentValues();
                values.put(Notes.MODIFIED_DATE, System.currentTimeMillis());
                // Only update values that have been specifically set
                if (title != null) {
                	values.put(Notes.TITLE, title);
                }
                if (text != null) {
                	values.put(Notes.NOTE, text);
                }
                if (tags != null) {
                	values.put(Notes.TAGS, tags);
                }
                if (action.equals(CryptoIntents.ACTION_ENCRYPT)) {
                	values.put(Notes.ENCRYPTED, 1);
                } else if (action.equals(CryptoIntents.ACTION_DECRYPT)) {
                	values.put(Notes.ENCRYPTED, 0);
                } else {
        	    	Log.i(TAG, "Wrong action");
    				Toast.makeText(this,
        					"Encrypted information incomplete",
        					Toast.LENGTH_SHORT).show();
    				return;
                }
                
                getContentResolver().update(uri, values, null, null);
                
                // we are done
                finish();
                
    		} else {
    			Toast.makeText(this,
    					"Failed to invoke encrypt",
    					Toast.LENGTH_SHORT).show();
    			
    			finish();
    		}
    		break;
    	}
    }
    

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_ID_GET_FROM_MARKET:
			return new GetFromMarketDialog(this, 
					RD.string.safe_not_available,
					RD.string.safe_get_oi_filemanager,
					RD.string.safe_market_uri,
					RD.string.safe_developer_uri);

		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		FilenameDialog fd;
		
		dialog.setOnDismissListener(mDismissListener);
		
		switch (id) {
		case DIALOG_ID_GET_FROM_MARKET:
			break;
		}
	}
	
	OnDismissListener mDismissListener = new OnDismissListener() {
		
		public void onDismiss(DialogInterface dialoginterface) {
			finish();
		}
		
	};

}
