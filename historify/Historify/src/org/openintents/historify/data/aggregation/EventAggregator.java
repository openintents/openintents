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

package org.openintents.historify.data.aggregation;

import java.util.ArrayList;

import org.openintents.historify.data.loaders.EventLoader;
import org.openintents.historify.data.loaders.SourceLoader;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.Event;
import org.openintents.historify.data.model.source.AbstractSource;
import org.openintents.historify.data.providers.Events;
import org.openintents.historify.uri.ContentUris;
import org.openintents.historify.utils.UriUtils;

import android.app.Activity;
import android.database.Cursor;

/**
 * 
 * This class is responsible for aggregating events provided by the
 * different sources, and converting their content into a single sequence of
 * events ordered by the {@link Events#PUBLISHED_TIME} field.
 * 
 * @author berke.andras
 */
public class EventAggregator {

	private Activity mContext;
	private Contact mContact;
	private EventLoader mLoader;
	
	private MergedCursor mMergedCursor;
	
	public EventAggregator(Activity context, Contact contact) {
		
		mContext = context;
		mContact = contact;
		mLoader = new EventLoader();
	}

	public void query() {

		//load enabled sources
		SourceLoader sourceLoader = new SourceLoader(ContentUris.Sources);
		ArrayList<AbstractSource> enabledSources = new ArrayList<AbstractSource>();
		
		Cursor sourcesCursor = sourceLoader.openManagedCursor(mContext, mContact);
		for(int i=0;i<sourcesCursor.getCount();i++) {
			AbstractSource source = sourceLoader.loadFromCursor(sourcesCursor, i);
			if(source.isEnabled()) enabledSources.add(source);
		}
		
		//open cursors for enabled sources
		MergedCursor.Builder builder = new MergedCursor.Builder(Events.PUBLISHED_TIME);
		EventLoader eventLoader = new EventLoader();
		for(AbstractSource source : enabledSources) {
			Cursor eventsCursor = eventLoader.openCursor(mContext, UriUtils.sourceAuthorityToUri(source.getAuthority()), mContact);
			if(eventsCursor!=null)
				builder.add(eventsCursor, source);
		}
		
		//create merged cursor from the source cursors
		mMergedCursor = builder.build();

	}
	
	public int getCount() {
		return mMergedCursor.getCount();
	}

	public Event getItem(int position) {
		
		Event retval = mLoader.loadFromCursor(mMergedCursor, position);
		if(retval!=null) {			
			AbstractSource source = mMergedCursor.getSource();
			retval.setSource(source);
			
		}
		
		return retval;
	}

	public void release() {
		mMergedCursor.release();
	}

}
