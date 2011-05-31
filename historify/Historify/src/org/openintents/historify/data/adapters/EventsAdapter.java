package org.openintents.historify.data.adapters;

import java.util.Date;

import org.openintents.historify.data.loaders.EventLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.Event;
import org.openintents.historify.data.providers.internal.Messaging;
import org.openintents.historify.data.providers.internal.Telephony;
import org.openintents.historify.utils.DateUtils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EventsAdapter extends BaseAdapter {

	private Activity mContext;
	
	private EventLoader mLoader;
	private Cursor mCursor;
	
	private Contact mContact;

	public EventsAdapter(Activity context, Contact contact) {

		mContext = context;
		mContact = contact;
		mLoader = new EventLoader();
		
		load();
	}	

	public void load() {
		
		//test load with messaging provider
        mCursor = mLoader.openCursor(mContext, Messaging.SOURCE_URI, mContact); 
        notifyDataSetChanged();
	}

	public int getCount() {
		return mCursor == null ? 0 : mCursor.getCount();
	}

	public Event getItem(int position) {
		return mCursor == null ? null : mLoader.loadFromCursor(mCursor, position);
	}
	
	public long getItemId(int position) {
		return -1;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {

		Event event = getItem(position);

		if (convertView == null) {
			convertView = ((LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(android.R.layout.two_line_list_item, null);
		}

		TextView tv = (TextView) convertView.findViewById(android.R.id.text1);
		tv.setText(DateUtils.formatDate(new Date(event.getPublishedTime()))+" ("+event.getOriginator()+")");
		
		tv = (TextView) convertView.findViewById(android.R.id.text2);
		tv.setText(event.getMessage());
		
		return convertView;
	}
}
