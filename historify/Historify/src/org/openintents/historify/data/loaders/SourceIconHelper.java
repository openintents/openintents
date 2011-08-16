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

package org.openintents.historify.data.loaders;

import org.openintents.historify.R;
import org.openintents.historify.data.model.Event;
import org.openintents.historify.data.model.IconLoadingStrategy;
import org.openintents.historify.data.model.source.EventSource;
import org.openintents.historify.utils.UriUtils;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

/**
 * 
 * Class for loading icon resources.
 * 
 * @author berke.andras
 */
public class SourceIconHelper {
 	

	
	/**
	 * Sets the timeline icon of the given ImageView. 
	 * 
	 * @param context Context
	 * @param source The source the event belongs to.
	 * @param event The event which icon should be loaded.
	 * @param iv The ImageView where the icon should appear.
	 */
	public void toImageView(Context context, EventSource source, Event event, ImageView iv) {
		
		if(source.getIconLoadingStrategy()==IconLoadingStrategy.useSourceIcon || event==null /*|| !source.isInternal()*/) {
			//source icon is used
			toImageView(context, source.getIcon(), iv);
		} else {
			//event icon is used
			toImageView(context, event.getCustomIcon(), iv);
		}
		
	}

	/**
	 * Loading source icon to a given ImageView.
	 * @param context Context
	 * @param resUri The resource Uri of the image which should be loaded.
	 * @param iv The ImageView where the icon should appear.
	 */
	public void toImageView(Context context, Uri resUri, ImageView iv) {

		if(resUri==null) {
			//default icon
			iv.setImageResource(R.drawable.source_default);
		}
		
		else if(ContentResolver.SCHEME_ANDROID_RESOURCE.equals(resUri.getScheme())) {
			
			//package drawable
			String packageName = resUri.getAuthority();
			String type = UriUtils.DRAWABLE_TYPE;
			String name = resUri.getLastPathSegment();
			int resId;
			try {
				resId = context.getPackageManager().getResourcesForApplication(resUri.getAuthority()).getIdentifier(name, type, packageName);
				Drawable d = context.getPackageManager().getDrawable(packageName, resId, null);
				iv.setImageDrawable(d);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				iv.setImageResource(R.drawable.source_default);
			}
			
		}
		
		else if(UriUtils.APP_ICON_SCHEME.equals(resUri.getScheme())) {
			//external application's icon
			String packageName = resUri.getAuthority();
			try {
				iv.setImageDrawable(context.getPackageManager().getApplicationIcon(packageName));
			} catch (NameNotFoundException e) {
				e.printStackTrace();
				iv.setImageResource(R.drawable.source_default);
			}
		}
		
		else {
			//file:// or content://
			iv.setImageURI(resUri);
		}
	}
}
