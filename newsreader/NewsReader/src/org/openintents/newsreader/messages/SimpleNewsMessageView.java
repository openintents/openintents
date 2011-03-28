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


import org.openintents.newsreader.R;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/*
 * Basic View for displaying News Messages. @author ronan 'zero' schwarz
 */

public class SimpleNewsMessageView extends LinearLayout {

	private TextView mMessage;
	private TextView mTitle;
	private TextView mLink;
	private LinearLayout mLinearLayout;

	public SimpleNewsMessageView(Context context, String title, String message,
			boolean expanded) {
		super(context);

		this.setOrientation(VERTICAL);

		// Here we build the child views in code. They could also have
		// been specified in an XML file.
		inflate(getContext(), R.layout.newsmessage_row, this);
		mTitle = (TextView) findViewById(R.id.title);
		mTitle.setText(title);		

		mMessage = (TextView) findViewById(R.id.message);
		mMessage.setText(message);

		mLink = (TextView) findViewById(R.id.link);

		mLinearLayout = (LinearLayout) findViewById(R.id.layout);
		
		setExpanded(expanded);
	}

	/**
	 * Convenience method to set the title of a NewsView
	 */
	public void setTitle(String title) {
		mTitle.setText(title);
	}

	public void setLink(String uri) {
		mLink.setText(uri);
	}

	public void setMessage(String message) {
		mMessage.setText(message);
	}

	/**
	 * Convenience method to expand or hide the message data
	 */
	public void setExpanded(boolean expanded) {
		mLink.setVisibility(expanded ? VISIBLE : GONE);
		mMessage.setVisibility(expanded ? VISIBLE : GONE);
		
		// Also change layout, so that mText can be aligned vertically
		mLinearLayout.setOrientation(expanded ? VERTICAL : HORIZONTAL);
	}

	public void setGrayedOut(boolean disabled) {
		mTitle.setEnabled(!disabled);
	}

}
