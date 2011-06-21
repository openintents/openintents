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

import org.openintens.samples.lendme.data.Item;
import org.openintens.samples.lendme.data.ItemsAdapter;
import org.openintens.samples.lendme.data.Item.Owner;
import org.openintens.samples.lendme.data.persistence.ItemsProviderHelper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
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
				((ItemsAdapter)mLstItems.getAdapter()).insert(data.getExtras());
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
		
		if(item.getItemId()==CONTEXT_ITEM_RETURNED) {
			onSetItemReturned(Long.valueOf(item.getIntent().getData().getLastPathSegment()));
			return true;
		} else if(item.getItemId()==CONTEXT_ITEM_REMINDER) {
			Toast.makeText(this, "reminder", Toast.LENGTH_SHORT).show();
			return true;
		} else return super.onContextItemSelected(item);
	}

	private void onSetItemReturned(long itemId) {
		
		((ItemsAdapter)mLstItems.getAdapter()).delete(itemId);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		((ItemsAdapter)mLstItems.getAdapter()).close();
	}

}
