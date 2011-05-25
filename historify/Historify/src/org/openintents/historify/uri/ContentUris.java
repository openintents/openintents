package org.openintents.historify.uri;

import android.net.Uri;

public final class ContentUris {

	public static final String SOURCES_AUTHORITY = "org.openintents.historify.sources";
	public static final Uri Sources = Uri.parse("content://"+SOURCES_AUTHORITY+"/sources");
}
