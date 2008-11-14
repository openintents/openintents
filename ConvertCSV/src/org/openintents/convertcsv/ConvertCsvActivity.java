package org.openintents.convertcsv;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.ContainsFull;
import org.openintents.provider.Shopping.Lists;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ConvertCsvActivity extends Activity {
	
	private static final String TAG = "ConvertCsvActivity";
	
	EditText mEditText;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
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
    	
    }
    

	private static final String[] PROJECTION_LISTS = new String[] { Lists._ID,
			Lists.NAME, Lists.IMAGE, Lists.SHARE_NAME, Lists.SHARE_CONTACTS,
			Lists.SKIN_BACKGROUND };
	

	private static final String[] PROJECTION_CONTAINS_FULL = new String[] {
			ContainsFull._ID, ContainsFull.ITEM_NAME, ContainsFull.ITEM_IMAGE,
			ContainsFull.STATUS, ContainsFull.ITEM_ID,
			ContainsFull.SHARE_CREATED_BY, ContainsFull.SHARE_MODIFIED_BY };
	
    private void startExport() {
    	
    	String fileName = mEditText.getText().toString();
    	
    	Log.i(TAG, "Exporting...");
    	
    	File file = new File(fileName);
		if (true) { // (!file.exists()) {
			try{
				FileOutputStream fos = new FileOutputStream(file);
				
				DataOutputStream dos = new DataOutputStream(fos);
				
				//dos.writeBytes("test, test2\ntest3, test4");
			
		        Cursor c = getContentResolver().query(Shopping.Lists.CONTENT_URI, PROJECTION_LISTS, null, null,
		                Shopping.Lists.DEFAULT_SORT_ORDER);
				
		        if (c != null) {
		        	int listcount = c.getCount();

		        	Log.i(TAG, "Number of lists: " + listcount);
		        	
		        	while (c.moveToNext()) {
			        	String name = c.getString(c.getColumnIndexOrThrow(Shopping.Lists.NAME));
			        	
			        	Log.i(TAG, "List: " + name);
			        	
			        	dos.writeBytes(name + "\n");
		        	}
		        }
	
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