package org.openintents.news.services;


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

public class NewsServiceSettings extends Activity {
	public static final int ACTIVITY_MODIFY=1001;
	
//	private NotificationManager mNM;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.newsservicesettings);
        
        //use only one instance to save memory
        Button bInstance=(Button)findViewById(R.id.startFeedService);
        bInstance.setOnClickListener(mStartServiceListener);
        bInstance=(Button)findViewById(R.id.stopFeedService);
        bInstance.setOnClickListener(mStopServiceListener);
        bInstance=(Button)findViewById(R.id.servicesettings_save);
        bInstance.setOnClickListener(mSave);
        bInstance=(Button)findViewById(R.id.servicesettings_cancel);
        bInstance.setOnClickListener(mCancel);
        
        
        
       // mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        
    }
    
    private OnClickListener mStartServiceListener=new OnClickListener(){

		@SuppressWarnings("static-access")
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			Toast.makeText(NewsServiceSettings.this, "ehlo1", Toast.LENGTH_SHORT).show();
			NewsreaderService.Test();

			Log.v("ServiceSettings","Starting service");   
			startService(new Intent(
					NewsServiceSettings.this,
					NewsreaderService.class)
			,null);
			
		}
    	
    	
    	
    };
    
    
    private OnClickListener mStopServiceListener=new OnClickListener(){

		public void onClick(View arg0) {
			Log.v("ServiceSettings","Stopping service");
			//mNM.notifyWithText(1, "ehlo2", NotificationManager.LENGTH_SHORT,null);
			Toast.makeText(NewsServiceSettings.this, "ehlo2", Toast.LENGTH_SHORT).show();
			stopService(new Intent(
					NewsServiceSettings.this,
					NewsreaderService.class));
			
		}
    	
    	
    };
    

    
    private OnClickListener mSave=new OnClickListener(){
    	public void onClick(View arg0){
			NewsServiceSettings.this.setResult(Activity.RESULT_OK);
			NewsServiceSettings.this.finish();    		
    	}
    };
    
    
    private OnClickListener mCancel= new OnClickListener(){
    	public void onClick(View arg0){
    		NewsServiceSettings.this.setResult(Activity.RESULT_CANCELED);
    		NewsServiceSettings.this.finish();
    	}
    };
    
	


}/*eoc*/