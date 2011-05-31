package org.openintents.historify.data.providers.internal;

import org.openintents.historify.data.providers.Events;

import android.net.Uri;
import android.provider.CallLog;

public class Messaging {

	public static final String SOURCE_NAME = "Messaging";
	
	public static final String MESSAGING_AUTHORITY = "org.openintents.historify.internal.messaging";
	
	public static final Uri SOURCE_URI = Uri.parse("content://"+MESSAGING_AUTHORITY+"/");
	
	//undocumented messaging provider API
	//handle with care
	public static class Messages {
		public static final Uri CONTENT_URI = Uri.parse("content://sms/");
		
		public static final String _ID = "_id";
		public static final String ADDRESS = "address";
		public static final String DATE = "date";
		public static final String TYPE = "type";
		public static final String BODY = "body";

		public static final int INCOMING_TYPE = CallLog.Calls.INCOMING_TYPE;
		public static final int OUTGOING_TYPE = CallLog.Calls.OUTGOING_TYPE;
	}
}
