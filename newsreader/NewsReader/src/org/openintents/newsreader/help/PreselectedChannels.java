package org.openintents.newsreader.help;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openintents.provider.News;
import org.openintents.provider.News.Categories;
import org.openintents.provider.News.Channel;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

public class PreselectedChannels {

	public static final String NAME = "NAME";
	public static final String DESCRIPTION = "DESCRIPTION";
	public static final String FEED_URL = "FEED_URL";
	public static final String FEED_TYPE = "FEED_TYPE";

	public List<Map> categories;
	public List<List<Map>> feeds;
	private News mNews;

	public PreselectedChannels(News news) {
		categories = new ArrayList<Map>();
		feeds = new ArrayList<List<Map>>();
		mNews = news;
	}

	/**
	 * Adds a feed to the content provider.
	 * 
	 * @param categoryId
	 * @param feedId
	 * @return 
	 */
	public Uri addFeed(int categoryId, int feedId) {
		Map feedMap = feeds.get(categoryId).get(feedId);
		Map categoryMap = categories.get(categoryId);

		String feedtype = (String) feedMap.get(FEED_TYPE);
		String feedurl = (String) feedMap.get(FEED_URL);
		String feedname = (String) feedMap.get(NAME);
		String category = (String) categoryMap.get(NAME);

		ContentValues cv = new ContentValues();
		if (feedtype.equals("application/rss+xml")) {
			cv.put(News.Channel.CHANNEL_TYPE, News.CHANNEL_TYPE_RSS);
		} else if (feedtype.equals("application/atom+xml")) {
			cv.put(News.Channel.CHANNEL_TYPE, News.CHANNEL_TYPE_ATM);
		} else {
			throw new RuntimeException(
					"PreselectedChannels: unknown feed type: " + feedtype);
		}
		cv.put(News.Channel.CHANNEL_LINK, feedurl);
		cv.put(News.Channel.CHANNEL_NAME, feedname);
		cv.put(News.Channel.UPDATE_CYCLE,"180");

		cv.put(News.Channel.CHANNEL_CATEGORIES, category);
		return mNews.insertIfNotExists(News.Channel.CONTENT_URI, Channel.CHANNEL_LINK
				+ " = ?", new String[] { feedurl }, cv);
	}

	/**
	 * Adds a category to the content provider.
	 * 
	 * @param categoryId
	 */
	public void addCategory(Context context, int categoryId) {
		Map categoryMap = categories.get(categoryId);
		String category = (String) categoryMap.get(NAME);

		// Add category
		ContentValues cv = new ContentValues();
		cv.put(News.Categories.NAME, category);
		mNews.insertIfNotExists(News.Categories.CONTENT_URI, Categories.NAME
				+ "= ?", new String[] { category }, cv);

	}

	/**
	 * Returns the name of the specified feed.
	 * 
	 * @param categoryId
	 * @param feedId
	 * @return
	 */
	public String getFeedName(int categoryId, int feedId) {
		Map feedMap = feeds.get(categoryId).get(feedId);
		String feedname = (String) feedMap.get(NAME);

		return feedname;
	}

	public boolean isSubscribed(int categoryId, int feedId) {
		Map feedMap = feeds.get(categoryId).get(feedId);
		String feedtype = (String) feedMap.get(FEED_TYPE);
		String feedurl = (String) feedMap.get(FEED_URL);

		ContentValues cv = new ContentValues();
		if (feedtype.equals("application/rss+xml")) {

			// ok
		} else if (feedtype.equals("application/atom+xml")) {

			// ok
		} else {
			throw new RuntimeException(
					"PreselectedChannels: unknown feed type: " + feedtype);
		}

		return mNews.existsFeedUrl(feedurl);
	}
}
