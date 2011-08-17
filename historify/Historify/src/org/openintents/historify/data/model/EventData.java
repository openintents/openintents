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

package org.openintents.historify.data.model;

import org.openintents.historify.data.providers.Events;
import org.openintents.historify.data.providers.Events.Originator;

/**
 * 
 * Model class describing the details of an Event in the timeline.
 * 
 * @author berke.andras
 */
public class EventData {

	//eventkey
	private String mEventKey;
	
	//CONTACT_LOOKUP_KEY
	private String mContactKey;
	
	//event time
	private long mPublishedTime;
	
	//displayed message string
	private String mMessage;
	
	//originator of the event
	private Events.Originator mOriginator;
	
	/**
	 * Constructor.
	 * 
	 * @param eventKey Event key field.
	 * @param contactKey Lookup key of a contact.
	 * @param publishedTime Event time.
	 * @param message Brief message of the event.
	 * @param originator Originator of the event.
	 */
	public EventData(String eventKey, String contactKey, long publishedTime,
			String message, Originator originator) {
		
		this.mEventKey = eventKey;
		this.mContactKey = contactKey;
		this.mPublishedTime = publishedTime;
		this.mMessage = message;
		this.mOriginator = originator;
	}

	public String getEventKey() {
		return mEventKey;
	}
	
	public String getContactKey() {
		return mContactKey;
	}

	public long getPublishedTime() {
		return mPublishedTime;
	}

	public String getMessage() {
		return mMessage;
	}

	public Events.Originator getOriginator() {
		return mOriginator;
	}
	
}
