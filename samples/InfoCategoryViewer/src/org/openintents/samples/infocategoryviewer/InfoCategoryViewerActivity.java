package org.openintents.samples.infocategoryviewer;

import org.openintents.compatibility.activitypicker.DialogHostingActivity;
import org.openintents.util.SDKVersion;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class InfoCategoryViewerActivity extends Activity {
	private static final String TAG = "InfoCategoryViewerActivity";
	private static final boolean debug = true;
	
	final static int REQUEST_PICK_LOCALE_SETTING = 1;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button b = (Button) findViewById(R.id.button);
        b.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				launchInfoActivity();
			}
        	
        });
    }

	/**
     * Show PICK_ACTIVITY dialog with all possible Locale settings 
     */
    void launchInfoActivity() {

    	// Does not exist yet in SDK 1.1
    	String CATEGORY_INFO = "android.intent.category.INFO";
    	
    	Intent launchIntent = new Intent(Intent.ACTION_MAIN);
    	launchIntent.addCategory(CATEGORY_INFO);
    	
		Intent pickIntent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        pickIntent.putExtra(Intent.EXTRA_INTENT, launchIntent);
        pickIntent.putExtra(Intent.EXTRA_TITLE,
                getText(R.string.title_select_activity));

        if (SDKVersion.SDKVersion < 3) {
        	if (debug) Log.i(TAG, "Compatibility mode for ActivityPicker");
            // SDK 1.1 backward compatibility:
            // We launch our own version of ActivityPicker:
            pickIntent.setClass(this, DialogHostingActivity.class);
            pickIntent.putExtra(DialogHostingActivity.EXTRA_DIALOG_ID, 
            			DialogHostingActivity.DIALOG_ID_ACTIVITY_PICKER);
        } else {
        	if (debug) Log.i(TAG, "Call system ActivityPicker");
        }
        
		Log.i(TAG, "Start activity for result 2");
        startActivityForResult(pickIntent, REQUEST_PICK_LOCALE_SETTING);
    }
    

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		//Log.i(TAG, "onActivityResult: " + requestCode + ", " + resultCode);
		//Log.i(TAG, "data: " + data.toURI());

		if (resultCode == RESULT_OK) {
			switch(requestCode) {
			case REQUEST_PICK_LOCALE_SETTING:
				startActivity(data);
				break;
			}
		}
	}
}