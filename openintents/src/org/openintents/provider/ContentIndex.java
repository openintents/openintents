package org.openintents.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public class ContentIndex {
	public static final class Entry implements BaseColumns {
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri
				.parse("content://org.openintents.contentindices/entries");

		/**
		 * The default sort order for this table.
		 */
		public static final String DEFAULT_SORT_ORDER = "modified DESC";

		public static final String QUERY_CONTENT_ID = "contentId";
		public static final String QUERY_KEYWORD = "keyword";
		public static final String QUERY_DIRECTORY = "directory";

		public static final String ITEM_ID = "item_id";
		public static final String BODY = "body";

		public static final String BODY_SPLIT = "\t";

	}

}
