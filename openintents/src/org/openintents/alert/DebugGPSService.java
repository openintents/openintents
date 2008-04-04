package org.openintents.alert;

import android.app.NotificationManager;
import android.app.Notification;
import android.app.Service;
import android.os.IBinder;
import android.os.Parcel;
import android.util.Log;
import android.content.ContentUris;
import android.database.Cursor;
import android.widget.Toast;
import android.location.LocationManager;
import android.location.Location;

import android.os.Looper;
import android.os.Handler;
import android.os.Message;

public class DebugGPSService  extends Service implements Runnable{

	private boolean alive=false;

	private static final String _TAG="DebugGPSService";
	LocationManager locMan;
    protected void onCreate() {

        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.
		Toast.makeText(this, "DebugGPSService started", Toast.LENGTH_SHORT).show();
		
		locMan=(LocationManager)getSystemService(android.content.Context.LOCATION_SERVICE);
        this.alive=true;
        Thread thr = new Thread(null, this, "NewsReaderService");
        thr.start();
    }


	public void run() {		
		  Log.d(_TAG,"BEFORE LOOPER BLOCK");
          Looper.prepare();
          
         Handler mHandler = new Handler() {
              public void handleMessage(Message msg) {
                  // process incoming messages here
              }
          };
          
        //  Looper.loop();
		  Log.d(_TAG,"AFERT LOOPER BLOCK");
		while (this.alive){
			
			try{
				Thread.sleep(30*1000);

				Location l=locMan.getCurrentLocation("gps");
				if (l==null)
				{
					//Toast.makeText(this, "Current Position>> null <<", Toast.LENGTH_SHORT).show();
					Log.d(_TAG,"Current Position>> null <<");
				}else{
					//Toast.makeText(this, "Current Position>>"+l.getLatitude()+":"+l.getLongitude()+"<<", Toast.LENGTH_SHORT).show();
					Log.d(_TAG,"Current Position>>"+l.getLatitude()+":"+l.getLongitude()+"<<");
				}
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

}