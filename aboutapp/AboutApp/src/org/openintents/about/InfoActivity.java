package org.openintents.about;

import android.content.Intent;


public class InfoActivity extends org.openintents.distribution.InfoActivity {

    @Override
    public void init() {

        mApplications = new int[] {
                R.string.info_app_filemanager, // OI File Manager
                R.string.info_app_shopping, // OI Shopping List
                R.string.info_app_notepad, // OI Notepad
                R.string.info_app_safe // OI Safe
        };

        mPackageNames = new String[] {
        		"org.openintents.filemanager", // OI File Manager
        		"org.openintents.shopping", // OI Shopping List
        		"org.openintents.notepad", // OI Notepad
        		"org.openintents.safe" // OI Safe
        };
        
        mMinVersionCodes = new int[] {
        		5, // OI File Manager
        		10004, // OI Shopping List
        		10052, // OI Notepad
        		4 // OI Safe
        };
        
        mMinVersionName = new String[] {
        		"1.1.0", // OI File Manager
        		"1.0.3", // OI Shopping List
        		"1.1.0", // OI Notepad
        		"1.0.0" // OI Safe
        };
        
        mInfoText = new int[] {
        		R.string.info_instructions, // OI File Manager
        		R.string.info_instructions, // OI Shopping List
        		R.string.info_instructions, // OI Notepad
        		R.string.info_instructions // OI Safe
        };

        mDeveloperUris = new String[] {
        		"http://www.openintents.org/en/filemanager", // OI File Manager
        		"http://www.openintents.org/en/shoppinglist", // OI Shopping List
        		"http://www.openintents.org/en/notepad", // OI Notepad
        		"http://www.openintents.org/en/safe" // OI Safe
        };

        mIntentAction = new String[] {
        		Intent.ACTION_MAIN, // OI File Manager
        		Intent.ACTION_VIEW, // OI Shopping List
        		Intent.ACTION_VIEW, // OI Notepad
        		Intent.ACTION_MAIN // OI Safe
        };

        mIntentData = new String[] {
        		"org.openintents.filemanager.FileManagerActivity", // OI File Manager
        		"content://org.openintents.shopping/items", // OI Shopping List
        		"content://org.openintents.notepad/notes", // OI Notepad
        		"org.openintents.safe.FrontDoor" // OI Safe
        };
    }
}
