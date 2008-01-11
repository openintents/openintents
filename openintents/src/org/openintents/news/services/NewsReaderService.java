package org.openintents.news.services;

import android.app.NotificationManager;
import android.app.Notification;
import android.app.Service;
import android.os.BinderNative;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;


public class NewsReaderService extends Service implements Runnable{

	private boolean alive=false;
	
	private NotificationManager mNM;
	
	
	
    @Override
    protected void onCreate() {
    	//Log.v("x","oncreate");     
        
    	//init NotifyManager.
    	mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.
        
        this.alive=true;
        Thread thr = new Thread(null, this, "NewsReaderService");
        thr.start();
        
    }
	
	@Override
	public IBinder getBinder() {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onDestroy(){
		this.alive=false;
		mNM.notifyWithText(1, "thread stopping", NotificationManager.LENGTH_SHORT,null);
	}
	

	
	public void run() {		
		while (this.alive){
			
			try{
				Thread.sleep(10*1000);
				
				mNM.notifyWithText(1, "thread running", NotificationManager.LENGTH_SHORT,null);
			}catch(Exception e){   
				
				
			}
			
		}
		//finished/stopp called. cleanup & exit
		this.cleanup();
	}

	
	private void cleanup(){
		mNM.notifyWithText(1, "cleaning up...", NotificationManager.LENGTH_SHORT,null);
		
		
	}
	
    // This is the object that receives interactions from clients.  See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new BinderNative() {
        @Override
        protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) {
            return super.onTransact(code, data, reply, flags);
        }
    };
	
	
	
}/*eoc*/
