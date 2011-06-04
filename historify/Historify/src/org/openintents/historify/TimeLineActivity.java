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
import org.openintents.historify.uri.Actions;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

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
		mTxtContact = (TextView) findViewById(R.id.timeline_txtContact);

		// read contact from Itent extras.
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
}
