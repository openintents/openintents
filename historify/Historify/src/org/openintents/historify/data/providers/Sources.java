package org.openintents.historify.data.providers;

import org.openintents.historify.data.model.AbstractSource;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public final class Sources {

	static final String DB_NAME = "sources.db";
	static final int DB_VERSION = 2;
	
	public static final class SourcesTable {
		
		public static final String _TABLE = "sources";
		
		public static final String _ID = BaseColumns._ID;
		public static final String NAME = "name";
		public static final String STATE = "state";
		public static final String IS_INTERNAL = "is_internal";
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.historify.source";
		
	}
	
    static class OpenHelper extends SQLiteOpenHelper {

        OpenHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	
            db.execSQL("CREATE TABLE " + SourcesTable._TABLE + " ("
                    + SourcesTable._ID + " INTEGER PRIMARY KEY,"
                    + SourcesTable.NAME + " TEXT NOT NULL,"
                    + SourcesTable.STATE + " TEXT DEFAULT "+AbstractSource.SourceState.ENABLED+","
                    + SourcesTable.IS_INTERNAL + " INTEGER DEFAULT 0);");
            
            //adding test data
            ContentValues cv = new ContentValues();
            cv.put(SourcesTable.NAME, "internal");
            cv.put(SourcesTable.IS_INTERNAL, 1);
            db.insert(SourcesTable._TABLE, null, cv);
            
            cv = new ContentValues();
            cv.put(SourcesTable.NAME, "external1");
            db.insert(SourcesTable._TABLE, null, cv);
            
            cv = new ContentValues();
            cv.put(SourcesTable.NAME, "external2");
            db.insert(SourcesTable._TABLE, null, cv);
        }

        private void onErase(SQLiteDatabase db) {
        	db.execSQL("DROP TABLE IF EXISTS "+SourcesTable._TABLE);
        }
        
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
           
        	Log.w(SourcesProvider.NAME, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            
            onErase(db);
            onCreate(db);
        }

    }
}
