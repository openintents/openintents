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

import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;

import org.openintents.convertcsv.PreferenceActivity;
import org.openintents.convertcsv.R;
import org.openintents.convertcsv.common.ConvertCsvBaseActivity;
import org.openintents.convertcsv.common.WrongFormatException;

import android.os.Bundle;
import android.widget.Toast;

public class ConvertCsvActivity extends ConvertCsvBaseActivity {

	public static final String TAG = "ConvertCsvActivity";

	final String HANDYSHOPPER_FORMAT = "handyshopper";

	public void setPreferencesUsed() {
		PREFERENCE_FILENAME = PreferenceActivity.PREFS_SHOPPINGLIST_FILENAME;
		DEFAULT_FILENAME = getString(R.string.default_shoppinglist_path);
		PREFERENCE_FORMAT = PreferenceActivity.PREFS_SHOPPINGLIST_FORMAT;
		DEFAULT_FORMAT = "outlook tasks";
		RES_STRING_FILEMANAGER_TITLE = R.string.filemanager_title_shoppinglist;
		RES_ARRAY_CSV_FILE_FORMAT = R.array.shoppinglist_format;
		RES_ARRAY_CSV_FILE_FORMAT_VALUE = R.array.shoppinglist_format_value;
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mConvertInfo.setText(R.string.convert_all_shoppinglists);
	}

	/**
	 * @param reader
	 * @throws IOException
	 */
	public void doImport(Reader reader) throws IOException,
			WrongFormatException {
		ImportCsv ic = new ImportCsv(this);
		if (0 == mSpinner.getSelectedItemId()) {
			ic.importCsv(reader);
		} else if (1 == mSpinner.getSelectedItemId()) {			
			ic.importHandyShopperCsv(reader);
		}
	}

	/**
	 * @param writer
	 * @throws IOException
	 */
	public void doExport(FileWriter writer) throws IOException {
		ExportCsv ec = new ExportCsv(this);		
		if (DEFAULT_FORMAT.equals(mSpinner.getSelectedItem())) {
			ec.exportCsv(writer);
		} else if (HANDYSHOPPER_FORMAT.equals(mSpinner.getSelectedItem())) {
			Toast.makeText(this, R.string.error_not_yet_implemented, Toast.LENGTH_LONG).show();
		}
	}

}