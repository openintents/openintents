package org.openintents.news.services;
/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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

//import org.openintents.news.services.NewsReaderService;
import org.openintents.R;
import android.app.Activity;
import android.widget.Toast;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class NewsServiceSettings extends Activity {
	public static final int ACTIVITY_MODIFY=1001;
	

	private boolean useWhileRoaming=false;
	private boolean startOnSystemBoot=false;
	private boolean debugMode=false;


	private CheckBox mDebugMode;
	private CheckBox mStartOnBoot;
	private CheckBox mDoRoaming;

	private static final String _TAG="NewsServiceSetting";
//	private NotificationManager mNM;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.newsservicesettings);
        
		SharedPreferences settings = getSharedPreferences(NewsreaderService1.PREFS_NAME, 0);
		useWhileRoaming		=settings.getBoolean(NewsreaderService1.DO_ROAMING,false);
		startOnSystemBoot	=settings.getBoolean(NewsreaderService1.ON_BOOT_START,false);
		debugMode			=settings.getBoolean(NewsreaderService1.DEBUG_MODE,false);

        //use only one instance to save memory
        Button bInstance=(Button)findViewById(R.id.startFeedService);
        bInstance.setOnClickListener(mStartServiceListener);
        bInstance=(Button)findViewById(R.id.stopFeedService);
        bInstance.setOnClickListener(mStopServiceListener);
        bInstance=(Button)findViewById(R.id.servicesettings_save);
        bInstance.setOnClickListener(mSave);
      //  bInstance=(Button)findViewById(R.id.servicesettings_cancel);
//        bInstance.setOnClickListener(mCancel);
        
        
        
       // mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	   mDebugMode=(CheckBox)findViewById(R.id.newsservice_debugmode);
	   mDebugMode.setChecked(debugMode);
	   mStartOnBoot=(CheckBox)findViewById(R.id.newsservice_startonboot);
	   mStartOnBoot.setChecked(startOnSystemBoot);        
	   mDoRoaming=(CheckBox)findViewById(R.id.newsservice_usewhileroaming);
	   mDoRoaming.setChecked(useWhileRoaming);  
    }
    
    private OnClickListener mStartServiceListener=new OnClickListener(){

		@SuppressWarnings("static-access")
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
		//	Toast.makeText(NewsServiceSettings.this, "ehlo1", Toast.LENGTH_SHORT).show();
			NewsreaderService1.Test();

			Log.v("ServiceSettings","Starting service");   
			startService(new Intent(
					NewsServiceSettings.this,
					NewsreaderService1.class)
			,null);
			
		}
    	
    	
    	
    };
    
    
    private OnClickListener mStopServiceListener=new OnClickListener(){

		public void onClick(View arg0) {
			Log.v("ServiceSettings","Stopping service");
			//mNM.notifyWithText(1, "ehlo2", NotificationManager.LENGTH_SHORT,null);
			//Toast.makeText(NewsServiceSettings.this, "ehlo2", Toast.LENGTH_SHORT).show();
			stopService(new Intent(
					NewsServiceSettings.this,
					NewsreaderService1.class));
			
		}
    	
    	
    };
    

    
    private OnClickListener mSave=new OnClickListener(){
    	public void onClick(View arg0){

			NewsServiceSettings.this.savePrefs();
			stopService(new Intent(
					NewsServiceSettings.this,
					NewsreaderService1.class));
			
			startService(new Intent(
					NewsServiceSettings.this,
					NewsreaderService1.class)
			,null);
			/*
			NewsServiceSettings.this.setResult(Activity.RESULT_OK);
			NewsServiceSettings.this.finish();    		
			*/
    	}
    };
    
    
    private OnClickListener mCancel= new OnClickListener(){
    	public void onClick(View arg0){
    		NewsServiceSettings.this.setResult(Activity.RESULT_CANCELED);
    		NewsServiceSettings.this.finish();
    	}
    };
    
	@Override
	protected void onPause(){
		super.onPause();
		savePrefs();
		
	}  	
	private void savePrefs(){
		Log.d(_TAG,"saving preferences");
		debugMode=mDebugMode.isChecked();
		startOnSystemBoot=mStartOnBoot.isChecked();
		useWhileRoaming=mDoRoaming.isChecked();
		// Save user preferences. We need an Editor object to
		// make changes. All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(NewsreaderService1.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		//editor.putBoolean("silentMode", mSilentMode);
		editor.putBoolean(NewsreaderService1.DO_ROAMING,useWhileRoaming);
		editor.putBoolean(NewsreaderService1.ON_BOOT_START,startOnSystemBoot);
		editor.putBoolean(NewsreaderService1.DEBUG_MODE,debugMode);


		// Don't forget to commit your edits!!!
		editor.commit();
	}


}/*eoc*/