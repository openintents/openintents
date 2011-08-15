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

package org.openintents.timescape.api.data;

import org.openintents.timescape.R;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

/**
 * 
 * Class for loading icon resources.
 * 
 * @author berke.andras
 */
public class PluginIconHelper {
	
	/**
	 * Loading source icon.
	 * @param context
	 * @param source
	 * @param iv
	 */
	public void toImageView(Context context, Uri resUri, ImageView iv) {

		
		if(resUri==null) {
			//default icon
			iv.setImageResource(R.drawable.plugin_default);
		}
		
		else if(ContentResolver.SCHEME_ANDROID_RESOURCE.equals(resUri.getScheme())) {
			
			try {
				//accessing other applications drawable resource
				String packageName = resUri.getAuthority();
				String type = "drawable";
				String name = resUri.getLastPathSegment();
				int resId = context.getPackageManager().getResourcesForApplication(resUri.getAuthority()).getIdentifier(name, type, packageName);
				
				Drawable d = context.getPackageManager().getDrawable(packageName, resId, null);
				iv.setImageDrawable(d);
				
			} catch(Exception e) {
				e.printStackTrace();
				iv.setImageResource(R.drawable.plugin_default);
			}
		}
		
		else {
			//file:// or content://
			iv.setImageURI(resUri);
		}
	}
}
