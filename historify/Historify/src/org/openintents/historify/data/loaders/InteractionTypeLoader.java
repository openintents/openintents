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

import org.openintents.historify.data.model.source.EventSource;
import org.openintents.historify.data.model.source.InteractionType;
import org.openintents.historify.data.model.source.EventSource.SourceState;
import org.openintents.historify.data.providers.Sources;
import org.openintents.historify.data.providers.internal.QuickPosts;
import org.openintents.historify.uri.ContentUris;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;

public class InteractionTypeLoader {

	public static String[] PROJECTION = new String[] {
		Sources.SourcesTable.NAME,
		Sources.SourcesTable.ICON_URI,
		Sources.SourcesTable.INTERACT_INTENT,
		Sources.SourcesTable.INTERACT_ACTION_TITLE
	};
		
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_ICON_URI = 1;
	private static final int COLUMN_INTERACT_INTENT = 2;
	private static final int COLUMN_INTERACT_ACTION_TITLE = 3;
	
	public Cursor openCursor(Activity context) {
		
		//load interaction types for external sources
		StringBuilder selection = new StringBuilder();
		selection.append(Sources.SourcesTable.STATE);
		selection.append(" = '");
		selection.append(SourceState.ENABLED);
		selection.append("' AND ");
		selection.append(Sources.SourcesTable.INTERACT_INTENT);
		selection.append(" NOT NULL");
		
		Cursor cursorSources = context.getContentResolver().query(ContentUris.Sources, PROJECTION, selection.toString(), null, Sources.SourcesTable.NAME);
		
		//load interaction types for Q! sources
		if(isQuickPostSourceAvailable(context)) {
			
			selection = new StringBuilder();
			selection.append(QuickPosts.QuickPostSourcesTable.INTERACT_INTENT);
			selection.append(" NOT NULL");
			
			Cursor cursorQpSources = context.getContentResolver().query(ContentUris.QuickPostSources, PROJECTION, selection.toString(), null, QuickPosts.QuickPostSourcesTable.NAME);
			if(cursorQpSources!=null) {
				
//				cursorQpSources.moveToFirst();
//				for(String s : cursorQpSources.getColumnNames()) {
//					Log.v(s," "+cursorQpSources.getString(cursorQpSources.getColumnIndex(s)));
//				}
			
				
				return new MergeCursor(new Cursor[] {cursorSources, cursorQpSources});
			}	
		} else {
			return cursorSources;
		}
		
		return null;
	}

	private boolean isQuickPostSourceAvailable(Context context) {
		
		SourceLoader sourceLoader = new SourceLoader(ContentUris.Sources);
		EventSource source = sourceLoader.loadFromSourceAuthority(context, QuickPosts.QUICKPOSTS_AUTHORITY);
		return source!=null && source.isEnabled();
	}

	public InteractionType loadFromCursor(Cursor cursor, int pos) {
		
		cursor.moveToPosition(pos);
		
		Uri iconUri = Uri.parse(cursor.getString(COLUMN_ICON_URI)); 
		String actionTitle = cursor.isNull(COLUMN_INTERACT_ACTION_TITLE) ? 
				cursor.getString(COLUMN_NAME) : 
				cursor.getString(COLUMN_INTERACT_ACTION_TITLE);
		String intentAction = cursor.getString(COLUMN_INTERACT_INTENT);
		
		return new InteractionType(iconUri, actionTitle, intentAction);
	}

}
