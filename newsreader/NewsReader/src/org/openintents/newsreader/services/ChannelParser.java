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
import java.io.InputStreamReader;
import java.net.URL;

import org.openintents.lib.HTTPUtils;
import org.openintents.provider.News;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;

public class ChannelParser {

	private XmlPullParser mParser;
	private static final String _TAG = "ChannelParser";

	Context mContext;
	
	public ChannelParser(Context context) {
		mContext = context;
		
		try {
			mParser = XmlPullParserFactory.newInstance().newPullParser();
			mParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
	}

	public InputStream fetch(String rpc) {
		URL u = null;
		try {
			u = new URL(rpc);
		} catch (java.net.MalformedURLException mu) {
			System.out.println("Malformed URL>>" + mu.getMessage());
			Log.e(_TAG, "Malformed URL>>" + mu.getMessage());
		}

		InputStream inputStream = null;
		if (u != null) {
			try {
				inputStream = HTTPUtils.open(rpc);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		return inputStream;
	}

	public Integer parse(InputStream is) {
		Log.d(_TAG, "parse::entering");

		if (is == null) {
			Log.w(_TAG, "Document was null. Connection broken?");
			return null;
		}

		Integer channelType = null;
		try {
			String encoding = NewsServiceSettings.getDefaultEncodingFromPrefs(mContext);
			mParser.setInput(new InputStreamReader(is, encoding));

			int eventType = mParser.getEventType();

			while (eventType != XmlPullParser.END_DOCUMENT) {

				String tag = mParser.getName();
				Log.v(_TAG, eventType + " - " + tag + " - " + mParser.getNamespace());
				channelType = parseChannelType(tag);

				
				if (eventType == XmlPullParser.END_TAG || eventType == XmlPullParser.START_TAG) {
					// only handle the first start tag event
					break;
				}
				eventType = mParser.next();
			}
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return channelType;
	}

	public Integer parseChannelType(String tag) {

		if ("rss".equalsIgnoreCase(tag)) {
			return News.CHANNEL_TYPE_RSS;
		} else if ("rdf".equalsIgnoreCase(tag)) {
			return News.CHANNEL_TYPE_RSS;
		} else if ("feed".equalsIgnoreCase(tag)) {
			return News.CHANNEL_TYPE_ATM;
		}

		return null;
	}
}
