package org.openintents.newsreader.channels;

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


import java.util.HashMap;
import java.util.List;

import org.openintents.newsreader.R;
import org.openintents.provider.News;

import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/*
 * 
 * @author ronan 'zero' schwarz
 */
public class ChannelListAdapter extends CursorAdapter {

	private List<HashMap> data;

	private static final String TAG = "ChannelListAdapter";

	/**
	 * Busy: During scrolling thumbnails will not be retrieved.
	 */
	public boolean mBusy = false;

	private int mAdapterKey;

	public ChannelListAdapter(Context context, Cursor c){
		super(context, c);
		mBusy = false;		
		//mThread = thread;
		//mAdapterKey = adapterKey;
	}

	@Override
	public void notifyDataSetChanged() {
		Log.v(TAG, "notifyDataSetChanged");
		super.notifyDataSetChanged();
	}

	@Override
	public void notifyDataSetInvalidated() {
		Log.v(TAG, "notifyDataSetInvalidated");
		super.notifyDataSetInvalidated();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		final ChannelListView smv = (ChannelListView) view;

		
		int id = cursor.getInt(cursor
				.getColumnIndexOrThrow(News.Channel._ID));

		if (smv.getId() == id) {
			// nothing to do - receyceled for the same id.
			return;
		}

		int type = cursor.getInt(cursor
				.getColumnIndexOrThrow(News.Channel.CHANNEL_TYPE));
		String name = cursor.getString(cursor
				.getColumnIndexOrThrow(News.Channel.CHANNEL_NAME));
		String iconUri = cursor.getString(cursor
				.getColumnIndexOrThrow(News.Channel.CHANNEL_ICON_URI));
		
		int count = cursor.getInt(cursor
				.getColumnIndexOrThrow(News.Channel.CONTENT_COUNT));



		smv.setType(type);
		smv.setName(name);
		smv.setCount(count);

		Log.d(TAG,"iconuri>"+iconUri+"<");
		
		if (iconUri==null || iconUri.equals(""))
		{
			Log.d(TAG,"type>"+type+"<");

			if (type==News.CHANNEL_TYPE_ATM)
			{
				smv.setIconResource(R.drawable.atom_icon_small);
			}else if (type==News.CHANNEL_TYPE_RSS)
			{
				smv.setIconResource(R.drawable.rss_icon_small);
			}
		}else{
			try
			{				
				final BitmapFactory.Options bmfo=new BitmapFactory.Options();
				bmfo.outHeight=28;
				bmfo.outWidth=28;
				smv.setIconBitmap(BitmapFactory.decodeFile(iconUri,bmfo));				
			}
			catch (Exception e)
			{
				Log.e(TAG,"error reading icon,"+e.getMessage());
				if (type==News.CHANNEL_TYPE_ATM)
				{
					smv.setIconResource(R.drawable.atom_icon_small);
				}else if (type==News.CHANNEL_TYPE_RSS)
				{
					smv.setIconResource(R.drawable.rss_icon_small);
				}
			}

		}

	}


/*
	public View getView(int position, View convertView, ViewGroup parent) {
		SimpleNewsChannelView smv;

		HashMap cData = (HashMap) this.data.get(position);
		if (convertView == null) {

			smv = new SimpleNewsChannelView(this.context, cData);
		} else {
			smv = (SimpleNewsChannelView) convertView;
			smv.setData(cData);

		}

		return smv;
	}
	`*/

	public View newView(Context context, Cursor cursor, ViewGroup parent) {

		return new ChannelListView(context);
	}

	public int getType(int position) {
		if (getCursor().moveToPosition(position)) {
			return getCursor()
					.getInt(
							getCursor().getColumnIndexOrThrow(
									News.Channel.CHANNEL_TYPE));
		} else {
			return News.CHANNEL_TYPE_UNSUPPORTED;
		}
	}


}