package org.openintents.wifiqr.android.activity;

/**
****************************************************************************
* Copyright (C) 2010 OpenIntents.org                                       *
*                                                                          *
* Licensed under the Apache License, Version 2.0 (the "License");          *
* you may not use this file except in compliance with the License.         *
* You may obtain a copy of the License at                                  *
*                                                                          *
*      http://www.apache.org/licenses/LICENSE-2.0                          *
*                                                                          *
* Unless required by applicable law or agreed to in writing, software      *
* distributed under the License is distributed on an "AS IS" BASIS,        *
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
* See the License for the specific language governing permissions and      *
* limitations under the License.                                           *
****************************************************************************
*/

import org.openintents.wifiqr.R;
import org.openintents.wifiqr.WifiQR;
import org.openintents.wifiqr.R.layout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public class WQRMainActivity extends Activity {
    protected static final int RC_SCAN_CODE = 333;
	protected static final int RC_GEN_CODE = 444;
	private static final String TAG = WQRMainActivity.class.getSimpleName();


	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        ((ImageButton)findViewById(R.id.button_opengenerate)).setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				Intent i=new Intent();
				i.setClass(getApplicationContext(), ChooseNetworkConfigActivity.class);
				startActivityForResult(i, RC_GEN_CODE);
				
			}			
		});

        ((ImageButton)findViewById(R.id.button_openscan)).setOnClickListener(new View.OnClickListener(){

			public void onClick(View v) {
				Intent i=new Intent();
				i.setAction("com.google.zxing.client.android.SCAN");
				i.putExtra("SCAN_MODE","QR_CODE_MODE");
				startActivityForResult(i, RC_SCAN_CODE);
				
			}
		});
        
        checkForZxing();
        
        
    }
    
    
    private void checkForZxing(){
    	PackageManager pm= getPackageManager();
    	
    	try 
    	{
			pm.getApplicationInfo("com.google.zxing.client.android", PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			AlertDialog.Builder builder= new AlertDialog.Builder(this); 
			builder.setMessage(R.string.install_zxing);
			builder.setPositiveButton(android.R.string.ok, new OnClickListener(){

				public void onClick(DialogInterface dialog, int which) {
					Intent intent =new Intent();
					intent.setData(Uri.parse("market://search?p=com.google.zxing.client.android"));
					intent.setAction(Intent.ACTION_VIEW);
					startActivity(intent);
				}});
			builder.create().show();
		}
    }


	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(requestCode==RC_GEN_CODE && data!=null){
			String configStr=data.getStringExtra(WifiQR.EXTRA_WIFI_CONFIG_STRING);
			Log.v(TAG,"configstr="+configStr);
			Intent intent=new Intent();
			intent.putExtra("ENCODE_DATA", configStr);
			intent.putExtra("ENCODE_TYPE","TEXT_TYPE");
			intent.setAction("com.google.zxing.client.android.ENCODE");
			startActivity(intent);
		}else if (requestCode==RC_SCAN_CODE && data!=null){
			String configStr=data.getStringExtra("SCAN_RESULT");
			Log.v(TAG,"configstr="+configStr);
		}
		
	}
    
    
    
    
}