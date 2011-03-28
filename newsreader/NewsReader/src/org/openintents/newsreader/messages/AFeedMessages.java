package org.openintents.newsreader.messages;
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
import java.util.HashMap;

import org.openintents.newsreader.R;
import org.openintents.newsreader.services.AbstractFeedFetcherThread;
import org.openintents.newsreader.services.AtomSaxFetcherThread;
import org.openintents.newsreader.services.ChannelParser;
import org.openintents.newsreader.services.IconRetrieverThread;
import org.openintents.newsreader.services.NewsreaderService;
import org.openintents.newsreader.services.RSSSaxFetcherThread;
import org.openintents.provider.MagnoliaTagging;
import org.openintents.provider.News;
import org.openintents.provider.News.Channel;
import org.openintents.provider.News.Contents;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class AFeedMessages extends ListActivity {

	private static final int MENU_CHANNELITEM = 1001;
	private static final int MENU_CHANNELSETTINGS = 1002;
	private static final int MENU_TAGS = 1003;
	private static final int MENU_SCAN = 1004;
	private static final int MENU_SEARCH = 1005;
	private static final int MENU_MARK_ALL_AS_READ = 1006;

	private static final int SUBMENU_CHANNELITEM_TAG = 2001;
	private static final int SUBMENU_CHANNELITEM_DELETE = 2002;
	private static final int SUBMENU_CHANNELITEM_FOLLOW = 2003;
	private static final int SUBMENU_CHANNELITEM_MAGNOLIA = 2004;
	private static final int SUBMENU_CHANNELITEM_URL = 2005;
	private static final int SUBMENU_CHANNELITEM_SHOW_CONTENT = 2006;

	private static final int SUBMENU_DEBUG_1 = 6006;

	private static final int SUBMENU_CHANNELITEM_TOGGLE = 2007;

	private static final int SUBMENU_CHANNELSETTINGS_TAG = 3001;
	private static final int SUBMENU_CHANNELSETTINGS_EDITSETTINGS = 3002;
	private static final int SUBMENU_CHANNELSETTINGS_DELETEALL = 3003;

	private static final String _TAG = "AFeedMessages";
	protected static final int CONNECTIVITY_EVENT = 10000;

	private Cursor mCursor;

	private String feedID = "";
	private String feedName = "";

	private String channelType = "0";
	private News mNews;
	private String feedLink;

	private boolean offlineMode;
	private AlertDialog mCurrentMsgDialog;
	private String feedUpdateMsgs;
	private ConnectivityManager mConnectionManager;
	private int mCurrentPosition;
	private boolean notConnected;

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		setContentView(R.layout.messages);
		
		mNews = new News(getContentResolver());

		// Add footer
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout footer = (LinearLayout) inflater.inflate(
				R.layout.messages_footer, null);
		Button bt = (Button) footer.findViewById(R.id.button);
		bt.setOnClickListener(new OnClickListener() {

			public void onClick(View arg0) {
				scan();
			}

		});
		getListView().setItemsCanFocus(true);
		getListView().addFooterView(footer);

		mConnectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	private void checkOfflineMode() {
		String clickaction = PreferenceManager
				.getDefaultSharedPreferences(this).getString(
						News.PREFS_MESSAGE_CLICK_ACTION,
						News.PREF_VALUE_OFFLINE); // Default value is offline
		// if preference does not exist yet.

		Log.v(_TAG, "message click action " + clickaction);

		// Default is off:
		offlineMode = true;
		notConnected = false; // assume always online

		if (mConnectionManager != null) {
			NetworkInfo ni = mConnectionManager.getActiveNetworkInfo();
			if (ni != null) {
				Log.v(_TAG, ni.getTypeName());
				NetworkInfo.State nis = ni.getState();

				if (nis.equals(android.net.NetworkInfo.State.CONNECTED)
						|| nis.equals(android.net.NetworkInfo.State.CONNECTING)) {
					if (!News.PREF_VALUE_OFFLINE.equals(clickaction)) {
						offlineMode = false;
					}
					notConnected = false;
				} else {
					notConnected = true;
				}
			}
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Log.d(_TAG, "Clicked at >>" + position);

		if (offlineMode) {
			showOfflineItem(position);
		} else {
			followItemLink(position);
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle icicle) {
		super.onSaveInstanceState(icicle);
		if (mCurrentMsgDialog != null && mCurrentMsgDialog.isShowing()) {
			icicle.putInt(this.getClass().getName(), mCurrentPosition);
		}
		stopManagingCursor(mCursor);
		Log.d("AFEEDLIST", "onFreeze: entering");

	}

	@Override
	protected void onRestoreInstanceState(Bundle state) {
		super.onRestoreInstanceState(state);
		int position = state.getInt(this.getClass().getName(), -1);
		if (position >= 0) {
			showOfflineItem(position);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(_TAG, "onResume: entering");
		checkOfflineMode();
		init();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void init() {
		initFieldsFromIntent();

		mCursor = managedQuery(News.Contents.CONTENT_URI,
				News.Contents.PROJECTION_MAP, News.Contents.CHANNEL_ID + "="
						+ "=" + feedID, null, null);
		mCursor.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				AFeedMessages.this.setTitle(feedName + "(" + mCursor.getCount()
						+ ")");
			}
		});

		this.setTitle(feedName + "(" + mCursor.getCount() + ")");

		// always scan if no entries yet or
		// if feed is viewed directly (e.g. from browser)
		if (mCursor.getCount() == 0 || Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			scan();
		}

		// Use our own list adapter
		setListAdapter(new MessageListAdapter(mCursor, this, channelType));

		// Add context menu
		getListView().setOnCreateContextMenuListener(
				new View.OnCreateContextMenuListener() {

					public void onCreateContextMenu(ContextMenu contextmenu,
							View view, ContextMenu.ContextMenuInfo obj) {
						contextmenu.add(0,
								AFeedMessages.SUBMENU_CHANNELITEM_SHOW_CONTENT,
								0, R.string.showoffline).setIcon(
								android.R.drawable.ic_menu_info_details);

						contextmenu.add(0,
								AFeedMessages.SUBMENU_CHANNELITEM_DELETE, 0,
								R.string.delete).setIcon(
								android.R.drawable.ic_menu_delete);

						//contextmenu.add(0,AFeedMessages.SUBMENU_CHANNELITEM_TAG
						// ,"Tag
						// Local",R.drawable.tagging_application001a);
						// contextmenu.add(0,
						// AFeedMessages.SUBMENU_CHANNELITEM_MAGNOLIA, 0,
						// R.string.magnolia_tagging);
						// contextmenu.add(0, AFeedMessages.SUBMENU_DEBUG_1, 0,
						// "show debug info").setIcon(
						// android.R.drawable.ic_menu_info_details);

						/*
						 * android.view.SubMenu submenu;
						 * submenu=contextmenu.addSubMenu
						 * (0,AFeedMessages.MENU_TAGS,
						 * "Tags",R.drawable.tagging_application001a);
						 * //submenu.
						 * add(0,AFeedMessages.SUBMENU_CHANNELITEM_TAG,"Tag
						 * Local",R.drawable.tagging_application001a);
						 * submenu.add
						 * (0,AFeedMessages.SUBMENU_CHANNELITEM_MAGNOLIA
						 * ,"Magnolia"
						 * ,R.drawable.tagging_magnolia_application001a);
						 */
					}

				});

		/*
		 * // Add click action getListView().setOnItemClickListener( new
		 * AdapterView.OnItemClickListener() {
		 * 
		 * @Override public void onItemClick(AdapterView parent, View v, int
		 * position, long id) { // Clicking an item starts editing it
		 * followItemLink(position); }
		 * 
		 * });
		 */
	}

	private void initFieldsFromIntent() {
		if (TextUtils.isEmpty(getIntent().getAction())
				&& getIntent().getExtras() != null) {
			Bundle b = getIntent().getExtras();
			// get channel info
			channelType = b.getString(News.Channel.CHANNEL_TYPE);
			feedID = b.getString(News.Channel._ID);
			feedName = b.getString(News.Channel.CHANNEL_NAME);
			feedLink = b.getString(News.Channel.CHANNEL_LINK);
			feedUpdateMsgs = b.getString(News.Channel.UPDATE_MSGS);
			Log.v(_TAG, "bundle: " + channelType + " " + " " + feedID + " "
					+ feedName + " " + feedLink);
			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
					.cancel(NewsreaderService.NEW_NEWS_NOTIFICATION);
		} else if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
			feedID = mNews.findChannel(Channel.CONTENT_URI, getIntent()
					.getDataString());
			feedLink = getIntent().getDataString();
			int channelTypeInt = getChannelType(getIntent().getType());
			channelType = String.valueOf(channelTypeInt);

			Log.v(_TAG, "view: " + channelType + " " + " " + feedID + " "
					+ feedName + " " + feedLink);

			if (feedID == null) {
				HashMap map = new HashMap();
				map.put(News.Channel.CHANNEL_LINK, feedLink);

				AbstractFeedFetcherThread thread = null;
				if (channelTypeInt == News.CHANNEL_TYPE_ATM) {
					thread = new AtomSaxFetcherThread(map, mNews, this);

				} else if (channelTypeInt == News.CHANNEL_TYPE_RSS) {
					thread = new RSSSaxFetcherThread(map, mNews, this);
				} else {

				}

				if (thread != null) {
					InputStream is = thread.fetch();
					feedName = thread.parseTitle(is);
				} else {
					feedName = getString(R.string.feedfromurl);
				}

				ContentValues cv = new ContentValues();
				cv.put(Channel.CHANNEL_LINK, feedLink);
				cv.put(Channel.CHANNEL_TYPE, channelType);
				cv.put(Channel.CHANNEL_NAME, feedName);
				// default is 0 (= insertOnly)
				feedUpdateMsgs = String.valueOf(0);
				cv.put(News.Channel.UPDATE_MSGS, feedUpdateMsgs);

				// default is 0 (= no notification)
				cv.put(News.Channel.NOTIFY_NEW, String.valueOf(0));

				Uri newUri = mNews.insert(Channel.CONTENT_URI, cv);
				feedID = newUri.getLastPathSegment();
				Log.v(_TAG, "new id: " + channelType + " " + " " + feedID + " "
						+ feedName + " " + feedLink);

			} else {
				Cursor cursor = getContentResolver().query(
						Uri.withAppendedPath(Channel.CONTENT_URI, feedID),
						new String[] { Channel._ID, Channel.CHANNEL_TYPE,
								Channel.UPDATE_MSGS }, null, null, null);
				cursor.moveToFirst();
				channelType = cursor.getString(1);
				feedUpdateMsgs = cursor.getString(2);
				cursor.close();

				Log.v(_TAG, "old id: " + channelType + " " + " " + feedID + " "
						+ feedName + " " + feedLink);
			}			
		} else {
			Log.v(_TAG, "unsupported intent:" + getIntent());
			feedID = "-1";
		}

	}

	private int getChannelType(String type) {
		int result;
		if ("application/rss+xml".equals(type)) {
			// RSS feed
			result = News.CHANNEL_TYPE_RSS;
		} else if ("application/atom+xml".equals(type)) {
			// Atom feed
			result = News.CHANNEL_TYPE_RSS;
		} else {
			ChannelParser parser = new ChannelParser(this);
			String rpc = feedLink;
			Log.v(_TAG, "url " + rpc);
			InputStream is = parser.fetch(rpc);
			Integer cType = parser.parse(is);
			if (cType == null) {
				result = News.CHANNEL_TYPE_UNSUPPORTED;
			} else {
				result = cType;
			}
		}
		return result;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.v("AFeedMessages", "onCreateOptionsMenu:entering");

		boolean result = super.onCreateOptionsMenu(menu);
		menu.add(0, AFeedMessages.MENU_SCAN, 0, R.string.update).setIcon(		
				R.drawable.ic_menu_refresh);
		menu.add(0, AFeedMessages.MENU_MARK_ALL_AS_READ, 0, R.string.markallasread)
			.setIcon(android.R.drawable.ic_menu_view);
		return result;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.v(_TAG, "onOptionsItemSelected: item.id>>" + item.getItemId()
				+ "<<");

		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case MENU_SCAN:

			scan();

			break;
		case MENU_MARK_ALL_AS_READ:
			markAllAsRead();
			break;
		}
		return true;
	}

	/**
	 * requires feedId, feedLink and channelType
	 */
	private void scan() {
		setProgressBarIndeterminateVisibility(true);
		// mProgressDialog = ProgressDialog.show(this,
		// getString(R.string.update),
		// getString(R.string.update));
		new Thread() {
			@Override
			public void run() {

				HashMap<String, String> data = new HashMap();
				data.put(News.Channel._ID, feedID);
				data.put(News.Channel.CHANNEL_LINK, feedLink);
				data.put(News.Channel.UPDATE_MSGS, feedUpdateMsgs);

				AbstractFeedFetcherThread rt = null;
				if (String.valueOf(News.CHANNEL_TYPE_RSS).equals(channelType)) {
					rt = new RSSSaxFetcherThread(data, mNews,
							AFeedMessages.this);

				} else if (String.valueOf(News.CHANNEL_TYPE_ATM).equals(
						channelType)) {
					rt = new AtomSaxFetcherThread(data, mNews,
							AFeedMessages.this);
				} else {
					Log.d(_TAG, "unsupported type " + channelType);
				}
				if (rt != null) {
					InputStream is = rt.fetch();
					if (is != null) {
						rt.parse(is);
					} else {
						runOnUiThread(new Runnable() {
							public void run() {
								Toast.makeText(AFeedMessages.this,
										getString(R.string.urlfailed),
										Toast.LENGTH_LONG).show();

							}
						});
					}
				}
				runOnUiThread(new Runnable() {
					public void run() {
						setProgressBarIndeterminateVisibility(true);
						// mProgressDialog
						// .setMessage(getString(R.string.checkicon));
					}
				});

				data = new HashMap();
				data.put(News.Channel._ID, feedID);
				data.put(News.Channel.CHANNEL_NAME, feedName);
				data.put(News.Channel.CHANNEL_LINK, feedLink);
				data.put(News.Channel.CHANNEL_TYPE, channelType);
				IconRetrieverThread iconThread = new IconRetrieverThread(
						AFeedMessages.this, data);

				String is = iconThread.getUri();
				iconThread.downloadIcon(is);

				runOnUiThread(new Runnable() {
					public void run() {
						setProgressBarIndeterminateVisibility(false);
						// mProgressDialog.dismiss();
					}
				});

			}
		}.start();

	}

	private void markAllAsRead(){
		
		mNews.markFeedAsRead(Long.parseLong(feedID));
		
	}



	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case SUBMENU_CHANNELITEM_FOLLOW:
			followItemLink(menuInfo.position);
			break;
		case SUBMENU_CHANNELITEM_SHOW_CONTENT:
			showOfflineItem(menuInfo.position);
			break;
		case SUBMENU_CHANNELITEM_TOGGLE:
			((MessageListAdapter) getListAdapter()).toggle(menuInfo.position);
			break;
		case SUBMENU_CHANNELITEM_DELETE:
			menuDelete(menuInfo.id);
			break;
		case SUBMENU_CHANNELITEM_TAG:
			menuTag(menuInfo.position);
			break;
		case SUBMENU_CHANNELITEM_MAGNOLIA:
			menuMagnolia(menuInfo.position);
			break;
		case SUBMENU_DEBUG_1:
			showDebugInfo(menuInfo.position);
			break;
		}

		return true;
	}

	private void showDebugInfo(int position) {
		Cursor cursor = (Cursor) getListAdapter().getItem(position);
		String pubDate = cursor.getString(cursor
				.getColumnIndexOrThrow(Contents.ITEM_PUB_DATE));
		String created = cursor.getString(cursor
				.getColumnIndexOrThrow(Contents.CREATED_ON));
		String id = cursor
				.getString(cursor.getColumnIndexOrThrow(Contents._ID));
		Toast.makeText(
				this,
				"Pub date: " + pubDate + "\n created: " + created + " id: "
						+ id, Toast.LENGTH_LONG).show();

	}

	private void showOfflineItem(int position) {
		Log.v(_TAG, "show offline " + position);
		
		if (mCursor == null) {
			init();
		}

		mCursor.moveToPosition(position);
		SingleMessageWebView view = new SingleMessageWebView(this);
		CharSequence titel = setWebViewMessage(view, mCursor);

		Builder b = new AlertDialog.Builder(this).setView(view).setTitle(titel);

		setCurrentMessageToRead(mCursor);

		// get icon uri
		Cursor channelC = getContentResolver().query(
				Uri.withAppendedPath(Channel.CONTENT_URI, feedID),
				new String[] { Channel._ID, Channel.CHANNEL_ICON_URI }, null,
				null, null);
		String iconUri = null;
		if (channelC.moveToFirst()) {
			iconUri = channelC.getString(1);
		}
		// set dialog icon
		channelC.close();
		setIcon(b, iconUri, Integer.parseInt(channelType));

		// show dialog
		mCurrentMsgDialog = b.show();
		mCurrentPosition = position;
	}

	private void setCurrentMessageToRead(Cursor cursor) {
		ContentValues cv = new ContentValues();
		cv.put(News.Contents.READ_STATUS, News.STATUS_READ);
		mNews.update(Uri.withAppendedPath(Contents.CONTENT_URI, cursor
				.getString(mCursor.getColumnIndexOrThrow(News.Contents._ID))),
				cv, null, null);
	}

	private CharSequence setWebViewMessage(final SingleMessageWebView view,
			Cursor cursor) {
		CharSequence content = cursor.getString(cursor
				.getColumnIndexOrThrow(News.Contents.ITEM_CONTENT));
		CharSequence contentType = cursor.getString(cursor
				.getColumnIndexOrThrow(News.Contents.ITEM_CONTENT_TYPE));
		CharSequence titel = cursor.getString(cursor
				.getColumnIndexOrThrow(News.Contents.ITEM_TITLE));
		Log.v(_TAG, "show offline " + content + " " + titel);

		if (News.CONTENT_TYPE_G.equals(contentType) && !notConnected) {			
			String link = cursor.getString(cursor
					.getColumnIndexOrThrow(News.Contents.ITEM_LINK));
			view.loadUrl(link);

		} else {
			/*
			view.loadUrl(Uri.withAppendedPath(
					Contents.CONTENT_URI,
					cursor.getString(cursor
							.getColumnIndexOrThrow(News.Contents._ID)))
					.toString());
			*/
			
			// Load data specifying encoding:
			
			Uri uri = Uri.withAppendedPath(
					Contents.CONTENT_URI,
					cursor.getString(cursor
							.getColumnIndexOrThrow(News.Contents._ID)));
			
			// Obtain the data from the content provider
			String[] projection = new String[] { Contents._ID,
					Contents.ITEM_CONTENT };
			Cursor c = getContentResolver().query(uri, projection, null, null, null);
			c.moveToFirst();
			String data = c.getString(1);
			c.close();
			
			// Internal strings are always stored as UTF-8, so the encoding is UTF-8:
			view.loadDataWithBaseURL(uri.toString(), data, "text/html", "utf-8", null);
		}
		view.setMessageLink(cursor.getString(cursor
				.getColumnIndexOrThrow(News.Contents.ITEM_LINK)));
		view.disableButtons(cursor.isFirst(), cursor.isLast());
		return titel;
	}

	private void setIcon(Builder b, String iconUri, int type) {

		if (iconUri == null || iconUri.equals("")) {

			if (type == News.CHANNEL_TYPE_ATM) {
				b.setIcon(R.drawable.atom_icon_small);
			} else if (type == News.CHANNEL_TYPE_RSS) {
				b.setIcon(R.drawable.rss_icon_small);
			}
		} else {
			try {
				b.setIcon(Drawable.createFromPath(iconUri));
			} catch (Exception e) {
				Log.e(_TAG, "error reading icon," + e.getMessage());
				if (type == News.CHANNEL_TYPE_ATM) {
					b.setIcon(R.drawable.atom_icon_small);
				} else if (type == News.CHANNEL_TYPE_RSS) {
					b.setIcon(R.drawable.rss_icon_small);
				}
			}

		}

	}

	private void followItemLink(int position) {

		int pos = position;
		Log.d(_TAG, "followItemLink: pos>>" + pos + "<<");
		if (pos > -1) {

			mCursor.moveToPosition(pos);
			Uri uri = null;

			String strUri = mCursor.getString(mCursor
					.getColumnIndexOrThrow(News.Contents.ITEM_LINK));
			uri = Uri.parse(strUri);

			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			startActivity(intent);
		}
	}

	private void menuDelete(long l) {

		int result = getContentResolver().delete(
				Uri.withAppendedPath(Contents.CONTENT_URI, String.valueOf(l)),
				null, null);
		Log.d(_TAG, " deletet row " + l + " >" + result);

	}

	private void menuTag(int position) {
	}

	private void menuMagnolia(int pos) {
		// setSelection(position);

		Bundle b = new Bundle();
		// int pos=getSelectedItemPosition();
		if (pos > -1) {

			mCursor.moveToPosition(pos);
			String strUri = "";
			String desc = "";

			strUri = mCursor.getString(mCursor
					.getColumnIndexOrThrow(News.Contents.ITEM_LINK));
			desc = mCursor.getString(mCursor
					.getColumnIndexOrThrow(News.Contents.ITEM_TITLE));

			b.putString(MagnoliaTagging.URI, strUri);
			b.putString(MagnoliaTagging.DESCRIPTION, desc);
			Intent intent = new Intent();
			intent.setAction(MagnoliaTagging.TAGACTION);
			intent.addCategory(Intent.CATEGORY_DEFAULT);
			intent.putExtras(b);
			startActivity(intent);
		}
	}

	public void showPrevious(SingleMessageWebView view) {
		mCursor.moveToPosition(mCurrentPosition);
		if (!mCursor.isFirst()) {
			mCursor.moveToPrevious();
			CharSequence title = setWebViewMessage(view, mCursor);
			mCurrentMsgDialog.setTitle(title);
			setCurrentMessageToRead(mCursor);
			mCurrentPosition--;
		}

	}

	public void showNext(SingleMessageWebView view) {
		mCursor.moveToPosition(mCurrentPosition);
		if (!mCursor.isLast()) {
			mCursor.moveToNext();
			CharSequence title = setWebViewMessage(view, mCursor);
			mCurrentMsgDialog.setTitle(title);
			setCurrentMessageToRead(mCursor);
			mCurrentPosition++;
		}

	}

}/* eoc */
