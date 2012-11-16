package org.openintents.provider;
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
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;
import android.provider.BaseColumns;
import java.util.*;

import org.openintents.newsreader.NewsProvider;

public class News {

	public static final String _TAG = "News";

	public static interface Channel extends BaseColumns {

		public static final Uri CONTENT_URI = Uri
				.parse("content://org.openintents.news/channels");

		/** the link to the data path */
		public static final String CHANNEL_LINK = "channel_link";

		/**
		 * use this for storing the data path, additional information about the
		 * channel
		 */
		public static final String CHANNEL_DATA_LINK = "channel_data_link";

		/**
		 * feed_title(atom)
		 */
		public static final String CHANNEL_NAME = "channel_name";

		/** either atom,rss podcast_atom or podcast_rss */
		public static final String CHANNEL_TYPE = "channel_type";

		/** either NATURE_SYSTEM or NATURE_USER. defaults to user(0) */
		public static final String CHANNEL_NATURE = "channel_nature";

		public static final String CHANNEL_DESC = "channel_desc";

		public static final String CHANNEL_ICON_URI = "channel_icon_uri";

		public static final String CHANNEL_COPYRIGHT = "channel_cpr";

		public static final String CHANNEL_CATEGORIES = "channel_categories";

		public static final String NOTIFY_NEW = "notify_new";

		/** how often the channel will update, in minutes. */
		public static final String UPDATE_CYCLE = "update_cycle";

		/** how long items should be stored, 0=>forever */
		public static final String HISTORY_LENGTH = "history_len";

		/**
		 * last time the feed was checked, in raw milliseconds
		 * feed_lastchecked(atom)
		 */
		public static final String LAST_UPDATE = "last_upd";

		/**
		 * last time a new message was added to the feed (date type)
		 */
		public static final String LAST_PUBDATE = "last_pubd";

		/** the channel language */
		public static final String CHANNEL_LANG = "channel_lang";

		/**
		 * flag indicating that messages are updated in contrast to insert only.
		 * ("delete before update")
		 */
		public static final String UPDATE_MSGS = "update_msgs";

		/**
		 * last time a new message was added to the feed (date type) (not a
		 * field in the channel table but calculated with outer left join and
		 * group by)
		 */
		public static final String CONTENT_CREATED = "content_created";

		/**
		 * number of messages (not a field in the channel table but calculated
		 * with outer left join and group by)
		 */
		public static final String CONTENT_COUNT = "content_count";

		public static final String[] PROJECTION_MAP = { _ID, CHANNEL_LINK,
				CHANNEL_DATA_LINK, CHANNEL_NAME, CHANNEL_TYPE, CHANNEL_NATURE,
				CHANNEL_DESC, CHANNEL_ICON_URI, CHANNEL_COPYRIGHT, NOTIFY_NEW,
				UPDATE_CYCLE, UPDATE_MSGS, HISTORY_LENGTH, CHANNEL_LANG };

		public static final String[] PROJECTION_WITH_CONTENT_MAP = { _ID,
				CHANNEL_LINK, CHANNEL_DATA_LINK, CHANNEL_NAME, CHANNEL_TYPE,
				CHANNEL_NATURE, CHANNEL_DESC, CHANNEL_ICON_URI,
				CHANNEL_COPYRIGHT, NOTIFY_NEW, UPDATE_CYCLE, HISTORY_LENGTH,
				CHANNEL_LANG, UPDATE_MSGS, CONTENT_CREATED, CONTENT_COUNT };

		public static final String DEFAULT_SORT_ORDER = CHANNEL_NAME;

		public static final String SORT_ORDER_WITH_CONTENT_LATEST = CONTENT_CREATED
				+ " DESC";
		public static final String SORT_ORDER_WITH_MOST_NEW_MSG = CONTENT_COUNT
				+ " DESC";
		public static final String SORT_ORDER_BY_TITLE = CHANNEL_NAME;
		public static final String SORT_ORDER_BY_CREATION_DATE = _ID;

		public static final String QUERY_STATUS_UNREAD = "status_unread";

	}

	public static interface Contents extends BaseColumns {

		public static final Uri CONTENT_URI = Uri
				.parse("content://org.openintents.news/contents");

		/**
		 * feed_id(atom)
		 */
		public static final String CHANNEL_ID = "channel_id";

		public static final String CHANNEL_TYPE = Channel.CHANNEL_TYPE;

		/**
		 * entry_id (atom)
		 */
		public static final String ITEM_GUID = "item_guid";

		/**
		 * entry_link(atom)
		 */
		public static final String ITEM_LINK = "item_link";

		/*
		 * type is CDATA/String, a short description of the article or the
		 * article itself
		 */
		/**
		 * entry_summary(atom)
		 * 
		 * @deprecated use ITEM_CONTENT
		 */

		public static final String ITEM_SUMMARY = "item_summary";
		/**
		 * the mime type, if any entry_summary_type (atom)
		 * 
		 * @deprecated use ITEM_CONTENT_TYPE
		 */
		public static final String ITEM_SUMMARY_TYPE = "item_summary_type";

		/**
		 * entry_content(atom), item_description(rss)
		 */
		public static final String ITEM_CONTENT = "item_content";
		/**
		 * the mime type, if any entry_content_type(atom)
		 */
		public static final String ITEM_CONTENT_TYPE = "item_content_type";

		/**
		 * entry_title (atom)
		 */
		public static final String ITEM_TITLE = "item_title";

		public static final String ITEM_AUTHOR = "item_author";

		public static final String READ_STATUS = "read_status";

		public static final String ITEM_PUB_DATE = "item_pubdate";
		public static final String CREATED_ON = "created_on";

		public static final String[] PROJECTION_MAP = { _ID, CHANNEL_ID,
				ITEM_GUID, ITEM_LINK, ITEM_CONTENT, ITEM_CONTENT_TYPE,
				ITEM_TITLE, ITEM_AUTHOR, READ_STATUS, ITEM_PUB_DATE, CREATED_ON };

		public static final String DEFAULT_SORT_ORDER = CREATED_ON + " DESC, "
				+ _ID + " ASC";

	}

	public static final class Categories implements BaseColumns {
		public static final Uri CONTENT_URI = Uri
				.parse("content://org.openintents.news/categories");

		public static final String NAME = "name";
		public static final String DEFAULT_SORT_ORDER = NAME;

		public static final String[] PROJECTION_MAP = { _ID, NAME };

		public static final String QUERY_USED_ONLY = "usedonly";

		public static final String QUERY_VALUE_Y = "Y";

		public static final String QUERY_COMPRESS = "compress";
	}

	public ContentResolver mContentResolver;

	// some convenience constants.
	public static final String FEED_TYPE = "FEEDTYPE";
	public static final String FEED_TYPE_RSS = "RSS";
	public static final String FEED_TYPE_ATOM = "ATOM";
	public static final String FEED_TYPE_RDF = "RDF";

	public static final String FEED_NATURE = "feed_nature";
	public static final int FEED_NATURE_USER = 0;
	public static final int FEED_NATURE_SYSTEM = 1;

	public static final int CHANNEL_NATURE_USER = 0;
	public static final int CHANNEL_NATURE_SYSTEM = 1;

	public static final String CHANNEL_TYPE = "FEEDTYPE";
	public static final int CHANNEL_TYPE_RSS = 0;
	public static final int CHANNEL_TYPE_ATM = 1;
	public static final int CHANNEL_TYPE_POD = 2;
	public static final int CHANNEL_TYPE_RDF = 3;
	public static final int CHANNEL_TYPE_UNSUPPORTED = -1;

	public static final String MESSAGE_COUNT = "MESSAGE_COUNT";
	public static final String _ID = "_id";

	public static final int STATUS_UNREAD = 0;
	public static final int STATUS_READ = 1;
	public static final int STATUS_DELETED = -1;

	public static final String CAT_DELIMITER = ";";

	public static final String PREFS_MESSAGE_CLICK_ACTION = "message_click_action";
	public static final String PREF_VALUE_ONLINE = "online";
	public static final String PREF_VALUE_OFFLINE = "offline";

	public static final String PREFS_CHANNEL_SORT_ORDER = "channel_sort_order";
	public static final String PREF_VALUE_NEW = "new";
	public static final String PREF_VALUE_LATEST = "latest";
	public static final String PREF_VALUE_TITLE = "title";
	public static final String PREF_VALUE_CREATED = "created";

	/** generated content */
	public static final String CONTENT_TYPE_G = "G";

	public News(ContentResolver contentResolver) {
		mContentResolver = contentResolver;
	}

	/**
	 *@param uri
	 *            the content uri to insert to
	 *@param cv
	 *            the ContentValues that will be inserted to
	 */
	public Uri insert(Uri uri, ContentValues cv) {

		return mContentResolver.insert(uri, cv);

	}

	/**
	 *@param uri
	 *            the content uri to insert to
	 *@param selection
	 *            the selection to check against
	 *@param selectionArgs
	 *            the arguments applied to selection string (optional)
	 *@param cs
	 *            the ContentValues that will be inserted if selection returns 0
	 *            rows.
	 *@return null if already exists
	 */
	public Uri insertIfNotExists(Uri uri, String selection,
			String[] selectionArgs, ContentValues cs) {
		return insertOrUpdate(uri, selection, selectionArgs, cs, true);

	}

	/**
	 * inserts or update data if insertOnly returns null if data exists if not
	 * insertOnly uri of inserted or updated data returned.
	 * 
	 * if not insertOnly the first row is updated that matches the search
	 * criteria.
	 * 
	 * @param uri
	 * @param selection
	 * @param selectionArgs
	 * @param cs
	 * @param insertOnly
	 * @return
	 */
	public Uri insertOrUpdate(Uri uri, String selection,
			String[] selectionArgs, ContentValues cs, boolean insertOnly) {
		Uri u = null;

		String[] projection = { BaseColumns._ID };
		// query(Uri uri, String[] projection, String selection, String[]
		// selectionArgs, String sortOrder)
		Log.d(_TAG, "insertIfNotExists:entering ");
		Log.d(_TAG, "checking for\n uri:" + uri + "\n selection:" + selection
				+ "\n selArgs[]:" + selectionArgs + "\n cv:" + cs);
		Cursor c = mContentResolver.query(uri, projection, selection,
				selectionArgs, null);

		if (c != null) {
			Log.d(_TAG, "returned count of>>" + c.getCount());
			if (!insertOnly) {

				// do update
				c.moveToFirst();
				Log.d(_TAG, "uri part>>" + uri.getSchemeSpecificPart() + "<<");
				String tid = c.getString(c
						.getColumnIndexOrThrow(BaseColumns._ID));
				u = Uri.fromParts(uri.getScheme(), uri.getSchemeSpecificPart(),
						tid);

				// update data
				mContentResolver.update(uri, cs, null, null);
				c.close();
			} else {
				// if insert only return null to indicate that content exists
				u = insert(uri, cs);
				c.close();
			}

		}
		Log.d(_TAG, "insertIfNotExists:leaving");
		return u;

	}

	/**
	 *@param uri
	 *            the content uri to delete
	 *@param selection
	 *            the selection to check against
	 *@param selectionArgs
	 *            the arguments applied to selection string (optional)
	 *@return number of deleted rows
	 */
	public int delete(Uri uri, String selection, String[] selectionArgs) {

		return mContentResolver.delete(uri, selection, selectionArgs);
	}

	/**
	 *@param uri
	 *            the content uri to update
	 *@param cv
	 *            the ContentValues that will be update in selected rows.
	 *@param selection
	 *            the selection to check against
	 *@param selectionArgs
	 *            the arguments applied to selection string (optional)
	 *@return number of updated rows
	 */
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return mContentResolver.update(uri, values, selection, selectionArgs);
	}

	/**
	 * @param contentUri
	 * @param feedurl
	 * @return true if the feedurl exists in the list of subscribed feeds
	 */
	public boolean existsFeedUrl(String feedurl) {
		Cursor cursor = mContentResolver.query(Channel.CONTENT_URI,
				new String[] { _ID }, Channel.CHANNEL_LINK + " = ?",
				new String[] { feedurl }, null);
		boolean result = cursor.getCount() > 0;
		cursor.close();
		return result;
	}

	/**
	 * @param contentUri
	 * @param feedurl
	 * @return true if the feedurl exists in the list of subscribed feeds
	 */
	public String findChannel(Uri contentUri, String feedurl) {
		Cursor cursor = mContentResolver.query(contentUri,
				new String[] { _ID }, Channel.CHANNEL_LINK + " = ?",
				new String[] { feedurl }, null);
		String result = null;
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			result = cursor.getString(0);
		}
		cursor.close();
		return result;
	}

	public void deleteMessages(String channelId) {
		mContentResolver.delete(Contents.CONTENT_URI, Contents.CHANNEL_ID
				+ " = ?", new String[] { channelId });

	}

	public static void compressCategories(ContentResolver contentResolver) {
		Uri u = Categories.CONTENT_URI.buildUpon().appendQueryParameter(
				Categories.QUERY_COMPRESS, Categories.QUERY_VALUE_Y).build();
		contentResolver.delete(u, null, null);
	}


	public void markFeedAsRead(long feedID){
		int res=0;
		ContentValues cv = new ContentValues();
		cv.put(Contents.READ_STATUS, STATUS_READ);
		res=update(Contents.CONTENT_URI,
			cv,
			Contents.CHANNEL_ID+"="+feedID,
			null
		);
		Log.d(_TAG,"marked >"+res+"< messages");
	}
}
