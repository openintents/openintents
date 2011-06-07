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

package org.openintents.historify.fragments;

import org.openintents.historify.R;
import org.openintents.historify.data.adapters.TimeLineAdapter;
import org.openintents.historify.data.loaders.ContactLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.Event;
import org.openintents.historify.uri.Actions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
public class TimeLineFragment extends Fragment {

	private static final String NAME = "TimeLineFragment";

	// model
	private Contact mContact;
	private TimeLineAdapter mAdapter;

	// views
	private ListView mLstTimeLine;
	private TextView mTxtContact;

	/**
	 * Constructor. Initialize parameters.
	 * 
	 * @param context
	 *            Activity context.
	 * @param contactLookupKey
	 *            The lookupkey of the contact whom timeline has to displayed.
	 */
	public TimeLineFragment(Activity context, String contactLookupKey) {

		if (contactLookupKey == null) {
			Log.w(NAME, "Contact lookupkey not provided.");
		} else {
			mContact = new ContactLoader().loadFromLookupKey(context,
					contactLookupKey);
		}
	}

	/** Called to have the fragment instantiate its user interface view. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup layout = (ViewGroup) inflater.inflate(
				R.layout.timeline_fragment, container, false);

		// init list
		mLstTimeLine = (ListView) layout
				.findViewById(R.id.timeline_lstTimeLine);
		mLstTimeLine.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				onEventClicked((Event) parent.getItemAtPosition(position));
			}
		});

		// init list empty view
		View lstContactsEmptyView = inflater.inflate(R.layout.list_empty_view,
				null);
		((TextView) lstContactsEmptyView)
				.setText(R.string.timeline_no_events);
		((ViewGroup) mLstTimeLine.getParent()).addView(lstContactsEmptyView);
		mLstTimeLine.setEmptyView(lstContactsEmptyView);

		
		mTxtContact = (TextView) layout.findViewById(R.id.timeline_txtContact);

		return layout;
	}

	/**
	 * Called when the fragment's activity has been created and this fragment's
	 * view hierarchy instantiated.
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if(mContact!=null) {
			mTxtContact.setText(mContact.getName());
			mAdapter = new TimeLineAdapter(getActivity(), mContact);
			mLstTimeLine.setAdapter(mAdapter);			
		} else {
			mTxtContact.setText("");
		}

	}


	private void onEventClicked(Event event) {

		if (event.getSource().getEventIntent() != null) {
			Intent i = new Intent();
			i.setAction(event.getSource().getEventIntent());
			i.putExtra(Actions.EXTRA_EVENT_ID, event.getId());
			i.putExtra(Actions.EXTRA_EVENT_KEY, event.getEventKey());
			startActivity(i);
		}
	}
}
