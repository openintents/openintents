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
import org.openintents.newsreader.channels.ChannelSettings;
import org.openintents.provider.News;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ChooseCategoriesDialog extends Activity {

	public static final String _TAG = "ChooseCategoriesDialog";

	public static final int STATE_CREATE = 1;
	public static final int STATE_EDIT = 2;

	public static final String CURRENT_CATS = "CURRENT_CATS";

	private Cursor mCategories;

	private ListView mCategoryList;
	private ListView mMembersList;

	private Button bInstance;

	public void onCreate(Bundle bundle) {

		super.onCreate(bundle);

		setTheme(android.R.style.Theme_Dialog);
		setContentView(R.layout.choosecategoriesdialog);

		mCategoryList = (ListView) findViewById(R.id.newsreader_choosecats_availablecats);
		mMembersList = (ListView) findViewById(R.id.newsreader_choosecats_members);

		mCategoryList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView parent, View v,
							int position, long id) {
						// Clicking an item starts editing it
						addContactAsMember(position);
					}

				});

		mMembersList
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					public void onItemClick(AdapterView parent, View v,
							int position, long id) {
						// Clicking an item starts editing it
						deleteMember(position);
					}

				});

		// init buttons
		bInstance = (Button) findViewById(R.id.newsreader_choosecats_add);
		bInstance.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (mCategoryList.getSelectedItemPosition() >= 0) {
					addContactAsMember(mCategoryList.getSelectedItemPosition());
				} else {
					Toast.makeText(ChooseCategoriesDialog.this,
							"no contact selected", 1000);
				}
			}
		});

		bInstance = (Button) findViewById(R.id.newsreader_choosecats_del);
		bInstance.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				if (mMembersList.getSelectedItemPosition() >= 0) {
					deleteMember(mMembersList.getSelectedItemPosition());
				} else {
					Toast.makeText(ChooseCategoriesDialog.this,
							"no contact selected", 1000);
				}
			}
		});

		bInstance = (Button) findViewById(R.id.newsreader_choosecats_save);
		bInstance.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				ChooseCategoriesDialog.this.save();
			}
		});

		init();
	}

	public void init() {

		mCategories = managedQuery(News.Categories.CONTENT_URI,
		// new String[]{android.provider.Contacts.PeopleColumns.NAME},
				null, null, null, null);
		// managedQuery(Uri uri, String[] projection, String selection, String[]
		// selectionArgs, String sortOrder)
		SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
				android.R.layout.simple_list_item_1, mCategories,
				new String[] { News.Categories.NAME },
				new int[] { android.R.id.text1 });

		mCategoryList.setAdapter(sca);

		Bundle b = getIntent().getExtras();

		String s = b.getString(CURRENT_CATS);
		String[] ccats = s.split(News.CAT_DELIMITER);
		ArrayAdapter aa = new ArrayAdapter(this,
				android.R.layout.simple_list_item_1);
		for (int i = 0; i < ccats.length; i++) {
			aa.add(ccats[i]);
		}

		mMembersList.setAdapter(aa);
		/*
		 * if (mState == STATE_CREATE) { long now = System.currentTimeMillis();
		 * String snow = Long.toString(now); mName.setText(snow); ContentValues
		 * cv = new ContentValues(); cv.put(JamSessions.Session.NAME, snow);
		 * mSessionUri = JamSessions.insert(JamSessions.Session.CONTENT_URI,
		 * cv); cSession = managedQuery(mSessionUri,
		 * JamSessions.Session.PROJECTION, null, null); Log.d(_TAG, "uri>>" +
		 * mSessionUri.toString());
		 * 
		 * } else if (mState == STATE_EDIT) { mSessionUri =
		 * getIntent().getData(); cSession = managedQuery(mSessionUri,
		 * JamSessions.Session.PROJECTION, null, null); cSession.first();
		 * mName.setText(cSession.getString(cSession
		 * .getColumnIndex(JamSessions.Session.NAME))); }
		 * 
		 * cDetails = managedQuery(JamSessions.SessionDetails.CONTENT_URI,
		 * JamSessions.SessionDetails.PROJECTION,
		 * JamSessions.SessionDetails.SESSION_ID + " =" +
		 * mSessionUri.getLastPathSegment(), null);
		 * 
		 * sca = new SimpleCursorAdapter(this,
		 * android.R.layout.simple_list_item_1, cDetails, new String[] {
		 * JamSessions.SessionDetails.CONTACT_NAME }, new int[] {
		 * android.R.id.text1 }); mMembersList.setAdapter(sca);
		 */
	}

	private void addContactAsMember(int pos) {

		mCategories.moveToPosition(pos);

		String name = mCategories.getString(mCategories
				.getColumnIndex(News.Categories.NAME));
		ArrayAdapter adapter = (ArrayAdapter) mMembersList.getAdapter();
		adapter.add(name);

		save();

	}

	private void deleteMember(int pos) {

		ArrayAdapter adapter = (ArrayAdapter) mMembersList.getAdapter();
		adapter.remove(adapter.getItem(pos));
		/*
		 * cDetails.moveTo(pos); cDetails.deleteRow();
		 */
		save();
	}

	private void save() {
		ArrayAdapter aa = (ArrayAdapter) mMembersList.getAdapter();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < aa.getCount(); i++) {
			String category = (String) aa.getItem(i);
			if (!TextUtils.isEmpty(category)) {
				sb.append(category);
				sb.append(News.CAT_DELIMITER);
			}
		}
		// remove last delimiter
		if (sb.length() > 0) {
			sb.delete(sb.length() - News.CAT_DELIMITER.length() - 1, sb
					.length() - 1);
		}
		
		Intent result = new Intent();
		Bundle b = new Bundle();
		b.putString(CURRENT_CATS, sb.toString());
		result.putExtras(b);
		setResult(RESULT_OK, result);

	}

}/* eoc */