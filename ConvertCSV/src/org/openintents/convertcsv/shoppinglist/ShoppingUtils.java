/* 
 * Copyright (C) 2008 OpenIntents.org
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

package org.openintents.convertcsv.shoppinglist;

import org.openintents.provider.Shopping.Contains;
import org.openintents.provider.Shopping.ContainsFull;
import org.openintents.provider.Shopping.Items;
import org.openintents.provider.Shopping.Lists;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class ShoppingUtils {

	private static final String TAG = "ShoppingUtils";
	
	public static final String[] PROJECTION_LISTS = new String[] { Lists._ID,
			Lists.NAME, Lists.IMAGE, Lists.SHARE_NAME, Lists.SHARE_CONTACTS,
			Lists.SKIN_BACKGROUND };

	public static final String[] PROJECTION_CONTAINS_FULL = new String[] {
			ContainsFull._ID, ContainsFull.ITEM_NAME, ContainsFull.ITEM_IMAGE,
			ContainsFull.STATUS, ContainsFull.ITEM_ID, ContainsFull.LIST_ID,
			ContainsFull.SHARE_CREATED_BY, ContainsFull.SHARE_MODIFIED_BY };

	public static long getOrCreateListId(Context context, String listName) {
		Cursor cursor = context.getContentResolver().query(Lists.CONTENT_URI,
				new String[] { Lists._ID, Lists.NAME }, Lists.NAME + " = ?",
				new String[] { listName }, Lists.DEFAULT_SORT_ORDER);
		if (cursor != null && cursor.moveToNext()) {
			return cursor.getLong(cursor.getColumnIndexOrThrow(Lists._ID));
		} else {
			ContentValues values = new ContentValues(1);
			values.put(Lists.NAME, listName);
			Uri uri = context.getContentResolver().insert(Lists.CONTENT_URI,
					values);
			Log.i(TAG, "Insert new list: " + uri);
			return Long.parseLong(uri.getPathSegments().get(1));
		}
	}

	public static long getItemId(Context context, final String name) {
		// TODO check whether item exists

		// Add item to list:
		ContentValues values = new ContentValues(1);
		values.put(Items.NAME, name);
		try {
			Uri uri = context.getContentResolver().insert(Items.CONTENT_URI,
					values);
			Log.i(TAG, "Insert new item: " + uri);
			return Long.parseLong(uri.getPathSegments().get(1));
		} catch (Exception e) {
			Log.i(TAG, "Insert item failed", e);
			return -1;
		}
	}

	public static long addItemToList(Context context, final long itemId,
			final long listId, final long status) {
		// TODO check whether "contains" entry exists

		// Add item to list:
		ContentValues values = new ContentValues(2);
		values.put(Contains.ITEM_ID, itemId);
		values.put(Contains.LIST_ID, listId);
		values.put(Contains.STATUS, status);
		try {
			Uri uri = context.getContentResolver().insert(Contains.CONTENT_URI,
					values);
			Log.i(TAG, "Insert new entry in 'contains': "
					+ uri);
			return Long.parseLong(uri.getPathSegments().get(1));
		} catch (Exception e) {
			Log.i(TAG,
					"insert into table 'contains' failed", e);
			return -1;
		}
	}

}
