package org.openintents.historify;


import org.openintents.historify.data.adapters.ContactsAdapter;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.uri.Actions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class ContactsActivity extends Activity {
	
	public static final String NAME = "ContactsActivity";
	
	private ContactsAdapter mAdapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.contacts);

		mAdapter = new ContactsAdapter(this, getIntent().hasExtra(Actions.EXTRA_MODE_FAVORITES));

		ListView lstContacts = (ListView) findViewById(R.id.contacts_lstContacts);
		View lstContactsEmptyView = getLayoutInflater().inflate(R.layout.contacts_empty_view,null);
		((ViewGroup)lstContacts.getParent()).addView(lstContactsEmptyView);
		lstContacts.setEmptyView(lstContactsEmptyView);
		
		lstContacts.setAdapter(mAdapter);

		lstContacts
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Contact selected = (Contact) parent
								.getItemAtPosition(position);
						launchTimeLineActivity(selected);
					}
				});

	}

	private void launchTimeLineActivity(Contact selected) {

		String contactLookupKey = String.valueOf(selected.getLookupKey());

		Intent intent = new Intent();
		intent.setAction(Actions.SHOW_TIMELINE);
		intent.putExtra(Actions.EXTRA_CONTACT_LOOKUP_KEY, contactLookupKey);
		startActivity(intent);
	}
}
