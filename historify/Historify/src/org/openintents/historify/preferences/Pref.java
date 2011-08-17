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

package org.openintents.historify.preferences;

/**
 * 
 * Helper class for keys and values stored in the preferences.
 * 
 * @author berke.andras
 */
public final class Pref {

	public static final String TIMELINE_TOP_PANEL_VISIBILITY = "timeline.top_panel";
	public static final boolean DEF_TIMELINE_TOP_PANEL_VISIBILITY = false;

	public static final String TOOLTIP_RESTORE_TOP_PANEL_VISIBILITY = "tooltip.restore_panel";
	public static final boolean DEF_TOOLTIP_VISIBILITY = true;

	public static final String MY_AVATAR_ICON = "my_avatar_icon";

	public enum MyAvatar {
		defaultIcon("default"), customizedIcon("customized");

		String value;

		MyAvatar(String value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value;
		}

		public static MyAvatar fromString(String string) {
			for (MyAvatar v : values()) {
				if (v.toString().equals(string))
					return v;
			}
			return null;
		}
	}

	public static final MyAvatar DEF_AVATAR_ICON = MyAvatar.defaultIcon;

	public static final String STARTUP_ACTION = "startup_action";
	public static final String DEF_STARTUP_ACTION = "show welcome screen";

	public static final String TIMELINE_THEME = "timeline.theme";
	public static final String DEF_TIMELINE_THEME = "bubbles";

	public static final String LAST_SHOWN_CONTACT = "last_show_contact";
	public static final String DEF_LAST_SHOWN_CONTACT = null;

}
