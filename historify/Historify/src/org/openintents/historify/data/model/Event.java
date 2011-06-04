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
 * Model class representing an Event in the timeline.
 * 
 * @author berke.andras
 */
public class Event {

	//id
	private long mId;
	
	//CONTACT_LOOKUP_KEY
	private String mContactKey;
	
	//event time
	private long mPublishedTime;
	
	//displayed message string
	private String mMessage;
	
	//originator of the event
	private Events.Originator mOriginator;
	
	public Event(long mId, String mContactKey, long mPublishedTime,
			String mMessage, Originator mOriginator) {
		this.mId = mId;
		this.mContactKey = mContactKey;
		this.mPublishedTime = mPublishedTime;
		this.mMessage = mMessage;
		this.mOriginator = mOriginator;
	}

	public long getId() {
		return mId;
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
