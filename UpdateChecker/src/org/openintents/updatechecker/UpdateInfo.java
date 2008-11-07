/*
 * Copyright (C) 2008  OpenIntents.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.openintents.updatechecker;

import org.openintents.updatechecker.activity.UpdateCheckerActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.BaseColumns;

public class UpdateInfo implements BaseColumns {

	public static final String LAST_CHECK = "last_check";
	public static final String PACKAGE_NAME = "package_name";
	public static final String UPDATE_URL = "update_url";

	public static final String DEFAULT_SORT_ORDER = LAST_CHECK;

	public static final String CONTENT_TYPE = "vnd.android.cursor.dir/org.openintents.updates";
	public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/org.openintents.updates";

	public static final String AUTHORITY = "org.openintents.updateinfo";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
			+ "/info");
	public static final String LAST_CHECK_VERSION_CODE = "last_check_version_code";
	public static final String LAST_CHECK_VERSION_NAME = "last_check_version_name";

}
