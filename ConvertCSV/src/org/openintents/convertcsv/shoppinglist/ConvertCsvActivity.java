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

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.openintents.convertcsv.PreferenceActivity;
import org.openintents.convertcsv.R;
import org.openintents.convertcsv.common.ConvertCsvBaseActivity;

import android.os.Bundle;

public class ConvertCsvActivity extends ConvertCsvBaseActivity {
	
	public static final String TAG = "ConvertCsvActivity";
	
    public void setPreferencesUsed() {
    	PREFERENCE_FILENAME = PreferenceActivity.PREFS_SHOPPINGLIST_FILENAME;
    	DEFAULT_FILENAME = getString(R.string.default_shoppinglist_path);
    	RES_STRING_FILEMANAGER_TITLE = R.string.filemanager_title_shoppinglist;
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
	public void doImport(FileReader reader) throws IOException {
		ImportCsv ic = new ImportCsv(this);
		ic.importCsv(reader);
	}
    
	/**
	 * @param writer
	 * @throws IOException
	 */
	public void doExport(FileWriter writer) throws IOException {
		ExportCsv ec = new ExportCsv(this);
		ec.exportCsv(writer);
	}
    
}