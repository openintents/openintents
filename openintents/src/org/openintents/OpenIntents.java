/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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

package org.openintents;

/**
 * Provides OpenIntents action and category specifiers.
 * 
 * These specifiers extend the standard Android specifiers.
 */
public abstract class OpenIntents {
	
	// -----------------------------------------------
	//                     Tags
	// -----------------------------------------------
	/** identifier for tag action. */
	public static final String TAG_ACTION = "org.openintents.action.TAG";
	
	// -----------------------------------------------
	//                     Categories
	// -----------------------------------------------
	/** 
	 * Main category specifier.
	 * 
	 * Applications placed into this category in the 
	 * AndroidManifest.xml file are displayed in the
	 * main view of OpenIntents.
	 *  */
	public static final String MAIN_CATEGORY = "org.openintents.category.MAIN";

	/** Settings category specifier.
	 * 
	 * Applications placed into this category in the 
	 * AndroidManifest.xml file are displayed in the
	 * settings tab of OpenIntents.*/
	public static final String SETTINGS_CATEGORY 
		= "org.openintents.category.SETTINGS";
}
