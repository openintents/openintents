package org.openintents.historify.data.providers.internal;

import android.net.Uri;

public class Telephony {

	public static final String SOURCE_NAME = "Telephony";
	
	public static final String TELEPHONY_AUTHORITY = "org.openintents.historify.internal.telephony";
	public static final String EVENTS_PATH = "events";
	
	public static final Uri SOURCE_URI = Uri.parse("content://"+TELEPHONY_AUTHORITY+"/"+EVENTS_PATH);
}
