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

/**
 * 
 * Model class representing a Contact in the device's contact list.
 * 
 * @author berke.andras
 */
public class Contact {
	
	private String mKey; //CONTACT_LOOKUP_KEY
	
	private String mDisplayedName;
	
	private long mLastTimeContacted;
	
	private String mGivenName;
	
	/**
	 * Constructor.
	 * 
	 * @param key Lookup key of the contact.
	 * @param displayedName Displayed contact name.
	 * @param lastTimeContacted the time of last contact UTC. 
	 */
	public Contact(String key, String displayedName, long lastTimeContacted) {
		this.mKey = key;
		this.mDisplayedName = displayedName;
		this.mLastTimeContacted = lastTimeContacted;
	}

	public String getDisplayedName() {
		return mDisplayedName;
	}

	public String getGivenName() {
		return mGivenName;
	}
	
	public void setGivenName(String givenName) {
		this.mGivenName = givenName;
	}
	
	public String getLookupKey() {
		return mKey;
	}

	public long getLastTimeContacted() {
		return mLastTimeContacted;
	}	
}
