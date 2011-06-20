/* 
 * Copyright (C) 2011 OpenIntents.org
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

package org.openintents.samples.historify;

import org.openintents.historify.data.model.EventData;
import org.openintents.historify.data.providers.Events.Originator;
import org.openintents.historify.services.bridge.HistorifyBridge;
import org.openintents.historify.services.bridge.HistorifyBridge.QuickPostContext;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	public static final int REQUEST_PICK_A_CONTACT = 42;
	
	private HistorifyBridge mBridge;
	private String pickedContactKey;
	
	private TextView txtPick;
	private Button btnPick;
	
	private TextView txtPost1, txtPost2;
	private Button btnPost1, btnPost2;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        initView();
        initBridge();
    }

	private void initView() {
		
		txtPick = (TextView)findViewById(R.id.main_txtPick);
		btnPick = (Button)findViewById(R.id.main_btnPick);
		
		btnPick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				launchContactPicker();
			}
		});
		
		txtPost1 = (TextView)findViewById(R.id.main_txtPost1);
		txtPost2 = (TextView)findViewById(R.id.main_txtPost2);
		btnPost1 = (Button)findViewById(R.id.main_btnPost1);
		btnPost2 = (Button)findViewById(R.id.main_btnPost2);
		
		btnPost1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quickPost("event1",txtPost1.getText().toString(),Originator.user);
			}
		});
		
		btnPost2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				quickPost("event2", txtPost2.getText().toString(),Originator.contact);
			}
		});
	}

	private void launchContactPicker() {
		
		Intent i = new Intent();
		i.setAction(Intent.ACTION_PICK);
		i.setData(Contacts.CONTENT_URI);
		
		startActivityForResult(i,REQUEST_PICK_A_CONTACT);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		if(requestCode==REQUEST_PICK_A_CONTACT) {
			if(resultCode==RESULT_OK) {
				Uri contactUri = data.getData();
				Uri lookupUri = Contacts.getLookupUri(getContentResolver(), contactUri);
				if(lookupUri!=null) {
					pickedContactKey = lookupUri.getPathSegments().get(2);
					txtPick.setText(lookupUri.toString());
				}
			}
		}
		else super.onActivityResult(requestCode, resultCode, data);
	}
	
	/**
	 * Initializing the HistorifyBridge for QuickPost mode.
	 */
	private void initBridge() {
		
		mBridge = new HistorifyBridge(R.drawable.icon);
		
		QuickPostContext quickPostContext = new QuickPostContext(
				"HelloQuickPost", "Test source for QuickPosting.", null,0);
        mBridge.setQuickPostContext(quickPostContext);
	}
	
	/**
	 * Posting an event through the Bridge.
	 */
	private void quickPost(String eventKey, String eventMessage, Originator originator) {
		
		if(pickedContactKey==null) {
			Toast.makeText(this, "Pick a contact first!", Toast.LENGTH_SHORT).show();
			return;
		}
		
		EventData eventData = new EventData(eventKey, pickedContactKey, System.currentTimeMillis(), eventMessage, originator);
		mBridge.quickPost(this, eventData);
		
		Toast.makeText(this, "Posted event: "+eventMessage, Toast.LENGTH_SHORT).show();
	}
}