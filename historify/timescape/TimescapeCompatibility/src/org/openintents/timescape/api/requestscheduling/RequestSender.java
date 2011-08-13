/*
 * Copyright (C) 2010 Sony Ericsson Mobile Communications AB.
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
 * limitations under the License
 *
 */

package org.openintents.timescape.api.requestscheduling;

import com.sonyericsson.eventstream.EventStreamConstants;

import android.content.Context;
import android.content.Intent;

public class RequestSender {

	public void requestRegisterPlugin(Context context) {
		
		Intent intent = new Intent();
		intent.setAction(EventStreamConstants.Intents.REGISTER_PLUGINS_REQUEST_INTENT);
		context.sendBroadcast(intent);
	}

	public void requestRefresh(Context context) {

		Intent intent = new Intent();
		intent.setAction(EventStreamConstants.Intents.REFRESH_REQUEST_INTENT);
		context.sendBroadcast(intent);
	}
}
