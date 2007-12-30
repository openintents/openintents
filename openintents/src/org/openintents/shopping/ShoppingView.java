/* 
 * Copyright (C) 2007 OpenIntents.org
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

package org.openintents.shopping;

import java.util.ArrayList;

import org.openintents.R;
import org.openintents.provider.Shopping.Items;
import org.openintents.provider.Shopping.Lists;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;

public class ShoppingView extends Activity {

	private static final String TAG = "ShoppingProvider";

	private Spinner mSpinner_ListFilter;
	private ListView mList_Items;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		//setTheme(android.R.style.Theme_White);
		//setTheme(android.R.style.Theme_Dialog);
		//setTheme(android.R.style.Theme_Dark);
		//setTheme(android.R.style.Theme_Black);
		setContentView(R.layout.shopping);

		mSpinner_ListFilter = (Spinner) findViewById(R.id.spinner_listfilter);
		/*
		mSpinner_ListFilter.setOnItemSelectedListener(new OnItemSelectedListener() {
			
		});
        */
		
		Button button = (Button) findViewById(R.id.button_add_item);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText edittext = (EditText) findViewById(R.id.edittext_add_item);
				
				insertItem(edittext.getText().toString());
			}
		});
		
		mList_Items = (ListView) findViewById(R.id.list_items);
		
		
		fillListFilter();
		fillItems();
	}
	
	protected void insertItem(String item) {
		ContentValues values = new ContentValues(1);
		values.put(Items.NAME, item);
		try {
			getContentResolver().insert(Items.CONTENT_URI, values);
		} catch (Exception e) {
			Log.i(TAG, "insert item failed", e);
			return;
		}
		
	}
	private void fillListFilter() {
		// Get a cursor with all lists
		Cursor c = getContentResolver().query(Lists.CONTENT_URI, 
				new String[] { Lists._ID, Lists.IMAGE, Lists.NAME }, 
				null, null, Lists.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		if (c == null) {
			Log.e(TAG, "missing shopping provider");
			mSpinner_ListFilter.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no shopping provider" }));
			return;
		}
		
		if (c.count() < 1) {
			// We have to create default shopping list:
			ContentValues values = new ContentValues(1);
			
			// TODO: Put the following string into resource my_shopping_list
			values.put(Lists.NAME, "My shopping list");
			
			try {
				getContentResolver().insert(Lists.CONTENT_URI, values);
			} catch (Exception e) {
				Log.i(TAG, "insert failed", e);
				return;
			}
			
			// The insertion should have worked, so let us call ourselves
			// to try filling the list again:
			fillListFilter();
			return;
		}

		ArrayList<String> list = new ArrayList<String>();
		// TODO: Create summary of all lists
		// list.add(ALL);
		while (c.next()) {
			list.add(c.getString(2));
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		mSpinner_ListFilter.setAdapter(adapter);
		
	}


	private void fillItems() {
		Log.i(TAG, "Starting fillItems()");
		
		// Get a cursor for all items
		Cursor c = getContentResolver().query(
				Items.CONTENT_URI,
				new String[] { Items._ID, Items.IMAGE, Items.NAME},
				null, null,
				Items.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		if (c == null) {
			Log.e(TAG, "missing shopping provider");
			mList_Items.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no shopping provider" }));
			return;
		}
		
		ListAdapter adapter = new SimpleCursorAdapter(this,
				// Use a template that displays a text view
				R.layout.shopping_item_row,
				// Give the cursor to the list adapter
				c,
				// Map the IMAGE and NAME to...
				new String[] { Items.IMAGE, Items.NAME },
				// the view defined in the XML template
				new int[] { R.id.image_URI, R.id.name });
		mList_Items.setAdapter(adapter);
		/*
		mList_Items.setAdapter(new ArrayAdapter(this,
				android.R.layout.simple_list_item_1,
				new String[] { "no shopping provider" }));
				*/
		
	
	}

}