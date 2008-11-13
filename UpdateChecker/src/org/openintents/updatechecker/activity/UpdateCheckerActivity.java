/*
 * Copyright (C) 2008  OpenIntents.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.openintents.updatechecker.activity;

import org.openintents.updatechecker.R;
import org.openintents.updatechecker.UpdateChecker;
import org.openintents.updatechecker.db.UpdateInfo;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StatFs;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class UpdateCheckerActivity extends Activity {
	private static final String TAG = "UpdateChecker";
	public static final String EXTRA_LATEST_VERSION = "latest_version";
	public static final String EXTRA_COMMENT = "comment";
	private static final String MARKET_PREFIX_1 = "market://";
	private static final String MARKET_PREFIX_2 = "http://market.android.com/";
	private static final String MARKET_PACKAGE_SEARCH_PREFIX 
				= "market://search?q=pname:";
	//			= "http://market.android.com/search?q=pname:";
	private String mPackageName = null;
	private RadioGroup mRadioGroup;
	private int mLatestVersion;
	private String mLatestVersionName;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.v(TAG, "create");
		super.onCreate(savedInstanceState);

		// check for a new version of UpdateChecker
		// Update.check(this);

		setContentView(R.layout.update_available);

		setFromIntent(getIntent());

		// view = (TextView) findViewById(R.id.text);
		//
		// view.setText(getString(R.string.about_text_extended, getVersionNumber(),
		// getSDInfo(), getOSInfo()));

		mPackageName = getIntent().getStringExtra(
				UpdateChecker.EXTRA_PACKAGE_NAME);

		((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(mPackageName.hashCode());
		
		mRadioGroup = (RadioGroup) findViewById(R.id.action_choice);

		Button button = (Button) findViewById(R.id.ok);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				switch (mRadioGroup.getCheckedRadioButtonId()) {
				case R.id.do_update:
					Intent intent = (Intent) getIntent().getParcelableExtra(
							UpdateChecker.EXTRA_UPDATE_INTENT);
					Log.d(TAG, "Do update: " + intent.getAction() + ", " + intent.getDataString());
					if (isMarketIntent(intent)){
						try {
							startActivity(intent);	
						} catch (ActivityNotFoundException e) {
							Toast.makeText(UpdateCheckerActivity.this, R.string.market_not_available, Toast.LENGTH_SHORT).show();
							Log.e(TAG, "Market not found", e);
						}
					} else {
						Intent warnIntent = new Intent(UpdateCheckerActivity.this, WarnActivity.class);
						warnIntent.putExtra(UpdateInfo.EXTRA_WARN_INTENT, intent);
						try {
							startActivity(warnIntent);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(UpdateCheckerActivity.this, getString(R.string.update_not_started, e.toString()), Toast.LENGTH_LONG).show();
							Log.e(TAG, "Update not started", e);
						}
					}
						
					
					break;
				case R.id.remind_me_later:
					updateUpdateTime();
					break;
				case R.id.ignore_this_update:
					updateLastIgnoredVersion();
					break;
				case R.id.ignore_all_further_updates:
					UpdateInfo.setNoUpdates(UpdateCheckerActivity.this, mPackageName, true);
					break;					
				}
				finish();
			}
		});
		
		Log.v(TAG, "package name = " + mPackageName);


	}

	public static void searchMarketForPackage(Context context, String packageName) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		Uri uri = Uri.parse(MARKET_PACKAGE_SEARCH_PREFIX + packageName);
		intent.setData(uri);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, R.string.market_not_available, Toast.LENGTH_SHORT).show();
		}
	}

	protected boolean isMarketIntent(Intent intent) {
		return intent.getDataString() != null && (intent.getDataString().startsWith(MARKET_PREFIX_1) || intent.getDataString().startsWith(MARKET_PREFIX_2)); 
		
	}


	protected void updateLastIgnoredVersion() {
		ContentValues values = new ContentValues();
		values.put(UpdateInfo.IGNORE_VERSION_CODE, mLatestVersion);
		values.put(UpdateInfo.IGNORE_VERSION_NAME, mLatestVersionName);
		getContentResolver().update(UpdateInfo.CONTENT_URI, values,
				UpdateInfo.PACKAGE_NAME + " = ? ",
				new String[] { mPackageName });

	}

	protected void updateUpdateTime() {
		ContentValues values = new ContentValues();
		values.put(UpdateInfo.LAST_CHECK, System.currentTimeMillis());
		getContentResolver().update(UpdateInfo.CONTENT_URI, values,
				UpdateInfo.PACKAGE_NAME + " = ? ",
				new String[] { mPackageName });
	}

	private void setFromIntent(Intent intent) {
		TextView view = (TextView) findViewById(R.id.text_update);

		String appName = intent.getStringExtra(UpdateChecker.EXTRA_APP_NAME);
		String comment = intent.getStringExtra(UpdateChecker.EXTRA_COMMENT);
		if (appName != null ) {
			if (comment != null){
			view.setText(getString(R.string.update_available_2, appName,
					comment));
			} else {
				view.setText(getString(R.string.update_available_no_comment, appName));
			}
		}

		mLatestVersion = intent
				.getIntExtra(UpdateChecker.EXTRA_LATEST_VERSION, 0);
		mLatestVersionName = intent
				.getStringExtra(UpdateChecker.EXTRA_LATEST_VERSION_NAME);

		
		if (mLatestVersionName != null){
			setTitle(getString(R.string.latest_version_2, mLatestVersionName));
		} else {
			setTitle(R.string.app_name);
		}
		
		int visibility;
		if (mLatestVersion > 0 || mLatestVersionName != null) {
			visibility = View.VISIBLE;
		} else {
			visibility = View.GONE;

		}		

		findViewById(R.id.ignore_this_update).setVisibility(visibility);

	}

	@Override
	protected void onResume() {
		super.onResume();

		if (mPackageName != null) {
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
					.cancel(mPackageName.hashCode());
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Log.v(TAG, "new intent");
		setFromIntent(intent);
		mPackageName = intent.getStringExtra(UpdateChecker.EXTRA_PACKAGE_NAME);
		if (mPackageName != null) {
			((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
					.cancel(mPackageName.hashCode());
		}
		Log.v(TAG, "package name = " + mPackageName);
	}

	private String getSDInfo() {
		StatFs stats = new StatFs("/sdcard");
		int space = stats.getAvailableBlocks() * stats.getBlockSize();
		return getString(R.string.free_space, String.valueOf(space));
	}

	private String getOSInfo() {

		return getString(R.string.os_info, Build.BOARD, Build.BRAND,
				Build.DEVICE, Build.ID, Build.MODEL);

	}

	/**
	 * Get current version number.
	 * 
	 * @return
	 */
	private String getVersionNumber() {
		String version = "?";
		try {
			PackageInfo pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			version = pi.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Package name not found", e);
		}
		;
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
			PackageInfo pi = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			name = getString(pi.applicationInfo.labelRes);
		} catch (PackageManager.NameNotFoundException e) {
			Log.e(TAG, "Package name not found", e);
		}
		;
		return name;
	}

}