package org.openintents.historify.data.providers.internal;

import org.openintents.historify.R;
import org.openintents.historify.data.providers.Events;
import org.openintents.historify.uri.ContentUris;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.CursorJoiner;
import android.database.CursorJoiner.Result;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.util.Log;

public class TelephonyProvider extends ContentProvider{

	public static final String NAME = "TelephonyProvider";

	private static final UriMatcher sUriMatcher;
	private static final int EVENTS = 1;
	private static final int EVENT_ID = 2;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		
		sUriMatcher.addURI(ContentUris.SOURCES_AUTHORITY, Telephony.EVENTS_PATH+"/#",
				EVENT_ID);
		
		sUriMatcher.addURI(Telephony.TELEPHONY_AUTHORITY,
				Telephony.EVENTS_PATH+"/*", EVENTS);
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public String getType(Uri uri) {

		switch (sUriMatcher.match(uri)) {
		case EVENTS:
			return Events.CONTENT_TYPE; 
		case EVENT_ID:
			return Events.ITEM_CONTENT_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		String lookupKey = null;
		String phoneSelection = null;
		
		switch (sUriMatcher.match(uri)) {
		case EVENTS:
			//2nd path segment contains the lookup key (content:://authority/events/{CONTACT_LOOKUP_KEY})
			lookupKey = uri.getPathSegments().get(1);
			phoneSelection = Phone.LOOKUP_KEY + " = '"+lookupKey+"'";
			break;
			
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		
		ContentResolver resolver = getContext().getContentResolver();
		
		//querying phone numbers of the contact
		Cursor phoneCursor = resolver.query(Phone.CONTENT_URI, new String[] {
				Phone.NUMBER
		}, phoneSelection, null, null);
		
		StringBuilder phoneNumbers = new StringBuilder();
		while(phoneCursor.moveToNext()) {
			phoneNumbers.append("'");
			phoneNumbers.append(phoneCursor.getString(0));
			phoneNumbers.append("'");
			phoneNumbers.append(",");
		}
		if(phoneNumbers.length()>0)
			phoneNumbers.setLength(phoneNumbers.length()-1);
		
		//build where clause for call log query
		String where = CallLog.Calls.NUMBER + " IN ("+phoneNumbers.toString()+")" 
			+ " AND "+CallLog.Calls.TYPE +" != "+CallLog.Calls.MISSED_TYPE
			+ " AND "+CallLog.Calls.DURATION + " != 0";
		
		//format call duration string
		String eventMessage = getContext().getString(R.string.telephony_event_message);
		eventMessage = String.format("'"+eventMessage+"'", "' || "
				+"strftime('%M:%S', time("+CallLog.Calls.DURATION+ ", 'unixepoch'))"
				+" || '");
		
		//execute query
		//column names are mapped as defined in .data.providers.Events
		return getContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, 
				new String[] {
					CallLog.Calls._ID + " AS "+Events._ID,
					"NULL AS "+Events.EVENT_KEY,
					eventMessage+ " AS "+Events.MESSAGE,
					CallLog.Calls.DATE+" AS "+Events.PUBLISHED_TIME,
					"'"+lookupKey+"' AS "+Events.CONTACT_KEY,
					"REPLACE("+
						"REPLACE("+ CallLog.Calls.TYPE+","
									+CallLog.Calls.INCOMING_TYPE+",'"+Events.Originator.contact+"'),"
									+CallLog.Calls.OUTGOING_TYPE+",'"+Events.Originator.user+"') AS "+Events.ORIGINATOR
				}, where, null, CallLog.Calls.DATE + " DESC");
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// not supported
		return null;
	}



	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// not supported
		return 0;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// not supported
		return 0;
	}

}
