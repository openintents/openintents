package org.openintents.provider;

import android.net.ContentURI;
import android.provider.BaseColumns;

public abstract class Location {
	
	public static final class Locations implements BaseColumns {
		/**
         * The content:// style URL for this table
         */
        public static final ContentURI CONTENT_URI
                = ContentURI.create("content://org.openintents.locations/locations");

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
       
        /**
         * The latitude of the location
         * <P>Type: DOUBLE</P>
         */
        public static final String LATITUDE = "latitude";

        /**
         * The longitude of the location
         * <P>Type: DOUBLE</P>
         */
        public static final String LONGITUDE = "longitude";

        /**
         * The timestamp for when the note was created
         * <P>Type: INTEGER (long)</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the note was last modified
         * <P>Type: INTEGER (long)</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }
}
