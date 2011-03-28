package org.openintents.newsreader.reader;
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

import org.openintents.lib.Update;
import org.openintents.newsreader.About;
import org.openintents.newsreader.R;
import org.openintents.newsreader.categories.CategorieChangeView;
import org.openintents.newsreader.categories.CategorieChangeView.OnCategorieChangeListener;
import org.openintents.newsreader.channels.ChannelListAdapter;
import org.openintents.newsreader.channels.SearchSuggestionProvider;
import org.openintents.newsreader.help.PreselectedChannelsActivity;
import org.openintents.newsreader.messages.AFeedMessages;
import org.openintents.provider.News;
import org.openintents.provider.News.Channel;

import android.app.Activity;
import android.app.SearchManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.SearchRecentSuggestions;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Activity with list of feeds, menu to add feeds and start feed reader service.
 * 
 */
public class Newsreader extends Activity implements OnCategorieChangeListener {
	
	private static final boolean debug = false;

	// Define Menu Constants
	public static final int MENU_CREATE_MANUALLY = 1001;
	public static final int MENU_DELETE = 1002;
	public static final int MENU_SETTINGS = 1003;
	public static final int MENU_EDIT = 1004;
	private static final int MENU_SEARCH = 1005;
	public static final int MENU_CREATE = 1006;
	private static final int MENU_INFO = 1007;
	private static final int MENU_MARK_AS_READ = 1008;
        private static final int MENU_UPDATEALL=1009;

	public static final String _TAG = "Newsreader";

	private static int SUBACTIVITY_PRESELECTED_CHANNELS = 1;

	private static final String SELECTED_CATEGORY="selCat";
	private static final String CURRENT_SEARCH="curSearch";
	
	private Cursor mChannelsCursor;
	private ListView mChannelsView;

	private News mNews;

	private String mSearchTerm;

	private String[] mSelectionArgs;

	private String mSelection;

	private String mCatname;

	private SharedPreferences mSettings;

	CategorieChangeView ccv;

	
	    
	@Override
	public void onCreate(Bundle icicle) {
		//Application.checkDeadline(this);
		Update.check(this);

		mNews = new News(getContentResolver());
		this.setTitle(R.string.newsreader);

		setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

		super.onCreate(icicle);
		
		checkEULA();

		initSearchFromIntent(getIntent());

		mSettings = PreferenceManager.getDefaultSharedPreferences(this);

		setContentView(R.layout.newsreader);

		mChannelsView = (ListView) findViewById(R.id.newsreader_listitems);
		mChannelsView.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView parent, View v, int position,
					long id) {
				parent.setSelection(position);
				Newsreader.this.openChannel(position);
			}

		});
		// Add context menu
		mChannelsView
				.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {

					public void onCreateContextMenu(ContextMenu contextmenu,
							View view, ContextMenu.ContextMenuInfo obj) {
						contextmenu.add(0, MENU_EDIT, 0, R.string.editfeed);
						contextmenu.add(0, MENU_MARK_AS_READ, 0, R.string.markfeedasread);
						contextmenu.add(0, MENU_DELETE, 0, R.string.deletefeed);
					}

				});

		// Add footer
		LayoutInflater inflater = (LayoutInflater) this
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		LinearLayout footer = (LinearLayout) inflater.inflate(
				R.layout.newsreader_feedfooter, null);
		mChannelsView.setItemsCanFocus(true);
		mChannelsView.addFooterView(footer, null, true);
		Button button = (Button) footer.findViewById(R.id.button);

		button.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View view) {
				addFeeds();
			}
		});

		ccv = new CategorieChangeView(this);
		ccv.setOnCategorieChangeListener(this);
		LayoutParams params = new AbsListView.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		ccv.setLayoutParams(params);
		// Add header
		mChannelsView.addHeaderView(ccv, null, false);
		updateChannelsList(null, mSearchTerm);
	}
	
	/**
	 * Test whether EULA has been accepted. Otherwise display EULA.
	 */
	private void checkEULA() {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
		boolean accepted = sp.getBoolean(EulaActivity.PREFERENCES_EULA_ACCEPTED, false);
		
		if (accepted) {
			if (debug) Log.i(_TAG, "Eula has been accepted.");
		} else {
			if (debug) Log.i(_TAG, "Eula has not been accepted yet.");
			Intent i = new Intent(this, EulaActivity.class);
			startActivity(i);
			finish();
		}
	}

	private void initSearchFromIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			mSearchTerm = intent.getStringExtra(SearchManager.QUERY);

			// Record the query string in the recent queries suggestions
			// provider.
			SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
					this, SearchSuggestionProvider.AUTHORITY,
					SearchSuggestionProvider.MODE);
			suggestions.saveRecentQuery(mSearchTerm, null);

		} else {
			mSearchTerm = null;
		}
	}

	private void addFeeds() {
		Intent intent = new Intent(this, PreselectedChannelsActivity.class);
		startActivityForResult(intent, SUBACTIVITY_PRESELECTED_CHANNELS);
	}

	@Override
	protected void onNewIntent(Intent intent1) {

		if (debug) Log.v(_TAG, intent1.getAction());
		super.onNewIntent(intent1);
		initSearchFromIntent(intent1);
		updateChannelsList(mCatname, mSearchTerm);
	}

	/*
	 * 
	 * private void calculateListSize(){ Log.d(_TAG,"calculateListSize:
	 * entering"); // int rssCount=mRSSListView.getAdapter().getCount(); int
	 * rssCount=4; if (rssCount==0){rssCount=1;} / int
	 * atomCount=mAtomListView.getAdapter().getCount(); if
	 * (atomCount==0){atomCount=1;}
	 */
	/*
	 * int iHeight=12;
	 * 
	 * mLayoutParams= new LinearLayout.LayoutParams(
	 * LinearLayout.LayoutParams.FILL_PARENT,
	 * LinearLayout.LayoutParams.WRAP_CONTENT); mLayoutParams.width=130;
	 * mLayoutParams.height=iHeightrssCount;
	 * 
	 * mRSSListView.setLayoutParams(mLayoutParams); /
	 * mLayoutParams.height=iHeightatomCount;
	 * mAtomListView.setLayoutParams(mLayoutParams);
	 */
	/*
	 * Log.d(_TAG,"calculateListSize: leaving"); }
	 */

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (debug) Log.v(_TAG, "onOptionsItemSelected: item.id>>" + item.getItemId()
				+ "<<");

		switch (item.getItemId()) {
		case MENU_CREATE_MANUALLY:
			menuCreate();
			break;
		case MENU_CREATE:
			addFeeds();
			break;
		case MENU_SETTINGS:
			menuSettings();
			break;
		case MENU_SEARCH:
			onSearchRequested();
			break;
		case MENU_INFO:
			showInfo();
			break;
			
		case MENU_UPDATEALL:
		      updateAll();
		      break;
		}
		return super.onOptionsItemSelected(item);
	}

	private void showInfo() {
		startActivity(new Intent(this, About.class));
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item
				.getMenuInfo();
		switch (item.getItemId()) {
		case MENU_EDIT:
			menuEdit(menuInfo.position);
			break;
		case MENU_DELETE:
			menuDelete(menuInfo.position);
			break;
		case MENU_MARK_AS_READ:
			menuMarkAsRead(menuInfo.position);
			break;
		}

		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = super.onCreateOptionsMenu(menu);

		//menu.add(0, MENU_CREATE_MANUALLY, 0, R.string.addfeed_manually)
		//		.setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, MENU_CREATE, 0, R.string.addfeed).setIcon(
				android.R.drawable.ic_menu_add);
		menu.add(0, MENU_SEARCH, 0, R.string.search).setIcon(
				android.R.drawable.ic_search_category_default);
		menu.add(0, MENU_SETTINGS, 0, R.string.settings).setIcon(
				R.drawable.ic_menu_preferences);	//android.R.drawable.ic_menu_preferences);		
		menu.add(0, MENU_INFO, 0, R.string.about).setIcon(
				android.R.drawable.ic_menu_info_details);
		menu.add(0,MENU_UPDATEALL,0,R.string.updateall).setIcon(
				R.drawable.ic_menu_refresh);
		return result;

	}

	private void menuCreate() {
		if (debug) Log.v(_TAG, "menuCreate:entering");

		Intent i = new Intent(this,
				org.openintents.newsreader.channels.ChannelSettings.class);
		// startSubActivity(i,AFeedSettings.ACTIVITY_CREATE);
		i.setAction(Intent.ACTION_INSERT);
		i.setData(getIntent().getData());
		startActivity(i);
	}

	private void menuEdit(int position) {
		// first is header ?
		mChannelsCursor.moveToPosition(position - 1);
		int feedType = mChannelsCursor.getInt(mChannelsCursor
				.getColumnIndexOrThrow(News.Channel.CHANNEL_TYPE));
		String feedname = mChannelsCursor.getString(mChannelsCursor
				.getColumnIndexOrThrow(News.Channel.CHANNEL_TYPE));
		long _id = mChannelsCursor.getLong(mChannelsCursor
				.getColumnIndexOrThrow(News.Channel._ID));

		Uri u = null;

		Intent i = new Intent(this,
				org.openintents.newsreader.channels.ChannelSettings.class);
		// startSubActivity(i,AFeedSettings.ACTIVITY_EDIT);
		i.setAction(Intent.ACTION_EDIT);
		i.setData(getIntent().getData());
		Bundle b = new Bundle();

		u = ContentUris.withAppendedId(News.Channel.CONTENT_URI, _id);

		b.putString("URI", u.toString());
		b.putString(News.FEED_TYPE, new String("" + feedType));
		i.putExtras(b);
		startActivity(i);

		/*
		 * 
		 * 
		 * Object o = mChannelsView.getItemAtPosition(position); int res; if (o
		 * != null) {
		 * 
		 * Log.d(_TAG, "SelectedItem>>" + o.toString() + "<<"); HashMap h =
		 * (HashMap) o; String feedType = (String) h.get(News.FEED_TYPE); long
		 * _id = Long.parseLong(((String) h.get(News._ID))); }
		 */
	}

	private void menuDelete(int position) {

		int res;
		// first is header ?
		mChannelsCursor.moveToPosition(position - 1);
		int feedType = mChannelsCursor.getInt(mChannelsCursor
				.getColumnIndexOrThrow(News.Channel.CHANNEL_TYPE));
		String feedname = mChannelsCursor.getString(mChannelsCursor
				.getColumnIndexOrThrow(News.Channel.CHANNEL_TYPE));
		long _id = mChannelsCursor.getLong(mChannelsCursor
				.getColumnIndexOrThrow(News.Channel._ID));

		Uri u = ContentUris.withAppendedId(News.Channel.CONTENT_URI, _id);

		res = mNews.delete(u, null, null);
		if (debug) Log.d(_TAG, "deleted " + res + " rows from channels");
		if (res > 0) {
			res = mNews.delete(News.Contents.CONTENT_URI,
					News.Contents.CHANNEL_ID + "=" + _id, null);
			if (debug) Log.d(_TAG, "deleted " + res + " rows from contentss");
		}

		updateChannelsList(null, null);

		/*
		 * 
		 * if (o != null) {
		 * 
		 * Log.d(_TAG, "SelectedItem>>" + o.toString() + "<<"); HashMap h =
		 * (HashMap) o; String feedType = (String) h.get(News.FEED_TYPE); long
		 * _id = Long.parseLong(((String) h.get(News._ID))); if
		 * (feedType.equals(News.FEED_TYPE_RSS)) { Uri u =
		 * ContentUris.withAppendedId(News.RSSFeeds.CONTENT_URI, _id); res =
		 * News.delete(u, null, null); Log.d(_TAG, "deleted " + res + " rows");
		 * } else if (feedType.equals(News.FEED_TYPE_ATOM)) { } else {
		 * Log.d(_TAG, "SelectedItem>>null<<"); }
		 */
	}

	private void menuMarkAsRead(int position){
		mChannelsCursor.moveToPosition(position - 1);
		long _id = mChannelsCursor.getLong(mChannelsCursor
				.getColumnIndexOrThrow(News.Channel._ID));
		mNews.markFeedAsRead(_id);
		updateChannelsList(null, null);

	}

        private void updateAll(){
		Intent intent = new Intent(getApplicationContext(),
			org.openintents.newsreader.services.NewsreaderService.class);
		intent.putExtra("FORCE_UPDATE",true);
		startService(intent);

        }

	private void menuSettings() {
		if (debug) Log.v(_TAG, "menuSettings:entering");
		// TODO Auto-generated method stub

		Intent intent = new Intent();
		intent.setAction("org.openintents.action.CONFIG_NEWSREADERSERVICE");
		intent.addCategory(Intent.CATEGORY_DEFAULT);
		// intent.putExtras(b);
		startActivity(intent);

		// Intent i= new Intent(this,ServiceSettings.class);
		// startSubActivity(i,ServiceSettings.ACTIVITY_MODIFY);
	}

	@Override
	protected void onPause() {
		super.onPause();
		SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();	
		editor.putString(SELECTED_CATEGORY,mCatname);
		editor.putString(CURRENT_SEARCH,mSearchTerm);
		editor.commit();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (debug) Log.d(_TAG, "onResume: entering");
		// calculateListSize();
		// mRSSListView.debug();
		// Log.d(_TAG,"count from obj>>"+mRSSListView.getAdapter().getCount());

		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);	
		if (mSearchTerm==null)
		{//searchterm could have been set by calling intent
			mSearchTerm=sp.getString(CURRENT_SEARCH,null);
		}
		if (debug) Log.d(_TAG,"cat>"+sp.getString(SELECTED_CATEGORY,null));
		//restore instance state
		String nuCat=sp.getString(SELECTED_CATEGORY,null);
		if (nuCat!=null && !nuCat.equals(mCatname))
		{	//if the categorie changed in settings, this will trigger update	
			ccv.setCurrentCategorie(nuCat);
		}else{
			//if categorie didn't change, we have to update manually
			updateChannelsList(mCatname, mSearchTerm);
		}

		if (debug) Log.d(_TAG, "onResume: leaving");
	}

	@Override
	protected void onSaveInstanceState(Bundle icicle) {
		if (debug) Log.d(_TAG, "onFreeze: entering");

		super.onSaveInstanceState(icicle);

	}



	private void updateChannelsList(String catname, String searchTerm) {

		if ("*".equals(searchTerm) || TextUtils.isEmpty(searchTerm)) {
			// no searchterm
			if (getString(R.string.all).equals(catname)
					|| TextUtils.isEmpty(catname)) {
				// no category
				mSelectionArgs = null;
				mSelection = null;
				setTitle(getString(R.string.newsreader));
			} else {
				// category only
				mSelectionArgs = new String[] { "%" + catname + "%" };
				mSelection = "upper(" + News.Channel.CHANNEL_CATEGORIES
						+ ") like upper(?) ";
				setTitle(getString(R.string.newsreader));
			}
		} else {
			if (getString(R.string.all).equals(catname)
					|| TextUtils.isEmpty(catname)) {
				// searchterm only
				mSelectionArgs = new String[] { "%" + searchTerm + "%" };
				mSelection = "upper(" + Channel.CHANNEL_NAME
						+ ") like upper(?)";
				setTitle(getString(R.string.newsreader_select_1, searchTerm));
			} else {
				// searchterm and category
				mSelectionArgs = new String[] { "%" + searchTerm + "%",
						"%" + catname + "%" };
				mSelection = "upper(" + Channel.CHANNEL_NAME
						+ ") like upper(?) AND upper("
						+ News.Channel.CHANNEL_CATEGORIES + ") like upper(?) ";
				setTitle(getString(R.string.newsreader_select_1, searchTerm));
			}

		}
		if (debug) Log.v(_TAG, "search: " + searchTerm + " " + catname);
		if (debug) Log.v(_TAG, "selection: " + mSelection);

		Builder uriBuilder = News.Channel.CONTENT_URI.buildUpon();
		uriBuilder.appendQueryParameter(Channel.CONTENT_CREATED, "Y");
	
	//	String sortOrder = mSettings.getString(News.PREFS_CHANNEL_SORT_ORDER,
		String sortOrder = 
			PreferenceManager.getDefaultSharedPreferences(this)
			.getString(News.PREFS_CHANNEL_SORT_ORDER,
				News.PREF_VALUE_LATEST);
		if (debug) Log.d(_TAG,"updateList:sortOrder>"+sortOrder);
		if (News.PREF_VALUE_LATEST.equals(sortOrder)) {
			sortOrder = Channel.SORT_ORDER_WITH_CONTENT_LATEST;
			
			//uriBuilder.appendQueryParameter(Channel.QUERY_STATUS_UNREAD, "Y");
		} else if (News.PREF_VALUE_TITLE.equals(sortOrder)) {
			sortOrder = Channel.SORT_ORDER_BY_TITLE;
		} else if (News.PREF_VALUE_CREATED.equals(sortOrder)) {
			sortOrder = Channel.SORT_ORDER_BY_CREATION_DATE;
		} else {
			// most new messages
			sortOrder = Channel.SORT_ORDER_WITH_MOST_NEW_MSG;
			//
		}

		if (debug) Log.v(_TAG, "sort order: " + sortOrder);
		Uri uri = uriBuilder.build();
		mChannelsCursor = managedQuery(uri,
				News.Channel.PROJECTION_WITH_CONTENT_MAP, mSelection,
				mSelectionArgs, sortOrder);

		ChannelListAdapter channelsAdapter = new ChannelListAdapter(this,
				mChannelsCursor);
		if (debug) Log.d(_TAG, "channelsAdapter count>>" + channelsAdapter.getCount()
				+ "<<");
		mChannelsView.setAdapter(channelsAdapter);
	}

	public void openChannel(int position) {

		Cursor cursor = (Cursor) mChannelsView.getItemAtPosition(position);

		String feedType = cursor.getString(cursor
				.getColumnIndexOrThrow(News.Channel.CHANNEL_TYPE));
		String feedname = cursor.getString(cursor
				.getColumnIndexOrThrow(News.Channel.CHANNEL_NAME));
		String _id = cursor.getString(cursor
				.getColumnIndexOrThrow(News.Channel._ID));
		String feedLink = cursor.getString(cursor
				.getColumnIndexOrThrow(News.Channel.CHANNEL_LINK));
		String updateMsgs = cursor.getString(cursor
				.getColumnIndexOrThrow(Channel.UPDATE_MSGS));

		Bundle b = new Bundle();
		b.putString(News.Channel.CHANNEL_TYPE, feedType);
		b.putString(News.Channel._ID, _id);
		b.putString(News.Channel.CHANNEL_NAME, feedname);
		b.putString(News.Channel.CHANNEL_LINK, feedLink);
		b.putString(News.Channel.UPDATE_MSGS, updateMsgs);
		Intent i = new Intent(this, AFeedMessages.class);
		i.setData(getIntent().getData());
		i.putExtras(b);
		startActivity(i);

		/*
		 * 
		 * if (o != null) {
		 * 
		 * Log.d(_TAG, "SelectedItem>>" + o.toString() + "<<"); HashMap h =
		 * (HashMap) o; String feedType = (String) h.get(News.FEED_TYPE); String
		 * _id = (String) h.get(News._ID); Integer count = Integer
		 * .parseInt((String) h.get(News.MESSAGE_COUNT)); if (count == null ||
		 * count <= 0) { // only start intent if messages available. Toast
		 * .makeText( this,
		 * "No message found yet. Inforce update from feed by using menu.",
		 * Toast.LENGTH_LONG).show(); } else {
		 * 
		 * 
		 * }
		 * 
		 * } else { Log.d(_TAG, "SelectedItem>>null<<"); }
		 */
	}

	public void onCategorieChange(String catname) {
		if (catname != null && catname.equals(mCatname) || catname == mCatname) {
			// don't do anything
			if (debug) Log.d(_TAG,"onCatChange,IGNORING EVENT");
		} else {
			mCatname = catname;
			updateChannelsList(catname, mSearchTerm);
		}
	}

	public static final String PREFERENCES_INIT_DEFAULT_VALUES = "InitView";
	public static final String PREFERENCES_DONT_SHOW_INIT_DEFAULT_VALUES = "dontShowInitDefaultValues";

}/* eoc */
