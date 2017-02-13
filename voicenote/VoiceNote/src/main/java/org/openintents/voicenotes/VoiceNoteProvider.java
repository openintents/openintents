package org.openintents.voicenotes;

import java.util.HashMap;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class VoiceNoteProvider extends ContentProvider {

	private static final String DATABASE_NAME = "voicenotes.db";
	public static final int DATABASE_VERSION = 1;
	public static final String VOICE_NOTES_TABLE_NAME = "voicenotes";
	public static final String TAG = "VoiceNoteProvider";

    private static HashMap<String, String> sVoiceNotesProjectionMap;
    
    private static final int VOICENOTES = 1;
    private static final int VOICENOTE_ID = 2;

    private static final UriMatcher sUriMatcher;
    
	/**
	 * This class helps open, create, and upgrade the database file.
	 */
	private static class DatabaseHelper extends SQLiteOpenHelper {

		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + VOICE_NOTES_TABLE_NAME + " (" + VoiceNote._ID
					+ " INTEGER PRIMARY KEY," + VoiceNote.DATA_URI + " TEXT,"
					+ VoiceNote.VOICE_URI + " TEXT" + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
					+ newVersion + ", which will destroy all old data");
			db.execSQL("DROP TABLE IF EXISTS " + VOICE_NOTES_TABLE_NAME);
			onCreate(db);
		}
	}

	private DatabaseHelper mOpenHelper;

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        switch (sUriMatcher.match(uri)) {
        case VOICENOTES:
            qb.setTables(VOICE_NOTES_TABLE_NAME);
            qb.setProjectionMap(sVoiceNotesProjectionMap);
            break;

        case VOICENOTE_ID:
            qb.setTables(VOICE_NOTES_TABLE_NAME);
            qb.setProjectionMap(sVoiceNotesProjectionMap);
            qb.appendWhere(VoiceNote._ID + "=" + uri.getPathSegments().get(1));
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        // If no sort order is specified use the default
        String orderBy;
        if (TextUtils.isEmpty(sortOrder)) {
            orderBy = VoiceNote.DEFAULT_SORT_ORDER;
        } else {
            orderBy = sortOrder;
        }
        
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, orderBy);

		// Tell the cursor what uri to watch, so it knows when its source data changes
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;
    }
	
	@Override
	public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
        case VOICENOTES:
            return VoiceNote.CONTENT_TYPE;

        case VOICENOTE_ID:
            return VoiceNote.CONTENT_ITEM_TYPE;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }
	}
	

	@Override
	public Uri insert(Uri uri, ContentValues contentValues) {
		// Validate the requested uri
        if (sUriMatcher.match(uri) != VOICENOTES) {
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(VOICE_NOTES_TABLE_NAME, VoiceNote.DATA_URI, contentValues);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(VoiceNote.CONTENT_URI, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        throw new SQLException("Failed to insert row into " + uri);
	}

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case VOICENOTES:
            count = db.delete(VOICE_NOTES_TABLE_NAME, where, whereArgs);
            break;

        case VOICENOTE_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.delete(VOICE_NOTES_TABLE_NAME, VoiceNote._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        
        return count;
    }
    
    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)) {
        case VOICENOTES:
            count = db.update(VOICE_NOTES_TABLE_NAME, values, where, whereArgs);
            break;

        case VOICENOTE_ID:
            String noteId = uri.getPathSegments().get(1);
            count = db.update(VOICE_NOTES_TABLE_NAME, values, VoiceNote._ID + "=" + noteId
                    + (!TextUtils.isEmpty(where) ? " AND (" + where + ')' : ""), whereArgs);
            break;

        default:
            throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
	
    static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(VoiceNote.AUTHORITY, "voicenotes", VOICENOTES);
        sUriMatcher.addURI(VoiceNote.AUTHORITY, "voicenotes/#", VOICENOTE_ID);

        sVoiceNotesProjectionMap = new HashMap<String, String>();
        sVoiceNotesProjectionMap.put(VoiceNote._ID, VoiceNote._ID);
        sVoiceNotesProjectionMap.put(VoiceNote.DATA_URI, VoiceNote.DATA_URI);
        sVoiceNotesProjectionMap.put(VoiceNote.VOICE_URI, VoiceNote.VOICE_URI);
    }
}
