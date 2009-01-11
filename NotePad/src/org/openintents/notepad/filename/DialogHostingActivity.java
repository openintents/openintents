package org.openintents.notepad.filename;

import org.openintents.distribution.GetFileManagerFromMarketDialog;
import org.openintents.intents.FileManagerIntents;
import org.openintents.notepad.NotePadIntents;
import org.openintents.notepad.R;
import org.openintents.notepad.util.IntentUtils;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

public class DialogHostingActivity extends Activity {

	private static final String TAG = "FilenameActivity";

	public static final int DIALOG_ID_SAVE = 1;
	public static final int DIALOG_ID_OPEN = 2;
	public static final int DIALOG_ID_NO_FILE_MANAGER_AVAILABLE = 3;
	
	public static final String EXTRA_DIALOG_ID = "org.openintents.notepad.extra.dialog_id";
	
    EditText mEditText;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		if (i != null) {
			int dialogId = i.getIntExtra(EXTRA_DIALOG_ID, 0);
			switch (dialogId) {
			case DIALOG_ID_SAVE:
				Log.i(TAG, "Show Save dialog");
				saveFile();
				break;
			case DIALOG_ID_OPEN:
				Log.i(TAG, "Show Save dialog");
				openFile();
				break;
			case DIALOG_ID_NO_FILE_MANAGER_AVAILABLE:
				Log.i(TAG, "Show no file manager dialog");
				showDialog(DIALOG_ID_NO_FILE_MANAGER_AVAILABLE);
				break;
			}
		}
		
		
	}


	/**
	 * 
	 */
	private void saveFile() {
		
		// Check whether intent exists
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
		intent.setData(getIntent().getData());
		if (IntentUtils.isIntentAvailable(this, intent)) {
			intent.putExtra(NotePadIntents.EXTRA_URI, getIntent().getStringExtra(NotePadIntents.EXTRA_URI));
			intent.putExtra(FileManagerIntents.EXTRA_TITLE, getText(R.string.menu_save_to_sdcard));
			intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getText(R.string.save));
			intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
			startActivity(intent);
			finish();
		} else {
			showDialog(DIALOG_ID_SAVE);
		}
	}
	

	private void openFile() {
		
		// Check whether intent exists
		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_FILE);
		intent.setData(getIntent().getData());
		if (IntentUtils.isIntentAvailable(this, intent)) {
			intent.putExtra(NotePadIntents.EXTRA_URI, getIntent().getStringExtra(NotePadIntents.EXTRA_URI));
			intent.putExtra(FileManagerIntents.EXTRA_TITLE, getText(R.string.menu_open_from_sdcard));
			intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getText(R.string.open));
			intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
			startActivity(intent);
			finish();
		} else {
			showDialog(DIALOG_ID_OPEN);
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_ID_SAVE:
			return new FilenameDialog(this);
		case DIALOG_ID_OPEN:
			return new FilenameDialog(this);
		case DIALOG_ID_NO_FILE_MANAGER_AVAILABLE:
			Log.i(TAG, "fmd - create");
			return new GetFileManagerFromMarketDialog(this);

		}
		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		FilenameDialog fd;
		
		dialog.setOnDismissListener(mDismissListener);
		
		switch (id) {
		case DIALOG_ID_SAVE:
			fd = (FilenameDialog) dialog;
			fd.setTitle(R.string.menu_save_to_sdcard);
			
			break;
		case DIALOG_ID_OPEN:
			fd = (FilenameDialog) dialog;
			fd.setTitle(R.string.menu_open_from_sdcard);
			break;
			
		case DIALOG_ID_NO_FILE_MANAGER_AVAILABLE:
			Log.i(TAG, "fmd - prepare");
			/*
			GetFileManagerFromMarketDialog gd = (GetFileManagerFromMarketDialog) dialog;
			gd.setMessageResource(R.string.filemanager_not_available);
			gd.setInfoResources(R.string.filemanager_get_oi_filemanager, 
					R.string.filemanager_market_uri, 
					R.drawable.ic_launcher_folder_small, 
					R.string.update_error);
					*/
			break;
		}
	}
	
	OnDismissListener mDismissListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialoginterface) {
			DialogHostingActivity.this.finish();
		}
		
	};
	
}
