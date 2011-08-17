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

package org.openintents.historify.utils;

import android.os.Handler;
import android.os.Message;
import android.widget.BaseAdapter;

/**
 * Helper class for refreshing pretty times (like '5 minutes ago') in adapters
 * that uses the pretty time format.
 * 
 * @author berke.andras
 */
public class PrettyTimeRefreshHelper {

	private static class RefreshHandler extends Handler {

		private BaseAdapter mAdapter;

		public RefreshHandler(BaseAdapter adapter) {
			super();
			mAdapter = adapter;
		}

		@Override
		public void handleMessage(Message msg) {
			if (mAdapter.getCount() != 0)
				mAdapter.notifyDataSetChanged();
		}
	}

	private static class PrettyTimeRefresherThread extends Thread {

		private RefreshHandler mRefreshHandler;
		private boolean running = true;

		public PrettyTimeRefresherThread(RefreshHandler refreshHandler) {
			mRefreshHandler = refreshHandler;
		}

		@Override
		public void run() {

			while (running) {
				try {
					Thread.sleep(60000);
					mRefreshHandler.sendEmptyMessage(0);
				} catch (InterruptedException e) {

				}
			}
		}

		public void requestStop() {
			running = false;
			interrupt();
			try {
				join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private PrettyTimeRefresherThread mPrettyTimeRefresherThread;

	public void startRefresher(BaseAdapter adapter) {

		stopRefresher();

		mPrettyTimeRefresherThread = new PrettyTimeRefresherThread(
				new RefreshHandler(adapter));
		mPrettyTimeRefresherThread.setName("PrettyTimeRefresherThread");
		mPrettyTimeRefresherThread.start();
	}

	public void stopRefresher() {

		if (mPrettyTimeRefresherThread != null) {
			mPrettyTimeRefresherThread.requestStop();
			mPrettyTimeRefresherThread = null;
		}
	}

}
