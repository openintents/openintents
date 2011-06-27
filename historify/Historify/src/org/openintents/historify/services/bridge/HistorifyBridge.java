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

import org.openintents.historify.data.model.EventData;
import org.openintents.historify.data.providers.Events;
import org.openintents.historify.uri.Actions;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

/**
 * 
 * Helper class for clients, for catching broadcast intents sent by Historify
 * and accessing the BridgeService for the REGISTER_SOURCE and QUICK_POST
 * functions.
 * 
 * @author berke.andras
 */
public class HistorifyBridge {

	private static final String N = "Historify.Bridge";
	
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

	public static class QuickPostContext {
		
		private String sourceName;
		private String sourceDescription;
		private String iconUri;
		private int version;
		
		public QuickPostContext(String sourceName, String sourceDescription, String iconUri, int version) {
			super();
			this.sourceName = sourceName;
			this.sourceDescription = sourceDescription;
			this.iconUri = iconUri;
			this.version = version;
			
			if(this.sourceName==null) {
				throw new NullPointerException("Source name cannot be null.");
			}
		}

		public String getSourceName() {
			return sourceName;
		}

		public String getSourceDescription() {
			return sourceDescription;
		}
				
		public String getIconUri() {
			return iconUri;
		}

		public int getVersion() {
			return version;
		}
	}
	
	private int mIconResource;
	private QuickPostContext mQuickPostContext;
	
	/**
	 * 
	 * @param iconResource
	 *            Icon used if posting notifications.
	 */
	public HistorifyBridge(int iconResource) {
		this.mIconResource = iconResource;

	}

	public void setQuickPostContext(QuickPostContext quickPostContext) {
		this.mQuickPostContext = quickPostContext;
	}
	
	public void quickPost(Context context, EventData eventData) {
		
		if(mQuickPostContext==null) {
			throw new NullPointerException("QuickPost context is not set.");
		}
		
		if(eventData==null) {
			throw new NullPointerException("Event data cannot be null.");
		}
		
		int uid = determineUid(context);
		
		if(uid==0) {
			Log.e(N,"Cannot determine package UID.");
			return;
		}
		
		Intent intent = new Intent();
		intent.setAction(Actions.ACTION_QUICK_POST);
		
		//quickpost source data
		intent.putExtra(Actions.EXTRA_SOURCE_NAME, mQuickPostContext.getSourceName());
		intent.putExtra(Actions.EXTRA_SOURCE_DESCRIPTION, mQuickPostContext.getSourceDescription());
		intent.putExtra(Actions.EXTRA_SOURCE_ICON_URI, mQuickPostContext.getIconUri());
		intent.putExtra(Actions.EXTRA_SOURCE_UID, uid);
		intent.putExtra(Actions.EXTRA_SOURCE_VERSION, mQuickPostContext.getVersion());
		
		//quickpost event data
		intent.putExtra(Events.EVENT_KEY,eventData.getEventKey());
		intent.putExtra(Events.CONTACT_KEY, eventData.getContactKey());
		intent.putExtra(Events.PUBLISHED_TIME, eventData.getPublishedTime());
		intent.putExtra(Events.MESSAGE, eventData.getMessage());
		intent.putExtra(Events.ORIGINATOR, eventData.getOriginator());
		
		postIntent(context, intent);
	}
	
	public void registerSource(Context context, String name, String authority,
			String description, String iconUri, int version) {

		//determine application's uid
		int uid  = determineUid(context);
		
		if(uid==0) {
			Log.e(N,"Cannot determine package UID.");
			return;
		}

		Intent intent = new Intent();
		intent.setAction(Actions.ACTION_REGISTER_SOURCE);
		intent.putExtra(Actions.EXTRA_SOURCE_NAME, name);
		intent.putExtra(Actions.EXTRA_SOURCE_AUTHORITY, authority);
		intent.putExtra(Actions.EXTRA_SOURCE_UID, uid);
		intent.putExtra(Actions.EXTRA_SOURCE_DESCRIPTION, description);
		intent.putExtra(Actions.EXTRA_SOURCE_ICON_URI, iconUri);
		intent.putExtra(Actions.EXTRA_SOURCE_VERSION, version);

		postIntent(context, intent);
	}
	
	private void postIntent(Context context, Intent intent) {
	
		try {
			context.startService(intent);
		} catch (SecurityException se) {
			// can't access service, maybe it's not installed,
			// or there are other configuration or permission problems.
			postNotification(
					context,
					"Application Error",
					"Unable to communicate with Historify. Reinstalling might solve the issue.");
		}

	}
	
	private int determineUid(Context context) {
		
		String packageName=context.getPackageName();
		int uid = 0;
		try {
			uid = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.uid;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		return uid;
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
