package org.openintents.localebridge;

import java.net.URISyntaxException;

import org.openintents.compatibility.activitypicker.DialogHostingActivity;
import org.openintents.intents.AutomationIntents;
import org.openintents.intents.LocaleBridgeIntents;
import org.openintents.localebridge.util.LocaleUtils;
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
public class LocaleBridge extends Activity {
	private static final String TAG = "LocaleBridge";
	private static final boolean debug = true;
	
	final static int REQUEST_PICK_AUTOMATION_TASK = 1;
	final static int REQUEST_SET_AUTOMATION_TASK = 2;

	final static String BUNDLE_COMPONENT_NAME = "component";
	
	ComponentName mEditAutomationActivity;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(TAG, "onCreate()");
        
        if (savedInstanceState != null) {
        	if (savedInstanceState.containsKey(BUNDLE_COMPONENT_NAME)) {
	        	String componentString = savedInstanceState.getString(BUNDLE_COMPONENT_NAME);
	        	mEditAutomationActivity = ComponentName.unflattenFromString(componentString);
        	}
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

		if (mEditAutomationActivity != null) {
			String componentString = mEditAutomationActivity.flattenToString();
			outState.putString(BUNDLE_COMPONENT_NAME, componentString);
		}
	}

	/**
     * Show PICK_ACTIVITY dialog with all possible Locale settings 
     */
	private void selectAutomationTask() {
		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT,
                new Intent(AutomationIntents.ACTION_EDIT_AUTOMATION));
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
				// Remember setting activity for later
				mEditAutomationActivity = data.getComponent();
				
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
		
		Intent localeIntent = new Intent();
		
		// Use the action to set the broadcast action
		// (for compatibility with old mode)
		localeIntent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_ACTION_FIRE, 
				com.twofortyfouram.Intent.ACTION_FIRE_SETTING);
		
		// Store the original automation intent safely:
		final String storedAutomationIntent = automationIntent.toURI();
		localeIntent.putExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_INTENT, storedAutomationIntent);
		
		// Store component for fire
		ComponentName cn = LocaleUtils.getRunAutomationComponent(this, localeIntent, 
				mEditAutomationActivity, AutomationIntents.ACTION_RUN_AUTOMATION);

		if (cn != null) {
			localeIntent.putExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_RUN_COMPONENT, 
					cn.flattenToString());
		}
		
		// Convert description to blurb
		String description = automationIntent.getStringExtra(AutomationIntents.EXTRA_DESCRIPTION);
		localeIntent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_BLURB, description);
		
		// Extract icon:
		

		// Store the component name to the locale intent:
		localeIntent.putExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_COMPONENT, 
				mEditAutomationActivity.flattenToString());
		
		Log.i(TAG, "Storing: " + mEditAutomationActivity.flattenToString());

		Log.i(TAG, "into locale intent: " + localeIntent.toURI());
		
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

		// Extract the component
		mEditAutomationActivity = ComponentName.unflattenFromString(
				localeIntent.getStringExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_COMPONENT));
		
		// Set this component in the newly created intent:
		automationIntent.setComponent(mEditAutomationActivity);

		Log.i(TAG, "into Locale intent: " + automationIntent.toURI());
		
		return automationIntent;
	}
	
}