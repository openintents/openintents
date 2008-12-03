package org.openintents.convertcsv.shoppinglist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.openintents.convertcsv.PreferenceActivity;
import org.openintents.convertcsv.R;
import org.openintents.convertcsv.common.ConvertCsvBaseActivity;

import android.util.Log;
import android.widget.Toast;

public class ConvertCsvActivity extends ConvertCsvBaseActivity {
	
	public static final String TAG = "ConvertCsvActivity";
	
    public void setPreferencesUsed() {
    	PREFERENCE_FILENAME = PreferenceActivity.PREFS_SHOPPINGLIST_FILENAME;
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