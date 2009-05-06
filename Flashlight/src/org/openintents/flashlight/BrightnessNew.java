package org.openintents.flashlight;



import android.app.Activity;
import android.view.WindowManager;

import android.util.Log;

class BrightnessNew {

	private Activity activity;
	
   /* class initialization fails when this throws an exception */
   static {
		try
		{

		java.lang.reflect.Field field = WindowManager.LayoutParams.class.getField("screenBrightness");
		
		} catch (Exception ex) {
		   throw new RuntimeException(ex);
		}
   }

   /* calling here forces class initialization */
   public static void checkAvailable() {}


   public BrightnessNew(Activity activity) {
	
		this.activity=activity;
   }

   public void setBrightness(float val) {
	   Log.d("BrightnessNew for SDK 1.5","brightness set to >"+val);
		WindowManager.LayoutParams lp=this.activity.getWindow().getAttributes();
		lp.screenBrightness=val;
		this.activity.getWindow().setAttributes(lp);
   }
}