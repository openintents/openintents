package org.openintents.news.views;
/*
<!-- 
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
 -->*/

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TableLayout;
//import android.widget.TableLayout.LayoutParams;
import android.widget.TableRow;
//import android.widget.TableRow.LayoutParams;
import android.content.Context;
import org.openintents.provider.News;
import org.openintents.news.*;
import java.util.HashMap;
/*
 *
 *@author ronan 'zero' schwarz
 */


public class SimpleNewsChannelView extends LinearLayout{

	private TextView mMessages;
	private TextView mChannelName;
	//private TextView mLink;
	
	private HashMap data;
	private Context context;
	private String feedType;
	private String channelName;
	//if u got more than maxint messages in one feed, then you're fubar'ed ;]
	private String messageCount;
	
	public SimpleNewsChannelView(Context context, HashMap data) {
		super(context);
		
		this.context=context;
	//	this.setOrientation(HORIZONTAL);

		this.data=data;
		init();

	}


	public void setData(HashMap data){
		this.data=data;
		init();
	}

	private void init(){
		feedType=(String)data.get(News.FEED_TYPE);
		if (feedType==null||feedType.equals(""))
		{
			//TODO: Tthrow some error
		}
		
		if (feedType.equals(News.FEED_TYPE_RSS))
		{
			channelName=(String)data.get(News.RSSFeeds.CHANNEL_NAME);

		}else if (feedType.equals(News.FEED_TYPE_ATOM))
		{
			channelName=(String)data.get(News.AtomFeeds.FEED_TITLE);

		}
		
		messageCount=(String)data.get(News.MESSAGE_COUNT);
		if (messageCount==null||messageCount.equals(""))
		{
			messageCount="0";
		}


		//setGravity(android.view.Gravity.RIGHT);
		TableRow tr= new TableRow(this.context);

		mChannelName = new TextView(this.context);
		mChannelName.setText(channelName);
		mChannelName.setWidth(280);
		
		//tr.addView(mChannelName);
		addView(mChannelName, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		mMessages = new TextView(this.context);
		mMessages.setText(messageCount);
		mMessages.setAlignment(android.text.Layout.Alignment.ALIGN_OPPOSITE);
		//tr.setLayoutParams(new TableRow.LayoutParams());
		//tr.addView(mMessages);
		addView(mMessages, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		//addView(tr);
	}


}/*eoc*/