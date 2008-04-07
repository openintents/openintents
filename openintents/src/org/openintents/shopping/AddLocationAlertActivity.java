/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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

package org.openintents.shopping;

import org.openintents.R;
import org.openintents.provider.Shopping;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


/**
 * Allows to edit the share settings for a shopping list.
 */
public class AddLocationAlertActivity extends Activity 
	implements OnClickListener {
	/**
     * TAG for logging.
     */
    private static final String TAG = "AddLocationAlertActivity";
    
    TextView mTags;
    TextView mLocation;
    
    Uri mUri;
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.shopping_add_location_alert);

        // Get the uri of the list
        mUri = getIntent().getData();

        // Set up click handlers for the text field and button
        mTags = (TextView) this.findViewById(R.id.tags);
        mLocation = (TextView) this.findViewById(R.id.location);
        
        Button picklocation = (Button) this.findViewById(R.id.picklocation);
        picklocation.setOnClickListener(this);

        Button addlocationalert = (Button) this.findViewById(R.id.addlocationalert);
        addlocationalert.setOnClickListener(this);

        Button viewalerts = (Button) this.findViewById(R.id.viewalerts);
        viewalerts.setOnClickListener(this);

    }

    public void onClick(final View v)
    {
        switch (v.getId()) {
        case R.id.picklocation:
        	pickLocation();
        	break;
        case R.id.addlocationalert:
        	addLocationAlert();
        	break;
        case R.id.viewalerts:
        	viewAlerts();
        	break;
        default:
        	// Don't know what to do - do nothing.	
        	Log.e(TAG, "AddLocationAlertActivity: Unexpedted view id clicked.");
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();

        // TODO Here we should store temporary information
       	
    }

    public void pickLocation() {
    	
    }
    
    public void addLocationAlert() {
    	
    }
    
    public void viewAlerts() {
    	
    }
    	
}
