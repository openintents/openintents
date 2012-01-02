/* 
 * Copyright (C) 2008-2012 OpenIntents.org
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

package org.openintents.flashlight;

import android.hardware.Camera;

/*
 * CameraFlash: Wrapper class for Camera API calls
 * that have been introduced in Android 2.0 (API level 5).
 */
public class CameraFlash {

	/* class initialization fails when this throws an exception */
	static {
		try {
			Class.forName("android.hardware.Camera");
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/* calling here forces class initialization */
	public static void checkAvailable() {}

	private Camera mCamera;

	public boolean isOff() {
		return mCamera == null;
	}

	public void lightsOn(){
		if (mCamera == null){
			mCamera = Camera.open();
			Camera.Parameters params = mCamera.getParameters();
			params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			mCamera.setParameters(params);
		}
	}

	public void lightsOff(){
		if (mCamera != null){
			Camera.Parameters params = mCamera.getParameters();
			params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			mCamera.setParameters(params);
			mCamera.release();
			mCamera = null;
		}
	}

}
