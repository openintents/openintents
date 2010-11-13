package org.openintents.calendarpicker.container;

import java.util.Date;

public class SimpleCalendarDay {

	private int event_count;
	private Date date;

	public void reset(Date date) {
		this.date = date;
		this.event_count = 0;
	}

	public Date getDate() {
		return this.date;
	}

	public void incrementEventCount() {
		this.event_count++;
	}
	
	public int getEventCount() {
		return this.event_count;
	}
}