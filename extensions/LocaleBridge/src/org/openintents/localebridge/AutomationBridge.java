package org.openintents.localebridge;

import java.net.URISyntaxException;

import org.openintents.compatibility.activitypicker.DialogHostingActivity;
import org.openintents.intents.AutomationIntents;
import org.openintents.intents.LocaleBridgeIntents;
import org.openintents.utils.SDKVersion;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * Present OI Automation tasks to Locale.
 * 
 * @author Peli
 *
 */
public class AutomationBridge extends Activity {
	private static final String TAG = "LocaleBridge";
	private static final boolean debug = true;
	
	final static int REQUEST_PICK_AUTOMATION_TASK = 1;
	final static int REQUEST_SET_AUTOMATION_TASK = 2;
	
	ComponentName mAutomationTaskActivity;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(TAG, "onCreate()");
        
        if (savedInstanceState != null) {
        	
        } else {
        	// New instance.

            final Intent intent = getIntent();
            
            if (intent.hasExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_INTENT)) {
            	Intent automationIntent = convertLocaleIntent2AutomationIntent(intent);
            	if (automationIntent != null) {
            		Log.i(TAG, "Start activity for result 1");
            		startActivityForResult(automationIntent, REQUEST_SET_AUTOMATION_TASK);
            	} else {
            		// Some error occured
            		setResult(RESULT_CANCELED);
            		Log.i(TAG, "finish 1");
            		finish();
            	}
            } else {
            	// Select new locale intent
            	selectAutomationTask();
            }
        }
        
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

        Log.i(TAG, "onSaveInstanceState()");
	}

	/**
     * Show PICK_ACTIVITY dialog with all possible Locale settings 
     */
	private void selectAutomationTask() {
		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT,
                new Intent(AutomationIntents.ACTION_EDIT_AUTOMATION_SETTINGS));
        pickIntent.putExtra(Intent.EXTRA_TITLE,
                getText(R.string.title_select_automation_task));

        if (SDKVersion.SDKVersion < 3) {
        	if (debug) Log.i(TAG, "Compatibility mode for ActivityPicker");
            // SDK 1.1 backward compatibility:
            // We launch our own version of ActivityPicker:
            pickIntent.setClass(this, DialogHostingActivity.class);
            pickIntent.putExtra(DialogHostingActivity.EXTRA_DIALOG_ID, 
            			DialogHostingActivity.DIALOG_ID_ACTIVITY_PICKER);
        } else {
        	if (debug) Log.i(TAG, "Call system ActivityPicker");
        }
        
		Log.i(TAG, "Start activity for result 2");
        startActivityForResult(pickIntent, REQUEST_PICK_AUTOMATION_TASK);
	}
    
    

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		Log.i(TAG, "onActivityResult: " + requestCode + ", " + resultCode);
		//Log.i(TAG, "data: " + data.toURI());

		if (resultCode == RESULT_OK) {
			switch(requestCode) {
			case REQUEST_PICK_AUTOMATION_TASK:
        		Log.i(TAG, "Start activity for result 3");
				startActivityForResult(data, REQUEST_SET_AUTOMATION_TASK);
				break;
			case REQUEST_SET_AUTOMATION_TASK:
				convertAndReturnIntent(data);
				break;
			}
		}
		
		// If we reach this point, some error occured
		if (requestCode == REQUEST_SET_AUTOMATION_TASK) {
			switch (resultCode) {
			case RESULT_OK:
				// see above.
				break;
			case RESULT_CANCELED:
			default:
				setResult(RESULT_CANCELED);
    			Log.i(TAG, "finish 2");
				finish();
				break;
			}
		}
		
	}
	
	/**
	 * 
	 * @param intent
	 */
	void convertAndReturnIntent(Intent intent) {
		Log.i(TAG, "Converting and returning");
		Intent automationIntent = convertAutomationIntent2LocaleIntent(intent);
		setResult(RESULT_OK, automationIntent);
		Log.i(TAG, "finish 3");
		finish();
	}
	
	public Intent convertAutomationIntent2LocaleIntent(Intent automationIntent) {
		Log.i(TAG, "Converting automation intent: " + automationIntent.toURI());
		
		// Return new intent for Locale, starting from the broadcast intent
		Intent broadcastIntent;
		try {
			broadcastIntent = Intent.getIntent(automationIntent.getStringExtra(AutomationIntents.EXTRA_BROADCAST_INTENT));
		} catch (URISyntaxException e) {
			// if it did not work, return null
			return null;
		}
		// No need to create new, because getIntent() above does this already.
		Intent localeIntent = broadcastIntent;
		
		// Use the action to set the broadcast action
		String actionFire = broadcastIntent.getAction();
		localeIntent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_ACTION_FIRE, actionFire);
		
		// Store the original automation intent safely:
		final String storedAutomationIntent = automationIntent.toURI();
		localeIntent.putExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_INTENT, storedAutomationIntent);
		
		// Convert description to blurb
		String description = automationIntent.getStringExtra(AutomationIntents.EXTRA_DESCRIPTION);
		localeIntent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_BLURB, description);
		
		// Extract icon:
		

		Log.i(TAG, "into automation intent: " + localeIntent.toURI());
		
		return localeIntent;
	}
	
	public Intent convertLocaleIntent2AutomationIntent(Intent localeIntent) {
		
		// Extract the original Locale intent:
		String encodedIntent = localeIntent.getStringExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_INTENT);
		Log.i(TAG, "Converting automation intent: " + encodedIntent);
		
		Intent automationIntent;
		try {
			automationIntent = Intent.getIntent(encodedIntent);
		} catch (URISyntaxException e) {
			// Error decoding the original intent
			return null;
		}

		Log.i(TAG, "into Locale intent: " + automationIntent.toURI());
		
		return automationIntent;
	}
	
}