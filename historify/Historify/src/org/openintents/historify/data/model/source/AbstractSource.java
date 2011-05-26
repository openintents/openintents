package org.openintents.historify.data.model.source;

public abstract class AbstractSource {

	public enum SourceState {
		ENABLED, DISABLED, ERROR;

		public static SourceState parseString(String stateString) {
			for (SourceState s : values()) {
				if (s.toString().equals(stateString))
					return s;
			}
			return ERROR;
		}
	}

	private long mId;
	private String mName;
	private SourceState mState;
	private SourceFilter mSourceFilter;

	protected boolean mIsInternal = false;

	public AbstractSource(long id, String name) {
		mId = id;
		mName = name;
		mState = SourceState.ENABLED;
	}

	public boolean isInternal() {
		return mIsInternal;
	}

	public long getId() {
		return mId;
	}

	public String getName() {
		return mName;
	}

	public SourceState getState() {
		return mState;
	}

	public void setState(SourceState mState) {
		this.mState = mState;
	}

	public boolean isEnabled() {
		return (mSourceFilter==null && mState==SourceState.ENABLED) || (mSourceFilter!=null && mSourceFilter.getFilteredState()==SourceState.ENABLED);
	}

	public void setEnabled(boolean checked) {
		SourceState newState = checked ? SourceState.ENABLED : SourceState.DISABLED;
		if(mSourceFilter==null) setState(newState);
		else mSourceFilter.setFilteredState(newState);
	}
	
	public void setSourceFilter(SourceFilter mSourceFilter) {
		this.mSourceFilter = mSourceFilter;
	}
	
	public SourceFilter getSourceFilter() {
		return mSourceFilter;
	}
	
	public static AbstractSource factoryMethod(boolean isInternal, long id,
			String name, String state) {

		AbstractSource retval = isInternal ? new InternalSource(id, name)
				: new ExternalSource(id, name);
		retval.setState(SourceState.parseString(state));

		return retval;
	}


}
