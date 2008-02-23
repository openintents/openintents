package org.openintents.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.provider.BaseColumns;

public abstract class News {

	public static final class RSSFeeds implements BaseColumns{
		
		public static final Uri CONTENT_URI= Uri
			.parse("content://org.openintents.news/rss");
		
		public static final String RSS_FORMAT="2.0";
		
		/* type long/int , unique channel id*/
		//user _ID field as it' aslready unique
		//public static final String CHANNEL_ID="channel_id";
		//public static final String CHANNEL_ID_TYPE="INTEGER";
		
		public static final String _ID="_id";
		public static final String _COUNT="_count";

		public static final String ITEM_GUID="item_guid";
		public static final String ITEM_GUID_TYPE="INTEGER";
		
		/*type uri, the address to fetch the data from*/
		public static final String CHANNEL_LINK="channel_link";
		public static final String CHANNEL_LINK_TYPE="VARCHAR";
		
		public static final String CHANNEL_NAME="channel_name";
		public static final String CHANNEL_NAME_TYPE="VARCHAR";
		
		/*how often the channel will update, in minutes.*/
		public static final String UPDATE_CYCLE="update_cycle";
		public static final String UPDATE_CYCLE_TYPE="INTEGER";
		
		/*how long items should be stored, 0=>forever*/
		public static final String HISTORY_LENGTH="history_len";
		public static final String HISTORY_LENGTH_TYPE="INTEGER";
		
		/*how long items should be stored, 0=>forever*/
		public static final String LAST_UPDATE="last_upd";
		public static final String LAST_UPDATE_TYPE="INTEGER";		
		
		/*the channel language */
		public static final String CHANNEL_LANG="channel_lang";
		public static final String CHANNEL_LANG_TYPE="VARCHAR";
		
		/*the channel description */
		public static final String CHANNEL_DESC="channel_desc";
		public static final String CHANNEL_DESC_TYPE="TEXT";
		
		public static final String CHANNEL_COPYRIGHT="channel_cpr";
		public static final String CHANNEL_COPYRIGHT_TYPE="VARCHAR";
		
		/*URI to an image, mostly a channel icon. make this a local uri whenever possible */
		public static final String CHANNEL_IMAGE_URI="channel_img";
		public static final String CHANNEL_IMAGE_URI_TYPE="VARCHAR";

		public static final String DEFAULT_SORT_ORDER = "";
		
		
		
		
	}/*eoc RSSFeeds*/
	
	
	
	public static final class RSSFeedContents implements BaseColumns{
		/*Uri for accessing Channel Items in RSS2.0 Format*/
		public static final Uri CONTENT_URI= Uri
		.parse("content://org.openintents.news/rsscontents");
	
		
		public static final String RSS_FORMAT="2.0";

		public static final String CHANNEL_ID="channel_id";
		public static final String CHANNEL_ID_TYPE="INTEGER";
		
		public static final String ITEM_GUID="item_guid";
		public static final String ITEM_GUID_TYPE="INTEGER";

		public static final String ITEM_LINK="item_link";
		public static final String ITEM_LINK_TYPE="VARCHAR";
		
		/*type is CDATA/String, a short description of the article or the article itself*/
		public static final String ITEM_DESCRIPTION="item_desc";
		public static final String ITEM_DESCRIPTION_TYPE="TEXT";
		
		public static final String ITEM_TITLE="item_title";
		public static final String ITEM_TITLE_TYPE="VARCHAR";
		
		public static final String ITEM_AUTHOR="item_author";
		public static final String ITEM_AUTHOR_TYPE="VARCHAR";
		
	}/*eoc RSSFeedContents*/
	
	
	public static final class AtomFeeds implements BaseColumns{
	
		public static final Uri CONTENT_URI=
			Uri.parse("content://org.openintents.news/atom");
			
			public static final String DEFAULT_SORT_ORDER="";

			public static final String _ID="_id";
			public static final String _COUNT="_count";

			public static final String FEED_ID="feed_id";

			public static final String FEED_TITLE="feed_title";

			/*pls note: FEED_UPDATE is the updated elemnt of the xml stream
				whereas FEED_LAST_CHECKED should be used for internal time intervalls.
			*/
			public static final String FEED_UPDATED="feed_updated";
			public static final String FEED_LAST_CHECKED="feed_lastchecked";
			/*how often the channel will update, in minutes.*/
			public static final String UPDATE_CYCLE="update_cycle";
			/*how long items should be stored, 0=>forever*/
			public static final String HISTORY_LENGTH="history_len";
			
			public static final String FEED_LINK="feed_link";
			public static final String FEED_LINK_SELF="feed_link_self"; //<- use this for retrieving data.
			public static final String FEED_LINK_ALTERNATE="feed_link_alternate";
	
			public static final String FEED_ICON="feed_icon";
			public static final String FEED_RIGHTS="feed_rights";
			

	}/*eoc AtomFeeds*/

	public static final class AtomFeedContents implements BaseColumns{
		
		public static final Uri CONTENT_URI=
			Uri.parse("content://org.openintents.news/atomcontents");

		public static final String DEFAULT_SORT_ORDER="";

		public static final String _ID="_id";
		public static final String _COUNT="_count";

		public static final String FEED_ID="feed_id";

		public static final String ENTRY_ID="entry_id";

		public static final String ENTRY_TITLE="entry_title";

		public static final String ENTRY_UPDATED="entry_updated";

		public static final String ENTRY_CONTENT="entry_content";
		public static final String ENTRY_CONTENT_TYPE="entry_content_type";

		public static final String ENTRY_LINK="entry_link";
		public static final String ENTRY_LINK_ALTERNATE="entry_link_alternate";

		public static final String ENTRY_SUMMARY="entry_summary";
		public static final String ENTRY_SUMMARY_TYPE="entry_summary_type"; //Defaults to text

	}/*eoc AtomFeedContents*/


	public static ContentResolver mContentResolver;


	//some conveniece constants.
	public static final String FEED_TYPE="FEEDTYPE";
	public static final String FEED_TYPE_RSS="RSS";
	public static final String FEED_TYPE_ATOM="ATOM";

	public static final String MESSAGE_COUNT="MESSAGE_COUNT";
	public static final String _ID="_id";

	/*
	*@deprecated use insert(uri,contentvalues) instead.
	*/
	public static Uri ins(ContentValues cv){
		//return  new org.openintents.news.rss.RSSFeedProvider().insert(RSSFeeds.CONTENT_URI,cv);
//		return mContentResolver.insert(ContentURI.create("content://org.openintents.news"),cv);
		//return new org.openintents.news.NewsProvider().insert(ContentURI.create("content://org.openintents.news"),cv);//works
		//return new org.openintents.news.NewsProvider().insert(RSSFeeds.CONTENT_URI,cv);//works
		return mContentResolver.insert(RSSFeeds.CONTENT_URI,cv);//works
	}


	public static Uri insert(Uri uri, ContentValues cv){

		return mContentResolver.insert(uri,cv);
		
	}

	public static int delete(Uri uri,String selection,String[] selectionArgs){

		return mContentResolver.delete(uri,selection,selectionArgs);
	}


}
