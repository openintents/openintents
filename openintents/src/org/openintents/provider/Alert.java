package org.openintents.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.util.Log;
import android.provider.BaseColumns;
import java.util.*;


public class Alert{


	public static final String _TAG="org.openintents.provider.Alert";

	public static final String TYPE_LOCATION	="location";
	public static final String TYPE_SENSOR		="sensor";
	public static final String TYPE_GENERIC		="generic";
	public static final String TYPE_COMBINED	="combined";

	public static final String NATURE_USER	="user";
	public static final String NATURE_SYSTEM="system";

	public static ContentResolver mContentResolver;
	
		
	public static final class Generic implements BaseColumns{

		public static final Uri CONTENT_URI= Uri.parse
								("org.openintents.alert");


		public static final String CONDITION1="condition1";

		public static final String CONDITION2="condition2";

		public static final String TYPE="alert_type";

		public static final String RULE="rule";

		public static final String NATURE="nature";

		public static final String ACTIVE="active";

		public static final String ACTIVATE_ON_BOOT="activate_on_boot";

		public static final String INTENT="intent";

		public static final String[] PROJECTION={
			_ID,
			_COUNT,
			CONDITION1,
			CONDITION2,
			TYPE,
			RULE,
			NATURE,
			ACTIVE,
			ACTIVATE_ON_BOOT,
			INTENT
		};

	};


	public static final class Location implements BaseColumns	{
		
		public static final Uri CONTENT_URI= Uri.parse
								("org.openintents.alert/location");

		public static final String POSITION=Generic.CONDITION1;

		public static final String DISTANCE=Generic.CONDITION2;

		public static final String TYPE=Generic.TYPE;

		public static final String RULE=Generic.RULE;

		public static final String NATURE=Generic.NATURE;

		public static final String ACTIVE=Generic.ACTIVE;

		public static final String ACTIVATE_ON_BOOT=Generic.ACTIVATE_ON_BOOT;

		public static final String INTENT=Generic.INTENT;
		
		public static final String[] PROJECTION={
			_ID,
			_COUNT,
			POSITION,
			DISTANCE,
			TYPE,
			RULE,
			NATURE,
			ACTIVE,
			ACTIVATE_ON_BOOT,
			INTENT
		};
			

	};


	public static class ACTION{
		
		public static final String POSITION_REACHED="POSITION_REACHED";

		public static final String ADD_LOCATION_ALERT="ADD_LOCATION_ALERT";

		
	};


	/**
	 *@param uri the content uri to insert to
	 *@param cv the ContentValues that will be inserted to
	*/
	public static Uri insert(Uri uri, ContentValues cv){

		return mContentResolver.insert(uri,cv);
		
	}


	/**
	 *@param uri the content uri to delete
	 *@param selection the selection to check against
	 *@param selectionArgs the arguments applied to selection string (optional)	 
	 *@return number of deleted rows
	 */
	public static int delete(Uri uri,String selection,String[] selectionArgs){

		return mContentResolver.delete(uri,selection,selectionArgs);
	}

	/**
	 *@param uri the content uri to update
	 *@param cv the ContentValues that will be update in selected rows.
	 *@param selection the selection to check against
	 *@param selectionArgs the arguments applied to selection string (optional)	 
	 *@return number of updated rows
	 */
	public static int update(Uri uri,ContentValues values, String selection, String[] selectionArgs){
		return mContentResolver.update(uri,values,selection,selectionArgs);
	}

}/*eoc*/