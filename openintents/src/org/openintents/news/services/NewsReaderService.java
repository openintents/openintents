package org.openintents.news.services;


import android.app.NotificationManager;
import android.app.Notification;
import android.app.Service;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;
import android.content.ContentUris;
import android.database.Cursor;
import android.widget.Toast;

import java.util.HashMap;

import org.openintents.provider.News;
import org.openintents.news.*;



public class NewsReaderService extends Service implements Runnable{

	private boolean alive=false;
	
//	private NotificationManager mNM;
	
	private static final String _TAG="NewsReaderService";

	private static long now;
	
	private Cursor mRSSCursor;
	private Cursor mATMCursor;

	private static final String[] RSS_PROJECTION= new String[] {
		News.RSSFeeds._ID,
		News.RSSFeeds._COUNT,
		News.RSSFeeds.CHANNEL_NAME,
		News.RSSFeeds.CHANNEL_LINK,
		News.RSSFeeds.UPDATE_CYCLE,
		News.RSSFeeds.LAST_UPDATE		
	};
	
	private static final String[] ATOM_PROJECTION=new String[]{
		News.AtomFeeds._ID,
		News.AtomFeeds._COUNT,
		News.AtomFeeds.FEED_TITLE,
		News.AtomFeeds.FEED_LINK_SELF,
		News.AtomFeeds.UPDATE_CYCLE,
		News.AtomFeeds.FEED_LAST_CHECKED

	};


    protected void onCreate() {
    	//Log.v("x","oncreate");     
        
    	//init NotifyManager.
  //  	mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
		Toast.makeText(this, "NewsReaderService started", Toast.LENGTH_SHORT).show();
        
        this.alive=true;
        Thread thr = new Thread(null, this, "NewsReaderService");
        thr.start();
        
    }
	
	
	public IBinder onBind(android.content.Intent i){
		return null;
	}

	@Override
	public void onDestroy(){
		Toast.makeText(this, "NewsReaderService stoping..", Toast.LENGTH_SHORT).show();
		this.alive=false;
	//	mNM.notifyWithText(1, "thread stopping", NotificationManager.LENGTH_SHORT,null);
	}
	

	
	public void run() {		

			mRSSCursor=News.mContentResolver.query(News.RSSFeeds.CONTENT_URI,RSS_PROJECTION,null,null,null);			
			mATMCursor=News.mContentResolver.query(News.RSSFeeds.CONTENT_URI,RSS_PROJECTION,null,null,null);
			


		while (this.alive){
			
			try{
				Thread.sleep(60*1000);
				//wake up cursor, see if data changed.
				mRSSCursor.requery();
				mRSSCursor.first();

				mATMCursor.requery();
				mATMCursor.first();

				int rssLen=mRSSCursor.count();
				int atmLen=mATMCursor.count();

				int rssCLinkCol		=mRSSCursor.getColumnIndex(News.RSSFeeds.CHANNEL_LINK);
				int rssIDCol		=mRSSCursor.getColumnIndex(News.RSSFeeds._ID);
				int rssUpdCyCol		=mRSSCursor.getColumnIndex(News.RSSFeeds.UPDATE_CYCLE);
				int rssLastUpdCol	=mRSSCursor.getColumnIndex(News.RSSFeeds.LAST_UPDATE);
				now=System.currentTimeMillis();
				Log.d(_TAG,"# RSS Feeds>>"+rssLen+"<< , time is >>"+now+"<<");
				for (int i1=0;i1<rssLen ;i1++ )
				{
					Log.d(_TAG,"# i1>>"+i1+"<<");
					
					long lastUpdate=mRSSCursor.getLong(rssLastUpdCol);
					long updateCycle=mRSSCursor.getLong(rssUpdCyCol);
				
					//convert update cycle to milliseconds
					updateCycle=updateCycle * 60 * 1000;
					if (lastUpdate+updateCycle<now)
					{					
						HashMap <String,String>data=new HashMap();
						data.put(News.RSSFeeds._ID,mRSSCursor.getString(rssIDCol));
						data.put(News.RSSFeeds.CHANNEL_LINK,mRSSCursor.getString(rssCLinkCol));
						RSSFetcherThread rt=new RSSFetcherThread(data);
						Log.d(_TAG,"# >>"+i1+"<< will start thread now");
						rt.start();
						Log.d(_TAG,"# >>"+i1+"<< called start.");
					}
					mRSSCursor.next();



				}

				for (int i2=0;i2<atmLen ;i2++ )
				{
					//TODO: query ATM feeds.
				}


				//next thing to do is sleep, so keep resources low.
				mRSSCursor.deactivate();
				mATMCursor.deactivate();
				Log.d(_TAG,"will sleep now");
			}catch(Exception e){   
				
				Log.e(_TAG,"Error:"+e.getMessage());
				e.printStackTrace();
			}
			
		}
		//finished/stopp called. cleanup & exit
		this.cleanup();
	}

	
	private void cleanup(){
		Log.d(_TAG,"Cleaning up...");
	//	mNM.notifyWithText(1, "cleaning up...", NotificationManager.LENGTH_SHORT,null);
		mRSSCursor.close();
		mATMCursor.close();
		
	}
	
	public static void Test(){
		Log.d(_TAG,"TEST CALL");
	}
	
	
	
}/*eoc*/
