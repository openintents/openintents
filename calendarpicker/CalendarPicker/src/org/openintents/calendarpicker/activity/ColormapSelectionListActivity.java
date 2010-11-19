/*
 * Copyright (C) 2010 Karl Ostmo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openintents.calendarpicker.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openintents.calendarpicker.R;
import org.openintents.calendarpicker.activity.prefs.CalendarDisplayPreferences;
import org.openintents.calendarpicker.view.ColormapView;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;


public class ColormapSelectionListActivity extends ListActivity {

	static final String TAG = "ColormapSelectionListActivity";
	
	public static final int[][] COLOR_LISTS = new int[][] {
			new int[] {Color.BLACK, Color.RED},
			new int[] {Color.BLUE, Color.YELLOW, Color.MAGENTA},
			new int[] {Color.CYAN, Color.MAGENTA},
			new int[] {Color.BLACK, Color.RED, Color.YELLOW, Color.WHITE},
			new int[] {Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA},
			new int[] {Color.GREEN, Color.WHITE, Color.MAGENTA},
	};
	
	public static final String[] COLORLIST_LABELS = new String[] {
			"Red",
			"Cotton Candy",
			"Cool",
			"Hot",
			"Rainbow",
			"Watermelon"
	};
	
	public static final String LABEL = "LABEL";
	public static final String COLORLIST = "COLORLIST";
	
	String[] keymap = new String[] {LABEL, COLORLIST};
	
    // ========================================================================
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        
        List<Map<String, ?>> list = new ArrayList<Map<String, ?>>();
        for (int i=0; i< COLOR_LISTS.length; i++) {
            Map<String, Object> stuff = new HashMap<String, Object>();
        	stuff.put(LABEL, COLORLIST_LABELS[i]);
        	stuff.put(COLORLIST, COLOR_LISTS[i]);
        	list.add(stuff);
        }
        
        
        SimpleAdapter sa = new SimpleAdapter(this, list, R.layout.list_item_colormap, keymap, new int[] {android.R.id.text1, R.id.colormap_view});
        sa.setViewBinder(new ViewBinder() {

			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				if (data instanceof int[]) {
					ColormapView colormap_view = (ColormapView) view;
					colormap_view.setColors((int[]) data);
					return true;
				}
				return false;
			}
        });
        setListAdapter(sa);
    }

    // ========================================================================    
    @Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
    	Intent result = new Intent();
    	result.putExtra(CalendarDisplayPreferences.INTENT_EXTRA_COLORMAP_INDEX, position);
    	this.setResult(Activity.RESULT_OK, result);
    	finish();
    }
}

