package org.openintents.safe;

import java.io.File;
import java.io.FileNotFoundException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class CryptoContentProvider extends ContentProvider {

	private static final boolean debug = true;
	private static final String TAG = "CryptoContentProvider";

	public static final String AUTHORITY = "org.openintents.safe";
	public static final Uri CONTENT_URI
		=Uri.parse("content://"+AUTHORITY);

	public static final String SESSION_FILE = "session";
	
	private static final int ENCRYPT_ID = 2;
	private static final int DECRYPT_ID = 3;

	private static final UriMatcher sUriMatcher;

	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(AUTHORITY, "encrypt/*", ENCRYPT_ID);
		sUriMatcher.addURI(AUTHORITY, "decrypt/*", DECRYPT_ID);
	}
	
	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public int delete(Uri uri, String s, String[] as) {
		// not supported
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// return file extension (uri.lastIndexOf("."))
		return null; //mMimeTypes.getMimeType(uri.toString());
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentvalues) {
		// not supported
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] as, String s, String[] as1, String s1) {
		/*
		if (uri.toString().startsWith(
				MIME_TYPE_PREFIX)) {
			MatrixCursor c = new MatrixCursor(new String[] { Images.Media.DATA,
					Images.Media.MIME_TYPE });
			// data = absolute path = uri - content://authority/mimetype
			String data = uri.toString().substring(20 + AUTHORITY.length());
			String mimeType = mMimeTypes.getMimeType(data);
			c.addRow(new String[] { data, mimeType });
			return c;
		} else {
			throw new RuntimeException("Unsupported uri");
		}
		*/
		return null;
	}
	
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {
		if (debug) Log.d(TAG,"openFile("+uri.toString()+","+mode+")");

		ParcelFileDescriptor pfd = null;
		try {
			String filesDir=getContext().getFilesDir().toString();
			if (debug) Log.d(TAG,"openFile: filesDir="+filesDir);

			String path=filesDir;
			String cryptSession;
			String sessionFile;
	        int modeBits = 0;
			switch (sUriMatcher.match(uri)) {
				case ENCRYPT_ID:
					if (debug) Log.d(TAG,"openFile: ENCRYPT");
			        modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY |
			        	ParcelFileDescriptor.MODE_CREATE;
			        cryptSession = uri.getPathSegments().get(1);
			        sessionFile=SESSION_FILE+"."+cryptSession;
			        path += "/"+sessionFile;
					break;
				case DECRYPT_ID:
					if (debug) Log.d(TAG,"openFile: DECRYPT");
			        modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
			        cryptSession = uri.getPathSegments().get(1);
			        sessionFile=SESSION_FILE+"."+cryptSession;
			        path += "/"+sessionFile;
					break;
				default:
					throw new IllegalArgumentException("Unknown URI " + uri);
			}

			if (debug) Log.d(TAG,"openFile: path="+path);
	        pfd=ParcelFileDescriptor.open(new File(path), modeBits);
	        if (!getContext().deleteFile(sessionFile)) {
	        	Log.e(TAG,"openFile: unable to delete: "+sessionFile);
	        }
		} catch (FileNotFoundException e) {
			if (debug) Log.d(TAG,"openFile: FileNotFound");
			throw e;
		} catch (IllegalArgumentException e) {
			throw e;
		}

		return pfd;
	}

	@Override
	public int update(Uri uri, ContentValues contentvalues, String s,
			String[] as) {
		// not supported
		return 0;
	}

}
