/* 
 * Copyright (C) 2011 OpenIntents.org
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

package org.openintents.historify.uri;

import org.openintents.historify.data.providers.Events;

/**
 * 
 * Helper class for Intent actions and extra fields.
 * 
 * @author berke.andras
 */
public final class Actions {

	// ContactsActivity
	// ----------------------------------------------------------------------------------------------
	public static final String EXTRA_MODE_FAVORITES = "favorites";


	// TimeLineActiviy
	// ----------------------------------------------------------------------------------------------
	public static final String ACTION_SHOW_TIMELINE = "org.openintents.historify.SHOW_TIMELINE";
	public static final String EXTRA_CONTACT_LOOKUP_KEY = "lookupKey";

	
	// EventIntents for internal providers
	// ----------------------------------------------------------------------------------------------
	public static final String ACTION_VIEW_MESSAGING_EVENT = "org.openintents.historify.VIEW_MESSAGING_EVENT";
	// EventIntent extras
	public static final String EXTRA_EVENT_ID = Events._ID;
	public static final String EXTRA_EVENT_KEY = Events.EVENT_KEY;

	
	// ConfigIntents for internal providers
	// ----------------------------------------------------------------------------------------------
	public static final String ACTION_CONFIG_QUICKPOSTS = "org.openintents.historify.CONFIG_QUICKPOSTS";
	
	
	// BridgeService
	// ----------------------------------------------------------------------------------------------

	public static final String ACTION_REGISTER_SOURCE = "org.openintents.historify.REGISTER_SOURCE";
	// mandatory parameters
	public static final String EXTRA_SOURCE_NAME = "name";
	public static final String EXTRA_SOURCE_AUTHORITY = "authority";
	public static final String EXTRA_SOURCE_UID = "uid";
	public static final String EXTRA_SOURCE_VERSION = "version";
	// optional parameters
	public static final String EXTRA_SOURCE_DESCRIPTION = "description";
	public static final String EXTRA_SOURCE_ICON_URI = "icon_uri";
	public static final String EXTRA_EVENT_INTENT = "event_intent";
	public static final String EXTRA_CONFIG_INTENT = "config_intent";

	public static final String BROADCAST_REQUEST_REGISTER_SOURCE = "org.openintents.historify.REQUEST_REGISTER_SOURCE";;
	public static final String EXTRA_PACKAGE_NAME = "package_name";
	public static final String EXTRA_ADDRESSED = "addressed";

	public static final String ACTION_QUICK_POST = "org.openintents.historify.QUICK_POST";
	// mandatory parameters
	//EXTRA_SOURCE_NAME = "name";
	//EXTRA_SOURCE_UID = "uid";
	//EXTRA_SOURCE_VERSION = "version";
	//also fields defined in .data.providers.Events

}
