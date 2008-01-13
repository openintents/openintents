package org.openintents.provider;

import android.net.ContentURI;
import android.provider.BaseColumns;

public abstract class RSSFeed {

	public static final class RSSFeeds implements BaseColumns{
		
		public static final ContentURI CONTENT_URI= ContentURI
			.create("org.openintents.news/rss");
		
		public static final String RSS_FORMAT="2.0";
		
		/* type long/int , unique channel id*/
		//user _ID field as it' aslready unique
		//public static final String CHANNEL_ID="channel_id";
		//public static final String CHANNEL_ID_TYPE="INTEGER";
		
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
		public static final ContentURI CONTENT_URI= ContentURI
		.create("org.openintents.news/rsscontents");
	
		
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
	
	

}
