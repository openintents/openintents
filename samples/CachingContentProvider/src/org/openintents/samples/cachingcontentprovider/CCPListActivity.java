package org.openintents.samples.cachingcontentprovider;
/*
<!-- 
 * Copyright (C) 2010 OpenIntents UG
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
 --> 
 */

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListAdapter;
import android.widget.SimpleCursorAdapter;

public class CCPListActivity extends ListActivity {
    
	Cursor mCursor;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Put a managed wrapper around the retrieved cursor so we don't have to worry about
        // requerying or closing it as the activity changes state.
        mCursor = this.getContentResolver().query(Customers.CONTENT_URI, null, null, null, null);
        startManagingCursor(mCursor);

        // Now create a new list adapter bound to the cursor.
        // SimpleListAdapter is designed for binding to a Cursor.
        ListAdapter adapter = new SimpleCursorAdapter(
                this, // Context.
                R.layout.three_row_layout,  // Specify the row template to use (here, 3 columns bound to the 3 retrieved rows).
                mCursor,                                              // Pass in the cursor to bind to.
                new String[] {Customers.NAME,Customers.EMAIL,Customers._ID},           // Array of cursor columns to bind to.
                new int[] {R.id.text1, R.id.text2,R.id.text3});  // Parallel array of which template objects to bind to those columns.

        // Bind to our new adapter.
        setListAdapter(adapter);
    }
    
        
}