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

package org.openintents.countdown.db;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Convenience definitions for CountdownProvider
 */
public final class Countdown {
    public static final String AUTHORITY = "org.openintents.countdown";

    // This class cannot be instantiated
    private Countdown() {}
    
    /**
     * Notes table
     */
    public static final class Durations implements BaseColumns {
        // This class cannot be instantiated
        private Durations() {}

        /**
         * The path after the authority.
         */
        public static final String PATH = "durations";
        
        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + PATH);

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of durations.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.openintents.countdown.duration";

        /**
         * The MIME type of a {@link #CONTENT_URI} sub-directory of a single duration.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.openintents.countdown.duration";

        /**
         * The title of the note
         * <P>Type: TEXT</P>
         */
        public static final String TITLE = "title";

        /**
         * Duration in seconds for this countdown.
         * 
         * Alternatively USER_DEADLINE_DATE can be set below
         * which has precedence.
         * 
         * <P>Type: INTEGER</P>
         */
        public static final String DURATION = "duration";

        /**
         * The deadline for this timer.
         * (If this is 0, timer is inactive).
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String DEADLINE_DATE = "deadline";
        
        /**
         * Ring (no = 0, 1 = yes).
         * <P>Type: INTEGER</P>
         */
        public static final String RING = "ring";

        /**
         * Ringtone URI
         * <P>Type: TEXT</P>
         */
        public static final String RINGTONE = "ringtone";

        /**
         * Vibrate (no = 0, 1 = yes).
         * <P>Type: INTEGER</P>
         */
        public static final String VIBRATE = "vibrate";
        
        /**
         * The timestamp for when the countdown was created
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the countdown was last modified
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         */
        public static final String MODIFIED_DATE = "modified";

        /**
         * The deadline set to a specific date.
         * This is an alternative way to set a countdown
         * instead of specifying 'DURATION'.
         * If this is > 0, it overrides the 'DURATION' value.
         * 
         * <P>Type: INTEGER (long from System.curentTimeMillis())</P>
         * @version Database version 2
         */
        public static final String USER_DEADLINE_DATE = "userdeadline";

        /**
         * Perform automation action at end of timer.
         * (no = 0, 1 = yes).
         * <P>Type: INTEGER</P>
         * @version Database version 2
         */
        public static final String AUTOMATE = "automate";
        
        /**
         * Intent to launch the settings for an automation.
         * <P>Type: TEXT</P>
         * @version Database version 2
         */
        public static final String AUTOMATE_INTENT = "automateintent";

        /**
         * Text to be displayed to represent the automation.
         * <P>Type: TEXT</P>
         * @version Database version 2
         */
        public static final String AUTOMATE_TEXT = "automatetext";

        /**
         * Status bar notification (no = 0, 1 = yes).
         * <P>Type: INTEGER</P>
         */
        public static final String NOTIFICATION = "notification";

        /**
         * Light notification (LED) (no = 0, 1 = yes).
         * <P>Type: INTEGER</P>
         */
        public static final String LIGHT = "light";
        
        /**
         * The default sort order for this table
         */
        //public static final String DEFAULT_SORT_ORDER = DEADLINE_DATE + " DESC";
        public static final String DEFAULT_SORT_ORDER = MODIFIED_DATE + " DESC";
        

        public static final String[] PROJECTION = new String[] {
                Durations._ID,
                Durations.TITLE,
                Durations.DURATION,
                Durations.DEADLINE_DATE,
                Durations.RING,
                Durations.RINGTONE,
                Durations.VIBRATE,
                Durations.CREATED_DATE,
                Durations.MODIFIED_DATE,
                Durations.USER_DEADLINE_DATE,
                Durations.AUTOMATE,
                Durations.AUTOMATE_INTENT,
                Durations.AUTOMATE_TEXT,
                Durations.NOTIFICATION,
                Durations.LIGHT
        };
    }
}
