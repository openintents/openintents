package org.openintents.historify.data.loaders;

import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.Event;
import org.openintents.historify.data.providers.Events;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;

public class EventLoader {

	public Cursor openCursor(Activity context, Uri providerAuthority, Contact contact) {
		
		Builder eventsUri = providerAuthority.buildUpon().appendPath(Events.EVENTS_PATH);
		if(contact!=null) eventsUri.appendPath(contact.getLookupKey());
		
        return context.managedQuery(eventsUri.build(), null, null, null, null);
	}
	
	public Event loadFromCursor(Cursor cursor, int position) {
		
		cursor.moveToPosition(position);
		
		int column_id = cursor.getColumnIndex(Events._ID);
		int column_contact_key = cursor.getColumnIndex(Events.CONTACT_KEY);
		int column_published_time = cursor.getColumnIndex(Events.PUBLISHED_TIME);
		int column_message = cursor.getColumnIndex(Events.MESSAGE);
		int column_originator = cursor.getColumnIndex(Events.ORIGINATOR);
		
		return new Event(
				cursor.getLong(column_id),
				cursor.getString(column_contact_key),
				cursor.getLong(column_published_time),
				cursor.getString(column_message),
				Events.Originator.parseString(cursor.getString(column_originator)));
	}
	
}
