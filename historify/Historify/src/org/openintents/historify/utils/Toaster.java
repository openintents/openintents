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
import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * 
 * Helper class for displaying Toast messages.
 * 
 * @author berke.andras
 */
public class Toaster {

	public static void toast(Context context, int resId) {
		
		toast(context, context.getString(resId));
	}
	
	public static void toast(Context context, String text) {
		
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}
	
	public static void toast(Fragment fragment, int resId) {
		
		toast(fragment.getActivity(), fragment.getString(resId));
	}

	public static void toast(Fragment fragment, String text) {
		
		Toast.makeText(fragment.getActivity(), text, Toast.LENGTH_SHORT).show();
	}
		
	public static void toast(Fragment fragment, int resId, String parameterText) {
		
		Toast.makeText(fragment.getActivity(), String.format(fragment.getString(resId), parameterText), Toast.LENGTH_SHORT).show();
		
	}
	
}
