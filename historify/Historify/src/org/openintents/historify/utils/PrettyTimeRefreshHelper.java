package org.openintents.historify.utils;


import android.os.Handler;
import android.os.Message;
import android.widget.BaseAdapter;

public class PrettyTimeRefreshHelper {

	private PrettyTimeRefresherThread mPrettyTimeRefresherThread;
	
	private static class RefreshHandler extends Handler {
		
		private BaseAdapter mAdapter;
		
		public RefreshHandler(BaseAdapter adapter) {
			super();
			mAdapter = adapter;
		}
		
		@Override
		public void handleMessage(Message msg) {
			if(mAdapter.getCount()!=0)
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
			
			while(running) {
				try {
					Thread.sleep(60000);
					mRefreshHandler.sendEmptyMessage(0);
				} catch(InterruptedException e) {
					e.printStackTrace();
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
	
	public void startRefresher(BaseAdapter adapter) {
		
		stopRefresher();
		
		mPrettyTimeRefresherThread = new PrettyTimeRefresherThread(new RefreshHandler(adapter));
		mPrettyTimeRefresherThread.setName("PrettyTimeRefresherThread");
		mPrettyTimeRefresherThread.start();
	}

	public void stopRefresher() {
		
		if(mPrettyTimeRefresherThread!=null) {
			mPrettyTimeRefresherThread.requestStop();
			mPrettyTimeRefresherThread = null;
		}
	}

}
