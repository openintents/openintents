package org.openintents.calendarpicker.activity.prefs;

import org.openintents.calendarpicker.R;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class CalendarDisplayPreferences extends PreferenceActivity {


	public final static String PREFKEY_ENABLE_TRANSPARENCY = "enable_transparency";

	public final static boolean DEFAULT_ENABLE_TRANSPARENCY = true;

	
	
	public final static String SHARED_PREFS_NAME = "calendar_display_prefs";
	
	// ========================================================================
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
//        getPreferenceManager().setSharedPreferencesName(SHARED_PREFS_NAME);
        addPreferencesFromResource(R.xml.calendar_display_settings);
    }
}
