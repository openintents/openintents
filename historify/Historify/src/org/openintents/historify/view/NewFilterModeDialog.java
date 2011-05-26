package org.openintents.historify.view;

import java.util.HashSet;
import java.util.List;

import org.openintents.historify.R;
import org.openintents.historify.data.adapters.ContactsAdapter;
import org.openintents.historify.data.adapters.ContactsDialogAdapter;
import org.openintents.historify.data.model.Contact;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class NewFilterModeDialog extends Dialog {

	private Contact mSelectedContact;

	public NewFilterModeDialog(Activity context, List<Contact> alreadyAdded) {
		super(context);
		
		//read already added contacts' lookup keys
		HashSet<String> alreadyAddedFilters = new HashSet<String>();
		for(Contact c : alreadyAdded) {
			alreadyAddedFilters.add(c.getLookupKey());
		}

		
		//init view
		setTitle(context.getString(R.string.sources_filter_mode_new_dialog));
		setContentView(R.layout.contacts);
				
		String alreadyAddedMessage = context.getString(R.string.sources_filter_mode_already_added);
		ContactsDialogAdapter adapter = new ContactsDialogAdapter(context, alreadyAddedFilters, alreadyAddedMessage);
		
		
		ListView lstContacts = (ListView) findViewById(R.id.contacts_lstContacts);
		lstContacts.setAdapter(adapter);
		
		View lstContactsEmptyView = getLayoutInflater().inflate(R.layout.contacts_empty_view,null);
		((ViewGroup)lstContacts.getParent()).addView(lstContactsEmptyView);
		lstContacts.setEmptyView(lstContactsEmptyView);

		lstContacts
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Contact selected = (Contact) parent
								.getItemAtPosition(position);
						onContactSelected(selected);
					}
				});

	}

	private void onContactSelected(Contact selected) {
		setSelectedContact(selected);
		dismiss();
	}

	private void setSelectedContact(Contact contact) {
		mSelectedContact = contact;
	}

	public Contact getSelectedContact() {
		return mSelectedContact;
	}

}
