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

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.activity.DayEventsListActivity;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

public class EventListAdapter extends ResourceCursorAdapter {

	static final String TAG = "EventListAdapter";
	
	public static class ExtraQuantityInfo {

		String column_name;
		public String format_string = "%.0f";
		
		public ExtraQuantityInfo(String column_name) {
			this.column_name = column_name;
		}
		
		public String getFormattedNumber(Cursor cursor) {
			
			Log.d(TAG, "All columns: " + TextUtils.join(",", cursor.getColumnNames()));
			Log.i(TAG, "Trying to get column: " + this.column_name);
			return String.format(this.format_string, cursor.getFloat(cursor.getColumnIndex(this.column_name)));
		}
	}
	
	DateFormat date_format;
	ExtraQuantityInfo[] extra_quantity_info;
    public EventListAdapter(Context context, int layout, Cursor cursor, DateFormat format, ExtraQuantityInfo[] extra_quantity_info) {
    	super(context, layout, cursor);
    	this.date_format = format;
    	this.extra_quantity_info = extra_quantity_info;
    }


    final static int QUANTITY_VIEW_ID_MAP[] = new int[] {
    	android.R.id.text1, android.R.id.text2
    };
    
	public void bindView(View view, Context context, Cursor cursor) {

		TextView category_name = (TextView) view.findViewById(R.id.category_name);
		TextView assignment_timestamp = (TextView) view.findViewById(R.id.assignment_timestamp);
		
		int timestamp_column = cursor.getColumnIndex(DayEventsListActivity.KEY_EVENT_TIMESTAMP);
		int name_column = cursor.getColumnIndex(DayEventsListActivity.KEY_EVENT_TITLE);
		
		category_name.setText(cursor.getString(name_column));

	    long timestamp = cursor.getLong(timestamp_column);
	    Date earliest = new Date(timestamp);
	    String formatted_date = this.date_format.format(earliest);
		assignment_timestamp.setText(formatted_date);
		
		for (int i=0; i<this.extra_quantity_info.length; i++) {
			if (this.extra_quantity_info[i] != null) {

				TextView tv = (TextView) view.findViewById(QUANTITY_VIEW_ID_MAP[i]);
				tv.setVisibility(View.VISIBLE);
				tv.setText(this.extra_quantity_info[i].getFormattedNumber(cursor));
			}
		}
	}
}