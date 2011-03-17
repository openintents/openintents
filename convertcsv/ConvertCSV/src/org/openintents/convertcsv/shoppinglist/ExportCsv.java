/* 
 * Copyright (C) 2008 OpenIntents.org
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

package org.openintents.convertcsv.shoppinglist;

import java.io.IOException;
import java.io.Writer;

import org.openintents.convertcsv.R;
import org.openintents.convertcsv.common.ConvertCsvBaseActivity;
import org.openintents.convertcsv.opencsv.CSVWriter;
import org.openintents.shopping.library.provider.Shopping;
import org.openintents.shopping.library.provider.Shopping.ContainsFull;
import org.openintents.shopping.library.provider.Shopping.Lists;
import org.openintents.shopping.library.provider.Shopping.Status;
import org.openintents.shopping.library.util.ShoppingUtils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

public class ExportCsv {

	public static final String[] PROJECTION_LISTS = new String[] { Lists._ID,
			Lists.NAME, Lists.IMAGE, Lists.SHARE_NAME, Lists.SHARE_CONTACTS,
			Lists.SKIN_BACKGROUND };

	public static final String[] PROJECTION_CONTAINS_FULL = new String[] {
		ContainsFull._ID, ContainsFull.ITEM_NAME, ContainsFull.ITEM_IMAGE,
		ContainsFull.STATUS, ContainsFull.ITEM_ID, ContainsFull.LIST_ID,
		ContainsFull.ITEM_TAGS,
		ContainsFull.SHARE_CREATED_BY, ContainsFull.SHARE_MODIFIED_BY };

	Context mContext;

	public ExportCsv(Context context) {
		mContext = context;
	}

	/**
	 * @param dos
	 * @throws IOException
	 */
	public void exportCsv(Writer writer) throws IOException {

		CSVWriter csvwriter = new CSVWriter(writer);

		csvwriter.write(mContext.getString(R.string.header_subject));
		csvwriter.write(mContext.getString(R.string.header_percent_complete));
		csvwriter.write(mContext.getString(R.string.header_categories));
		csvwriter.write(mContext.getString(R.string.header_tags));
		csvwriter.writeNewline();

		Cursor c = mContext.getContentResolver().query(
				Shopping.Lists.CONTENT_URI, PROJECTION_LISTS, null,
				null, Shopping.Lists.DEFAULT_SORT_ORDER);

		if (c != null) {

			while (c.moveToNext()) {

				String listname = c.getString(c
						.getColumnIndexOrThrow(Shopping.Lists.NAME));
				long id = c
						.getLong(c.getColumnIndexOrThrow(Shopping.Lists._ID));

				// Log.i(ConvertCsvActivity.TAG, "List: " + listname);

				Cursor ci = mContext.getContentResolver().query(
						Shopping.ContainsFull.CONTENT_URI,
						PROJECTION_CONTAINS_FULL,
						Shopping.ContainsFull.LIST_ID + " = ?",
						new String[] { "" + id },
						Shopping.ContainsFull.DEFAULT_SORT_ORDER);

				if (ci != null) {
					int itemcount = ci.getCount();
					ConvertCsvBaseActivity.dispatchSetMaxProgress(itemcount);
					int progress = 0;

					while (ci.moveToNext()) {
						ConvertCsvBaseActivity
								.dispatchConversionProgress(progress++);
						String itemname = ci
								.getString(ci
										.getColumnIndexOrThrow(Shopping.ContainsFull.ITEM_NAME));
						int status = ci
								.getInt(ci
										.getColumnIndexOrThrow(Shopping.ContainsFull.STATUS));
						int percentage = (status == Shopping.Status.BOUGHT) ? 1
								: 0;
						String tags = ci
								.getString(ci
										.getColumnIndexOrThrow(Shopping.ContainsFull.ITEM_TAGS));
						csvwriter.write(itemname);
						csvwriter.write(percentage);
						csvwriter.write(listname);
						csvwriter.write(tags);
						csvwriter.writeNewline();
					}
				}
			}
		}

		csvwriter.close();
	}

	String handyShopperColumns = "Need,Priority,Description,CustomText,Quantity,Units,Price,Aisle,Date,Category,Stores,PerStoreInfo,EntryOrder,Coupon,Tax,Tax2,AutoDelete,Private,Note,Alarm,AlarmMidi,Icon,AutoOrder";

	public static final String[] PROJECTION_CONTAINS_FULL_HANDY_SHOPPER = new String[] {
		ContainsFull._ID, ContainsFull.ITEM_NAME, ContainsFull.ITEM_IMAGE,
		ContainsFull.QUANTITY, ContainsFull.PRIORITY,
		ContainsFull.STATUS, ContainsFull.ITEM_ID, ContainsFull.LIST_ID,
		ContainsFull.ITEM_TAGS,
		ContainsFull.ITEM_PRICE, ContainsFull.ITEM_UNITS,
		ContainsFull.SHARE_CREATED_BY, ContainsFull.SHARE_MODIFIED_BY };

	/**
	 * @param dos
	 * @throws IOException
	 */
	public void exportHandyShopperCsv(Writer writer) throws IOException {

		CSVWriter csvwriter = new CSVWriter(writer);
		csvwriter.setLineEnd("\r\n");
		csvwriter.setQuoteCharacter(CSVWriter.NO_QUOTE_CHARACTER);
		
		csvwriter.write(handyShopperColumns);
		csvwriter.writeNewline();

		Cursor c = mContext.getContentResolver().query(
				Shopping.Lists.CONTENT_URI, PROJECTION_LISTS, null,
				null, Shopping.Lists.DEFAULT_SORT_ORDER);

		if (c != null) {

			while (c.moveToNext()) {

				String listname = c.getString(c
						.getColumnIndexOrThrow(Shopping.Lists.NAME));
				long id = c
						.getLong(c.getColumnIndexOrThrow(Shopping.Lists._ID));
				
				long listId = ShoppingUtils.getDefaultList(mContext);
				if (id != listId) {
					// TODO: Currently only default list supported.
					break;
				}
				
				// Log.i(ConvertCsvActivity.TAG, "List: " + listname);

				Cursor ci = mContext.getContentResolver().query(
						Shopping.ContainsFull.CONTENT_URI,
						PROJECTION_CONTAINS_FULL_HANDY_SHOPPER,
						Shopping.ContainsFull.LIST_ID + " = ?",
						new String[] { "" + id },
						Shopping.ContainsFull.DEFAULT_SORT_ORDER);

				if (ci != null) {
					int itemcount = ci.getCount();
					ConvertCsvBaseActivity.dispatchSetMaxProgress(itemcount);
					int progress = 0;

					while (ci.moveToNext()) {
						ConvertCsvBaseActivity
								.dispatchConversionProgress(progress++);
						String itemname = ci.getString(ci.getColumnIndexOrThrow(Shopping.ContainsFull.ITEM_NAME));
						int status = ci.getInt(ci.getColumnIndexOrThrow(Shopping.ContainsFull.STATUS));
						String tags = ci.getString(ci.getColumnIndexOrThrow(Shopping.ContainsFull.ITEM_TAGS));
						String priority = ci.getString(ci.getColumnIndex(Shopping.ContainsFull.PRIORITY));
						String quantity = ci.getString(ci.getColumnIndex(Shopping.ContainsFull.QUANTITY));
						String price = ci.getString(ci.getColumnIndex(Shopping.ContainsFull.ITEM_PRICE));
						String unit = ci.getString(ci.getColumnIndex(Shopping.ContainsFull.ITEM_UNITS));
						long itemId =  ci.getInt(ci.getColumnIndex(Shopping.ContainsFull.ITEM_ID));
						
						String statusText = getHandyShopperStatusText(status);
						
						// Split off first tag.
						int t = tags.indexOf(",");
						String firstTag = "";
						String otherTags = "";
						if (t >= 0) {
							firstTag = tags.substring(0,t); // -> Category
							otherTags = tags.substring(t+1); // -> CustomText
						} else {
							firstTag = tags; // -> Category
							otherTags = ""; // -> CustomText
						}
						
						// Retrieve note:
						String note = getHandyShopperNote(c, itemId);
						
						String stores = getHandyShopperStores(c, itemId);
						String perStoreInfo = getHandyShopperPerStoreInfo(c, itemId);
						
						
						csvwriter.write(statusText); // 0 Need
						csvwriter.write(priority); // 1 Priority
						csvwriter.write(itemname); // 2 Description
						csvwriter.write(otherTags); // 3 CustomText
						csvwriter.write(quantity); // 4 Quantity
						csvwriter.write(unit); // 5 Units
						csvwriter.write(price); // 6 Price
						csvwriter.write(""); // 7 Aisle
						csvwriter.write(""); // 8 Date
						csvwriter.write(firstTag); // 9 Category
						csvwriter.write(stores); // 10 Stores
						csvwriter.write(perStoreInfo); // 11 PerStoreInfo
						csvwriter.write(""); // 12 EntryOrder
						csvwriter.write(""); // 13 Coupon
						csvwriter.write(""); // 14 Tax
						csvwriter.write(""); // 15 Tax2
						csvwriter.write(""); // 16 AutoDelete
						csvwriter.write(""); // 17 Private
						csvwriter.write(note); // 18 Note
						csvwriter.write(""); // 19 Alarm
						csvwriter.write(""); // 20 AlarmMidi
						csvwriter.write(""); // 21 Icon
						csvwriter.write(""); // 22 AutoOrder
						
						csvwriter.writeNewline();
					}
					ci.close();
				}
			}
		}

		csvwriter.close();
	}

	String getHandyShopperStatusText(int status) {
		String statusText = "";
		if (status == Status.WANT_TO_BUY) {
			statusText = "x";
		} else if (status == Status.REMOVED_FROM_LIST) {
			statusText = "have";
		} else if (status == Status.BOUGHT){
			statusText = "";
		}
		return statusText;
	}

	private String getHandyShopperNote(Cursor c, long itemId) {
		Uri uri = ContentUris.withAppendedId(Shopping.Items.CONTENT_URI, itemId);
		
		String note = "";
		Cursor c1 = mContext.getContentResolver().query(uri, 
				new String[] {Shopping.Items.NOTE} , null, null, null);
		if (c != null) {
			if (c1.moveToFirst()) {
				note = c1.getString(0);
			}
			c1.close();
		}
		return note;
	}

	private String getHandyShopperStores(Cursor c, long itemId) {
		String stores = "";
		
		Cursor c1 = mContext.getContentResolver().query(
				Shopping.ItemStores.CONTENT_URI, 
				new String[] {Shopping.ItemStores.ITEM_ID,
				Shopping.ItemStores.STORE_ID} , 
				Shopping.ItemStores.ITEM_ID + " = ?",
				new String[] {"" + itemId}, null);
		if (c != null) {
			while (c1.moveToNext()) {
				long storeId = c1.getLong(c1.getColumnIndexOrThrow(Shopping.ItemStores.STORE_ID));
				Uri uri2 = ContentUris.withAppendedId(Shopping.Stores.CONTENT_URI, storeId);
				Cursor c2 = mContext.getContentResolver().query(uri2, 
						new String[] {Shopping.Stores.NAME}, null, null, null);
				if (c2 != null) {
					if (c2.moveToFirst()) {
						String storeName = c2.getString(c2.getColumnIndexOrThrow(Shopping.Stores.NAME));
						if (stores.equals("")) {
							stores = storeName;
						} else {
							stores += ";" + storeName;
						}
					}
					c2.close();
				}
			}
			c1.close();
		}
		return stores;
	}
	
	// Deal with per-store aisles and prices from column 11.
	// example value for column 11:    Big Y=/0.50;BJ's=11/0.42
	private String getHandyShopperPerStoreInfo(Cursor c, long itemId) {
		String perStoreInfo = "";

		Cursor c1 = mContext.getContentResolver().query(
				Shopping.ItemStores.CONTENT_URI, 
				new String[] {Shopping.ItemStores.ITEM_ID,
				Shopping.ItemStores.STORE_ID,
				Shopping.ItemStores.AISLE,
				Shopping.ItemStores.PRICE} , 
				Shopping.ItemStores.ITEM_ID + " = ?",
				new String[] {"" + itemId}, null);
		if (c != null) {
			while (c1.moveToNext()) {
				long storeId = c1.getLong(c1.getColumnIndexOrThrow(Shopping.ItemStores.STORE_ID));
				String aisle = c1.getString(c1.getColumnIndexOrThrow(Shopping.ItemStores.AISLE));
				String price = c1.getString(c1.getColumnIndexOrThrow(Shopping.ItemStores.PRICE));
				
				Uri uri2 = ContentUris.withAppendedId(Shopping.Stores.CONTENT_URI, storeId);
				Cursor c2 = mContext.getContentResolver().query(uri2, 
						new String[] {Shopping.Stores.NAME}, null, null, null);

				if (c2 != null) {
					if (c2.moveToFirst()) {
						String storeName = c2.getString(c2.getColumnIndexOrThrow(Shopping.Stores.NAME));
						
						String info = storeName + "=" + aisle + "/" + price;
						
						if (perStoreInfo.equals("")) {
							perStoreInfo = info;
						} else {
							perStoreInfo += ";" + info;
						}
					}
					c2.close();
				}
			}
			c1.close();
		}
		return perStoreInfo;
	}
	
}
