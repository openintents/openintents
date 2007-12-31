/* 
 * Copyright (C) 2007 OpenIntents.org
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

package org.openintents.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.ContentURI;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Definition for content provider related to shopping.
 *
 */
public abstract class Shopping {
	
	/**
	 * TAG for logging.
	 */
	private static final String TAG = "Shopping";
	
	/**
	 * Items that can be put into shopping lists.
	 */
	public static final class Items implements BaseColumns {
		/**
         * The content:// style URL for this table.
         */
        public static final ContentURI CONTENT_URI
                = ContentURI.create("content://org.openintents.shopping/items");

        /**
         * The default sort order for this table.
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
       
        /**
         * The name of the item.
         * <P>Type: TEXT</P>
         */
        public static final String NAME = "name";

        /**
         * An image of the item (uri).
         * <P>Type: TEXT</P>
         */
        public static final String IMAGE = "image";

        /**
         * The timestamp for when the item was created.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the item was last modified.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String MODIFIED_DATE = "modified";
        

		/**
		 * The timestamp for when the item was last accessed.
		 * <P>Type: INTEGER (long)</P>
		 */
		public static final String ACCESSED_DATE = "accessed";
    }
	
	/**
	 * Shopping lists that can contain items.
	 */
	public static final class Lists implements BaseColumns {
		/**
         * The content:// style URL for this table.
         */
        public static final ContentURI CONTENT_URI
                = ContentURI.create("content://org.openintents.shopping/lists");

        /**
         * The default sort order for this table.
         */
        public static final String DEFAULT_SORT_ORDER 
        	 = "modified DESC";
        	//= "modified ASC";
        
       
        /**
         * The name of the list.
         * <P>Type: TEXT</P>
         */
        public static final String NAME = "name";

        /**
         * An image of the list (uri).
         * <P>Type: TEXT</P>
         */
        public static final String IMAGE = "image";

        /**
         * The timestamp for when the item was created.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the item was last modified.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String MODIFIED_DATE = "modified";
        

		/**
		 * The timestamp for when the item was last accessed.
		 * <P>Type: INTEGER (long)</P>
		 */
		public static final String ACCESSED_DATE = "accessed";
    }


	/**
	 * Information which list contains which items/lists/(recipes)
	 */
	public static final class Contains implements BaseColumns {
		/**
         * The content:// style URL for this table.
         */
        public static final ContentURI CONTENT_URI
                = ContentURI.create("content://org.openintents.shopping/contains");

        /**
         * The default sort order for this table.
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
       
        /**
         * The id of the item.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String ITEM_ID = "item_id";

        /**
         * The id of the list that contains item_id.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String LIST_ID = "list_id";

        /**
         * Quantity specifier.
         * <P>Type: TEXT</P>
         */
        public static final String QUANTITY = "quantity";
        
        /**
         * Status: WANT_TO_BUY or BOUGHT.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String STATUS = "status";

        /**
         * The timestamp for when the item was created.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the item was last modified.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String MODIFIED_DATE = "modified";
        

		/**
		 * The timestamp for when the item was last accessed.
		 * <P>Type: INTEGER (long)</P>
		 */
		public static final String ACCESSED_DATE = "accessed";
    }

	/**
	 * Combined table of contents, items, and lists.
	 */
	public static final class ContainsFull implements BaseColumns {
		
		/**
         * The content:// style URL for this table.
         */
        public static final ContentURI CONTENT_URI
                = ContentURI.create(
                "content://org.openintents.shopping/containsfull");

        /**
         * The default sort order for this table.
         */
        public static final String DEFAULT_SORT_ORDER 
        	//= "contains.modified DESC";
        	= "contains.modified ASC";
        
        
        // Elements from Contains
        

        /**
         * The id of the item.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String ITEM_ID = "item_id";

        /**
         * The id of the list that contains item_id.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String LIST_ID = "list_id";

        /**
         * Quantity specifier.
         * <P>Type: TEXT</P>
         */
        public static final String QUANTITY = "quantity";
        
        /**
         * Status: WANT_TO_BUY or BOUGHT.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String STATUS = "status";

        /**
         * The timestamp for when the item was created.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the item was last modified.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String MODIFIED_DATE = "modified";
        

		/**
		 * The timestamp for when the item was last accessed.
		 * <P>Type: INTEGER (long)</P>
		 */
		public static final String ACCESSED_DATE = "accessed";

		// Elements from Items
		
        /**
         * The name of the item.
         * <P>Type: TEXT</P>
         */
        public static final String ITEM_NAME = "item_name";

        /**
         * An image of the item (uri).
         * <P>Type: TEXT</P>
         */
        public static final String ITEM_IMAGE = "item_image";

		// Elements from Lists
		
        /**
         * The name of the list.
         * <P>Type: TEXT</P>
         */
        public static final String LIST_NAME = "list_name";

        /**
         * An image of the list (uri).
         * <P>Type: TEXT</P>
         */
        public static final String LIST_IMAGE = "list_image";
	}
	
	/**
	 * Status of "contains" element.
	 */
	public static final class Status {
		
		/**
		 * Want to buy this item.
		 */
		public static final long WANT_TO_BUY = 1;
		
		/**
		 * Have bought this item.
		 */
		public static final long BOUGHT = 2;
		
		/**
		 * Checks whether a status is a valid possibility.
		 * @param s status to be checked.
		 * @return true if status is a valid possibility.
		 */
		public static boolean isValid(final long s) {
			return s == WANT_TO_BUY || s == BOUGHT;
		}
		
	}
	
	// Some convenience functions follow
	
	/**
	 * Adds a new item and returns its id.
	 * If the item exists already, the existing id is returned.
	 * @param cr result from calling getContentResolver() 
	 *                          within your activity.
	 * @param name New name of the item.
	 * @return id of the new or existing item.
	 */
	public static long insertItem(final ContentResolver cr, 
			final String name) {
		// TODO check whether item exists
		
		// Add item to list:
		ContentValues values = new ContentValues(1);
		values.put(Items.NAME, name);
		try {
			ContentURI uri = cr.insert(Items.CONTENT_URI, values);
			Log.i(TAG, "Insert new item: " + uri);
			return Long.parseLong(uri.getPathSegment(1));
		} catch (Exception e) {
			Log.i(TAG, "Insert item failed", e);
			return -1;
		}
	}

	/**
	 * Adds a new shopping list and returns its id.
	 * If the list exists already, the existing id is returned.
	 * @param cr result from calling getContentResolver() 
	 *                          within your activity.
	 * @param name New name of the list.
	 * @return id of the new or existing list.
	 */
	public static long insertList(final ContentResolver cr, 
			final String name) {
		// TODO check whether list exists
		
		// Add item to list:
		ContentValues values = new ContentValues(1);
		values.put(Lists.NAME, name);
		try {
			ContentURI uri = cr.insert(Lists.CONTENT_URI, values);
			Log.i(TAG, "Insert new list: " + uri);
			return Long.parseLong(uri.getPathSegment(1));
		} catch (Exception e) {
			Log.i(TAG, "insert list failed", e);
			return -1;
		}
	}
	

	/**
	 * Adds a new item to a specific list and returns its id.
	 * If the item exists already, the existing id is returned.
	 * @param cr result from calling getContentResolver() 
	 *                          within your activity.
	 * @param itemId The id of the new item.
	 * @param itemType The type of the new item
	 * @param listId The id of the shopping list the item is added.
	 * @return id of the "contains" table entry.
	 */
	public static long insertContains(final ContentResolver cr, 
			final long itemId, final long listId) {
		// TODO check whether "contains" entry exists
		
		// Add item to list:
		ContentValues values = new ContentValues(2);
		values.put(Contains.ITEM_ID, itemId);
		values.put(Contains.LIST_ID, listId);
		try {
			ContentURI uri = cr.insert(Contains.CONTENT_URI, values);
			Log.i(TAG, "Insert new entry in 'contains': " + uri);
			return Long.parseLong(uri.getPathSegment(1));
		} catch (Exception e) {
			Log.i(TAG, "insert into table 'contains' failed", e);
			return -1;
		}
	}
	
}
