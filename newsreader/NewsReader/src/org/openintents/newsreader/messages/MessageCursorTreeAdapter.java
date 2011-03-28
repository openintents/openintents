package org.openintents.newsreader.messages;
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

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;
import android.database.Cursor;

import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.SimpleCursorTreeAdapter;
import android.app.ExpandableListActivity;

import org.openintents.provider.News;

//import org.openintents.news.views.*;

public class MessageCursorTreeAdapter extends SimpleCursorTreeAdapter {

	private String feedType = "";
	private static final String _TAG = "MySimpleCursorTreeAdapter";

	private int mIDRow = 0;
	private int mFeedIDRow = 0;
	private ContentResolver mContentResolver;

	public MessageCursorTreeAdapter(Context context, Cursor cursor,
			int groupLayout, int childLayout, String[] groupFrom,
			int[] groupTo, String[] childrenFrom, int[] childrenTo,
			String feedType) {

		super(context, cursor, groupLayout, groupFrom, groupTo, childLayout,
				childrenFrom, childrenTo);
		Log.d(_TAG, "feedType is >>" + feedType + "<<");
		this.feedType = feedType;
		this.mIDRow = cursor.getColumnIndexOrThrow(News.Contents._ID);
		this.mFeedIDRow = cursor
				.getColumnIndexOrThrow(News.Contents.CHANNEL_ID);
	}

	@Override
	protected Cursor getChildrenCursor(Cursor groupCursor) {
		Cursor childCursor = null;

		Log.d(_TAG, "EHLLLLLLLLLLLLLLLLLLLLLOOOOOOOOOOOOOOOOOO");
		Log.d(_TAG, "::getChildrenCursor: feedType is >>" + feedType + "<<");
		

		// if (feedType.equals(News.FEED_TYPE_RSS))
		// {
		// query(Uri uri, String[] projection, String selection, String[]
		// selectionArgs, String sortOrder)

		String channelID = groupCursor.getString(mFeedIDRow);
		String itemID = groupCursor.getString(mIDRow);
		Log.d(_TAG, "::getChildrenCursor: dumping groupCursor:\n"
				+ dumpCursor(groupCursor)
				+ "\n-------------------------------------");

		childCursor = mContentResolver.query(News.Contents.CONTENT_URI,
				SUB_PROJECTION, News.Contents.CHANNEL_ID + "="
						+ channelID + " AND " + News.Contents._ID + "="
						+ itemID, null, null);

		Log.d(_TAG, "::getChildrenCursor: dumping childCursor:\n"
				+ dumpCursor(childCursor)
				+ "\n-------------------------------------");
		/*
		 * }else if (feedType.equals(News.FEED_TYPE_ATOM)) {
		 * childCursor=managedQuery( News.AtomFeedContents.CONTENT_URI,
		 * FeedMessages.ATM_PROJECTION,
		 * News.AtomFeedContents.FEED_ID+"="+feedID+" AND "
		 * +News.AtomFeedContents._ID+"="+groupCursor.getString(mIDRow) ,null);
		 * 
		 * }
		 */
		return childCursor;
	}

	private String dumpCursor(Cursor c) {
		StringBuffer buf = new StringBuffer();
		int curLen = c.getCount();
		buf.append("\n------------------------------------------\n");
		buf.append("-c.count()>" + curLen + "<\n");
		buf.append("--columns:\n");
		String[] colNames = c.getColumnNames();
		for (int i1 = 0; i1 < colNames.length; i1++) {
			buf.append("---[" + i1 + "] >>" + colNames[i1] + "<< \n");
		}
		buf.append("--\n");

		buf.append("----rows:\n");

		c.moveToFirst();
		for (int i = 0; i < curLen; i++) {

			buf.append("---row[" + i + "]\n");
			for (int n = 0; n < colNames.length; n++) {
				buf.append("----[" + n + "] >>" + colNames[n] + "<< =>"
						+ c.getString(n) + "< \n");
			}
			buf.append("---\n");
		}

		buf.append("\n------------------------------------------");
		c.moveToFirst();
		return buf.toString();

	}

	private static String[] SUB_PROJECTION = new String[] {
			News.Contents._ID, News.Contents._COUNT,
			News.Contents.ITEM_LINK,
			News.Contents.ITEM_CONTENT };


}