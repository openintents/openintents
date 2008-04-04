
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
import android.widget.Spinner;
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
	private Spinner	 mType;

	private int cond1Row=0;
	private int cond2Row=0;
	private int intentRow=0;
	private int intentCatRow=0;
	private int intentUriRow=0;
	private int typeRow=0;

	
	public void onCreate(Bundle b){	
		
		super.onCreate(b);

		setContentView(R.layout.alert_add_generic);

		mCond1=(TextView)findViewById(R.id.alert_addgeneric_condition1);
		mCond2=(TextView)findViewById(R.id.alert_addgeneric_condition2);
		mIntent=(TextView)findViewById(R.id.alert_addgeneric_intent);
		mIntentCat=(TextView)findViewById(R.id.alert_addgeneric_intentcategory);
		mIntentUri=(TextView)findViewById(R.id.alert_addgeneric_intenturi);

		mType=(Spinner)findViewById(R.id.alert_addgeneric_type);
		Alert.init(this);

		ArrayAdapter ad= new ArrayAdapter(
						this,
						android.R.layout.simple_spinner_item,
						new String[] {Alert.TYPE_GENERIC,Alert.TYPE_LOCATION,Alert.TYPE_DATE_TIME}
						);
		ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mType.setAdapter(ad);
		

		if (getIntent().getAction().equals(org.openintents.OpenIntents.ADD_GENERIC_ALERT)){
        	mState=STATE_CREATE;
			mType.setSelection(1);
        	//mChannelLink.setText("EHLO CREATOR!");
        }else if (getIntent().getAction().equals(Intent.EDIT_ACTION)){
        	mState=STATE_EDIT;
        	cUri=Uri.parse(b.getString("URI"));

			mCursor=managedQuery(cUri,Alert.Generic.PROJECTION,null,null);
			cond1Row=mCursor.getColumnIndex(Alert.Generic.CONDITION1);
			cond2Row=mCursor.getColumnIndex(Alert.Generic.CONDITION2);
			intentRow=mCursor.getColumnIndex(Alert.Generic.INTENT);
			intentCatRow=mCursor.getColumnIndex(Alert.Generic.INTENT_CATEGORY);
			intentUriRow=mCursor.getColumnIndex(Alert.Generic.INTENT_URI);
			typeRow=mCursor.getColumnIndex(Alert.Generic.TYPE);
			if (mCursor.count()>0)
			{
				mCursor.first();

				mCond1.setText(mCursor.getString(cond1Row));
				mCond2.setText(mCursor.getString(cond2Row));
				mIntent.setText(mCursor.getString(intentRow));
				mIntentCat.setText(mCursor.getString(intentCatRow));
				mIntentUri.setText(mCursor.getString(intentUriRow));
				//TODO: something usefull here. mType.setSelectedItem

			}

		}
		Log.d(_TAG,"state>>"+mState);

	}




	private void createDataSet(){
		ContentValues cv=new ContentValues();
		Uri typedUri=Alert.Generic.CONTENT_URI;
		String sType=(String)mType.getSelectedItem();
		if (sType.equals(Alert.TYPE_LOCATION))
		{
			typedUri=Alert.Location.CONTENT_URI;
		}else if (sType.equals(Alert.TYPE_DATE_TIME))
		{
			typedUri=Alert.DateTime.CONTENT_URI;
		}
		Log.d(_TAG,"creating dataset now");
		cv.put(Alert.Generic.CONDITION1,mCond1.getText().toString());
		cv.put(Alert.Generic.CONDITION2,mCond2.getText().toString());
		cv.put(Alert.Generic.INTENT,mIntent.getText().toString());
		cv.put(Alert.Generic.INTENT_CATEGORY,mIntentCat.getText().toString());
		cv.put(Alert.Generic.INTENT_URI,mIntentUri.getText().toString());
		cv.put(Alert.Generic.TYPE,sType);

		cUri = Alert.insert(typedUri,cv);
		mCursor=managedQuery(cUri,Alert.Generic.PROJECTION,null,null);


	}



	private void saveDataSet(){
		Log.d(_TAG,"creating dataset now");
		mCursor.updateString(cond1Row,mCond1.getText().toString());
		mCursor.updateString(cond2Row,mCond2.getText().toString());
		mCursor.updateString(intentRow,mIntent.getText().toString());
		mCursor.updateString(intentCatRow,mIntentCat.getText().toString());
		mCursor.updateString(intentUriRow,mIntentUri.getText().toString());
		mCursor.updateString(typeRow,((String)mType.getSelectedItem()));
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
		if (mCursor!=null)
		{
			mCursor.close();
		}
		
	}



}/*eoc*/