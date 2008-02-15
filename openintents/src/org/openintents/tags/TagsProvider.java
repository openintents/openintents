/* 
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
 */

package org.openintents.tags;

import java.util.HashMap;

import org.openintents.provider.Tag.Contents;
import org.openintents.provider.Tag.Tags;

import android.content.ContentProvider;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.UriMatcher;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.sqlite.SQLiteQueryBuilder;
import android.content.Resources;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

/**
 * Provides access to a database of tags and contents. Each tag has a tag_id and
 * a content_id, a creation date and a modified data. Both ids refer to an entry
 * to the contents table. A content row has a uri and a type.
 * 
 */
public class TagsProvider extends ContentProvider {

	private SQLiteDatabase mDB;

	private static final String TAG = "TagsProvider";
	private static final String DATABASE_NAME = "tags.db";
	private static final int DATABASE_VERSION = 1; // Release 0.1.0

	private static HashMap<String, String> TAG_PROJECTION_MAP;
	private static HashMap<String, String> CONTENT_PROJECTION_MAP;

	private static final int TAGS = 1;
	private static final int TAG_ID = 2;
	private static final int CONTENTS = 3;
	private static final int CONTENT_ID = 4;

	private static final UriMatcher URL_MATCHER;

	private static final String DEFAULT_TAG = "DEFAULT";

	private static class DatabaseHelper extends SQLiteOpenHelper {
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE content (_id INTEGER PRIMARY KEY,"
					+ "uri VARCHAR," + "type VARCHAR," + "created INTEGER"
					+ ");");
			db.execSQL("CREATE TABLE tag (_id INTEGER PRIMARY KEY,"
					+ "tag_id LONG," + "content_id LONG," + "created INTEGER,"
					+ "modified INTEGER," + "accessed INTEGER" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS content");
			db.execSQL("DROP TABLE IF EXISTS tag");
			onCreate(db);
		}
	}

	@Override
	public boolean onCreate() {
		DatabaseHelper dbHelper = new DatabaseHelper();
		mDB = dbHelper.openDatabase(getContext(), DATABASE_NAME, null,
				DATABASE_VERSION);
		return mDB != null;
	}

	public Cursor query(Uri url, String[] projection, String selection,
			String[] selectionArgs, String sort) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

		String defaultOrderBy = null;
		switch (URL_MATCHER.match(url)) {
		case TAGS:
			// queries for tags also return the uris, not only the ids.
			qb.setTables("tag tag, content content1, content content2");
			qb.setProjectionMap(TAG_PROJECTION_MAP);
			qb.appendWhere("tag.tag_id = content1._id AND "
					+ "tag.content_id = content2._id");
			defaultOrderBy = Tags.DEFAULT_SORT_ORDER;
			break;

		case TAG_ID:
			// queries for a tag just returns the ids.
			qb.setTables("tag");
			qb.appendWhere("_id=" + url.getPathSegments().get(1));
			break;

		case CONTENTS:
			qb.setTables("content");
			qb.setProjectionMap(CONTENT_PROJECTION_MAP);
			defaultOrderBy = Contents.DEFAULT_SORT_ORDER;
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		// If no sort order is specified use the default

		String orderBy;
		if (TextUtils.isEmpty(sort)) {
			orderBy = defaultOrderBy;
		} else {
			orderBy = sort;
		}

		Cursor c = qb.query(mDB, projection, selection, selectionArgs, null,null, orderBy);
		c.setNotificationUri(getContext().getContentResolver(), url);
		return c;
	}

	@Override
	public Uri insert(Uri url, ContentValues initialValues) {
		long rowID;
		ContentValues values;
		if (initialValues != null) {
			values = new ContentValues(initialValues);

		} else {
			values = new ContentValues();
		}

		// insert is only supported for tags
		if (URL_MATCHER.match(url) != TAGS) {
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		Long now = Long.valueOf(System.currentTimeMillis());
		Resources r = Resources.getSystem();

		// Make sure that the fields are all set
		if (!values.containsKey(Tags.CREATED_DATE)) {
			values.put(Tags.CREATED_DATE, now);
		}

		if (!values.containsKey(Tags.MODIFIED_DATE)) {
			values.put(Tags.MODIFIED_DATE, now);
		}

		if (!values.containsKey(Tags.ACCESS_DATE)) {
			values.put(Tags.ACCESS_DATE, now);
		}

		// lookup id for uri or create new
		replaceUriById(values, Tags.TAG_ID, Tags.URI_1, "TAG", DEFAULT_TAG);

		if (!values.containsKey(Tags.CONTENT_ID)
				&& !values.containsKey(Tags.URI_2)) {
			new SQLException("missing uri or content_id for insert " + url);
		}
		// lookup id for uri or create new
		replaceUriById(values, Tags.CONTENT_ID, Tags.URI_2, null, DEFAULT_TAG);

		// check whether tag already exists
		Cursor existingTag = mDB.query("tag", new String[] { Tags._ID },
				"tag_id = ? AND content_id = ?", new String[] {
						String.valueOf(values.get(Tags.TAG_ID)),
						String.valueOf((String) values.get(Tags.CONTENT_ID)) },
				null, null, null);

		if (!existingTag.next()) {
			// finally insert the tag.
			rowID = mDB.insert("tag", "tag", values);
			if (rowID > 0) {
				Uri uri = ContentUris.withAppendedId(Tags.CONTENT_URI,rowID);
				getContext().getContentResolver().notifyChange(uri, null);
				return uri;
			}
			throw new SQLException("Failed to insert row into " + url);
		} else {
			return null;
		}

	}

	/**
	 * lookup content id for given uri or create new entry in content table if
	 * not present
	 * 
	 * @param values
	 * @param idColumnName
	 * @param uriColumnName
	 * @param tagType
	 * @param defaultValue
	 */
	private void replaceUriById(ContentValues values, String idColumnName,
			String uriColumnName, String tagType, String defaultValue) {
		if (!values.containsKey(idColumnName)) {
			String tagUri;
			if (values.containsKey(uriColumnName)) {
				tagUri = values.getAsString(uriColumnName);
				values.remove(uriColumnName);
			} else {
				tagUri = defaultValue;
			}
			Cursor existingTag = mDB.query("content",
					new String[] { Contents._ID }, "uri = ?",
					new String[] { tagUri }, null, null, null);

			String contentId;
			if (!existingTag.next()) {
				contentId = String.valueOf(insertContent(tagUri, tagType));
			} else {
				contentId = existingTag.getString(0);
			}
			values.put(idColumnName, contentId);
		}
	}

	/**
	 * create new entry in content table
	 * 
	 * @param uri
	 * @param type
	 * @return
	 */
	private long insertContent(String uri, String type) {
		ContentValues values = new ContentValues();
		values.put(Contents.URI, uri);
		if (type != null) {
			values.put(Contents.TYPE, type);
		}
		long rowId = mDB.insert("content", "content", values);
		return rowId;
	}

	@Override
	public int delete(Uri url, String where, String[] whereArgs) {
		int count;
		long rowId = 0;		
		switch (URL_MATCHER.match(url)) {
		case TAGS:						
			where = "tag.tag_id = (select content1._id FROM content content1 WHERE content1.uri = ?) " +
					"AND tag.content_id = (select content2._id FROM content content2 WHERE content2.uri = ?)";
			
			count = mDB.delete("tag", where, whereArgs);

			// TODO remove unreferenced content
			break;

		case TAG_ID:
			String segment = url.getPathSegments().get(1);
			rowId = Long.parseLong(segment);
			String whereString;
			if (!TextUtils.isEmpty(where)) {
				whereString = " AND (" + where + ')';
			} else {
				whereString = "";
			}

			count = mDB
					.delete("tag", "_id=" + segment + whereString, whereArgs);
			// TODO remove unreferenced content
			break;

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}

		getContext().getContentResolver().notifyChange(url, null);
		return count;
	}

	@Override
	public int update(Uri url, ContentValues values, String where,
			String[] whereArgs) {
		// TODO which values can be updated.
		return 0;
	}

	@Override
	public String getType(Uri url) {
		switch (URL_MATCHER.match(url)) {
		case TAGS:
			return "vnd.openintents.cursor.dir/tag";

		case TAG_ID:
			return "vnd.openintents.cursor.item/tag";

		case CONTENTS:
			return "vnd.openintents.cursor.dir/content";

		case CONTENT_ID:
			return "vnd.openintents.cursor.item/content";

		default:
			throw new IllegalArgumentException("Unknown URL " + url);
		}
	}

	static {
		URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URL_MATCHER.addURI("org.openintents.tags", "tags", TAGS);
		URL_MATCHER.addURI("org.openintents.tags", "tags/#", TAG_ID);
		URL_MATCHER.addURI("org.openintents.tags", "contents", CONTENTS);
		URL_MATCHER.addURI("org.openintents.tags", "contents/#", CONTENT_ID);

		TAG_PROJECTION_MAP = new HashMap<String, String>();
		TAG_PROJECTION_MAP.put(Tags._ID, "tag._id");
		TAG_PROJECTION_MAP.put(Tags.TAG_ID, "tag.tag_id");
		TAG_PROJECTION_MAP.put(Tags.CONTENT_ID, "tag.content_id");
		TAG_PROJECTION_MAP.put(Tags.CREATED_DATE, "tag.created");
		TAG_PROJECTION_MAP.put(Tags.MODIFIED_DATE, "tag.modified");
		TAG_PROJECTION_MAP.put(Tags.ACCESS_DATE, "tag.accessed");
		TAG_PROJECTION_MAP.put(Tags.URI_1, "content1.uri as uri_1");
		TAG_PROJECTION_MAP.put(Tags.URI_2, "content2.uri as uri_2");

		CONTENT_PROJECTION_MAP = new HashMap<String, String>();
		CONTENT_PROJECTION_MAP.put(Contents._ID, "_id");
		CONTENT_PROJECTION_MAP.put(Contents.URI, "uri");
		CONTENT_PROJECTION_MAP.put(Contents.TYPE, "type");
	}
}
