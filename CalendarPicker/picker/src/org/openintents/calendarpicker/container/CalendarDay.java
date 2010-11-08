package org.openintents.calendarpicker.container;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CalendarDay implements Comparable<CalendarDay> {
	public Date date;
	public List<SimpleEvent> day_events = new ArrayList<SimpleEvent>();
	
	public CalendarDay(Date date) {
		this.date = date;
	}

	@Override
	public int compareTo(CalendarDay another) {
		return this.date.compareTo(another.date);
	}
}
