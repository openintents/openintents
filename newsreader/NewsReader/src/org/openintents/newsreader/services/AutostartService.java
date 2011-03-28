package org.openintents.newsreader.services;

/*
*	This file is part of OI Newsreader.
*	Copyright (C) 2007-2009 OpenIntents.org
*	OI Newsreader is free software: you can redistribute it and/or modify
*	it under the terms of the GNU General Public License as published by
*	the Free Software Foundation, either version 3 of the License, or
*	(at your option) any later version.
*
*	OI Newsreader is distributed in the hope that it will be useful,
*	but WITHOUT ANY WARRANTY; without even the implied warranty of
*	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*	GNU General Public License for more details.
*
*	You should have received a copy of the GNU General Public License
*	along with OI Newsreader.  If not, see <http://www.gnu.org/licenses/>.
*/

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.util.Log;

public class AutostartService extends BroadcastReceiver {

	private boolean startOnSystemBoot = false;

	private static final String _TAG = "org.openintents.news.services.AutostartService";

	public void onReceive(Context context, Intent intent) {

		// get prefs and check if we should do anthing
		SharedPreferences settings = context.getSharedPreferences(
				NewsreaderService.PREFS_NAME, 0);
		// useWhileRoaming
		// =settings.getBoolean(NewsreaderService.DO_ROAMING,false);
		startOnSystemBoot = settings.getBoolean(
				NewsreaderService.ON_BOOT_START, false);

		// startOnSystemBoot=true;
		Log.d(_TAG, "StartOnSystemBoot:" + startOnSystemBoot);
		if (!startOnSystemBoot) {// Nothing to do,go home
			return;
		} else {
			String action = intent.getAction();
			Log.d(_TAG, action);
			if (action == null) {
				Log.e(_TAG, "Action==null!");
				return;
			} else if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {

				context.startService(new Intent(context,
						NewsreaderService.class));

			}
		}
	}

}