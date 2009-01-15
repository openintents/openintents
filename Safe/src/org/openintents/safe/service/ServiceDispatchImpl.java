/* $Id: $
 * 
 * Copyright 2008 Isaac Potoczny-Jones
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

package org.openintents.safe.service;

// TODO: Currently the timer MIGHT not actually de-activate the service
// if there are still clients attached.  Should be fixed.

import org.openintents.intents.CryptoIntents;
import org.openintents.safe.CryptoHelper;
import org.openintents.safe.CryptoHelperException;


import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.os.CountDownTimer;

public class ServiceDispatchImpl extends Service {
	private CryptoHelper ch;
	private String masterKey;
    private CountDownTimer t;
    private int timeoutMinutes = 5;
	private long timeoutUntilStop = timeoutMinutes * 60000;
    
    @Override
    public IBinder onBind(Intent intent) {
        // Select the interface to return.  If your service only implements
        // a single interface, you can just return it here without checking
        // the Intent.
    	return (mBinder);
    }

    @Override
    public void onCreate() {
      super.onCreate();
      
	  Log.d( "ServieDispatchImpl","onCreate" );
    }
    
    @Override
    public void onDestroy() {
	  super.onDestroy();
	  masterKey = null;
	  ch = null;
	  ServiceNotification.clearNotification(ServiceDispatchImpl.this);
	  
	  Intent intent = new Intent(CryptoIntents.ACTION_CRYPTO_LOGGED_OUT);
	  sendBroadcast(intent);
	  
	  Log.d( "ADDERSERVICEIMPL","onDestroy" );
    }
    
    private void startTimer () {
    	t = new CountDownTimer(timeoutUntilStop, timeoutUntilStop) {
    		public void onTick(long millisUntilFinished) {
    			//doing nothing.
    		}

    		public void onFinish() {
    			stopSelf(); // countdown is over, stop the service.
    		}
    	};
    }
    
    private void restartTimer () {
    	// must be started with startTimer first.
    	if (t != null) {
    		t.cancel();
    		t.start();
    	}
    }

    /**
     * The ServiceDispatch is defined through IDL
     */
    private final ServiceDispatch.Stub mBinder = new ServiceDispatch.Stub() {
    	private String TAG = "SERVICEDISPATCH";

    	public String encrypt (String clearText)  {
    		restartTimer();
    		String cryptoText = null;
    		try {
    			cryptoText = ch.encrypt (clearText); 
    		} catch (CryptoHelperException e) {
    			Log.e(TAG, e.toString());
    		}  
    		return (cryptoText);
    	}

    	public String decrypt (String cryptoText)  {
    		restartTimer();
    		String clearText = null;
    		try {
    			clearText = ch.decrypt (cryptoText); 
    		} catch (CryptoHelperException e) {
    			Log.e(TAG, e.toString());
    		}  
    		return (clearText);
    	}

    	public void setPassword (String masterKeyIn){
    		startTimer(); //should be initial timer start
			ch = new CryptoHelper(CryptoHelper.EncryptionMedium);
			ch.setPassword(masterKeyIn);
			masterKey = masterKeyIn;
			
			ServiceNotification.setNotification(ServiceDispatchImpl.this);
    	}

		public String getPassword() {
    		restartTimer();
			return masterKey;
		}
		
		public void setTimeoutMinutes (int timeoutMinutesIn){
			timeoutMinutes = timeoutMinutesIn;
			Log.d(TAG,"set timeout to "+timeoutMinutes);
		}
    };

}
