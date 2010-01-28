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
import org.openintents.intents.NotepadIntents;
import org.openintents.notepad.NotePad.Notes;
import org.openintents.notepad.activity.SaveFileActivity;
import org.openintents.notepad.crypto.EncryptActivity;
import org.openintents.notepad.dialog.ThemeDialog;
import org.openintents.notepad.dialog.ThemeDialog.ThemeDialogListener;
import org.openintents.notepad.intents.NotepadInternalIntents;
import org.openintents.notepad.util.ExtractTitle;
import org.openintents.notepad.util.FileUriUtils;
import org.openintents.util.MenuIntentOptionsWithIcons;
import org.openintents.util.ThemeNotepad;
import org.openintents.util.ThemeUtils;
import org.openintents.util.UpperCaseTransformationMethod;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.Layout;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A generic activity for editing a note in a database.  This can be used
 * either to simply view a note {@link Intent#ACTION_VIEW}, view and edit a note
 * {@link Intent#ACTION_EDIT}, or create a new note {@link Intent#ACTION_INSERT}.  
 */
public class NoteEditor extends Activity implements ThemeDialogListener {
    private static final String TAG = "Notes";
    private static final boolean debug = true;

    /**
     * Standard projection for the interesting columns of a normal note.
     */
    private static final String[] PROJECTION = new String[] {
            Notes._ID, // 0
            Notes.NOTE, // 1
            Notes.TAGS, // 2
            Notes.ENCRYPTED, // 3
            Notes.THEME // 4
    };
    /** The index of the note column */
    private static final int COLUMN_INDEX_ID = 0;
    private static final int COLUMN_INDEX_NOTE = 1;
    private static final int COLUMN_INDEX_TAGS = 2;
    private static final int COLUMN_INDEX_ENCRYPTED = 3;
    private static final int COLUMN_INDEX_THEME = 4;
    
    // This is our state data that is stored when freezing.
    private static final String BUNDLE_ORIGINAL_CONTENT = "original_content";
    private static final String BUNDLE_STATE = "state";
    private static final String BUNDLE_URI = "uri";
    private static final String BUNDLE_SELECTION_START = "selection_start";
    private static final String BUNDLE_SELECTION_STOP = "selection_stop";
    private static final String BUNDLE_FILENAME = "filename";
    private static final String BUNDLE_FILE_CONTENT = "file_content";
    

    // Identifiers for our menu items.
    private static final int MENU_REVERT = Menu.FIRST;
    private static final int MENU_DISCARD = Menu.FIRST + 1;
    private static final int MENU_DELETE = Menu.FIRST + 2;
    private static final int MENU_ENCRYPT = Menu.FIRST + 3;
	private static final int MENU_UNENCRYPT = Menu.FIRST + 4;
	private static final int MENU_IMPORT = Menu.FIRST + 5;
	private static final int MENU_SAVE = Menu.FIRST + 6;
	private static final int MENU_SAVE_AS = Menu.FIRST + 7;
	private static final int MENU_THEME = Menu.FIRST + 8;
 	private static final int MENU_SETTINGS = Menu.FIRST + 9;
 	
	private static final int REQUEST_CODE_DECRYPT = 2;
	private static final int REQUEST_CODE_TEXT_SELECTION_ALTERNATIVE = 3;
	private static final int REQUEST_CODE_SAVE_AS = 4;

    // The different distinct states the activity can be run in.
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;
    private static final int STATE_EDIT_NOTE_FROM_SDCARD = 2;
    
    private static final int DIALOG_UNSAVED_CHANGES = 1;
    private static final int DIALOG_THEME = 2;
    
    private static final int GROUP_ID_TEXT_SELECTION_ALTERNATIVE = 1234; // some number that must not collide with others

    private int mState;
    private boolean mNoteOnly = false;
    private Uri mUri;
    private Cursor mCursor;
    private EditText mText;
    private String mOriginalContent;
    private int mSelectionStart;
    private int mSelectionStop;
    
    private String mDecryptedText;
    
    private String mFileContent;
    
    private String mTags;
    
    private String mTheme;
    
    Typeface mCurrentTypeface = null;
    public String mTextTypeface;
    public float mTextSize;
	public boolean mTextUpperCaseFont;
	public int mTextColor;
	public int mBackgroundPadding;
	
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
        
        if (debug) Log.d(TAG, "onCreate()");
        
        mDecryptedText = null;
        mSelectionStart = 0;
        mSelectionStop = 0;

        // If an instance of this activity had previously stopped, we can
        // get the original text it started with.
        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(BUNDLE_ORIGINAL_CONTENT);
            mState = savedInstanceState.getInt(BUNDLE_STATE);
            mUri = Uri.parse(savedInstanceState.getString(BUNDLE_URI));
            mSelectionStart = savedInstanceState.getInt(BUNDLE_SELECTION_START);
            mSelectionStop = savedInstanceState.getInt(BUNDLE_SELECTION_STOP);
            mFileContent = savedInstanceState.getString(BUNDLE_FILE_CONTENT);
        } else {
            // Do some setup based on the action being performed.
	        final Intent intent = getIntent();
	
	
	        final String action = intent.getAction();
	        if (Intent.ACTION_EDIT.equals(action) || Intent.ACTION_VIEW.equals(action)) {
	            // Requested to edit: set that state, and the data being edited.
	            mState = STATE_EDIT;
	            mUri = intent.getData();
	            
	            if (mUri.getScheme().equals("file")) {
	            	mState = STATE_EDIT_NOTE_FROM_SDCARD;
	            	// Load the file into a new note.
	            	
	            	mFileContent = readFile(FileUriUtils.getFile(mUri));
	            }
	            /*
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
	            	
	        	}*/
	        } else if (Intent.ACTION_INSERT.equals(action)) {
	            // Requested to insert: set that state, and create a new entry
	            // in the container.
	            mState = STATE_INSERT;
	            mUri = getContentResolver().insert(intent.getData(), null);
	            /*
	            intent.setAction(Intent.ACTION_EDIT);
	            intent.setData(mUri);
	            setIntent(intent);
				*/
	            
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
        }

        requestWindowFeature(Window.FEATURE_RIGHT_ICON);
        
        // Set the layout for this activity.  You can find it in res/layout/note_editor.xml
        setContentView(R.layout.note_editor);
        
        // The text view for our note, identified by its ID in the XML file.
        mText = (EditText) findViewById(R.id.note);

		if (mState == STATE_EDIT_NOTE_FROM_SDCARD) {
			// We add a text watcher, so that the title can be updated
			// to indicate a small "*" if modified.
			mText.addTextChangedListener(mTextWatcherSdCard);
		}

        if (mState != STATE_EDIT_NOTE_FROM_SDCARD) {
	        // Get the note!
	        mCursor = managedQuery(mUri, PROJECTION, null, null, null);
        } else {
        	mCursor = null;
        }
    }

	private TextWatcher mTextWatcherSdCard = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			//if (debug) Log.d(TAG, "after");
			mFileContent = s.toString();
			updateTitleSdCard();
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			//if (debug) Log.d(TAG, "before");
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			//if (debug) Log.d(TAG, "on");
		}
    	
    };
    
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
        if (debug) Log.d(TAG, "onResume");
        
        // Set auto-link on or off, based on the current setting.
        int autoLink = PreferenceActivity.getAutoLinkFromPreference(this);
     
        mText.setAutoLinkMask(autoLink);
        

        if (mState == STATE_EDIT || mState == STATE_INSERT) {
        	getNoteFromContentProvider();
        } else if (mState == STATE_EDIT_NOTE_FROM_SDCARD) {
        	getNoteFromFile();
        }
        
        // Make sure that we don't use the link movement method.
        // Instead, we need a blend between the arrow key movement (for regular navigation) and
        // the link movement (so the user can click on links).
        mText.setMovementMethod(new ArrowKeyMovementMethod() {
        	public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        		// This block is copied and pasted from LinkMovementMethod's
        		// onTouchEvent (without the part that actually changes the
        		// selection).
        		int action = event.getAction();

        		if (action == MotionEvent.ACTION_UP) {
        			int x = (int) event.getX();
        			int y = (int) event.getY();

        			x -= widget.getTotalPaddingLeft();
        			y -= widget.getTotalPaddingTop();

        			x += widget.getScrollX();
        			y += widget.getScrollY();

        			Layout layout = widget.getLayout();
        			int line = layout.getLineForVertical(y);
        			int off = layout.getOffsetForHorizontal(line, x);

        			ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

        			if (link.length != 0) {
        				link[0].onClick(widget);
        				return true;
        			}
        		}
        		
        		return super.onTouchEvent(widget, buffer, event);
        	}        	
        }
        );
        

		setTheme(loadTheme());
    }

	private void getNoteFromContentProvider() {
		// If we didn't have any trouble retrieving the data, it is now
        // time to get at the stuff.
        if (mCursor != null 
        		&& mCursor.requery()
        		&& mCursor.moveToFirst()) {

            // Modify our overall title depending on the mode we are running in.
             if (mState == STATE_EDIT) {
                setTitle(getText(R.string.title_edit));
            } else if (mState == STATE_INSERT) {
                setTitle(getText(R.string.title_create));
            }

            long id = mCursor.getLong(COLUMN_INDEX_ID);
            String note = mCursor.getString(COLUMN_INDEX_NOTE);
            long encrypted = mCursor.getLong(COLUMN_INDEX_ENCRYPTED);
			mTheme = mCursor.getString(COLUMN_INDEX_THEME);
            
            if (encrypted == 0) {
            	// Not encrypted

	            // This is a little tricky: we may be resumed after previously being
	            // paused/stopped.  We want to put the new text in the text view,
	            // but leave the user where they were (retain the cursor position
	            // etc).  This version of setText does that for us.
            	if (!note.equals(mText.getText().toString())) {
            		mText.setTextKeepState(note);
            		// keep state does not work, so we have to do it manually:
            		mText.setSelection(mSelectionStart, mSelectionStop);
            	}
            } else {
            	if (mDecryptedText != null) {
            		// Text had already been decrypted, use that:
            		mText.setTextKeepState(mDecryptedText);
            		// keep state does not work, so we have to do it manually:
            		mText.setSelection(mSelectionStart, mSelectionStop);
            		
            		setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, android.R.drawable.ic_lock_idle_lock);
            	} else {
            		// Decrypt note
            		
            		// Overwrite mText because it may contain unencrypted note
            		// from savedInstanceState.
            		//mText.setText(R.string.encrypted);
            		
	        		Intent i = new Intent();
	        		i.setAction(CryptoIntents.ACTION_DECRYPT);
	        		i.putExtra(CryptoIntents.EXTRA_TEXT, note);
	        		i.putExtra(PrivateNotePadIntents.EXTRA_ID, id);
	                
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
	
	private void getNoteFromFile() {
		if (debug) Log.d(TAG, "file: " + mFileContent);

		mText.setTextKeepState(mFileContent);
		// keep state does not work, so we have to do it manually:
		try {
			mText.setSelection(mSelectionStart, mSelectionStop);
		} catch (IndexOutOfBoundsException e) {
			// Then let's not adjust the selection.
		}

        // If we hadn't previously retrieved the original text, do so
        // now.  This allows the user to revert their changes.
        if (mOriginalContent == null) {
            mOriginalContent = mFileContent;
        }
        
		updateTitleSdCard();
	}
	
	private void updateTitleSdCard() {
        String modified = "";
        if (mOriginalContent != null && !mOriginalContent.equals(mFileContent)) {
        	modified = "* ";
        }
        String filename = FileUriUtils.getFilename(mUri);
        setTitle(modified + filename);
        //setTitle(getString(R.string.title_edit_file, modified + filename));
		//setFeatureDrawableResource(Window.FEATURE_RIGHT_ICON, android.R.drawable.ic_menu_save);
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	if (debug) Log.d(TAG, "onSaveInstanceState");
    	if (debug) Log.d(TAG, "file content: " + mFileContent);
    	
        // Save away the original text, so we still have it if the activity
        // needs to be killed while paused.
    	mSelectionStart = mText.getSelectionStart();
    	mSelectionStop = mText.getSelectionEnd();
    	mFileContent = mText.getText().toString();
    	
        outState.putString(BUNDLE_ORIGINAL_CONTENT, mOriginalContent);
        outState.putInt(BUNDLE_STATE, mState);
        outState.putString(BUNDLE_URI, mUri.toString());
        outState.putInt(BUNDLE_SELECTION_START, mSelectionStart);
        outState.putInt(BUNDLE_SELECTION_STOP, mSelectionStop);
        outState.putString(BUNDLE_FILE_CONTENT, mFileContent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (debug) Log.d(TAG, "onPause");

        mText.setAutoLinkMask(0);
        
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
                            String oldText = "";
                            Cursor cursor = getContentResolver().query(mUri, new String[]{"note"}, null, null, null);
                            if ( cursor.moveToFirst() ) {
                                oldText = cursor.getString(0);
                            }
	                    // Bump the modification time to now.
                            if ( ! oldText.equals(text) ) {
	                    values.put(Notes.MODIFIED_DATE, System.currentTimeMillis());
                            }
	                    
	                    String title = ExtractTitle.extractTitle(text);
		                values.put(Notes.TITLE, title);
	                }
	
	                // Write our text back into the provider.
	                values.put(Notes.NOTE, text);

	    			values.put(Notes.THEME, mTheme);
	    			
	                // Commit all of our changes to persistent storage. When the update completes
	                // the content provider will notify the cursor of the change, which will
	                // cause the UI to be updated.
	                getContentResolver().update(mUri, values, null, null);
	                
	            }
            } else {
            	// encrypted note: First encrypt and store encrypted note:

            	// Save current theme:
                ContentValues values = new ContentValues();
    			values.put(Notes.THEME, mTheme);
                getContentResolver().update(mUri, values, null, null);
            	
	            if (mDecryptedText != null) {
	            	// Decrypted had been decrypted.
	            	// We take the current version from 'text' and encrypt it.
	            	
	            	encryptNote(false);
	            	
	            	// Remove displayed note.
	            	//mText.setText(R.string.encrypted);
	            }
            }
        }
    }

	/**
	 * Encrypt the current note.
	 * @param text
	 */
	private void encryptNote(boolean encryptTags) {
        String text = mText.getText().toString();
        String title = ExtractTitle.extractTitle(text);
        String tags = getTags();
		//Log.i(TAG, "encrypt tags: " + tags);
		
		if (!encryptTags) {
			tags = null;
		}
        
		Intent i = new Intent(this, EncryptActivity.class);
		i.putExtra(PrivateNotePadIntents.EXTRA_ACTION, CryptoIntents.ACTION_ENCRYPT);
		i.putExtra(CryptoIntents.EXTRA_TEXT_ARRAY, EncryptActivity.getCryptoStringArray(text, title, tags));
		i.putExtra(PrivateNotePadIntents.EXTRA_URI, mUri.toString());
		startActivity(i);
	}
	
	/**
	 * Unencrypt the current note.
	 * @param text
	 */
	private void unencryptNote() {
        String text = mText.getText().toString();
        String title = ExtractTitle.extractTitle(text);
        String tags = getTags();
		//Log.i(TAG, "unencrypt tags: " + tags);
        
        ContentValues values = new ContentValues();
        values.put(Notes.MODIFIED_DATE, System.currentTimeMillis());
        values.put(Notes.TITLE, title);
        values.put(Notes.NOTE, text);
       	values.put(Notes.ENCRYPTED, 0);
        
        getContentResolver().update(mUri, values, null, null);
        mCursor.requery();
        
		setFeatureDrawable(Window.FEATURE_RIGHT_ICON, null);
		
		// Small trick: Tags have not been converted properly yet. Let's do it now:
		Intent i = new Intent(this, EncryptActivity.class);
		i.putExtra(PrivateNotePadIntents.EXTRA_ACTION, CryptoIntents.ACTION_DECRYPT);
		i.putExtra(CryptoIntents.EXTRA_TEXT_ARRAY, EncryptActivity.getCryptoStringArray(null, null, tags));
		i.putExtra(PrivateNotePadIntents.EXTRA_URI, mUri.toString());
		startActivity(i);
	}
	
	private String getTags() {
		String tags = mCursor.getString(COLUMN_INDEX_TAGS);
		
		if (!TextUtils.isEmpty(tags)) {
			return tags;
		} else {
			return "";
		}
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
                .setShortcut('1', 'e')
                .setIcon(android.R.drawable.ic_lock_lock); // TODO: better icon

        menu.add(1, MENU_UNENCRYPT, 0, R.string.menu_undo_encryption)
        	.setShortcut('1', 'e')
        	.setIcon(android.R.drawable.ic_lock_lock); // TODO: better icon
        
        menu.add(1, MENU_DELETE, 0, R.string.menu_delete)
            .setShortcut('9', 'd')
            .setIcon(android.R.drawable.ic_menu_delete);
        
        menu.add(2, MENU_IMPORT, 0, R.string.menu_import)
        	.setShortcut('1', 'i')
        	.setIcon(android.R.drawable.ic_menu_add);
        
        menu.add(2, MENU_SAVE, 0, R.string.menu_save)
			.setShortcut('2', 's')
			.setIcon(android.R.drawable.ic_menu_save);
        
        menu.add(2, MENU_SAVE_AS, 0, R.string.menu_save_as)
			.setShortcut('3', 'a')
			.setIcon(android.R.drawable.ic_menu_save);

		menu.add(3, MENU_THEME, 0, R.string.menu_theme).setIcon(
				android.R.drawable.ic_menu_manage).setShortcut('4', 't');

		menu.add(3, MENU_SETTINGS, 0, R.string.settings).setIcon(
				android.R.drawable.ic_menu_preferences).setShortcut('9', 's');

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
        	// We use mUri instead of getIntent().getData() in the
        	// following line, because mUri may have changed when inserting
        	// a new note.
            Intent intent = new Intent(null, mUri);
            intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
            //menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
            //        new ComponentName(this, NoteEditor.class), null, intent, 0, null);
            
            // Workaround to add icons:
            MenuIntentOptionsWithIcons menu2 = new MenuIntentOptionsWithIcons(this, menu);
            menu2.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                            new ComponentName(this, NoteEditor.class), null, intent, 0, null);
            
            // Add menu items for category CATEGORY_TEXT_SELECTION_ALTERNATIVE
            intent = new Intent(); // Don't pass data for this intent
            intent.addCategory(NotepadIntents.CATEGORY_TEXT_SELECTION_ALTERNATIVE);
            intent.setType("text/plain");
            // Workaround to add icons:
            menu2.addIntentOptions(GROUP_ID_TEXT_SELECTION_ALTERNATIVE, 0, 0,
                            new ComponentName(this, NoteEditor.class), null, intent, 0, null);
            
        }

        return true;
    }

    @Override
	public boolean onPrepareOptionsMenu(Menu menu) {

    	// Show "revert" menu item only if content has changed.
    	boolean contentChanged = !mOriginalContent.equals(mText.getText().toString());
    	
    	long encrypted = 0;
    	if (mCursor != null && mCursor.moveToFirst()) {
	    	encrypted = mCursor.getLong(COLUMN_INDEX_ENCRYPTED);
    	}
    	boolean isNoteUnencrypted = (encrypted == 0);
    	
    	
    	// Show comands on the URI only if the note is not encrypted
    	menu.setGroupVisible(Menu.CATEGORY_ALTERNATIVE, isNoteUnencrypted);
    	
    	if (mState == STATE_EDIT_NOTE_FROM_SDCARD) {
    		// Menus for editing from SD card
        	menu.setGroupVisible(0, false);
    		menu.setGroupVisible(1, false);
    		menu.setGroupVisible(2, true);
    		menu.findItem(MENU_SAVE).setEnabled(contentChanged);
    	} else {
    		// Menus for internal notes
        	menu.setGroupVisible(0, contentChanged);
    		menu.setGroupVisible(1, true);
    		menu.setGroupVisible(2, false);
    		
        	menu.findItem(MENU_ENCRYPT).setVisible(isNoteUnencrypted);
        	menu.findItem(MENU_UNENCRYPT).setVisible(!isNoteUnencrypted);
    	}
    	
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
        	encryptNote(true);
        	break;
        case MENU_UNENCRYPT:
        	unencryptNote();
        	break;
        case MENU_IMPORT:
        	importNote();
        	break;
        case MENU_SAVE:
        	saveNote();
        	break;
        case MENU_SAVE_AS:
        	saveAsNote();
        	break;
		case MENU_THEME:
			setThemeSettings();
			return true;
		case MENU_SETTINGS:
			showNotesListSettings();
			return true;
        }
        if (item.getGroupId() == GROUP_ID_TEXT_SELECTION_ALTERNATIVE) {
        	// Process manually:
        	// We pass the current selection along with the intent
        	startTextSelectionActivity(item.getIntent());
        	
        	// Consume event
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }

	/**
	 * Modifies an activity to pass along the currently selected text.
	 * @param intent
	 */
	private void startTextSelectionActivity(Intent intent) {
		Intent newIntent = new Intent(intent);
		
		String text = mText.getText().toString();
		int start = mText.getSelectionStart();
		int end = mText.getSelectionEnd();
		
		//if (debug) Log.i(TAG, "len: " + text.length() + ", start: " + start + ", end: " + end);
		if (end < start) {
			int swap = end;
			end = start;
			start = swap;
		}
		
		newIntent.putExtra(NotepadIntents.EXTRA_TEXT, text.substring(start, end));
		newIntent.putExtra(NotepadIntents.EXTRA_TEXT_BEFORE_SELECTION, text.substring(0, start));
		newIntent.putExtra(NotepadIntents.EXTRA_TEXT_AFTER_SELECTION, text.substring(end));
		
		startActivityForResult(newIntent, REQUEST_CODE_TEXT_SELECTION_ALTERNATIVE);
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

        		mText.setAutoLinkMask(0);
            	mText.setTextKeepState(mOriginalContent);
            	int autolink = PreferenceActivity.getAutoLinkFromPreference(this);
    	        mText.setAutoLinkMask(autolink);
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

    /**
     * Insert textToInsert at current position.
     * Optionally, if textBefore or textAfter are non-null,
     * replace the text before or after the current selection.
     * 
     * @author isaac
     * @author Peli
     */
    private void insertAtPoint (String textBefore, String textToInsert, String textAfter) {
    	String originalText = mText.getText().toString(); 
        int startPos = mText.getSelectionStart(); 
        int endPos = mText.getSelectionEnd();
        int newStartPos = startPos;
        int newEndPos = endPos;
        ContentValues values = new ContentValues(); 
        String newNote = "";
        StringBuffer sb = new StringBuffer();
        if (textBefore != null) {
        	sb.append(textBefore);
        	newStartPos = textBefore.length();
        } else {
        	sb.append(originalText.substring(0, startPos));
        }
        if (textToInsert != null) {
        	sb.append(textToInsert);
        	newEndPos = newStartPos + textToInsert.length();
        } else {
        	String text = originalText.substring(startPos, endPos);
        	sb.append(text);
        	newEndPos = newStartPos + text.length();
        }
        if (textAfter != null) {
        	sb.append(textAfter);
        } else {
        	sb.append(originalText.substring(endPos));
        }
        newNote = sb.toString();
        
        if (mState == STATE_EDIT_NOTE_FROM_SDCARD) {
        	mFileContent = newNote;
        	mSelectionStart = newStartPos;
        	mSelectionStop = newEndPos;
        } else {
	        // This stuff is only done when working with a full-fledged note. 
	        if (!mNoteOnly) { 
	            // Bump the modification time to now. 
	            values.put(Notes.MODIFIED_DATE, System.currentTimeMillis()); 
	            String title = ExtractTitle.extractTitle(newNote); 
	            values.put(Notes.TITLE, title); 
	        } 
	        // Write our text back into the provider.
	        values.put(Notes.NOTE, newNote);
	        // Commit all of our changes to persistent storage. When the update completes 
	        // the content provider will notify the cursor of the change, which will 
	        // cause the UI to be updated. 
	        getContentResolver().update(mUri, values, null, null);
        }
        
        //ijones: notification doesn't seem to trigger for some reason :( 
        mText.setTextKeepState(newNote);
        // Adjust cursor position according to new length:
        mText.setSelection(newStartPos, newEndPos);
    } 
    
    private void importNote() {
    	// Load the file into a new note.
    	
    	mFileContent = mText.getText().toString();
    	
    	Uri newUri = null;
    	
    	// Let's check whether the exactly same note already exists or not:
    	Cursor c = getContentResolver().query(Notes.CONTENT_URI, 
    			new String[] {Notes._ID},
    			Notes.NOTE + " = ?", new String[] {mFileContent}, null);
    	if (c != null && c.moveToFirst()) {
    		// Same note exists already:
    		long id = c.getLong(0);
    		newUri = ContentUris.withAppendedId(Notes.CONTENT_URI, id);
    	} else {
        	
        	// Add new note
        	// Requested to insert: set that state, and create a new entry
            // in the container.
            //mState = STATE_INSERT;
            ContentValues values = new ContentValues();
            values.put(Notes.NOTE, mFileContent);
            values.put(Notes.THEME, mTheme);
            newUri = getContentResolver().insert(Notes.CONTENT_URI, values);
            

            // If we were unable to create a new note, then just finish
            // this activity.  A RESULT_CANCELED will be sent back to the
            // original activity if they requested a result.
            if (newUri == null) {
                Log.e(TAG, "Failed to insert new note.");
                finish();
                return;
            }

            // The new entry was created, so assume all will end well and
            // set the result to be returned.
            //setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));
            //setResult(RESULT_OK, intent);
    	}
    	
        
        // Start a new editor:
    	Intent intent = new Intent();
    	intent.setAction(Intent.ACTION_EDIT);
        intent.setData(newUri);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        setIntent(intent);
        startActivity(intent);
        
        // and finish this editor
        finish();
        
    }
    
    private void saveNote() {
    	mFileContent = mText.getText().toString();
    	
    	File file = FileUriUtils.getFile(mUri);
    	SaveFileActivity.writeToFile(this, file, mFileContent);
    	
    	mOriginalContent = mFileContent;
    }
    
    /**
     * Show the "Save as" dialog.
     */
    private void saveAsNote() {
    	mFileContent = mText.getText().toString();
    	
    	Intent intent = new Intent();
    	intent.setAction(NotepadInternalIntents.ACTION_SAVE_TO_SD_CARD);
    	intent.setData(mUri);
    	intent.putExtra(NotepadInternalIntents.EXTRA_TEXT, mFileContent);
    	
    	startActivityForResult(intent, REQUEST_CODE_SAVE_AS);
    }

	void setThemeSettings() {
		showDialog(DIALOG_THEME);
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_UNSAVED_CHANGES:
			return getUnsavedChangesWarningDialog();
			
		case DIALOG_THEME:
			return new ThemeDialog(this, this);
		}
		return null;
	}

	@Override
	public String onLoadTheme() {
		return loadTheme();
	}

	@Override
	public void onSaveTheme(String theme) {
		saveTheme(theme);
	}

	@Override
	public void onSetTheme(String theme) {
		setTheme(theme);
	}

	/**
	 * Loads the theme settings for the currently selected theme.
	 * 
	 * Up to version 1.2.1, only one of 3 hardcoded themes are available. These are stored
	 * in 'skin_background' as '1', '2', or '3'.
	 * 
	 * Starting in 1.2.2, also themes of other packages are allowed.
	 * 
	 * @return
	 */
	public String loadTheme() {
		return mTheme;
		/*
		if (mCursor != null && mCursor.moveToFirst()) {
			// mCursorListFilter has been set to correct position
			// by calling getSelectedListId(),
			// so we can read out further elements:
			String skinBackground = mCursor
					.getString(COLUMN_INDEX_THEME);
	
			return skinBackground;
		} else {
			return null;
		}
		*/
	}

	public void saveTheme(String theme) {
		mTheme = theme;
		/*
		// Save theme only for content Uris with NotePad authority.
		// Don't save anything for file:// uri.
		if (mUri != null && mUri.getAuthority().equals(NotePad.AUTHORITY)) {
			ContentValues values = new ContentValues();
			values.put(Notes.THEME, theme);
			getContentResolver().update(mUri, values, null, null);
		}
		*/
	}

	/**
	 * Set theme according to Id.
	 * 
	 * @param themeId
	 */
	void setTheme(String themeName) {
		int size = PreferenceActivity.getFontSizeFromPrefs(this);

		// New styles:
		boolean themeFound = setRemoteStyle(themeName, size);
		
		if (!themeFound) {
			// Some error occured, let's use default style:
			setLocalStyle(R.style.Theme_Notepad, size);
		}
		
		applyTheme();
	}

	private void setLocalStyle(int styleResId, int size) {
		String styleName = getResources().getResourceName(styleResId);
		
		boolean themefound = setRemoteStyle(styleName, size);
		
		if (!themefound) {
			// Actually this should never happen.
			Log.e(TAG, "Local theme not found: " + styleName);
		}
	}
	
	private boolean setRemoteStyle(String styleName, int size) {
		if (TextUtils.isEmpty(styleName)) {
			Log.e(TAG, "Empty style name: " + styleName);
			return false;
		}
		
		PackageManager pm = getPackageManager();
		
		String packageName = ThemeUtils.getPackageNameFromStyle(styleName);
		
		if (packageName == null) {
			Log.e(TAG, "Invalid style name: " + styleName);
			return false;
		}
		
		Context c = null;
		try {
			c = createPackageContext(packageName, 0);
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Package for style not found: " + packageName + ", " + styleName);
			return false;
		}
		
		Resources res = c.getResources();
		
		int themeid = res.getIdentifier(styleName, null, null);
		
		if (themeid == 0) {
			Log.e(TAG, "Theme name not found: " + styleName);
			return false;
		}
		
		int[] attr = ThemeUtils.getAttributeIds(c, ThemeNotepad.ThemeNotepadAttributes, packageName);
		
		TypedArray a = c.obtainStyledAttributes(themeid, attr);
		
		mTextTypeface = a.getString(ThemeNotepad.ID_textTypeface);
		mCurrentTypeface = null;

		// Look for special cases:
		if ("monospace".equals(mTextTypeface)) {
			mCurrentTypeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL);
		} else if ("sans".equals(mTextTypeface)) {
			mCurrentTypeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL);
		} else if ("serif".equals(mTextTypeface)) {
			mCurrentTypeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL);
		} else if (!TextUtils.isEmpty(mTextTypeface)) {

			try {
				if (debug) Log.d(TAG, "Reading typeface: package: " + packageName + ", typeface: " + mTextTypeface);
				Resources remoteRes = pm.getResourcesForApplication(packageName);
				mCurrentTypeface = Typeface.createFromAsset(remoteRes.getAssets(),
						mTextTypeface);
				if (debug) Log.d(TAG, "Result: " + mCurrentTypeface);
			} catch (NameNotFoundException e) {
				Log.e(TAG, "Package not found for Typeface", e);
			}
		}
		
		mTextUpperCaseFont = a.getBoolean(ThemeNotepad.ID_textUpperCaseFont, false);
		
		mTextColor = a.getColor(ThemeNotepad.ID_textColor,
				android.R.color.white);
		
		if (size == 1) {
			mTextSize = a
					.getInt(ThemeNotepad.ID_textSizeSmall, 10);
		} else if (size == 2) {
			mTextSize = a.getInt(ThemeNotepad.ID_textSizeMedium,
					20);
		} else {
			mTextSize = a
					.getInt(ThemeNotepad.ID_textSizeLarge, 30);
		}

		if (mText != null) {
			mBackgroundPadding = a.getInteger(ThemeNotepad.ID_backgroundPadding, -1);
			if (mBackgroundPadding >=0){
				mText.setPadding(mBackgroundPadding, mBackgroundPadding, mBackgroundPadding, mBackgroundPadding);
			} else {
				// 9-patches do the padding automatically
				// todo clear padding 
			}
			try {
				Resources remoteRes = pm.getResourcesForApplication(packageName);
				int resid = a.getResourceId(ThemeNotepad.ID_background, 0);
				if (resid != 0) {
					Drawable d = remoteRes.getDrawable(resid);
					mText.setBackgroundDrawable(d);
				} else {
					// remove background
					mText.setBackgroundResource(0);
				}
			} catch (NameNotFoundException e) {
				Log.e(TAG, "Package not found for Theme background.", e);
			} catch (Resources.NotFoundException e) {
				Log.e(TAG, "Resource not found for Theme background.", e);
			}
		}

		int divider = a.getInteger(ThemeNotepad.ID_divider, 0);
		
		a.recycle();
		/*
		Drawable div = null;
		if (divider > 0) {
			div = getResources().getDrawable(divider);
		} else if (divider < 0) {
			div = null;
		} else {
			div = mDefaultDivider;
		}
		
		setDivider(div);
		*/
		
		return true;
	}
	
	private void applyTheme() {
		mText.setTextSize(mTextSize);
		mText.setTypeface(mCurrentTypeface);
		mText.setTextColor(mTextColor);

		if (mTextUpperCaseFont) {
			// Turn off autolinkmask, because it is not compatible with transformationmethod.
	        mText.setAutoLinkMask(0);
	        
			mText.setTransformationMethod(UpperCaseTransformationMethod.getInstance());
		} else {
			mText.setTransformationMethod(null);
			
	        // Set auto-link on or off, based on the current setting.
	        int autoLink = PreferenceActivity.getAutoLinkFromPreference(this);
	     
	        mText.setAutoLinkMask(autoLink);
		}
		
		mText.invalidate();
	}

	private void showNotesListSettings() {
		startActivity(new Intent(this, PreferenceActivity.class));
	}
	
	Dialog getUnsavedChangesWarningDialog() {
		return new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(R.string.warning_unsaved_changes_title)
		.setMessage(R.string.warning_unsaved_changes_message)
		.setPositiveButton(R.string.button_save,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						// Save
						saveNote();
						finish();
					}
				})
		.setNeutralButton(R.string.button_dont_save,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						// Don't save
						finish();
					}
				})
		.setNegativeButton(android.R.string.cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int whichButton) {
						// Cancel
					}
				})
		.create();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mState == STATE_EDIT_NOTE_FROM_SDCARD) {
				mFileContent = mText.getText().toString();
				if (! mFileContent.equals(mOriginalContent)) {
					// Show a dialog
					showDialog(DIALOG_UNSAVED_CHANGES);
					return true;
				}
			}
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
    	if (debug) Log.d(TAG, "onActivityResult: Received requestCode " + requestCode + ", resultCode " + resultCode);
    	switch(requestCode) {
    	case REQUEST_CODE_DECRYPT:
    		if (resultCode == RESULT_OK && data != null) {
    			String decryptedText = data.getStringExtra (CryptoIntents.EXTRA_TEXT);
    			long id = data.getLongExtra(PrivateNotePadIntents.EXTRA_ID, -1);
    			
    			// TODO: Check that id corresponds to current intent.
    			
    			if (id == -1) {
        	    	Log.e(TAG, "Wrong extra id");
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
    	case REQUEST_CODE_TEXT_SELECTION_ALTERNATIVE:
    		if (resultCode == RESULT_OK && data != null) {
    			// Insert result at current cursor position:
    			String text = data.getStringExtra(NotepadIntents.EXTRA_TEXT);
    			String textBefore = data.getStringExtra(NotepadIntents.EXTRA_TEXT_BEFORE_SELECTION);
    			String textAfter = data.getStringExtra(NotepadIntents.EXTRA_TEXT_AFTER_SELECTION);
    			
    			insertAtPoint(textBefore, text, textAfter);
    		}
    		break;
    	case REQUEST_CODE_SAVE_AS:
    		if (resultCode == RESULT_OK && data != null) {
    			// Set the new file name
    			mUri = data.getData();
    			if (debug) Log.d(TAG, "original: " + mOriginalContent + ", file: " + mFileContent);
    			mOriginalContent = mFileContent;
    			
    			updateTitleSdCard();
    		}
    	}
    }
}
