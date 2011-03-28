package org.openintents.newsreader.categories;
/*
*	This file is part of OI Newsreader.
*	Copyright (C) 2007-2009 OpenIntents.org
*	OI Newsreader is free software: you can redistribute it and/or modify
*	it under the terms of the GNU General Public License as published by
*	the Free Software Foundation, either version 3 of the License, or
*	(at your option) any later version.
*
*	OI Newsreader is distributed in the hope that it will be useful,
*	but WITHOUT ANY WARRANTY; without even the implied warranty of
*	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*	GNU General Public License for more details.
*
*	You should have received a copy of the GNU General Public License
*	along with OI Newsreader.  If not, see <http://www.gnu.org/licenses/>.
*/

import org.openintents.newsreader.R;
import org.openintents.provider.News;
import org.openintents.provider.News.Categories;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SimpleCursorAdapter.ViewBinder;

public class CategoriesListActivity extends ListActivity {

	private static final int MENU_ADD = 1001;
	private static final int MENU_SEARCH = 1002;

	private static final String _TAG = "CategoriesListActivity";
	private static final int CTX_MENU_DELETE = 2001;
	private static final int ADD_CATEGORY = 3001;

	private Cursor mCursor;
	private String mCurrentCategories;
	private News mNews;
	protected boolean mSavePending;
	private AlertDialog mDialog;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setTitle(R.string.select_categories);
		mNews = new News(getContentResolver());

		mCurrentCategories = getIntent().getStringExtra(
				ChooseCategoriesDialog.CURRENT_CATS);

		mCursor = managedQuery(Categories.CONTENT_URI, new String[] {
				Categories._ID, Categories.NAME }, null, null, null);

		final SimpleCursorAdapter adapter;
		if (Intent.ACTION_EDIT.equals(getIntent().getAction())) {
			if (mCurrentCategories == null) {
				mCurrentCategories = "";
			}

			adapter = new SimpleCursorAdapter(this,
					android.R.layout.simple_list_item_multiple_choice, mCursor,
					new String[] { Categories.NAME },
					new int[] { android.R.id.text1 });

			adapter.setViewBinder(new ViewBinder() {

				public boolean setViewValue(View view, Cursor cursor, int i) {
					String category = cursor.getString(i);
					Log.v(_TAG, i + " " + view);
					if (view instanceof CheckedTextView) {
						adapter.setViewText((TextView) view, category);
						boolean checkedCat = getCurrentCategories().contains(
								category);
						getListView().setItemChecked(cursor.getPosition(),
								checkedCat);

						return true;
					} else {
						Log.v(_TAG, "set view of " + view);
						return false;
					}

				}

			});

			getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

		} else {
			adapter = new SimpleCursorAdapter(this,
					android.R.layout.simple_list_item_1, mCursor,
					new String[] { Categories.NAME },
					new int[] { android.R.id.text1 });
		}

		setListAdapter(adapter);
	}

	protected String getCurrentCategories() {
		return mCurrentCategories;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(Menu.NONE, MENU_ADD, Menu.NONE, R.string.addcategory).setIcon(
				android.R.drawable.ic_menu_add);	
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		switch (item.getItemId()) {
		case MENU_ADD:

			showDialog(R.layout.add_category);
			break;		
		}
		return true;
	}

	public Dialog onCreateDialog(int dialogId) {
		Dialog result = super.onCreateDialog(dialogId);
		if (result == null && dialogId == R.layout.add_category) {
			LayoutInflater inflater = (LayoutInflater) this
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			LinearLayout view = (LinearLayout) inflater.inflate(
					R.layout.add_category, null);
			final AlertDialog d = new AlertDialog.Builder(this).setTitle(
					R.string.addcategory).setView(view).create();

			final EditText editView = (EditText) view
					.findViewById(R.id.editview);

			Button b = (Button) view.findViewById(R.id.cancel);
			b.setOnClickListener(new OnClickListener() {

				public void onClick(View view) {
					// cancel
					d.cancel();
				}

			});
			b = (Button) view.findViewById(R.id.ok);
			b.setOnClickListener(new OnClickListener() {

				public void onClick(View view) {

					// add trimmed category
					String newCat = editView.getText().toString().trim();

					if (newCat.length() > 0) {
						ContentValues cs = new ContentValues();
						cs.put(Categories.NAME, newCat);
						Uri uri = mNews.insertIfNotExists(
								Categories.CONTENT_URI, "upper("
										+ Categories.NAME + ") = upper(?)",
								new String[] { newCat }, cs);

						if (uri == null) {
							Toast
									.makeText(CategoriesListActivity.this,
											R.string.category_exists,
											Toast.LENGTH_LONG).show();
						} else {
							mCurrentCategories = mCurrentCategories
									+ News.CAT_DELIMITER + newCat;
							mSavePending = true;

							// close dialog
							d.dismiss();
						}
					}
				}

			});

			result = d;
		}

		return result;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			if (requestCode == ADD_CATEGORY) {
				mCurrentCategories = mCurrentCategories + News.CAT_DELIMITER
						+ data.getStringExtra(Categories.NAME);
				mSavePending = true;
			}
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu contextmenu, View view,
			ContextMenuInfo contextmenuinfo) {
		super.onCreateContextMenu(contextmenu, view, contextmenuinfo);
		contextmenu.add(Menu.NONE, CTX_MENU_DELETE, Menu.NONE, R.string.delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		long id = ((AdapterContextMenuInfo) item.getMenuInfo()).id;
		Log.v(_TAG, "ctx id:" + id);
		switch (item.getItemId()) {
		case CTX_MENU_DELETE:
			deleteCategory(id);
		}

		return true;
	}

	private void deleteCategory(long id) {
		getContentResolver().delete(
				Uri
						.withAppendedPath(Categories.CONTENT_URI, String
								.valueOf(id)), null, null);
		mCursor.requery();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		save();
		Log.d(_TAG, "Clicked at >>" + position);
	}

	protected void save() {
		Log.d(_TAG,"saving..");
		StringBuffer sb = new StringBuffer();

		SparseBooleanArray categories = getListView().getCheckedItemPositions();

		if (categories != null) {
			for (int i = 0; i < categories.size(); i++) {
				boolean checked = categories.get(i, false);
				if (checked) {
					sb.append(((Cursor) getListAdapter().getItem(i))
							.getString(1));
					sb.append(News.CAT_DELIMITER);
				}
			}
			// remove last delimiter
			if (sb.length() > 0) {
				sb.delete(sb.length() - News.CAT_DELIMITER.length(), sb
						.length());
			}
			mCurrentCategories = sb.toString();
			Log.v(_TAG, mCurrentCategories);

			Intent result = new Intent();
			Bundle b = new Bundle();
			b
					.putString(ChooseCategoriesDialog.CURRENT_CATS,
							mCurrentCategories);
			result.putExtras(b);
			setResult(RESULT_OK, result);
		} else {
			// single choice mode
			// do nothing
		}
		mSavePending = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(_TAG, "onResume: entering");
		Log.d(_TAG, "onResume: current cats>"+mCurrentCategories+"<");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(_TAG,"onPause, savePending>"+mSavePending);
		if (mSavePending) {
			save();
		}
	}

	protected void onSavedInstanceState(Bundle inState){
		Log.d(_TAG,"SavedInstanceState,");
	}

}/* eoc */
