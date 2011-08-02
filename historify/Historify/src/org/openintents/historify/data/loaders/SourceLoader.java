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
import org.openintents.historify.data.model.source.EventSource;
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
 * Helper class for loading and updating {@link EventSource} objects.
 * 
 * @author berke.andras
 */
public class SourceLoader {

	public static String[] DEFAULT_PROJECTION = new String[] {
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
		
	public static String[] BASIC_COLUMNS_PROJECTION = new String[] {
		QuickPosts.QuickPostSourcesTable._ID,
		QuickPosts.QuickPostSourcesTable.NAME,
		QuickPosts.QuickPostSourcesTable.DESCRIPTION,
		QuickPosts.QuickPostSourcesTable.ICON_URI,
		QuickPosts.QuickPostSourcesTable.EVENT_INTENT,
	};
	
	protected static final int COLUMN_ID = 0;
	protected static final int COLUMN_NAME = 1;
	protected static final int COLUMN_DESCRIPTION = 2;
	protected static final int COLUMN_ICON_URI = 3;
	protected static final int COLUMN_EVENT_INTENT = 4;
	protected static final int COLUMN_ICON_LOADING_STRATEGY = 5;
	protected static final int COLUMN_AUTHORITY = 6;
	protected static final int COLUMN_CONFIG_INTENT = 7;
	protected static final int COLUMN_IS_INTERNAL = 8;
	protected static final int COLUMN_STATE = 9;
	
	private Uri mSourcesUri;
	private String[] mProjection;
	
	public SourceLoader() {}
	
	public SourceLoader(Uri sourcesUri, String[] projection) {
		init(sourcesUri, projection);
	}
		
	public SourceLoader(Uri sourcesUri) {
		init(sourcesUri, DEFAULT_PROJECTION);
	}

	protected void init(Uri sourcesUri, String[] projection) {
		mSourcesUri = sourcesUri;
		mProjection = projection;		
	}
	
	public Cursor openCursor(Context context) {
		
		String selection = null;
		String[] selectionArgs = null;
								 
		return context.getContentResolver().query(mSourcesUri, mProjection, selection, selectionArgs, Sources.SourcesTable.NAME);
	}
	
	public Cursor openManagedCursor(Activity context, Uri sourceUri) {
		
		String selection = SourcesTable.AUTHORITY + " = ?";
		String[] selectionArgs = new String[] {
			sourceUri.getAuthority()	
		};
						 
		return context.managedQuery(mSourcesUri, mProjection, selection, selectionArgs, Sources.SourcesTable.NAME);
	}
	
	
	public EventSource loadFromCursor(Cursor cursor, int position) {
		
		cursor.moveToPosition(position);

		EventSource retval;
		
		if(mProjection==BASIC_COLUMNS_PROJECTION) {
			
			retval = EventSource.factoryMethod(
					false,
					cursor.getLong(COLUMN_ID), 
					cursor.getString(COLUMN_NAME),
					cursor.isNull(COLUMN_DESCRIPTION) ? null : cursor.getString(COLUMN_DESCRIPTION),
					cursor.isNull(COLUMN_ICON_URI) ? null : cursor.getString(COLUMN_ICON_URI),
					null, null, null, null, null);
		} else {

			retval = EventSource.factoryMethod(
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
		}
				
		return retval;
		
	}

	public void update(Context context, EventSource source) {

		Uri sourceUri = ContentUris.Sources.buildUpon().appendPath(String.valueOf(source.getId())).build();
		
		ContentValues cv = new ContentValues();
		cv.put(Sources.SourcesTable.STATE, source.getState().toString());
		
		context.getContentResolver().update(sourceUri, cv, null, null);
	}

	
}
