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

/**
 * 
 * Model class representing a SourceFilter that wraps an actual source and
 * override its default state with the filtered state.
 * 
 * @author berke.andras
 */
public class SourceFilter extends EventSource {

	private long mFilterId;
	private SourceState mFilteredState;

	/**
	 * Constructor
	 * 
	 * @param filterId
	 *            The id of the filter in the db.
	 * @param source
	 *            The source which should be filtered.
	 * @param filteredState
	 *            The initial filtered state of this filter
	 */
	public SourceFilter(long filterId, EventSource source,
			SourceState filteredState) {
		super(source);
		setInternal(source.isInternal());
		mFilteredState = filteredState;
		mFilterId = filterId;
	}

	@Override
	public boolean isEnabled() {
		return mFilteredState == null ? super.isEnabled()
				: mFilteredState == SourceState.ENABLED;
	}

	@Override
	public void setEnabled(boolean checked) {
		mFilteredState = checked ? SourceState.ENABLED : SourceState.DISABLED;
	}

	public long getFilterId() {
		return mFilterId;
	}

	public SourceState getFilteredState() {
		return mFilteredState;
	}
}
