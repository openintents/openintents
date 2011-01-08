/* 
 * Copyright (C) 2007-2009 OpenIntents.org
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

package org.openintents.compatibility.activitypicker;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;

public class DialogHostingActivity extends Activity {

	private static final String TAG = "DialogHostingActivity";
	private static final boolean debug = false;

	public static final int DIALOG_ID_ACTIVITY_PICKER = 0;
	
	public static final String EXTRA_DIALOG_ID = "org.openintents.notepad.extra.dialog_id";

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
		if (debug) Log.d(TAG, "onCreate");
		
		Intent i = getIntent();
		if (i != null && savedInstanceState == null) {
			if (debug) Log.d(TAG, "new dialog");
			int dialogId = i.getIntExtra(EXTRA_DIALOG_ID, 0);
			switch (dialogId) {
			case DIALOG_ID_ACTIVITY_PICKER:
				showDialog(DIALOG_ID_ACTIVITY_PICKER);
				break;
			}
		}
	}


	

	@Override
	protected Dialog onCreateDialog(int id) {
		if (debug) Log.d(TAG, "onCreateDialog");

		Dialog dialog = null;
		
		switch (id) {
		case DIALOG_ID_ACTIVITY_PICKER:
			dialog = new ActivityPickerDialog().createDialog(this);
			break;
		}
		if (dialog == null) {
			dialog = super.onCreateDialog(id);
		}
		if (dialog != null) {
			dialog.setOnDismissListener(mDismissListener);
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		super.onPrepareDialog(id, dialog);
		
		if (debug) Log.d(TAG, "onPrepareDialog");
		
		//dialog.setOnDismissListener(mDismissListener);
		
		switch (id) {
		case DIALOG_ID_ACTIVITY_PICKER:
			break;
		}
	}
	
	OnDismissListener mDismissListener = new OnDismissListener() {
		
		public void onDismiss(DialogInterface dialoginterface) {
			if (debug) Log.d(TAG, "Dialog dismissed. Pausing: " + mIsPausing);
			if (!mIsPausing) {
				if (debug) Log.d(TAG, "finish");
				// Dialog has been dismissed by user.
				DialogHostingActivity.this.finish();
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
