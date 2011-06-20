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

package org.openintents.historify.data.providers;

/**
 * Definitions for supported event provider columns.
 */
public final class Events {

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.historify.event";
	public static final String ITEM_CONTENT_TYPE ="vnd.android.cursor.item/vnd.historify.event";
	
	public static final String EVENTS_PATH = "events";
	public static final String EVENTS_FOR_CONTACTS_PATH = "contacts";
	public static final String EVENTS_BY_EVENT_KEYS_PATH = "event_keys";
	
	/**
	 * Long. Required, key field.
	 */
	public final static String _ID = "_id";
	
	/**
	 * String. Optional, for avoiding duplicates. Providers decide how to interpret
	 * this field.
	 */
	public final static String EVENT_KEY = "event_key";

	/**
	 * LookupKey of the contact associated with the event. Optional. 
	 */
	public final static String CONTACT_KEY = "contact_key";

	/**
	 * Long (UNIX Time). Required, to support default sorting order.
	 */
	public final static String PUBLISHED_TIME = "published_time";

	/**
	 * String. Required, short description of the event.
	 */
	public final static String MESSAGE = "message";
	
	/**
	 * Enum of {@link #Originator}. Optional, default value is {@link Originator#both} if {@link #CONTACT_KEY} has been set.
	 */
	public final static String ORIGINATOR = "originator";

	/**
	 * Enum used as values of the {@link #ORIGINATOR} field
	 */
	public enum Originator {
		user, contact, both;

		public static Originator parseString(String string) {
			for(Originator o : values()) {
				if(o.toString().equals(string)) return o;
			}
			return both;
		}
	}
}
