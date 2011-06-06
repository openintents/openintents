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

	//ContactsActivity
	public static final String EXTRA_MODE_FAVORITES = "favorites";

	//TimeLineActiviy
	public static final String SHOW_TIMELINE = "org.openintents.historify.SHOW_TIMELINE";
	public static final String EXTRA_CONTACT_LOOKUP_KEY = "lookupKey";

	//EventIntents for internal providers
	public static final String VIEW_MESSAGING_EVENT = "org.openintents.historify.VIEW_MESSAGING_EVENT";

	//EventIntent extras
	public static final String EXTRA_EVENT_ID = Events._ID;
	public static final String EXTRA_EVENT_KEY = Events.EVENT_KEY;

}
