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
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openintents.lib.HTTPUtils;
import org.openintents.newsreader.R;
import org.openintents.provider.News;
import org.openintents.provider.News.Channel;
import org.openintents.provider.News.Contents;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;
/**
 * abstract base class to fetch feeds. subclasses for a particular type of
 * feeds, e.g rss, atom must implement parseInternal and parseTitleInternal.
 * 
 * 
 */
/**
 * @author muef
 * extends Thread
 */
public abstract class AbstractFeedFetcherThread extends Thread  {

	public static final String _TAG = "FeedFetcherThread";
	protected HashMap config;
	protected News mNews;
	protected XmlPullParser mParser;
	private NewsreaderService mNewsreaderService;
	protected int countNewEntries;
	private String mRelPath;
	private String mAbsPath;
	private Context mContext;

	private ServiceHelper mServiceHelper;
	
	private static IntentFilter intentFilter;

	static {
		intentFilter=new IntentFilter();
		intentFilter.addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED);
		intentFilter.addAction("android.intent.action.DATA_STATE");
		intentFilter.addAction("android.intent.action.SERVICE_STATE");
	}


	public AbstractFeedFetcherThread(HashMap config, News news, Context context) {

		this.config = config;
		mNews = news;
		mContext = context;
		mServiceHelper=new ServiceHelper(mContext);
		try {
			mParser = XmlPullParserFactory.newInstance().newPullParser();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		Log.d(_TAG, "created, dumping config:\n" + config);

	}

	/**
	 * Constructor for simple title updates or browser views (i.e. default
	 * values)
	 * 
	 * @param datastring
	 * @param news
	 */
	public AbstractFeedFetcherThread(String datastring, News news,
			Context context) {
		this.config = new HashMap();
		this.config.put(Channel.CHANNEL_LINK, datastring);
		this.config.put(Channel.NOTIFY_NEW, "0");
		this.config.put(Channel.UPDATE_MSGS, "0");

		mNews = news;
		mContext = context;

		mServiceHelper=new ServiceHelper(mContext);

		try {
			mParser = XmlPullParserFactory.newInstance().newPullParser();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		// Log.d(_TAG, "created, dumping config:\n" + config);

	}

	protected void notifyNewEntries(int countNewEntries,String _id){
		mServiceHelper.notifyNewEntries(countNewEntries,_id);
	}

	public void run() {

		InputStream is = fetch();
		if (is != null) {
			parse(is);
		} else {
			Log.d(_TAG, "url failed.");
		}

	}

	public InputStream fetch() {
		String rpc = (String) this.config.get(News.Channel.CHANNEL_LINK);
		URL u = null;
		Log.d(_TAG, "::fetch:Construct Feed URL from>" + u);
		try {
			u = new URL(rpc);
		} catch (java.net.MalformedURLException mu) {
			System.out.println("::fetch:Malformed URL>>" + mu.getMessage());
			Log.e(_TAG, "::fetch:Malformed URL>>" + mu.getMessage());
		}

		Log.d(_TAG, "::fetch: Fetching Feed>" + u);

		InputStream inputStream = null;
		if (u != null) {
			mRelPath = u.getProtocol() + "://" + u.getHost() + u.getPath();
			mAbsPath = u.getProtocol() + "://" + u.getHost();
			try {
				inputStream = HTTPUtils.open(rpc);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		Log.v(_TAG, "::fetch: relPath = " + mRelPath);

		return inputStream;
	
	}

	public void parse(InputStream is) {		
		
		String _id = (String) this.config.get(News.Channel._ID);
		countNewEntries = 0;
		long now = System.currentTimeMillis();
		
		try {
			String encoding = NewsServiceSettings.getDefaultEncodingFromPrefs(mContext);
			mParser.setInput(new InputStreamReader(is, encoding));

			int eventType = mParser.getEventType();

			// parser started, so we have chances to get new messages
			// delete messages if required
			if ("1".equals(config.get(Channel.UPDATE_MSGS))) {
				mNews.deleteMessages(_id);
			}

			// start the actual parsing
			parseInternal( _id, eventType, now);

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		Log.v(_TAG, "count:" + config.get(Channel.NOTIFY_NEW) + " "
				+ countNewEntries);

		if (countNewEntries > 0 && "1".equals(config.get(Channel.NOTIFY_NEW))) {
			notifyNewEntries(countNewEntries, _id);
		}

		if (_id != null) {
			Uri u = ContentUris.withAppendedId(News.Channel.CONTENT_URI, Long
					.parseLong(_id));
			ContentValues cv = new ContentValues();

			// last update
			cv.put(News.Channel.LAST_UPDATE, now);

			// last pubdate
			// Uri pubDateUri =
			// u.buildUpon().appendQueryParameter(Channel.LAST_PUBDATE,
			// "Y").build();
			// Cursor cursor = mNews.mContentResolver.query(pubDateUri, new
			// String[]{Channel.LAST_PUBDATE}, null, null, null);
			// cursor.moveToFirst();
			// String lastPubDate = cursor.getString(0);
			// cursor.close();
			// cv.put(News.Channel.LAST_PUBDATE, lastPubDate);
			// Log.d(_TAG, "Update #" + _id + " pub date " + lastPubDate);

			int res = mNews.update(u, cv, null, null);
			Log.d(_TAG, "Updated #" + res + " " + countNewEntries + "rows");
		}

	}

	/**
	 * parse method that extracts the all items from mParser. first event type
	 * given, just write a loop to go through all events with eventType =
	 * mParser.next();
	 * @param eventType
	 * @param now
	 * 
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected abstract void parseInternal(String _id, int eventType, long now)
			throws XmlPullParserException, IOException;



	public void setNewsreaderService(NewsreaderService newsreaderService) {
		mNewsreaderService = newsreaderService;
	}

	public String parseTitle(InputStream is) {
		String title = null;
		try {
			String encoding = NewsServiceSettings.getDefaultEncodingFromPrefs(mContext);
			mParser.setInput(new InputStreamReader(is, encoding));

			int eventType = mParser.getEventType();
			title = parseTitleInternal(eventType);

		} catch (XmlPullParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return title;
	}

	/**
	 * parse method that extracts the title from mParser. first event type
	 * given, just write a loop to go through all events with eventType =
	 * mParser.next();
	 * 
	 * @param eventType
	 * @return
	 * @throws IOException
	 * @throws XmlPullParserException
	 */
	protected abstract String parseTitleInternal(int eventType)
			throws XmlPullParserException, IOException;

	public static void printlnCommon(Node n) {
		System.out.print(" nodeName=\"" + n.getNodeName() + "\"");
		String val = n.getNodeValue();
		if (val != null) {
			System.out.print(" nodeValue =");
			if (val.trim().equals("")) {
				System.out.print("[WS]");
			} else {
				System.out.print("\"" + n.getNodeValue() + "\"");
			}
		}
		System.out.println();
	}

	public static void echo(Node n) {

		int type = n.getNodeType();
		switch (type) {
		case Node.ATTRIBUTE_NODE:
			System.out.print("ATTR:");
			printlnCommon(n);
			break;
		case Node.DOCUMENT_NODE:
			System.out.print("DOC:");
			printlnCommon(n);
			break;
		case Node.ELEMENT_NODE:
			System.out.print("ELEM:");
			printlnCommon(n);
			NamedNodeMap atts = n.getAttributes();
			for (int i = 0; i < atts.getLength(); i++) {
				Node att = atts.item(i);
				echo(att);
			}
			break;
		case Node.TEXT_NODE:
			System.out.print("CHAR:");
			printlnCommon(n);
			break;
		default:
			System.out.print("UNSUPPORTED NODE:" + type);
			printlnCommon(n);
			break;
		}
		for (Node child = n.getFirstChild(); child != null; child = child
				.getNextSibling()) {
			echo(child);
		}
	}

	void insertFeedMessage(ContentValues cv) {
		
		try {
			sleep(250);
		} catch (InterruptedException e) {			
			e.printStackTrace();
		}
		StringBuffer selection = new StringBuffer();
		selection.append(News.Contents.CHANNEL_ID);
		selection.append("=");
		selection.append(cv.getAsString(Contents.CHANNEL_ID));

		if (!cv.containsKey(Contents.ITEM_GUID)) {
			cv.put(Contents.ITEM_GUID, cv.getAsString(Contents.ITEM_LINK));
		}

		if (mRelPath != null && mAbsPath != null) {
			String content = cv.getAsString(Contents.ITEM_CONTENT);
			String newContent = replaceRelativeLinks(content, mRelPath,
					mAbsPath);
			cv.put(Contents.ITEM_CONTENT, newContent);
		}

		if (cv.getAsString(Contents.ITEM_CONTENT) == null) {
			String content = mContext.getString(R.string.no_offline_msg, cv
					.getAsString(Contents.ITEM_LINK));
			Log.v(_TAG, "genereated content:" + content);
			cv.put(Contents.ITEM_CONTENT, content);
			cv.put(Contents.ITEM_CONTENT_TYPE, News.CONTENT_TYPE_G);
		}
		selection.append(" AND ");
		selection.append(News.Contents.ITEM_GUID);
		selection.append(" = '");
		selection.append(cv.getAsString(Contents.ITEM_GUID));
		selection.append("'");

		// insert if not exists
		// if requested, old feed messages have been deleted before parsing
		Uri rUri = mNews.insertIfNotExists(News.Contents.CONTENT_URI, selection
				.toString(), null, cv);

		Log.i(_TAG, "insert returned >>" + rUri + "<< for " + selection.append(cv.getAsString(Contents.ITEM_LINK)));
		if (rUri != null) {
			countNewEntries++;
		}		
	}

	public String replaceRelativeLinks(String content, String relPath,
			String absPath) {
		if (content == null) {
			// Don't replace anything.
			return content;
		}

		Log.d(_TAG, "content: " + content);
		Log.d(_TAG, "relPath: " + relPath);
		Log.d(_TAG, "absPath: " + relPath);
		Pattern p = Pattern.compile("(<a.*\\s*href\\s*=\\s*\")\\s*(/[^\"]*\")");
		Matcher m = p.matcher(content);
		String result = m.replaceAll("$1" + absPath + "$2");

		Pattern p2 = Pattern
				.compile("(<a.*\\s*href\\s*=\\s*\")\\s*([^h][^t][^t][^p][^\"]*\")");
		Matcher m2 = p2.matcher(result);
		result = m2.replaceAll("$1" + relPath + "$2");

		Log.d(_TAG, "result: " + result);
		return result;

	}




}