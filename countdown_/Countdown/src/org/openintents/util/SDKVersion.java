/* 
 * Copyright (C) 2007-2009 OpenIntents.org
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

package org.openintents.util;

import android.os.Debug;

public class SDKVersion {
	public static int SDKVersion;

   static {
       testSDKVersion();
   };

   private static void testSDKVersion() {
       try {
           Debug.class.getMethod(
                   "dumpHprofData", new Class[] { String.class } );
           // success, this is a newer device
           SDKVersion = 3;
          
	   } catch (NoSuchMethodException nsme) {
	       // failure, must be older device
		   SDKVersion = 2;
	   }
   }
}
