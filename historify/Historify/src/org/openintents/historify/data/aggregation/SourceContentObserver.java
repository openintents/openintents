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

package org.openintents.historify.data.aggregation;

import org.openintents.historify.data.model.source.EventSource;
import org.openintents.historify.uri.ContentUris;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

/**
 * 
 * Content observer for a single source of events. When notified, it triggers
 * the observer of the event aggregator.
 * 
 * @author berke.andras
 * 
 */
public class SourceContentObserver extends ContentObserver {

	private static final Uri sUriToNotify = ContentUris.MergedEvents;

	private EventSource mSource;
	private Context mContext;

	public SourceContentObserver(Context context, EventSource source) {
		super(new Handler());
		mContext = context;
		mSource = source;
	}

	@Override
	public boolean deliverSelfNotifications() {
		return true;
	}

	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		Log.v(EventAggregator.N, "Contents of " + mSource.getName()
				+ " have been changed.");
		mContext.getContentResolver().notifyChange(sUriToNotify, null);

	}
}
