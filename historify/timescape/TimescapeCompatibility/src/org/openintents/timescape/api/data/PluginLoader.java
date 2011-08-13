package org.openintents.timescape.api.data;

import org.openintents.timescape.api.provider.EventStreamHelper;
import org.openintents.timescape.api.provider.EventStreamHelper.PluginsTable;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class PluginLoader {

	public static final String[] PROJECTION = new String[] {
		PluginsTable.NAME,
		PluginsTable.DESCRIPTION,
		PluginsTable.ICON_URI,
		PluginsTable.CONFIG_ACTIVITY,
		PluginsTable.PLUGIN_KEY
	};
	
	private final int COLUMN_NAME = 0;
	private final int COLUMN_DESCRIPTION = 1;
	private final int COLUMN_ICON_URI = 2;
	private final int COLUMN_CONFIG_ACTIVITY= 3;
	private final int COLUMN_PLUGIN_KEY= 3;
	
	public Cursor openCursor(Context context) {
		
		Uri uri = EventStreamHelper.getUri(EventStreamHelper.PLUGINS_PATH);
		return context.getContentResolver().query(uri, PROJECTION, null, null, PluginsTable.NAME);
	}
	
	public Plugin loadFromCursor(Cursor c, int pos) {
		
		c.moveToPosition(pos);
		
		return new Plugin(
				c.getString(COLUMN_NAME),
				Uri.parse(c.getString(COLUMN_ICON_URI)),
				c.getString(COLUMN_CONFIG_ACTIVITY),
				c.getString(COLUMN_DESCRIPTION));
	}
}
