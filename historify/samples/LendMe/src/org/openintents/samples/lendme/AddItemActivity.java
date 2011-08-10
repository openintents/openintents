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

package org.openintents.samples.lendme;

import org.openintents.samples.lendme.data.ContactOperations;
import org.openintents.samples.lendme.data.HistorifyPostHelper;
import org.openintents.samples.lendme.data.Item.Owner;
import org.openintents.samples.lendme.data.persistence.ItemsProviderHelper.ItemsTable;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

public class AddItemActivity extends Activity {

	public static final String EXTRA_POST_TO_HISTORIFY = "post";
	
	public static final String EXTRA_MODE = "mode";
	public static final String EXTRA_CONTACT_LOOKUP_KEY = "lookup";
	
	private static final int REQUEST_PICK_A_CONTACT = 42;
	
	private RadioButton radioLend, radioBorrow;
	private Button btnPick;
	private EditText editName, editDescription;
	private CheckBox chkPost;
	private View btnAdd;
	
	private String pickedContactKey;
	private boolean canPostToHistorify;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_item);
		
		radioLend = (RadioButton) findViewById(R.id.add_item_radioLend);
		radioBorrow = (RadioButton) findViewById(R.id.add_item_radioBorrow);
		btnPick = (Button)findViewById(R.id.add_item_btnPick);
		editName = (EditText) findViewById(R.id.add_item_editName);
		editDescription = (EditText)findViewById(R.id.add_item_editDescription);
		chkPost = (CheckBox)findViewById(R.id.add_item_chkPost);
		btnAdd = findViewById(R.id.add_item_btnAdd);
				
		btnAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAdd();
			}
		});
		
		
		HistorifyPostHelper postHelper = HistorifyPostHelper.getInstance(this);
		canPostToHistorify = postHelper.canQuickPost(this);
		chkPost.setVisibility(canPostToHistorify ? View.VISIBLE : View.GONE);
		
		if(canPostToHistorify) {
			boolean shouldPost = postHelper.userPrefersPosting();
			chkPost.setChecked(shouldPost);	
		}
		
		if(getIntent().hasExtra(EXTRA_MODE)) {
			radioBorrow.setEnabled(false);
			radioLend.setEnabled(false);
			Owner owner = Owner.parseString(getIntent().getStringExtra(EXTRA_MODE));
			if(owner == Owner.Me) {
				radioBorrow.setChecked(false);
				radioLend.setChecked(true);
			} else {
				radioBorrow.setChecked(true);
				radioLend.setChecked(false);
			}
		} else {
			radioLend.setChecked(true);
		}
		
		if(getIntent().hasExtra(EXTRA_CONTACT_LOOKUP_KEY)) {
			onContactPicked(Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, getIntent().getStringExtra(EXTRA_CONTACT_LOOKUP_KEY)));
			btnPick.setOnClickListener(null);
		} else {
			btnPick.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onPick();
				}
			});
		}
			
		
	}
	
	private void onContactPicked(Uri lookupUri) {
		
		if(lookupUri!=null) {
			pickedContactKey = lookupUri.getPathSegments().get(2);
			String name = ContactOperations.loadContactName(getContentResolver(), pickedContactKey);
			if(name!=null)
				btnPick.setText(name);
			else {
				pickedContactKey = null;
				btnPick.setText(R.string.add_pick);
			}
				
		}
	}

	private void onPick() {
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
				onContactPicked(lookupUri);
			}
		}
		else super.onActivityResult(requestCode, resultCode, data);

	}
	
	private void onAdd() {
		
		String contactKey = pickedContactKey;
		String itemName = editName.getText().toString().trim();
		String itemDescription = editDescription.getText().toString().trim();
		
		if(contactKey==null) {
			Toaster.toast(this,R.string.add_msg_nocontact);
			return;
		}
		
		if(itemName.length()==0) {
			Toaster.toast(this, R.string.add_msg_mandatory);
			return;
		}
		
		if(itemDescription.length()==0) {
			itemDescription=null;
		}
		
		Intent data = new Intent();
		data.putExtra(ItemsTable.CONTACT_KEY, contactKey);
		data.putExtra(ItemsTable.ITEM_NAME, itemName);
		data.putExtra(ItemsTable.ITEM_DESCRIPTION, itemDescription);
		
		if(canPostToHistorify) {
			boolean shouldPost = chkPost.isChecked();
			HistorifyPostHelper.getInstance(this).setUserPrefersPosting(this, shouldPost);
			data.putExtra(EXTRA_POST_TO_HISTORIFY, shouldPost);
		}
		
		data.putExtra(EXTRA_MODE, radioLend.isChecked() ? Owner.Me.toString() : Owner.Contact.toString());
				
		setResult(RESULT_OK, data);
		finish();
	}
	
}
