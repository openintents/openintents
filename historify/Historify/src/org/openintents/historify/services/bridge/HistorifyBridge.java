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

import java.util.List;

import org.openintents.historify.data.model.EventData;
import org.openintents.historify.data.model.IconLoadingStrategy;
import org.openintents.historify.data.providers.Events;
import org.openintents.historify.uri.Actions;

import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

/**
 * Helper class used by clients for catching broadcast intents sent by Historify
 * and accessing the BridgeService for the REGISTER_SOURCE and QUICK_POST
 * functions.
 * 
 * @author berke.andras
 */
public class HistorifyBridge {

	private static final String N = "Historify.Bridge";

	/**
	 * 
	 * The purpose of this class is to facilitate dealing with contextual data
	 * of the client application when QuickPosting.<br/>
	 * <br/>
	 * A QuickPost client application always have to set up a proper context
	 * before QuickPosting by instantiating a new <code>QuickPostContext</code>
	 * and providing mandatory and optional parameters. The Context must contain
	 * the name and the version of the source. Other parameters are optional.
	 * 
	 * @author berke.andras
	 */
	public static class QuickPostContext {

		private String sourceName;
		private String sourceDescription;
		private String iconUri;
		private String eventIntent;
		private String interactIntent;
		private String interactActionTitle;
		private int version;

		/**
		 * Creates a new QuickPostContext.
		 * 
		 * @param sourceName
		 *            The name of the QuickPost source used when displaying
		 *            events in Historify. Could not be <b>null</b>.
		 * @param sourceDescription
		 *            The brief description of the QuickPost source. Could be
		 *            null.
		 * @param iconUri
		 *            The URI of a drawable resource used as an icon when
		 *            displaying events in Historify. The following schema are
		 *            supported: <code>android.resource://</code>,
		 *            <code>file://</code>, <code>content://</code>
		 * @param version
		 *            The version of the QuickPost source. By incrementing the
		 *            version value, client classes could update the contextual
		 *            data stored in Historify. Re-registering a context with
		 *            the same version value as previously will not take effect.
		 * 
		 * @throws NullPointerException
		 *             if sourceName is null.
		 */
		public QuickPostContext(String sourceName, String sourceDescription,
				String iconUri, int version) {

			this.sourceName = sourceName;
			this.sourceDescription = sourceDescription;
			this.iconUri = iconUri;
			this.version = version;

			if (this.sourceName == null) {
				throw new NullPointerException("Source name cannot be null.");
			}
		}

		/**
		 * Sets the <code>EVENT_INTENT</code> optional field of this QuickPost
		 * source.<br/>
		 * <br/>
		 * The event intent is fired by Historify when the user selects an event
		 * on timeline posted by this QuickPost source. Note that to let the
		 * receiver identify which event has been selected by the user,
		 * {@link Actions#EXTRA_EVENT_KEY} will be passed as Intent extra.
		 * 
		 * @param eventIntent
		 *            The action field of the <code>Intent</code> which should
		 *            be fired.
		 */
		public void setEventIntent(String eventIntent) {
			this.eventIntent = eventIntent;
		}

		/**
		 * Sets the <code>INTERACT_INTENT</code> optional field of this
		 * QuickPost source.<br/>
		 * <br/>
		 * The interact intent is fired by Historify when the user selects the
		 * interaction type associated with this QuickPost source in the
		 * timeline popup menu. Note that to let the receiver identify which
		 * contact has been selected by the user,
		 * {@link Actions#EXTRA_CONTACT_LOOKUP_KEY} will be passed as Intent
		 * extra.<br/>
		 * <br/>
		 * If the interact intent is not set in the QuickPost context, no
		 * interaction type will be shown for this QuickPost source.<br/>
		 * <br/>
		 * 
		 * @param interactIntent
		 *            The action field of the <code>Intent</code> which should
		 *            be fired.
		 * @param interactActionTitle
		 *            The label used to describe the interaction type of this
		 *            QuickPost source (e.g. "Call", "Compose").
		 */
		public void setInteractIntent(String interactIntent,
				String interactActionTitle) {
			this.interactIntent = interactIntent;
			this.interactActionTitle = interactActionTitle;
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

		public String getEventIntent() {
			return eventIntent;
		}

		public String getInteractIntent() {
			return interactIntent;
		}

		public String getInteractActionTitle() {
			return interactActionTitle;
		}
	}

	/**
	 * Broadcast receiver for processing the
	 * {@link Actions#BROADCAST_REQUEST_REGISTER_SOURCE} Intent sent by
	 * Historify.<br/>
	 * <br/>
	 * Derived classes must override the
	 * {@link #onRequestRegister(Context context)} method which is called when
	 * this component receives the <code>REQUEST_REGISTER_SOURCE</code> intent.
	 * Default implementation should set up a new {@link SourceData} instance
	 * and register it in Historify by calling
	 * {@link #registerSource(Context context, SourceData sourceData)}. See SDK
	 * sample app for an example.
	 * 
	 * @author berke.andras
	 */
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

	/**
	 * 
	 * The purpose of this class is to facilitate dealing with source data of
	 * the client application when registering a SharedSource.<br/>
	 * <br/>
	 * SharedSource client application may use a SourceData instance to describe
	 * the SharedSource to be registered by providing mandatory and optional
	 * parameters.<br/>
	 * <br/>
	 * A SharedSource's SourceData must always contain the name of the source
	 * and the authority of the provider used when querying events. Other
	 * parameters are optional.
	 * 
	 * @author berke.andras
	 */
	public static class SourceData {

		private String name;
		private String authority;
		private String description;
		private String iconUri;
		private String eventIntent;
		private String configIntent;
		private String interactIntent;
		private String interactActionTitle;
		private IconLoadingStrategy iconLoadingStrategy;
		private int version;

		/**
		 * Creates a new SourceData.
		 * 
		 * @param name
		 *            The name of the QuickPost source used when displaying
		 *            events in Historify. Could not be <b>null</b>.
		 * @param authority
		 *            The authority of the ContentProvider used by Historify to
		 *            query the events of this source. Could not be <b>null</b>.
		 * @param description
		 *            The brief description of the SharedSource. Could be null.
		 * @param iconUri
		 *            The URI of a drawable resource used as an icon when
		 *            displaying events in Historify. The following schema are
		 *            supported: <code>android.resource://</code>,
		 *            <code>file://</code>, <code>content://</code>
		 * @param version
		 *            The version of the SharedSource. By incrementing the
		 *            version value, client classes could update the SourceData
		 *            already stored in Historify. Re-registering a SharedSource
		 *            with the same version value as previously will not take
		 *            effect.
		 * 
		 * @throws NullPointerException
		 *             if name or authority is null.
		 */
		public SourceData(String name, String authority, String description,
				String iconUri, int version) {
			this.name = name;
			this.authority = authority;
			this.description = description;
			this.iconUri = iconUri;
			this.version = version;
			this.iconLoadingStrategy = IconLoadingStrategy.useSourceIcon;

			if (this.name == null)
				throw new NullPointerException("Source name cannot be null.");

			if (this.authority == null)
				throw new NullPointerException(
						"Provider authority name cannot be null.");
		}

		/**
		 * Sets the <code>EVENT_INTENT</code> optional field of this
		 * SharedSozrce.<br/>
		 * <br/>
		 * The event intent is fired by Historify when the user selects an event
		 * on timeline provided by this SharedSource. Note that to let the
		 * receiver identify which event has been selected by the user,
		 * {@link Actions#EXTRA_EVENT_ID} and {@link Actions#EXTRA_EVENT_KEY}
		 * will be passed as Intent extras.
		 * 
		 * @param eventIntent
		 *            The action field of the <code>Intent</code> which should
		 *            be fired.
		 */
		public void setEventIntent(String eventIntent) {
			this.eventIntent = eventIntent;
		}

		/**
		 * Sets the <code>CONFIG_INTENT</code> optional field of this
		 * SharedSource.<br/>
		 * <br/>
		 * The config intent may used by client applications to launch an
		 * Activity which lets the user could customize the behavior of the
		 * client and the events provided by the SharedSource.<br/>
		 * <br/>
		 * The Intent is fired by Historify when the user selects the 'more'
		 * button of this SharedSource in the 'my sources' menu. If the config
		 * intent is not set in the SourceData, no 'more' button will shown.<br/>
		 * <br/>
		 * 
		 * @param configIntent
		 *            The action field of the <code>Intent</code> which should
		 *            be fired.
		 */
		public void setConfigIntent(String configIntent) {
			this.configIntent = configIntent;
		}

		/**
		 * Sets the <code>INTERACT_INTENT</code> optional field of this
		 * SharedSource.<br/>
		 * <br/>
		 * The interact intent is fired by Historify when the user selects the
		 * interaction type associated with this SharedSource in the timeline
		 * popup menu. Note that to let the receiver identify which contact has
		 * been selected by the user, {@link Actions#EXTRA_CONTACT_LOOKUP_KEY}
		 * will be passed as Intent extra.<br/>
		 * <br/>
		 * If the interact intent is not set in the SourceData, no interaction
		 * type will be shown for this SharedSource.<br/>
		 * <br/>
		 * 
		 * @param interactIntent
		 *            The action field of the <code>Intent</code> which should
		 *            be fired.
		 * @param interactActionTitle
		 *            The label used to describe the interaction type of this
		 *            SharedSource (e.g. "Call", "Compose").
		 */
		public void setInteractIntent(String interactIntent,
				String interactActionTitle) {
			this.interactIntent = interactIntent;
			this.interactActionTitle = interactActionTitle;
		}

		/**
		 * Sets the <code>ICON_LOADING_STRATEGY</code> optional field of this
		 * SharedSource.<br/>
		 * <br/>
		 * The icon loading strategy defines that timeline icons for a
		 * particular source should be loaded from the source itself, or the
		 * custom icon of the event should be used.<br/>
		 * <br/>
		 * If not set, default value will be used which is
		 * {@link IconLoadingStrategy#useSourceIcon}
		 * 
		 * @param iconLoadingStrategy
		 *            The icon loading strategy that should be used for this
		 *            SharedSource.
		 */
		public void setIconLoadingStrategy(
				IconLoadingStrategy iconLoadingStrategy) {
			this.iconLoadingStrategy = iconLoadingStrategy;
		}

		public String getName() {
			return name;
		}

		public String getAuthority() {
			return authority;
		}

		public String getDescription() {
			return description;
		}

		public String getIconUri() {
			return iconUri;
		}

		public String getInteractIntent() {
			return interactIntent;
		}

		public String getInteractActionTitle() {
			return interactActionTitle;
		}

		public int getVersion() {
			return version;
		}

		public String getEventIntent() {
			return eventIntent;
		}

		public String getConfigIntent() {
			return configIntent;
		}

		public IconLoadingStrategy getIconLoadingStrategy() {
			return iconLoadingStrategy;
		}
	}

	private int mIconResource;
	private QuickPostContext mQuickPostContext;

	/**
	 * Public constructor.
	 * 
	 * @param iconResource
	 *            Icon used when posting notifications.
	 */
	public HistorifyBridge(int iconResource) {
		this.mIconResource = iconResource;

	}

	/**
	 * Checks if Historify Bridge is available on the system and able to handle
	 * QuickPosts.
	 * 
	 * @param context
	 *            Android context.
	 * @return <b>true</b> if the Bridge is available, <b>false</b> otherwise.
	 */
	public boolean canQuickPost(Context context) {

		Intent intent = new Intent();
		intent.setAction(Actions.ACTION_QUICK_POST);

		List<ResolveInfo> info = context.getPackageManager()
				.queryIntentServices(intent, PackageManager.MATCH_DEFAULT_ONLY);
		return info.size() != 0;

	}

	/**
	 * Setup the context for QuickPosting.<br/>
	 * <br/>
	 * Client must call this method if it is intended to post QuickPost events
	 * to Historify to set up the parameters of the source used when registering
	 * the QuickPost source in Historify.<br/>
	 * <br/>
	 * Note that after setting up the context, QuickPosts could be sent by
	 * calling {@link #quickPost(Context context, EventData eventData)}
	 * 
	 * @param quickPostContext
	 *            The context that should be used when QuickPosting.
	 */
	public void setQuickPostContext(QuickPostContext quickPostContext) {
		this.mQuickPostContext = quickPostContext;
	}

	/**
	 * Add a QuickPost event to Historify.<br/>
	 * <br/>
	 * Note that {@link #setQuickPostContext(QuickPostContext quickPostContext)}
	 * must be called at least once before the client could use this method.
	 * 
	 * @param context
	 *            Android context.
	 * @param eventData
	 *            The data of the event that should be posted in Historify.
	 * 
	 * @throw NullPointerException if the current QuickPost context is invalid
	 *        or eventData is <code>null</code>.
	 */
	public void quickPost(Context context, EventData eventData) {

		if (mQuickPostContext == null) {
			throw new NullPointerException("QuickPost context is not set.");
		}

		if (eventData == null) {
			throw new NullPointerException("Event data cannot be null.");
		}

		int uid = determineUid(context);

		if (uid == 0) {
			Log.e(N, "Cannot determine package UID.");
			return;
		}

		Intent intent = new Intent();
		intent.setAction(Actions.ACTION_QUICK_POST);

		// quickpost source data
		intent.putExtra(Actions.EXTRA_SOURCE_NAME, mQuickPostContext
				.getSourceName());
		intent.putExtra(Actions.EXTRA_SOURCE_DESCRIPTION, mQuickPostContext
				.getSourceDescription());
		intent.putExtra(Actions.EXTRA_SOURCE_ICON_URI, mQuickPostContext
				.getIconUri());
		intent.putExtra(Actions.EXTRA_SOURCE_UID, uid);
		intent.putExtra(Actions.EXTRA_SOURCE_VERSION, mQuickPostContext
				.getVersion());
		intent.putExtra(Actions.EXTRA_EVENT_INTENT, mQuickPostContext
				.getEventIntent());
		intent.putExtra(Actions.EXTRA_INTERACT_INTENT, mQuickPostContext
				.getInteractIntent());
		intent.putExtra(Actions.EXTRA_INTERACT_ACTION_TITLE, mQuickPostContext
				.getInteractActionTitle());

		// quickpost event data
		intent.putExtra(Events.EVENT_KEY, eventData.getEventKey());
		intent.putExtra(Events.CONTACT_KEY, eventData.getContactKey());
		intent.putExtra(Events.PUBLISHED_TIME, eventData.getPublishedTime());
		intent.putExtra(Events.MESSAGE, eventData.getMessage());
		intent.putExtra(Events.ORIGINATOR, eventData.getOriginator());

		postIntent(context, intent);
	}

	/**
	 * Register a SharedSource in Historify.<br/>
	 * <br/>
	 * Client may use this method to insert or update a source of events in
	 * Historify. <br/>
	 * <br/>
	 * Note that the suggested behavior for client applications is to define a
	 * {@link #RequestReceiver} and place a call of this method into the
	 * {@link RequestReceiver#onRequestRegister(Context)} function. See SDK
	 * sample app for an example.
	 * 
	 * @param context
	 *            Android context.
	 * @param sourceData
	 *            The metadata of the source that should be registered in
	 *            Historify.
	 * 
	 * @throw NullPointerException if sourceData is <code>null</code>.
	 */
	public void registerSource(Context context, SourceData sourceData) {

		if (sourceData == null) {
			throw new NullPointerException("Source data cannot be null.");
		}

		// determine application's uid
		int uid = determineUid(context);

		if (uid == 0) {
			Log.e(N, "Cannot determine package UID.");
			return;
		}

		Intent intent = new Intent();
		intent.setAction(Actions.ACTION_REGISTER_SOURCE);
		intent.putExtra(Actions.EXTRA_SOURCE_NAME, sourceData.getName());
		intent.putExtra(Actions.EXTRA_SOURCE_AUTHORITY, sourceData
				.getAuthority());
		intent.putExtra(Actions.EXTRA_SOURCE_UID, uid);
		intent.putExtra(Actions.EXTRA_SOURCE_DESCRIPTION, sourceData
				.getDescription());
		intent.putExtra(Actions.EXTRA_SOURCE_ICON_URI, sourceData.getIconUri());
		intent.putExtra(Actions.EXTRA_SOURCE_VERSION, sourceData.getVersion());
		intent
				.putExtra(Actions.EXTRA_EVENT_INTENT, sourceData
						.getEventIntent());
		intent.putExtra(Actions.EXTRA_CONFIG_INTENT, sourceData
				.getConfigIntent());
		intent.putExtra(Actions.EXTRA_INTERACT_INTENT, sourceData
				.getInteractIntent());
		intent.putExtra(Actions.EXTRA_INTERACT_ACTION_TITLE, sourceData
				.getInteractActionTitle());
		intent.putExtra(Actions.EXTRA_SOURCE_ICON_LOADING_STRATEGY, sourceData
				.getIconLoadingStrategy().toString());

		postIntent(context, intent);
	}

	private int determineUid(Context context) {

		String packageName = context.getPackageName();
		int uid = 0;
		try {
			uid = context.getPackageManager().getPackageInfo(packageName, 0).applicationInfo.uid;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		return uid;
	}

	private void postIntent(Context context, Intent intent) {

		try {
			context.startService(intent);
		} catch (SecurityException se) {
			// can't access service, maybe it's not installed,
			// or there are other configuration or permission problems.
			postNotification(context, "Application Error",
					"Unable to communicate with Historify. Reinstalling might solve the issue.");
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
