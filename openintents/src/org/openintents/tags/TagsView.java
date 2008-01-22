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

package org.openintents.tags;

import java.util.ArrayList;

import org.openintents.OpenIntents;
import org.openintents.R;
import org.openintents.provider.Tag.Contents;
import org.openintents.provider.Tag.Tags;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.Menu.Item;
import android.view.View.OnClickListener;
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

public class TagsView extends Activity {

	/** tag for logging */
	private static final String TAG = "tagView";
	private static final String TAG_ACTION = "TAG";
	protected static final String ALL = "ALL"; // TODO: Put string into resource
	private static final int MENU_VIEW_TAG = 1;

	private ListView mTags;
	private ListView mListContents;
	private Spinner mTagFilter;
	private String mFilter = null;
	private EditText mNewTagText;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.tags);

		mTags = (ListView) findViewById(R.id.tags);
		mListContents = (ListView) findViewById(R.id.contents);
		
		mNewTagText = (EditText)findViewById(R.id.new_tag);
		mListContents.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView adapter, View view, int position, long id){
				mNewTagText.setText((Cursor)adapter.getSelectedItem(), 1);
			}
		});

		mTagFilter = (Spinner) findViewById(R.id.tag_filter);
		mTagFilter.setOnItemSelectedListener(new OnItemSelectedListener() {

			public void onItemSelected(AdapterView parent, View v,
					int position, long id) {
				mFilter = ((TextView) v).getText().toString();
				if (ALL.equals(mFilter)) {
					mFilter = null;
				}
				fillDataTags();
			}

			public void onNothingSelected(AdapterView arg0) {
				mFilter = null;
				fillDataTags();
			}

		});
		
		String uri = (String) getIntent().getExtra(Tags.QUERY_URI);
		if(uri != null){
			TextView textView = (TextView)findViewById(R.id.new_content);
			textView.setText(uri);
		}
		
		Button button = (Button) findViewById(R.id.add_tag_button);
		button.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				EditText newTag = (EditText) findViewById(R.id.new_tag);
				EditText newContent = (EditText) findViewById(R.id.new_content);

				insertTag(newTag.getText().toString(), newContent.getText()
						.toString());
				
				String uri = (String) getIntent().getExtra(Tags.QUERY_URI);
				if(uri != null && newContent.getText().toString().equals(uri)){
					setResult(Activity.RESULT_OK);
					finish();
				}

			}

		});

		fillDataTagFilter();
		fillDataTags();
		fillDataContent();

	}

	protected void insertTag(String tag, String content) {
		ContentValues values = new ContentValues(2);
		values.put(Tags.URI_1, tag);
		values.put(Tags.URI_2, content);

		try {
			getContentResolver().insert(Tags.CONTENT_URI, values);
		} catch (Exception e) {
			Log.i(TAG, "insert failed", e);
			return;
		}
		fillDataTagFilter();
		fillDataTags();
		fillDataContent();

	}

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

	private void fillDataTags() {

		String filter = null;
		String[] filterArray = null;
		if (mFilter != null) {
			filter = "uri_1 = ?";
			filterArray = new String[] { mFilter };
		}
		// Get a cursor with all tags
		Cursor c = getContentResolver().query(
				Tags.CONTENT_URI,
				new String[] { Tags._ID, Tags.TAG_ID, Tags.CONTENT_ID,
						Tags.URI_1, Tags.URI_2 }, filter, filterArray,
				Tags.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		if (c == null) {
			Log.e(TAG, "missing tag provider");
			mTags.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no tag provider" }));
			return;
		}

		ListAdapter adapter = new SimpleCursorAdapter(this,
		// Use a template that displays a text view
				R.layout.tag_row,
				// Give the cursor to the list adapter
				c,
				// Map the TAG / CONTENT columns in the database to...
				new String[] { Tags.TAG_ID, Tags.CONTENT_ID, Tags.URI_1,
						Tags.URI_2 },
				// The "text1" view defined in the XML template
				new int[] { R.id.tag_tag, R.id.tag_content, R.id.tag_uri_1,
						R.id.tag_uri_2 });
		mTags.setAdapter(adapter);

	}

	private void fillDataContent() {

		// Get a cursor with all tags
		Cursor c = getContentResolver().query(Contents.CONTENT_URI,
				new String[] { Contents._ID, Contents.URI, Contents.TYPE },
				null, null, Contents.DEFAULT_SORT_ORDER);
		startManagingCursor(c);

		if (c == null) {
			Log.e(TAG, "missing tag provider");
			mTags.setAdapter(new ArrayAdapter(this,
					android.R.layout.simple_list_item_1,
					new String[] { "no tag provider" }));
			return;
		}

		SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
				// Use a template that displays a text view
				R.layout.tag_row,
				// Give the cursor to the list adapter
				c,
				// Map the CONTENTS column in the database to...
				new String[] { Contents._ID, Contents.URI, Contents.TYPE },
				// The view defined in the XML template
				new int[] { R.id.tag_tag, R.id.tag_content, R.id.tag_uri_1 });
		
		mListContents.setAdapter(adapter);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_VIEW_TAG, R.string.tags_view_tag);
		return true;
	}
	
	@Override
	public boolean onMenuItemSelected(int featureId, Item item) {

		super.onMenuItemSelected(featureId, item);

		switch (item.getId()) {
		case MENU_VIEW_TAG:
			Intent intent = new Intent(Intent.VIEW_ACTION, Tags.CONTENT_URI).putExtra(Tags.QUERY_TAG, mTagFilter.getSelectedItem());
			startActivity(intent);
		}

		return true;
	}
}