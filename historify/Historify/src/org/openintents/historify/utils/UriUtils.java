package org.openintents.historify.utils;

import android.net.Uri;

public class UriUtils {

	private static final String INTERNAL_DRAWABLE_SCHEME="internal.drawable";
	
	public static Uri drawableToUri(String drawableName) {
		return Uri.parse(INTERNAL_DRAWABLE_SCHEME+"://"+drawableName);
	}

	public static Uri sourceAuthorityToUri(String authority) {
		return Uri.parse("content://"+authority+"/");
	}
}
