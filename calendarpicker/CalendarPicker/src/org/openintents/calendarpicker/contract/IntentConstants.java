package org.openintents.calendarpicker.contract;

import android.content.ContentResolver;
import android.provider.BaseColumns;

public class IntentConstants {

	/** a Long */
	public static String INTENT_EXTRA_CALENDAR_EVENT_ID = BaseColumns._ID;

	
	public static String ANDROID_CALENDAR_AUTHORITY_1_0 = "calendar";
	public static String ANDROID_CALENDAR_AUTHORITY_2_0 = "com.android.calendar";
	
	public static final class CalendarDatePicker {
		
		/** an ISO 8601 date string */
		public static String INTENT_EXTRA_DATETIME = "datetime";
		
		/** A long */
		public static String INTENT_EXTRA_EPOCH = "epoch";
		
		public static String CONTENT_TYPE_DATETIME = "text/datetime";
		
		
		
		// Calendar event Intent extras
		public static final String EXTRA_EVENT_IDS = "com.googlecode.chartdroid.intent.extra.EVENT_IDS";
		public static final String EXTRA_EVENT_TIMESTAMPS = "com.googlecode.chartdroid.intent.extra.EVENT_TIMESTAMPS";
	}
	

	public static final String CONTENT_TYPE_BASE_SINGLE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/";
	public static final String CONTENT_TYPE_BASE_MULTIPLE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/";


	public static final class CalendarEventPicker implements BaseColumns {

//		private static final String VND_TYPE_DECLARATION = "vnd.org.openintents.event";
		private static final String VND_TYPE_DECLARATION = "event";

		// ==== CONTENT TYPES ====

		public static final String CONTENT_TYPE_ITEM_CALENDAR_EVENT = CONTENT_TYPE_BASE_SINGLE + VND_TYPE_DECLARATION;
		public static final String CONTENT_TYPE_CALENDAR_EVENT = CONTENT_TYPE_BASE_MULTIPLE + VND_TYPE_DECLARATION;
		

		// ==== COLUMNS ====
		
		public static final String COLUMN_EVENT_TITLE = "title";
		public static final String COLUMN_EVENT_QUANTITY = "quantity";
		public static final String COLUMN_EVENT_TIMESTAMP = "dtstart";
		public static final String COLUMN_EVENT_CALENDAR_ID = "calendar_id";
		
		

	}
}
