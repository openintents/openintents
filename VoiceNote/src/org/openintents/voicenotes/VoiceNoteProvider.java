package org.openintents.voicenotes;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

public class VoiceNoteProvider extends ContentProvider {

	private static final String DATABASE_NAME = "voicenotes.db";
	public static final int DATABASE_VERSION = 1;
	public static final String VOICE_NOTES_TABLE_NAME = "voicenotes";
	public static final String TAG = "VoiceNoteProvider";

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
	public int delete(Uri uri, String s, String[] as) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentvalues) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		long id = db.insert(VOICE_NOTES_TABLE_NAME, VoiceNote.DATA_URI, contentvalues);
		return Uri.withAppendedPath(uri, String.valueOf(id));
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = new DatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String orderBy) {
		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		return db.query(VOICE_NOTES_TABLE_NAME, projection, selection, selectionArgs, null, null, orderBy);
	}

	@Override
	public int update(Uri uri, ContentValues contentvalues, String s,
			String[] as) {
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		return db.update(VOICE_NOTES_TABLE_NAME, contentvalues, s, as);		
	}

}
