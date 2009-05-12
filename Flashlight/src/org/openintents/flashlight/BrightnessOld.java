package org.openintents.flashlight;

import android.os.IHardwareService;
import android.os.ServiceManager;
import android.os.RemoteException;
import android.util.Log;

class BrightnessOld {

   /* class initialization fails when this throws an exception */
   static {
		try
		{
		   Class.forName("android.os.IHardwareService.Stub");
		} catch (Exception ex) {
		   throw new RuntimeException(ex);
		}
   }

   /* calling here forces class initialization */
   public static void checkAvailable() {}

   public static void setGlobalDiv(int div) {
       
   }

   public BrightnessOld() {

   }

   public void setBrightness(int val) {
	   Log.d("BrightnessOld for SDK 1.0,1.1","brightness set to >"+val);
	  try {
			   IHardwareService hardware = IHardwareService.Stub.asInterface(
					   ServiceManager.getService("hardware"));
			   if (hardware != null) {
			   hardware.setScreenBacklight(val);
			   }
	   } catch (RemoteException doe) {
					Log.d("BrightnessOld for SDK 1.0,1.1","failed to call HardwareService");             
	  }  
	}
}