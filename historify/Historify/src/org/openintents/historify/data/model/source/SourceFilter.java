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

package org.openintents.historify.data.model.source;

import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.source.AbstractSource.SourceState;

/**
 * 
 * Model class representing a filter defined for a particular {@link Contact} - {@link AbstractSource} pair.
 * 
 * @author berke.andras
 */
public class SourceFilter {

	//id
	private long mId;
	
	//associated contact
	private Contact mContact;
	
	//associated source
	private AbstractSource mSource;
	
	//the state of this filter (enabled / disabled)
	private SourceState mFilteredState;
	
	public SourceFilter(long id, SourceState filteredState) {
		mId = id;
		mFilteredState = filteredState;
	}

	public long getId() {
		return mId;
	}
	
	public Contact getContact() {
		return mContact;
	}

	public AbstractSource getSource() {
		return mSource;
	}

	public SourceState getFilteredState() {
		return mFilteredState;
	}

	public void setContact(Contact mContact) {
		this.mContact = mContact;
	}
	
	public void setSource(AbstractSource mSource) {
		this.mSource = mSource;
	}
	
	public void setFilteredState(SourceState filteredState) {
		this.mFilteredState = filteredState;
	}

}
