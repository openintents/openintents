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