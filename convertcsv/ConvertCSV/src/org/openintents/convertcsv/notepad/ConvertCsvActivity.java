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

package org.openintents.convertcsv.notepad;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.openintents.convertcsv.PreferenceActivity;
import org.openintents.convertcsv.R;
import org.openintents.convertcsv.common.ConvertCsvBaseActivity;
import org.openintents.convertcsv.common.WrongFormatException;

import android.os.Bundle;

public class ConvertCsvActivity extends ConvertCsvBaseActivity {
	
	public static final String TAG = "ConvertCsvActivity";
	
    public void setPreferencesUsed() {
    	PREFERENCE_FILENAME = PreferenceActivity.PREFS_NOTEPAD_FILENAME;
    	DEFAULT_FILENAME = getString(R.string.default_notepad_filename);
    	PREFERENCE_FORMAT = PreferenceActivity.PREFS_NOTEPAD_FORMAT;
    	DEFAULT_FORMAT = ImportCsv.FORMAT_PALM_CSV;
    	PREFERENCE_ENCODING = PreferenceActivity.PREFS_NOTEPAD_ENCODING;
    	PREFERENCE_USE_CUSTOM_ENCODING = PreferenceActivity.PREFS_NOTEPAD_USE_CUSTOM_ENCODING;
    	RES_STRING_FILEMANAGER_TITLE = R.string.filemanager_title_notepad;
    	RES_ARRAY_CSV_FILE_FORMAT = R.array.notepad_format;
    	RES_ARRAY_CSV_FILE_FORMAT_VALUE = R.array.notepad_format_value;
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        if (mConvertInfo != null) {
        	mConvertInfo.setText(R.string.convert_all_notes);
        }
    }
    
    @Override
    public String getImportPolicyPrefString() {
    	return "notepad_import_policy";
    }
    
	/**
	 * @param reader
	 * @throws IOException
	 */
	public void doImport(Reader reader) throws IOException,
				WrongFormatException {
		ImportCsv ic = new ImportCsv(this);
		ic.importCsv(reader, getFormat(), getValidatedImportPolicy());
	}
    
	/**
	 * @param writer
	 * @throws IOException
	 */
	public void doExport(Writer writer) throws IOException {
		ExportCsv ec = new ExportCsv(this);
		ec.exportCsv(writer, getFormat());
	}


	
}