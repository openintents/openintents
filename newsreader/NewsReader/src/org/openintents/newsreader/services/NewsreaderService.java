package org.openintents.newsreader.services;

/*
*	This file is part of OI Newsreader.
*	Copyright (C) 2007-2009 OpenIntents.org
*	OI Newsreader is free software: you can redistribute it and/or modify
*	it under the terms of the GNU General Public License as published by
*	the Free Software Foundation, either version 3 of the License, or
*	(at your option) any later version.
*
*	OI Newsreader is distributed in the hope that it will be useful,
*	but WITHOUT ANY WARRANTY; without even the implied warranty of
*	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*	GNU General Public License for more details.
*
*	You should have received a copy of the GNU General Public License
*	along with OI Newsreader.  If not, see <http://www.gnu.org/licenses/>.
*/

import java.util.HashMap;

import org.openintents.newsreader.R;
import org.openintents.newsreader.messages.AFeedMessages;
import org.openintents.provider.News;
import org.openintents.provider.News.Channel;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import android.telephony.TelephonyManager;

public class NewsreaderService extends Service implements Runnable {

	private static boolean alive = false;

	public static final String DO_ROAMING = "use_while_roaming";
	public static final String ON_BOOT_START = "on_boot_start";
	public static final String SERVICE_ENABLED = "service_enabled";

	// private NotificationManager mNM;

	private static final String _TAG = "NewsReaderService";

	private Thread t;

	private static long now;

	private Cursor mCursor;

	private boolean useWhileRoaming = false;
	private boolean isRoaming = false;
	private boolean serviceEnabled=true;
	private boolean startOnSystemBoot = false;

        private boolean forceUpdate=false;

	private News mNews;

	private Looper mServiceLooper;

	private org.openintents.newsreader.services.NewsreaderService.ServiceHandler mServiceHandler;

	private static final String[] PROJECTION = new String[] { News.Channel._ID,
			News.Channel.CHANNEL_NAME, News.Channel.CHANNEL_LINK,
			News.Channel.UPDATE_CYCLE,

			News.Channel.CHANNEL_TYPE, News.Channel.NOTIFY_NEW,
			News.Channel.LAST_UPDATE, News.Channel.CHANNEL_ICON_URI,
			News.Channel.UPDATE_MSGS };

	int rssIDCol = 0;
	int rssNameCol = 1;
	int rssCLinkCol = 2;
	int rssUpdCyCol = 3;
	int rssTypeCol = 4;
	int rssNotifyCol = 5;
	int rssLastUpdCol = 6;
	int rssIconCol = 7;
	int rssUpdateMsgsCol = 8;

	public static final String PREFS_NAME = "org.openintents.news_preferences";

	public static final int NEW_NEWS_NOTIFICATION = 100;

	private static final int MSG_UPDATE_FEED = 1;
	private static final int MSG_TIME_CHANGED = 2;

	public static final String EXTRA_UPDATE_FEED = "updateFeed";

	private final class ServiceHandler extends Handler {

		public ServiceHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_UPDATE_FEED) {
				Log.v(_TAG, "id : " + (String) msg.obj);
				Cursor cursor = getContentResolver()
						.query(
								Uri.withAppendedPath(News.Channel.CONTENT_URI,
										(String) msg.obj), PROJECTION, null,
								null, null);
				if (cursor.moveToFirst()) {
					//updateFeed(cursor);
				} else {
					Log.v(_TAG, "id : " + (String) msg.obj + " feed deleted.");
				}
				cursor.close();

			} else if (msg.what == MSG_TIME_CHANGED) {
				mServiceHandler.removeMessages(MSG_UPDATE_FEED, msg.obj);
				Message msg2 = mServiceHandler.obtainMessage();
				msg2.obj = msg.obj;
				msg2.what = MSG_UPDATE_FEED;
				mServiceHandler.sendMessage(msg2);
			}
		}
	}

	public void onCreate() {

		mNews = new News(getContentResolver());

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		this.useWhileRoaming = settings.getBoolean(
				NewsreaderService.DO_ROAMING, false);
		this.startOnSystemBoot = settings.getBoolean(
				NewsreaderService.ON_BOOT_START, true);

		this.serviceEnabled=settings.getBoolean(SERVICE_ENABLED,true);

		Log.d(_TAG, "startup: \nuseWhileRoaming>>" + useWhileRoaming
				+ "<<\n startOnSystemBoot>>+" + startOnSystemBoot + "<<");		

		//HandlerThread thread = new HandlerThread("NewsreaderService");
		//thread.start();

	//	mServiceLooper = thread.getLooper();
	//	mServiceHandler = new ServiceHandler(mServiceLooper);

	}

	public IBinder onBind(android.content.Intent i) {
		return null;
	}

	@Override
	public void onStart(Intent intent,int i) {
	    Toast.makeText(this, R.string.newsservicestarted,
			    Toast.LENGTH_SHORT).show();
	    //new Thread(null, this, "NewsReaderService").start();

            forceUpdate=intent.getBooleanExtra("FORCE_UPDATE",false);
            Log.d(_TAG,"forceUpdate>>"+forceUpdate+"<<");
	    if (mCursor!=null)
	    {
		    Log.d(_TAG,"cursor is still here");
		    mCursor.requery();
	    }else{
		    Log.d(_TAG,"cursor is DEAD, recreate");
		    mCursor = getContentResolver().query(News.Channel.CONTENT_URI,
			    PROJECTION, null, null, null);			
	    }

	    TelephonyManager telMan=(TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
	    boolean isRoaming=telMan.isNetworkRoaming();
	    Log.w(_TAG,"roaming state of device is>"+isRoaming+"<");
	    if (isRoaming==false)
	    {
		    run();
	    }else{
		    Log.w(_TAG,"roaming state of device is>"+isRoaming+"<, DOING NOTHING");
	    }
	    stopSelf();
	    
	}

	@Override
	public void onDestroy() {
	//	if (alive) {
	//		Toast.makeText(this, R.string.newsservicestopping,
	//				Toast.LENGTH_SHORT).show();
	//	}
		//alive = false;
		//Log.d(_TAG, "NOTinLoop:alive?" + alive);
	//	mServiceLooper.quit();
		Log.d(_TAG,"onDestroy");
		if (this.serviceEnabled && forceUpdate==false)
		{
			scheduleRun();
		}
		

		cleanup();
	}


	public void run() {

		NewsreaderService.alive = true;
		


		Log.d(_TAG, "firstRun:alive?" + alive);
		try {

			mCursor.moveToFirst();

			int rssLen = mCursor.getCount();

			now = System.currentTimeMillis();

			Log.v(_TAG, "# RSS Feeds>>" + rssLen + "<< , time is >>" + now
					+ "<<");

			for (int i1 = 0; i1 < rssLen; i1++) {

				Log.v(_TAG, "# i1>>" + i1 + "<<");

				updateFeed(mCursor,false);

				mCursor.moveToNext();

			}

			// next thing to do is sleep, so keep resources low.
			mCursor.deactivate();

			Log.v(_TAG, "update done.");

		} catch (Exception e) {

			Log.e(_TAG, "Error:" + e.getMessage());
			e.printStackTrace();
		}
	}

	private void updateFeed(Cursor cursor,boolean forceUpdate) {
		long updateCycle = cursor.getLong(rssUpdCyCol);
		long lastupdate = cursor.getLong(rssLastUpdCol);
		String name = cursor.getString(rssNameCol);
		long now=System.currentTimeMillis();
		// convert update cycle to milliseconds
		updateCycle = updateCycle * 60 * 1000;
				

		if ((updateCycle == Long.MAX_VALUE || updateCycle == 0 )&& forceUpdate==false) {
			// ignore entries with never update
			Log.d(_TAG, "channel " + name + " ignored due to 'never' update.");
			return;

		}else if ((lastupdate+updateCycle>now) && forceUpdate==false)
		{
			Log.d(_TAG,"channel " +name+" ignored, update cycle not reached.");
			return;


		} else {

			Log.d(_TAG, "channel " + name + " updating " + updateCycle);

			String iconUri = cursor.getString(rssIconCol);
			int channelType = cursor.getInt(rssTypeCol);
			int notifyNew = cursor.getInt(rssNotifyCol);
			int updateMsgs = cursor.getInt(rssUpdateMsgsCol);
			String id = cursor.getString(rssIDCol);
			String channelLink = cursor.getString(rssCLinkCol);

			HashMap<String, String> data = new HashMap<String, String>();
			data.put(News.Channel._ID, id);
			data.put(News.Channel.CHANNEL_LINK, channelLink);
			data.put(News.Channel.NOTIFY_NEW, String.valueOf(notifyNew));
			data.put(News.Channel.UPDATE_MSGS, String.valueOf(updateMsgs));
			data.put(News.Channel.CHANNEL_NAME, name);
			AbstractFeedFetcherThread rt = null;
			if (channelType == News.CHANNEL_TYPE_RSS) {
				rt = new RSSSaxFetcherThread(data, mNews, this.getApplicationContext());
			} else if (channelType == News.CHANNEL_TYPE_ATM) {
				rt = new AtomSaxFetcherThread(data, mNews, this.getApplicationContext());
			} else {
			}

			if (rt != null) {
				//((AbstractFeedFetcherThread) rt).setNewsreaderService(this);

				Log.v(_TAG, "# >>" + id + "<< will start thread now");

				rt.start();

				Log.v(_TAG, "# >>" + id + "<< called start.");

			}

			if (iconUri == null || iconUri.equals("")) {
				data = new HashMap<String, String>();
				data.put(News.Channel._ID, id);
				data.put(News.Channel.CHANNEL_NAME, name);
				data.put(News.Channel.CHANNEL_LINK, channelLink);
				data.put(News.Channel.CHANNEL_TYPE, Integer
						.toString(channelType));
				IconRetrieverThread ir = new IconRetrieverThread(this, data);

				Log.v(_TAG, "# >>" + id + "<< will start ICON thread now");

				ir.start();

				Log.v(_TAG, "# >>" + id + "<< called start.");

			}			
			
			// send delayed message
		//	Message msg = mServiceHandler.obtainMessage();
		//	msg.obj = id;
		//	msg.what = MSG_UPDATE_FEED;
		//	mServiceHandler.sendMessageDelayed(msg, updateCycle);
		}
	}

	private void cleanup() {
		Log.d(_TAG, "Cleaning up...");
		// mNM.notifyWithText(1, "cleaning up...",
		// NotificationManager.LENGTH_SHORT,null);
		mCursor.close();
		// mATMCursor.close();

	}

	public static void Test() {
		Log.d(_TAG, "TEST CALL");
	}

	public static boolean isAlive() {
		return alive;
	}
	
	private void scheduleRun(){
		new ServiceHelper(getApplicationContext()).scheduleRun();
	}


}/* eoc */
