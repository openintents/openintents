package org.openintents.safe;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {

	public static final String PREFERENCE_ALLOW_EXTERNAL_ACCESS = "external_access";
	public static final String PREFERENCE_LOCK_TIMEOUT = "lock_timeout";
	public static final String PREFERENCE_LOCK_TIMEOUT_DEFAULT_VALUE = "5";
	public static final String PREFERENCE_FIRST_TIME_WARNING = "first_time_warning";
	public static final String PREFERENCE_KEYPAD = "keypad";
	public static final String PREFERENCE_KEYPAD_MUTE = "keypad_mute";
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		if (CategoryList.isSignedIn()==false) {
			finish();
			return;
		}

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
	protected void onResume() {
		super.onResume();

		if (CategoryList.isSignedIn()==false) {
			finish();
			return;
		}
	}

}
