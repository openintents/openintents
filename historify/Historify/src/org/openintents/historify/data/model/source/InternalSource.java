package org.openintents.historify.data.model.source;


public class InternalSource extends AbstractSource {

	public InternalSource(long id, String name) {
		super(id, name);
		mIsInternal = true;
	}

}
