package org.openintents.alert;
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

import android.os.Bundle;
import android.content.IntentReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.util.Log;
import android.content.SharedPreferences;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.content.ContentValues;


import org.openintents.provider.Alert;



public class LocationAlertDispatcher extends IntentReceiver{

	private Cursor mCursor;
	private int cond1Row=0;
	private int cond2Row=0;
	private int intentRow=0;
	private int intentCatRow=0;
	private int intentUriRow=0;
	private int typeRow=0;

	public static final String _TAG="LocationAlertDispatcher";

	public void onReceiveIntent(Context context,Intent intent){
		Log.d(_TAG,"Received Intent>>"+intent.getAction()+"<<");
		String geo="";
		Intent i=null;
		Alert.init(context);
		Bundle b=intent.getExtras();
		geo=b.getString(Alert.Location.POSITION);
		mCursor=Alert.mContentResolver.query(
				Alert.Location.CONTENT_URI,
				Alert.Location.PROJECTION,
				null,null,null);
		if (mCursor!=null && mCursor.count()>0)
		{
			
			cond1Row=mCursor.getColumnIndex(Alert.Generic.CONDITION1);
			cond2Row=mCursor.getColumnIndex(Alert.Generic.CONDITION2);
			intentRow=mCursor.getColumnIndex(Alert.Generic.INTENT);
			intentCatRow=mCursor.getColumnIndex(Alert.Generic.INTENT_CATEGORY);
			intentUriRow=mCursor.getColumnIndex(Alert.Generic.INTENT_URI);
			typeRow=mCursor.getColumnIndex(Alert.Generic.TYPE);

			mCursor.first();
			while (!mCursor.isAfterLast())
			{
				i=new Intent();
				try
				{ //TODO: INtent MIME TYPE
					i.setAction(mCursor.getString(intentRow));
					i.addCategory(mCursor.getString(intentCatRow));
					i.setData(Uri.parse(mCursor.getString(intentUriRow)));		
					context.broadcastIntent(i);
				}
				catch (Exception e)
				{
					Log.e(_TAG,"coulndt launch intent, reason>>"+e.getMessage());
				}
				mCursor.next();
			}

			mCursor.close();
		}
	}
	


}/**/