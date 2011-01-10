/* 
 * Copyright (C) 2007-2011 OpenIntents.org
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

package org.openintents.distribution;

/**
 * Resources for the distribution package.
 * RD = Resources Distribution
 * @author Peli
 *
 */
public class RD {
	public class layout {
		public static final int eula = R.layout.eula;
	}
	
	public class id {
		///////////////////////////////////////
		// About activity
		public static final int text = R.id.text;
		
		///////////////////////////////////////
		// Eula activity
		public static final int button1 = R.id.button1;
		public static final int button2 = R.id.button2;
		
	}
	
	public class string {
		///////////////////////////////////////
		// Eula activity

		///////////////////////////////////////
		// Update Menu
		public static final int update_box_text = R.string.update_box_text;
		public static final int update_check_now = R.string.update_check_now;
		public static final int update_app_url = R.string.update_app_url;
		public static final int update_app_developer_url = R.string.update_app_developer_url;
		public static final int update_get_updater = R.string.update_get_updater;
		public static final int update_checker_url = R.string.update_checker_url;
		public static final int update_checker_developer_url = R.string.update_checker_developer_url;
		public static final int update_error = R.string.update_error;

		///////////////////////////////////////
		// GetFromMarketDialog
		public static final int aboutapp_not_available = R.string.aboutapp_not_available;
		public static final int aboutapp_get = R.string.aboutapp_get;
		public static final int aboutapp_market_uri = R.string.aboutapp_market_uri;
		public static final int aboutapp_developer_uri = R.string.aboutapp_developer_uri;

		///////////////////////////////////////
		// GetFromMarketDialog
		public static final int filemanager_not_available = R.string.filemanager_not_available;
		public static final int filemanager_get_oi_filemanager = R.string.filemanager_get_oi_filemanager;
		public static final int filemanager_market_uri = R.string.filemanager_market_uri;
		public static final int filemanager_developer_uri = R.string.filemanager_developer_uri;

		///////////////////////////////////////
		// GetFromMarketDialog
		public static final int safe_not_available = R.string.safe_not_available;
		public static final int safe_not_available_decrypt = R.string.safe_not_available_decrypt;
		public static final int safe_get_oi_filemanager = R.string.safe_get_oi_filemanager;
		public static final int safe_market_uri = R.string.safe_market_uri;
		public static final int safe_developer_uri = R.string.safe_developer_uri;	
	}

	public class raw {
		///////////////////////////////////////
		// Eula activity
		public static final int license_short = R.raw.license_short;
		
	}
}
