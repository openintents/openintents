package org.openintents.newsreader.channels;

/*
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


import org.openintents.newsreader.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/*
 * Basic View for displaying media files with sync info. @author ronan 'zero'
 * schwarz
 */

public class ChannelListView extends LinearLayout {

	private TextView mCount;
	private TextView mTitle;
	

	private Context context;
	private int myType = 0;
	private ImageView mIcon;

	private LinearLayout leftrows;
	private LinearLayout rightrows;



	public static final String TAG = "ChannelListView";


	public ChannelListView(Context context) {
		super(context);
		this.context = context;
		this.setOrientation(HORIZONTAL);

		// inflate rating
		LayoutInflater inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout row_layout = (LinearLayout) inflater.inflate(
				R.layout.newsreader_channellist_row, null);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		addView(row_layout, lp);

		mTitle = (TextView) row_layout.findViewById(R.id.channelname);

		mCount = (TextView) row_layout.findViewById(R.id.channelcount);

		mIcon = (ImageView) row_layout.findViewById(R.id.icon);

		Log.d(TAG,"count view is>"+mCount+"< icon view is>"+mIcon+"<");

	}

	/**
	 * Convenience method to set the title of a NewsView
	 */
	public void setName(String title) {
		mTitle.setText(title);
	}

	public void setCount(int count) {

		mCount.setText(new String(""+count));
	}

	public void setType(int type) {
		this.myType = type;
	}

	public int getType() {
		return this.myType;
	}


	public void setIconBitmap(Bitmap bitmap) {
		if (bitmap != null) {
			mIcon.setImageBitmap(bitmap);
		}		
	}

	public void setIconDrawable(Drawable d) {
		if (d != null) {
			mIcon.setImageDrawable(d);
		}
	}

	public void setIconResource(int r) {
		
		mIcon.setImageResource(r);
		
	}

}
