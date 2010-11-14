package org.openintents.calendarpicker.container;

import java.util.Date;

public class SimpleEvent implements Comparable<SimpleEvent> {

	public long id;
	public Date timestamp;
	public String title;

	public SimpleEvent(long id, long timestamp, String title) {
		this(id, new Date(timestamp), title);
	}
	
	public SimpleEvent(long id, Date timestamp, String title) {
		this.id = id;
		this.timestamp = timestamp;
		this.title = title;
	}
	
	public int compareTo(SimpleEvent another) {
		return timestamp.compareTo(another.timestamp);
	}
}