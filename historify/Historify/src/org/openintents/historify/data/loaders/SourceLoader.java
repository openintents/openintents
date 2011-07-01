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

package org.openintents.historify.data.loaders;

import org.openintents.historify.data.loaders.SourceIconHelper.IconLoadingStrategy;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.data.model.source.AbstractSource;
import org.openintents.historify.data.model.source.SourceFilter;
import org.openintents.historify.data.model.source.AbstractSource.SourceState;
import org.openintents.historify.data.providers.Sources;
import org.openintents.historify.data.providers.Sources.SourcesTable;
import org.openintents.historify.data.providers.internal.QuickPosts;
import org.openintents.historify.uri.ContentUris;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * 
 * Helper class for loading and updating {@link AbstractSource} objects.
 * 
 * @author berke.andras
 */
public class SourceLoader {

	public static String[] SOURCES_PROJECTION = new String[] {
		Sources.SourcesTable._ID,
		Sources.SourcesTable.NAME,
		Sources.SourcesTable.DESCRIPTION,
		Sources.SourcesTable.ICON_URI,
		Sources.SourcesTable.EVENT_INTENT,
		Sources.SourcesTable.ICON_LOADING_STRATEGY,
		Sources.SourcesTable.AUTHORITY,
		Sources.SourcesTable.CONFIG_INTENT,
		Sources.SourcesTable.IS_INTERNAL,
		Sources.SourcesTable.STATE 
	};
	
	public static String[] FILTERED_SOURCES_PROJECTION = new String[] {
		Sources.SourcesTable._ID,
		Sources.SourcesTable.NAME,
		Sources.SourcesTable.DESCRIPTION,
		Sources.SourcesTable.ICON_URI,
		Sources.SourcesTable.EVENT_INTENT,
		Sources.SourcesTable.ICON_LOADING_STRATEGY,
		Sources.SourcesTable.AUTHORITY,
		Sources.SourcesTable.CONFIG_INTENT,
		Sources.SourcesTable.IS_INTERNAL,
		Sources.SourcesTable.STATE,
		Sources.FiltersTable._ID,
		Sources.FiltersTable.FILTERED_STATE
	};
	
	public static String[] SIMPLE_SOURCES_PROJECTION = new String[] {
		QuickPosts.QuickPostSourcesTable._ID,
		QuickPosts.QuickPostSourcesTable.NAME,
		QuickPosts.QuickPostSourcesTable.DESCRIPTION,
		QuickPosts.QuickPostSourcesTable.ICON_URI,
		QuickPosts.QuickPostSourcesTable.EVENT_INTENT,
	};
	
	private static final int COLUMN_ID = 0;
	private static final int COLUMN_NAME = 1;
	private static final int COLUMN_DESCRIPTION = 2;
	private static final int COLUMN_ICON_URI = 3;
	private static final int COLUMN_EVENT_INTENT = 4;
	private static final int COLUMN_ICON_LOADING_STRATEGY = 5;
	private static final int COLUMN_AUTHORITY = 6;
	private static final int COLUMN_CONFIG_INTENT = 7;
	private static final int COLUMN_IS_INTERNAL = 8;
	private static final int COLUMN_STATE = 9;
	private static final int COLUMN_FILTER_ID = 10;
	private static final int COLUMN_FILTERED_STATE = 11;
	
	private Uri mSourcesUri;
	private boolean mBasicColumnsOnly;
	
	public SourceLoader(boolean basicColumnsOnly, Uri sourcesUri) {
		mBasicColumnsOnly = basicColumnsOnly;
		mSourcesUri = sourcesUri;
	}
		
	public SourceLoader(Uri sourcesUri) {
		mBasicColumnsOnly = false;
		mSourcesUri = sourcesUri;
	}

	public Cursor openManagedCursor(Activity context, Contact filterModeContact) {
		
		String selection = null;
		String[] selectionArgs = null;
		
		Uri uri = null;
		String[] projection = null;
		
		if(filterModeContact!=null) {

			uri = mSourcesUri.buildUpon()
				.appendPath(ContentUris.FILTERED_SOURCES_PATH)
				.appendPath(filterModeContact.getLookupKey())
				.build();
			projection = FILTERED_SOURCES_PROJECTION;
			
		} else {
			
			uri = mSourcesUri;
			projection = mBasicColumnsOnly ?  SIMPLE_SOURCES_PROJECTION : SOURCES_PROJECTION;
		}
				 
		return context.managedQuery(uri, projection, selection, selectionArgs, Sources.SourcesTable.NAME);
	}
	
	public Cursor openManagedCursor(Activity context, Uri sourceUri) {
		
		String selection = SourcesTable.AUTHORITY + " = ?";
		String[] selectionArgs = new String[] {
			sourceUri.getAuthority()	
		};
		
		Uri uri = null;
		String[] projection = null;
					
		uri = mSourcesUri;
		projection = mBasicColumnsOnly ?  SIMPLE_SOURCES_PROJECTION : SOURCES_PROJECTION;
				 
		return context.managedQuery(uri, projection, selection, selectionArgs, Sources.SourcesTable.NAME);
	}
	
	
	public AbstractSource loadFromCursor(Cursor cursor, int position) {
		
		cursor.moveToPosition(position);

		AbstractSource retval;
		
		if(mBasicColumnsOnly) {
			
			retval = AbstractSource.factoryMethod(
					false,
					cursor.getLong(COLUMN_ID), 
					cursor.getString(COLUMN_NAME),
					cursor.isNull(COLUMN_DESCRIPTION) ? null : cursor.getString(COLUMN_DESCRIPTION),
					cursor.isNull(COLUMN_ICON_URI) ? null : cursor.getString(COLUMN_ICON_URI),
					null, null, null, null, null);
		} else {

			retval = AbstractSource.factoryMethod(
					cursor.getInt(COLUMN_IS_INTERNAL)>0,
					cursor.getLong(COLUMN_ID), 
					cursor.getString(COLUMN_NAME),
					cursor.isNull(COLUMN_DESCRIPTION) ? null : cursor.getString(COLUMN_DESCRIPTION),
					cursor.isNull(COLUMN_ICON_URI) ? null : cursor.getString(COLUMN_ICON_URI),
					IconLoadingStrategy.parseString(cursor.getString(COLUMN_ICON_LOADING_STRATEGY)),
					cursor.getString(COLUMN_AUTHORITY),
					cursor.isNull(COLUMN_EVENT_INTENT) ? null : cursor.getString(COLUMN_EVENT_INTENT),
					cursor.isNull(COLUMN_CONFIG_INTENT) ? null : cursor.getString(COLUMN_CONFIG_INTENT),
					cursor.getString(COLUMN_STATE));
			
			long filterId = cursor.isNull(COLUMN_FILTER_ID) ? -1 : cursor.getLong(COLUMN_FILTER_ID);
			if(filterId>-1) {
				SourceFilter filter = new SourceFilter(
						filterId, 
						SourceState.parseString(cursor.getString(COLUMN_FILTERED_STATE)));
				filter.setSource(retval);
				retval.setSourceFilter(filter);
			}

		}
				
		return retval;
		
	}

	public void update(Context context, AbstractSource source) {

		Uri sourceUri = ContentUris.Sources.buildUpon().appendPath(String.valueOf(source.getId())).build();
		
		ContentValues cv = new ContentValues();
		cv.put(Sources.SourcesTable.STATE, source.getState().toString());
		
		context.getContentResolver().update(sourceUri, cv, null, null);
	}

	
}
