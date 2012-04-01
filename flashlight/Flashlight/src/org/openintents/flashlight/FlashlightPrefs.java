package org.openintents.flashlight;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class FlashlightPrefs extends PreferenceActivity {

	public static final String PREFKEY_SAVED_COLOR = "saved_color";

	public static final String PREFKEY_USE_CAMERA_FLASH = "use_camera_flash";
	public static final String PREFKEY_COLOR_OPTIONS = "color_options";
	

	public static final boolean DEFAULT_USE_CAMERA_FLASH = false;
	public static final String DEFAULT_COLOR_OPTIONS = "0";


	// ========================================================================
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		addPreferencesFromResource(R.xml.settings);
	}

}