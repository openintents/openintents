/*   
 * 	 Copyright (C) 2008-2009 pjv (and others, see About dialog)
 * 
 * 	 This file is part of OI About.
 *
 *   OI About is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   OI About is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with OI About.  If not, see <http://www.gnu.org/licenses/>.
 *   
 *   
 *   
 *   Based on veecheck-sample.
 */

package org.openintents.about.versioncheck;

import org.openintents.about.R;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.util.Log;

import com.tomgibara.android.veecheck.VeecheckNotifier;
import com.tomgibara.android.veecheck.VeecheckService;
import com.tomgibara.android.veecheck.VeecheckState;
import com.tomgibara.android.veecheck.util.DefaultNotifier;
import com.tomgibara.android.veecheck.util.PrefState;

/**
 * @author pjv
 *
 */
public class VersioncheckService extends VeecheckService {

	private static final String TAG = "VersioncheckService";
	public static final int NOTIFICATION_ID = 1;

	@Override
	protected VeecheckNotifier createNotifier() {
		//it's good practice to set up filters to help guard against malicious intents 
		IntentFilter[] filters = new IntentFilter[1];
		try {
			IntentFilter filter = new IntentFilter(Intent.ACTION_VIEW);
			filter.addDataType("text/html");
			filter.addDataScheme("http");
			filters[0] = filter;
		} catch (MalformedMimeTypeException e) {
			Log.e(TAG, "Invalid data type for filter.", e);
		}
		
		//return a default notifier implementation
		return new DefaultNotifier(this, NOTIFICATION_ID, filters,
				new Intent(this, VersioncheckActivity.class),
				R.drawable.icon,
				R.string.vc_notify_ticker,
				R.string.vc_notify_title,
				R.string.vc_notify_message);
	}
	
	@Override
	protected VeecheckState createState() {
		return new PrefState(this);
	}
	
}
