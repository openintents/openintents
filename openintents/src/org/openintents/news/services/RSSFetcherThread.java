package org.openintents.news.services;

/*
 * Copyright (C) 2007-2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.openintents.provider.News;
import org.openintents.news.NewsProvider;

import android.net.Uri;
import android.text.TextUtils;
import android.content.ContentValues;
import android.util.Log;
import android.content.ContentUris;

import java.util.HashMap;
import java.net.URL;
import java.net.URLEncoder;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.Scanner;
import org.xml.sax.*;
import org.xml.sax.ext.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;



/*
 *Reads an RSS Feed and inserts items into news contentprovider.
 *
 *@author ronan 'zero' schwarz
 */
public class RSSFetcherThread extends Thread{

	
	public static final String _TAG="RSSFetcherThread";
	

	private HashMap config;

	public RSSFetcherThread(HashMap config){
		
		this.config=config;

		Log.d(_TAG,"created, dumping config:\n"+config);
	}
	

	/*
	 * fetch - parse - insert. 
	 * call them separately for simple tasks or unit testing.
	 */
	public void run(){
		
		Document doc=fetch();
		parse(doc);
		

	}



	public Document fetch(){

	
		String rpc=(String)this.config.get(News.RSSFeeds.CHANNEL_LINK);
		Element tag;
		URL u=null;
		Log.d(_TAG,"Fetching RSS Feed>"+u);
		try
		{
			u=new URL(rpc);	
		}catch(java.net.MalformedURLException mu){
			System.out.println("Malformed URL>>"+mu.getMessage());
			Log.e(_TAG,"Malformed URL>>"+mu.getMessage());
		}


		Document doc = null;
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			doc = db.parse(u.openStream());
			//doc=db.parse(rpc);
			System.out.println("##################done parsing#############");

		String s=new Scanner( u.openStream() ).useDelimiter( "\\Z" ).next();
		//System.out.println(s);

		} catch (java.io.IOException ioe) {
			System.out.println("Error >>"+ioe.getMessage());	
			ioe.printStackTrace();
			Log.e(_TAG,"Error >>"+ioe.getMessage());		
		
		} catch (ParserConfigurationException pce) {
			System.out.println("ERror >>"+pce.getMessage());
			Log.e(_TAG,"ERror >>"+pce.getMessage());
		} catch (SAXException se) {
			System.out.println("ERRROR>>"+se.getMessage());
			Log.e(_TAG,"ERRROR>>"+se.getMessage());
		}

		System.out.println(doc);

		return doc;
	}


	public void parse(Document doc){

		if (doc==null)
		{
			Log.w(_TAG,"Document was null. Connection broken?");
			return;
		}
		//TODO: parse doc.
		System.out.println(doc.toString());
		//System.out.println("UURI>>"+doc.getDocumentURI());
		String _id=(String)this.config.get(News.RSSFeeds._ID);
		//echo(doc.getFirstChild());

		NodeList nl=doc.getElementsByTagName("item");
		int nlen=nl.getLength();

		for (int i=0;i<nlen ;i++ )
		{
			Node item=nl.item(i);
			//printlnCommon(item);
			NodeList childs=item.getChildNodes();
			int childLen=childs.getLength();
			ContentValues cv=new ContentValues();
			System.out.println("LEN>"+childLen);
			StringBuffer selection=new StringBuffer();
			selection.append(News.RSSFeedContents.CHANNEL_ID);
			selection.append("=");
			selection.append(_id);

			cv.put(News.RSSFeedContents.CHANNEL_ID,_id);
			for (int n=0;n<childLen ;n++ )
			{

				Node node=childs.item(n);
				String nodeName=node.getNodeName();
				/*interesting note about android: 
				node.getTextContent is not in their implementation of org.w3c.dom.Node
				they still use java 1.5 . So we have to read text the sick old way.
				*/
				//String nodeValue=node.getTextContent();
				try
				{
					String nodeValue="";

					//String nodeValue=node.getFirstChild().getFirstChild().getNodeValue();
					Node el1=(Node)node.getFirstChild();
					if (el1!=null)
					{
					//	System.out.println("sub node el1 is of type>>"+el1.getNodeType());
						nodeValue=el1.getNodeValue();
					}else{
					//	System.out.println("node "+nodeName+" seems to have no data. :( ");
						Log.d(_TAG,"node "+nodeName+" seems to have no data. :( ");
					}
					/*
					Log.d(_TAG,">>node named>>"+nodeName+"<< has value\n"
						+nodeValue+
						"\n ##############################################\n"
						);
						*/
				//	System.out.println("#############################>>"+nodeName+"#######################");
				//	System.out.println(nodeValue+"\n");

					if (nodeName.equalsIgnoreCase("link"))
					{
						cv.put(News.RSSFeedContents.ITEM_LINK,nodeValue);
					}else if (nodeName.equalsIgnoreCase("guid"))
					{
						cv.put(News.RSSFeedContents.ITEM_GUID,nodeValue);
						selection.append(" AND ");
						selection.append(News.RSSFeedContents.ITEM_GUID);
						selection.append(" like '");
						selection.append(nodeValue);
						selection.append("'");
					}else if (nodeName.equalsIgnoreCase("title"))
					{
						cv.put(News.RSSFeedContents.ITEM_TITLE,nodeValue);
					}else if (nodeName.equalsIgnoreCase("description"))
					{
						cv.put(News.RSSFeedContents.ITEM_DESCRIPTION,nodeValue);
					}else if (nodeName.equalsIgnoreCase("author"))
					{
						cv.put(News.RSSFeedContents.ITEM_AUTHOR,nodeValue);
					}
				}
				catch (NullPointerException npe){
					Log.e(_TAG,"Node >>"+nodeName+"<< had now Text entry");
			//		System.out.println("exception >"+npe.getMessage());
					throw npe;
				}
				catch (Exception e)
				{
					//System.out.println("excecption >"+e.getMessage());
					e.printStackTrace();
				}
			}

			//System.out.println("DUMPING NODE TYPE\n TEXT>>"+Node.TEXT_NODE+"\n CDATA>"+Node.CDATA_SECTION_NODE);
			
			
			Uri rUri=News.insertIfNotExists(News.RSSFeedContents.CONTENT_URI,selection.toString(),null,cv);
			Log.d(_TAG,"insert returned >>"+rUri+"<<");



		}
		ContentValues cv=new ContentValues();
		long now=System.currentTimeMillis();
		cv.put(News.RSSFeeds.LAST_UPDATE,now);
		Uri u=ContentUris.withAppendedId(News.RSSFeeds.CONTENT_URI,Long.parseLong(_id));
		int res=News.update(u,cv,null,null);
		Log.d(_TAG,"Updated #"+res+" rows");


		//return null;
	}











	public static void printlnCommon(Node n){
		System.out.print(" nodeName=\"" + n.getNodeName() + "\"");
		String val = n.getNodeValue();
		if(val != null)
		{ 
			System.out.print(" nodeValue =");
			if(val.trim().equals("")) 
			{
				System.out.print("[WS]");
			}else {
				System.out.print("\"" + n.getNodeValue() + "\"");
			}
		} 
		System.out.println();
	}

	public static void echo(Node n){

		int type = n.getNodeType();
		switch (type){
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
				for(int i = 0; i < atts.getLength(); i++) 
				{
					Node att = atts.item(i); echo(att); 
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
		for (Node child = n.getFirstChild(); child != null; child = child.getNextSibling())
		{
			echo(child);
			}
	}







}