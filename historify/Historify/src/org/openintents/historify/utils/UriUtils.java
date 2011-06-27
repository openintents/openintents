package org.openintents.historify.utils;

import android.content.Context;
import android.net.Uri;

public class UriUtils {

	public static final String INTERNAL_DRAWABLE_SCHEME="internal.drawable";
	public static final String APP_ICON_SCHEME="appicon";
	
	public static Uri drawableToUri(String drawableName) {
		return Uri.parse(INTERNAL_DRAWABLE_SCHEME+"://"+drawableName);
	}

	public static Uri sourceAuthorityToUri(String authority) {
		return Uri.parse("content://"+authority+"/");
	}
	
	public static Uri getAppIconUri(Context context, int uid) {
		
		String packageName=context.getPackageManager().getPackagesForUid(uid)[0];
		
		return Uri.parse(APP_ICON_SCHEME+"://"+packageName+"/");
	}

}
