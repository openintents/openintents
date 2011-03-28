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
public class IconFetcherThread extends Thread {

	public static final String _TAG = "IconFetcherThread";
	public static final String CACHE_PATH="newsreader/cache/";


	private HashMap config;

	private String iconUri="";
	private Context context;
	private News mNews;

	public IconFetcherThread(Context context,HashMap config) {

		this.config = config;
		this.context=context;
		mNews = new News(context.getContentResolver());
		Log.d(_TAG, "created, dumping config:\n" + config);
	}

	/*
	 * fetch - parse - insert. call them separately for simple tasks or unit
	 * testing.
	 */
	public void run() {

		int  type = Integer.parseInt((String) this.config.get(News.Channel.CHANNEL_TYPE));

		Document doc = fetch();
		if (type==News.CHANNEL_TYPE_RSS)
		{		
			parseRSS(doc);
		}else if (type==News.CHANNEL_TYPE_ATM)
		{
			//TODO
		}		
		if (!iconUri.equals(""))
		{
			downloadIcon(iconUri);
		}

	}

	public Document fetch() {

		String rpc = (String) this.config.get(News.Channel.CHANNEL_LINK);
		Element tag;
		URL u = null;
		try {
			u = new URL(rpc);
		} catch (java.net.MalformedURLException mu) {
			System.out.println("Malformed URL>>" + mu.getMessage());
			Log.e(_TAG, "Malformed URL>>" + mu.getMessage());
		}
		Log.d(_TAG, "Fetching RSS Feed>" + u);
		Document doc = null;
		if (u != null) {

			try {
				DocumentBuilderFactory dbf = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				doc = db.parse(HTTPUtils.open(rpc));
				// doc=db.parse(rpc);
				System.out
						.println("##################done parsing#############");

				String s = new Scanner(u.openStream()).useDelimiter("\\Z")
						.next();
				// System.out.println(s);

			} catch (java.io.IOException ioe) {
				System.out.println("Error >>" + ioe.getMessage());
				ioe.printStackTrace();
				Log.e(_TAG, "Error >>" + ioe.getMessage());

			} catch (ParserConfigurationException pce) {
				System.out.println("ERror >>" + pce.getMessage());
				Log.e(_TAG, "ERror >>" + pce.getMessage());
			} catch (SAXException se) {
				System.out.println("ERRROR>>" + se.getMessage());
				Log.e(_TAG, "ERRROR>>" + se.getMessage());
			}
		}

		System.out.println(doc);

		return doc;
	}

	public void parseRSS(Document doc) {

		if (doc == null) {
			Log.w(_TAG, "Document was null. Connection broken?");
			return;
		}
		// TODO: parse doc.
		System.out.println(doc.toString());
		// System.out.println("UURI>>"+doc.getDocumentURI());
		String _id = (String) this.config.get(News.Channel._ID);
		 echo(doc.getFirstChild());

		NodeList nl = doc.getElementsByTagName("image");

		int nlen = nl.getLength();
		Log.d(_TAG,"list of image elements has lenght>"+nlen);
		if (nlen==0)
		{
			return;
		}
		Node item = nl.item(0);
		//printlnCommon(item);
		NodeList childs = item.getChildNodes();
		int childLen = childs.getLength();
		
		System.out.println("LEN>" + childLen);

		String nodeName = "";
		String nodeValue = "";

		for (int n = 0; n < childLen; n++) {

			Node node = childs.item(n);

			nodeName = node.getNodeName();
			if (nodeName.equalsIgnoreCase("url"))
			{
			
				/*
				 * interesting note about android: node.getTextContent is not in
				 * their implementation of org.w3c.dom.Node they still use java
				 * 1.5 . So we have to read text the sick old way.
				 */
				// String nodeValue=node.getTextContent();
				try {				
					// String
					// nodeValue=node.getFirstChild().getFirstChild().getNodeValue();
					Node el1 = (Node) node.getFirstChild();
					if (el1 != null) {
						// System.out.println("sub node el1 is of
						// type>>"+el1.getNodeType());
						nodeValue = el1.getNodeValue();
					} else {
						// System.out.println("node "+nodeName+" seems to have
						// no data. :( ");
						Log.d(_TAG, "node " + nodeName
								+ " seems to have no data. :( ");
					}					
				} catch (NullPointerException npe) {
					Log.e(_TAG, "Node >>" + nodeName + "<< had now Text entry");
					// System.out.println("exception >"+npe.getMessage());
					throw npe;
				} catch (Exception e) {
					// System.out.println("excecption >"+e.getMessage());
					e.printStackTrace();
				}

				this.iconUri=nodeValue;
				return;

			}
		}


		// System.out.println("DUMPING NODE TYPE\n
		// TEXT>>"+Node.TEXT_NODE+"\n CDATA>"+Node.CDATA_SECTION_NODE);

	}



	private void downloadIcon(String iconUri){

		if (iconUri==null || iconUri.equals(""))
		{
			return;
		}


		String channelName=(String)this.config.get(News.Channel.CHANNEL_NAME);
		DownloadingFileWorker dfw=new DownloadingFileWorker(iconUri);
		String localfile=dfw.downloadMediaToPrivateFiles(this.context,"icon_"+channelName);

		if (localfile==null || localfile.equals(""))
		{
			return;
		}

		ContentValues cv =new ContentValues();
		/*
		cv.put(News.Contents.CHANNEL_ID, 
			((String)this.config.get(News.Contents.CHANNEL_ID))	
		);*/
		cv.put(News.Channel.CHANNEL_ICON_URI,localfile);
		Log.d(_TAG,"CHANNEL ID>"+(String)this.config.get(News.Channel._ID));
		long rowID=Long.parseLong((String)this.config.get(News.Contents._ID));
		Uri nUri = ContentUris.withAppendedId(News.Channel.CONTENT_URI,rowID);
		
		mNews.update(nUri,cv,null,null);

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