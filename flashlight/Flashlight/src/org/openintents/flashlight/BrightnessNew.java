package org.openintents.flashlight;



import android.app.Activity;
import android.view.WindowManager;

import android.util.Log;

class BrightnessNew extends Brightness {

	private Activity activity;
	static private java.lang.reflect.Field fieldScreenBrightness;

	/* class initialization fails when this throws an exception */
	static {
		try
		{

			fieldScreenBrightness = WindowManager.LayoutParams.class.getField("screenBrightness");

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

		// SDK 1.5 call:
		//lp.screenBrightness=val;

		try {
			fieldScreenBrightness.setFloat(lp, val);
		} catch (IllegalAccessException e) {
			Log.d("BrightnessNew for SDK 1.5", "Setting brightness failed");
		}

		this.activity.getWindow().setAttributes(lp);
	}
}