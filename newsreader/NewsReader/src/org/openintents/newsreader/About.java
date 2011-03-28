/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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

package org.openintents.newsreader;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * About dialog
 * The int extra with name TEXT_ID (= "textId") is used to fill the text view.
 * The default is the open intents text.
 * 
 * The int extra with name TITLE_ID (= "titleId") is used to fill the title.
 * The default is "About OpenIntents".
 *
 * @author openintent.org
 *
 */
public class About extends Activity {
	private static final String TAG = "About";

	public static final String TEXT_ID = "textId";
	public static final String TITLE_ID = "titleId";

	@Override
	protected void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);

		setTheme(android.R.style.Theme_Dialog);
		setContentView(R.layout.about);
		
		String version = "?";
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Package name not found", e);
		};
		
		if (getIntent() != null ){
			TextView text = (TextView)findViewById(R.id.text);
			text.setText(getString(getIntent().getIntExtra(TEXT_ID, R.string.about_openintents_text), version));
			setTitle(getString(getIntent().getIntExtra(TITLE_ID, R.string.about_openintents), getString(R.string.newsreader_app_name)));
		}
	}

}
