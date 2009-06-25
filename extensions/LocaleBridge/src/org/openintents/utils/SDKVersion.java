package org.openintents.utils;

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
