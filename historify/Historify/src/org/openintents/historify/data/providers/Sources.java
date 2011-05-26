package org.openintents.historify.data.providers;

import org.openintents.historify.data.model.source.AbstractSource;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

public final class Sources {

	static final String DB_NAME = "sources.db";
	static final int DB_VERSION = 8;
	
	public static final class SourcesTable {
		
		public static final String _TABLE = "sources";
		
		public static final String _ID = BaseColumns._ID;
		public static final String NAME = "name";
		public static final String IS_INTERNAL = "is_internal";
		public static final String STATE = "state";
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.historify.source";
		public static final String ITEM_CONTENT_TYPE ="vnd.android.cursor.item/vnd.historify.source";
		
	}
	
	public static final class FiltersTable {
		
		public static final String _TABLE = "filters";
		
		public static final String _ID = "filter_id";
		public static final String CONTACT_LOOKUP_KEY = "contact_lookup_key";
		public static final String SOURCE_ID = "source_id";
		public static final String FILTERED_STATE = "filtered_state";
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.historify.source_filter";
		public static final String ITEM_CONTENT_TYPE ="vnd.android.cursor.item/vnd.historify.source_filter";
	}
	
	public static final class FilteredSourcesView {
		
		public static final String _VIEW = "filtered_sources";
		
		public static final String JOIN_CLAUSE =
			SourcesTable._TABLE+" LEFT OUTER JOIN "+FiltersTable._TABLE+" ON "+
			SourcesTable._TABLE+"."+SourcesTable._ID+" = "+
			FiltersTable._TABLE+"."+FiltersTable.SOURCE_ID;
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.historify.filtered_source";
		
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
            
            db.execSQL("CREATE TABLE " + FiltersTable._TABLE + " ("
            		+ FiltersTable._ID + " INTEGER PRIMARY KEY,"
                    + FiltersTable.CONTACT_LOOKUP_KEY + " TEXT NOT NULL,"
                    + FiltersTable.SOURCE_ID + " INTEGER UNIQUE NOT NULL,"
                    + FiltersTable.FILTERED_STATE + " TEXT NOT NULL,"
                    + " FOREIGN KEY ("+FiltersTable.SOURCE_ID + ") REFERENCES "+SourcesTable._TABLE+" ("+SourcesTable._ID+"));");
            
            //adding test data
            ContentValues cv = new ContentValues();
            cv.put(SourcesTable.NAME, "Dummy Internal Provider");
            cv.put(SourcesTable.IS_INTERNAL, 1);
            db.insert(SourcesTable._TABLE, null, cv);
            
            cv = new ContentValues();
            cv.put(SourcesTable.NAME, "Dummy External Provider 1");
            db.insert(SourcesTable._TABLE, null, cv);
            
            cv = new ContentValues();
            cv.put(SourcesTable.NAME, "Dummy External Provider 2");
            db.insert(SourcesTable._TABLE, null, cv);
        }

        private void onErase(SQLiteDatabase db) {
        	db.execSQL("DROP TABLE IF EXISTS "+SourcesTable._TABLE);
        	db.execSQL("DROP TABLE IF EXISTS "+FiltersTable._TABLE);
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
