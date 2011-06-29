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

package org.openintents.historify.ui.fragments;

import org.openintents.historify.R;
import org.openintents.historify.data.adapters.TimeLineAdapter;
import org.openintents.historify.data.loaders.ContactIconHelper;
import org.openintents.historify.data.loaders.ContactLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.Event;
import org.openintents.historify.uri.Actions;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
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
	private ImageView mImgContactIcon;

	/** Called to have the fragment instantiate its user interface view. */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		ViewGroup layout = (ViewGroup) inflater.inflate(
				R.layout.fragment_timeline, container, false);

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
		mImgContactIcon = (ImageView) layout.findViewById(R.id.timeline_imgContactIcon);
		
		return layout;
	}

	/**
	 * Called when the fragment's activity has been created and this fragment's
	 * view hierarchy instantiated.
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		String contactLookupKey = getContactLookupKey();
		
		if (contactLookupKey == null) {
			Log.w(NAME, "Contact lookupkey not provided.");
		} else {
			mContact = new ContactLoader().loadFromLookupKey(getActivity(),
					contactLookupKey);
		}
		
		if(mContact!=null) {
			mTxtContact.setText(mContact.getName());
			mAdapter = new TimeLineAdapter(getActivity(), mContact);
			mLstTimeLine.setAdapter(mAdapter);
			
			Drawable icon = ContactIconHelper.getIconDrawable(getActivity(), mContact.getLookupKey());
			if(icon==null) mImgContactIcon.setImageResource(R.drawable.contact_default_large);
			else mImgContactIcon.setImageDrawable(icon);
			
		} else {
			mTxtContact.setText("");
		}

	}

	private String getContactLookupKey() {
		return getArguments().getString(Actions.EXTRA_CONTACT_LOOKUP_KEY);
	}

	private void onEventClicked(Event event) {

		if (event.getSource().getEventIntent() != null) {
			Intent i = new Intent();
			i.setAction(event.getSource().getEventIntent());
			i.putExtra(Actions.EXTRA_EVENT_ID, event.getId());
			i.putExtra(Actions.EXTRA_EVENT_KEY, event.getEventKey());
			
			if(!event.getSource().isInternal())
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
			
			startActivity(i);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		//release merged cursors
		mAdapter.releaseCursors();
	}
}
