package org.openintents.updatechecker;

import android.net.Uri;
import android.provider.BaseColumns;

public class UpdateInfo implements BaseColumns {


	public static final String LAST_CHECK = "last_check";
	public static final String PACKAGE_NAME = "package_name";
	
	public static final String DEFAULT_SORT_ORDER = LAST_CHECK;

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.openintents.updates";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.openintents.updates";

	public static final String AUTHORITY = "org.openintents.updateinfo";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/info");

}
