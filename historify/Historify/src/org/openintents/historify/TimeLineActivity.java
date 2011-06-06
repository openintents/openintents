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

package org.openintents.historify;

import org.openintents.historify.data.adapters.TimeLineAdapter;
import org.openintents.historify.data.loaders.ContactLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.Event;
import org.openintents.historify.data.providers.Events;
import org.openintents.historify.uri.Actions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * 
 * Displays Historify timeline.
 * 
 * @author berke.andras
 * 
 */
public class TimeLineActivity extends Activity {

	private static final String NAME = "TimeLineActivity";

	// model
	private Contact mContact;
	private TimeLineAdapter mAdapter;

	// views
	private ListView mLstTimeLine;
	private TextView mTxtContact;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);

		// init view
		mLstTimeLine = (ListView) findViewById(R.id.timeline_lstTimeLine);
		mLstTimeLine.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent,
					View view, int position, long id) {
				onEventClicked((Event)parent.getItemAtPosition(position));
			}
		});
		
		mTxtContact = (TextView) findViewById(R.id.timeline_txtContact);

		// read contact from Intent extras.
		String contactLookupKey = getIntent().getStringExtra(
				Actions.EXTRA_CONTACT_LOOKUP_KEY);
		if (contactLookupKey == null) {
			Log.w(NAME, "Contact lookupkey not provided.");
			finish();
		} else {
			load(contactLookupKey);
		}

	}

	/** Load data to be displayed */
	private void load(String contactLookupKey) {

		mContact = new ContactLoader()
				.loadFromLookupKey(this, contactLookupKey);

		if (mContact == null)
			finish();
		else {
			mTxtContact.setText(mContact.getName());

			mAdapter = new TimeLineAdapter(this, mContact);
			mLstTimeLine.setAdapter(mAdapter);
		}

	}
	
	private void onEventClicked(Event event) {
		
		if(event.getSource().getEventIntent()!=null) {			
			Intent i = new Intent();
			i.setAction(event.getSource().getEventIntent());
			i.putExtra(Actions.EXTRA_EVENT_ID, event.getId());
			i.putExtra(Actions.EXTRA_EVENT_KEY, event.getEventKey());
			startActivity(i);
		}
	}

}
