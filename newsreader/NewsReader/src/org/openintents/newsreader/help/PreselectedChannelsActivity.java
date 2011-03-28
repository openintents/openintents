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

package org.openintents.newsreader.help;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.openintents.lib.HTTPUtils;
import org.openintents.newsreader.R;
import org.openintents.provider.News;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.XmlResourceParser;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TwoLineListItem;
import android.widget.ExpandableListView.ExpandableListContextMenuInfo;

/**
 * Demonstrates expandable lists using a custom {@link ExpandableListAdapter}
 * from {@link BaseExpandableListAdapter}.
 */
public class PreselectedChannelsActivity extends ExpandableListActivity {
	private static final String TAG = "PreselectedChannelsActivity";

	private static final int MENU_CREATE_MANUALLY = Menu.FIRST;

	private static final int MENU_SEARCH = MENU_CREATE_MANUALLY + 1;

	//for falling back
	private static final Uri DEFAULT_FEEDS_DIRECTORY = Uri
			.parse("http://www.rssfeeds.com");

	
	private Uri FEEDS_DIRECTORY=null;
	
	private static final int DIALOG_SEARCH_WEB_HELP = 1;

	private static final String DEFAULT_FEEDS = "http://www.openintents.org/newsreader/defaultfeeds";

	ExpandableListAdapter mAdapter;

	static PreselectedChannels mPreselectedChannels;

	private TextView mChannel;
	private LinearLayout mClipboardContent;
	private TextView mClipboardTitle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Header has to be added before setListAdapter() is called.
		addListHeader();

		// Set up our adapter
		// setAdapter(new PreselectedChannels(new News(getContentResolver())));

		if (mPreselectedChannels == null) {
			// load channels from server or assets
			final ProgressDialog pd = ProgressDialog.show(this,
					getString(R.string.update),
					getString(R.string.looking_up_default_feeds));
			Thread t = new Thread() {
				@Override
				public void run() {
					PreselectedChannelsParser pcp = new PreselectedChannelsParser(
							new News(getContentResolver()));
					final PreselectedChannels channels = getPreselectedChannels(pcp);
					
					if (channels != null) {
						runOnUiThread(new Runnable() {
							public void run() {
								setAdapter(channels);
							};

						});

					}
					runOnUiThread(new Runnable() {
						public void run() {
							pd.dismiss();
						}
					});

				}
			};

			t.start();
		} else {
			// set channels immediately
			setAdapter(mPreselectedChannels);
		}

		/*
		 * // Play around with setting check marks -> Has to be done by hand.
		 * 
		 * ExpandableListView explist = getExpandableListView();
		 * //explist.setChoiceMode(ExpandableListView.CHOICE_MODE_MULTIPLE);
		 * long packed = explist.getPackedPositionForChild(0, 0); Log.i(TAG,
		 * "Packed: " + packed); explist.setItemChecked(1, true);
		 */

		registerForContextMenu(getExpandableListView());

		try
		{//sanity check for localized uris
			FEEDS_DIRECTORY=Uri.parse(
				new java.net.URL(getString(R.string.online_feeds_dir)).toString()
			);
			
		}
		catch (java.net.MalformedURLException mue)
		{
			Log.e(TAG,"Feeds Dir from strings was not a valid url");
			FEEDS_DIRECTORY=DEFAULT_FEEDS_DIRECTORY;
		}
			
	}

	private void setAdapter(PreselectedChannels preselectedChannels) {
		mPreselectedChannels = preselectedChannels;
		mAdapter = new SimpleExpandableListAdapter(
				this,
				(List) mPreselectedChannels.categories,
				android.R.layout.simple_expandable_list_item_1,
				android.R.layout.simple_expandable_list_item_1, // .._item_2 if
				// there are
				// descriptions
				new String[] { mPreselectedChannels.NAME,
						mPreselectedChannels.DESCRIPTION }, new int[] {
						android.R.id.text1, android.R.id.text2 },
				(List) mPreselectedChannels.feeds,
				android.R.layout.simple_expandable_list_item_1, // .._item_2 if
				// there are
				// descriptions
				new String[] { mPreselectedChannels.NAME,
						mPreselectedChannels.DESCRIPTION }, new int[] {
						android.R.id.text1, android.R.id.text2 }) {
			@Override
			public View getChildView(int groupPosition, int childPosition,
					boolean isLastChild, View convertView, ViewGroup parent) {

				TextView view = (TextView) super.getChildView(groupPosition,
						childPosition, isLastChild, convertView, parent);
				if (mPreselectedChannels.isSubscribed(groupPosition,
						childPosition)) {
					view.setTextColor(Color.GRAY);
				} else {
					// TODO look for a better way to unset the color.
					view.setTextColor(Color.WHITE);
				}
				return view;
			}
		};
		setListAdapter(mAdapter);		
	}

	private PreselectedChannels getPreselectedChannels(
			PreselectedChannelsParser pcp) {

		PreselectedChannels preselectedChannels = null;
		// use url from server
		try {
			InputStream in = HTTPUtils.open(DEFAULT_FEEDS);
			preselectedChannels = pcp.fromXML(in);
		} catch (XmlPullParserException e) {
			Log
					.e(
							TAG,
							"PreselectedChannelsActivity: XmlPullParserException",
							e);
		} catch (IOException e) {
			Log.e(TAG, "PreselectedChannelsActivity: IOException", e);
		}

		if (preselectedChannels == null) {

			// use resource xml
			XmlResourceParser in = this.getResources().getXml(
					R.xml.defaultfeeds);

			try {
				preselectedChannels = pcp.fromXMLResource(in);
			} catch (XmlPullParserException e) {
				Log.e(TAG,
						"PreselectedChannelsActivity: XmlPullParserException",
						e);
				throw new RuntimeException(
						"PreselectedChannelsActivity: XmlPullParserException");
			} catch (IOException e) {
				Log.e(TAG, "PreselectedChannelsActivity: IOException", e);
				throw new RuntimeException(
						"PreselectedChannelsActivity: IOException");
			}
		}

		return preselectedChannels;
	}

	/**
	 * Adds a header to the list.
	 */
	private void addListHeader() {
		ExpandableListView expandableListView = this.getExpandableListView();

		// Add footer
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout header = (LinearLayout) inflater.inflate(
				R.layout.newsreader_preselectedfeedheader, null);
		expandableListView.setItemsCanFocus(true);
		expandableListView.addHeaderView(header, null, true);
		Button button = (Button) header.findViewById(R.id.button);

		button.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View view) {
				showDialog(DIALOG_SEARCH_WEB_HELP);
			}
		});

		mChannel = (TextView) header.findViewById(R.id.channel);

		button = (Button) header.findViewById(R.id.button_clipboard);

		button.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View view) {
				Intent i = new Intent(PreselectedChannelsActivity.this,
						org.openintents.newsreader.channels.ChannelSettings.class);
				// startSubActivity(i,AFeedSettings.ACTIVITY_CREATE);
				i.setAction(Intent.ACTION_INSERT);
				// i.setData(getIntent().getData());
				startActivity(i);
			}
		});

		mClipboardContent = (LinearLayout) header.findViewById(R.id.clipboard);

		mClipboardTitle = (TextView) header.findViewById(R.id.clipboard_title);

	}

	@Override
	protected void onResume() {
		super.onResume();

		// get url from clip board
		ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
		CharSequence clipboard = cm.getText();
		if (!TextUtils.isEmpty(clipboard)) {
			mClipboardContent.setVisibility(View.VISIBLE);
			mClipboardTitle.setText(R.string.clipboard_content);
			mChannel.setText(clipboard);
		} else {
			mClipboardContent.setVisibility(View.GONE);
			mClipboardTitle.setText(R.string.clipboard_empty);
		}

	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_SEARCH_WEB_HELP:
			return new AlertDialog.Builder(this)
			// .setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.search_web_help).setPositiveButton(
							R.string.open_browser,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									/* User clicked OK so do some stuff */

									Intent intent = new Intent(
											Intent.ACTION_VIEW, FEEDS_DIRECTORY);
									PreselectedChannelsActivity.this
											.startActivity(intent);
								}
							}).setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									/* User clicked Cancel so do some stuff */
								}
							}).create();
		}
		return null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_CREATE_MANUALLY, 0, R.string.addfeed_manually)
				.setIcon(android.R.drawable.ic_menu_add);

		menu.add(0, MENU_SEARCH, 0, R.string.search_web).setIcon(
				android.R.drawable.ic_menu_search);

		return result;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log
				.v(TAG, "onOptionsItemSelected: item.id>>" + item.getItemId()
						+ "<<");

		switch (item.getItemId()) {
		case MENU_CREATE_MANUALLY:
			createFeedManually();
			break;
		case MENU_SEARCH:
			showDialog(DIALOG_SEARCH_WEB_HELP);
			// Intent intent = new Intent(Intent.ACTION_VIEW, FEEDS_DIRECTORY);
			// startActivity(intent);
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void createFeedManually() {
		Intent i = new Intent(this,
				org.openintents.newsreader.channels.ChannelSettings.class);
		// startSubActivity(i,AFeedSettings.ACTIVITY_CREATE);
		i.setAction(Intent.ACTION_INSERT);
		i.setData(getIntent().getData());
		startActivity(i);
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		// Log.i(TAG, "Clicked: " + groupPosition + ", " + childPosition + ", "
		// + id);

		// Register the feed
		Uri newUri = mPreselectedChannels.addFeed(groupPosition, childPosition);
		if (newUri != null) {
			mPreselectedChannels.addCategory(this, groupPosition);

			// Show toast
			String feedname = mPreselectedChannels.getFeedName(groupPosition,
					childPosition);
			String toastMsg = getString(R.string.feedadded, feedname);
			((TextView) v).setTextColor(Color.GRAY);
			Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
		} else {
			// exists already
		}
		return true;
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		// menu.setHeaderTitle("Sample feeds");
		// menu.add(0, 0, 0, "Add");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// TODO currently no context menu, see createContextMenu
		ExpandableListContextMenuInfo info = (ExpandableListContextMenuInfo) item
				.getMenuInfo();

		String title = "";
		if (info.targetView instanceof TextView) {
			title = ((TextView) info.targetView).getText().toString();
		} else if (info.targetView instanceof TwoLineListItem) {
			title = ((TwoLineListItem) info.targetView).getText1().getText()
					.toString();
		}

		int type = ExpandableListView
				.getPackedPositionType(info.packedPosition);
		if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
			int groupPos = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);
			int childPos = ExpandableListView
					.getPackedPositionChild(info.packedPosition);
			Toast.makeText(
					this,
					title + ": Child " + childPos + " clicked in group "
							+ groupPos, Toast.LENGTH_SHORT).show();
			return true;
		} else if (type == ExpandableListView.PACKED_POSITION_TYPE_GROUP) {
			int groupPos = ExpandableListView
					.getPackedPositionGroup(info.packedPosition);
			Toast.makeText(this, title + ": Group " + groupPos + " clicked",
					Toast.LENGTH_SHORT).show();
			return true;
		}

		return false;
	}

}
