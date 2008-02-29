package org.openintents.news.services;



import java.util.HashMap;

import org.openintents.provider.News;
import org.openintents.news.*;

public class TestRSSFetcher{



	public static void main(String[] args){

		
		System.out.println("###################init##############");
		HashMap <String,String> config=new HashMap();

		config.put(News.RSSFeeds.CHANNEL_LINK,"http://www.theregister.co.uk/comms/mobile/headlines.rss");
		//config.put(News.RSSFeeds.CHANNEL_LINK,"http://feeds.boingboing.net/boingboing/ibag");

		RSSFetcherThread t= new RSSFetcherThread(config);

		t.run();



	}

}