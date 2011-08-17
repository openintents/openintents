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

import org.openintents.historify.data.model.source.EventSource;
import org.openintents.historify.data.providers.Events;
import org.openintents.historify.data.providers.Events.Originator;

import android.net.Uri;

/**
 * 
 * Model class representing an Event in the timeline.
 * 
 * @author berke.andras
 */
public class Event {

	//id
	private long mId;
	
	//event data
	private EventData mEventData;
	
	//source of the event
	private EventSource mSource;

	//custom icon associated with this event
	private Uri mIconUri;
	
	/**
	 * Constructor.
	 * 
	 * @param id Event id field.
	 * @param eventKey Event key field.
	 * @param contactKey Lookup key of a contact.
	 * @param publishedTime Event time.
	 * @param message Brief message of the event.
	 * @param originator Originator of the event.
	 * @param iconUri Uri of an image associated with the event.
	 */
	public Event(long id, String eventKey, String contactKey, long publishedTime,
			String message, Originator originator, Uri iconUri) {
	
		mId = id;
		mIconUri = iconUri;
		mEventData = new EventData(eventKey, contactKey, publishedTime, message, originator);
	}

	public long getId() {
		return mId;
	}

	public String getEventKey() {
		return mEventData.getEventKey();
	}
	
	public String getContactKey() {
		return mEventData.getContactKey();
	}

	public long getPublishedTime() {
		return mEventData.getPublishedTime();
	}

	public String getMessage() {
		return mEventData.getMessage();
	}

	public Events.Originator getOriginator() {
		return mEventData.getOriginator();
	}
		
	public Uri getCustomIcon() {
		return mIconUri;
	}

	public void setCustomIcon(Uri mIconUri) {
		this.mIconUri = mIconUri;
	}
	
	public EventSource getSource() {
		return mSource;
	}

	public void setSource(EventSource mSource) {
		this.mSource = mSource;
	}

}
