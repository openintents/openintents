/* 
 * Copyright (C) 2007-2011 OpenIntents.org
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


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

/**
 * Displays the Eula for the first time, reading it from a raw resource.
 * 
 * @version 2009-01-17, 13:00 UTC
 * @author Peli
 *
 */
public class EulaActivity extends Activity {

	/** TAG for log messages. */
	private static final String TAG = "EulaActivity";
	
	public static final String PREFERENCES_EULA_ACCEPTED = "eula_accepted";
	
	/**
	 * Extra for main intent.
	 * Specifies activity that should be launched after Eula has been accepted.
	 */
	private static final String EXTRA_LAUNCH_ACTIVITY_PACKAGE = "org.openintents.extra.launch_activity_package";
	private static final String EXTRA_LAUNCH_ACTIVITY_CLASS = "org.openintents.extra.launch_activity_class";
	private static final String EXTRA_LAUNCH_ACTIVITY_INTENT = "org.openintents.extra.launch_activity_intent";
	
	private Button mAgree;
	private Button mDisagree;
	
	private String mLaunchPackage;
	private String mLaunchClass;
	private Intent mLaunchIntent;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		
		setContentView(R.layout.eula);
		
		// Extras are provided by checkEula() below.
		Intent i = getIntent();
		Bundle b = i.getExtras();
		mLaunchPackage = b.getString(EXTRA_LAUNCH_ACTIVITY_PACKAGE);
		mLaunchClass = b.getString(EXTRA_LAUNCH_ACTIVITY_CLASS);
		//mLaunchIntent 
		mLaunchIntent = b.getParcelable(EXTRA_LAUNCH_ACTIVITY_INTENT);
		
		//mIntroContinue = (Button) findViewById(R.id.intro_continue);
		mAgree = (Button) findViewById(R.id.button1);
		mAgree.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				acceptEula();
			}
		});
		
		mDisagree = (Button) findViewById(R.id.button2);
		mDisagree.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				refuseEula();
			}
		});
		
		TextView text = (TextView) findViewById(R.id.text);
		text.setText(readLicenseFromRawResource(R.raw.license_short));
		
	}
	
	/**
	 * Accept EULA and proceed with main application.
	 */
	public void acceptEula() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean(PREFERENCES_EULA_ACCEPTED, true);
		e.commit();
		
		// Call the activity that originally called checkEula()
		Intent i;
		if (mLaunchIntent != null) {
			i = mLaunchIntent;
			
			// Android 2.3: category LAUNCHER needs to be removed,
			// otherwise main activity is not called.
			i.removeCategory(Intent.CATEGORY_LAUNCHER);
		} else {
			i = new Intent();
			i.setClassName(mLaunchPackage, mLaunchClass);
		}
		i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
		startActivity(i);
		finish();
	}
	
	/**
	 * Refuse EULA.
	 */
	public void refuseEula() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor e = sp.edit();
		e.putBoolean(PREFERENCES_EULA_ACCEPTED, false);
		e.commit();
		
		finish();
	}

	public static boolean checkEula(Activity activity) {
		return checkEula(activity, activity.getIntent());
	}
	
	/**
	 * Test whether EULA has been accepted. Otherwise display EULA.
	 * 
	 * @return True if Eula has been accepted.
	 */
	public static boolean checkEula(Activity activity, Intent intent) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(activity);
		boolean accepted = sp.getBoolean(PREFERENCES_EULA_ACCEPTED, false);
		
		if (accepted) {
			Log.i(TAG, "Eula has been accepted.");
			return true;
		} else {
			Log.i(TAG, "Eula has not been accepted yet.");
			
			// Launch Eula activity
			Intent i = new Intent(activity, EulaActivity.class);
			ComponentName ci = activity.getComponentName();
			
			// Specify in intent extras which activity should be called
			// after Eula has been accepted.
			Log.d(TAG, "Local package name: " + ci.getPackageName());
			Log.d(TAG, "Local class name: " + ci.getClassName());
			i.putExtra(EXTRA_LAUNCH_ACTIVITY_PACKAGE, ci.getPackageName());
			i.putExtra(EXTRA_LAUNCH_ACTIVITY_CLASS, ci.getClassName());
			if (intent != null) {
				i.putExtra(EXTRA_LAUNCH_ACTIVITY_INTENT, intent);
			}
			i.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
			activity.startActivity(i);
			activity.finish();
			return false;
		}
	}
	
	/**
	 * Read license from raw resource.
	 * @param resourceid ID of the raw resource.
	 * @return
	 */
	private String readLicenseFromRawResource(int resourceid) {

		// Retrieve license from resource:
		String license = "";
		Resources resources = getResources();
    		
		//Read in the license file as a big String
		BufferedReader in
		   = new BufferedReader(new InputStreamReader(
				resources.openRawResource(resourceid)));
		String line;
		StringBuilder sb = new StringBuilder();
		try {
			while ((line = in.readLine()) != null) { // Read line per line.
				if (TextUtils.isEmpty(line)) {
					// Empty line: Leave line break
					sb.append("\n\n");
				} else {
					sb.append(line);
					sb.append(" ");
				}
			}
			license = sb.toString();
		} catch (IOException e) {
			//Should not happen.
			e.printStackTrace();
		}
		
    	
    	return license;
	}
}
