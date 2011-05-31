package org.openintents.historify.data.providers.internal;

import org.openintents.historify.data.providers.Events;

import android.net.Uri;

public class Telephony {

	public static final String SOURCE_NAME = "Telephony";
	
	public static final String TELEPHONY_AUTHORITY = "org.openintents.historify.internal.telephony";
	
	public static final Uri SOURCE_URI = Uri.parse("content://"+TELEPHONY_AUTHORITY+"/");
}
