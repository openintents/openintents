/* 
 * Copyright (C) 2007-2009 OpenIntents.org
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

package org.openintents.intents;

/**
 * Intents for automation.
 * 
 * @author Peli
 * @version 1.0.0
 */
public class AutomationIntents {

	/**
	 * Activity Action: This activity is called to create or edit
	 * automation settings.
	 * 
	 * There can be several activities in an apk package that implement this intent.
	 * 
	 * <p>Constant Value: "org.openintents.action.EDIT_AUTOMATION_SETTINGS"</p>
	 */
	public static final String ACTION_EDIT_AUTOMATION = "org.openintents.action.EDIT_AUTOMATION";

	/**
	 * Broadcast Action: This broadcast is sent to the same package in order to
	 * activate an automation.
	 * 
	 * There can only be one broadcast receiver per package that implements this intent.
	 * Any differentiation should be done through intent extras.
	 * 
	 * <p>Constant Value: "org.openintents.action.EDIT_AUTOMATION_SETTINGS"</p>
	 */
	public static final String ACTION_RUN_AUTOMATION = "org.openintents.action.RUN_AUTOMATION";
	
	/**
	 * String extra containing a human readable description of the action to be performed.
	 * 
	 * <p>Constant Value: "org.openintents.extra.DESCRIPTION"</p>
	 */
	public static final String EXTRA_DESCRIPTION = "org.openintents.extra.DESCRIPTION";

}
