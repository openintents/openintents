package org.openintents.calendarpicker.container;

import java.util.Date;

public class SimpleEvent implements Comparable<SimpleEvent> {

	long id;
	public Date timestamp;

	public SimpleEvent(long id, long timestamp) {
		this.id = id;
		this.timestamp = new Date(timestamp);
		
//		Log.i(TAG, "Added Date: " + this.timestamp);
	}
	
	public SimpleEvent(long id, Date timestamp) {
		this.id = id;
		this.timestamp = timestamp;
	}
	
	public int compareTo(SimpleEvent another) {
		return timestamp.compareTo(another.timestamp);
	}
}