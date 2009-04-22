/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.home.widgets.shopping;


import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.IBinder;
import android.text.format.Time;
import android.util.Log;
import android.widget.RemoteViews;
import android.database.Cursor;
import android.content.ContentValues;

import org.openintents.provider.Shopping;

/**
 * Define a simple widget that shows the Wiktionary "Word of the day." To build
 * an update we spawn a background {@link Service} to perform the API queries.
 */
public class SingleRowShoppingWidget extends AppWidgetProvider {

	private static final String TAG="SingleRowShoppingWidget";


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
            int[] appWidgetIds) {
        // To prevent any ANR timeouts, we perform the update in a service
        context.startService(new Intent(context, UpdateService.class));
    }
    
    public static class UpdateService extends Service {

		public static final String LIST_ID="LISTID";
		public static final String ITEM_ID="ITEMID";
		public static final String ITEM_NAME="ITEMNAME";

		static final String[] mStringItems = new String[] { Shopping.ContainsFull._ID,
				Shopping.ContainsFull.ITEM_NAME, Shopping.ContainsFull.ITEM_IMAGE,
				Shopping.ContainsFull.ITEM_TAGS, Shopping.ContainsFull.ITEM_PRICE,
				Shopping.ContainsFull.QUANTITY, Shopping.ContainsFull.STATUS, Shopping.ContainsFull.ITEM_ID,
				Shopping.ContainsFull.SHARE_CREATED_BY, Shopping.ContainsFull.SHARE_MODIFIED_BY };
		private static final int mStringItemsCONTAINSID = 0;
		private static final int mStringItemsITEMNAME = 1;
		private static final int mStringItemsITEMIMAGE = 2;
		static final int mStringItemsITEMTAGS = 3;
		static final int mStringItemsITEMPRICE = 4;
		private static final int mStringItemsQUANTITY = 5;
		static final int mStringItemsSTATUS = 6;
		static final int mStringItemsITEMID = 7;
		private static final int mStringItemsSHARECREATEDBY = 8;
		private static final int mStringItemsSHAREMODIFIEDBY = 9;



		private static final String TAG="SingleRowShoppingWidget$UpdateService";
        @Override
        public void onStart(Intent intent, int startId) {
            // Build the widget update for today

			if (intent.hasExtra(ITEM_NAME) && intent.hasExtra(ITEM_ID)&& intent.hasExtra(LIST_ID))
			{
				checkItem(getApplicationContext(),
					intent.getLongExtra(ITEM_ID,-1),
					intent.getStringExtra(ITEM_NAME),
					intent.getLongExtra(LIST_ID,-1));
			}
            RemoteViews updateViews = buildUpdate(getApplicationContext());
            publishUpdate(updateViews);
        }

        /**
         * Build a widget update to show the current Wiktionary
         * "Word of the day." Will block until the online API returns.
         */
        public RemoteViews buildUpdate(Context context) {
			
			Log.d(TAG,"buildUpdate:entering");
			String itemName="";
			long itemId=-1;
            Resources res = context.getResources();
            RemoteViews updateViews = null;

			long listId=Shopping.getDefaultList();
			//TODO: read sortOrder from Prefs
			String sortOrder="";

			Cursor mCursorItems = context.getContentResolver().query(
				Shopping.ContainsFull.CONTENT_URI, mStringItems,
				Shopping.Contains.STATUS+"="+Shopping.Status.WANT_TO_BUY+ " AND list_id = ? ", 
				new String[] { String.valueOf(listId) }, sortOrder);

			if (mCursorItems.getCount()>0)
			{
			
				mCursorItems.moveToFirst();
				itemName=mCursorItems.getString(mCursorItems.getColumnIndex(Shopping.ContainsFull.ITEM_NAME));
				itemId=mCursorItems.getLong(mCursorItems.getColumnIndex(Shopping.ContainsFull._ID));
				mCursorItems.close();
				
				updateViews = new RemoteViews(context.getPackageName(), R.layout.shopping_item_row_small);

				updateViews.setTextViewText(R.id.name, itemName);
				Intent i=new Intent(context, UpdateService.class);
				i.putExtra(LIST_ID,listId);
				i.putExtra(ITEM_ID,itemId);
				i.putExtra(ITEM_NAME,itemName);
				PendingIntent pi=PendingIntent.getService(
					context,
					0,
					i,
					PendingIntent.FLAG_ONE_SHOT
					);

				updateViews.setOnClickPendingIntent(R.id.check, pi);

			}else{
				//TODO: display some nice "Youre done with list" message
				updateViews = new RemoteViews(context.getPackageName(), R.layout.no_items);

			}

			Log.d(TAG,"buildUpdate:leaving");
            return updateViews;
        }
        

		private void checkItem(Context context,long itemId,String itemName,long listId){
			if (itemId==-1 || listId==-1)
			{
				return;
			}
			ContentValues values = new ContentValues();
			values.put(Shopping.Contains.STATUS, Shopping.Status.BOUGHT);
			
			context.getContentResolver().update(
					Uri.withAppendedPath(Shopping.Contains.CONTENT_URI,
							String.valueOf(itemId)), values, null, null);

		}


		private void publishUpdate(RemoteViews updateViews){
            // Push update for this widget to the home screen
            ComponentName thisWidget = new ComponentName(getApplicationContext(), SingleRowShoppingWidget.class);
            AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
            manager.updateAppWidget(thisWidget, updateViews);
		}

        @Override
        public IBinder onBind(Intent intent) {
            // We don't need to bind to this service
            return null;
        }
    }
}

