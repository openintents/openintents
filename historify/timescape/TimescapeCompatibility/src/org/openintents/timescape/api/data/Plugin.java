/*
 * Copyright (C) 2010 Sony Ericsson Mobile Communications AB.
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
 * limitations under the License
 *
 */

package org.openintents.timescape.api.data;

import android.net.Uri;

public class Plugin {

	private String mName;
	private Uri mIconUri;
	private String mConfigurationActivity;
	private String mConfigurationText;
	
	public Plugin(String name, Uri iconUri, String configurationActivity, String configurationText) {
		
		mName = name;
		mIconUri = iconUri;
		mConfigurationActivity = configurationActivity;
		mConfigurationText = configurationText;
	}
	
	public String getName() {
		return mName;
	}
	
	public Uri getIconUri() {
		return mIconUri;
	}
	
	public String getConfigurationActivity() {
		return mConfigurationActivity;
	}
	
	public String getConfigurationText() {
		return mConfigurationText;
	}
}
