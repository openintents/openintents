/*
 * Copyright 2010 Sony Ericsson Mobile Communications AB
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.sonyericsson.eventstream;

import android.net.Uri;

public interface EventStreamConstants {
    public interface ProviderUris {
        Uri FRIEND_PROVIDER_URI = Uri.parse("content://com.sonyericsson.eventstream/friends");
        Uri EVENT_PROVIDER_URI = Uri.parse("content://com.sonyericsson.eventstream/events");
        Uri SOURCE_PROVIDER_URI = Uri.parse("content://com.sonyericsson.eventstream/sources");
        Uri PLUGIN_PROVIDER_URI = Uri.parse("content://com.sonyericsson.eventstream/plugins");
    };
    public interface Intents {
        String REGISTER_PLUGINS_REQUEST_INTENT = "com.sonyericsson.eventstream.REGISTER_PLUGINS";
        String REFRESH_REQUEST_INTENT = "com.sonyericsson.eventstream.REFRESH_REQUEST";
        String VIEW_EVENT_INTENT = "com.sonyericsson.eventstream.VIEW_EVENT_DETAIL";
        String STATUS_UPDATE_INTENT = "com.sonyericsson.eventstream.SEND_STATUS_UPDATE";
        String EXTRA_STATUS_UPDATE_MESSAGE = "new_status_message";
    }

    public interface PluginColumns {
        String API_VERSION = "api_version";
        String CONFIGURATION_ACTIVITY = "config_activity";
        String CONFIGURATION_STATE = "config_state";
        String CONFIGURATION_TEXT = "config_text";
        String ICON_URI = "icon_uri";
        String NAME = "name";
        String PLUGIN_KEY = "plugin_key";
        String STATUS_SUPPORT = "status_support";
        String STATUS_TEXT_MAX_LENGTH  = "status_text_max_length";
    }

    public interface SourceColumns {
        String ID = "_id";
        String CURRENT_STATUS = "current_status";
        String ENABLED = "enabled";
        String ICON_URI = "icon_uri";
        String NAME = "name";
        String STATUS_TIMESTAMP = "status_timestamp"; 
    }

    public interface FriendColumns {
    	String SOURCE_ID = "source_id";
        String PLUGIN_ID = "plugin_id";
        String DISPLAY_NAME = "display_name";
        String PROFILE_IMAGE_URI = "profile_image_uri";
        String CONTACTS_REFERENCE = "contacts_reference";
        String FRIEND_KEY = "friend_key";
    }
    
    public interface EventColumns {
        String EVENT_KEY = "event_key";
        String FRIEND_KEY = "friend_key";
        String GEODATA = "geodata";
        String IMAGE_URI = "image_uri";
        String MESSAGE = "message";
        String OUTGOING = "outgoing";
        String PERSONAL = "personal";
        String PUBLISHED_TIME = "published_time";
        String SOURCE_ID = "source_id";
        String STATUS_ICON_URI = "status_icon_uri";
        String TITLE = "title";
    }

//    public interface ConfigState {
//        int CONFIGURED = 0;
//        int NOT_CONFIGURED = 1;
//        int CONFIGURATION_NOT_NEEDED = 2;
//    }
//
//    public interface StatusSupport {
//        int HAS_SUPPORT_FALSE = 0;
//        int HAS_SUPPORT_TRUE = 1;
//    }
//
//    public interface SourceState {
//        int NOT_ENABLED = 0;
//        int ENABLED = 1;
//    }
}
