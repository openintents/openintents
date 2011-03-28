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
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class SingleMessageWebView extends LinearLayout {

	private WebView mWebView;

	private Button mGotoButton;

	private AFeedMessages context;

	private String mMessageLink;

	private Button mPrevButton;

	private Button mNextButton;

	public SingleMessageWebView(final AFeedMessages context) {
		super(context);

		this.context = context;

		this.setOrientation(HORIZONTAL);

		// inflate rating
		LayoutInflater inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		LinearLayout internalLayout = (LinearLayout) inflater.inflate(
				R.layout.newsreader_showmessage, null);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		addView(internalLayout, lp);

		mWebView = (WebView) internalLayout
				.findViewById(R.id.newsreader_showmessage_wv);
		mGotoButton = (Button) internalLayout
				.findViewById(R.id.newsreader_showmessage_goto);

		mGotoButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				followLink();
			}
		});

		mPrevButton = (Button) internalLayout
				.findViewById(R.id.newsreader_showmessage_prev);

		mPrevButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				context.showPrevious(SingleMessageWebView.this);
			}
		});

		mNextButton = (Button) internalLayout
				.findViewById(R.id.newsreader_showmessage_next);

		mNextButton.setOnClickListener(new Button.OnClickListener() {

			public void onClick(View v) {
				context.showNext(SingleMessageWebView.this);
			}
		});

	}

	public void loadData(String data, String mimeType, String encoding) {

		mWebView.loadData(data, mimeType, encoding);
	}

	public void loadDataWithBaseURL(String baseUrl, String data, String mimeType, String encoding, String failUrl) {

		mWebView.loadDataWithBaseURL(baseUrl, data, mimeType, encoding, failUrl);
	}

	public void loadUrl(String url) {
		mWebView.loadUrl(url);
	}

	public void setMessageLink(String mMessageLink) {
		this.mMessageLink = mMessageLink;
	}

	private void followLink() {
		if (mMessageLink != null) {
			Uri uri = Uri.parse(mMessageLink);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			context.startActivity(intent);
		} else {
			Toast.makeText(context, R.string.invalid_link, Toast.LENGTH_SHORT).show();
		}
	}

	public void disableButtons(boolean first, boolean last) {
		mPrevButton.setEnabled(!first);
		mNextButton.setEnabled(!last);
	}

}