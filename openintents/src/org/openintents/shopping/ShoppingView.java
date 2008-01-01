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
import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.ContainsFull;
import org.openintents.provider.Shopping.Lists;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Spannable;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;

public class ShoppingView extends Activity //implements AdapterView.OnItemClickListener 
{

	/**
	 * TAG for logging.
	 */
	private static final String TAG = "ShoppingProvider";
	
	private static final int MENU_NEW_LIST = 1;

	/**
	 * Private members connected to Spinner ListFilter
	 */
	private Spinner mSpinnerListFilter;
	private Cursor mCursorListFilter;
	private static final String[] mStringListFilter = 
		new String[] { Lists._ID, Lists.NAME, Lists.IMAGE};
	private static final int mStringListFilterID = 0;
	private static final int mStringListFilterNAME = 1;
	private static final int mStringListFilterIMAGE = 2;
	
	ListView mListItems;
	Cursor mCursorItems;
	private String TEST;
	private static final String[] mStringItems =
		new String[] { 
				ContainsFull._ID, 
				ContainsFull.ITEM_NAME,
				ContainsFull.ITEM_IMAGE,
				ContainsFull.STATUS};
	private static final int mStringItemsCONTAINSID = 0;
	private static final int mStringItemsITEMNAME = 1;
	private static final int mStringItemsITEMIMAGE = 2;
	private static final int mStringItemsSTATUS = 3;
	
	protected Context mDialogContext;
	protected Dialog mDialog;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		//setTheme(android.R.style.Theme_White);
		//setTheme(android.R.style.Theme_Dialog);
		//setTheme(android.R.style.Theme_Dark);
		//setTheme(android.R.style.Theme_Black);
		setContentView(R.layout.shopping);

		mSpinnerListFilter = (Spinner) findViewById(R.id.spinner_listfilter);
		
		mSpinnerListFilter.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView parent, View v,
					int position, long id) {
				fillItems();
			}
			
			public void onNothingSelected(AdapterView arg0) {
				fillItems();
			}
		});
        
		EditText et = (EditText) findViewById(R.id.edittext_add_item);
		et.setKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent key) {
				//Log.i(TAG, "KeyCode: " + keyCode 
				//		+ " =?= " 
				//		+Integer.parseInt(getString(R.string.key_return)) );
				
				// Shortcut: Instead of pressing the button, 
				// one can also press the "Enter" key.
				if (key.isDown() && 
						keyCode == Integer.parseInt(getString(R.string.key_return)))
				{
					insertNewItem();
					
					return true;
					
				};
				return false;
			}
			
		});
		
		Button button = (Button) findViewById(R.id.button_add_item);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				insertNewItem();
				
			}
		});
		
		mListItems = (ListView) findViewById(R.id.list_items);
		//mListItems.setOnItemClickListener(this);
		
		mListItems.setOnItemClickListener(
			new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView parent, 
						View v, int pos, long id) {
					// strike item through or undo this.
					Log.i(TAG, "onItemClick: Pos:" + pos + ", id: " + id);
					//Log.i(TAG, "Contains ID: " + getSelectedItemContainsId(pos));
					// id contains what we need...
					
					Cursor c = (Cursor) parent.obtainItem(pos);
					
					Log.i(TAG, "via Cursor: " 
							+ c.getString(mStringItemsITEMNAME)
							+ "old status: " + c.getLong(mStringItemsSTATUS));
					
					// Toggle status:
					long oldstatus = c.getLong(mStringItemsSTATUS);
					long newstatus = Shopping.Status.BOUGHT;
					if (oldstatus == Shopping.Status.BOUGHT) {
						newstatus = Shopping.Status.WANT_TO_BUY;
					}
						
					c.updateLong(mStringItemsSTATUS, newstatus);
					
					Log.i(TAG, "Commit now:");
					c.commitUpdates();
					
					Log.i(TAG, "Requery now:");
					c.requery();
					
					// fillItems();
					
				}
				
		});
		mListItems.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView parent, View v,
					int position, long id) {
				// Log.i(TAG, "mListItems selected: pos:" 
				// 	+ position + ", id:" + id);
				
			}

			@Override
			public void onNothingSelected(AdapterView arg0) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		
		fillListFilter();
		fillItems();
	}
	
	/**
	 * Inserts new item from edit box into 
	 * currently selected shopping list.
	 */
	private void insertNewItem() {
		EditText edittext = 
			(EditText) findViewById(R.id.edittext_add_item);
						
		long listId = getSelectedListId();
		
		long itemId = Shopping.insertItem(getContentResolver(), 
				edittext.getText().toString());
		
		Log.i(TAG, "Insert new item. " 
				+ " itemId = " + itemId + ", listId = " + listId);
		Shopping.insertContains(getContentResolver(), 
				itemId, listId);
		
		edittext.setText("");
		
		fillItems();
	}
	
	// Menu

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_NEW_LIST, R.string.new_list);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(Item item) {
		super.onOptionsItemSelected(item);
		switch (item.getId()) {
		case MENU_NEW_LIST:
			
			mDialog = new Dialog(ShoppingView.this);
			
			mDialog.setContentView(R.layout.input_box);
			
			mDialog.setTitle(getString(R.string.ask_new_list));
			
			EditText et = (EditText) mDialog.findViewById(R.id.edittext);
			et.setText(getString(R.string.new_list));
			et.selectAll();
			
			// Accept OK also when user hits "Enter"
			et.setKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(final View v, final int keyCode, 
						final KeyEvent key) {
					//Log.i(TAG, "KeyCode: " + keyCode);
					
					if (key.isDown() && keyCode == Integer
								.parseInt(getString(R.string.key_return))) {
						// User pressed "Enter" 
						EditText edittext = (EditText) 
							mDialog.findViewById(R.id.edittext);
						
						Shopping.insertList(getContentResolver(), 
							edittext.getText().toString());
						
						edittext.setText("");
						fillListFilter();
						
						mDialog.dismiss();
						return true;	
					}
					return false;
				}
				
			});
			
			
			Button bOk = (Button) mDialog.findViewById(R.id.ok);
			bOk.setOnClickListener(new OnClickListener() {
				public void onClick(final View v) {
					EditText edittext = (EditText) mDialog
						.findViewById(R.id.edittext);
					
					Shopping.insertList(getContentResolver(), 
							edittext.getText().toString());
					
					edittext.setText("");
					fillListFilter();
					
					mDialog.dismiss();
				}
			});
			
			Button bCancel = (Button) mDialog.findViewById(R.id.cancel);
			bCancel.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					mDialog.cancel();
				}
			});
			
			mDialog.show();
			
			break;	
		}
		return true;
	}
	
	// Helper functions
	/**
	 * Returns the ID of the selected shopping list.
	 * @return ID of selected shopping list.
	 */
	private long getSelectedListId() {
		// Obtain Id of currently selected shopping list:
		mCursorListFilter.moveTo(
				mSpinnerListFilter.getSelectedItemIndex());
		return mCursorListFilter.getLong(mStringListFilterID);
	};
	
	private void fillListFilter() {
		// Get a cursor with all lists
		mCursorListFilter = getContentResolver().query(Lists.CONTENT_URI, 
				mStringListFilter, 
				null, null, Lists.DEFAULT_SORT_ORDER);
		startManagingCursor(mCursorListFilter);

		if (mCursorListFilter == null) {
			Log.e(TAG, "missing shopping provider");
			mSpinnerListFilter.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no shopping provider" }));
			return;
		}
		
		if (mCursorListFilter.count() < 1) {
			// We have to create default shopping list:
			// TODO Put the following string into resource my_shopping_list
			Shopping.insertList(getContentResolver(), "My shopping list");
			
			// TODO Check if insertion really worked. Otherwise
			//      we may end up in infinite recursion.
			
			// The insertion should have worked, so let us call ourselves
			// to try filling the list again:
			fillListFilter();
			return;
		}

		ArrayList<String> list = new ArrayList<String>();
		// TODO Create summary of all lists
		// list.add(ALL);
		while (mCursorListFilter.next()) {
			list.add(mCursorListFilter.getString(mStringListFilterNAME));
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		mSpinnerListFilter.setAdapter(adapter);
		
	}


	private void fillItems() {
		Log.i(TAG, "Starting fillItems()");
		
		long listId = getSelectedListId();
		
		// Get a cursor for all items that are contained 
		// in currently selected shopping list.
		mCursorItems = getContentResolver().query(
				ContainsFull.CONTENT_URI,
				mStringItems,
				"list_id = " + listId, null,
				ContainsFull.DEFAULT_SORT_ORDER);
		startManagingCursor(mCursorItems);
		

		if (mCursorItems == null) {
			Log.e(TAG, "missing shopping provider");
			mListItems.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no shopping provider" }));
			return;
		}
		
		ListAdapter adapter = new mSimpleCursorAdapter(this,
				// Use a template that displays a text view
				R.layout.shopping_item_row,
				// Give the cursor to the list adapter
				mCursorItems,
				// Map the IMAGE and NAME to...
				new String[] { 
					ContainsFull.ITEM_NAME, 
					ContainsFull.ITEM_IMAGE },
				// the view defined in the XML template
				new int[] { 
					R.id.name, 
					R.id.image_URI });
		mListItems.setAdapter(adapter);
		
		TEST = new String("ok");
		Log.i(TAG, "fillItems: mCursorItems : " + (mCursorItems == null) 
				+ ", " + TEST);
		
		//strikeItems();
	}
	
	/**
	 * 
	 * Extend the SimpleCursorAdapter to strike through items.
	 * if STATUS == Shopping.Status.BOUGHT
	 * 
	 */
	public class mSimpleCursorAdapter extends SimpleCursorAdapter {

		/**
		 * Constructor simply calls super class.
		 * @param context Context.
		 * @param layout Layout.
		 * @param c Cursor.
		 * @param from Projection from.
		 * @param to Projection to.
		 */
		mSimpleCursorAdapter(final Context context, final int layout, 
				final Cursor c, final String[] from, final int[] to) {
			super(context, layout, c, from, to);
		}
		
		@Override
		public void bindView(final View view, final Context context, 
				final Cursor cursor) {
			//Log.i(TAG, "bindView " + view.toString());
			super.bindView(view, context, cursor);
			
			TextView t = (TextView) view.findViewById(R.id.name);
			if (cursor.getLong(mStringItemsSTATUS) 
					== Shopping.Status.BOUGHT) {
				// We have bought the item,
				// so we strike it through:
				
				// First convert text to 'spannable'
				t.setText(t.getText(), TextView.BufferType.SPANNABLE);
				Spannable str = (Spannable) t.getText();
				
				// Strikethrough
				str.setSpan(new StrikethroughSpan(), 0, str.length(),
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				
				// apply color
				// TODO: How to get color from resource?
				//Drawable colorStrikethrough = context
				//	.getResources().getDrawable(R.drawable.strikethrough);
				str.setSpan(new ForegroundColorSpan(0x33336600), 0,
						str.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		
	}
}

