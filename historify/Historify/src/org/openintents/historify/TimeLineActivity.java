package org.openintents.historify;

import org.openintents.historify.data.loaders.ContactLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.uri.Actions;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class TimeLineActivity extends Activity {

	private static final String NAME = "TimeLineActivity";
	
	//private ListView mLstTimeLine;
	private TextView mTxtContact;

	private Contact mContact;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.timeline);

		//mLstTimeLine = (ListView) findViewById(R.id.timeline_lstTimeLine);
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
		}
		
	}
}
