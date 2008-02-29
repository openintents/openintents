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





import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CursorAdapter;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import java.util.HashMap;
import java.util.List;
import org.openintents.provider.News;

/*
 *
 *@author ronan 'zero' schwarz
 */
public class MessageListAdapter extends CursorAdapter{
	
	private boolean[] mExpanded;
	private String feedType;

	private static final String _TAG="MessgeListAdapter";

	public MessageListAdapter(Cursor c, Context context){
		super(c,context);
		initBoolTable(c.count());
	}

	public MessageListAdapter(Cursor c, Context context, boolean autoRequery) {
		super(c,context,autoRequery);
		
		initBoolTable(c.count());

	}


	public MessageListAdapter(Cursor c, Context context,String feedType){
		super(c,context);
		
		initBoolTable(c.count());
		this.feedType=feedType;
	}

	public MessageListAdapter(Cursor c, Context context, boolean autoRequery,String feedType) {
		super(c,context,autoRequery);
		
		initBoolTable(c.count());
		this.feedType=feedType;

	}


	public void initBoolTable(int count){

		mExpanded=new boolean[count];				
		for (int i=0;i<count ;i++ )
		{
			mExpanded[i]=false;
		}
	}


	public void bindView(View view, Context context, Cursor cursor){
		Log.d(_TAG,"bindView::entering");
	}


	public void toggle(int position) {
		// TODO Auto-generated method stub
		mExpanded[position]=!mExpanded[position];
		notifyDataSetChanged();
		
	}


	public View getView(int position, View convertView, ViewGroup parent) {
		SimpleNewsMessageView smv;
		String message="";
		String title="";
		String link="";

		mCursor.moveTo(position);
		if (this.feedType.equals(News.FEED_TYPE_RSS))
		{
			message=mCursor.getString(mCursor.getColumnIndex(News.RSSFeedContents.ITEM_DESCRIPTION));
			title=mCursor.getString(mCursor.getColumnIndex(News.RSSFeedContents.ITEM_TITLE));
			link=mCursor.getString(mCursor.getColumnIndex(News.RSSFeedContents.ITEM_LINK));
		
		}else if (this.feedType.equals(News.FEED_TYPE_ATOM))
		{
			message=mCursor.getString(mCursor.getColumnIndex(News.AtomFeedContents.ENTRY_SUMMARY));
			title=mCursor.getString(mCursor.getColumnIndex(News.AtomFeedContents.ENTRY_TITLE));
			link=mCursor.getString(mCursor.getColumnIndex(News.AtomFeedContents.ENTRY_LINK));
		}else{
			Log.e(_TAG,"feedType not Set, will display empty view");
		}


		if (convertView==null)
		{
			smv=new SimpleNewsMessageView(mContext,"","",true);
			smv.setMessage(message);
			smv.setTitle(title);
			smv.setLink(link);			
			smv.setExpanded(mExpanded[position]);
		}
		else{
			smv=(SimpleNewsMessageView)convertView;
			smv.setMessage(message);
			smv.setTitle(title);
			smv.setLink(link);			
			smv.setExpanded(mExpanded[position]);
			
			
		}
					smv=new SimpleNewsMessageView(mContext,"","",true);
			smv.setMessage(message);
			smv.setTitle(title);
			smv.setLink(link);			
			smv.setExpanded(mExpanded[position]);
		return smv;
	}


 	public View newView(Context context, Cursor cursor, ViewGroup parent){
	
		Log.d(_TAG,"::newView: entering");
			
		return null;
	}


}/*eoc*/