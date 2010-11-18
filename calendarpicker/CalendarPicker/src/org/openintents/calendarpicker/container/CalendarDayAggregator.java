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

public class CalendarDayAggregator {

	private float[] aggregate_quantities = new float[CalendarPickerConstants.CalendarEventPicker.IntentExtras.EXTRA_QUANTITY_COLUMN_NAMES.length];
	private int event_count;
	private Date date;

	public void reset(Date date) {
		this.date = date;
		this.event_count = 0;
		
		for (int i=0; i<this.aggregate_quantities.length; i++)
			this.aggregate_quantities[i] = 0;
	}

	public Date getDate() {
		return this.date;
	}

	public void incrementEventCount() {
		this.event_count++;
	}
	
	public void addAggregateQuantity(int index, float quantity) {
		this.aggregate_quantities[index] += quantity;
	}
	
	public float getAggregateQuantity(int index) {
		return this.aggregate_quantities[index];
	}
	
	public int getEventCount() {
		return this.event_count;
	}
}