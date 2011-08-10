/*
 * Copyright (C) 2010 Sony Ericsson Mobile Communications AB.
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
 * limitations under the License
 *
 */

package com.sonyericsson.eventstream;

import android.net.Uri;

public interface PluginConstants {

    /** Plug-in service intent values */
    public static class ServiceIntentCmd {

        private static final String PACKAGE_NAME = "com.sonyericsson.eventstream";

        /** Indicate the service of a event refresh intent */
        public static final String ACTION_REFRESH_REQUEST = PACKAGE_NAME + ".action.REFRESH_EVENTS";

        /** Indicate the service of a plug-in registration intent */
        public static final String ACTION_REGISTER_PLUGIN = PACKAGE_NAME + ".action.REGISTER_PLUGIN";

        /** Indicate the service of a add new source intent */
        public static final String ACTION_ADD_SOURCE_FEED = PACKAGE_NAME + ".action.ADD_SOURCE_FEED";

        /** Indicate the service of a url of new source feed */
        public static final String EXTRA_SOURCE_FEED_URL = PACKAGE_NAME + ".extra.SOURCE_FEED_URL";

        /** Indicate the service of a add new source intent */
        public static final String ACTION_DELETE_SOURCE_FEED = PACKAGE_NAME + ".action.DELETE_SOURCE_FEED";

        /** Indicate the service of a url of new source feed */
        public static final String EXTRA_SOURCE_FEED_ID = PACKAGE_NAME + ".extra.SOURCE_FEED_ID";

        /** Indicate the service of a proxy set intent */
        public static final String ACTION_SET_PROXY = PACKAGE_NAME + ".action.SET_PROXY";

        /** Indicate the service of a proxy url */
        public static final String EXTRA_PROXY_URL = PACKAGE_NAME + "extra.PROXY_URL";

        /** Indicate the service of a proxy port */
        public static final String EXTRA_PROXY_PORT = PACKAGE_NAME + "extra.PROXY_PORT";
    }

    /** Event stream constants needed to access the engine */
    public static class EventStream {
        // Event Stream provider Uri:s
        public static final Uri EVENTSTREAM_FRIEND_PROVIDER_URI = Uri
        .parse("content://com.sonyericsson.eventstream/friends");

        public static final Uri EVENTSTREAM_EVENT_PROVIDER_URI = Uri
        .parse("content://com.sonyericsson.eventstream/events");

        public static final Uri EVENTSTREAM_SOURCE_PROVIDER_URI = Uri
        .parse("content://com.sonyericsson.eventstream/sources");

        public static final Uri EVENTSTREAM_PLUGIN_PROVIDER_URI = Uri
        .parse("content://com.sonyericsson.eventstream/plugins");

        // Intents sent by the EventStream
        public interface Intents {

            public static final String REGISTER_PLUGINS_REQUEST_INTENT = "com.sonyericsson.eventstream.REGISTER_PLUGINS";

            public static final String REFRESH_REQUEST_INTENT = "com.sonyericsson.eventstream.REFRESH_REQUEST";

            public static final String VIEW_EVENT_INTENT = "com.sonyericsson.eventstream.VIEW_EVENT_DETAIL";

            public static final String STATUS_UPDATE_INTENT = "com.sonyericsson.eventstream.SEND_STATUS_UPDATE";
        }

        // Extra parameters used in intents
        public interface IntentData {
            public static final String SOURCE_ID_EXTRA = "source_id";

            public static final String STATUS_UPDATE_MESSAGE_EXTRA = "new_status_message";

            public static final String FRIEND_KEY_EXTRA = "friend_key";

            public static final String EVENT_KEY_EXTRA = "event_key";
        }

        // Column definitions in the Event Stream provider

        public interface PluginColumns {
            public static final String API_VERSION = "api_version";

            public static final String CONFIGURATION_STATE = "config_state";

            public static final String CONFIGURATION_ACTIVITY = "config_activity";

            public static final String CONFIGURATION_TEXT = "config_text";

            public static final String NAME = "name";

            public static final String ICON_URI = "icon_uri";

            public static final String STATUS_SUPPORT = "status_support";
        }

        public interface SourceColumns {
            public static final String ID_COLUMN = "_id";

            public static final String NAME = "name";

            public static final String ICON_URI = "icon_uri";

            public static final String ENABLED = "enabled";

            public static final String CURRENT_STATUS = "current_status";
        }

        public interface EventColumns {
            public static final String ID_COLUMN = "_id";

            public static final String SOURCE_ID = "source_id";

            public static final String FRIEND_ID = "friend_id";

            public static final String MESSAGE = "message";

            public static final String IMAGE_URI = "image_uri";

            public static final String PUBLISHED_TIME = "published_time";

            public static final String STATUS_ICON_URI = "status_icon_uri";

            public static final String GEODATA = "geodata";

            public static final String TITLE = "title";

            public static final String PERSONAL = "personal";

            public static final String OUTGOING = "outgoing";

            public static final String EVENT_KEY = "event_key";
        }

        public interface FriendColumns {
            public static final String ID_COLUMN = "_id";

            public static final String SOURCE_ID = "source_id";

            public static final String DISPLAY_NAME = "display_name";

            public static final String PROFILE_IMAGE_URI = "profile_image_uri";

            public static final String FRIEND_KEY = "friend_key";
        }

        public interface ConfigState {
            public static final int CONFIGURED = 0;

            public static final int NOT_CONFIGURED = 1;

            public static final int CONFIGURATION_NOT_NEEDED = 2;
        }

        public interface StatusSupport {
            public static final int HAS_SUPPORT_FALSE = 0;

            public static final int HAS_SUPPORT_TRUE = 1;
        }
    }

}

