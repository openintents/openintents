package org.openintents.distribution.demo;

import android.content.Intent;

public class InfoActivity extends org.openintents.distribution.InfoActivity {

    @Override
    public void init() {

        mApplications = new int[] {
                R.string.info_app_notepad, // OI Notepad
                R.string.info_app_shopping, // OI Shopping List
                R.string.info_app_shopping_upcoming // OI Shopping List
        };

        mPackageNames = new String[] {
        		"org.openintents.notepad", // OI Notepad
        		"org.openintents.shopping", // OI Shopping List
        		"org.openintents.shopping" // OI Shopping List
        };
        
        mMinVersionCodes = new int[] {
        		10059, // OI Notepad
        		10013, // OI Shopping List
        		99999 // OI Shopping List
        };
        
        mMinVersionName = new String[] {
        		"1.2.0", // OI Notepad
        		"1.2.1", // OI Shopping List
        		"9.9.9" // OI Shopping List
        };
        
        mInfoText = new int[] {
        		R.string.info_text_notepad, // OI Notepad
        		R.string.info_text_shopping, // OI Shopping List
        		R.string.info_text_shopping // OI Shopping List
        };

        mDeveloperUris = new String[] {
        		"http://www.openintents.org/en/notepad", // OI Notepad
        		"http://www.openintents.org/en/shoppinglist", // OI Shopping List
        		"http://www.openintents.org/en/shoppinglist" // OI Shopping List
        };

        mIntentAction = new String[] {
        		Intent.ACTION_VIEW, // OI Notepad
        		Intent.ACTION_VIEW, // OI Shopping List
        		Intent.ACTION_VIEW // OI Shopping List
        };

        mIntentData = new String[] {
        		"content://org.openintents.notepad/notes", // OI Notepad
        		"content://org.openintents.shopping/items", // OI Shopping List
        		"content://org.openintents.shopping/items" // OI Shopping List
        };
    }
    
}
