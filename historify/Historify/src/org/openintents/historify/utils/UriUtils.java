package org.openintents.historify.utils;

import android.net.Uri;

public class UriUtils {

	public static Uri resourceToUri(int resId) {
		return Uri.parse("android.resource://org.openintents.historify/"+resId);
	}
}
