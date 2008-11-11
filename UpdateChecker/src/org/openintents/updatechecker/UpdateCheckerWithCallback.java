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

package org.openintents.updatechecker;

import android.os.DeadObjectException;
import android.os.RemoteException;
import android.util.Log;

public class UpdateCheckerWithCallback implements Runnable {
	private static final String TAG = "UpdateCheckerWithCallback";
	

	private String mLink;	
	private IUpdateCheckerServiceCallback mCallback;


	private UpdateChecker mChecker;

	public UpdateCheckerWithCallback(String link, IUpdateCheckerServiceCallback cb) {

		mLink = link;
		mCallback = cb;
		// TODO 
		mChecker = new UpdateChecker(null, null, 0, null);
	}

	public void run() {
		mChecker.checkForUpdate(mLink);

		sendResult();

	}

	private void sendResult() {
		Log.v(TAG, "send result");
		try {

			mCallback.onVersionChecked(mChecker.getLatestVersion(), mChecker.getApplicationId(), mChecker.getComment(), mChecker.getLatestVersionName());
		} catch (DeadObjectException e) {
			// The IUpdateCheckerServiceCallback will take care of
			// removing
			// the dead object for us.
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

}