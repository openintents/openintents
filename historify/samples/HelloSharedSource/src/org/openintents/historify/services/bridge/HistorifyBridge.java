/* 
 * Copyright (C) 2011 OpenIntents.org
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

package org.openintents.historify.services.bridge;

import org.openintents.historify.uri.Actions;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;

/**
 * 
 * Helper class for clients, for catching broadcast intents sent by Historify
 * and accessing the BridgeService for the REGISTER_SOURCE and QUICK_POST
 * functions.
 * 
 * @author berke.andras
 */
public class HistorifyBridge {

	public abstract static class RequestReceiver extends BroadcastReceiver {

		@Override
		public final void onReceive(Context context, Intent intent) {

			if (Actions.BROADCAST_REQUEST_REGISTER_SOURCE.equals(intent
					.getAction())) {
				// check if the broadcast is addressed to us.
				String packageName = intent
						.getStringExtra(Actions.EXTRA_PACKAGE_NAME);
				if (context.getPackageName().equals(packageName)
						|| !intent.getBooleanExtra(Actions.EXTRA_ADDRESSED,
								true)) {
					onRequestRegister(context);
				}
			}
		}

		protected abstract void onRequestRegister(Context context);

	}

	private int mIconResource;

	/**
	 * 
	 * @param iconResource
	 *            Icon used if posting notifications.
	 */
	public HistorifyBridge(int iconResource) {
		this.mIconResource = iconResource;

	}

	public void registerSource(Context context, String name, String authority,
			String description, String iconUri) {

		Intent intent = new Intent();

		//determine application's uid
		String packageName=context.getPackageName();
		int uid;
		try {
			uid = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.uid;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		intent.setAction(Actions.ACTION_REGISTER_SOURCE);
		intent.putExtra(Actions.EXTRA_SOURCE_NAME, name);
		intent.putExtra(Actions.EXTRA_SOURCE_AUTHORITY, authority);
		intent.putExtra(Actions.EXTRA_SOURCE_UID, uid);
		intent.putExtra(Actions.EXTRA_SOURCE_DESCRIPTION, description);
		intent.putExtra(Actions.EXTRA_SOURCE_ICON_URI, iconUri);

		try {
			context.startService(intent);
		} catch (SecurityException se) {
			// can't access service, maybe because this application was
			// installed BEFORE Historify.
			//
			// http://stackoverflow.com/questions/4567812/define-a-permission-for-third-party-apps-to-use-in-android
			// "My own app, which defined the permission for other apps to use,
			// must be installed before other apps who want to use my
			// permissions. Otherwise, those apps must be re-installed, to use
			// my permissions.
			postNotification(
					context,
					"Error while registering",
					"Unable to register SharedSource. Reinstalling might solve the issue.");
		}

	}

	private void postNotification(Context context, String title,
			String expandedText) {

		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new Notification(mIconResource, title,
				System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		Intent voidIntent = new Intent(context, Dialog.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				voidIntent, 0);

		String appLabel = context
				.getString(context.getApplicationInfo().labelRes);
		notification.setLatestEventInfo(context, appLabel, expandedText,
				pendingIntent);

		nm.notify(0, notification);
	}
}
