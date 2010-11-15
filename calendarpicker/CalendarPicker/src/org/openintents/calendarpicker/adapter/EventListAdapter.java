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

package org.openintents.calendarpicker.adapter;

import java.text.DateFormat;
import java.util.Date;

import org.openintents.calendarpicker.activity.DayEventsListActivity;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import org.openintents.calendarpicker.R;

public class EventListAdapter extends ResourceCursorAdapter {

	static final String TAG = "EventListAdapter"; 
	
    public EventListAdapter(Context context, int layout, Cursor cursor) {
    	super(context, layout, cursor);
    }


	public void bindView(View view, Context context, Cursor cursor) {

		TextView category_name = (TextView) view.findViewById(R.id.category_name);
		TextView assignment_timestamp = (TextView) view.findViewById(R.id.assignment_timestamp);
		
		int timestamp_column = cursor.getColumnIndex(DayEventsListActivity.KEY_EVENT_TIMESTAMP);
		int name_column = cursor.getColumnIndex(DayEventsListActivity.KEY_EVENT_TITLE);
		
		category_name.setText(cursor.getString(name_column));

	    long timestamp = cursor.getLong(timestamp_column);
	    Date earliest = new Date(timestamp);
	    String formatted_date = DateFormat.getDateTimeInstance().format(earliest);
		assignment_timestamp.setText(formatted_date);
	}
}