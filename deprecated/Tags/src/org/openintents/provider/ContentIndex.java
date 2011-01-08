package org.openintents.provider;

import org.openintents.tags.content.ContentIndexProvider;
import org.openintents.tags.content.Directory;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

public class ContentIndex {
	public static final class Entry implements BaseColumns {
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri
				.parse("content://org.openintents.contentindices/entries");

		/**
		 * The default sort order for this table.
		 */
		public static final String DEFAULT_SORT_ORDER = "modified DESC";

		public static final String QUERY_CONTENT_ID = "contentId";
		public static final String QUERY_KEYWORD = "keyword";
		public static final String QUERY_DIRECTORY = "directory";

		public static final String ITEM_ID = "item_id";
		public static final String BODY = "body";

		public static final String BODY_SPLIT = "\t";

	}

	public static final class Dir implements BaseColumns {
		/**
		 * The content:// style URL for this table
		 */
		public static final Uri CONTENT_URI = Uri
				.parse("content://org.openintents.contentindices/directories");

		/**
		 * The default sort order for this table.
		 */
		public static final String DEFAULT_SORT_ORDER = "refreshed DESC";

		public static final String PACKAGE = "package";
		public static final String URI = "uri";
		public static final String NAME = "name";
		public static final String FLAGS = "flags";

		public static final String[] PROJECTION_PACKAGENAMES = new String[] {
				_ID, PACKAGE, URI, NAME, FLAGS };

	}

	public static final String QUERY_CONTENT_BODY_URI = "contentBodyUri";

	public static final int FLAG_NO_PICK = 1;

	public static final int FLAG_PICK_CHECKED = 2;

	public static final int FLAG_APP_MISSING = 128;

	public static final class ContentBody {

		public static final String[] COLUMNS = new String[] { "BODY1", "BODY2" };

	}

	private ContentResolver mResolver;

	public ContentIndex(ContentResolver contentResolver) {
		mResolver = contentResolver;
	}

	/**
	 * returns an ArrayListCursor containing the first string column of the
	 * requested content body uris.
	 * 
	 * @param uri
	 *            ContentURI with query parameters QUERY_CONTENT_BODY_URI.
	 * @return
	 */
	public Cursor getContentBody(Uri uri) {
		Uri entryUri = Entry.CONTENT_URI.buildUpon().appendQueryParameter(
				QUERY_CONTENT_BODY_URI, uri.toString()).build();
		return mResolver.query(entryUri, null, null, null, null);
	}

	public final int updateDirectory(Directory directory) {
		Uri uri = ContentUris.withAppendedId(Dir.CONTENT_URI, directory.id);
		return mResolver.update(uri, ContentIndexProvider
				.getContentValues(directory), null, null);
	}

	/**
	 * adds the directory. directory._id is ignored, no checks for duplicates.
	 * 
	 * @param directory
	 *            the directory to add
	 * @return the content URI of the inserted directory, null if insert failed.
	 */
	public final Uri addDirectory(Directory directory) {
		Uri uri = Dir.CONTENT_URI;
		return mResolver.insert(uri, ContentIndexProvider
				.getContentValues(directory));
	}

	/**
	 * deletes the package with the given uri
	 * 
	 * @param uriData
	 * @return
	 */
	public final int deleteContentType(String uriData) {
		Uri uri = Dir.CONTENT_URI;
		return mResolver.delete(uri, Dir.URI + " = ?",
				new String[] { uriData });
	}

	public final Cursor getPackageNames() {
		Uri uri = Dir.CONTENT_URI;
		return mResolver.query(uri, Dir.PROJECTION_PACKAGENAMES, Dir.FLAGS + " is null or "  + Dir.FLAGS + " < " + ContentIndex.FLAG_APP_MISSING, null,
				null);
	}

	public int deletePackage(String package_name) {
		Uri uri = Dir.CONTENT_URI;
		return mResolver.delete(uri, Dir.PACKAGE + " = ?",
				new String[] { package_name });
		
	}

}
