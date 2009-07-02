package org.openintents.notepad.activity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.openintents.notepad.NotePad;
import org.openintents.notepad.PrivateNotePadIntents;
import org.openintents.notepad.R;
import org.openintents.notepad.NotePad.Notes;
import org.openintents.notepad.filename.DialogHostingActivity;
import org.openintents.notepad.util.FileUriUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SaveFileActivity extends Activity {
	private static final String TAG = "SaveFileActivity";

	private static final int REQUEST_CODE_SAVE = 1;

	private static final int DIALOG_OVERWRITE_WARNING = 1;
	
	private static final String BUNDLE_SAVE_URI = "save_uri";
	private static final String BUNDLE_SAVE_FILENAME = "save_filename";
	
	Uri mSaveUri;
	File mSaveFilename;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			// retrieve data from saved instance
			if (savedInstanceState.containsKey(BUNDLE_SAVE_URI)) {
				mSaveUri = Uri.parse(savedInstanceState.getString(BUNDLE_SAVE_URI));
			}
			if (savedInstanceState.containsKey(BUNDLE_SAVE_FILENAME)) {
				mSaveFilename = new File(savedInstanceState.getString(BUNDLE_SAVE_FILENAME));
			}
		} else {
			// start new activity
			final Intent intent = getIntent();
			final Uri uri = intent.getData();
			if (uri != null) {
				saveToSdCard(uri);
			} else {
				Log.w(TAG, "Invalid URI");
				finish();
			}
		}
	}


	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mSaveUri != null) {
			outState.putString(BUNDLE_SAVE_URI, mSaveUri.toString());
		}
		if (mSaveFilename != null) {
			outState.putString(BUNDLE_SAVE_FILENAME, mSaveFilename.getAbsolutePath());
		}
	}


	private void saveToSdCard(Uri noteUri) {
		
		File sdcard = getSdCardPath();
		
		// Construct file name:
		Cursor c = getContentResolver().query(noteUri, new String[] {NotePad.Notes._ID, NotePad.Notes.TITLE}, null, null, null);
		String filename;
		if (c != null & c.moveToFirst()) {
			filename = c.getString(1) + ".txt";
		} else {
			Log.w(TAG, "Unvalid note URI");
			finish();
			return;
		}
		if (c != null) {
			c.close();
		}
		
		// Avoid dangerous characters:
		filename = filename.replace("/", "");
		filename = filename.replace("\\", "");
		filename = filename.replace(":", "");
		filename = filename.replace("?", "");
		filename = filename.replace("*", "");
		Uri uri = FileUriUtils.getUri(FileUriUtils.getFile(sdcard, filename));
		
		Intent i = new Intent(this, DialogHostingActivity.class);
		i.putExtra(DialogHostingActivity.EXTRA_DIALOG_ID, DialogHostingActivity.DIALOG_ID_SAVE);
		i.putExtra(PrivateNotePadIntents.EXTRA_URI, noteUri.toString());
		i.setData(uri);
		startActivityForResult(i, REQUEST_CODE_SAVE);
	}
	

    protected void onActivityResult (int requestCode, int resultCode, Intent intent) {
    	Log.i(TAG, "Received requestCode " + requestCode + ", resultCode " + resultCode);
    	switch(requestCode) {
    	case REQUEST_CODE_SAVE:
    		if (resultCode == RESULT_OK && intent != null) {
    			// File name should be in Uri:
    			File filename = FileUriUtils.getFile(intent.getData());
    			Uri uri = Uri.parse(intent.getStringExtra(PrivateNotePadIntents.EXTRA_URI));
    			
    			if (filename.exists()) {
    				// TODO Warning dialog

    				//Toast.makeText(this, "File exists already",
    				//		Toast.LENGTH_SHORT).show();
    				
    				// Remember for later:
    				// TODO: Store to bundle!
    				mSaveUri = uri;
    				mSaveFilename = filename;
    				
    				showDialog(DIALOG_OVERWRITE_WARNING);
    			} else {
    				// save file
    				saveFile(uri, filename);
    			}
    		} else {
    			// nothing to do.
    			finish();
    		}
    		break;
    	default:
    		// We should never reach here...
    		finish();
    	}
    }

    private File getSdCardPath() {
    	return android.os.Environment
			.getExternalStorageDirectory();
    }
    
    private void saveFile(Uri uri, File file) {
    	Log.i(TAG, "Saving file: uri: " + uri + ", file: " + file);
    	Cursor c = getContentResolver().query(uri, new String[] {Notes.ENCRYPTED, Notes.NOTE}, null, null, null);
    	
    	if (c != null && c.getCount() > 0) {
    		c.moveToFirst();
    		long encrypted = c.getLong(0);
    		String note = c.getString(1);
    		if (encrypted == 0) {
    			// Save to file
    			Log.d(TAG, "Save unencrypted file.");
    			writeToFile(file, note);
    		} else {
    			// TODO: decrypt first, then save to file

    			Log.d(TAG, "Save encrypted file.");
    		}
    	} else {
    		Log.e(TAG, "Error saving file: Uri not valid: " + uri);
    	}
    	
    	// Saving is the final step of this activity, so we can finish:
    	finish();
    }
    
    void writeToFile(File file, String text) {
	    try {
	    	FileWriter fstream = new FileWriter(file);
	        BufferedWriter out = new BufferedWriter(fstream);
		    out.write(text);
		    out.close();
			Toast.makeText(this, R.string.note_saved,
					Toast.LENGTH_SHORT).show();
	    } catch (IOException e) {
			Toast.makeText(this, R.string.error_writing_file,
					Toast.LENGTH_SHORT).show();
	    	Log.e(TAG, "Error writing file");
	    }
    }

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_OVERWRITE_WARNING:
			return getOverwriteWarningDialog();
		}
		return null;
	}
	

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		}
	}

	Dialog getOverwriteWarningDialog() {
		return new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.warning_file_exists_title)
		.setMessage(R.string.warning_file_exists_message)
		.setPositiveButton(android.R.string.ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						// click Ok
	    				// save file
	    				saveFile(mSaveUri, mSaveFilename);
					}
				})
		.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						// click Cancel
						finish();
					}
				})
		.create();
	}
}
