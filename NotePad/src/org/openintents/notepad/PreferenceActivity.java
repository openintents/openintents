/* 
 * Copyright (C) 2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.notepad;

import org.openintents.notepad.NotePad.Notes;
import org.openintents.util.IntentUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class PreferenceActivity extends android.preference.PreferenceActivity {

	public static final String PREFS_SORTORDER = "sortorder";
	public static final String PREFS_SORTORDER_DEFAULT = "2";
	public static final String PREFS_FONTSIZE = "fontsize";
	public static final String PREFS_FONTSIZE_DEFAULT = "2";
	public static final String PREFS_EXTENSIONS_MARKET = "preference_extensions_market";
	public static final String PREFS_AUTOLINK = "autolink";

	@Override
	protected void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);

		addPreferencesFromResource(R.xml.preferences);

		// Set enabled state of Market preference
		PreferenceScreen sp = (PreferenceScreen) findPreference(PREFS_EXTENSIONS_MARKET);
		sp.setEnabled(isMarketAvailable());
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
		
		if (sortOrder >= 0 && sortOrder < Notes.SORT_ORDERS.length)
		{
			return Notes.SORT_ORDERS[sortOrder];
		}
		
		// Value out of range - somebody messed with the preferences.
		return Notes.SORT_ORDERS[0];
	}

	public static int getFontSizeFromPrefs(Context context) {
		int size = Integer.parseInt(PreferenceManager
				.getDefaultSharedPreferences(context).getString(
						PREFS_FONTSIZE,
						PREFS_FONTSIZE_DEFAULT));
		return size;
	}
	
	/**
	 * Check whether Market is available.
	 * @return true if Market is available
	 */
	private boolean isMarketAvailable() {
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setData(Uri.parse(getString(R.string.preference_extensions_market_link)));
		return IntentUtils.isIntentAvailable(this, i);
	}
}
