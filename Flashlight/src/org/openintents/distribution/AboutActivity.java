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

package org.openintents.distribution;

//Version Nov 12, 2008

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * About dialog
 *
 * @author Peli
 *
 */
public class AboutActivity extends Activity {
	private static final String TAG = "About";

	@Override
	protected void onCreate(Bundle icicle) {
		
		super.onCreate(icicle);

		setTheme(android.R.style.Theme_Dialog);
		setContentView(RD.layout.about);
		
		String version = getVersionNumber();
		String name = getApplicationName();

		setTitle(getString(RD.string.about_title, name));
		
		TextView text = (TextView)findViewById(RD.id.text);
		text.setText(getString(RD.string.about_text, version));
	}
	
	/**
	 * Get current version number.
	 * 
	 * @return
	 */
	private String getVersionNumber() {
		String version = "?";
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Package name not found", e);
		};
		return version;
	}
	
	/**
	 * Get application name.
	 * 
	 * @return
	 */
	private String getApplicationName() {
		String name = "?";
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			name = getString(pi.applicationInfo.labelRes);
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Package name not found", e);
		};
		return name;
	}

}
