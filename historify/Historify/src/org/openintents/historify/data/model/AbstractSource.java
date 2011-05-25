package org.openintents.historify.data.model;

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

	protected boolean mIsInternal = false;

	public AbstractSource(long id, String name) {
		mId = id;
		mName = name;
		mState = SourceState.ENABLED;
	}

	public boolean isInternal() {
		return mIsInternal;
	}

	public boolean isEnabled() {
		return mState == SourceState.ENABLED;
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

	public static AbstractSource factoryMethod(boolean isInternal, long id,
			String name, String state) {

		AbstractSource retval = isInternal ? new InternalSource(id, name)
				: new ExternalSource(id, name);
		retval.setState(SourceState.parseString(state));

		return retval;
	}


}
