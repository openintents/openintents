/* 
 * Copyright (C) 2011 OpenIntents.org
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
 */

package org.openintents.historify.ui;

import java.util.Date;

import org.openintents.historify.R;
import org.openintents.historify.data.loaders.EventLoader;
import org.openintents.historify.data.loaders.SourceIconHelper;
import org.openintents.historify.data.loaders.SourceLoader;
import org.openintents.historify.data.model.Event;
import org.openintents.historify.data.model.source.EventSource;
import org.openintents.historify.data.providers.Events;
import org.openintents.historify.data.providers.internal.Messaging;
import org.openintents.historify.data.providers.internal.QuickPosts;
import org.openintents.historify.data.providers.internal.Telephony;
import org.openintents.historify.data.providers.internal.QuickPosts.QuickPostSourcesTable;
import org.openintents.historify.uri.Actions;
import org.openintents.historify.uri.ContentUris;
import org.openintents.historify.utils.DateUtils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 
 * Activity for handling internal sources' event intents.
 * 
 * @author berke.andras
 */
public class EventIntentHandlerActivity extends Activity {

	private static final String N = "EventIntentHandler";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_event_intent_handler);
		
		String action = getIntent().getAction();
		
		if(Actions.ACTION_VIEW_MESSAGING_EVENT.equals(action)) {
			handleInternalEventIntent(Messaging.SOURCE_URI, getIntent().getLongExtra(Actions.EXTRA_EVENT_ID, -1));
		} else if(Actions.ACTION_VIEW_CALLOG_EVENT.equals(action)) {
			handleInternalEventIntent(Telephony.SOURCE_URI, getIntent().getLongExtra(Actions.EXTRA_EVENT_ID, -1));
		} else if(Actions.ACTION_VIEW_QUICKPOST_EVENT.equals(action)) {
			handleQuickPostEventIntent();
		} else {
			finish();
		}
		
	}

	
	private void handleInternalEventIntent(Uri eventSource, long eventId) {
		
		if(eventId!=-1) {
			Event event = null;
			EventSource source = null;
			EventLoader eventLoader = new EventLoader();
			Cursor ec = eventLoader.openCursor(this, eventSource, eventId);
			if(ec.getCount()!=0) {
				event = eventLoader.loadFromCursor(ec,0);
				
				SourceLoader sourceLoader = new SourceLoader(ContentUris.Sources);
				Cursor sc = sourceLoader.openManagedCursor(this, eventSource);
				if(sc.getCount()!=0) {
					source = sourceLoader.loadFromCursor(sc, 0);
					if(source!=null)
						event.setSource(source);
				}
			}
			ec.close();
			
			if(event!=null && source!=null) {
				loadEventToView(event);
				initViewButton();
				return;
			}
		}
		
		finish();
	}
	
	private void initViewButton() {
		
		String lookupKey = getIntent().getStringExtra(Actions.EXTRA_CONTACT_LOOKUP_KEY);		
		Uri contactUri = 
			Contacts.lookupContact(getContentResolver(),
					Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, lookupKey));
		
		final Intent i = new Intent();
		i.setAction(Intent.ACTION_VIEW);
		i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setData(contactUri);
		
		View btnView = findViewById(R.id.event_intent_handler_btnView);
		btnView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(i);
				finish();
			}
		});
	}


	private void loadEventToView(Event event) {
		
		TextView tv= (TextView) findViewById(R.id.timeline_listitem_txtMessage);
		tv.setText(event.getMessage());

		tv = (TextView) findViewById(R.id.timeline_listitem_txtDate);
		tv.setText(DateUtils.formatDate(new Date(event.getPublishedTime())));

		ImageView iv = (ImageView)findViewById(R.id.timeline_listitem_imgIcon);
		new SourceIconHelper().toImageView(this, event.getSource(), event, iv);

	}


	private void handleQuickPostEventIntent() {
		
		long eventId = getIntent().getLongExtra(Actions.EXTRA_EVENT_ID, -1);
		if(eventId!=-1) {
			
			//query the event's EVENT_INTENT as registered by the QuickPost client app
			Uri eventUri = Uri.withAppendedPath(QuickPosts.SOURCE_URI,Events.EVENTS_PATH+"/"+eventId);
			
			String intentAction = null;
			Cursor c = getContentResolver().query(eventUri, null, null, null, null);
			if(c.moveToFirst()) {
				intentAction = c.getString(c.getColumnIndex(QuickPostSourcesTable.EVENT_INTENT));
			}
			c.close();
			
			//fire intent if action is not null
			if(intentAction!=null) {
				Intent i = new Intent();
				i.setAction(intentAction);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
				i.putExtra(Actions.EXTRA_EVENT_KEY, getIntent().getStringExtra(Actions.EXTRA_EVENT_KEY));
				try {
					startActivity(i);	
				} catch(ActivityNotFoundException e) {
					Log.e(N, "Unable to launch Activity to handle action: "+intentAction);
				}
				
			}
		}
		 
		finish();
	}
}
