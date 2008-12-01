package org.openintents.convertcsv.notepad;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openintents.convertcsv.R;
import org.openintents.convertcsv.shoppinglist.ExportCsv;
import org.openintents.convertcsv.shoppinglist.ImportCsv;
import org.openintents.provider.Shopping;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConvertCsvActivity extends Activity {
	
	public static final String TAG = "ConvertCsvActivity";
	
	EditText mEditText;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.convert);
        
        mEditText = (EditText) findViewById(R.id.file_path);
        
        Button buttonImport = (Button) findViewById(R.id.file_import);
        
        buttonImport.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startImport();
			}
        });
        
        Button buttonExport = (Button) findViewById(R.id.file_export);
        
        buttonExport.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startExport();
			}
        });
    }
    
    private void startImport() {
    	// First delete old lists
    	getContentResolver().delete(Shopping.Contains.CONTENT_URI, null, null);
    	getContentResolver().delete(Shopping.Items.CONTENT_URI, null, null);
    	getContentResolver().delete(Shopping.Lists.CONTENT_URI, null, null);
    	

    	String fileName = mEditText.getText().toString();
    	
    	Log.i(TAG, "Exporting...");
    	
    	File file = new File(fileName);
		if (true) { // (!file.exists()) {
			try{
				FileInputStream fis = new FileInputStream(file);
				
				DataInputStream dis = new DataInputStream(fis);
				
				ImportCsv ic = new ImportCsv(this);
				ic.importCsv(dis);
				
				dis.close();
			} catch (FileNotFoundException e) {
				Toast.makeText(this, R.string.error_writing_file, Toast.LENGTH_SHORT);
				Log.i(TAG, "File not found", e);
			} catch (IOException e) {
				Toast.makeText(this, R.string.error_writing_file, Toast.LENGTH_SHORT);
				Log.i(TAG, "IO exception", e);
				
			}
		}
    }

	private void startExport() {
    	
    	String fileName = mEditText.getText().toString();
    	
    	Log.i(TAG, "Exporting...");
    	
    	File file = new File(fileName);
		if (true) { // (!file.exists()) {
			try{
				FileOutputStream fos = new FileOutputStream(file);
				
				DataOutputStream dos = new DataOutputStream(fos);
				
				//dos.writeBytes("test, test2\ntest3, test4");

				ExportCsv ec = new ExportCsv(this);
				ec.exportCsv(dos);
	
				dos.close();
			} catch (FileNotFoundException e) {
				Toast.makeText(this, R.string.error_writing_file, Toast.LENGTH_SHORT);
				Log.i(TAG, "File not found", e);
			} catch (IOException e) {
				Toast.makeText(this, R.string.error_writing_file, Toast.LENGTH_SHORT);
				Log.i(TAG, "IO exception", e);
				
			}
		}
    }
}