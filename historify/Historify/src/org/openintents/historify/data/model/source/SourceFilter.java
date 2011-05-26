package org.openintents.historify.data.model.source;

import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.source.AbstractSource.SourceState;

public class SourceFilter {

	private long mId;
	private Contact mContact;
	private AbstractSource mSource;
	private SourceState mFilteredState;
	
	public SourceFilter(long id, SourceState filteredState) {
		mId = id;
		mFilteredState = filteredState;
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

	public long getId() {
		return mId;
	}
}
