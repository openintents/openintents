
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


public class AddGenericAlertActivity extends Activity{


	public static final int STATE_CREATE=1001;
	public static final int STATE_EDIT=1002;
	
	public static final int STATE_SAVE=201;
	public static final int STATE_CANCEL=202;

	private Uri cUri=null;
		
	private int mState;

	private Cursor mCursor;

	public static final String _TAG="AddGenericAlertActivity";

	private TextView mCond1;
	private TextView mCond2;
	private TextView mIntent;
	private TextView mIntentCat;
	private TextView mIntentUri;

	private int Cond1Row=0;
	private int Cond2Row=0;
	private int IntentRow=0;
	private int IntentCatRow=0;
	private int IntentUriRow=0;

	
	public void onCreate(Bundle b){	
		
		super.onCreate(b);

		setContentView(R.layout.alert_add_generic);

		mCond1=(TextView)findViewById(R.id.alert_addgeneric_condition1);
		mCond2=(TextView)findViewById(R.id.alert_addgeneric_condition2);
		mIntent=(TextView)findViewById(R.id.alert_addgeneric_intent);
		mIntentCat=(TextView)findViewById(R.id.alert_addgeneric_intentcategory);
		mIntentUri=(TextView)findViewById(R.id.alert_addgeneric_intenturi);

		Alert.mContentResolver=getContentResolver();

		if (getIntent().getAction().equals(org.openintents.OpenIntents.ADD_GENERIC_ALERT)){
        	mState=STATE_CREATE;
        	//mChannelLink.setText("EHLO CREATOR!");
        }else if (getIntent().getAction().equals(Intent.EDIT_ACTION)){
        	mState=STATE_EDIT;
        	cUri=Uri.parse(b.getString("URI"));

			mCursor=managedQuery(cUri,Alert.Generic.PROJECTION,null,null);
			Cond1Row=mCursor.getColumnIndex(Alert.Generic.CONDITION1);
			Cond2Row=mCursor.getColumnIndex(Alert.Generic.CONDITION2);
			IntentRow=mCursor.getColumnIndex(Alert.Generic.INTENT);
			IntentCatRow=mCursor.getColumnIndex(Alert.Generic.INTENT_CATEGORY);
			IntentUriRow=mCursor.getColumnIndex(Alert.Generic.INTENT_URI);
			if (mCursor.count()>0)
			{
				mCursor.first();

				mCond1.setText(mCursor.getString(Cond1Row));
				mCond2.setText(mCursor.getString(Cond2Row));
				mIntent.setText(mCursor.getString(IntentRow));
				mIntentCat.setText(mCursor.getString(IntentCatRow));
				mIntentUri.setText(mCursor.getString(IntentUriRow));

			}

		}
		Log.d(_TAG,"state>>"+mState);

	}




	private void createDataSet(){
		ContentValues cv=new ContentValues();
		Log.d(_TAG,"creating dataset now");
		cv.put(Alert.Generic.CONDITION1,mCond1.getText().toString());
		cv.put(Alert.Generic.CONDITION2,mCond2.getText().toString());
		cv.put(Alert.Generic.INTENT,mIntent.getText().toString());
		cv.put(Alert.Generic.INTENT_CATEGORY,mIntentCat.getText().toString());
		cv.put(Alert.Generic.INTENT_URI,mIntentUri.getText().toString());

		cUri = Alert.insert(Alert.Generic.CONTENT_URI,cv);
		mCursor=managedQuery(cUri,Alert.Generic.PROJECTION,null,null);


	}



	private void saveDataSet(){
		Log.d(_TAG,"creating dataset now");
		mCursor.updateString(Cond1Row,mCond1.getText().toString());
		mCursor.updateString(Cond2Row,mCond2.getText().toString());
		mCursor.updateString(IntentRow,mIntent.getText().toString());
		mCursor.updateString(IntentCatRow,mIntentCat.getText().toString());
		mCursor.updateString(IntentUriRow,mIntentUri.getText().toString());
		mCursor.commitUpdates();

	}



	public void onPause(){
		super.onPause();
		if (mState==STATE_CREATE)
		{
			createDataSet();
		}else if (mState==STATE_EDIT)
		{
			saveDataSet();
		}
		mCursor.close();
	}



}/*eoc*/