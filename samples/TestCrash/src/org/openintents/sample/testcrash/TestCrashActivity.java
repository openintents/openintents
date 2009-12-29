package org.openintents.sample.testcrash;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * This application is a test application for OI Logcat.
 * 
 * Its sole purpose is to crash after pressing a button.
 * 
 * @author Peli
 *
 */
public class TestCrashActivity extends Activity {
	Button b;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        b = (Button) findViewById(R.id.Button01);
        
        b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Start an empty intent, which will crash this activity:
				Intent i = new Intent();
				startActivity(i);
			}
		});
    }
    
    
}