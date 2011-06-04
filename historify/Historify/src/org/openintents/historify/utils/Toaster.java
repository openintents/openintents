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

import android.content.Context;
import android.widget.Toast;

/**
 * 
 * Helper class for displaying Toast messages.
 * 
 * @author berke.andras
 */
public class Toaster {

	public static void toast(Context context, int resId) {
		
		Toast.makeText(context, context.getString(resId), Toast.LENGTH_SHORT).show();
	}

	public static void toast(Context context, int resId, String parameterText) {
		
		Toast.makeText(context, String.format(context.getString(resId), parameterText), Toast.LENGTH_SHORT).show();
		
	}
	
}
