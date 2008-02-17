/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Definition for content provider related to tag.
 * 
 */
public abstract class Tag {

	public static final class Tags implements BaseColumns {
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri.parse("content://org.openintents.tags/tags");

		/**
		 * The default sort order for this table.
		 */
		public static final String DEFAULT_SORT_ORDER = "modified DESC";

		/**
		 * The id of the tag.
		 * <P>
		 * Type: STRING
		 * </P>
		 */
		public static final String TAG_ID = "tag_id";

		/**
		 * The id of the content.
		 * <P>
		 * Type: STRING
		 * </P>
		 */
		public static final String CONTENT_ID = "content_id";

		/**
		 * The timestamp for when the tag was created.
		 * <P>
		 * Type: INTEGER (long)
		 * </P>
		 */
		public static final String CREATED_DATE = "created";

		/**
		 * The timestamp for when the tag was last modified.
		 * <P>
		 * Type: INTEGER (long)
		 * </P>
		 */
		public static final String MODIFIED_DATE = "modified";

		/**
		 * The timestamp for when the tag was last modified.
		 * <P>
		 * Type: INTEGER (long)
		 * </P>
		 */
		public static final String ACCESS_DATE = "accessed";

		/**
		 * First URI of the relationship (usually the tag).
		 * 
		 */
		public static final String URI_1 = "uri_1";

		/**
		 * Second URI of the relationship (usually the content).
		 * 
		 */
		public static final String URI_2 = "uri_2";

		/**
		 * The Uri to be tagged that the query is about.
		 * 
		 */
		public static final String QUERY_URI = "uri";

		/**
		 * The tag that the query is about.
		 */
		public static final String QUERY_TAG = "tag";
	}

	public static final class Contents implements BaseColumns {
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri
				.parse("content://org.openintents.tags/contents");

		/**
		 * The default sort order for this table.
		 */
		public static final String DEFAULT_SORT_ORDER = "type DESC, uri";

		/**
		 * The uri of the content.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String URI = "uri";

		/**
		 * The type of the content, e.g TAG null means CONTENT.
		 * <P>
		 * Type: TEXT
		 * </P>
		 */
		public static final String TYPE = "type";

		/**
		 * The timestamp for when the note was created.
		 * <P>
		 * Type: INTEGER (long)
		 * </P>
		 */
		public static final String CREATED_DATE = "created";

	}

}
