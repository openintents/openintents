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

import org.openintents.historify.R;
import org.openintents.historify.data.loaders.SourceFilterOperation;
import org.openintents.historify.data.model.source.EventSource.SourceState;
import org.openintents.historify.data.providers.Sources;
import org.openintents.historify.data.providers.Sources.SourcesTable;
import org.openintents.historify.ui.SourcesActivity;
import org.openintents.historify.uri.Actions;
import org.openintents.historify.uri.ContentUris;
import org.openintents.historify.utils.UriUtils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * Helper class used by {@link BridgeService} to insert or update the state of
 * SharedSources.
 * 
 * @author berke.andras
 */
public class SourceRegistrationHelper {

	public void registerSource(Context context, Bundle parameterSet) {

		if (parameterSet == null) {
			Log.e(BridgeService.N, "No parameters provided");
			return;
		}

		String name = parameterSet.getString(Actions.EXTRA_SOURCE_NAME);
		String authority = parameterSet
				.getString(Actions.EXTRA_SOURCE_AUTHORITY);
		int uid = parameterSet.getInt(Actions.EXTRA_SOURCE_UID);
		int version = parameterSet.getInt(Actions.EXTRA_SOURCE_VERSION);
		
//		String description = parameterSet
//				.getString(Actions.EXTRA_SOURCE_DESCRIPTION);
		String iconUri = parameterSet.getString(Actions.EXTRA_SOURCE_ICON_URI);
		
		if(iconUri==null) {
			//caller didn't provided an icon. caller application's app icon will be used
			iconUri = UriUtils.getAppIconUri(context, uid).toString();
			parameterSet.putString(Actions.EXTRA_SOURCE_ICON_URI, iconUri);
		}

		if (name == null || authority == null || uid == 0) {
			Log.e(BridgeService.N,
					"Source name, authority and uid are mandatory parameters!");
			return;
		}

		ContentResolver resolver = context.getContentResolver();

		// check if authority already registered
		Cursor cursor = resolver.query(ContentUris.Sources, new String[] {Sources.SourcesTable.VERSION, Sources.SourcesTable._ID, Sources.SourcesTable.UID},
				Sources.SourcesTable.AUTHORITY + " = ?",
				new String[] { authority }, null);
		
		Long sourceId = null;
		
		if (cursor.moveToNext()) {
			
			//if the source is registered by an application with a different uid
			//we have to ignore the register request.
			if(cursor.getInt(2)!=uid) {
				Log.w(BridgeService.N, "Source for authority '" + authority
						+ "' has been already registered by a different application.");
				cursor.close();
				return;
			} else {
				//check if we store the latest version of this source
				if(cursor.getInt(0)!=version) {
					//update source in the sources table
					Log
					.v(BridgeService.N, "Updating '" + name
							+ "' source.");
					sourceId = cursor.getLong(1);
				} else {
					//same version, we do nothing
					Log
					.v(BridgeService.N, "Source '" + name
							+ "' already registered.");
					cursor.close();
					return;
				}
			}
		}

		cursor.close();
		
		Long newSourceId = insertOrUpdateSource(resolver, parameterSet, sourceId);
		
		Log
				.v(BridgeService.N, "Source '" + name
						+ "' registered successfully.");
		
		if(sourceId==null) {
			//new source ha been added
			//we have to insert source filters for all the contacts
			//that are already filtered
			new SourceFilterOperation().insertFiltersForNewSource(context, newSourceId, SourceState.ENABLED);
		}
		
		
		postNotification(context, name);
	}

	private Long insertOrUpdateSource(ContentResolver resolver, Bundle parameterSet, Long updateRow) {
		
		
		String name = parameterSet.getString(Actions.EXTRA_SOURCE_NAME);
		String description = parameterSet.getString(Actions.EXTRA_SOURCE_DESCRIPTION);
		String authority = parameterSet.getString(Actions.EXTRA_SOURCE_AUTHORITY);
		String iconUri = parameterSet.getString(Actions.EXTRA_SOURCE_ICON_URI);
		int uid = parameterSet.getInt(Actions.EXTRA_SOURCE_UID);
		int version = parameterSet.getInt(Actions.EXTRA_SOURCE_VERSION);
		
		String eventIntent = parameterSet.getString(Actions.EXTRA_EVENT_INTENT);
		String configIntent = parameterSet.getString(Actions.EXTRA_CONFIG_INTENT);
		String interactIntent = parameterSet.getString(Actions.EXTRA_INTERACT_INTENT);
		String interactActionTitle = parameterSet.getString(Actions.EXTRA_INTERACT_ACTION_TITLE);
		
		ContentValues values = new ContentValues();
		values.put(SourcesTable.NAME, name);
		values.put(SourcesTable.DESCRIPTION, description);
		values.put(SourcesTable.AUTHORITY, authority);
		values.put(SourcesTable.ICON_URI, iconUri);
		values.put(SourcesTable.EVENT_INTENT, eventIntent);
		values.put(SourcesTable.CONFIG_INTENT, configIntent);
		values.put(SourcesTable.INTERACT_INTENT, interactIntent);
		values.put(SourcesTable.INTERACT_ACTION_TITLE, interactActionTitle);
		values.put(SourcesTable.UID, uid);
		values.put(SourcesTable.VERSION, version);
		
		if(updateRow==null) {
			//insert
			Uri rowUri = resolver.insert(ContentUris.Sources, values);
			return rowUri == null ? null : Long.valueOf(rowUri.getLastPathSegment());
		} else {
			//update
			String where = SourcesTable._ID + " = "+updateRow;
			int res = resolver.update(ContentUris.Sources, values, where, null);
			return res==0 ? null : updateRow;
		}		
	}
	
	private void postNotification(Context context, String name) {
		
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new Notification(R.drawable.icon, context.getString(R.string.notification_source_added_ticker), System.currentTimeMillis());
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		
		Intent intent = new Intent(context,SourcesActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
		
		notification.setLatestEventInfo(context, name, context.getString(R.string.notification_source_added), pendingIntent);
		nm.notify(0, notification);
		
	}

	public void requestRegisterSource(Context context, Integer uid) {
		
		try {
			
			Intent broadCast = new Intent();
			broadCast.setAction(Actions.BROADCAST_REQUEST_REGISTER_SOURCE);
			
			if(uid!=null) { //addressed request for a particular external source
				
				String[] packages = context.getPackageManager().getPackagesForUid(uid);
				String packageName = packages[0]; //shared uids not supported
				
				if(packageName!=null) {
					Log.v(BridgeService.N,"-- "+packageName);
					
					broadCast.putExtra(Actions.EXTRA_ADDRESSED, true);
					broadCast.putExtra(Actions.EXTRA_PACKAGE_NAME, packageName);
					context.sendBroadcast(broadCast);
				}
			} else { //request for all external sources
				broadCast.putExtra(Actions.EXTRA_ADDRESSED, false);
				context.sendBroadcast(broadCast);
			}
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void unregisterSource(Context context, int uid) {

		
		String where = SourcesTable.UID + " = "+uid;
		Cursor c = context.getContentResolver().query(ContentUris.Sources, new String[] {SourcesTable._ID}, 
				where, null, null); 
		if(c.moveToFirst()) {
			//there is a registered source in the uninstalled package
		
			long sourceId = c.getLong(0);
		
			new SourceFilterOperation().removeFiltersOfDeletedSource(context, sourceId);

			context.getContentResolver().delete(ContentUris.Sources, where, null);
			
		}

	}

}
