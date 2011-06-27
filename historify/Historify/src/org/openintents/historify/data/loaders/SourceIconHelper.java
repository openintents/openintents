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
import org.openintents.historify.data.model.source.AbstractSource;
import org.openintents.historify.utils.UriUtils;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.widget.ImageView;

/**
 * 
 * Class for loading icon resources.
 * 
 * @author berke.andras
 */
public class SourceIconHelper {
 	
	public enum IconLoadingStrategy {
		useSourceIcon, useEventIcon;

		public static IconLoadingStrategy parseString(String string) {
			for(IconLoadingStrategy value : values()) {
				if(value.toString().equals(string)) return value;
			}
			return null;
		}
	}
	
	public void toImageView(Context context, AbstractSource source, Event event, ImageView iv) {
		
		if(source.getIconLoadingStrategy()==IconLoadingStrategy.useSourceIcon || event==null || !source.isInternal()) {
			toImageView(context, source.getIcon(), iv);
		} else {
			//used for loading custom event icons (eg. QuickPost)
			toImageView(context, event.getCustomIcon(), iv);
		}
		
	}

	/**
	 * Loading source icon.
	 * @param context
	 * @param source
	 * @param iv
	 */
	private void toImageView(Context context, Uri resUri, ImageView iv) {

		if(resUri==null) {
			//default icon
			iv.setImageResource(R.drawable.source_default);
		}
		
		else if(UriUtils.INTERNAL_DRAWABLE_SCHEME.equals(resUri.getScheme())) {
			//package drawable
			String resName = resUri.getAuthority();
			iv.setImageResource(context.getResources().getIdentifier(resName, "drawable", context.getPackageName()));
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
