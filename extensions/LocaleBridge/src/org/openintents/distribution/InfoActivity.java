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

package org.openintents.distribution;

import org.openintents.localebridge.R;
import org.openintents.util.IntentUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;

public class InfoActivity extends Activity {


	private static final String TAG = "InfoActivity";
	private static final boolean debug = !false;

	public static final int DIALOG_INFO = 1;
	public static final int DIALOG_GET_FROM_MARKET = 2;

	/**
	 * Whether dialog is simply pausing while hidden by another activity
	 * or when configuration changes.
	 * If this is false, then we can safely finish this activity if a dialog
	 * gets dismissed.
	 */
	private boolean mIsPausing = false;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		if (i != null && savedInstanceState == null) {
			if (debug) Log.d(TAG, "new dialog");
			if (isApplicationAvailable()) {
				showDialog(DIALOG_INFO);
			} else {
				showDialog(DIALOG_GET_FROM_MARKET);
			}
		}
	}
	
	Intent getApplicationIntent() {
		Intent intent = new Intent();
		intent.setClassName("org.openintents.countdown", "org.openintents.countdown.list.CountdownListActivity");
		return intent;
	}

	boolean isApplicationAvailable() {
		return IntentUtils.isIntentAvailable(this, getApplicationIntent());
	}
	
	void launchApplication() {
		startActivity(getApplicationIntent());
		finish();
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		
		switch (id) {
		case DIALOG_INFO:
			dialog = buildInfoDialog();
			break;
		case DIALOG_GET_FROM_MARKET:
			dialog = new GetFromMarketDialog(this, 
					RD.string.countdown_not_available,
					RD.string.countdown_get,
					RD.string.countdown_market_uri,
					RD.string.countdown_developer_uri);
		}
		if (dialog == null) {
			dialog = super.onCreateDialog(id);
		}
		if (dialog != null) {
			dialog.setOnDismissListener(mDismissListener);
		}
		return dialog;
	}

	private AlertDialog buildInfoDialog() {
		return new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher_appwidget)
			.setTitle(RD.string.app_name)
			.setMessage(RD.string.info_text)
			.setPositiveButton(RD.string.info_launch,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							// click Ok
							launchApplication();
						}
					})
			.create();
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		
		switch (id) {
		case DIALOG_INFO:
			break;
		}
	}
	
	OnDismissListener mDismissListener = new OnDismissListener() {
		
		public void onDismiss(DialogInterface dialoginterface) {
			if (debug) Log.d(TAG, "Dialog dismissed. Pausing: " + mIsPausing);
			if (!mIsPausing) {
				if (debug) Log.d(TAG, "finish");
				// Dialog has been dismissed by user.
				InfoActivity.this.finish();
			} else {
				// Probably just a screen orientation change. Don't finish yet.
				// Dialog has been dismissed by system.
			}
		}
		
	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (debug) Log.d(TAG, "onSaveInstanceState");
		// It is important to set mIsPausing here, so that
		// the dialog does not get closed on orientation changes.
		mIsPausing = true;
		if (debug) Log.d(TAG, "onSaveInstanceState. Pausing: " + mIsPausing);
	}

	@Override
	protected void onResume() {
		if (debug) Log.d(TAG, "onResume");
		super.onResume();
		// In case another activity is called, and we are resumed,
		// mIsPausing should be reset to its original state.
		mIsPausing = false;
	}
	
}
