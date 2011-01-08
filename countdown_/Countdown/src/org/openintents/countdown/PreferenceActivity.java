/* 
 * Copyright (C) 2008-2009 OpenIntents.org
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

package org.openintents.countdown;

import org.openintents.util.IntentUtils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

public class PreferenceActivity extends android.preference.PreferenceActivity {

	public static final String PREFS_NOTIFICATION_TIMEOUT = "notification_timeout";
	public static final String PREFS_NOTIFICATION_TIMEOUT_DEFAULT = "300";
	public static final String PREFS_EXTENSIONS_MARKET = "preference_extensions_market";

	@Override
	protected void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);

		addPreferencesFromResource(R.xml.preferences);

		// Set enabled state of Market preference
		PreferenceScreen sp = (PreferenceScreen) findPreference(PREFS_EXTENSIONS_MARKET);
		sp.setEnabled(isMarketAvailable());
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

	/**
	 * Returns the notification timeout in seconds from preferences.
	 * Performs error-checking.
	 * 
	 * @param context The context to grab the preferences from.
	 */
	static public long getNotificationTimeoutFromPrefs(Context context) {
		long notificationTimeout = Long.parseLong(PREFS_NOTIFICATION_TIMEOUT_DEFAULT);
		try {
			notificationTimeout = Long.parseLong(PreferenceManager.getDefaultSharedPreferences(context)
			.getString(PREFS_NOTIFICATION_TIMEOUT, PREFS_NOTIFICATION_TIMEOUT_DEFAULT));
		} catch (NumberFormatException e) {
			// Guess somebody messed with the preferences and put a string into this
			// field. We'll use the default value then.
		}
		return notificationTimeout;
	}
}
