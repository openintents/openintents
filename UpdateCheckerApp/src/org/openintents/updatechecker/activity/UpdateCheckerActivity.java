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
import org.openintents.updatechecker.UpdateApplication;
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
	private static final String MARKET_PACKAGE_SEARCH_PREFIX = "market://search?q=pname:";
	// = "http://market.android.com/search?q=pname:";
	private static final String ANDAPPSTORE_PACKAGE_SEARCH_PREFIX = "http://andappstore.com/AndroidPhoneApplications/apps/!search?s=";
	private static final String ANDAPPSTORE_PREFIX = "http://andappstore.com";

	private String mPackageName = null;
	private RadioGroup mRadioGroup;
	private int mLatestVersion;
	private String mLatestVersionName;
	private int mCurrentVersion;
	private String mCurrentVersionName;
	private boolean mIsMarketIntent;
	private boolean mIsAppandstoreIntent;
	private String mAppName;

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
		// view.setText(getString(R.string.about_text_extended,
		// getVersionNumber(),
		// getSDInfo(), getOSInfo()));

		mPackageName = getIntent().getStringExtra(
				UpdateChecker.EXTRA_PACKAGE_NAME);

		((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
				.cancel(mPackageName.hashCode());

		mRadioGroup = (RadioGroup) findViewById(R.id.action_choice);

		Button button = (Button) findViewById(R.id.ok);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				switch (mRadioGroup.getCheckedRadioButtonId()) {
				case R.id.do_update:
					Intent intent = (Intent) getIntent().getParcelableExtra(
							UpdateChecker.EXTRA_UPDATE_INTENT);
					Log.d(TAG, "Do update: " + intent.getAction() + ", "
							+ intent.getDataString());
					if (UpdateApplication.AND_APP_STORE || mIsMarketIntent) {
						try {
							startActivity(intent);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(UpdateCheckerActivity.this,
									R.string.market_not_available,
									Toast.LENGTH_SHORT).show();
							Log.e(TAG, "Market not found", e);
						}
					} else {
						Intent warnIntent = new Intent(
								UpdateCheckerActivity.this, WarnActivity.class);
						warnIntent.putExtra(UpdateInfo.EXTRA_WARN_INTENT,
								intent);
						try {
							startActivity(warnIntent);
						} catch (ActivityNotFoundException e) {
							Toast.makeText(
									UpdateCheckerActivity.this,
									getString(R.string.update_not_started, e
											.toString()), Toast.LENGTH_LONG)
									.show();
							Log.e(TAG, "Update not started", e);
						}
					}
					setResult(RESULT_OK);
					break;
				case R.id.do_update_defaultStore:
					searchDefaultStoreForPackage(UpdateCheckerActivity.this,
							mPackageName, mAppName);

					setResult(RESULT_OK);
					break;
				case R.id.remind_me_later:
					updateUpdateTime();

					// Cancelling result means that no list refresh is required
					// by the calling activity.
					setResult(RESULT_CANCELED);
					break;
				case R.id.ignore_this_update:
					updateLastIgnoredVersion();
					setResult(RESULT_OK);
					break;
				case R.id.ignore_all_further_updates:
					UpdateInfo.setNoUpdates(UpdateCheckerActivity.this,
							mPackageName, true);
					setResult(RESULT_OK);
					break;
				}
				finish();
			}
		});

		Log.v(TAG, "package name = " + mPackageName);

	}

	public static void searchDefaultStoreForPackage(Context context,
			String packageName, String appName) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		Uri uri;
		if (UpdateApplication.AND_APP_STORE) {
			uri = Uri.parse(ANDAPPSTORE_PACKAGE_SEARCH_PREFIX
					+ Uri.decode(appName));
		} else {
			uri = Uri.parse(MARKET_PACKAGE_SEARCH_PREFIX + packageName);
		}
		intent.setData(uri);
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Toast.makeText(context, R.string.market_not_available,
					Toast.LENGTH_SHORT).show();
		}
	}

	protected boolean isMarketIntent(Intent intent) {
		return intent.getDataString() != null
				&& (intent.getDataString().startsWith(MARKET_PREFIX_1) || intent
						.getDataString().startsWith(MARKET_PREFIX_2));

	}

	protected boolean isAndAppIntent(Intent intent) {
		return intent.getDataString() != null
				&& intent.getDataString().startsWith(ANDAPPSTORE_PREFIX);
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

		mAppName = intent.getStringExtra(UpdateChecker.EXTRA_APP_NAME);
		String comment = intent.getStringExtra(UpdateChecker.EXTRA_COMMENT);

		Intent updateIntent = (Intent) getIntent().getParcelableExtra(
				UpdateChecker.EXTRA_UPDATE_INTENT);
		mIsMarketIntent = isMarketIntent(updateIntent);
		mIsAppandstoreIntent = isAndAppIntent(updateIntent);

		String location = null;
		if (mIsMarketIntent || mIsAppandstoreIntent) {
			location = (mIsMarketIntent ? getString(R.string.android_market)
					: getString(R.string.andappstore));
		}

		if (mAppName != null) {
			if (comment != null) {
				if (location != null) {
					view.setText(getString(R.string.update_available_3,
							mAppName, location, comment));
				} else {
					view.setText(getString(R.string.update_available_2,
							mAppName, comment));
				}
			} else {
				if (location != null) {
					view.setText(getString(
							R.string.update_available_location_no_comment,
							mAppName, location));
				} else {
					view.setText(getString(
							R.string.update_available_no_comment, mAppName));
				}
			}
		}

		mCurrentVersion = intent.getIntExtra(
				UpdateChecker.EXTRA_CURRENT_VERSION, 0);
		mCurrentVersionName = intent
				.getStringExtra(UpdateChecker.EXTRA_CURRENT_VERSION_NAME);

		view = (TextView) findViewById(R.id.current_version);
		if (mCurrentVersionName != null) {
			view.setText(getString(R.string.current_version,
					mCurrentVersionName));
			view.setVisibility(View.VISIBLE);
		} else {
			view.setVisibility(View.GONE);
		}

		mLatestVersion = intent.getIntExtra(UpdateChecker.EXTRA_LATEST_VERSION,
				0);
		mLatestVersionName = intent
				.getStringExtra(UpdateChecker.EXTRA_LATEST_VERSION_NAME);

		if (mLatestVersionName != null) {
			setTitle(getString(R.string.newer_version, mLatestVersionName));
		} else {
			setTitle(R.string.app_name);
		}

		// visibility for ignore this update
		int visibility;
		if (mLatestVersion > 0 || mLatestVersionName != null) {
			visibility = View.VISIBLE;
		} else {
			visibility = View.GONE;

		}

		findViewById(R.id.ignore_this_update).setVisibility(visibility);

		// visibility for do update market
		view = (TextView) findViewById(R.id.do_update_defaultStore);
		if (!mIsMarketIntent && !UpdateApplication.AND_APP_STORE) {
			visibility = View.VISIBLE;
		} else if (!mIsAppandstoreIntent && UpdateApplication.AND_APP_STORE) {
			visibility = View.VISIBLE;
			view.setText(R.string.do_update_andAppStore);
		} else {
			visibility = View.GONE;
		}
		view.setVisibility(visibility);

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

}