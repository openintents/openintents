package org.openintents.calendarpicker;

import android.content.ContentResolver;
import android.provider.BaseColumns;

public class IntentConstants {

	/** a Long */
	public static String INTENT_EXTRA_CALENDAR_EVENT_ID = BaseColumns._ID;



	public static final class CalendarPicker {
		
		/** an ISO 8601 date string */
		public static String INTENT_EXTRA_DATETIME = "datetime";
		
		public static String CONTENT_TYPE_DATETIME = "text/datetime";
	}
	

	public static final String CATEGORY_CALENDAR = "com.googlecode.chartdroid.intent.category.CALENDAR";

	// Pie chart extras
	public static final String EXTRA_COLORS = "com.googlecode.chartdroid.intent.extra.COLORS";
	public static final String EXTRA_LABELS = "com.googlecode.chartdroid.intent.extra.LABELS";
	public static final String EXTRA_DATA = "com.googlecode.chartdroid.intent.extra.DATA";

	// Calendar extras
	public static final String EXTRA_EVENT_IDS = "com.googlecode.chartdroid.intent.extra.EVENT_IDS";
	public static final String EXTRA_EVENT_TIMESTAMPS = "com.googlecode.chartdroid.intent.extra.EVENT_TIMESTAMPS";



	public static final String CONTENT_TYPE_BASE_SINGLE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/";
	public static final String CONTENT_TYPE_BASE_MULTIPLE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/";


	public static final class CalendarEvent implements BaseColumns {

		private static final String VND_TYPE_DECLARATION = "vnd.org.openintents.event";

		// ==== CONTENT TYPES ====

		public static final String CONTENT_TYPE_ITEM_CALENDAR_EVENT = CONTENT_TYPE_BASE_SINGLE + VND_TYPE_DECLARATION;
		public static final String CONTENT_TYPE_CALENDAR_EVENT = CONTENT_TYPE_BASE_MULTIPLE + VND_TYPE_DECLARATION;
		

		// ==== COLUMNS ====

		public static final String COLUMN_EVENT_TITLE = "title";
		public static final String COLUMN_EVENT_QUANTITY = "quantity";
		public static final String COLUMN_EVENT_TIMESTAMP = "timestamp";
	}
}
