
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



	private static final int MENU_GENERIC_CREATE=101;
	private static final int MENU_GENERIC_EDIT=102;
	private static final int MENU_DELETE=103;


	@Override
	protected void onFreeze(Bundle icicle){
		stopManagingCursor(mCursor);
		Log.d(_TAG, "onFreeze: entering");
		
	}  

	@Override
	protected void onResume(){
		super.onResume();
		Log.d(_TAG,"onResume: entering");
		//init();
	}

    @Override
    public void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
        
		setContentView(R.layout.alert_list);
   
		init();
    }


	public void init(){
	
		Alert.mContentResolver=getContentResolver();

//		Log.d(_TAG,"contetnresovler is>>"+Alert.mContentResolver.toString());
//		Log.d(_TAG,"uri is >>"+Alert.Generic.CONTENT_URI+"<<");
//		Log.d(_TAG,"projection is >>"+Alert.Generic.PROJECTION.toString()+"<<");
		//Log.d(_TAG,"strange data path >>"+Alert.mContentResolver.getDataFilePath(Alert.Generic.CONTENT_URI)+"<<");

//		AlertProvider.test(Alert.Generic.CONTENT_URI);
//		AlertProvider.test(Uri.parse("content://org.openintents.alert"));
//		AlertProvider.test(Uri.parse("content://org.openintents.alert/"));
//		AlertProvider.test(Alert.Location.CONTENT_URI);
		
		mCursor=managedQuery(Alert.Generic.CONTENT_URI,Alert.Generic.PROJECTION,null,null);
		//new AlertProvider().query(Alert.Generic.CONTENT_URI,Alert.Generic.PROJECTION,null,null,null);
		//mCursor=Alert.mContentResolver.query(Alert.Generic.CONTENT_URI,Alert.Generic.PROJECTION,null,null,null);
		Log.d(_TAG,"cursor is now>>"+mCursor+"<<");

		if (mCursor.count()==0)
		{
			Log.e(_TAG,"Cursor was empty");

		}
		SimpleCursorAdapter sca=new SimpleCursorAdapter(
			this,
			R.layout.alert_list_row,
			mCursor,
			Alert.Generic.PROJECTION,
			new int[]{
				R.id.alert_id,
				R.id.alert_count,
				R.id.alert_cond1,
				R.id.alert_cond2,
				R.id.alert_type,
				R.id.alert_rule,
				R.id.alert_nature,
				R.id.alert_active,
				R.id.alert_onboot,
				R.id.alert_intent,
				R.id.alert_intentcat,
				R.id.alert_intenturi
				}
		);

		this.setListAdapter(sca);




	}


	@Override
	public boolean onOptionsItemSelected(Item item){
		Log.v(_TAG,"onOptionsItemSelected: item.id>>"+item.getId()+"<<");
		int iID=item.getId();
		if (iID==MENU_GENERIC_CREATE)
		{
			menuCreate();
		}else if (iID==MENU_GENERIC_EDIT)
		{
			menuEdit();
		}else if (iID==MENU_DELETE)
		{
			menuDelete();
		}

		return super.onOptionsItemSelected(item);
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		boolean result= super.onCreateOptionsMenu(menu);
		
		menu.add(0,MENU_GENERIC_CREATE,"Add Generic",R.drawable.settings001a);
		menu.add(0,MENU_GENERIC_EDIT,"Edit Generic",R.drawable.settings001a);
		menu.add(0,MENU_DELETE,"Delete Generic",R.drawable.settings001a);
		//menu.add(0,MENU_SERVICESETTINGS, "ServiceSettings",R.drawable.settings001a);
		
		return result;
		
	}


	private void menuCreate(){

		Intent intent = new Intent();
		intent.setAction(org.openintents.OpenIntents.ADD_GENERIC_ALERT);
		intent.addCategory(Intent.DEFAULT_CATEGORY);
		//intent.putExtras(b);
		startActivity(intent);		
	}
	private void menuEdit(){
		Intent intent = new Intent();
		intent.setAction(org.openintents.OpenIntents.EDIT_GENERIC_ALERT);
		intent.addCategory(Intent.DEFAULT_CATEGORY);
		//intent.putExtras(b);
		startActivity(intent);		
	
	}
	private void menuDelete(){
		long i=0;
		int res=0;
		
		i=getSelectedItemId();
		Uri u=ContentUris.withAppendedId(Alert.Generic.CONTENT_URI, i); // ??? _id);
		res=Alert.delete(u,null,null);


	
	}


}/*eoc*/