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

package org.openintens.samples.lendme;

import org.openintens.samples.lendme.data.persistence.ItemsProviderHelper.ItemsTable;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AddItemActivity extends Activity {

	private static final int REQUEST_PICK_A_CONTACT = 42;
	
	private Button btnPick;
	private EditText editName, editDescription;
	private View btnAdd;
	
	private String pickedContactKey;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_item);
		
		btnPick = (Button)findViewById(R.id.add_item_btnPick);
		editName = (EditText) findViewById(R.id.add_item_editName);
		editDescription = (EditText)findViewById(R.id.add_item_editDescription);
		btnAdd = findViewById(R.id.add_item_btnAdd);
		
		btnPick.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onPick();
			}
		});
		
		btnAdd.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onAdd();
			}
		});
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
				if(lookupUri!=null) {
					pickedContactKey = lookupUri.getPathSegments().get(2);
					String name = loadContactName(pickedContactKey);
					if(name!=null)
						btnPick.setText(loadContactName(pickedContactKey));
					else {
						pickedContactKey = null;
						btnPick.setText(R.string.add_pick);
					}
						
				}
			}
		}
		else super.onActivityResult(requestCode, resultCode, data);

	}
	
	private String loadContactName(String contactKey) {
		
		Uri lookupUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI,contactKey);
		String name = null;
		
		Cursor c = getContentResolver().query(lookupUri, new String[] {Contacts.DISPLAY_NAME}, null, null,null);
		if(c.moveToFirst()) {
			name = c.getString(0);
		}
		c.close();
		
		return name;
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
		
		setResult(RESULT_OK, data);
		finish();
	}
	
}
