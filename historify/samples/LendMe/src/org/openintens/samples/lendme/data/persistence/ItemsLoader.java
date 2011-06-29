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

package org.openintens.samples.lendme.data.persistence;

import org.openintens.samples.lendme.data.HistorifyPostHelper;
import org.openintens.samples.lendme.data.Item;
import org.openintens.samples.lendme.data.Item.Owner;
import org.openintens.samples.lendme.data.persistence.ItemsProviderHelper.ItemsTable;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

public class ItemsLoader {

	public static String[] PROJECTION = new String[] {
		ItemsTable._ID,
		ItemsTable.CONTACT_KEY,
		ItemsTable.LENDING_START,
		ItemsTable.ITEM_NAME,
		ItemsTable.ITEM_DESCRIPTION,
		ItemsTable.OWNER
	};
	
	private static final int COLUMN_ID = 0;
	private static final int COLUMN_CONTACT_KEY = 1;
	private static final int COLUMN_LENDING_START = 2;
	private static final int COLUMN_ITEM_NAME = 3;
	private static final int COLUMN_ITEM_DESCRIPTION = 4;
	private static final int COLUMN_OWNER = 5;
		
	public Cursor openCursor(Context context, Owner owner) {
		
		Uri contentUri = ItemsProviderHelper.CONTENT_URI;
		String where = ItemsTable.OWNER + " = '"+owner.toString()+"'";
		
		return context.getContentResolver().query(contentUri, null, where, null, ItemsTable.LENDING_START+ " DESC");
	}
	
	public Item loadFromCursor(Cursor cursor, int position) {
		
		cursor.moveToPosition(position);
				
		return new Item(
				cursor.getLong(COLUMN_ID),
				cursor.getString(COLUMN_CONTACT_KEY),
				Owner.parseString(cursor.getString(COLUMN_OWNER)),
				cursor.getLong(COLUMN_LENDING_START),
				cursor.getString(COLUMN_ITEM_NAME),
				cursor.getString(COLUMN_ITEM_DESCRIPTION));
	}

	public long insert(Context context, Bundle parameterSet) {
		
		Uri contentUri = ItemsProviderHelper.CONTENT_URI;
		
		ContentValues values = new ContentValues();
		values.put(ItemsTable.CONTACT_KEY, parameterSet.getString(ItemsTable.CONTACT_KEY));
		values.put(ItemsTable.LENDING_START, System.currentTimeMillis());
		values.put(ItemsTable.ITEM_NAME, parameterSet.getString(ItemsTable.ITEM_NAME));
		
		String desc = parameterSet.getString(ItemsTable.ITEM_DESCRIPTION);
		if(desc!=null)
			values.put(ItemsTable.ITEM_DESCRIPTION, desc);
		
		values.put(ItemsTable.OWNER, parameterSet.getString(ItemsTable.OWNER));
		
		return Long.valueOf(context.getContentResolver().insert(contentUri, values).getLastPathSegment());
	}

	public void delete(Context context, long itemId) {
		
		Uri contentUri = Uri.withAppendedPath(ItemsProviderHelper.CONTENT_URI, String.valueOf(itemId));
		context.getContentResolver().delete(contentUri, null, null);
	}

	public Item query(Context context, String eventKey) {
		
		long id=HistorifyPostHelper.getInstance(context).getItemId(eventKey);
		String where = ItemsTable._ID + " = "+id;
		
		Item retval = null;
		
		Cursor c = context.getContentResolver().query(ItemsProviderHelper.CONTENT_URI, null, where, null, ItemsTable.LENDING_START+ " DESC");
		if(c.getCount()!=0) {
			retval = loadFromCursor(c, 0);
		}
		c.close();
		
		return retval;
	}
}
