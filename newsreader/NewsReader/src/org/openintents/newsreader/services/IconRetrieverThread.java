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

import java.net.URL;
import java.net.URI;
import java.util.HashMap;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.openintents.lib.HTTPUtils;
import org.openintents.lib.DownloadingFileWorker;
import org.openintents.provider.News;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;

import android.net.Uri;
import android.util.Log;

/*
 * Checks the icon link of a Channel and saves this.
 * 
 * @author ronan 'zero' schwarz
 */
public class IconRetrieverThread extends Thread {

	public static final String _TAG = "IconFetcherThread";
	public static final String CACHE_PATH = "newsreader/cache/";

	private HashMap config;

	private String iconUri = "";
	private Context context;
	private News mNews;

	public IconRetrieverThread(Context context, HashMap config) {

		this.config = config;
		this.context = context;
		mNews = new News(context.getContentResolver());
		Log.d(_TAG, "created, dumping config:\n" + config);
	}

	/*
	 * fetch - parse - insert. call them separately for simple tasks or unit
	 * testing.
	 */
	public void run() {

		iconUri = getUri();

		downloadIcon(iconUri);

	}

	public String getUri() {
		String linkUri = "";
		linkUri = (String) this.config.get(News.Channel.CHANNEL_LINK);

		URL u = null;
		try {
			u = new URL(linkUri);
			Log.d(_TAG, "host now>>" + u.getHost() + "<<");

			String[] t = u.getHost().split("\\.");
			Log.d(_TAG, "t len now>>" + t.length + "<<");
			iconUri = "http://" + t[t.length - 2] + "." + t[t.length - 1];
			Log.d(_TAG, "iconURI now>>" + iconUri + "<<");
			iconUri += "/favicon.ico";
			Log.d(_TAG, "iconURI now>>" + iconUri + "<<");
		} catch (java.net.MalformedURLException mu) {
			System.out.println("Malformed URL>>" + mu.getMessage());
			Log.e(_TAG, "Malformed URL>>" + mu.getMessage());
		} catch (Exception e) {
			Log.e(_TAG, "EXCEPTION>" + e.getMessage());
			e.printStackTrace();
		}
		return iconUri;
	}

	public void downloadIcon(String iconUri) {

		if (iconUri == null || iconUri.equals("")) {
			return;
		}

		String channelName = (String) this.config
				.get(News.Channel.CHANNEL_NAME);
		DownloadingFileWorker dfw = new DownloadingFileWorker(iconUri);
		String localfile = dfw.downloadMediaToPrivateFiles(this.context,
				"icon_" + channelName + ".ico");


		if (localfile == null || localfile.equals("")) {

			return;
		}

		ContentValues cv = new ContentValues();
		/*
		 * cv.put(News.Contents.CHANNEL_ID,
		 * ((String)this.config.get(News.Contents.CHANNEL_ID)) );
		 */
		// save absolute path
		localfile = context.getFileStreamPath(localfile).getAbsolutePath();
		cv.put(News.Channel.CHANNEL_ICON_URI, localfile);

		Log.d(_TAG, "CHANNEL ID>" + (String) this.config.get(News.Channel._ID));
		long rowID = Long
				.parseLong((String) this.config.get(News.Contents._ID));
		Uri nUri = ContentUris.withAppendedId(News.Channel.CONTENT_URI, rowID);

		mNews.update(nUri, cv, null, null);

	}

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

}