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
import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.ContainsFull;
import org.openintents.provider.Shopping.Lists;

import android.content.Context;
import android.database.Cursor;

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

}
