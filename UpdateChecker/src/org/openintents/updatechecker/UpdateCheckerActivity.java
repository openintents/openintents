package org.openintents.updatechecker;

import android.app.Activity;
import android.os.Bundle;

public class UpdateCheckerActivity extends Activity {
    private static final String TAG = "UpdateChecker";
	public static final String EXTRA_LATEST_VERSION = "latest_version";
	public static final String EXTRA_COMMENT = "comment";


	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Update.check(this);
    }
    
    
}