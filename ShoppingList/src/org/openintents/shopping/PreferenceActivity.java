package org.openintents.shopping;

import android.os.Bundle;

public class PreferenceActivity extends android.preference.PreferenceActivity {

	public static final String PREFS_SORTORDER = "sortorder";
	public static final String PREFS_SORTORDER_DEFAULT = "contains.modified ASC";
	public static final String PREFS_FONTSIZE = "fontsize";
	public static final String PREFS_FONTSIZE_DEFAULT = "2";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}
}
