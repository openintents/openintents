package org.openintents.historify.data.model;

import org.openintents.historify.data.providers.Events;
import org.openintents.historify.data.providers.Events.Originator;

public class Event {

	private long mId;
	private String mContactKey;
	private long mPublishedTime;
	private String mMessage;
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
