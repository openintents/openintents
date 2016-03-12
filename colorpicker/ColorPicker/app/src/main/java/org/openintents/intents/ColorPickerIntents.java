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
 * @version 2010-05-30
 * 
 * @author Peli
 * @author Karl Ostmo
 */
public final class ColorPickerIntents {

	/**
	 * Activity Action: Pick color.
	 * 
	 * <p>Displays a color picker. The color is returned in EXTRA_COLOR.</p>
	 * 
	 * <p>Constant Value: "org.openintents.action.SET_FLASHLIGHT_COLOR"</p>
	 */
	public final static String ACTION_PICK_COLOR = "org.openintents.action.PICK_COLOR";

	/**
	 * Color.
	 * 
	 * <p>Color as integer value, as used in setColor() and related.</p>
	 * 
	 * <p>Constant Value: "org.openintents.extra.COLOR"</p>
	 */
	public final static String EXTRA_COLOR = "org.openintents.extra.COLOR";
}
