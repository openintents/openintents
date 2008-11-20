package org.openintents.voicenotes;

import org.openintents.intents.ProviderIntents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.util.Log;

public class VoiceNoteReceiver extends BroadcastReceiver {
	private static final String TAG = "VoiceNoteReceiver";

	private static final int NOTES = 1;
	private static final int NOTE_ID = 2;
	private static final int SHOPPINGLISTS = 3;
	private static final int SHOPPINGLIST_ID = 4;
	
	
    private static final UriMatcher sUriMatcher;
    
	@Override
	public void onReceive(Context context, Intent intent) {
		
		Log.i(TAG, "Received intent: " + intent.getAction() + ", " + intent.getDataString());
		
		String action = intent.getAction();
		Uri data = intent.getData();
		
		if (action.equals(ProviderIntents.ACTION_DELETED)) {
			
			delete(context, data);
		}

	}
	
	/**
	 * Delete all recordings connected to a URI.
	 * 
	 * @param context
	 * @param data
	 */
	private void delete(Context context, Uri data) {

        switch (sUriMatcher.match(data)) {
        case NOTES:
        case SHOPPINGLISTS:
        	
    		// delete all notes associated with this URI directory:
    		context.getContentResolver().delete(
    				VoiceNote.CONTENT_URI, 
    				VoiceNote.DATA_URI + " LIKE ?", 
    				new String[] { data.toString() + "/%" });
        	break;
        case NOTE_ID:
        case SHOPPINGLIST_ID:

    		// delete all notes associated with this URI item:
    		context.getContentResolver().delete(
    				VoiceNote.CONTENT_URI, 
    				VoiceNote.DATA_URI + " = ?", 
    				new String[] { data.toString()});
			break;
        }
	}

	static final String NOTEPAD_AUTHORITY = "org.openintents.notepad";
	static final String SHOPPINGLIST_AUTHORITY = "org.openintents.shopping";
	
	static {
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(NOTEPAD_AUTHORITY, "notes", NOTES);
        sUriMatcher.addURI(NOTEPAD_AUTHORITY, "notes/#", NOTE_ID);
        sUriMatcher.addURI(SHOPPINGLIST_AUTHORITY, "lists", SHOPPINGLISTS);
        sUriMatcher.addURI(SHOPPINGLIST_AUTHORITY, "lists/#", SHOPPINGLIST_ID);
	}
}
