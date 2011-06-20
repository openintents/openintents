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

package org.openintents.historify.uri;

import org.openintents.historify.data.providers.internal.QuickPosts;

import android.net.Uri;

/**
 * 
 * Helper class for Content URIs used by the application internally.
 * 
 * @author berke.andras
 */
public final class ContentUris {

	public static final String SOURCES_AUTHORITY = "org.openintents.historify.sources";
	
	public static final Uri Sources = Uri.parse("content://"+SOURCES_AUTHORITY+"/sources");
	public static final Uri FilteredSources = Uri.parse("content://"+SOURCES_AUTHORITY+"/filtered_sources");
	public static final Uri Filters = Uri.parse("content://"+SOURCES_AUTHORITY+"/filters");
	public static final Uri QuickPostSources = Uri.withAppendedPath(QuickPosts.SOURCE_URI, QuickPosts.QUICKPOST_SOURCES_PATH);

	public static Uri fromAuthorityString(String authority) {
		return Uri.parse("content://"+authority+"/");
	}
}
