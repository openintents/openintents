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
import android.content.Context;
import android.widget.LinearLayout.LayoutParams;


/*
 *Basic View for displaying News Messages.
 *@author ronan 'zero' schwarz
 */

public class SimpleNewsMessageView extends LinearLayout{

	private TextView mMessage;
	private TextView mTitle;
	private TextView mLink;
	
	public SimpleNewsMessageView(Context context, String title, String message, boolean expanded) {
		super(context);
		
		this.setOrientation(VERTICAL);
		
		// Here we build the child views in code. They could also have
		// been specified in an XML file.
		
		mTitle = new TextView(context);
		mTitle.setText(title);
		addView(mTitle, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		mMessage = new TextView(context);
		mMessage.setText(message);
		addView(mMessage, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		mLink = new TextView(context);
		
		addView(mLink, new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		
		mLink.setVisibility(expanded ? VISIBLE : GONE);
		mMessage.setVisibility(expanded ? VISIBLE : GONE);
	}
	
	/**
	 * Convenience method to set the title of a NewsView
	 */
	public void setTitle(String title) {
		mTitle.setText(title);
	}
	
	public void setLink(String uri){
		mLink.setText(uri);
	}
	
	public void setMessage(String message){
		mMessage.setText(message);
	}
	/**
	 * Convenience method to expand or hide the message data
	 */
	public void setExpanded(boolean expanded) {
		mLink.setVisibility(expanded ? VISIBLE : GONE);
		mMessage.setVisibility(expanded ? VISIBLE : GONE);
		
	}


}
