package org.openintents.historify.data.providers;

import org.openintents.historify.data.loaders.SourceIconHelper.IconLoadingStrategy;
import org.openintents.historify.data.model.source.AbstractSource;
import org.openintents.historify.data.providers.Sources.SourcesTable;
import org.openintents.historify.data.providers.internal.FactoryTest;
import org.openintents.historify.data.providers.internal.Messaging;
import org.openintents.historify.data.providers.internal.QuickPosts;
import org.openintents.historify.data.providers.internal.Telephony;
import org.openintents.historify.uri.Actions;
import org.openintents.historify.utils.UriUtils;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class DefaultSources {

	public void insert(SQLiteDatabase db) {

		//telephony
		ContentValues cv = new ContentValues();
		cv.put(SourcesTable.NAME, Telephony.SOURCE_NAME);
		cv.put(SourcesTable.DESCRIPTION, Telephony.DESCRIPTION);
		cv.put(SourcesTable.AUTHORITY, Telephony.TELEPHONY_AUTHORITY);
		cv.put(SourcesTable.EVENT_INTENT, Actions.ACTION_VIEW_CALLOG_EVENT);
		cv.put(SourcesTable.IS_INTERNAL, 1);
		cv.put(SourcesTable.ICON_URI, UriUtils.drawableToUri("source_telephony").toString());
		db.insert(SourcesTable._TABLE, null, cv);

		//messaging
		cv = new ContentValues();
		cv.put(SourcesTable.NAME, Messaging.SOURCE_NAME);
		cv.put(SourcesTable.DESCRIPTION, Messaging.DESCRIPTION);
		cv.put(SourcesTable.AUTHORITY, Messaging.MESSAGING_AUTHORITY);
		cv.put(SourcesTable.EVENT_INTENT, Actions.ACTION_VIEW_MESSAGING_EVENT);
		cv.put(SourcesTable.IS_INTERNAL, 1);
		cv.put(SourcesTable.ICON_URI, UriUtils.drawableToUri("source_messaging").toString());
		db.insert(SourcesTable._TABLE, null, cv);

		//factory test
		cv = new ContentValues();
		cv.put(SourcesTable.NAME, FactoryTest.SOURCE_NAME);
		cv.put(SourcesTable.DESCRIPTION, FactoryTest.DESCRIPTION);
		cv.put(SourcesTable.AUTHORITY, FactoryTest.FACTORY_TEST_AUTHORITY);
		cv.put(SourcesTable.IS_INTERNAL, 1);
		cv.put(SourcesTable.ICON_URI, UriUtils.drawableToUri("source_factory_test").toString());
		cv.put(SourcesTable.CONFIG_INTENT, Actions.ACTION_CONFIG_FACTORYTEST);
		cv.put(SourcesTable.STATE, AbstractSource.SourceState.DISABLED.toString());
		db.insert(SourcesTable._TABLE, null, cv);
		
		//quickposts
		cv.put(SourcesTable.NAME, QuickPosts.SOURCE_NAME);
		cv.put(SourcesTable.DESCRIPTION, QuickPosts.DESCRIPTION);
		cv.put(SourcesTable.AUTHORITY, QuickPosts.QUICKPOSTS_AUTHORITY);
		cv.put(SourcesTable.EVENT_INTENT, Actions.ACTION_VIEW_QUICKPOST_EVENT);
		cv.put(SourcesTable.IS_INTERNAL, 0);
		cv.put(SourcesTable.ICON_URI, UriUtils.drawableToUri("source_quick_post").toString());
		cv.put(SourcesTable.CONFIG_INTENT, Actions.ACTION_CONFIG_QUICKPOSTS);
		cv.put(SourcesTable.ICON_LOADING_STRATEGY, IconLoadingStrategy.useEventIcon.toString());
		db.insert(SourcesTable._TABLE, null, cv);
		
	}
}
