/* 
 * Copyright (C) 2008-2011 OpenIntents.org
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

import org.openintents.util.VersionUtils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.view.Menu;
import android.view.MenuItem;

/**
 * @version 2009-10-23: support Market and aTrackDog
 * @version 2009-02-04
 * @author Peli
 *
 */
public class UpdateDialog extends AlertDialog implements OnClickListener {
	
	private static final String TAG = "UpdateMenu";
	private static final boolean DEBUG_NO_MARKET = true;
	
	/**
	 * If any of the following applications is installed,
	 * there is no need for a manual "Update" menu entry.
	 */
	public static final String[] UPDATE_CHECKER = new String[]
	    {
			"org.openintents.updatechecker", // OI Update
			"com.android.vending", // Google's Android Market
			"com.a0soft.gphone.aTrackDog" // aTrackDog
	    };

    Context mContext;
    
    public UpdateDialog(Context context) {
        super(context);
        mContext = context;

        //setTitle(context.getText(R.string.menu_edit_tags));
        String version = VersionUtils.getVersionNumber(mContext);
        String messageText = mContext.getString(R.string.oi_distribution_update_box_text, version);
        String appname = mContext.getString(R.string.oi_distribution_aboutapp);
        messageText += " " + mContext.getString(R.string.oi_distribution_download_message, appname);
        setMessage(messageText);
    	setButton(mContext.getText(R.string.oi_distribution_update_check_now), this);
    	setButton2(mContext.getText(R.string.oi_distribution_update_get_updater), this);
    }

	public void onClick(DialogInterface dialog, int which) {
		final Intent intent  = new Intent(Intent.ACTION_VIEW);
		
    	if (which == BUTTON1) {
			intent.setData(Uri.parse(mContext.getString(R.string.oi_distribution_update_app_developer_url)));
			GetFromMarketDialog.startSaveActivity(mContext, intent);
    	} else if (which == BUTTON2) {
			intent.setData(Uri.parse(mContext.getString(R.string.oi_distribution_update_checker_developer_url)));
			GetFromMarketDialog.startSaveActivity(mContext, intent);
    	}
		
	}
	
	/**
	 * Check if no updater application is installed.
	 * 
	 * @param context
	 * @return
	 */
	public static boolean isUpdateMenuNecessary(Context context) {
		PackageInfo pi = null;
		
		// Test for existence of all known update checker applications.
		for (int i = 0; i < UPDATE_CHECKER.length; i++) {
			try {
				pi = context.getPackageManager().getPackageInfo(
						UPDATE_CHECKER[i], 0);
			} catch (NameNotFoundException e) {
				// ignore
			}
			if (pi != null && !DEBUG_NO_MARKET) {
				// At least one kind of update checker exists,
				// so there is no need to add a menu item.
				return false;
			}
		}
		
		// If we reach this point, we add a menu item for manual update.
		return true; 
	}

}
