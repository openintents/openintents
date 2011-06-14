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
import org.openintents.historify.SourcesActivity;
import org.openintents.historify.data.providers.Sources;
import org.openintents.historify.uri.Actions;
import org.openintents.historify.uri.ContentUris;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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
		String description = parameterSet
				.getString(Actions.EXTRA_SOURCE_DESCRIPTION);
		String iconUri = parameterSet.getString(Actions.EXTRA_SOURCE_ICON_URI);

		if (name == null && authority == null) {
			Log.e(BridgeService.N,
					"Source name and authority are mandatory parameters!");
			return;
		}

		ContentResolver resolver = context.getContentResolver();

		// check if authority already registered
		Cursor cursor = resolver.query(ContentUris.Sources, null,
				Sources.SourcesTable.AUTHORITY + " = ?",
				new String[] { authority }, null);

		if (cursor.moveToNext()) {
			Log.w(BridgeService.N, "Source for authority '" + authority
					+ "' has been already registered.");
			cursor.close();
			return;
		}

		cursor.close();

		ContentValues cv = new ContentValues();
		cv.put(Sources.SourcesTable.NAME, name);
		cv.put(Sources.SourcesTable.AUTHORITY, authority);
		if (description != null)
			cv.put(Sources.SourcesTable.DESCRIPTION, description);
		if (iconUri != null)
			cv.put(Sources.SourcesTable.ICON_URI, iconUri);

		resolver.insert(ContentUris.Sources, cv);

		Log
				.v(BridgeService.N, "Source '" + name
						+ "' registered successfully.");
		
		postNotification(context, name, iconUri);
	}

	private void postNotification(Context context, String name, String iconUri) {
		
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
		
		
	}

}
