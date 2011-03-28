package org.openintents.newsreader.services;

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

import java.io.IOException;
import java.util.HashMap;

import org.openintents.provider.News;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

/*
 * Reads an RSS Feed and inserts items into news contentprovider.
 * 
 * @author ronan 'zero' schwarz
 */
public class RSSSaxFetcherThread extends AbstractFeedFetcherThread {

	public static final String _TAG = "RSSFetcherThread";

	public RSSSaxFetcherThread(HashMap config, News news, Context context) {
		super(config, news, context);
	}

	public RSSSaxFetcherThread(String datastring, News news, Context context) {
		super(datastring, news, context);
	}

	@Override
	protected void parseInternal(String _id,
			int eventType, long now) throws XmlPullParserException, IOException {
		boolean inItem = false;

		ContentValues cv = null;
		while (eventType != XmlPullParser.END_DOCUMENT) {
			String tag = mParser.getName();

			if (eventType == XmlPullParser.START_TAG) {
				if (tag.equalsIgnoreCase("item")) {
					Log.v(_TAG, "new convent values");
					cv = new ContentValues();
					cv.put(News.Contents.CHANNEL_ID, _id);
					cv.put(News.Contents.CREATED_ON, now);
					inItem = true;
				} else {
					if (inItem) {
						if (tag.equalsIgnoreCase("link")) {

							String nodeValue = mParser.nextText();
							cv.put(News.Contents.ITEM_LINK, nodeValue);
						} else if (tag.equalsIgnoreCase("guid")) {
							String nodeValue = mParser.nextText();
							cv.put(News.Contents.ITEM_GUID, nodeValue);
						} else if (tag.equalsIgnoreCase("title")) {
							String nodeValue = mParser.nextText();
							cv.put(News.Contents.ITEM_TITLE, nodeValue);
						} else if (tag.equalsIgnoreCase("description")) {
							String nodeValue = mParser.nextText();
							cv.put(News.Contents.ITEM_CONTENT, nodeValue);
						} else if (tag.equalsIgnoreCase("author")) {
							String nodeValue = mParser.nextText();
							cv.put(News.Contents.ITEM_AUTHOR, nodeValue);
						} else if (tag.equalsIgnoreCase("pubdate")) {
							String nodeValue = mParser.nextText();
							cv.put(News.Contents.ITEM_PUB_DATE, nodeValue);
						} else {
							Log.v(_TAG, "ignoreing in item: " + tag);
						}
					} else {
						Log.v(_TAG, "ignoreing: " + tag);
					}
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if (tag.equalsIgnoreCase("item")) {
					final ContentValues cvFinal = cv;					
					insertFeedMessage(cvFinal);
					inItem = false;
				}
			}

			eventType = mParser.next();

		}
	}

	protected String parseTitleInternal(int eventType)
			throws XmlPullParserException, IOException {
		while (eventType != XmlPullParser.END_DOCUMENT) {
			String tag = mParser.getName();

			if (eventType == XmlPullParser.START_TAG) {
				if (tag.equalsIgnoreCase("title")) {
					return mParser.nextText();
				}
			}

			eventType = mParser.next();

		}
		return null;
	}

}