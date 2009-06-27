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
 * Present Locale settings to applications that only understand OI automation tasks.
 * 
 * @author Peli
 *
 */
public class AutomationBridge extends Activity {
	private static final String TAG = LogConstants.TAG;
	private static final boolean debug = LogConstants.debug;
	
	final static int REQUEST_PICK_LOCALE_SETTING = 1;
	final static int REQUEST_SET_LOCALE_SETTING = 2;
	
	final static String BUNDLE_COMPONENT_NAME = "component";
	
	ComponentName mLocaleSettingActivity;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(TAG, "onCreate()");
        
        if (savedInstanceState != null) {
        	if (savedInstanceState.containsKey(BUNDLE_COMPONENT_NAME)) {
	        	String componentString = savedInstanceState.getString(BUNDLE_COMPONENT_NAME);
	        	mLocaleSettingActivity = ComponentName.unflattenFromString(componentString);
        	}
        } else {
        	// New instance.

            final Intent intent = getIntent();
            
            if (intent.hasExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_INTENT)) {
            	Intent localeIntent = convertAutomationIntent2LocaleIntent(intent);
            	if (localeIntent != null) {
            		Log.i(TAG, "Start activity for result 1");
            		startActivityForResult(localeIntent, REQUEST_SET_LOCALE_SETTING);
            	} else {
            		// Some error occured
            		setResult(RESULT_CANCELED);
            		Log.i(TAG, "finish 1");
            		finish();
            	}
            } else {
            	// Select new locale intent
            	selectLocaleSetting();
            }
        }
        
    }

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

        Log.i(TAG, "onSaveInstanceState()");
		
		if (mLocaleSettingActivity != null) {
			String componentString = mLocaleSettingActivity.flattenToString();
			outState.putString(BUNDLE_COMPONENT_NAME, componentString);
		}
	}

	/**
     * Show PICK_ACTIVITY dialog with all possible Locale settings 
     */
	private void selectLocaleSetting() {
		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT,
                new Intent(com.twofortyfouram.Intent.ACTION_EDIT_SETTING));
        pickIntent.putExtra(Intent.EXTRA_TITLE,
                getText(R.string.title_select_locale_setting));

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
        startActivityForResult(pickIntent, REQUEST_PICK_LOCALE_SETTING);
	}
    
    

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		//Log.i(TAG, "onActivityResult: " + requestCode + ", " + resultCode);
		//Log.i(TAG, "data: " + data.toURI());

		if (resultCode == RESULT_OK) {
			switch(requestCode) {
			case REQUEST_PICK_LOCALE_SETTING:
				// Remember setting activity for later
				mLocaleSettingActivity = data.getComponent();
				
				// Add Locale specific breadcrumb:
				Intent intent = new Intent(data);
				intent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_BREADCRUMB, getString(R.string.app_name));
        		Log.i(TAG, "Start activity for result 3");
				startActivityForResult(intent, REQUEST_SET_LOCALE_SETTING);
				break;
			case REQUEST_SET_LOCALE_SETTING:
				convertAndReturnIntent(data);
				break;
			}
		}
		
		// If we reach this point, some error occured
		if (requestCode == REQUEST_SET_LOCALE_SETTING) {
			switch (resultCode) {
			case RESULT_OK:
				// see above.
				break;
			case RESULT_CANCELED:
			case com.twofortyfouram.Intent.RESULT_REMOVE:
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
		Intent automationIntent = convertLocaleIntent2AutomationIntent(intent);
		setResult(RESULT_OK, automationIntent);
		Log.i(TAG, "finish 3");
		finish();
	}
	
	public Intent convertLocaleIntent2AutomationIntent(Intent localeIntent) {
		Log.i(TAG, "Converting locale intent: " + localeIntent.toURI());
		
		// Call again this bridge class when modifying settings:
		Intent automationIntent = new Intent(this, AutomationBridge.class);
		
		// Store the original locale intent safely:
		final String storedLocaleIntent = localeIntent.toURI();
		automationIntent.putExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_INTENT, storedLocaleIntent);
		
		// Extract the broadcast action:
		String actionFire = localeIntent.getStringExtra(com.twofortyfouram.Intent.EXTRA_STRING_ACTION_FIRE);
		
		ComponentName cn = null;
		if (actionFire != null) {
			// Compatibility with old Locale intent definition
			cn = LocaleUtils.getRunAutomationComponent(this, localeIntent, mLocaleSettingActivity, actionFire);
		} else {
			// New Locale intent definition
			cn = LocaleUtils.getRunAutomationComponent(this, localeIntent, mLocaleSettingActivity);
		}
		if (cn != null) {
			automationIntent.putExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_RUN_COMPONENT, 
					cn.flattenToString());
		}
		
		/*
		// Create a copy of localeIntent, but with the new action
		Intent broadcastIntent = new Intent(localeIntent);
		broadcastIntent.setAction(actionFire);
		
		// Set this as the action to be performed:
		automationIntent.putExtra(AutomationIntents.EXTRA_BROADCAST_INTENT, broadcastIntent.toURI());
		*/
		
		// Convert blurb to description
		String blurb = localeIntent.getStringExtra(com.twofortyfouram.Intent.EXTRA_STRING_BLURB);
		automationIntent.putExtra(AutomationIntents.EXTRA_DESCRIPTION, blurb);
		
		// Extract icon:
		

		// Store the component name to the locale intent:
		automationIntent.putExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_COMPONENT, 
				mLocaleSettingActivity.flattenToString());

		Log.i(TAG, "into automation intent: " + automationIntent.toURI());
		
		return automationIntent;
	}
	
	public Intent convertAutomationIntent2LocaleIntent(Intent automationIntent) {
		
		// Extract the original Locale intent:
		String encodedIntent = automationIntent.getStringExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_INTENT);
		Log.i(TAG, "Converting automation intent: " + encodedIntent);
		
		Intent localeIntent;
		try {
			localeIntent = Intent.getIntent(encodedIntent);
		} catch (URISyntaxException e) {
			// Error decoding the original intent
			return null;
		}

		// Add Locale-specific breadcrumb
		localeIntent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_BREADCRUMB, getString(R.string.app_name));

		// Extract the component
		mLocaleSettingActivity = ComponentName.unflattenFromString(
				automationIntent.getStringExtra(LocaleBridgeIntents.EXTRA_LOCALE_BRIDGE_COMPONENT));
		
		// Set this component in the newly created intent:
		localeIntent.setComponent(mLocaleSettingActivity);

		Log.i(TAG, "into Locale intent: " + localeIntent.toURI());
		
		return localeIntent;
	}
	
}