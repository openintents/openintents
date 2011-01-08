/* 
 * Copyright (C) 2007-2009 OpenIntents.org
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

package org.openintents.countdown.activity;

import org.openintents.countdown.db.Countdown.Durations;
import org.openintents.intents.CountdownIntents2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

/**
 * @deprecated Should be moved in separate apk.
 * @author Peli
 *
 */
public class SetCountdownActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

	    final Intent intent = getIntent();
	
	    final String action = intent.getAction();
	    
	    if ((CountdownIntents2.ACTION_SET_COUNTDOWN.equals(action)
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
	        	
//	        	newIntent.putExtra(AutomationIntents.EXTRA_ACTIVITY_INTENT, automationIntent);
	        	
	        	startActivity(newIntent);
	        }
	        
	        if (c != null) {
	        	c.close();
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
