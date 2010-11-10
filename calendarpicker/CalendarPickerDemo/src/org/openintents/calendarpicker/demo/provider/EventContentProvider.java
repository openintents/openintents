package org.openintents.calendarpicker.demo.provider;

import java.util.List;

import org.openintents.calendarpicker.contract.IntentConstants;
import org.openintents.calendarpicker.demo.Demo;
import org.openintents.calendarpicker.demo.Demo.EventWrapper;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class EventContentProvider extends ContentProvider {
	

	static final String TAG = "EventContentProvider";
	
	// This must be the same as what as specified as the Content Provider authority
	// in the manifest file.
	public static final String AUTHORITY = "org.openintents.calendarpicker.demo.provider.events";
	
	
	static Uri BASE_URI = new Uri.Builder().scheme(ContentResolver.SCHEME_CONTENT).authority(AUTHORITY).path("events").build();


	// the appended ID is actually not used in this demo.
   public static Uri constructUri(long data_id) {
       return ContentUris.withAppendedId(BASE_URI, data_id);
   }
   
   @Override
   public boolean onCreate() {
       return true;
   }

   @Override
   public String getType(Uri uri) {
	   Log.d(TAG, "Providing type: " + IntentConstants.CalendarEventPicker.CONTENT_TYPE_CALENDAR_EVENT);
	   return IntentConstants.CalendarEventPicker.CONTENT_TYPE_CALENDAR_EVENT;
   }

   @Override
   public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

		MatrixCursor c = new MatrixCursor(new String[] {
				BaseColumns._ID,
				IntentConstants.CalendarEventPicker.COLUMN_EVENT_TIMESTAMP,
				IntentConstants.CalendarEventPicker.COLUMN_EVENT_TITLE});

		List<EventWrapper> generated_events = Demo.generateRandomEvents(5);
//		Log.i(TAG, "Generated " + generated_events.size() + " events.");
		
		int i=0;
		for (EventWrapper event : generated_events) {
			c.newRow()
				.add(event.id)
				.add( event.timestamp/1000 )
				.add( "Event " + i );
			
			i++;
		}

//		Log.i(TAG, "Generated cursor with " + c.getCount() + " rows.");
		return c;
   }


   @Override
   public int delete(Uri uri, String s, String[] as) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }

   @Override
   public Uri insert(Uri uri, ContentValues contentvalues) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }
   
   @Override
   public int update(Uri uri, ContentValues contentvalues, String s, String[] as) {
       throw new UnsupportedOperationException("Not supported by this provider");
   }
}
