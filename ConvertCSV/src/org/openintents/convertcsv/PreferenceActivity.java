package org.openintents.convertcsv;

import android.os.Bundle;

public class PreferenceActivity extends android.preference.PreferenceActivity {
	public static final String PREFS_NOTEPAD_FILENAME = "notepad_filename";
	public static final String PREFS_SHOPPINGLIST_FILENAME = "shoppinglist_filename";
	public static final String PREFS_ASK_IF_FILE_EXISTS = "ask_if_file_exists";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}
}
