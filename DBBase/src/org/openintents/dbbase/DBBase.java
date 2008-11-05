package org.openintents.dbbase;

import android.net.Uri;
import android.provider.BaseColumns;

public class DBBase {

	public static final String USER_DB = "user.db";

	public static class Tables implements BaseColumns {

		public static final String TABLE_NAME = "table_name";
		public static final Uri CONTENT_URI = Uri
				.parse("content://org.openintents.db/tables");
		public static final String QUERY_CREATE_TABLE = "create_table";

	}

	public static class Columns implements BaseColumns {

		public static final String TABLE_ID = "table_id";
		public static final String COL_NAME = "col_name";
		public static final String COL_TYPE = "col_type";
		public static final Uri CONTENT_URI = Uri
				.parse("content://org.openintents.db/columns");

	}
}
