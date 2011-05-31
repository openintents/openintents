package org.openintents.historify;

import java.util.ArrayList;

import org.openintents.historify.data.adapters.EventsAdapter;
import org.openintents.historify.data.loaders.ContactLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.providers.Events;
import org.openintents.historify.data.providers.internal.Messaging;
import org.openintents.historify.data.providers.internal.Telephony;
import org.openintents.historify.uri.Actions;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class TimeLineActivity extends Activity {

	private static final String NAME = "TimeLineActivity";
	
	private ListView mLstTimeLine;
	private TextView mTxtContact;

	private Contact mContact;
	private EventsAdapter mAdapter;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);

		mLstTimeLine = (ListView) findViewById(R.id.timeline_lstTimeLine);
		mTxtContact = (TextView) findViewById(R.id.timeline_txtContact);
		
		String contactLookupKey = getIntent().getStringExtra(Actions.EXTRA_CONTACT_LOOKUP_KEY);
		if(contactLookupKey==null) {
			Log.w(NAME, "Contact lookupkey not provided.");
			finish();
		} else {
			load(contactLookupKey);	
		}
		
	}

	private void load(String contactLookupKey) {

		mContact = new ContactLoader().loadFromLookupKey(this, contactLookupKey);
		
		if(mContact == null) finish();
		else {
			mTxtContact.setText(mContact.getName());
			
			mAdapter = new EventsAdapter(this, mContact);
			mLstTimeLine.setAdapter(mAdapter);
		}
		
	}
}
