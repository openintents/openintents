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
 * Helper class for Intent actions and extra fields used by Historify and its
 * client applications.
 * 
 * @author berke.andras
 */
public final class Actions {

	// ----------------------------------------------------------------------------------------------
	// Common
	// ----------------------------------------------------------------------------------------------
	// ----------------------------------------------------------------------------------------------

	public static final String EXTRA_CONTACT_LOOKUP_KEY = "lookupKey";
	public static final String EXTRA_EVENT_ID = Events._ID;
	public static final String EXTRA_EVENT_KEY = Events.EVENT_KEY;

	
	// ----------------------------------------------------------------------------------------------
	// BridgeService
	// ----------------------------------------------------------------------------------------------
	// ----------------------------------------------------------------------------------------------

	// REQUEST_REGISTER_SOURCE
	public static final String BROADCAST_REQUEST_REGISTER_SOURCE = "org.openintents.historify.REQUEST_REGISTER_SOURCE";;
	public static final String EXTRA_PACKAGE_NAME = "package_name";
	public static final String EXTRA_ADDRESSED = "addressed";

	// REGISTER_SOURCE
	public static final String ACTION_REGISTER_SOURCE = "org.openintents.historify.REGISTER_SOURCE";
	public static final String EXTRA_SOURCE_AUTHORITY = "authority";

	// QUICK_POST
	public static final String ACTION_QUICK_POST = "org.openintents.historify.QUICK_POST";

	// REGISTER_SOURCE and QUICK_POST common fields
	// mandatory parameters
	public static final String EXTRA_SOURCE_NAME = "name";
	public static final String EXTRA_SOURCE_UID = "uid";
	public static final String EXTRA_SOURCE_VERSION = "version";
	// optional parameters
	public static final String EXTRA_SOURCE_DESCRIPTION = "description";
	public static final String EXTRA_SOURCE_ICON_URI = "icon_uri";
	public static final String EXTRA_SOURCE_ICON_LOADING_STRATEGY = "icon_loading_strategy";
	public static final String EXTRA_EVENT_INTENT = "event_intent";
	public static final String EXTRA_CONFIG_INTENT = "config_intent";
	public static final String EXTRA_INTERACT_INTENT = "interact_intent";
	public static final String EXTRA_INTERACT_ACTION_TITLE = "interact_action_title";

	
	// ----------------------------------------------------------------------------------------------
	// INTERNAL
	// ----------------------------------------------------------------------------------------------
	// ----------------------------------------------------------------------------------------------

	// ContactsActivity
	public static final String EXTRA_MODE_FAVORITES = "favorites";

	// TimeLineActiviy
	public static final String ACTION_SHOW_TIMELINE = "org.openintents.historify.SHOW_TIMELINE";

	// EventIntents for internal providers
	public static final String ACTION_VIEW_MESSAGING_EVENT = "org.openintents.historify.VIEW_MESSAGING_EVENT";
	public static final String ACTION_VIEW_CALLOG_EVENT = "org.openintents.historify.VIEW_CALLOG_EVENT";
	public static final String ACTION_VIEW_QUICKPOST_EVENT = "org.openintents.historify.VIEW_QUICKPOST_EVENT";

	// InteractIntents for internal providers
	public static final String ACTION_INTERACT_FACTORYTEST = "org.openintents.historify.INTERACT_FACTORYTEST";

	// ConfigIntents for internal providers
	public static final String ACTION_CONFIG_QUICKPOSTS = "org.openintents.historify.CONFIG_QUICKPOSTS";
	public static final String ACTION_CONFIG_FACTORYTEST = "org.openintents.historify.CONFIG_FACTORYTEST";

}
