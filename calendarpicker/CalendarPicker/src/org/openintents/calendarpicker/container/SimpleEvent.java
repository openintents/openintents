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

import java.util.Date;

import org.openintents.calendarpicker.contract.CalendarPickerConstants;

public class SimpleEvent implements Comparable<SimpleEvent> {

	public long id;
	public float[] quantities = new float[CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES.length];
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
	
	public void setQuantity0(float quantity) {
		this.quantities[0] = quantity;
	}
	
	public void setQuantity1(float quantity) {
		this.quantities[1] = quantity;
	}
	
	public int compareTo(SimpleEvent another) {
		return timestamp.compareTo(another.timestamp);
	}
}