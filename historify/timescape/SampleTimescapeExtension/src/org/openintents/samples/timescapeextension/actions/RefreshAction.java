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

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.sonyericsson.eventstream.PluginConstants.EventStream;

/**
 * Syncronize events with EventStream. This sample extension inserts only a single event.
 *
 */
public class RefreshAction extends AbstractAction {

	public RefreshAction(Context context) {
		super(context);
	}

	@Override
	public void run() {
		insertToEventStream();
	}

	protected void insertToEventStream() {
		ContentResolver contentResolver = getContentResolver();

		Cursor cursor = null;
		try {
			cursor = contentResolver.query(
					EventStream.EVENTSTREAM_EVENT_PROVIDER_URI,
					new String[] { BaseColumns._ID }, // dummy projection
					EventStream.EventColumns.EVENT_KEY + " = ?",
					new String[] { "myevent" }, null);

			if (cursor != null && cursor.getCount() == 0) {
				// not registered in event stream yet

				ContentValues values = new ContentValues();
				values.put(EventStream.EventColumns.EVENT_KEY, "myevent");
				values.put(EventStream.EventColumns.SOURCE_ID,
						PersistentSourceId.get(mContext));
				// values.put(EventStream.EventColumns.TITLE, "Title");
				values.put(EventStream.EventColumns.MESSAGE,
						"Message of my event.");
				values.put(EventStream.EventColumns.PUBLISHED_TIME, System
						.currentTimeMillis());
				values.putNull(EventStream.EventColumns.IMAGE_URI);
				values.put(EventStream.EventColumns.OUTGOING, 0);
				values.put(EventStream.EventColumns.PERSONAL, 1);
				values.put(EventStream.FriendColumns.FRIEND_KEY,
						TestFriend.FRIEND_KEY);

				contentResolver.insert(
						EventStream.EVENTSTREAM_EVENT_PROVIDER_URI, values);
			} else {

			}
		} finally {
			if (cursor != null) {
				cursor.close();
			}
		}

	}
}
