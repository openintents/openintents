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

import org.openintens.samples.lendme.data.ContactOperations;
import org.openintens.samples.lendme.data.HistorifyPostHelper;
import org.openintens.samples.lendme.data.Item;
import org.openintens.samples.lendme.data.ItemsAdapter;
import org.openintens.samples.lendme.data.Item.Owner;
import org.openintens.samples.lendme.data.persistence.ItemsProviderHelper;
import org.openintens.samples.lendme.data.persistence.ItemsProviderHelper.ItemsTable;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;

public class ItemsActivity extends Activity {

	public static final String EXTRA_OWNER = "owner";
	private static final int REQUEST_ADD_ITEM = 42;
	private static final int CONTEXT_ITEM_RETURNED = 1;
	private static final int CONTEXT_ITEM_REMINDER = 2;
	
	private ListView mLstItems;
	private Owner mFilterForOwner;
	
	private int editedPosition = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.items);
		mLstItems = (ListView)findViewById(R.id.items_lst);
		
		mFilterForOwner = Owner.parseString(getIntent().getStringExtra(EXTRA_OWNER));
		
		if(mFilterForOwner==null) {
			finish();
		} else {
			
			ItemsAdapter adapter = new ItemsAdapter(this, mFilterForOwner);
			mLstItems.setAdapter(adapter);
			
			mLstItems.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> adapterView, View view,
						int pos, long id) {
					
					if(pos==0) onAddNewItem();
					else onEditItem(pos);
				}
			});			
			
			registerForContextMenu(mLstItems);
		}
	}

	private void onAddNewItem() {
		
		Intent i = new Intent(this, AddItemActivity.class);
		startActivityForResult(i, REQUEST_ADD_ITEM);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if(requestCode == REQUEST_ADD_ITEM) {
			if(resultCode==RESULT_OK) {
				data.putExtra(ItemsTable.OWNER, mFilterForOwner.toString());
				long itemId = ((ItemsAdapter)mLstItems.getAdapter()).insert(data.getExtras());
				
				boolean shouldPost = data.getBooleanExtra(HistorifyPostHelper.PREF_NAME, true);
				HistorifyPostHelper postHelper = HistorifyPostHelper.getInstance(this);
				postHelper.setUserPrefersPosting(this, shouldPost);
				
				if(shouldPost)
					postHelper.postLendingStartEvent(this, data.getExtras(), itemId);
			}
		} else super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	private void onEditItem(int position) {
		editedPosition = position;
		openContextMenu(mLstItems);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		
		int pos;
		
		if(editedPosition!=-1) {
			pos = editedPosition;
			editedPosition = -1;
		} else {
			pos=((AdapterContextMenuInfo)menuInfo).position;
		}
		
		if(pos==0) return;
		else {
			Item item = (Item) mLstItems.getItemAtPosition(pos);
			menu.setHeaderTitle(item.getName());
			
			Uri data = Uri.withAppendedPath(ItemsProviderHelper.CONTENT_URI, String.valueOf(item.getId()));
			Intent i = new Intent(null, data);
			
			menu.add(Menu.NONE, CONTEXT_ITEM_RETURNED, Menu.NONE, R.string.main_returned).setIntent(i);
			
			if(mFilterForOwner!=Owner.Contact)
				menu.add(Menu.NONE, CONTEXT_ITEM_REMINDER, Menu.NONE, R.string.main_reminder).setIntent(i);
		}
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		
		Uri data = item.getIntent().getData();
		Long id = null;
		if(data!=null) {
			id = Long.valueOf(data.getLastPathSegment()); 
		}
		
		if(item.getItemId()==CONTEXT_ITEM_RETURNED) {
			onSetItemReturned(id);
			return true;
		} else if(item.getItemId()==CONTEXT_ITEM_REMINDER) {
			onSendReminder(id);
			return true;
		} else return super.onContextItemSelected(item);
	}

	private void onSendReminder(long itemId) {
		
		final Item item = ((ItemsAdapter)mLstItems.getAdapter()).getItemById(itemId);
		
		final String[] phoneNumbers = ContactOperations.loadContactPhones(getContentResolver(), item.getContactKey());
		if(phoneNumbers.length==0) {
			//no phone for contact
			Toaster.toast(this, R.string.main_msg_nophone);
		} else if(phoneNumbers.length==1){
			//exactly one phone number
			ContactOperations.displaySmsSender(this, phoneNumbers[0],item);
		} else {
			//multiple phone numbers
			//show number selector dialog
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.main_msg_select_phone);
			builder.setItems(phoneNumbers, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ContactOperations.displaySmsSender(ItemsActivity.this, phoneNumbers[which],item);
				}
			});
			builder.show();
		}
		
	}

	private void onSetItemReturned(final long itemId) {		
		
		final Item item = ((ItemsAdapter)mLstItems.getAdapter()).getItemById(itemId);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(item.getName());
		
		View content = getLayoutInflater().inflate(R.layout.return_item_dialog, null);
		final CheckBox chkPost = (CheckBox)content.findViewById(R.id.return_item_dialog_chkPost);
		chkPost.setChecked(HistorifyPostHelper.getInstance(this).userPrefersPosting());
		
		builder.setView(content);
		
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				((ItemsAdapter)mLstItems.getAdapter()).delete(itemId);
				
				boolean shouldPost = chkPost.isChecked();
				HistorifyPostHelper postHelper = HistorifyPostHelper.getInstance(ItemsActivity.this);
				postHelper.setUserPrefersPosting(ItemsActivity.this, shouldPost);
				
				if(shouldPost)
					postHelper.postReturedEvent(ItemsActivity.this, item);
			}
		});
		
		builder.setNegativeButton(R.string.cancel, null);
		
		builder.show();
		
		
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		((ItemsAdapter)mLstItems.getAdapter()).close();
	}

}
