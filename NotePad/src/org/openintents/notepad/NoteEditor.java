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

/*
 * Original copyright:
 * Based on the Android SDK sample application NotePad.
 * Copyright (C) 2007 Google Inc.
 * Licensed under the Apache License, Version 2.0.
 */

package org.openintents.notepad;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.openintents.intents.CryptoIntents;
import org.openintents.notepad.NotePad.Notes;
import org.openintents.notepad.crypto.EncryptActivity;
import org.openintents.notepad.util.ExtractTitle;
import org.openintents.notepad.util.FileUriUtils;
import org.openintents.util.MenuIntentOptionsWithIcons;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

/**
 * A generic activity for editing a note in a database.  This can be used
 * either to simply view a note {@link Intent#ACTION_VIEW}, view and edit a note
 * {@link Intent#ACTION_EDIT}, or create a new note {@link Intent#ACTION_INSERT}.  
 */
public class NoteEditor extends Activity {
    private static final String TAG = "Notes";

    /**
     * Standard projection for the interesting columns of a normal note.
     */
    private static final String[] PROJECTION = new String[] {
            Notes._ID, // 0
            Notes.NOTE, // 1
            Notes.ENCRYPTED, // 2
    };
    /** The index of the note column */
    private static final int COLUMN_INDEX_ID = 0;
    private static final int COLUMN_INDEX_NOTE = 1;
    private static final int COLUMN_INDEX_ENCRYPTED = 2;
    
    // This is our state data that is stored when freezing.
    private static final String ORIGINAL_CONTENT = "origContent";
    private static final String ORIGINAL_STATE = "origState";

    // Identifiers for our menu items.
    private static final int MENU_REVERT = Menu.FIRST;
    private static final int MENU_DISCARD = Menu.FIRST + 1;
    private static final int MENU_DELETE = Menu.FIRST + 2;
    private static final int MENU_ENCRYPT = Menu.FIRST + 3;
	private static final int MENU_UNENCRYPT = Menu.FIRST + 4;

	private static final int REQUEST_CODE_DECRYPT = 2;

    // The different distinct states the activity can be run in.
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;
    //private static final int STATE_NOTE_FROM_SDCARD = 2;

    private int mState;
    private boolean mNoteOnly = false;
    private Uri mUri;
    private Cursor mCursor;
    private EditText mText;
    private String mOriginalContent;
    
    private String mDecryptedText;
    
    private String mFilename;

    /**
     * A custom EditText that draws lines between each line of text that is displayed.
     */
    public static class LinedEditText extends EditText {
        private Rect mRect;
        private Paint mPaint;

        // we need this constructor for LayoutInflater
        public LinedEditText(Context context, AttributeSet attrs) {
            super(context, attrs);
            
            mRect = new Rect();
            mPaint = new Paint();
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setColor(0x800000FF);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
            int count = getLineCount();
            Rect r = mRect;
            Paint paint = mPaint;

            for (int i = 0; i < count; i++) {
                int baseline = getLineBounds(i, r);

                canvas.drawLine(r.left, baseline + 1, r.right, baseline + 1, paint);
            }

            super.onDraw(canvas);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mDecryptedText = null;

        final Intent intent = getIntent();

        // Do some setup based on the action being performed.

        final String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action) || Intent.ACTION_VIEW.equals(action)) {
            // Requested to edit: set that state, and the data being edited.
            mState = STATE_EDIT;
            mUri = intent.getData();

            if (mUri.getScheme().equals("file")) {
            	// Load the file into a new note.
            	
            	mFilename = FileUriUtils.getFilename(mUri);
            	
            	String text = readFile(FileUriUtils.getFile(mUri));
            	
            	if (text == null) {
            		Log.e(TAG, "Error reading file");
                    finish();
                    return;
            	}
            	
            	// Let's check whether the exactly same note already exists or not:
            	Cursor c = getContentResolver().query(Notes.CONTENT_URI, 
            			new String[] {Notes._ID},
            			Notes.NOTE + " = ?", new String[] {text}, null);
            	if (c != null && c.getCount() > 0) {
            		// Same note exists already:
            		c.moveToFirst();
            		long id = c.getLong(0);
            		mUri = ContentUris.withAppendedId(Notes.CONTENT_URI, id);
            	} else {
	            	
	            	// Add new note
	            	// Requested to insert: set that state, and create a new entry
	                // in the container.
	                mState = STATE_INSERT;
	                ContentValues values = new ContentValues();
	                values.put(Notes.NOTE, text);
	                mUri = getContentResolver().insert(Notes.CONTENT_URI, values);
	                intent.setAction(Intent.ACTION_EDIT);
	                intent.setData(mUri);
	                setIntent(intent);
	
	                // If we were unable to create a new note, then just finish
	                // this activity.  A RESULT_CANCELED will be sent back to the
	                // original activity if they requested a result.
	                if (mUri == null) {
	                    Log.e(TAG, "Failed to insert new note into " + getIntent().getData());
	                    finish();
	                    return;
	                }
	
	                // The new entry was created, so assume all will end well and
	                // set the result to be returned.
	                //setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
	                setResult(RESULT_OK, intent);
            	}
        	}
        } else if (Intent.ACTION_INSERT.equals(action)) {
            // Requested to insert: set that state, and create a new entry
            // in the container.
            mState = STATE_INSERT;
            mUri = getContentResolver().insert(intent.getData(), null);
            intent.setAction(Intent.ACTION_EDIT);
            intent.setData(mUri);
            setIntent(intent);

            // If we were unable to create a new note, then just finish
            // this activity.  A RESULT_CANCELED will be sent back to the
            // original activity if they requested a result.
            if (mUri == null) {
                Log.e(TAG, "Failed to insert new note into " + getIntent().getData());
                finish();
                return;
            }

            // The new entry was created, so assume all will end well and
            // set the result to be returned.
            //setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
            setResult(RESULT_OK, intent);

        } else {
            // Whoops, unknown action!  Bail.
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        requestWindowFeature(Window.FEATURE_RIGHT_ICON);
        
        // Set the layout for this activity.  You can find it in res/layout/note_editor.xml
        setContentView(R.layout.note_editor);
        
        // The text view for our note, identified by its ID in the XML file.
        mText = (EditText) findViewById(R.id.note);
        

        // Get the note!
        mCursor = managedQuery(mUri, PROJECTION, null, null, null);

        // If an instance of this activity had previously stopped, we can
        // get the original text it started with.
        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
            mState = savedInstanceState.getInt(ORIGINAL_STATE);
        }
    }
    
    public String readFile(File file) {

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        DataInputStream dis = null;
        StringBuffer sb = new StringBuffer();

        try {
          fis = new FileInputStream(file);

          // Here BufferedInputStream is added for fast reading.
          bis = new BufferedInputStream(fis);
          dis = new DataInputStream(bis);

          // dis.available() returns 0 if the file does not have more lines.
          while (dis.available() != 0) {

          // this statement reads the line from the file and print it to
            // the console.
        	  sb.append(dis.readLine());
        	  if (dis.available() != 0) {
        		  sb.append("\n");
        	  }
          }

          // dispose all the resources after using them.
          fis.close();
          bis.close();
          dis.close();

        } catch (FileNotFoundException e) {
        	Log.e(TAG, "File not found", e);
			Toast.makeText(this, R.string.file_not_found,
					Toast.LENGTH_SHORT).show();
			return null;
        } catch (IOException e) {
        	Log.e(TAG, "File not found", e);
			Toast.makeText(this, R.string.error_reading_file,
					Toast.LENGTH_SHORT).show();
			return null;
        }
        
        return sb.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

        // If we didn't have any trouble retrieving the data, it is now
        // time to get at the stuff.
        if (mCursor != null) {
        	mCursor.requery();
            // Make sure we are at the one and only row in the cursor.
            mCursor.moveToFirst();

            // Modify our overall title depending on the mode we are running in.
            if (mFilename == null) {
	            if (mState == STATE_EDIT) {
	                setTitle(getText(R.string.title_edit));
	            } else if (mState == STATE_INSERT) {
	                setTitle(getText(R.string.title_create));
	            }
            } else {
            	if (mState == STATE_EDIT) {
	                setTitle(getString(R.string.title_edit_file, mFilename));
	            } else if (mState == STATE_INSERT) {
	                setTitle(getString(R.string.title_create_file, mFilename));
	            }
            }

            long id = mCursor.getLong(COLUMN_INDEX_ID);
            String note = mCursor.getString(COLUMN_INDEX_NOTE);
            long encrypted = mCursor.getLong(COLUMN_INDEX_ENCRYPTED);
            
            if (encrypted == 0) {
            	// Not encrypted

	            // This is a little tricky: we may be resumed after previously being
	            // paused/stopped.  We want to put the new text in the text view,
	            // but leave the user where they were (retain the cursor position
	            // etc).  This version of setText does that for us.
	            mText.setTextKeepState(note);
            } else {
            	if (mDecryptedText != null) {
            		// Text had already been decrypted, use that:
            		mText.setTextKeepState(mDecryptedText);
            		setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, android.R.drawable.ic_lock_idle_lock);
            	} else {
            	// Decrypt note
	
	        		Intent i = new Intent();
	        		i.setAction(CryptoIntents.ACTION_DECRYPT);
	        		i.putExtra(CryptoIntents.EXTRA_TEXT, note);
	        		i.putExtra(NotePadIntents.EXTRA_ID, id);
	                
	                try {
	                	startActivityForResult(i, REQUEST_CODE_DECRYPT);
	                } catch (ActivityNotFoundException e) {
	        			Toast.makeText(this,
	        					R.string.decryption_failed,
	        					Toast.LENGTH_SHORT).show();
	        			Log.e(TAG, "failed to invoke decrypt");
	                }
            	}
            }
            
            // If we hadn't previously retrieved the original text, do so
            // now.  This allows the user to revert their changes.
            if (mOriginalContent == null) {
                mOriginalContent = note;
            }

        } else {
            setTitle(getText(R.string.error_title));
            mText.setText(getText(R.string.error_message));
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save away the original text, so we still have it if the activity
        // needs to be killed while paused.
        outState.putString(ORIGINAL_CONTENT, mOriginalContent);
        outState.putInt(ORIGINAL_STATE, mState);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");

        // The user is going somewhere else, so make sure their current
        // changes are safely saved away in the provider.  We don't need
        // to do this if only editing.
        if (mCursor != null) {

        	mCursor.moveToFirst();
        	
            long encrypted = mCursor.getLong(COLUMN_INDEX_ENCRYPTED);
            
            if (encrypted == 0) {
                String text = mText.getText().toString();
	            int length = text.length();
	
	            // If this activity is finished, and there is no text, then we
	            // do something a little special: simply delete the note entry.
	            // Note that we do this both for editing and inserting...  it
	            // would be reasonable to only do it when inserting.
	            if (isFinishing() && (length == 0) && !mNoteOnly) {
	                setResult(RESULT_CANCELED);
	                deleteNote();
	
	            // Get out updates into the provider.
	            } else {
	                ContentValues values = new ContentValues();
	
	                // This stuff is only done when working with a full-fledged note.
	                if (!mNoteOnly) {
	                    // Bump the modification time to now.
	                    values.put(Notes.MODIFIED_DATE, System.currentTimeMillis());
	                    
	                    String title = ExtractTitle.extractTitle(text);
		                values.put(Notes.TITLE, title);
	                }
	
	                // Write our text back into the provider.
	                values.put(Notes.NOTE, text);
	
	                // Commit all of our changes to persistent storage. When the update completes
	                // the content provider will notify the cursor of the change, which will
	                // cause the UI to be updated.
	                getContentResolver().update(mUri, values, null, null);
	                
	            }
            } else {
            	// encrypted note: First encrypt and store encrypted note:

	            if (mDecryptedText != null) {
	            	// Decrypted had been decrypted.
	            	// We take the current version from 'text' and encrypt it.
	            	
	            	encryptNote();
	            }
            }
        }
    }

	/**
	 * Encrypt the current note.
	 * @param text
	 */
	private void encryptNote() {
        String text = mText.getText().toString();
        String title = ExtractTitle.extractTitle(text);
        
		Intent i = new Intent(this, EncryptActivity.class);
		i.putExtra(NotePadIntents.EXTRA_ACTION, CryptoIntents.ACTION_ENCRYPT);
		i.putExtra(CryptoIntents.EXTRA_TEXT_ARRAY, EncryptActivity.getCryptoStringArray(text, title, null));
		i.putExtra(NotePadIntents.EXTRA_URI, mUri.toString());
		startActivity(i);
	}
	
	/**
	 * Unencrypt the current note.
	 * @param text
	 */
	private void unencryptNote() {
        String text = mText.getText().toString();
        String title = ExtractTitle.extractTitle(text);
        
        ContentValues values = new ContentValues();
        values.put(Notes.MODIFIED_DATE, System.currentTimeMillis());
        values.put(Notes.TITLE, title);
        values.put(Notes.NOTE, text);
       	values.put(Notes.ENCRYPTED, 0);
        
        getContentResolver().update(mUri, values, null, null);
        mCursor.requery();
        
		setFeatureDrawable(Window.FEATURE_RIGHT_ICON, null);
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        // Build the menus that are shown when editing.
        
        //if (!mOriginalContent.equals(mText.getText().toString())) {

            menu.add(0, MENU_REVERT, 0, R.string.menu_revert)
                    .setShortcut('0', 'r')
                    .setIcon(android.R.drawable.ic_menu_revert);
        //}

        menu.add(1, MENU_ENCRYPT, 0, R.string.menu_encrypt)
                .setShortcut('0', 'e')
                .setIcon(android.R.drawable.ic_lock_lock); // TODO: better icon

        menu.add(1, MENU_UNENCRYPT, 0, R.string.menu_undo_encryption)
                .setShortcut('0', 'e')
                .setIcon(android.R.drawable.ic_lock_lock); // TODO: better icon
        
        menu.add(1, MENU_DELETE, 0, R.string.menu_delete)
            .setShortcut('1', 'd')
            .setIcon(android.R.drawable.ic_menu_delete);
        
        /*
        if (mState == STATE_EDIT) {
        	
            menu.add(0, REVERT_ID, 0, R.string.menu_revert)
                    .setShortcut('0', 'r')
                    .setIcon(android.R.drawable.ic_menu_revert);
                   
            if (!mNoteOnly) {
                menu.add(1, DELETE_ID, 0, R.string.menu_delete)
                        .setShortcut('1', 'd')
                        .setIcon(android.R.drawable.ic_menu_delete);
            }

        // Build the menus that are shown when inserting.
        } else {
            menu.add(1, DISCARD_ID, 0, R.string.menu_discard)
                    .setShortcut('0', 'd')
                    .setIcon(android.R.drawable.ic_menu_delete);
        }
        */

        // If we are working on a full note, then append to the
        // menu items for any other activities that can do stuff with it
        // as well.  This does a query on the system for any activities that
        // implement the ALTERNATIVE_ACTION for our data, adding a menu item
        // for each one that is found.
        if (!mNoteOnly) {
            Intent intent = new Intent(null, getIntent().getData());
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            //menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
            //        new ComponentName(this, NoteEditor.class), null, intent, 0, null);
            
            // Workaround to add icons:
            MenuIntentOptionsWithIcons menu2 = new MenuIntentOptionsWithIcons(this, menu);
            menu2.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                            new ComponentName(this, NoteEditor.class), null, intent, 0, null);
            
        }

        return true;
    }

    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {

    	// Show "revert" menu item only if content has changed.
    	boolean contentChanged = !mOriginalContent.equals(mText.getText().toString());
    	menu.setGroupVisible(0, contentChanged);
    	
    	mCursor.moveToFirst();
    	long encrypted = mCursor.getLong(COLUMN_INDEX_ENCRYPTED);
    	boolean showEnrypt = (encrypted == 0);
    	
    	menu.findItem(MENU_ENCRYPT).setVisible(showEnrypt);
    	menu.findItem(MENU_UNENCRYPT).setVisible(!showEnrypt);
    	
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
        case MENU_DELETE:
            deleteNote();
            finish();
            break;
        case MENU_DISCARD:
            cancelNote();
            break;
        case MENU_REVERT:
            cancelNote();
            break;
        case MENU_ENCRYPT:
        	encryptNote();
        	break;
        case MENU_UNENCRYPT:
        	unencryptNote();
        	break;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Take care of canceling work on a note.  Deletes the note if we
     * had created it, otherwise reverts to the original text.
     */
    private final void cancelNote() {
        if (mCursor != null) {
        	String tmp = mText.getText().toString();
        	//if (mState == STATE_EDIT) {
                // Put the original note text back into the database
                //mCursor.close();
                //mCursor = null;
                //ContentValues values = new ContentValues();
                //values.put(Notes.NOTE, mOriginalContent);
                //getContentResolver().update(mUri, values, null, null);
            	mText.setText(mOriginalContent);
            //} else if (mState == STATE_INSERT) {
                // We inserted an empty note, make sure to delete it
                //deleteNote();
                //mText.setText("");
            //}
        	mOriginalContent = tmp;
        }
        //mCursor.requery();
        //setResult(RESULT_CANCELED);
        //finish();
    }

    /**
     * Take care of deleting a note.  Simply deletes the entry.
     */
    private final void deleteNote() {
        if (mCursor != null) {
            mCursor.close();
            mCursor = null;
            getContentResolver().delete(mUri, null, null);
            mText.setText("");
        }
    }
    /*
    private final void discardNote() {
        //if (mCursor != null) {
        //    mCursor.close();
        //    mCursor = null;
        //    getContentResolver().delete(mUri, null, null);
        //    mText.setText("");
        //}
    	mOriginalContent = mText.getText().toString();
    	mText.setText("");
    }
    */

    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	Log.i(TAG, "Received requestCode " + requestCode + ", resultCode " + resultCode);
    	switch(requestCode) {
    	case REQUEST_CODE_DECRYPT:
    		if (resultCode == RESULT_OK && data != null) {
    			String decryptedText = data.getStringExtra (CryptoIntents.EXTRA_TEXT);
    			long id = data.getLongExtra(NotePadIntents.EXTRA_ID, -1);
    			
    			// TODO: Check that id corresponds to current intent.
    			
    			if (id == -1) {
        	    	Log.i(TAG, "Wrong extra id");
    				Toast.makeText(this,
        					"Decrypted information incomplete",
        					Toast.LENGTH_SHORT).show();

            		finish();
    				return;
    			}

    	    	mDecryptedText = decryptedText;
	            
    		} else {
    			Toast.makeText(this,
    					R.string.decryption_failed,
    					Toast.LENGTH_SHORT).show();
    			Log.e(TAG, "decryption failed");
    			
        		finish();
    		}
    		break;
    	}
    }
}
