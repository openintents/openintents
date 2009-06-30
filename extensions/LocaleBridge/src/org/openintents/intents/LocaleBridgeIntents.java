/* 
 * Copyright (C) 2008-2009 OpenIntents.org
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
 * Intents for Locale bridge.
 * 
 * @author Peli
 * @version 1.0.0
 */
public class LocaleBridgeIntents {

	/**
	 * String extra for a broadcast intent to be performed.
	 * 
	 * IMPORTANT: Encode the Intent to a String using toURI()
	 * before using putExtra().
	 * Convert the String extra back to an Intent using Intent.getIntent().
	 * 
	 * <p>Constant Value: "org.openintents.localebridge.extra.LOCALE_BRIDGE_INTENT"</p>
	 */
	public static final String EXTRA_LOCALE_BRIDGE_INTENT = "org.openintents.localebridge.extra.LOCALE_BRIDGE_INTENT";


	/**
	 * String extra for component name of the setting activity.
	 * 
	 * <p>Constant Value: "org.openintents.localebridge.extra.LOCALE_BRIDGE_COMPONENT"</p>
	 */
	public static final String EXTRA_LOCALE_BRIDGE_COMPONENT = "org.openintents.localebridge.extra.LOCALE_BRIDGE_COMPONENT";

	/**
	 * String extra for component name of the run automation (fire setting) broadcast intent.
	 * 
	 * <p>Constant Value: "org.openintents.localebridge.extra.LOCALE_BRIDGE_COMPONENT"</p>
	 */
	public static final String EXTRA_LOCALE_BRIDGE_RUN_COMPONENT = "org.openintents.localebridge.extra.LOCALE_BRIDGE_RUN_COMPONENT";
	
}
