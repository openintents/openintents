/* 
 * Copyright (C) 2008 OpenIntents.org
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
 * @version 2009-02-08
 * 
 * @author Peli
 */
public final class FlashlightIntents {

	/**
	 * Activity Action: Start the flashlight.
	 * 
	 * <p>This starts the flashlight.</p>
	 * 
	 * <p>Constant Value: "org.openintents.action.START_FLASHLIGHT"</p>
	 */
	public final static String ACTION_START_FLASHLIGHT = "org.openintents.action.START_FLASHLIGHT";
	
	/**
	 * Activity Action: Pick color.
	 * 
	 * <p>Displays a color picker. The color is returned in EXTRA_COLOR.</p>
	 * 
	 * <p>Constant Value: "org.openintents.action.SET_FLASHLIGHT_COLOR"</p>
	 */
	public final static String ACTION_PICK_COLOR = "org.openintents.action.PICK_COLOR";
	
	/**
	 * Broadcast Action: Set flashlight property.
	 * 
	 * <p>The color is given in EXTRA_COLOR.</p>
	 * 
	 * <p>Constant Value: "org.openintents.action.SET_FLASHLIGHT"</p>
	 */
	public static final String ACTION_SET_FLASHLIGHT = "org.openintents.action.SET_FLASHLIGHT";

	/**
	 * Color.
	 * 
	 * <p>Color as integer value, as used in setColor() and related.</p>
	 * 
	 * <p>Constant Value: "org.openintents.extra.COLOR"</p>
	 */
	public final static String EXTRA_COLOR = "org.openintents.extra.COLOR";
	
	/**
	 * Brightness.
	 * 
	 * <p>Brightness as float value, from 0 to 1. Use value < 0 for user setting.</p>
	 * 
	 * <p>Constant Value: "org.openintents.extra.BRIGHTNESS"</p>
	 */
	public final static String EXTRA_BRIGHTNESS = "org.openintents.extra.BRIGHTNESS";

}
