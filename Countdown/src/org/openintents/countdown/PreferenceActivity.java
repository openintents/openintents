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

package org.openintents.countdown;

import org.openintents.util.IntentUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceScreen;

public class PreferenceActivity extends android.preference.PreferenceActivity {

	//public static final String PREFS_SORTORDER = "sortorder";
	//public static final String PREFS_SORTORDER_DEFAULT = "2";
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
}
