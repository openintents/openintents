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

package org.openintens.samples.lendme.data;

public class Item {

	public enum Owner {
		Me, Contact;

		public static Owner parseString(String string) {
			for(Owner o : values()) {
				if(o.toString().equals(string))
					return o;
			}
			return null;
		}
	}
	
	private long id;
	private String contactKey;
	private long lendingStart;
	private String name;
	private String description;
	private Owner owner;
	
	public Item(long id, String contactKey, Owner owner, long lendingStart, String name,
			String description) {
		this.id = id;
		this.contactKey = contactKey;
		this.owner = owner;
		this.lendingStart = lendingStart;
		this.name = name;
		this.description = description;
	}

	public long getId() {
		return id;
	}
	
	public String getContactKey() {
		return contactKey;
	}

	public Owner getOwner() {
		return owner;
	}
	
	public long getLendingStart() {
		return lendingStart;
	}


	public String getName() {
		return name;
	}


	public String getDescription() {
		return description;
	}
	
}
