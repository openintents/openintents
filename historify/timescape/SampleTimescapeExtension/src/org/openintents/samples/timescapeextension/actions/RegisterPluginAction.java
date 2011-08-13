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

package org.openintents.samples.timescapeextension.actions;

import java.util.ArrayList;

import org.openintents.samples.timescapeextension.ConfigActivity;
import org.openintents.samples.timescapeextension.PluginService;
import org.openintents.samples.timescapeextension.R;

import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;

import com.sonyericsson.eventstream.PluginConstants.EventStream;
import com.sonyericsson.eventstream.PluginConstants.EventStream.FriendColumns;

/**
 * Registering this extension, its sample source and its test friend
 *
 */
public class RegisterPluginAction extends AbstractAction {

	public RegisterPluginAction(Context context) {
		super(context);
	}

	@Override
	public void run() {
		insertOrUpdatePlugin();

		boolean alreadyContains = insertOrUpdateSource();
		if (!alreadyContains)
			insertFriend();

		// launch broadcast itent for adding events
		Intent intent = new Intent();
		intent.setAction(EventStream.Intents.REFRESH_REQUEST_INTENT);
		mContext.sendBroadcast(intent);

	}

	private void insertOrUpdatePlugin() {

		Cursor cursor = null;
		try {
			cursor = getContentResolver().query(
					EventStream.EVENTSTREAM_PLUGIN_PROVIDER_URI, null, null,
					null, null);

			ContentValues pluginRegistrationValues = new ContentValues();

			pluginRegistrationValues.put(
					EventStream.PluginColumns.STATUS_SUPPORT,
					EventStream.StatusSupport.HAS_SUPPORT_FALSE);
			pluginRegistrationValues.put(EventStream.PluginColumns.NAME,
					getString(R.string.plugin_name));

			Builder iconUriBuilder = new Uri.Builder().scheme(
					ContentResolver.SCHEME_ANDROID_RESOURCE).authority(
					getPackageName()).appendPath(
					Integer.toString(R.drawable.icon));
			pluginRegistrationValues.put(EventStream.PluginColumns.ICON_URI,
					iconUriBuilder.toString());

			pluginRegistrationValues.put(EventStream.PluginColumns.API_VERSION,
					PluginService.PLUGIN_VERSION);

			ComponentName componentName = new ComponentName(getPackageName(),
					ConfigActivity.class.getName());
			pluginRegistrationValues.put(
					EventStream.PluginColumns.CONFIGURATION_ACTIVITY,
					componentName.flattenToShortString());
			pluginRegistrationValues.put(
					EventStream.PluginColumns.CONFIGURATION_STATE,
					EventStream.ConfigState.CONFIGURATION_NOT_NEEDED);

			if (cursor != null && cursor.moveToNext() && cursor.getCount() > 0) {
				// Update the plugin data
				getContentResolver().update(
						EventStream.EVENTSTREAM_PLUGIN_PROVIDER_URI,
						pluginRegistrationValues, null, null);
			} else {
				// Register ourself for the first time
				getContentResolver().insert(
						EventStream.EVENTSTREAM_PLUGIN_PROVIDER_URI,
						pluginRegistrationValues);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}
	}

	private boolean insertOrUpdateSource() {

		Cursor cursor = null;
		boolean retval = false;
		try {
			Builder iconUriBuilder = new Uri.Builder().scheme(
					ContentResolver.SCHEME_ANDROID_RESOURCE).authority(
					getPackageName()).appendPath(
					Integer.toString(R.drawable.icon));

			ContentValues cv = new ContentValues();
			cv.put(EventStream.SourceColumns.ICON_URI, iconUriBuilder
					.toString());
			cv.put(EventStream.SourceColumns.NAME,
					getString(R.string.source_name));

			ContentResolver cr = getContentResolver();
			cursor = cr.query(EventStream.EVENTSTREAM_SOURCE_PROVIDER_URI,
					null, null, null, null);
			if ((null != cursor) && cursor.moveToNext()) {
				cr.update(EventStream.EVENTSTREAM_SOURCE_PROVIDER_URI, cv,
						null, null);
				retval = true;
			} else {
				cv.put(EventStream.SourceColumns.ENABLED, 1); // 1: enabled
				Uri sourceUri = cr.insert(
						EventStream.EVENTSTREAM_SOURCE_PROVIDER_URI, cv);
				if (sourceUri != null)
					PersistentSourceId.set(mContext, sourceUri);
			}
		} finally {
			if (null != cursor) {
				cursor.close();
			}
		}

		return retval;
	}

	private void insertFriend() {

		try {

			long rawContactId = insertRawContact();
			if (rawContactId != -1) {
				ContentValues values = new ContentValues();
				values.put(FriendColumns.CONTACTS_REFERENCE, ContentUris
						.withAppendedId(RawContacts.CONTENT_URI, rawContactId)
						.toString());
				values.put(FriendColumns.SOURCE_ID, PersistentSourceId
						.get(mContext));
				values.put(FriendColumns.FRIEND_KEY, TestFriend.FRIEND_KEY);
				values.put(FriendColumns.DISPLAY_NAME, TestFriend.NAME);
				values.put(FriendColumns.PROFILE_IMAGE_URI,
						TestFriend.IMAGE_URI);
				getContentResolver().insert(
						EventStream.EVENTSTREAM_FRIEND_PROVIDER_URI, values);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private long insertRawContact() {

		ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

		int rawContactInsertIndex = ops.size();
		ops.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
				.withValue(RawContacts.ACCOUNT_TYPE, TestFriend.ACCOUNT_TYPE)
				.withValue(RawContacts.ACCOUNT_NAME, TestFriend.ACCOUNT_NAME)
				.build());

		ops.add(ContentProviderOperation.newInsert(Data.CONTENT_URI)
				.withValueBackReference(Data.RAW_CONTACT_ID,
						rawContactInsertIndex).withValue(Data.MIMETYPE,
						StructuredName.CONTENT_ITEM_TYPE).withValue(
						StructuredName.DISPLAY_NAME, TestFriend.NAME).build());

		try {
			ContentProviderResult[] results = getContentResolver().applyBatch(
					ContactsContract.AUTHORITY, ops);
			return ContentUris.parseId(results[0].uri);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (OperationApplicationException e) {
			e.printStackTrace();
		}

		return -1;
	}
}
