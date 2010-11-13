package org.openintents.calendarpicker.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.adapter.EventListAdapter;
import org.openintents.calendarpicker.contract.IntentConstants;

import android.app.Activity;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
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
import android.widget.AdapterView.OnItemClickListener;


public abstract class AbstractEventsListActivity extends ListActivity {

	static final String TAG = "AbstractEventsListActivity"; 

	public static final String KEY_ROWID = BaseColumns._ID;
	public static final String KEY_EVENT_TIMESTAMP = IntentConstants.CalendarEventPicker.COLUMN_EVENT_TIMESTAMP;
	public static final String KEY_EVENT_TITLE = IntentConstants.CalendarEventPicker.COLUMN_EVENT_TITLE;

	// ========================================================================
    abstract Cursor requery();

    // ========================================================================
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

//    	registerForContextMenu( category_listview );
    	

		if (savedInstanceState != null) {
//			autocomplete_textview.setText( savedInstanceState.getString("search_text") );
		}
		
		
        final StateRetainer a = (StateRetainer) getLastNonConfigurationInstance();
        if (a != null) {
        	sorting_order = a.sorting_order;
        } else {
        	
        }
    }

    // ========================================================================
    OnItemClickListener category_choice_listener = new OnItemClickListener() {

		public void onItemClick(AdapterView<?> adapter_view, View arg1, int position, long id) {
			
			Cursor cursor = (Cursor) ((CursorAdapter) adapter_view.getAdapter()).getItem(position);

			Intent i = new Intent();
			i.putExtra(BaseColumns._ID, id);
			i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_DATETIME, getIntent().getLongExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_DATETIME, 0));

			int epoch_column = cursor.getColumnIndex(IntentConstants.CalendarEventPicker.COLUMN_EVENT_TIMESTAMP);
			long event_epoch = cursor.getLong(epoch_column);
			i.putExtra(IntentConstants.CalendarDatePicker.INTENT_EXTRA_EPOCH, event_epoch);

	        setResult(Activity.RESULT_OK, i);
			finish();
		}
    };    

    // ========================================================================
    class StateRetainer {
    	Cursor cursor;
    	Stack<SortCriteria> sorting_order;
    }

    // ========================================================================
    @Override
    public Object onRetainNonConfigurationInstance() {
    	
    	StateRetainer state = new StateRetainer();
    	state.cursor = ((CursorAdapter) getListAdapter()).getCursor();
    	state.sorting_order = sorting_order;
        return state;
    }

    // ========================================================================
    @Override
    protected void onSaveInstanceState(Bundle out_bundle) {
    	Log.i(TAG, "onSaveInstanceState");

    }

    // ========================================================================
    @Override
    protected void onRestoreInstanceState(Bundle in_bundle) {
    	Log.i(TAG, "onRestoreInstanceState");
    	
    }

    // ========================================================================
    @Override
    protected Dialog onCreateDialog(int id) {
    	
        switch (id) {
 
        }
        
        return null;
    }

    // ========================================================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_event_list, menu);

        return true;
    }

    // ========================================================================
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        return true;
    }

    // ========================================================================
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

    // ========================================================================
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != Activity.RESULT_CANCELED) {
	  	   	switch (requestCode) {

	   		default:
		    	break;
		   }
		}
    }
    

    // ========================================================================
    // NOTE: The criteria are read from right-to-left in the queue; Highest priority is
    // on top of the stack.
    Stack<SortCriteria> sorting_order = new Stack<SortCriteria>();
    enum SortCriteria {
    	ALPHA, DATE
    }
    String[] sort_column_names = {
		KEY_EVENT_TITLE,
		KEY_EVENT_TIMESTAMP
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

