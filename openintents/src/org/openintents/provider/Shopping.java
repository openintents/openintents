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

import android.net.ContentURI;
import android.provider.BaseColumns;

/**
 * Definition for content provider related to shopping.
 *
 */
public abstract class Shopping {
	
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
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
       
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

}
