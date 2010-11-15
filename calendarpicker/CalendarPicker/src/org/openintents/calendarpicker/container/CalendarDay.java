/*
 * Copyright (C) 2010 Karl Ostmo
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
