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

import org.openintents.historify.data.providers.Events;
import org.openintents.historify.data.providers.Events.Originator;
import org.openintents.historify.data.providers.internal.QuickPosts;
import org.openintents.historify.data.providers.internal.QuickPosts.QuickPostEventsTable;
import org.openintents.historify.data.providers.internal.QuickPosts.QuickPostSourcesTable;
import org.openintents.historify.uri.Actions;
import org.openintents.historify.uri.ContentUris;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * 
 * Helper class for handling the ACTION_QUICK_POST Intent.
 * 
 * @author berke.andras
 */
public class QuickPostHelper {

	public void quickPost(Context context, Bundle parameterSet) {
		
		if (parameterSet == null) {
			Log.e(BridgeService.N, "No parameters provided");
			return;
		}
		
		int uid = parameterSet.getInt(Actions.EXTRA_SOURCE_UID);
		int version = parameterSet.getInt(Actions.EXTRA_SOURCE_VERSION);
		
		if(uid==0) {
			Log.e(BridgeService.N, "Source uid is a mandatory parameter!");
			return;
		}
		
		//check if the QuickPostProvider stores the latest version of this QuickPost source
		ContentResolver resolver = context.getContentResolver();
		String where = QuickPostSourcesTable.UID + " = "+uid;
		Long sourceId = null;
		
		Cursor c = resolver.query(ContentUris.QuickPostSources, new String[] {
				QuickPostSourcesTable._ID, QuickPostSourcesTable.VERSION
		}, where, null, null);
		if(c.moveToFirst()) {
			//source already registered
			//check if the version provided by the Intent is greater than the stored value 
			if(version>c.getLong(1)) {
				//newer version
				sourceId = insertOrUpdateQuickPostSource(resolver, parameterSet,c.getLong(0));
			} else {
				sourceId = c.getLong(0);
			}
		} else {
			//source not registered yet
			sourceId = insertOrUpdateQuickPostSource(resolver, parameterSet, null);
		}
		c.close();
		
		//source is up to date
		//now we could insert the posted event
		if(sourceId!=null) {
			
			String eventKey = parameterSet.getString(Events.EVENT_KEY);
			
			//if the caller provided an EVENT_KEY, we have to check if the associated
			//row is already stored.
			if(eventKey!=null) {
				Uri eventUri = 
					QuickPosts.SOURCE_URI.buildUpon()
					.appendPath(Events.EVENTS_PATH)
					.appendPath(Events.EVENTS_BY_EVENT_KEYS_PATH)
					.appendPath(eventKey).build();
				
				c = resolver.query(eventUri, new String[] {
						QuickPostSourcesTable._ID
				}, null, null, null);
				
				if(c.moveToFirst()) {
					//event already stored
					//won't insert it again
					Log.w(BridgeService.N,"Event already stored.");
					c.close();
					return;
				}
				
				c.close();
			}
			
			//event haven't stored yet.
			insertQuickPostEvent(resolver, sourceId, parameterSet);
		}
				
	}

	private void insertQuickPostEvent(ContentResolver resolver, Long sourceId,
			Bundle parameterSet) {
	
		String eventKey = parameterSet.getString(Events.EVENT_KEY);
		String contactKey = parameterSet.getString(Events.CONTACT_KEY);
		long publishedTime = parameterSet.getLong(Events.PUBLISHED_TIME);
		String message = parameterSet.getString(Events.MESSAGE);
		Originator originator = (Originator) parameterSet.get(Events.ORIGINATOR);

		ContentValues cv = new ContentValues();
		cv.put(Events.EVENT_KEY, eventKey);
		cv.put(Events.CONTACT_KEY, contactKey);
		cv.put(Events.PUBLISHED_TIME, publishedTime);
		cv.put(Events.MESSAGE, message);
		cv.put(Events.ORIGINATOR, originator.toString());
		cv.put(QuickPostEventsTable.SOURCE_ID, sourceId);
		
		resolver.insert(Uri.withAppendedPath(QuickPosts.SOURCE_URI,Events.EVENTS_PATH), cv);
	}

	private Long insertOrUpdateQuickPostSource(ContentResolver resolver, Bundle parameterSet, Long updateRow) {

		String name = parameterSet.getString(Actions.EXTRA_SOURCE_NAME);
		String description = parameterSet.getString(Actions.EXTRA_SOURCE_DESCRIPTION);
		String iconUri = parameterSet.getString(Actions.EXTRA_SOURCE_ICON_URI);
		int uid = parameterSet.getInt(Actions.EXTRA_SOURCE_UID);
		int version = parameterSet.getInt(Actions.EXTRA_SOURCE_VERSION);
		
		ContentValues values = new ContentValues();
		values.put(QuickPostSourcesTable.NAME, name);
		values.put(QuickPostSourcesTable.DESCRIPTION, description);
		values.put(QuickPostSourcesTable.ICON_URI, iconUri);
		values.put(QuickPostSourcesTable.UID, uid);
		values.put(QuickPostSourcesTable.VERSION, version);
		
		if(updateRow==null) {
			//insert
			Uri rowUri = resolver.insert(ContentUris.QuickPostSources, values);
			return rowUri == null ? null : Long.valueOf(rowUri.getLastPathSegment());
		} else {
			//update
			String where = QuickPostSourcesTable._ID + " = "+updateRow;
			int res = resolver.update(ContentUris.QuickPostSources, values, where, null);
			return res==0 ? null : updateRow;
		}
		
	}

}
