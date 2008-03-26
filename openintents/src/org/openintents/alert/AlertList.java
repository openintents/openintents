
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
package org.openintents.alert;

import org.openintents.provider.Alert;
import org.openintents.R;
import org.openintents.R.*;

import android.app.ListActivity;
import android.os.Bundle;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.content.ContentValues;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.view.View.OnClickListener;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class AlertList extends ListActivity{

	private static final String _TAG="AlertList";

	private Cursor mCursor;

	@Override
	protected void onFreeze(Bundle icicle){
		stopManagingCursor(mCursor);
		Log.d(_TAG, "onFreeze: entering");
		
	}  

	@Override
	protected void onResume(){
		super.onResume();
		Log.d(_TAG,"onResume: entering");
		init();
	}

    @Override
    public void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
        
        
		init();
    }


	public void init(){

		Alert.mContentResolver=getContentResolver();

		Log.d(_TAG,"contetnresovler is>>"+Alert.mContentResolver);


		//mCursor=managedQuery(Alert.Generic.CONTENT_URI,Alert.Generic.PROJECTION,null,null);
		mCursor=Alert.mContentResolver.query(Alert.Generic.CONTENT_URI,Alert.Generic.PROJECTION,null,null,null);
		if (mCursor.count()==0)
		{
			Log.e(_TAG,"Cursor was empty");

		}
		SimpleCursorAdapter sca=new SimpleCursorAdapter(
			this,
			R.layout.alert_list,
			mCursor,
			Alert.Generic.PROJECTION,
			new int[]{R.id.alert_id,R.id.alert_count,R.id.alert_cond1,R.id.alert_cond1,R.id.alert_cond1}
		);

		this.setListAdapter(sca);




	}



}/*eoc*/