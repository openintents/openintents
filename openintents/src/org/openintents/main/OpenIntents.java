package org.openintents.main;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.app.ListActivity;

public class OpenIntents extends Activity {

	private String[] activitylist = {
    "Show favorite locations",
    "Lookup geo->label",
    "Lookup label->geo"};

	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        ListView list = (ListView) findViewById(R.id.activities);
        list.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, activitylist));
        
    }

}