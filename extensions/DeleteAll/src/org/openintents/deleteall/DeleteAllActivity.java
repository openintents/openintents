package org.openintents.deleteall;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class DeleteAllActivity extends Activity {

	private static final String TAG = "DeleteAllActivity";
	private static final boolean debug = !false;

	public static final int DIALOG_ID_DELETE_NOTES = 1;
	public static final int DIALOG_ID_DELETE_SHOPPING_LISTS = 2;
	
	public static final String EXTRA_DIALOG_ID = "org.openintents.deleteall.extra.dialog_id";
	
	Uri contentUriNotepad = Uri.parse("content://org.openintents.notepad/notes");
	
	Uri contentUriShoppingLists = Uri.parse("content://org.openintents.shopping/lists");
	Uri contentUriShoppingItems = Uri.parse("content://org.openintents.shopping/items");

	/**
	 * Whether dialog is simply pausing while hidden by another activity
	 * or when configuration changes.
	 * If this is false, then we can safely finish this activity if a dialog
	 * gets dismissed.
	 */
	private boolean mIsPausing = false;
	
    /** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Intent i = getIntent();
		if (i != null && savedInstanceState == null) {
			if (debug) Log.d(TAG, "new dialog");
			Uri data = i.getData();
			if (debug) Log.d(TAG, "Intent data: " + data.toString());
			if (data.compareTo(contentUriNotepad) == 0) {
				showDialog(DIALOG_ID_DELETE_NOTES);
			} else if (data.toString().startsWith(contentUriShoppingLists.toString())) {
				showDialog(DIALOG_ID_DELETE_SHOPPING_LISTS);
			}
		}
	}



	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog = null;
		
		switch (id) {
		case DIALOG_ID_DELETE_NOTES:

            dialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.delete_all_notes)
                .setMessage(R.string.delete_all_notes_confirm)
                .setPositiveButton(R.string.delete_all_notes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	deleteAllNotes();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	// Don't do anything
                    }
                })
                .create();
			break;
		case DIALOG_ID_DELETE_SHOPPING_LISTS:

            dialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.delete_all_lists)
                .setMessage(R.string.delete_all_lists_confirm)
                .setPositiveButton(R.string.delete_all_lists, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	deleteAllShoppingLists();
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    	// Don't do anything
                    }
                })
                .create();
			break;
		}
		if (dialog == null) {
			dialog = super.onCreateDialog(id);
		}
		if (dialog != null) {
			// Close this activity if dialog gets closed
			dialog.setOnDismissListener(mDismissListener);
		}
		return dialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		
		switch (id) {
		case DIALOG_ID_DELETE_NOTES:
			break;
		}
	}
	
	OnDismissListener mDismissListener = new OnDismissListener() {
		
		public void onDismiss(DialogInterface dialoginterface) {
			if (debug) Log.d(TAG, "Dialog dismissed. Pausing: " + mIsPausing);
			if (!mIsPausing) {
				if (debug) Log.d(TAG, "finish");
				// Dialog has been dismissed by user.
				DeleteAllActivity.this.finish();
			} else {
				// Probably just a screen orientation change. Don't finish yet.
				// Dialog has been dismissed by system.
			}
		}
		
	};

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (debug) Log.d(TAG, "onSaveInstanceState");
		// It is important to set mIsPausing here, so that
		// the dialog does not get closed on orientation changes.
		mIsPausing = true;
		if (debug) Log.d(TAG, "onSaveInstanceState. Pausing: " + mIsPausing);
	}

	@Override
	protected void onResume() {
		if (debug) Log.d(TAG, "onResume");
		super.onResume();
		// In case another activity is called, and we are resumed,
		// mIsPausing should be reset to its original state.
		mIsPausing = false;
	}
	
	private void deleteAllNotes() {
		// Delete all notes
		getContentResolver().delete(contentUriNotepad, null, null);
	}

	private void deleteAllShoppingLists() {
		ContentResolver cr = getContentResolver();
		// Delete all items
		cr.delete(contentUriShoppingItems, null, null);
		
		// Delete all lists
		cr.delete(contentUriShoppingLists, null, null);
	}
	
}