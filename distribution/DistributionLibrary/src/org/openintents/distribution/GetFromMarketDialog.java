/* 
 * Copyright (C) 2007-2011 OpenIntents.org
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

package org.openintents.distribution;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

/**
 * @version 2009-02-04
 * @author Peli
 *
 */
public class GetFromMarketDialog extends AlertDialog implements OnClickListener {
	private static final String TAG = "StartSaveActivity";

    Context mContext;
    int mMarketUri;
    int mDeveloperUri;
    
    public GetFromMarketDialog(Context context, int message, int buttontext, int market_uri, int developer_uri) {
        super(context);
        mContext = context;
        mMarketUri = market_uri;
        mDeveloperUri = developer_uri;

        //setTitle(context.getText(R.string.menu_edit_tags));
        setMessage(mContext.getText(message));
    	setButton(mContext.getText(buttontext), this);
        
    }

	public void onClick(DialogInterface dialog, int which) {
    	if (which == BUTTON1) {
    		Uri uri = Uri.parse(mContext.getString(mMarketUri));
    		
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(uri);
			
			uri= Uri.parse(mContext.getString(mDeveloperUri));
			Intent intent2 = new Intent(Intent.ACTION_VIEW);
			intent2.setData(uri);
			startSaveActivity(mContext, intent, intent2);
    	}
		
	}
	
	/**
	 * Start an activity but prompt a toast if activity is not found
	 * (instead of crashing).
	 * 
	 * @param context
	 * @param intent
	 * @param intent2 Alternative intent to call, if the first is not reachable
	 */
	public static void startSaveActivity(Context context, Intent intent, Intent intent2) {
		try {
			context.startActivity(intent);
		} catch (ActivityNotFoundException e) {
			Log.e(TAG, "Error starting activity.", e);
			try {
				context.startActivity(intent2);
			} catch (ActivityNotFoundException e2) {
				Toast.makeText(context,
						RD.string.update_error,
						Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Error starting second activity.", e2);
			}
		}
	}
}
