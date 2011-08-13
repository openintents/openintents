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

package org.openintents.samples.timescapeextension.actions;

import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;

/**
 * Helper class for storing the ID of this extension's event source.
 *
 */
public class PersistentSourceId {

	public static void set(Context context, Uri sourceUri) {
		context.getSharedPreferences("source", Context.MODE_PRIVATE).edit().putLong("id", ContentUris.parseId(sourceUri)).commit();
	}
	
	public static long get(Context context) {
		return context.getSharedPreferences("source", Context.MODE_PRIVATE).getLong("id", 0);
	}
}
