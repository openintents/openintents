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

package org.openintents.historify.data.providers.internal;

import org.openintents.historify.utils.UriUtils;

import android.net.Uri;
import android.provider.CallLog;

/**
 * 
 * Helper class for constants in {@link MessagingProvider}.
 * 
 * @author berke.andras
 */
public class Messaging {

	public static final String SOURCE_NAME = "Messaging";
	public static final String DESCRIPTION = "Sent and received sms messages.";

	public static final String MESSAGING_AUTHORITY = "org.openintents.historify.internal.messaging";
	public static final Uri SOURCE_URI = UriUtils
			.sourceAuthorityToUri(MESSAGING_AUTHORITY);

	// undocumented messaging provider API
	// handle with care
	public static class Messages {
		public static final Uri CONTENT_URI = Uri.parse("content://sms/");

		public static final String _ID = "_id";
		public static final String ADDRESS = "address";
		public static final String DATE = "date";
		public static final String TYPE = "type";
		public static final String BODY = "body";

		public static final int INCOMING_TYPE = CallLog.Calls.INCOMING_TYPE;
		public static final int OUTGOING_TYPE = CallLog.Calls.OUTGOING_TYPE;
	}
	
}
