package org.openintents.shopping;

import org.openintents.provider.Shopping.Contains;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.method.KeyListener;
import android.text.method.TextKeyListener;

public class PreferenceActivity extends android.preference.PreferenceActivity {

	public static final String PREFS_SORTORDER = "sortorder";
	public static final String PREFS_SORTORDER_DEFAULT = "3";
	public static final String PREFS_ROWSIZE = "fontsize";
	public static final String PREFS_ROWSIZE_DEFAULT = "2";
	public static final String PREFS_LOADLASTUSED = "loadlastused";
	public static final boolean PREFS_LOADLASTUSED_DEFAULT = true;
	public static final String PREFS_LASTUSED = "lastused";
	public static final String PREFS_HIDECHECKED = "hidechecked";
	public static final boolean PREFS_HIDECHECKED_DEFAULT = false;
	public static final String PREFS_CAPITALIZATION = "capitalization";
	public static final String PREFS_SHOW_PRICE = "showprice";
	public static final boolean PREFS_SHOW_PRICE_DEFAULT = true;
	public static final String PREFS_SHOW_TAGS = "showtags";
	public static final boolean PREFS_SHOW_TAGS_DEFAULT = true;

	public static final int PREFS_CAPITALIZATION_DEFAULT = 1;

	private static final TextKeyListener.Capitalize smCapitalizationSettings[] = {
			TextKeyListener.Capitalize.NONE,
			TextKeyListener.Capitalize.SENTENCES,
			TextKeyListener.Capitalize.WORDS };

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
	 * @param context
	 *            The context to grab the preferences from.
	 */
	static public String getSortOrderFromPrefs(Context context) {
		int sortOrder = 0;
		try {
			sortOrder = Integer.parseInt(PreferenceManager
					.getDefaultSharedPreferences(context).getString(
							PREFS_SORTORDER, PREFS_SORTORDER_DEFAULT));
		} catch (NumberFormatException e) {
			// Guess somebody messed with the preferences and put a string into
			// this
			// field. We'll use the default value then.
		}

		if (sortOrder >= 0 && sortOrder < Contains.SORT_ORDERS.length) {
			return Contains.SORT_ORDERS[sortOrder];
		}

		// Value out of range - somebody messed with the preferences.
		return Contains.SORT_ORDERS[0];
	}

	public static boolean getHideCheckedItemsFromPrefs(Context context) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		return prefs.getBoolean(PREFS_HIDECHECKED, PREFS_HIDECHECKED_DEFAULT);
	}

	/**
	 * Returns a KeyListener for edit texts that will match the capitalization
	 * preferences of the user.
	 * 
	 * @ param context The context to grab the preferences from.
	 */
	static public KeyListener getCapitalizationKeyListenerFromPrefs(
			Context context) {
		int capitalization = PREFS_CAPITALIZATION_DEFAULT;
		try {
			capitalization = Integer.parseInt(PreferenceManager
					.getDefaultSharedPreferences(context).getString(
							PREFS_CAPITALIZATION,
							Integer.toString(PREFS_CAPITALIZATION_DEFAULT)));
		} catch (NumberFormatException e) {
			// Guess somebody messed with the preferences and put a string
			// into this
			// field. We'll use the default value then.
		}

		if (capitalization < 0
				|| capitalization > smCapitalizationSettings.length) {
			// Value out of range - somebody messed with the preferences.
			capitalization = PREFS_CAPITALIZATION_DEFAULT;
		}

		return new TextKeyListener(smCapitalizationSettings[capitalization],
				true);
	}

}
