/*
 * Copyright (C) 2010 Karl Ostmo
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

package org.openintents.calendarpicker.contract;

import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.provider.BaseColumns;

public class CalendarPickerConstants {

	public static String ANDROID_CALENDAR_AUTHORITY_1_0 = "calendar";
	public static String ANDROID_CALENDAR_AUTHORITY_2_0 = "com.android.calendar";
	
	public static String ANDROID_CALENDAR_PROVIDER_PATH_CALENDARS = "calendars";
	public static String ANDROID_CALENDAR_PROVIDER_PATH_EVENTS = "events";
	
	
    
    
    public static class DownloadInfo {
    	
    	public final static String PACKAGE_NAME_CALENDAR_PICKER = "org.openintents.calendarpicker";
    	public final static String CALENDAR_PICKER_WEBSITE = "http://www.openintents.org/en/calendarpicker";
    	
    	public final static String APK_DOWNLOAD_URL_PREFIX = "http://openintents.googlecode.com/files/";
    	public final static String APK_APP_NAME = "CalendarPicker";
    	public final static String APK_VERSION_NAME = "1.0.0";
    	public final static Uri APK_DOWNLOAD_URI = Uri.parse(APK_DOWNLOAD_URL_PREFIX + APK_APP_NAME + "-" + APK_VERSION_NAME + ".apk");
    	
    	
        // ========================================================================
    	public static final String MARKET_PACKAGE_DETAILS_PREFIX = "market://details?id=";
    	public static final String MARKET_PACKAGE_DETAILS_STRING = MARKET_PACKAGE_DETAILS_PREFIX + PACKAGE_NAME_CALENDAR_PICKER;

        // ========================================================================
    	public static Intent getMarketDownloadIntent(String package_name) {
    		Uri market_uri = Uri.parse(MARKET_PACKAGE_DETAILS_PREFIX + package_name);
    		return new Intent(Intent.ACTION_VIEW, market_uri);
    	}
    	
    	// ================================================
    	public static boolean isIntentAvailable(Context context, Intent intent) {
    		final PackageManager packageManager = context.getPackageManager();
    		List<ResolveInfo> list = packageManager.queryIntentActivities(intent,
    				PackageManager.MATCH_DEFAULT_ONLY);
    		return list.size() > 0;
    	}
    }
	

    // ========================================================================
	public static final class CalendarDatePicker {

		public static String CONTENT_TYPE_DATETIME = "text/datetime";
		
		/** Date picker Intent extras */
		public static final class IntentExtras {
			
			/** an ISO 8601 date string */
			public static String INTENT_EXTRA_DATETIME = "datetime";
			
			/** A long */
			public static String INTENT_EXTRA_EPOCH = "epoch";
		}
	}
	

	public static final String CONTENT_TYPE_BASE_SINGLE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/";
	public static final String CONTENT_TYPE_BASE_MULTIPLE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/";


	public static final class CalendarEventPicker {

		private static final String VND_TYPE_DECLARATION = "event";

		// ==== CONTENT TYPES ====
		public static final String CONTENT_TYPE_ITEM_CALENDAR_EVENT = CONTENT_TYPE_BASE_SINGLE + VND_TYPE_DECLARATION;
		public static final String CONTENT_TYPE_CALENDAR_EVENT = CONTENT_TYPE_BASE_MULTIPLE + VND_TYPE_DECLARATION;


		/** Calendar event Intent extras */
		public static final class IntentExtras {
			public static final String EXTRA_EVENT_IDS = "org.openintents.calendarpicker.intent.extra.EVENT_IDS";
			public static final String EXTRA_EVENT_TIMESTAMPS = "org.openintents.calendarpicker.intent.extra.EVENT_TIMESTAMPS";
			public static final String EXTRA_EVENT_TITLES = "org.openintents.calendarpicker.intent.extra.EVENT_TITLES";

			public static final String EXTRA_CALENDAR_ID = "calendar_id";
			
			/** Boolean for color mapping the calendar background color to aggregate day quantity */
			public static final String EXTRA_VISUALIZE_QUANTITIES = "org.openintents.calendarpicker.intent.extra.VISUALIZE_QUANTITIES";
			
			/** Boolean for whether to display the day's event count. Default: true */
			public static final String EXTRA_SHOW_EVENT_COUNT = "org.openintents.calendarpicker.intent.extra.SHOW_EVENT_COUNT";
			
			
			public static final String[] EXTRA_QUANTITY_COLUMN_NAMES = new String[] {
				"org.openintents.calendarpicker.intent.extra.QUANTITY0_COLUMN_NAME",
				"org.openintents.calendarpicker.intent.extra.QUANTITY1_COLUMN_NAME"
			};
			
			public static final String[] EXTRA_QUANTITY_FORMATS = new String[] {
				"org.openintents.calendarpicker.intent.extra.QUANTITY0_NUMBER_FORMAT",
				"org.openintents.calendarpicker.intent.extra.QUANTITY1_NUMBER_FORMAT"
			};
			
			/** Integer for selecting the quantity for determining the background color values
			 * If this value is omitted, the day's event count will be used for the color value. */
			public static final String EXTRA_BACKGROUND_COLORMAP_QUANTITY_INDEX = "org.openintents.calendarpicker.intent.extra.BACKGROUND_COLORMAP_QUANTITY_INDEX";
			
			/** An array of color integers */
			public static final String EXTRA_BACKGROUND_COLORMAP_COLORS = "org.openintents.calendarpicker.intent.extra.BACKGROUND_COLORMAP_COLORS";
		}
		
		/** Columns to supply when implementing a ContentProvider for events */
		public static final class ContentProviderColumns implements BaseColumns {
			public static final String TITLE = "title";
			public static final String TIMESTAMP = "dtstart";
			public static final String CALENDAR_ID = "calendar_id";
		}
	}
}
