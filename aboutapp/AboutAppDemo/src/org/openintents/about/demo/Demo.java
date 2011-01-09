package org.openintents.about.demo;

import org.openintents.intents.AboutIntents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Demo extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button button = (Button) findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				showAbout();
			}
        });
        
        button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				showAboutWithExtras();
			}
        });

        button = (Button) findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View arg0) {
				showAboutForOtherPackage();
			}
        });
    }
    
    void showAbout() {

		Intent intent = new Intent(AboutIntents.ACTION_SHOW_ABOUT_DIALOG);

		// Additional information (application name, version, ...)
		// are retrieved from the Manifest.
		// You can set additional fields in tags.
		
		// Start about activity. Needs to be "forResult" with requestCode>=0
		// because the About dialog may call elements from your Manifest by your
		// package name.
		startActivityForResult(intent, 0);
    }
    
    void showAboutWithExtras() {
    	
    	// Specify intent extras if you want to override the information
    	// specified in the Manifest.
    	
    	// Code put into separate class for clarity:
    	ShowAboutWithExtras.showAboutWithExtras(this);
    }
    
    void showAboutForOtherPackage() {

		Intent intent = new Intent(AboutIntents.ACTION_SHOW_ABOUT_DIALOG);

		// Show About screen for a different package:
		intent.putExtra(AboutIntents.EXTRA_PACKAGE_NAME, "com.android.contacts");
		intent.putExtra(AboutIntents.EXTRA_PACKAGE_NAME, "org.openintents.notepad");
		
		startActivity(intent);
    }
}