/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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

package org.openintents.tags.content;

import java.net.URISyntaxException;
import java.util.ArrayList;

import org.openintents.R;
import org.openintents.main.OpenIntents;
import org.openintents.provider.Tag;
import org.openintents.provider.ContentIndex.Dir;
import org.openintents.provider.Tag.Contents;
import org.openintents.provider.Tag.Tags;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.ContentURI;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.SubMenu;
import android.view.View;
import android.view.Menu.Item;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * View to show tags in a hierarchical manner.
 * 
 * 
 * 
 */
public class ContentBrowserView extends ListActivity {

	// tag for logging
	private static final String TAG = "tagHierarchyView";
	protected static final String ALL = "ALL"; // TODO: Put string into
	// resource
	private static final int MENU_ADD_TAG = 1;
	private static final int MENU_VIEW_CONTENT = 2;
	private static final int MENU_REMOVE_TAG = 3;
	private static final int MENU_PACKAGES = 4;
	
	protected static final int REQUEST_PICK = 1;
	 

	private AutoCompleteTextView mTagFilter;

	private ListAdapter mTaggedContentAdapter;

	/**
	 * Called when the activity is first created.
	 * 
	 * @param icicle
	 *            bundle
	 */
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.tags_content_browser);

		getListView();

		mTagFilter = (AutoCompleteTextView) findViewById(R.id.tag_filter);

		mTagFilter.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence cs, int a, int b, int c) {
				fillDataTaggedContent();
			}

			public void beforeTextChanged(CharSequence cs, int a, int b, int c) {
				// do nothing
			}
		});

		fillDataTagFilter();
	}

	/**
	 * fill data for filter (auto complete).
	 */
	private void fillDataTagFilter() {
		// Get a cursor with all tags
		Cursor c = getContentResolver().query(Contents.CONTENT_URI,
				new String[] { Contents._ID, Contents.URI, Contents.TYPE },
				"type like 'TAG%'", null, Contents.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		if (c == null) {
			Log.e(TAG, "missing tag provider");
			mTagFilter.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no tag provider" }));
			return;
		}

		ArrayList<String> list = new ArrayList<String>();
		list.add(ALL);
		while (c.next()) {
			list.add(c.getString(1));
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, list);
		adapter.setDropDownViewResource(android.R.layout.simple_list_item_1);
		mTagFilter.setAdapter(adapter);

	}

	/**
	 * fill main list.
	 */
	private void fillDataTaggedContent() {

		// Get a cursor with all tags
		Cursor c = getContentResolver().query(Tags.CONTENT_URI,
				new String[] { Tags._ID, Tags.URI_2 }, "content1.uri like ?",
				new String[] { mTagFilter.getText().toString() },
				"content1.uri");
		startManagingCursor(c);

		if (c == null) {
			Log.e(TAG, "missing tag provider");
			setListAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no tag provider" }));
			return;
		}

		ContentListAdapter adapter = new ContentListAdapter(c, this);
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_ADD_TAG, R.string.tags_add_tag);
		menu.add(0, MENU_VIEW_CONTENT, R.string.tags_view_content);
		menu.add(0, MENU_REMOVE_TAG, R.string.tags_remove_tag);
		
		menu.addSeparator(0, 0);
		menu.add(0, MENU_PACKAGES, R.string.menu_package_list);

		Intent intent = new Intent(null, Tags.CONTENT_URI);
		intent.addCategory(Intent.ALTERNATIVE_CATEGORY);
		menu.addIntentOptions(Menu.ALTERNATIVE, 0, new ComponentName(this,
				ContentBrowserView.class), null, intent, 0, null);

		
		intent = new Intent(null, Tags.CONTENT_URI);
		intent.addCategory(Intent.SELECTED_ALTERNATIVE_CATEGORY);
		menu.addIntentOptions(Menu.ALTERNATIVE, 0, new ComponentName(this,
				ContentBrowserView.class), null, intent, 0, null);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		boolean show = getListView().getSelectedItem() instanceof Cursor;
		menu.get(1).setShown(show);
		menu.get(2).setShown(show);
		
		return true;
	}

	@Override
	public boolean onMenuItemSelected(int featureId, Item item) {
		super.onMenuItemSelected(featureId, item);
		
		String tag = mTagFilter.getText().toString();
		Intent intent;
		switch (item.getId()) {
		case MENU_ADD_TAG:
			// pick a directory, expect a content uri of the given directory as return value
			intent = new Intent(Intent.PICK_ACTION, Dir.CONTENT_URI.addQueryParameter("q", "content"));
			startSubActivity(intent, REQUEST_PICK);
			break;
		case MENU_VIEW_CONTENT:
			String uri = ((Cursor) getListView().getSelectedItem())
					.getString(1);
			try {
				intent = new Intent(Intent.VIEW_ACTION, new ContentURI(uri));
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				intent = null;
			}
			if (intent != null) {
				startActivity(intent);
			}
			break;
		case MENU_REMOVE_TAG:
			uri = ((Cursor) getListView().getSelectedItem()).getString(1);
			try {
				getContentResolver().getProvider(Tags.CONTENT_URI).delete(
						Tags.CONTENT_URI, "uri_1 = ? AND uri_2 = ?",
						new String[] { tag, uri });
			} catch (DeadObjectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;
		case MENU_PACKAGES:
			startActivity(new Intent(this, PackageList.class));
			break;
		}

		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			String data, Bundle extras) {
		super.onActivityResult(requestCode, resultCode, data, extras);

		String tag = mTagFilter.getText().toString();

		switch (requestCode) {
		case REQUEST_PICK:
			Intent intent = new Intent(org.openintents.OpenIntents.TAG_ACTION,
					Tags.CONTENT_URI).putExtra(Tags.QUERY_TAG, tag).putExtra(
					Tags.QUERY_URI, data);
			startActivity(intent);
		}
	}
}