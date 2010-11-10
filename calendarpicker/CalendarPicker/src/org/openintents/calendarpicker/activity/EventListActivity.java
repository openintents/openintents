package org.openintents.calendarpicker.activity;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Stack;

import org.openintents.calendarpicker.IntentConstants;
import org.openintents.calendarpicker.adapter.EventListAdapter;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import org.openintents.calendarpicker.R;


public class EventListActivity extends ListActivity {

	static final String TAG = "EventListActivity"; 

	
	final int DIALOG_RUNONCE_INSTRUCTIONS = 1;
	public static final String PREFKEY_SHOW_EVENT_LIST_INSTRUCTIONS = "PREFKEY_SHOW_EVENT_LIST_INSTRUCTIONS";

    
	
	public static final String KEY_ROWID = BaseColumns._ID;
	public static final String KEY_TIMESTAMP = IntentConstants.CalendarEventPicker.COLUMN_EVENT_TIMESTAMP;
	public static final String KEY_EVENT_TITLE = IntentConstants.CalendarEventPicker.COLUMN_EVENT_TITLE;
	
	
	Cursor requery() {

        Uri intent_data = getIntent().getData();
    	Log.d(TAG, "Querying content provider for: " + intent_data);
    	
        Date d = new Date(getIntent().getLongExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_DATETIME, -1));
        
        
        
        
        Log.e(TAG, "Received date: " + d.getDate());
        long day_begin = d.getTime()/1000;
        long day_end = day_begin + 86400;
        
        
		
		Cursor cursor = managedQuery(intent_data,
				new String[] {
					KEY_ROWID,
//					"strftime('%s', " + KEY_TIMESTAMP + ") AS " + KEY_TIMESTAMP,
					KEY_TIMESTAMP,	// XXX
					KEY_EVENT_TITLE},
				KEY_TIMESTAMP + " >= datetime(?, 'unixepoch') AND " + KEY_TIMESTAMP + " < datetime(?, 'unixepoch')",
				new String[] {Long.toString(day_begin), Long.toString(day_end)}, constructOrderByString());

		String header_text = cursor.getCount() + " event(s) on " + new DateFormatSymbols().getShortMonths()[d.getMonth()] + " " + d.getDate();
		((TextView) findViewById(R.id.list_header)).setText(header_text);
		
		return cursor;
	}
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().requestFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.list_activity_event_list);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON, R.drawable.titlebar_icon);

        // Initialize sort bucket
        for (SortCriteria x : SortCriteria.values()) sorting_order.add(x);

        
    	
    	Cursor cursor = requery();
    	

        setListAdapter(new EventListAdapter(
        		this,
        		R.layout.list_item_event,
        		cursor));

        getListView().setOnItemClickListener(category_choice_listener);
//        category_listview.setEmptyView(findViewById(R.id.empty_categories));

//    	registerForContextMenu( category_listview );
    	
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		if (!settings.getBoolean(PREFKEY_SHOW_EVENT_LIST_INSTRUCTIONS, false)) {
//			showDialog(DIALOG_RUNONCE_INSTRUCTIONS);
		}
		
		if (savedInstanceState != null) {
//			autocomplete_textview.setText( savedInstanceState.getString("search_text") );
		}
		
		
        final StateRetainer a = (StateRetainer) getLastNonConfigurationInstance();
        if (a != null) {
        	sorting_order = a.sorting_order;
        } else {
        	
        }
    }
    // =============================================
    
    OnItemClickListener category_choice_listener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> adapter_view, View arg1, int position, long id) {
			
//			Cursor c = (Cursor) ((CursorAdapter) adapter_view.getAdapter()).getItem(position);
//			int rowid_column = c.getColumnIndex("_id");
//			long rowid = c.getLong(rowid_column);
			
			Intent i = new Intent();
			i.putExtra(IntentConstants.INTENT_EXTRA_CALENDAR_EVENT_ID, id);
			i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_DATETIME, getIntent().getLongExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_DATETIME, 0));
	        setResult(Activity.RESULT_OK, i);
			finish();
		}
    };    
    // =============================================
    
    
    class StateRetainer {
    	Cursor cursor;
    	Stack<SortCriteria> sorting_order;
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
    	
    	StateRetainer state = new StateRetainer();
    	state.cursor = ((CursorAdapter) getListAdapter()).getCursor();
    	state.sorting_order = sorting_order;
        return state;
    }
    
    
    
    
	
    @Override
    protected void onSaveInstanceState(Bundle out_bundle) {
    	Log.i(TAG, "onSaveInstanceState");

    }
    
    @Override
    protected void onRestoreInstanceState(Bundle in_bundle) {
    	Log.i(TAG, "onRestoreInstanceState");
    	
    }
    
    
    
    

    @Override
    protected Dialog onCreateDialog(int id) {
    	
        switch (id) {
 
        }
        
        return null;
    }
    





    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_event_list, menu);

        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_sort_alpha:
        {
        	sortList(SortCriteria.ALPHA);
            return true;
        }
        case R.id.menu_sort_recent:
        {
        	sortList(SortCriteria.DATE);
            return true;
        }
        /*
        case R.id.menu_sort_usage:
        {
        	sortList(SortCriteria.FREQUENCY);
            return true;
        }
        */
        }

        return super.onOptionsItemSelected(item);
    }

    // ========================================================   

    
	
    // ========================================================

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != Activity.RESULT_CANCELED) {
	  	   	switch (requestCode) {

	   		default:
		    	break;
		   }
		}
    }
    
    
    
    
    
    
    // ========================================================
    
    // NOTE: The criteria are read from right-to-left in the queue; Highest priority is
    // on top of the stack.
    Stack<SortCriteria> sorting_order = new Stack<SortCriteria>();
    enum SortCriteria {
    	ALPHA, DATE
    }
    String[] sort_column_names = {
		KEY_EVENT_TITLE,
		KEY_TIMESTAMP
	};
    boolean[] default_ascending = {true, false};
    
    String constructOrderByString() {
    	List<String> sort_pieces = new ArrayList<String>();
    	for (int i=0; i<sorting_order.size(); i++) {
    		int sort_col = sorting_order.get(i).ordinal();
    		sort_pieces.add( sort_column_names[sort_col] + " " + (default_ascending[sort_col] ? "ASC" : "DESC") );
    	}
    	Collections.reverse(sort_pieces);
    	return TextUtils.join(", ", sort_pieces);
    }
    
    
    void sortList(SortCriteria criteria) {
    	sorting_order.remove(criteria);
    	sorting_order.push(criteria);
    	
    	((ResourceCursorAdapter) getListAdapter()).changeCursor( requery() );		
    }
}

