

package org.openintents.timescape.api.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.openintents.timescape.api.provider.EventStreamHelper.EventsTable;
import org.openintents.timescape.api.provider.EventStreamHelper.FriendsTable;
import org.openintents.timescape.api.provider.EventStreamHelper.OpenHelper;
import org.openintents.timescape.api.provider.EventStreamHelper.PluginsTable;
import org.openintents.timescape.api.provider.EventStreamHelper.SourcesTable;

import com.sonyericsson.eventstream.EventStreamConstants.EventColumns;
import com.sonyericsson.eventstream.EventStreamConstants.FriendColumns;
import com.sonyericsson.eventstream.EventStreamConstants.PluginColumns;
import com.sonyericsson.eventstream.EventStreamConstants.SourceColumns;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;

public class EventStreamCompatibleProvider extends ContentProvider {

	public static final String N = "EventStreamCompatibleProvider";

	private static final UriMatcher sUriMatcher;
	private static final int PLUGINS = 1;
	private static final int SOURCES = 2;
	private static final int FRIENDS = 3;
	private static final int EVENTS = 4;

	private static final Map<String, String> sPluginsProjection, sSourcesProjection;
	
	static {
		sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		sUriMatcher.addURI(EventStreamHelper.AUTHORITY, EventStreamHelper.PLUGINS_PATH, PLUGINS);
		sUriMatcher.addURI(EventStreamHelper.AUTHORITY, EventStreamHelper.SOURCES_PATH, SOURCES);
		sUriMatcher.addURI(EventStreamHelper.AUTHORITY, EventStreamHelper.FRIENDS_PATH, FRIENDS);
		sUriMatcher.addURI(EventStreamHelper.AUTHORITY, EventStreamHelper.EVENTS_PATH, EVENTS);
		
		sPluginsProjection = new HashMap<String, String>();
		sPluginsProjection.put(PluginColumns.API_VERSION, PluginsTable.API_VERSION);
		sPluginsProjection.put(PluginColumns.CONFIGURATION_ACTIVITY, PluginsTable.CONFIG_ACTIVITY);
		sPluginsProjection.put(PluginColumns.CONFIGURATION_TEXT, PluginsTable.DESCRIPTION);
		sPluginsProjection.put(PluginColumns.ICON_URI, PluginsTable.ICON_URI);
		sPluginsProjection.put(PluginColumns.NAME, PluginsTable.NAME);
		sPluginsProjection.put(PluginColumns.PLUGIN_KEY, PluginsTable.PLUGIN_KEY);
		sPluginsProjection.put(PluginColumns.CONFIGURATION_STATE, PluginsTable.CONFIG_STATE);
		//unsupported fields
		sPluginsProjection.put(PluginColumns.STATUS_SUPPORT, "null");
		sPluginsProjection.put(PluginColumns.STATUS_TEXT_MAX_LENGTH, "null");
		
		sSourcesProjection = new HashMap<String, String>();
		sSourcesProjection.put(SourceColumns.ID, SourcesTable._ID);
		sSourcesProjection.put(SourceColumns.CURRENT_STATUS, "null");
		sSourcesProjection.put(SourceColumns.NAME, "null");
		sSourcesProjection.put(SourceColumns.ICON_URI, "null");
		sSourcesProjection.put(SourceColumns.STATUS_TIMESTAMP, "null");
		
	}

	
	private EventStreamHelper.OpenHelper mOpenHelper; 
	
	@Override
	public boolean onCreate() {
		mOpenHelper = new OpenHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch(sUriMatcher.match(uri)) {
		
		case PLUGINS:
			return PluginsTable.CONTENT_TYPE;
		
		case SOURCES:
			return SourcesTable.CONTENT_TYPE;
		
		case FRIENDS:
			return FriendsTable.CONTENT_TYPE;			
			
		case EVENTS:
			return EventsTable.CONTENT_TYPE;
			
		default:
			return null;
		}
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		
		int uid = Binder.getCallingUid();
		
		switch(sUriMatcher.match(uri)) {
		
		case PLUGINS:
			qb.setTables(PluginsTable._TABLE);
			if(uid!=getContext().getApplicationInfo().uid)
				qb.appendWhere(PluginsTable.UID + " = "+uid);
			qb.setProjectionMap(sPluginsProjection);
			break;
		case SOURCES:
			qb.setTables(SourcesTable._TABLE);
			if(uid!=getContext().getApplicationInfo().uid)
				qb.appendWhere(SourcesTable.UID + " = "+uid);
			qb.setProjectionMap(sSourcesProjection);
			break;
		case FRIENDS:
			qb.setTables(FriendsTable._TABLE);
			if(uid!=getContext().getApplicationInfo().uid)
				qb.appendWhere(FriendsTable.UID + " = "+uid);
			break;
		case EVENTS:
			qb.setTables(EventsTable._TABLE);
			if(uid!=getContext().getApplicationInfo().uid)
				qb.appendWhere(FriendsTable.UID + " = "+uid);
			break;
		default:
			throw new IllegalArgumentException("Unknown URI.");
		}
		
		return qb.query(mOpenHelper.getWritableDatabase(), projection, selection, selectionArgs, null, null, sortOrder);
		
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
						
		String table = null;
		Uri notificationUri = null;
		
		switch(sUriMatcher.match(uri)) {
		
		case PLUGINS:
			table = PluginsTable._TABLE;
			values = mapPluginColumns(values, Binder.getCallingUid());
			notificationUri = EventStreamHelper.getUri(EventStreamHelper.PLUGINS_PATH);
			break;
		case SOURCES:
			table = SourcesTable._TABLE;
			values = mapSourceColumns(values, Binder.getCallingUid());
			notificationUri = EventStreamHelper.getUri(EventStreamHelper.SOURCES_PATH);
			break;
		case FRIENDS:
			table = FriendsTable._TABLE;
			values = mapFriendColumns(values, Binder.getCallingUid());
			break;
		case EVENTS:
			table = EventsTable._TABLE;
			values = mapEventColumns(values, Binder.getCallingUid());
			break;
		default:
			throw new IllegalArgumentException("Unknown URI.");
		}
		
		
		long id = mOpenHelper.getWritableDatabase().insert(table, null, values);
		if(id!=-1) {
			
			if(notificationUri!=null)
				getContext().getContentResolver().notifyChange(notificationUri, null);
			
			return Uri.withAppendedPath(uri, String.valueOf(id));
		}
		
		return null;
	}


	private ContentValues mapPluginColumns(ContentValues values, int uid) {

		ContentValues retval = new ContentValues();
		
		retval.put(PluginsTable.UID, uid);
		
		if(values.containsKey(PluginsTable.API_VERSION))
			retval.put(PluginsTable.API_VERSION, values.getAsInteger(PluginColumns.API_VERSION));
		
		//map optional fields
		if(values.containsKey(PluginsTable.NAME))
			retval.put(PluginsTable.NAME, values.getAsString(PluginColumns.NAME));
		
		if(values.containsKey(PluginsTable.DESCRIPTION))
			retval.put(PluginsTable.DESCRIPTION, values.getAsString(PluginColumns.CONFIGURATION_TEXT));
		
		if(values.containsKey(PluginsTable.ICON_URI))
			retval.put(PluginsTable.ICON_URI, values.getAsString(PluginColumns.ICON_URI));
		
		if(values.containsKey(PluginsTable.CONFIG_ACTIVITY))
			retval.put(PluginsTable.CONFIG_ACTIVITY, values.getAsString(PluginColumns.CONFIGURATION_ACTIVITY));
		
		if(values.containsKey(PluginsTable.CONFIG_STATE))
			retval.put(PluginsTable.CONFIG_STATE, values.getAsInteger(PluginColumns.CONFIGURATION_STATE));
		
		if(values.containsKey(PluginsTable.PLUGIN_KEY))
			retval.put(PluginsTable.PLUGIN_KEY, values.getAsString(PluginColumns.PLUGIN_KEY));
		
		return retval;
	}

	private ContentValues mapFriendColumns(ContentValues values, int uid) {
		
		ContentValues retval = new ContentValues();
		
		retval.put(PluginsTable.UID, uid);
		
		if(values.containsKey(FriendColumns.CONTACTS_REFERENCE))
			retval.put(FriendsTable.CONTACTS_REFERENCE,values.getAsString(FriendColumns.CONTACTS_REFERENCE));
		
		if(values.containsKey(FriendColumns.FRIEND_KEY))
			retval.put(FriendsTable.FRIEND_KEY,values.getAsString(FriendColumns.FRIEND_KEY));

		if(values.containsKey(FriendColumns.PLUGIN_ID))
			retval.put(FriendsTable.PLUGIN_ID,values.getAsString(FriendColumns.PLUGIN_ID));
		
		if(values.containsKey(FriendColumns.SOURCE_ID))
			retval.put(FriendsTable.SOURCE_ID,values.getAsString(FriendColumns.SOURCE_ID));

		return retval;
		
	}

	private ContentValues mapEventColumns(ContentValues values, int uid) {

		ContentValues retval = new ContentValues();
		retval.put(SourcesTable.UID, uid);
		
		if(values.containsKey(EventColumns.EVENT_KEY))
			retval.put(EventsTable.EVENT_KEY, values.getAsString(EventColumns.EVENT_KEY));

		if(values.containsKey(EventColumns.FRIEND_KEY))
			retval.put(EventsTable.FRIEND_KEY, values.getAsString(EventColumns.FRIEND_KEY));

		if(values.containsKey(EventColumns.MESSAGE))
			retval.put(EventsTable.MESSAGE, values.getAsString(EventColumns.MESSAGE));

		if(values.containsKey(EventColumns.OUTGOING))
			retval.put(EventsTable.OUTGOING, values.getAsInteger(EventColumns.OUTGOING));

		if(values.containsKey(EventColumns.PERSONAL))
			retval.put(EventsTable.PERSONAL, values.getAsInteger(EventColumns.PERSONAL));
		
		if(values.containsKey(EventColumns.PUBLISHED_TIME))
			retval.put(EventsTable.PUBLISHED_TIME, values.getAsLong(EventColumns.PUBLISHED_TIME));
		
		if(values.containsKey(EventColumns.SOURCE_ID))
			retval.put(EventsTable.SOURCE_ID, values.getAsInteger(EventColumns.SOURCE_ID));
		
		return retval;
	}

	private ContentValues mapSourceColumns(ContentValues values, int uid) {

		ContentValues retval = new ContentValues();
		retval.put(SourcesTable.UID, uid);
		return retval;
	}

	
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		
		switch(sUriMatcher.match(uri)) {
		case PLUGINS:
			break;
		default:
			return 0;
		}
		
		int uid = Binder.getCallingUid();
		values = mapPluginColumns(values, uid);
		
		if(selection==null) selection="";
		else selection+=" AND ";
		selection+=PluginsTable.UID + " = "+uid;
		
		int retval =  mOpenHelper.getWritableDatabase().update(PluginsTable._TABLE, values, selection, selectionArgs);
		
		if(0<retval) {
			Uri notificationUri = EventStreamHelper.getUri(EventStreamHelper.PLUGINS_PATH);
			getContext().getContentResolver().notifyChange(notificationUri, null);
		
			
		}		
		return retval;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
