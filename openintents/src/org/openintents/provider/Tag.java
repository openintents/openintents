package org.openintents.provider;

import android.net.ContentURI;
import android.provider.BaseColumns;

public abstract class Tag {
	
	public static final class Tags implements BaseColumns {
		/**
         * The content:// style URL for this table
         */
        public static final ContentURI CONTENT_URI
                = ContentURI.create("content://org.openintents.tags/tags");

        /**
         * The default sort order for this table.
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
       
        /**
         * The name of the tag.
         * <P>Type: TEXT</P>
         */
        public static final String NAME = "name";

        /**
         * The label of the tag.
         * <P>Type: TEXT</P>
         */
        public static final String LABEL = "label";

        /**
         * The timestamp for when the note was created.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String CREATED_DATE = "created";

        /**
         * The timestamp for when the note was last modified.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String MODIFIED_DATE = "modified";
    }
	
	public static final class Contents implements BaseColumns {
		/**
         * The content:// style URL for this table
         */
        public static final ContentURI CONTENT_URI
                = ContentURI.create("content://org.openintents.tags/contents");

        /**
         * The default sort order for this table.
         */
        public static final String DEFAULT_SORT_ORDER = "modified DESC";
        
       
        /**
         * The uri of the content.
         * <P>Type: TEXT</P>
         */
        public static final String URI = "uri";
 

        /**
         * The timestamp for when the note was created.
         * <P>Type: INTEGER (long)</P>
         */
        public static final String CREATED_DATE = "created";
        
    }
}
