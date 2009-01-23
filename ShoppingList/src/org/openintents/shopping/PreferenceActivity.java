package org.openintents.shopping;


import org.openintents.provider.Shopping.Contains;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class PreferenceActivity extends android.preference.PreferenceActivity {

	public static final String PREFS_SORTORDER = "sortorder";
	public static final String PREFS_SORTORDER_DEFAULT = "3";
	public static final String PREFS_FONTSIZE = "fontsize";
	public static final String PREFS_FONTSIZE_DEFAULT = "2";
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
	}
	

	/**
	 * Returns the sort order for the notes list based on the user preferences.
	 * Performs error-checking.
	 * 
	 * @param context The context to grab the preferences from.
	 */
	static public String getSortOrderFromPrefs(Context context) {
		int sortOrder = 0;
		try {
			sortOrder = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
			.getString(PREFS_SORTORDER, PREFS_SORTORDER_DEFAULT));
		} catch (NumberFormatException e) {
			// Guess somebody messed with the preferences and put a string into this
			// field. We'll use the default value then.
		}
		
		if (sortOrder >= 0 && sortOrder < Contains.SORT_ORDERS.length)
		{
			return Contains.SORT_ORDERS[sortOrder];
		}
		
		// Value out of range - somebody messed with the preferences.
		return Contains.SORT_ORDERS[0];
	}
}
