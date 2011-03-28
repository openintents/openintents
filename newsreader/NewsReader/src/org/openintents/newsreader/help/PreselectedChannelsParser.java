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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openintents.provider.News;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.XmlResourceParser;

public class PreselectedChannelsParser {

	private static final String LOG_TAG = "PreselectedChannelsParser";

	public static final String TAG_FEEDLIST = "FeedList";
	public static final String TAG_FEEDCATEGORY = "FeedCategory";
	public static final String TAG_LINK = "link";
	
	public static final String ATTR_NAME = "name";
	public static final String ATTR_REL = "rel";
	public static final String ATTR_TYPE = "type";
	public static final String ATTR_TITLE = "title";
	public static final String ATTR_HREF = "href";
	
	private XmlPullParser mXpp;
	private PreselectedChannels mPc;
	
	private Map mCategoryMap;
	private List<Map> mFeedList;

	private News mNews;
    
	public PreselectedChannelsParser(News news) {
		mNews = news;
	}
	public PreselectedChannels fromXML(InputStream in)
			throws XmlPullParserException, IOException {
		XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

		mXpp = factory.newPullParser();
		mXpp.setInput(new InputStreamReader(in));

		return parse();
	}
	
	public PreselectedChannels fromXMLResource(XmlResourceParser in)
	throws XmlPullParserException, IOException {
		mXpp = in;
		
		return parse();
	}

	public PreselectedChannels parse()
			throws XmlPullParserException, IOException {
		
		mPc = new PreselectedChannels(mNews);
		
		mCategoryMap = null;
		mFeedList = null;
		
		int eventType = mXpp.getEventType();

		while (eventType != XmlPullParser.END_DOCUMENT) {
			String tag = mXpp.getName();

			if (eventType == XmlPullParser.START_TAG) {
				if (tag.equals(TAG_FEEDLIST)) {
					
				} else if (tag.equals(TAG_FEEDCATEGORY)) {
					addFeedCategoryStart();
				} else if (tag.equals(TAG_LINK)) {
					addFeed();
				}
			} else if (eventType == XmlPullParser.END_TAG) {
				if (tag.equals(TAG_FEEDCATEGORY)) {
					addFeedCategoryEnd();
				}
			}

			eventType = mXpp.next();
		}

		return mPc;
	}

	private void addFeedCategoryStart() {
		String category = mXpp.getAttributeValue(null, ATTR_NAME);
	
		mCategoryMap = new HashMap();
		mFeedList = new ArrayList<Map>();
        
		mCategoryMap.put(PreselectedChannels.NAME, category);
		//mCategoryMap.put(PreselectedChannels.DESCRIPTION, description);
	}
	
	private void addFeedCategoryEnd() {
		mPc.categories.add(mCategoryMap);
		mPc.feeds.add(mFeedList);
	}
	
	private void addFeed() {
		if (mFeedList == null) {
			throw new RuntimeException("PreselectedChannelsParser: <FeedCategory> tag missing!");
		}
		String rel = mXpp.getAttributeValue(null, ATTR_REL);
		String type = mXpp.getAttributeValue(null, ATTR_TYPE);
		String title = mXpp.getAttributeValue(null, ATTR_TITLE);
		String href = mXpp.getAttributeValue(null, ATTR_HREF);
		
		Map feedMap = new HashMap();
        feedMap.put(PreselectedChannels.NAME, title);
        //curChildMap.put(PreselectedChannels.DESCRIPTION, "");
        feedMap.put(PreselectedChannels.FEED_URL, href);
        feedMap.put(PreselectedChannels.FEED_TYPE, type);
        mFeedList.add(feedMap);
	}
	
}
