/* 
 * Copyright (C) 2011 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.historify.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;

public class UriUtils {

	public static final String DRAWABLE_TYPE = "drawable";

	// special scheme means that the applications icon 
	// should be used as a drawable
	public static final String APP_ICON_SCHEME = "app.icon";

	public static Uri drawableToUri(Context context, String drawableName) {

		return new Uri.Builder()
				.scheme(ContentResolver.SCHEME_ANDROID_RESOURCE).authority(
						context.getPackageName()).appendPath(DRAWABLE_TYPE)
				.appendPath(drawableName).build();
	}

	public static Uri appIconToUri(Context context, int uid) {

		String packageName = context.getPackageManager().getPackagesForUid(uid)[0];
		return Uri.parse(APP_ICON_SCHEME + "://" + packageName + "/");
	}

	public static Uri sourceAuthorityToUri(String authority) {

		return Uri.parse("content://" + authority + "/");
	}

}
