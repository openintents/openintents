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


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;

import org.openintents.newsreader.R;
import org.openintents.newsreader.messages.AFeedMessages;
import org.openintents.provider.News;
import org.openintents.provider.News.Channel;
import android.util.Log;
import android.net.Uri;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.database.Cursor;
import android.app.AlarmManager;
import java.util.Date;

public class ServiceHelper{

	private Context mContext;

	public ServiceHelper(Context c){
		mContext=c;
	}

	private static final String _TAG="ServiceHelper";

	public  void notifyNewEntries(int count, String id) {

		Uri uri = Uri.withAppendedPath(Channel.CONTENT_URI, id);
		Log.v(_TAG, uri.toString());
		Cursor cursor = mContext.getContentResolver().query(uri,
				News.Channel.PROJECTION_MAP, null, null, null);
		if (cursor.moveToFirst()) {
			String feedType = cursor.getString(cursor
					.getColumnIndexOrThrow(News.Channel.CHANNEL_TYPE));
			String feedname = cursor.getString(cursor
					.getColumnIndexOrThrow(News.Channel.CHANNEL_NAME));
			String feedLink = cursor.getString(cursor
					.getColumnIndexOrThrow(News.Channel.CHANNEL_LINK));

			// Set the icon, scrolling text and that it should be shown
			// immediately
			Notification notification = new Notification(
					R.drawable.newsreader_icon, 
					mContext.getString(	R.string.newnews, feedname, String.valueOf(count)),
					System.currentTimeMillis());

			// The PendingIntent to launch our activity if the user selects this
			// notification

			Bundle b = new Bundle();
			b.putString(News.Channel.CHANNEL_TYPE, feedType);
			b.putString(News.Channel._ID, id);
			b.putString(News.Channel.CHANNEL_NAME, feedname);
			b.putString(News.Channel.CHANNEL_LINK, feedLink);

			Intent i = new Intent(mContext, AFeedMessages.class);
			i.setData(uri);
			i.putExtras(b);

			PendingIntent contentIntent = PendingIntent.getActivity(mContext, 0, i,
					0);
			notification.setLatestEventInfo(mContext,
					mContext.getString(R.string.newsreader_app_name), 
					mContext.getString(R.string.newnews, feedname, String.valueOf(count)),
					contentIntent);

			// Send the notification.
			// We use a layout id because it is a unique number. We use it later
			// to
			// cancel.
			((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE))
					.notify(NewsreaderService.NEW_NEWS_NOTIFICATION, notification);
			Log.v(_TAG, "notification sent:" + notification);
		} else {
			Log.v(_TAG, "notification not sent.");
		}
	}


	public void scheduleRun(){
		AlarmManager am=(AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
		long time=System.currentTimeMillis();
		time=time+(10*60*1000);
		Intent intent = new Intent(mContext,NewsreaderService.class);
		PendingIntent operation=PendingIntent.getService(mContext,0,intent,PendingIntent.FLAG_ONE_SHOT);
		Log.i(_TAG,"scheduling run of newsreader service for >"+java.text.DateFormat.getInstance().format(new Date(time)));
		am.set(AlarmManager.RTC_WAKEUP,time,operation);

	
	}

}