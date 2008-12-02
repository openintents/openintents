package org.openintents.shopping;

import android.os.Bundle;

public class PreferenceActivity extends android.preference.PreferenceActivity {
	public static final String PREFS_EXPORT_REPLACE_BR = "export_replace_br";
	public static final String PREFS_EXPORT_FILENAME = "export_filename";
	public static final String PREFS_EXPORT_COMPLETED_ONLY = "export_complete_only";
	public static final String PREFS_EXPORT_DATE_FORMAT = "export_date_format";
	public static final String PREFS_EXPORT_TIME_FORMAT = "export_time_format";
	public static final String PREFS_START_JOBS_IMMEDIATELY = "start_jobs_immediately";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}
}
