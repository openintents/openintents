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

import java.util.ArrayList;

import org.openintents.R;
import org.openintents.provider.Tag;
import org.openintents.provider.ContentIndex.Dir;
import org.openintents.provider.Tag.Contents;
import org.openintents.provider.Tag.Tags;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * View to show tags in a hierarchical manner.
 * 
 * 
 * 
 */
public class ContentBrowserView extends ListActivity implements Runnable {

	// tag for logging
	private static final String TAG = "tagHierarchyView";
	protected static final String ALL = "ALL"; // TODO: Put string into
	// resource
	private static final int MENU_ADD_TAG = 1;
	private static final int MENU_VIEW_CONTENT = 2;
	private static final int MENU_REMOVE_TAG = 3;
	private static final int MENU_PACKAGES = 4;

	protected static final int REQUEST_DIR_PICK = 1;
	protected static final int REQUEST_CONTENT_PICK = 2;

	private AutoCompleteTextView mTagFilter;

	private ListAdapter mTaggedContentAdapter;
	private Tag mTags;

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

		mTags = new Tag(this);
		fillDataTagFilter();
		Thread t = new Thread(this);
		t.start();
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

		menu.add(0, MENU_ADD_TAG, R.string.tags_add_tag, R.drawable.new_doc);
		menu.add(0, MENU_VIEW_CONTENT, R.string.tags_view_content,
				R.drawable.window);
		menu
				.add(0, MENU_REMOVE_TAG, R.string.tags_remove_tag,
						R.drawable.trash);

		menu.add(0, MENU_PACKAGES, R.string.menu_package_list,
				R.drawable.advanced);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);

		boolean show = getListView().getSelectedItemId() != Long.MIN_VALUE;
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
			// pick a directory, expect a content uri of the given directory as
			// return value
			intent = new Intent(Intent.PICK_ACTION, Dir.CONTENT_URI);
			startSubActivity(intent, REQUEST_DIR_PICK);
			break;
		case MENU_VIEW_CONTENT:
			String uri = ((Cursor) getListView().getSelectedItem())
					.getString(1);
			try {
				intent = new Intent(Intent.VIEW_ACTION, Uri.parse(uri));
			} catch (Exception e) {
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
			mTags.removeTag(tag, uri);

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
		case REQUEST_DIR_PICK:
			if (data != null) {
				// data is the picked content directory
				Uri uri = Uri.parse(data);
				Intent intent = new Intent(Intent.PICK_ACTION, uri);
				if (getPackageManager().resolveActivity(intent, 0) != null) {
					startSubActivity(intent, REQUEST_CONTENT_PICK);
				} else {
					AlertDialog.show(this, "info", 0, "no pick activity for " + data, "ok", true);
				}
			}
			break;
		case REQUEST_CONTENT_PICK:
			if (data != null) {
				// data is the picked content
				mTags.startAddTagActivity(tag, data);
			}
		}
	}

	@Override
	protected void onListItemClick(ListView listview, View view, int i, long l1) {
		setSelection(i);
	}

	public void run() {
		DirectoryRegister r = new DirectoryRegister(this);
		Resources res = getResources();
		try {
			r.fromXML(res.openRawResource(R.raw.browser));
			r.fromXML(res.openRawResource(R.raw.contacts));
			r.fromXML(res.openRawResource(R.raw.notepad));
			r.fromXML(res.openRawResource(R.raw.media));
			r.fromXML(res.openRawResource(R.raw.shopping));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}