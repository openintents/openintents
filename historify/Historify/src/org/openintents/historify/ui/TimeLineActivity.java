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

package org.openintents.historify.ui;

import org.openintents.historify.R;
import org.openintents.historify.data.loaders.ContactLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.ui.fragments.ContactsListFragment;
import org.openintents.historify.ui.fragments.TimeLineFragment;
import org.openintents.historify.ui.views.ActionBar;
import org.openintents.historify.ui.views.ActionBar.Action;
import org.openintents.historify.uri.Actions;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;

/**
 * 
 * Displays Historify timeline.
 * 
 * @author berke.andras
 * 
 */
public class TimeLineActivity extends FragmentActivity {

	public static final String N = "TimeLineActivity";
	
	private ActionBar actionBar;
	private TimeLineFragment timeLineFragment;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_timeline);
		
		String contactLookupKey = getIntent().getStringExtra(Actions.EXTRA_CONTACT_LOOKUP_KEY);
		
		if(contactLookupKey==null) {
			Log.e(N, "Contact lookupkey not provided.");
			finish();
		} else {
			
			Contact contact = new ContactLoader().loadFromLookupKey(this, contactLookupKey);
			if(contact!=null) {
				setFragmentParameters(contact);
				setupActionBar(contact);	
			} else {
				Log.e(N, "Contact could not be loaded.");
				finish();
			}
		}
	}

	private void setFragmentParameters(Contact contact) {
		timeLineFragment = (TimeLineFragment)getSupportFragmentManager().findFragmentById(R.id.timeline_fragment);
		timeLineFragment.setContact(contact);
	}

	private void setupActionBar(Contact contact) {
		
		actionBar = new ActionBar((ViewGroup) findViewById(R.id.actionbar), contact.getName());
		actionBar.setup();

	}

}
