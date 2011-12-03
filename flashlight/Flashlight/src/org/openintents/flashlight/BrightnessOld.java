package org.openintents.flashlight;

import android.content.Context;
import android.os.IHardwareService;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.Settings;
import android.util.Log;

class BrightnessOld extends Brightness {

	/** Not valid value of brightness */
	private static final int NOT_VALID = -1;

	private static int mUserBrightness = NOT_VALID;;


	private Context mContext;

	/* class initialization fails when this throws an exception */
	static {
		try
		{
			Class.forName("android.os.IHardwareService");
			Class.forName("android.os.IHardwareService$Stub");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/* calling here forces class initialization */
	public static void checkAvailable() {}

	public static void setGlobalDiv(int div) {

	}

	public BrightnessOld(Context context) {
		mContext = context;
	}

	public void setBrightness(float val) {
		if (val >= 0) {
			int oldSdkValue = (int) (255 * val);
			if (oldSdkValue > 255) {
				oldSdkValue = 255;
			}


			//			boolean res=false;
			// set screen brightness
			if (mUserBrightness == NOT_VALID) {
				mUserBrightness = Settings.System.getInt(mContext.getContentResolver(), 
						Settings.System.SCREEN_BRIGHTNESS, NOT_VALID);
			}
			setBrightnessOld(oldSdkValue);
		} else {
			// newSdkValue < 0
			// => Use default value

			// Unset screen brightness
			if (mUserBrightness != NOT_VALID) {
				//Settings.System.putInt(mContext.getContentResolver(), 
				//		Settings.System.SCREEN_BRIGHTNESS, mUserBrightness);
				setBrightnessOld(mUserBrightness);
				mUserBrightness = NOT_VALID;
			}
		}
	}

	private void setBrightnessOld(int oldSdkValue) {
		Log.d("BrightnessOld for SDK 1.0,1.1","brightness set to >"+oldSdkValue);
		try {
			IHardwareService hardware = IHardwareService.Stub.asInterface(
					ServiceManager.getService("hardware"));
			if (hardware != null) {
				hardware.setScreenBacklight(oldSdkValue);
			}
		} catch (RemoteException doe) {
			Log.d("BrightnessOld for SDK 1.0,1.1","failed to call HardwareService");             
		}  
	}
}