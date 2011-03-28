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


import android.database.DataSetObserver;
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
import org.openintents.provider.News.Contents;

/*
 *
 *@author ronan 'zero' schwarz
 */
public class MessageListAdapter extends CursorAdapter {

	protected boolean[] mExpanded;
	private String feedType;
	private Context mContext;

	private static final String _TAG = "MessgeListAdapter";

	public MessageListAdapter(Cursor c, Context context) {
		super(context, c);
		initBoolTable(c.getCount());
		mContext = context;
	}

	public MessageListAdapter(Cursor c, Context context, boolean autoRequery) {
		super(context, c, autoRequery);
		registerDataSetObserver(new MyObserver(this));
		initBoolTable(c.getCount());
		mContext = context;
	}

	public MessageListAdapter(Cursor c, Context context, String feedType) {
		super(context, c);
		registerDataSetObserver(new MyObserver(this));
		initBoolTable(c.getCount());
		this.feedType = feedType;
		mContext = context;
	}

	public MessageListAdapter(Cursor c, Context context, boolean autoRequery,
			String feedType) {
		super(context, c, autoRequery);
		registerDataSetObserver(new MyObserver(this));
		initBoolTable(c.getCount());
		this.feedType = feedType;
		mContext = context;
	}

	public void initBoolTable(int count) {

		mExpanded = new boolean[count];
		for (int i = 0; i < count; i++) {
			mExpanded[i] = false;
		}
	}

	public void bindView(View view, Context context, Cursor cursor) {
		Log.d(_TAG, "bindView::entering");
	}

	public void toggle(int position) {
		// never used
		mExpanded[position] = !mExpanded[position];
		notifyDataSetChanged();

	}

	public View getView(int position, View convertView, ViewGroup parent) {
		SimpleNewsMessageView smv;
		String message = "";
		String title = "";
		String link = "";

		getCursor().moveToPosition(position);

		Log.e(_TAG, "feedType:" + this.feedType);
		message = getCursor().getString(
				getCursor().getColumnIndexOrThrow(News.Contents.ITEM_CONTENT));
		title = getCursor().getString(
				getCursor().getColumnIndexOrThrow(News.Contents.ITEM_TITLE));
		link = getCursor().getString(
				getCursor().getColumnIndexOrThrow(News.Contents.ITEM_LINK));
		int readStatus = getCursor().getInt(
				getCursor().getColumnIndexOrThrow(Contents.READ_STATUS));
		if (convertView == null) {
			smv = new SimpleNewsMessageView(mContext, "", "", true);
			Log.v("MessageListAdapter", "new view created");
		} else {
			smv = (SimpleNewsMessageView) convertView;

			Log.v("MessageListAdapter", "view reused");
		}

		smv.setGrayedOut(readStatus == 1);
		smv.setMessage(message);
		smv.setTitle(title);
		smv.setLink(link);
		smv.setExpanded(mExpanded[position]);
		return smv;
	}

	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		Log.d(_TAG, "::newView: entering");

		return null;
	}

	protected static final class MyObserver extends DataSetObserver {

		MessageListAdapter parent;

		public MyObserver(MessageListAdapter parent) {
			this.parent = parent;
		}

		public void onChanged() {
			Log.d("MessageListAdapter.MyObserver", "onChange called");
			Log.d("MessageListAdapter.MyObserver", "my Parent cursor count>>"
					+ this.parent.getCursor().getCount());
			Log.d("MessageListAdapter.MyObserver",
					"my Parent boolTable count>>"
							+ this.parent.mExpanded.length);
			if (this.parent.getCursor().getCount() != this.parent.mExpanded.length) {
				this.parent.initBoolTable(this.parent.getCursor().getCount());
			}

		}

		public void onInvalidated() {
		}
	};

}/* eoc */