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

package org.openintents.countdown;

import org.openintents.countdown.db.Countdown.Durations;
import org.openintents.countdown.util.CountdownUtils;
import org.openintents.countdown.widget.DurationPicker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A generic activity for editing a note in a database.  This can be used
 * either to simply view a note {@link Intent#ACTION_VIEW}, view and edit a note
 * {@link Intent#ACTION_EDIT}, or create a new note {@link Intent#ACTION_INSERT}.  
 */
public class CountdownEditorActivity extends Activity {
    private static final String TAG = "Notes";
    
    // This is our state data that is stored when freezing.
    private static final String ORIGINAL_CONTENT = "origContent";

    // Identifiers for our menu items.
    private static final int REVERT_ID = Menu.FIRST;
    private static final int DISCARD_ID = Menu.FIRST + 1;
    private static final int MENU_DELETE = Menu.FIRST + 2;
    private static final int MENU_PICK_RINGTONE = Menu.FIRST + 3;

    // The different distinct states the activity can be run in.
    private static final int STATE_EDIT = 0;
    private static final int STATE_INSERT = 1;

    private static final int MSG_UPDATE_DISPLAY = 1;
    
    private static final int REQUEST_CODE_RINGTONE = 1;
    
    private int mState;
    private Uri mUri;
    private Cursor mCursor;
    private EditText mText;
    private String mOriginalContent;
    
	private long mDuration;
	private long mDeadline;
    
    private DurationPicker mDurationPicker;
    private Button mStart;
    private Button mStop;
    private Button mModify;
    private Button mCont;
    private TextView mCountdownView;
    
    private CheckBox mRingtoneView;
    private CheckBox mVibrateView;
    
    private long mRing;
    private Uri mRingtoneUri;
    private long mVibrate;
    private int mRingtoneType;
    
    private long UNCHECKED = 0;
    private long CHECKED = 1;
    
    // private boolean mStartCountdown;
    
    private int mCountdownState;
    private static final int STATE_COUNTDOWN_IDLE = 1;
    private static final int STATE_COUNTDOWN_RUNNING = 2;
    private static final int STATE_COUNTDOWN_MODIFY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        mCountdownState = STATE_COUNTDOWN_IDLE;
        mRingtoneType = RingtoneManager.TYPE_ALL;
        
        // Do some setup based on the action being performed.

        final String action = intent.getAction();
        if (Intent.ACTION_EDIT.equals(action) || Intent.ACTION_VIEW.equals(action)) {
            // Requested to edit: set that state, and the data being edited.
            mState = STATE_EDIT;
            mUri = intent.getData();
            
            // If we have been called by the notification, cancel it:
            cancelNotification(mUri);
            
        } else if (Intent.ACTION_INSERT.equals(action)) {
            // Requested to insert: set that state, and create a new entry
            // in the container.
            mState = STATE_INSERT;
            
            // Prepare default values
            ContentValues cv = new ContentValues();
            cv.put(Durations.RING, CHECKED);
            cv.put(Durations.RINGTONE, RingtoneManager.getDefaultUri(mRingtoneType).toString());
            cv.put(Durations.VIBRATE, CHECKED);
            
            mUri = getContentResolver().insert(intent.getData(), cv);
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
            setResult(RESULT_OK, (new Intent()).setAction(mUri.toString()));

        } else {
            // Whoops, unknown action!  Bail.
            Log.e(TAG, "Unknown action, exiting");
            finish();
            return;
        }

        // Set the layout for this activity.  You can find it in res/layout/note_editor.xml
        setContentView(R.layout.countdown_editor);
        
        // The text view for our note, identified by its ID in the XML file.
        mText = (EditText) findViewById(R.id.title);
        
        mDurationPicker = (DurationPicker) findViewById(R.id.durationpicker);
        
        // Reset to 0 to show 2-digit numbers.
        mDurationPicker.setCurrentMinute(0);
        mDurationPicker.setCurrentSecond(0);
        
        mCountdownView = (TextView) findViewById(R.id.countdown);

        mStart = (Button) findViewById(R.id.start);
        mStart.setOnClickListener(new View.OnClickListener() {

			
			public void onClick(View arg0) {
				start();
			}
        	
        });
        
        mStop = (Button) findViewById(R.id.stop);
        mStop.setOnClickListener(new View.OnClickListener() {

			
			public void onClick(View arg0) {
				stop();
			}
        	
        });

        mModify = (Button) findViewById(R.id.modify);
        mModify.setOnClickListener(new View.OnClickListener() {

			
			public void onClick(View arg0) {
				modify();
			}
        	
        });

        mCont = (Button) findViewById(R.id.cont);
        mCont.setOnClickListener(new View.OnClickListener() {

			
			public void onClick(View arg0) {
				cont();
			}
        	
        });
        
        mRingtoneView = (CheckBox) findViewById(R.id.ringtone);
        
        mRingtoneView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			
			public void onCheckedChanged(CompoundButton view, boolean checked) {
				setRing(checked);
			}
        	
        });
        
        mVibrateView = (CheckBox) findViewById(R.id.vibrate);

        mVibrateView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			
			public void onCheckedChanged(CompoundButton view, boolean checked) {
				setVibrate(checked);
			}

        	
        });
        
        // Get the countdown!
        mCursor = managedQuery(mUri, Durations.PROJECTION, null, null, null);

        // If an instance of this activity had previously stopped, we can
        // get the original text it started with.
        if (savedInstanceState != null) {
            mOriginalContent = savedInstanceState.getString(ORIGINAL_CONTENT);
        }
        
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If we didn't have any trouble retrieving the data, it is now
        // time to get at the stuff.
        if (mCursor != null) {
            // Make sure we are at the one and only row in the cursor.
            mCursor.moveToFirst();

            // Modify our overall title depending on the mode we are running in.
            if (mState == STATE_EDIT) {
                setTitle(getText(R.string.title_edit));
            } else if (mState == STATE_INSERT) {
                setTitle(getText(R.string.title_create));
            }

            // This is a little tricky: we may be resumed after previously being
            // paused/stopped.  We want to put the new text in the text view,
            // but leave the user where they were (retain the cursor position
            // etc).  This version of setText does that for us.
            String title = mCursor.getString(mCursor.getColumnIndexOrThrow(Durations.TITLE));
            mText.setTextKeepState(title);
            
            // If we hadn't previously retrieved the original text, do so
            // now.  This allows the user to revert their changes.
            if (mOriginalContent == null) {
                mOriginalContent = title;
            }
            
            mDuration = mCursor.getLong(mCursor.getColumnIndexOrThrow(Durations.DURATION));
            
            mDurationPicker.setDuration(mDuration);
            

            mDeadline = mCursor.getLong(mCursor.getColumnIndexOrThrow(Durations.DEADLINE_DATE));
            

            mRing = mCursor.getLong(mCursor.getColumnIndexOrThrow(Durations.RING));
            //Log.i(TAG, "onResume Ring: " + mRing);

            String uristring = mCursor.getString(mCursor.getColumnIndexOrThrow(Durations.RINGTONE));
            //Log.i(TAG, "onResume Ringtone: " + uristring);
            if (uristring != null) {
            	mRingtoneUri = Uri.parse(uristring);
            } else {
            	mRingtoneUri = null;
            }
            
            mVibrate = mCursor.getLong(mCursor.getColumnIndexOrThrow(Durations.VIBRATE));

            mDeadline = mCursor.getLong(mCursor.getColumnIndexOrThrow(Durations.DEADLINE_DATE));
            
            updateCheckboxes();
            updateViews();
    		
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
    }

    @Override
    protected void onPause() {
        super.onPause();

        // The user is going somewhere else, so make sure their current
        // changes are safely saved away in the provider.  We don't need
        // to do this if only editing.
        if (mCursor != null) {
            
            ContentValues values = new ContentValues();

            long now = System.currentTimeMillis();
            
            // Bump the modification time to now.
            values.put(Durations.MODIFIED_DATE, now);

            String text = mText.getText().toString();
            values.put(Durations.TITLE, text);
            

    		// Set the current time.
            mDuration = mDurationPicker.getDuration();
    		values.put(Durations.DURATION, mDuration);
    		
    		//if (mStartCountdown) {
        		values.put(Durations.DEADLINE_DATE, mDeadline);
        		
    		//}
        		
        	values.put(Durations.RING, mRing);
        	Log.i(TAG, "Ring: " + mRing);
        	
        	String uristring = null;
        	if (mRingtoneUri != null) {
        		uristring = mRingtoneUri.toString();
        	}
        	values.put(Durations.RINGTONE, uristring);
        	Log.i(TAG, "Ringtone: " + uristring);
        	
        	values.put(Durations.VIBRATE, mVibrate);
        	Log.i(TAG, "Vibrate: " + mVibrate);
    		
            // Commit all of our changes to persistent storage. When the update completes
            // the content provider will notify the cursor of the change, which will
            // cause the UI to be updated.
            getContentResolver().update(mUri, values, null, null);
        }
        
        // Cancel notifications
        mHandler.removeMessages(MSG_UPDATE_DISPLAY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, MENU_PICK_RINGTONE, 0, R.string.menu_pick_ringtone)
                .setShortcut('1', 'd')
                .setIcon(android.R.drawable.ic_menu_manage);
        
        // Build the menus that are shown when editing.
        if (mState == STATE_EDIT) {
            menu.add(0, MENU_DELETE, 0, R.string.menu_delete)
                    .setShortcut('1', 'd')
                    .setIcon(android.R.drawable.ic_menu_delete);
        

        // Build the menus that are shown when inserting.
        } else {
            menu.add(0, MENU_DELETE, 0, R.string.menu_delete)
                    .setShortcut('0', 'd')
                    .setIcon(android.R.drawable.ic_menu_delete);
        }

        // If we are working on a full note, then append to the
        // menu items for any other activities that can do stuff with it
        // as well.  This does a query on the system for any activities that
        // implement the ALTERNATIVE_ACTION for our data, adding a menu item
        // for each one that is found.
        Intent intent = new Intent(null, getIntent().getData());
        intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
        menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
                new ComponentName(this, CountdownEditorActivity.class), null, intent, 0, null);
    
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle all of the possible menu actions.
        switch (item.getItemId()) {
        case MENU_DELETE:
            deleteNote();
            finish();
            break;
        case DISCARD_ID:
            cancelNote();
            break;
        case REVERT_ID:
            cancelNote();
            break;
        case MENU_PICK_RINGTONE:
        	pickRingtone();
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
            if (mState == STATE_EDIT) {
                // Put the original note text back into the database
                mCursor.close();
                mCursor = null;
                ContentValues values = new ContentValues();
                //values.put(Durations.NOTE, mOriginalContent);
                getContentResolver().update(mUri, values, null, null);
            } else if (mState == STATE_INSERT) {
                // We inserted an empty note, make sure to delete it
                deleteNote();
            }
        }
        setResult(RESULT_CANCELED);
        finish();
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
    
    private final void start() {
    	mCountdownState = STATE_COUNTDOWN_RUNNING;
		
    	long now = System.currentTimeMillis();
        mDuration = mDurationPicker.getDuration();
		
    	mDeadline = now + mDuration;
    	//finish();

    	setAlarm(mDeadline);
    	
    	updateViews();
    }

    private final void stop() {
    	mCountdownState = STATE_COUNTDOWN_IDLE;
		
    	//mStartCountdown = false;
    	
    	//long now = System.currentTimeMillis();
        //mDuration = mDurationPicker.getDuration();
		
    	mDeadline = 0;
    	//finish();

    	cancelAlarm();
    	mHandler.removeMessages(MSG_UPDATE_DISPLAY);
    	
    	updateViews();
    }

    /**
     * Modify the current time.
     * Note that the countdown continues to run in the background.
     * One can see this by pressing the "back" button, then entering again.
     */
    private final void modify() {
    	mCountdownState = STATE_COUNTDOWN_MODIFY;
		
    	// Set current time to modify timer temporarily
    	long now = System.currentTimeMillis();
        
    	long temporaryDuration = mDeadline - now;
    	
    	if (temporaryDuration < 0) {
    		temporaryDuration = 0;
    	}
    	
    	mOriginalDuration = mDuration;
    	
    	mDurationPicker.setDuration(temporaryDuration);
    	
    	updateViews();
    }
    
    long mOriginalDuration;

    private final void cont() {
    	mCountdownState = STATE_COUNTDOWN_RUNNING;
    	
    	long now = System.currentTimeMillis();
        mDuration = mDurationPicker.getDuration();
		
    	mDeadline = now + mDuration;
    	//finish();
    	
    	// Set original duration
    	mDuration = mOriginalDuration;
    	mDurationPicker.setDuration(mDuration);

    	cancelAlarm();
    	mHandler.removeMessages(MSG_UPDATE_DISPLAY);
    	
    	setAlarm(mDeadline);
    	updateViews();
    }
    
    public void setAlarm(long time) {
    	CountdownUtils.setAlarm(this, mUri, time);
    }
    
    public void cancelAlarm() {
    	CountdownUtils.cancelAlarm(this, mUri);
    }
    
    /**
     * Cancel notification (if it exists).
     * @param uri
     */
    public void cancelNotification(Uri uri) {

        int notification_id = Integer.parseInt(uri.getLastPathSegment());
        
        // look up the notification manager service
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // cancel the notification that we started in IncomingMessage
        nm.cancel(notification_id);
    }
    
    
    public void updateViews() {
    	updateCountdown();
		
		updateButtons();
		
		//updateCheckboxes();
    }

	/**
	 * 
	 */
	private void updateCountdown() {
		long now = System.currentTimeMillis();
		
		long delta = mDeadline - now;
		
		//mDurationView.setText("" + CountdownUtils.getDurationString(mDuration));
		
		if (mCountdownState == STATE_COUNTDOWN_MODIFY) {
			mDurationPicker.setVisibility(View.VISIBLE);
			mCountdownView.setVisibility(View.INVISIBLE);
		} else if (delta > 0) {
			//mDurationView.setText("");
			mCountdownState = STATE_COUNTDOWN_RUNNING;
			mDurationPicker.setVisibility(View.INVISIBLE);
			mCountdownView.setVisibility(View.VISIBLE);
			mCountdownView.setText("" + CountdownUtils.getDurationString(delta));
			mCountdownView.setTextAppearance(this, android.R.style.TextAppearance_Large);
			mCountdownView.setTextSize(64);

    		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_DISPLAY), 1000);
    		
    		if (delta < 2000) {
    			// Save the text as the notification may go off soon:
    	        ContentValues values = new ContentValues();
    	    	values.put(Durations.TITLE, mText.getText().toString());
    	    	
    	        getContentResolver().update(mUri, values, null, null);
    	        mCursor.requery();
    		}
		} else if (delta > -3000) {
			mCountdownState = STATE_COUNTDOWN_RUNNING;
			mDurationPicker.setVisibility(View.INVISIBLE);
			mCountdownView.setVisibility(View.VISIBLE);
			mCountdownView.setText("" + CountdownUtils.getDurationString(0));
			mCountdownView.setTextColor(0xffff0000);

    		mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_UPDATE_DISPLAY), 1000);
		} else {
			mCountdownState = STATE_COUNTDOWN_IDLE;
			mDurationPicker.setVisibility(View.VISIBLE);
			mCountdownView.setVisibility(View.INVISIBLE);
		}
	}
    
    private void updateButtons() {
    	switch (mCountdownState) {
    	case STATE_COUNTDOWN_IDLE:
    		mStart.setVisibility(View.VISIBLE);
    		mStop.setVisibility(View.GONE);
    		mModify.setVisibility(View.GONE);
    		mCont.setVisibility(View.GONE);
    		break;
    	case STATE_COUNTDOWN_RUNNING:
    		mStart.setVisibility(View.GONE);
    		mStop.setVisibility(View.VISIBLE);
    		mModify.setVisibility(View.VISIBLE);
    		mCont.setVisibility(View.GONE);
    		break;
    	case STATE_COUNTDOWN_MODIFY:
    		mStart.setVisibility(View.GONE);
    		mStop.setVisibility(View.GONE);
    		mModify.setVisibility(View.GONE);
    		mCont.setVisibility(View.VISIBLE);
    		break;
    	}
    }

    private void updateCheckboxes() {
    	mRingtoneView.setChecked(mRing == CHECKED);
    	
    	Ringtone ring = RingtoneManager.getRingtone(this, mRingtoneUri);
    	String ringname = ring.getTitle(this);
    	String s = getString(R.string.ringtone, ringname);
    	mRingtoneView.setText(s);
    	
    	mVibrateView.setChecked(mVibrate == CHECKED);
    	
    }
    
    private void setRing(boolean checked) {
    	if (checked) {
			mRing = CHECKED;
		} else {
			mRing = UNCHECKED;
		}

        ContentValues values = new ContentValues();
    	values.put(Durations.RING, mRing);
    	
        getContentResolver().update(mUri, values, null, null);
        mCursor.requery();
    }
    

	/**
	 * @param checked
	 */
	private void setVibrate(boolean checked) {
		if (checked) {
			mVibrate = CHECKED;
		} else {
			mVibrate = UNCHECKED;
		}

        ContentValues values = new ContentValues();
    	values.put(Durations.VIBRATE, mVibrate);
    	
        getContentResolver().update(mUri, values, null, null);
        mCursor.requery();
	}
    
    private void pickRingtone() {
		Intent i = new Intent();
		i.setAction(RingtoneManager.ACTION_RINGTONE_PICKER);
		
		i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, mRingtoneUri);
		i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, false);
		i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, mRingtoneType);
		
		startActivityForResult(i, REQUEST_CODE_RINGTONE);
    }
    
	/** Handle the process of updating the timer */
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == MSG_UPDATE_DISPLAY) {
				// Update
				updateViews();
	            
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		

		if (requestCode == REQUEST_CODE_RINGTONE
				&& resultCode == RESULT_OK) {
			Bundle bundle = data.getExtras();
			mRingtoneUri = (Uri) bundle.get(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
			Log.i(TAG, "New ringtone: " + mRingtoneUri);
			
            ContentValues values = new ContentValues();

        	values.put(Durations.RING, CHECKED);
        	
        	String uristring = null;
        	if (mRingtoneUri != null) {
        		uristring = mRingtoneUri.toString();
        	}
        	values.put(Durations.RINGTONE, uristring);
    		
        	//Log.i(TAG, "Uri: " + mUri.toString());
        	
            // Commit all of our changes to persistent storage. When the update completes
            // the content provider will notify the cursor of the change, which will
            // cause the UI to be updated.
            getContentResolver().update(mUri, values, null, null);
            
            mCursor.requery();
		}
	}
}
