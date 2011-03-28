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
import java.io.InputStream;
import java.util.HashMap;

import org.openintents.provider.News;
import org.openintents.provider.News.Contents;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

/**
 *Reads an RSS Feed and inserts items into news contentprovider.
 * 
 *@author ronan 'zero' schwarz
 */
public class AtomSaxFetcherThread extends AbstractFeedFetcherThread {

	public static final String _TAG = "AtomFetcherThread";

	public AtomSaxFetcherThread(HashMap config, News news, Context context) {
		super(config, news, context);
	}

	public AtomSaxFetcherThread(String datastring, News news, Context context) {
		super(datastring, news, context);
	}

	@Override
	protected void parseInternal( String _id,
			int eventType, long now) throws XmlPullParserException,
			IOException {
		boolean inEntry = false;

		ContentValues cv = null;
		
		while (eventType != XmlPullParser.END_DOCUMENT) {
			String tag = mParser.getName();

			
			if (eventType == XmlPullParser.START_TAG) {
				if (tag.equalsIgnoreCase("entry")) {
					cv = new ContentValues();
					cv.put(News.Contents.CHANNEL_ID, _id);
					cv.put(News.Contents.CREATED_ON, now);
					inEntry = true;
				} else {
					if (inEntry) {
						if (tag.equalsIgnoreCase("link")) {
							String nodeValue = mParser.getAttributeValue(null,
									"href");
							cv.put(News.Contents.ITEM_LINK, nodeValue);
						} else if (tag.equalsIgnoreCase("id")) {
							String nodeValue = mParser.nextText();
							cv.put(News.Contents.ITEM_GUID, nodeValue);
						} else if (tag.equalsIgnoreCase("title")) {
							String nodeValue = mParser.nextText();
							cv.put(News.Contents.ITEM_TITLE, nodeValue);
						} else if (tag.equalsIgnoreCase("summary")) {
							String nodeValue = mParser.nextText();
							cv.put(News.Contents.ITEM_CONTENT, nodeValue);
						} else if (tag.equalsIgnoreCase("content")) {
							String nodeValue = mParser.nextText();
							cv.put(News.Contents.ITEM_CONTENT, nodeValue);
						} else if (tag.equalsIgnoreCase("updated")) {
							String nodeValue = mParser.nextText();
							cv.put(News.Contents.ITEM_PUB_DATE, nodeValue);
						} else {
							Log.v("AtomSaxFetcherThread",
									"ignoreing in entry: " + tag);
						}
					} else {
						Log.v("AtomSaxFetcherThread", "ignoreing: " + tag);
					}
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if (tag.equalsIgnoreCase("entry")) {
					insertFeedMessage(cv);
					inEntry = false;

				}
			}

			eventType = mParser.next();

		}

	}

	public String parseTitleInternal(int eventType)
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