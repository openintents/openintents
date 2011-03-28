package org.openintents.newsreader.channels;
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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import org.openintents.newsreader.R;
import org.openintents.newsreader.categories.ChooseCategoriesDialog;
import org.openintents.newsreader.services.AbstractFeedFetcherThread;
import org.openintents.newsreader.services.AtomSaxFetcherThread;
import org.openintents.newsreader.services.ChannelParser;
import org.openintents.newsreader.services.NewsreaderService;
import org.openintents.newsreader.services.RSSSaxFetcherThread;
import org.openintents.provider.News;
import org.openintents.provider.News.Categories;
import org.openintents.provider.News.Channel;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

public class ChannelSettings extends Activity {

	public static final int STATE_CREATE = 1001;
	public static final int STATE_EDIT = 1002;

	public static final int STATE_SAVE = 201;
	public static final int STATE_CANCEL = 202;

	public static final int REQUEST_EDIT_CATS = 404;

	private Uri cUri = null;

	private static final String _TAG = "ChannelSettings";

	private int mState;
	private Cursor mCursor;

	private long feedID = 0;
	private String feedType;

	private static String[] uCycles = new String[] { "10", "30", "60", "90",
			"120", "180" };

	private static String[] CHANNEL_PROJECTION = new String[] {
			News.Channel._ID, News.Channel.CHANNEL_NAME,
			News.Channel.CHANNEL_LINK, News.Channel.CHANNEL_CATEGORIES,
			News.Channel.CHANNEL_TYPE, News.Channel.CHANNEL_ICON_URI,
			News.Channel.NOTIFY_NEW, Channel.UPDATE_MSGS,
			News.Channel.UPDATE_CYCLE };

	private EditText mChannelLink = null;
	private EditText mChannelName = null;
	private ActionSpinner mChannelCats = null;
	private Spinner mType = null;
	private Spinner mUpdateCycle = null;
	private News mNews;
	private CheckBox mNotificationBox;
	private CheckBox mUpdateBox;
	protected boolean mCancle;
	private Button mButtonAdvanced;
	private LinearLayout mAdvancedSettings;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.channelsettings);

		mNews = new News(getContentResolver());
		// use only one instance to save memory
		Button bInstance = (Button) findViewById(R.id.saveFeedSettings);
		bInstance.setOnClickListener(mSaveFeedSettings);
		// bInstance = (Button) findViewById(R.id.CancelFeedSettings);
		// bInstance.setOnClickListener(mCancelFeedSettings);

		// bInstance = (Button) findViewById(R.id.editfeedcats);
		// bInstance.setOnClickListener(new OnClickListener() {
		// public void onClick(View v) {
		// editCats();
		// }
		// });

		bInstance = (Button) findViewById(R.id.checkurl);
		bInstance.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				checkFeed();
			}

		});

		mButtonAdvanced = (Button) findViewById(R.id.button_advanced_settings);
		mButtonAdvanced.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				toggleAdvancedSettings();
			}

		});

		mAdvancedSettings = (LinearLayout) findViewById(R.id.afeedsettings_ft2);

		// Don't show advanced settings by default.
		// TODO: Remember user selection in shared preferences?
		showAdvancedSettings(false);

		mChannelLink = (EditText) findViewById(R.id.feedurl);
		mChannelName = (EditText) findViewById(R.id.feedname);
		mChannelCats = (ActionSpinner) findViewById(R.id.feedcats);
		mChannelCats.setActionPerformer(new OnClickListener() {
			public void onClick(View view) {
				editCats();

			}
		});

		mNotificationBox = (CheckBox) findViewById(R.id.notificationbox);
		mUpdateBox = (CheckBox) findViewById(R.id.updatebox);

		mType = (Spinner) findViewById(R.id.afeedsettings_feedtype);
		mUpdateCycle = (Spinner) findViewById(R.id.afeedsettings_updatecycle);

		ArrayAdapter<String> ad = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, new String[] {
						News.FEED_TYPE_RSS, News.FEED_TYPE_ATOM });
		ad
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mType.setAdapter(ad);
		mType.setSelection(0);

		ArrayList<String> objects = new ArrayList<String>(Arrays
				.asList(uCycles));
		ad = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, objects);
		ad
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ad.add(getString(R.string.never));
		mUpdateCycle.setAdapter(ad);
		mUpdateCycle.setSelection(5);

		String action = getIntent().getAction();
		if (action.equals(Intent.ACTION_INSERT)) {
			mState = STATE_CREATE;
			// mChannelLink.setText("EHLO CREATOR!");
			setTitle(R.string.addnewfeed);
		} else if (action.equals(Intent.ACTION_EDIT)) {
			mState = STATE_EDIT;
			setTitle(R.string.editfeed);
			extractEditIntent();
		} else if (action.equals(Intent.ACTION_VIEW)) {
			mState = STATE_CREATE;
			setTitle(R.string.addnewfeed);
			extractBrowserIntent();
		} else {
			// unknown action, exit

		}
		Log.d("AFEEDSETTINGS", "cUri>>" + cUri + "<<");

	}

	private void toggleAdvancedSettings() {
		boolean currentlyvisible = (mAdvancedSettings.getVisibility() == View.VISIBLE);

		showAdvancedSettings(!currentlyvisible);
	}

	private void showAdvancedSettings(boolean show) {
		if (show) {
			// Show
			mAdvancedSettings.setVisibility(View.VISIBLE);
			mButtonAdvanced.setText(R.string.hide_advanced_settings);
		} else {
			// Hide
			mAdvancedSettings.setVisibility(View.GONE);
			mButtonAdvanced.setText(R.string.show_advanced_settings);
		}
	}

	/**
	 * 
	 */
	private void extractEditIntent() {

		Bundle b = getIntent().getExtras();
		cUri = Uri.parse(b.getString("URI"));
		feedID = Long.parseLong(cUri.getLastPathSegment());
		Log.d(_TAG, "State Edit:: cUri>>" + cUri + "<< type>>" + feedType
				+ "<<");

		mCursor = managedQuery(cUri, CHANNEL_PROJECTION, null, null, null);

		Log
				.d(_TAG, "State Edit::mCursor.count()>>" + mCursor.getCount()
						+ "<<");
		int mChannelType = 0;
		if (mCursor.getCount() > 0) {
			mCursor.moveToFirst();
			mChannelName.setText(mCursor.getString(mCursor
					.getColumnIndexOrThrow(News.Channel.CHANNEL_NAME)));
			mChannelLink.setText(mCursor.getString(mCursor
					.getColumnIndexOrThrow(News.Channel.CHANNEL_LINK)));
			String cats = mCursor.getString(mCursor
					.getColumnIndexOrThrow(News.Channel.CHANNEL_CATEGORIES));
			// bug #70
			if (cats == null) {	
				cats = "";
			}
			setCategories(cats);

			preselectSpinner(mCursor.getString(mCursor
					.getColumnIndexOrThrow(News.Channel.UPDATE_CYCLE)));

			mChannelType = mCursor.getInt(mCursor
					.getColumnIndexOrThrow(News.Channel.CHANNEL_TYPE));

			String iconUri = mCursor.getString(mCursor
					.getColumnIndexOrThrow(News.Channel.CHANNEL_ICON_URI));

			Log.v(_TAG, "" + iconUri);

			Log
					.v(
							_TAG,
							"check box "
									+ (mCursor
											.getInt(mCursor
													.getColumnIndexOrThrow(News.Channel.NOTIFY_NEW)) == 1));

			mNotificationBox.setChecked(mCursor.getInt(mCursor
					.getColumnIndexOrThrow(News.Channel.NOTIFY_NEW)) == 1);
			mUpdateBox.setChecked(mCursor.getInt(mCursor
					.getColumnIndexOrThrow(News.Channel.UPDATE_MSGS)) == 1);

		} else {
			Log.e(_TAG, "Cursor was empty");
		}

		if (mChannelType == News.CHANNEL_TYPE_RSS) {
			mType.setSelection(0);
			feedType = News.FEED_TYPE_RSS;

		} else if (mChannelType == News.CHANNEL_TYPE_ATM) {
			mType.setSelection(1);
			feedType = News.FEED_TYPE_ATOM;
		}
		//onyl two types supported atm, enforce that.

	}

	private void setCategories(String cats) {
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, new String[] { cats });
		adapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mChannelCats.setAdapter(adapter);
	}

	String getCategories() {
		return (String) mChannelCats.getSelectedItem();
	}

	private void setIcon(ImageView icon, String iconUri, int type) {

		if (iconUri == null || iconUri.equals("")) {

			if (type == News.CHANNEL_TYPE_ATM) {
				icon.setImageResource(R.drawable.atom_icon_small);
			} else if (type == News.CHANNEL_TYPE_RSS) {
				icon.setImageResource(R.drawable.rss_icon_small);
			}
		} else {
			try {
				final BitmapFactory.Options bmfo = new BitmapFactory.Options();
				bmfo.outHeight = 28;
				bmfo.outWidth = 28;
				icon.setImageBitmap(BitmapFactory.decodeFile(iconUri, bmfo));
			} catch (Exception e) {
				Log.e(_TAG, "error reading icon," + e.getMessage());
				if (type == News.CHANNEL_TYPE_ATM) {
					icon.setImageResource(R.drawable.atom_icon_small);
				} else if (type == News.CHANNEL_TYPE_RSS) {
					icon.setImageResource(R.drawable.rss_icon_small);
				}
			}

		}

	}

	/**
	 * Handles the intent that is launched by a browser.
	 */
	private void extractBrowserIntent() {
		Intent intent = getIntent();
		String type = intent.getType();
		String datastring = intent.getDataString();

		if ("application/rss+xml".equals(getIntent().getType())) {
			// RSS feed
			updateRSSChannel(datastring, true);
		} else if ("application/atom+xml".equals(getIntent().getType())) {
			// Atom feed
			updateAtomChannel(datastring, true);
		} else {
			// Unknown link
			mTitleSuggestion = null;
			mChannelLink.setText(datastring);
			mChannelLink.setEnabled(true);
			Log.i(_TAG, "extractBrowserIntent: Unknown MIME type: " + type);
		}
	}

	private void updateAtomChannel(String datastring, final boolean disableLink) {
		AtomSaxFetcherThread thread = new AtomSaxFetcherThread(datastring,
				mNews, this);
		updateChannel(datastring, disableLink, thread, 1);
	}

	private void updateChannel(String datastring, final boolean disableLink,
			AbstractFeedFetcherThread thread, final int typeSelection) {
		Log.v(_TAG, "update channel - type:" + typeSelection);
		InputStream is = thread.fetch();
		mTitleSuggestion = thread.parseTitle(is);
		Log.v(_TAG, "title suggestion:" + mTitleSuggestion);
		is = thread.fetch();
		if (is != null) {
			thread.parse(is);
			runOnUiThread(new Runnable() {
				public void run() {

					mChannelName.setText(mTitleSuggestion);
					if (disableLink) {
						mChannelLink.setEnabled(false);
					}
					mChannelName.requestFocus();
					mType.setSelection(typeSelection);

					// hide advanced settings.
					showAdvancedSettings(false);
				}
			});
		} else {
			runOnUiThread(new Runnable() {
				public void run() {
					Toast.makeText(ChannelSettings.this,
							getString(R.string.urlfailed), Toast.LENGTH_LONG)
							.show();

				}
			});
		}
	}

	private void updateRSSChannel(String datastring, final boolean disableLink) {

		AbstractFeedFetcherThread thread = new RSSSaxFetcherThread(datastring,
				mNews, this);
		updateChannel(datastring, disableLink, thread, 0);
	}

	// Save new Settings (overriding old) to datacontainer.
	private OnClickListener mSaveFeedSettings = new OnClickListener() {

		public void onClick(View arg0) {
			mCancle = false;
			createOrSave();

		}

	};

	private void editCats() {

		Intent i = new Intent(this,
				org.openintents.newsreader.categories.CategoriesListActivity.class);

		i.setAction(Intent.ACTION_EDIT);
		i.setData(getIntent().getData());
		Bundle b = new Bundle();
		b.putString(ChooseCategoriesDialog.CURRENT_CATS, (String) mChannelCats
				.getSelectedItem());
		i.putExtras(b);
		startActivityForResult(i, REQUEST_EDIT_CATS);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d(_TAG, "onActivityResult\n reqCode>" + requestCode + "<\nresCode>"
				+ resultCode + "<\n data>" + data + "\n------");

		if (requestCode == REQUEST_EDIT_CATS) {
			if (resultCode == RESULT_OK) {

				Bundle b = data.getExtras();
				if (b != null) {
					setCategories(b
							.getString(ChooseCategoriesDialog.CURRENT_CATS));
				}
			}
		}
	}

	// Cancel Method, Just Quit Activity
	private OnClickListener mCancelFeedSettings = new OnClickListener() {

		public void onClick(View arg0) {

			mCancle = true;
			ChannelSettings.this.setResult(Activity.RESULT_CANCELED);
			ChannelSettings.this.finish();
		}

	};
	private String mTitleSuggestion;
	protected ProgressDialog mProgressDialog;

	public void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();

		// get url from clip board
		if (TextUtils.isEmpty(mChannelLink.getText())) {
			ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			if (!TextUtils.isEmpty(cm.getText())) {
				mChannelLink.setText(cm.getText());

				// Check automatically:
				checkFeed();
			}
		}
	}

	private void preselectSpinner(String data) {
		//mUpdateCycle.setSelectedItem(mCursor.getString(mCursor.getColumnIndex(
		// News.RSSFeeds.UPDATE_CYCLE)));
		Log.d(_TAG,"preselectSpinner,data(1)>"+data);
		if (data == null || data.equals("")) {
			data = getString(R.string.never);
		}
		Log.d(_TAG,"preselectSpinner,data(2)>"+data);
		for (int i = 0; i < uCycles.length; i++) {
			if (data.equals(uCycles[i])) {
				mUpdateCycle.setSelection(i);
				return;
			}
		}

		// set to never
		mUpdateCycle.setSelection(uCycles.length);
	}

	private void saveDataSet() {
		String myLink = getChannelLink();

		ContentValues cv = new ContentValues();
		cv.put(Channel.CHANNEL_NAME, mChannelName.getText().toString());
		cv.put(Channel.CHANNEL_LINK, myLink);
		cv.put(Channel.UPDATE_CYCLE, getUpdateCycle());
		String categories = getCategories();
		cv.put(Channel.CHANNEL_CATEGORIES, categories);
		cv.put(Channel.NOTIFY_NEW, (mNotificationBox.isChecked() ? 1 : 0));
		cv.put(Channel.UPDATE_MSGS, (mUpdateBox.isChecked() ? 1 : 0));

		getContentResolver().update(
				Uri.withAppendedPath(Channel.CONTENT_URI, String
						.valueOf(feedID)), cv, null, null);

		insertIfNotExistsCategories(categories);
		Intent intent = new Intent(this, NewsreaderService.class);
		intent.putExtra(NewsreaderService.EXTRA_UPDATE_FEED, String
				.valueOf(feedID));
		startService(intent);
	}


	private String getChannelLink(){
		String myLink = mChannelLink.getText().toString();
		Log.d(_TAG, "myLink was>>" + myLink);
		if (myLink != null && !myLink.equals("")
				&& !myLink.startsWith("http://")) {

			myLink = "http://" + myLink;
			Log.d(_TAG, "myLink now>>" + myLink);
		}
		return myLink;
	}

	private void createDataSet() {

		ContentValues v = new ContentValues();
		String myLink = getChannelLink();
		String categories = getCategories();

		v.put(News.Channel.CHANNEL_NAME, mChannelName.getText().toString());
		v.put(News.Channel.CHANNEL_LINK, myLink);

		v.put(News.Channel.CHANNEL_CATEGORIES, categories);
		v.put(News.Channel.NOTIFY_NEW, (mNotificationBox.isChecked() ? 1 : 0));
		v.put(News.Channel.UPDATE_MSGS, (mUpdateBox.isChecked() ? 1 : 0));
		v.put(News.Channel.UPDATE_CYCLE, getUpdateCycle());

		if (feedType.equals(News.FEED_TYPE_RSS)) {
			v.put(News.Channel.CHANNEL_TYPE, News.CHANNEL_TYPE_RSS);
		} else if (feedType.equals(News.FEED_TYPE_ATOM)) {
			v.put(News.Channel.CHANNEL_TYPE, News.CHANNEL_TYPE_ATM);
		} else {
			// should never happen
			Log.e(_TAG, "Unrecognized Feed Type. How did tis happen?");
		}

		if (v.containsKey(News.Channel.CHANNEL_TYPE)) {
			cUri = mNews.insert(News.Channel.CONTENT_URI, v);
			feedID = Long.parseLong(cUri.getLastPathSegment());
			mCursor = managedQuery(cUri, CHANNEL_PROJECTION, null, null, null);
		} else {
			// TODO alert user, do not finish
		}

		insertIfNotExistsCategories(categories);
		Log.d(_TAG, "returned uri >>" + cUri + "<<");

	}

	private void insertIfNotExistsCategories(String categories) {
		if (TextUtils.isEmpty(categories)) {
			// early exit
			return;
		}

		String[] categoryArray = categories.split(News.CAT_DELIMITER);
		for (String cat : categoryArray) {

			ContentValues cs = new ContentValues();
			cs.put(Categories.NAME, cat);
			Uri result = mNews.insertIfNotExists(News.Categories.CONTENT_URI,
					"upper(" + Categories.NAME + ") = upper(?)",
					new String[] { cat }, cs);
			Log.v(_TAG, cat + " " + result);
		}

	}

	private String getUpdateCycle() {
		Log.d(_TAG,"getUpdateCycle: entering");
		String updateCycle = ((String) mUpdateCycle.getSelectedItem());
		if (getString(R.string.never).equals(updateCycle)) {
			updateCycle = null;
		}
		return updateCycle;
	}

	private void checkFeed() {

		mProgressDialog = ProgressDialog.show(ChannelSettings.this,
				getString(R.string.update),
				getString(R.string.looking_up_feed_information));

		new Thread() {
			@Override
			public void run() {

				ChannelParser parser = new ChannelParser(ChannelSettings.this);
				String rpc = getChannelLink();
				Log.v(_TAG, "url " + rpc);
				InputStream is = parser.fetch(rpc);
				Integer channelType = parser.parse(is);

				if (channelType != null) {
					if (channelType == News.CHANNEL_TYPE_ATM) {
						updateAtomChannel(rpc, false);
					} else if (channelType == News.CHANNEL_TYPE_RSS) {
						updateRSSChannel(rpc, false);
					} else {

						runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(ChannelSettings.this,
										getString(R.string.unknown_feed_type),
										Toast.LENGTH_LONG).show();

							}
						});
					}
				} else {

					runOnUiThread(new Runnable() {
						public void run() {
							Toast.makeText(ChannelSettings.this,
									getString(R.string.invalid_feed_url),
									Toast.LENGTH_LONG).show();

						}
					});
				}
				runOnUiThread(new Runnable() {

					public void run() {
						mProgressDialog.dismiss();

					}

				});
			}
		}.start();

	}

	private void createOrSave() {
		// Delete clipboard content
		ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		cm.setText(null);

		ChannelSettings.this.feedType = (String) ChannelSettings.this.mType
				.getSelectedItem();
		if (ChannelSettings.this.mState == ChannelSettings.STATE_CREATE) {

			ChannelSettings.this.createDataSet();
			ChannelSettings.this.setResult(Activity.RESULT_OK);
			ChannelSettings.this.finish();

		} else if (ChannelSettings.this.mState == ChannelSettings.STATE_EDIT) {

			ChannelSettings.this.saveDataSet();
			ChannelSettings.this.mCursor.close();
			ChannelSettings.this.setResult(Activity.RESULT_OK);
			ChannelSettings.this.finish();

		}
	}

}/* eoc */
