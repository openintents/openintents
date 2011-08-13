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

package org.openintents.samples.timescapeextension.actions;

import android.content.ContentResolver;
import android.content.Context;

/**
 * Base class represents a single action processed by the PluginService. 
 *
 */
public abstract class AbstractAction {
	
	protected Context mContext;
	
	protected AbstractAction(Context context) {
		mContext = context;
	}
	
	public abstract void run();
	
	protected ContentResolver getContentResolver() {
		return mContext.getContentResolver();
	}
	
	protected String getString(int resId) {
		return mContext.getResources().getString(resId);
	}
	
	protected String getPackageName() {
		return mContext.getPackageName();
	}

}
