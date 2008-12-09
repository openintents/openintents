package org.openintents.convertcsv.notepad;

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
    	PREFERENCE_FILENAME = PreferenceActivity.PREFS_NOTEPAD_FILENAME;
    }
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
    	mConvertInfo.setText(R.string.convert_all_notes);
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