package org.openintents.convertcsv;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.openintents.provider.Shopping;
import org.openintents.provider.Shopping.Contains;
import org.openintents.provider.Shopping.ContainsFull;
import org.openintents.provider.Shopping.Items;
import org.openintents.provider.Shopping.Lists;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
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
				
				String line = dis.readLine();

				if (line != null && line.startsWith("Subject")) {
					// ignore first line
					line = dis.readLine();
				}
				while (line != null) {
					String[] tokens = line.split(",");
					if (tokens.length == 3) {
						String itemname = tokens[0];
						long status = (tokens[1].equals("1")) ? 1 : 0;
						String listname = tokens[2];

						// Add item to list
						long listId = getOrCreateListId(listname);
						long itemId = getItemId(itemname);
						
						if (status == 1) {
							status = Shopping.Status.BOUGHT;
						} else {
							status = Shopping.Status.WANT_TO_BUY;
						}
						
						addItemToList(itemId, listId, status);
						
					}
					
					
					line = dis.readLine();
				}
				
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
    

	public long getOrCreateListId(String listName) {
		Cursor cursor = getContentResolver().query(Lists.CONTENT_URI,
				new String[] { Lists._ID, Lists.NAME }, Lists.NAME + " = ?",
				new String[] { listName }, Lists.DEFAULT_SORT_ORDER);
		if (cursor != null && cursor.moveToNext()) {
			return cursor.getLong(cursor.getColumnIndexOrThrow(Lists._ID));
		} else {
			ContentValues values = new ContentValues(1);
			values.put(Lists.NAME, listName);
			Uri uri = getContentResolver().insert(Lists.CONTENT_URI, values);
			Log.i(TAG, "Insert new list: " + uri);
			return Long.parseLong(uri.getPathSegments().get(1));
		}
	}
	
	public long getItemId(final String name) {
		// TODO check whether item exists

		// Add item to list:
		ContentValues values = new ContentValues(1);
		values.put(Items.NAME, name);
		try {
			Uri uri = getContentResolver().insert(Items.CONTENT_URI, values);
			Log.i(TAG, "Insert new item: " + uri);
			return Long.parseLong(uri.getPathSegments().get(1));
		} catch (Exception e) {
			Log.i(TAG, "Insert item failed", e);
			return -1;
		}
	}
	
	public long addItemToList(final long itemId, final long listId, final long status) {
		// TODO check whether "contains" entry exists

		// Add item to list:
		ContentValues values = new ContentValues(2);
		values.put(Contains.ITEM_ID, itemId);
		values.put(Contains.LIST_ID, listId);
		values.put(Contains.STATUS, status);
		try {
			Uri uri = getContentResolver().insert(Contains.CONTENT_URI, values);
			Log.i(TAG, "Insert new entry in 'contains': " + uri);
			return Long.parseLong(uri.getPathSegments().get(1));
		} catch (Exception e) {
			Log.i(TAG, "insert into table 'contains' failed", e);
			return -1;
		}
	}

	private static final String[] PROJECTION_LISTS = new String[] { Lists._ID,
			Lists.NAME, Lists.IMAGE, Lists.SHARE_NAME, Lists.SHARE_CONTACTS,
			Lists.SKIN_BACKGROUND };
	

	private static final String[] PROJECTION_CONTAINS_FULL = new String[] {
			ContainsFull._ID, ContainsFull.ITEM_NAME, ContainsFull.ITEM_IMAGE,
			ContainsFull.STATUS, ContainsFull.ITEM_ID,
			ContainsFull.LIST_ID, 
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

	        	dos.writeBytes("Subject" + "," + "% Complete" + "," + "Categories" + "\n");
			
		        Cursor c = getContentResolver().query(Shopping.Lists.CONTENT_URI, PROJECTION_LISTS, null, null,
		                Shopping.Lists.DEFAULT_SORT_ORDER);
				
		        if (c != null) {
		        	int listcount = c.getCount();

		        	Log.i(TAG, "Number of lists: " + listcount);
		        	
		        	while (c.moveToNext()) {
			        	String listname = c.getString(c.getColumnIndexOrThrow(Shopping.Lists.NAME));
			        	long id = c.getLong(c.getColumnIndexOrThrow(Shopping.Lists._ID));
			        	
			        	Log.i(TAG, "List: " + listname);
			        	
			        	Cursor ci = getContentResolver().query(Shopping.ContainsFull.CONTENT_URI, 
			        			PROJECTION_CONTAINS_FULL, Shopping.ContainsFull.LIST_ID + " = ?", 
			        			new String[] {""+id}, Shopping.ContainsFull.DEFAULT_SORT_ORDER);
			        	

				        if (ci != null) {
				        	int itemcount = ci.getCount();

				        	while (ci.moveToNext()) {
				        		String itemname = ci.getString(ci.getColumnIndexOrThrow(Shopping.ContainsFull.ITEM_NAME));
				        		int status = ci.getInt(ci.getColumnIndexOrThrow(Shopping.ContainsFull.STATUS));
					        	int percentage = (status == Shopping.Status.BOUGHT) ? 1 : 0;

					        	dos.writeBytes(itemname + "," + percentage + "," + listname + "\n");
				        	}
				        }
			        	//dos.writeBytes("\n");
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