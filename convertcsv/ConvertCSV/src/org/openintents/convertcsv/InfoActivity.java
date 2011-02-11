package org.openintents.convertcsv;

import android.content.Intent;


public class InfoActivity extends org.openintents.distribution.InfoActivity {

    @Override
    public void init() {

        mApplications = new int[] {
                R.string.info_app_shopping, // OI Shopping List
                R.string.info_app_notepad, // OI Notepad
        };

        mPackageNames = new String[] {
        		"org.openintents.shopping", // OI Shopping List
        		"org.openintents.notepad", // OI Notepad
        };
        
        mMinVersionCodes = new int[] {
        		10004, // OI Shopping List
        		10052, // OI Notepad
        };
        
        mMinVersionName = new String[] {
        		"1.0.3", // OI Shopping List
        		"1.1.0", // OI Notepad
        };
        
        mInfoText = new int[] {
        		R.string.info_instructions, // OI Shopping List
        		R.string.info_instructions, // OI Notepad
        };

        mDeveloperUris = new String[] {
        		"http://www.openintents.org/en/shoppinglist", // OI Shopping List
        		"http://www.openintents.org/en/notepad", // OI Notepad
        };

        mIntentAction = new String[] {
        		Intent.ACTION_VIEW, // OI Shopping List
        		Intent.ACTION_VIEW, // OI Notepad
        };

        mIntentData = new String[] {
        		"content://org.openintents.shopping/items", // OI Shopping List
        		"content://org.openintents.notepad/notes", // OI Notepad
        };
    }
}
