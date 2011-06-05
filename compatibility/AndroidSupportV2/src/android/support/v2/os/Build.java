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

package android.support.v2.os;

/**
 * Information about the current build, extracted from system properties.
 * 
 * This class ensures backward compatibility down to Android 1.1 (API level 2).
 */
public class Build {
	public static class VERSION {
		public static int SDK_INT = 2;
		
		static {
			try {
				// Android 1.6 (v4) and higher:
				// access Build.VERSION.SDK_INT.
				SDK_INT = android.os.Build.VERSION.class.getField("SDK_INT").getInt(null);
			} catch (Exception e) {
				try {
					// Android 1.5 (v3) and lower:
					// access Build.VERSION.SDK.
					SDK_INT = Integer.parseInt((String) android.os.Build.VERSION.class.getField("SDK").get(null));
				} catch (Exception e2) {
					// This should never happen:
					SDK_INT = 2;
				}
			}
		}	
	}
}
