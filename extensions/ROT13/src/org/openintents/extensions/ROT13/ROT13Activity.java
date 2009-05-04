package org.openintents.extensions.ROT13;

import org.openintents.intents.NotepadIntents;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

public class ROT13Activity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        
        // Obtain extra
        Intent intent = getIntent();
        if (intent != null) {
        	String text = intent.getStringExtra(NotepadIntents.EXTRA_TEXT);
        	String textBeforeSelection = intent.getStringExtra(NotepadIntents.EXTRA_TEXT_BEFORE_SELECTION);
        	String textAfterSelection = intent.getStringExtra(NotepadIntents.EXTRA_TEXT_AFTER_SELECTION);
        	
        	if (TextUtils.isEmpty(text) && 
        			textBeforeSelection != null && textAfterSelection != null){
        		// Nothing had been selected, so let us select all:
        		text = textBeforeSelection + textAfterSelection;
        		textBeforeSelection = "";
        		textAfterSelection = "";
        	}

        	text = ROT13(text);
        	
        	intent.putExtra(NotepadIntents.EXTRA_TEXT, text);
        	intent.putExtra(NotepadIntents.EXTRA_TEXT_BEFORE_SELECTION, textBeforeSelection);
        	intent.putExtra(NotepadIntents.EXTRA_TEXT_AFTER_SELECTION, textAfterSelection);
        	setResult(RESULT_OK, intent);
        }
        
        finish();
    }
    
    /**
     * ROT13.
     * 
     * Implementation adapted from
     * http://www.cs.princeton.edu/introcs/31datatype/Rot13.java.html
     * 
     * @param text
     * @return
     */
    private String ROT13(String text) {
    	int len = text.length();
    	StringBuffer sb = new StringBuffer(len);
	    for (int i = 0; i < len; i++) {
	        char c = text.charAt(i);
	        if       (c >= 'a' && c <= 'm') c += 13;
	        else if  (c >= 'n' && c <= 'z') c -= 13;
	        else if  (c >= 'A' && c <= 'M') c += 13;
	        else if  (c >= 'A' && c <= 'Z') c -= 13;
	        sb.append(c);
	    }
	    return sb.toString();
	}
}