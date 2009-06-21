package org.openintents.countdown.activity;

import org.openintents.countdown.db.Countdown.Durations;
import org.openintents.intents.AutomationIntents;
import org.openintents.intents.CountdownIntents;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

public class SetCountdownActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    final Intent intent = getIntent();
	
	    final String action = intent.getAction();
	    
	    if ((CountdownIntents.ACTION_SET_COUNTDOWN.equals(action)
	    		// menu.addIntentOptions() does not provide the action in the intent returned,
	    		// only the data.
	    		|| action == null) && intent.getData() != null) {
	    	// Set countdown for the specified URI.
	    	// First check whether a countdown with this URI already exists:
	    	ContentResolver cr = getContentResolver();
	        Cursor c = cr.query(Durations.CONTENT_URI, 
	        		new String[] {Durations._ID, Durations.AUTOMATE_INTENT }, 
					Durations.AUTOMATE_INTENT + " like ?", 
					new String[] {intent.getData().toString() + "%"}, null);
	        if (c != null && c.moveToFirst()) {
	        	// At least one countdown has the same action.
	        	// TODO: Ask if there are several countdowns with same URI.
	        	// Now simply return the first
	        	
	        	long id = c.getLong(0);
	        	
	        	Uri uri = Uri.withAppendedPath(Durations.CONTENT_URI, "" + id);
	        	
	        	// Start with exiting intent, to pass all other extras
	        	Intent newIntent = new Intent(intent);
	        	newIntent.setAction(Intent.ACTION_EDIT);
	        	newIntent.setData(uri);
	        	newIntent.setComponent(null); // clear component from original intent
	        	removeCategories(newIntent);
	        		        	
	        	startActivity(newIntent);
	        	
	        } else {
	        	// create a new Countdown:
	        	
	        	// Start with exiting intent, to pass all other extras
	        	Intent newIntent = new Intent(intent);
	        	newIntent.setAction(Intent.ACTION_INSERT);
	        	newIntent.setData(Durations.CONTENT_URI);
	        	newIntent.setComponent(null); // clear component from original intent
	        	removeCategories(newIntent);
	        	
	        	// Automation intent:
	        	Intent automationIntent = new Intent(Intent.ACTION_VIEW);
	        	automationIntent.setData(intent.getData());
	        	
	        	newIntent.putExtra(AutomationIntents.EXTRA_ACTIVITY_INTENT, automationIntent);
	        	
	        	startActivity(newIntent);
	        }
	    }
	    
	    finish();
    }
    
    /**
     * Remove all categories from intent.
     * @param intent
     */
    void removeCategories(Intent intent) {
    	for (String category : intent.getCategories()) {
    		intent.removeCategory(category);
    	}
    }
}
