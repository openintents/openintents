package org.openintents.extensions.insertdate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.openintents.intents.NotepadIntents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class InsertDateActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        
        // Obtain extra
        Intent intent = getIntent();
        if (intent != null) {
        	String text = intent.getStringExtra(NotepadIntents.EXTRA_TEXT);
        	//String textBeforeSelection = intent.getStringExtra(NotepadIntents.EXTRA_TEXT_BEFORE_SELECTION);
        	//String textAfterSelection = intent.getStringExtra(NotepadIntents.EXTRA_TEXT_AFTER_SELECTION);
        	
        	// In this example, we don't really use the text.

            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            text = dateFormat.format(date);
        	
        	intent.putExtra(NotepadIntents.EXTRA_TEXT, text);
        	setResult(RESULT_OK, intent);
        }
        
        finish();
    }
}