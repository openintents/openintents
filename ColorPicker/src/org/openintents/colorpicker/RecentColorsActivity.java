/* 
 * Copyright (C) 2010 OpenIntents.org
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

package org.openintents.colorpicker;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.openintents.intents.ColorPickerIntents;
import org.openintents.widget.SwatchAdapter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

public class RecentColorsActivity extends Activity {

	public static final int MAX_REMEMBERED_COLORS = 12;

	static final String PREFKEY_RECENT_COLORS = "PREFKEY_RECENT_COLORS";
	private static final String COLORS_PREFERENCE_DELIMITER = ",";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setBackgroundDrawableResource(R.drawable.background_semi_transparent);
        setContentView(R.layout.recentcolors);
        
	    final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
	    
    
	    
	    final GridView grid_view = (GridView) findViewById(android.R.id.list);
	    grid_view.setAdapter( new SwatchAdapter(this, deserializeColors(settings)) );
	    grid_view.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int position, long id) {
				int color = ((SwatchAdapter) arg0.getAdapter()).getItem(position);
				Intent intent = new Intent();
				intent.putExtra(ColorPickerIntents.EXTRA_COLOR, color);
				setResult(RESULT_OK, intent);
				finish();
			}
	    });
	    
	    // Set the "empty view"
	    View emptyView = findViewById(R.id.empty_gridview_text);
	    grid_view.setEmptyView(emptyView);
	    
	    findViewById(R.id.button_clear_history).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				settings.edit().putString(PREFKEY_RECENT_COLORS, "").commit();
				finish();
			}
	    });
	}
	
	private static List<Integer> deserializeColors(SharedPreferences settings) {

	    String colors_delimited = settings.getString(PREFKEY_RECENT_COLORS, "");
	    String[] color_int_strings = TextUtils.split(colors_delimited, COLORS_PREFERENCE_DELIMITER);
	    List<Integer> color_ints = new ArrayList<Integer>(color_int_strings.length);
	    
	    for (String int_string : color_int_strings)
	    	color_ints.add( Integer.parseInt(int_string) );
	    
	    return color_ints;
	}
	
	public static void addRecentColor(SharedPreferences settings, int color) {
		
		Set<Integer> used_colors = new LinkedHashSet<Integer>();
		used_colors.add(color);	// Put the newly-selected color at the front of the list
		
		// The LinkedHashSet de-duplicates elements while preserving the order
		used_colors.addAll( deserializeColors(settings) );
	    
	    String serialized_colors = TextUtils.join(COLORS_PREFERENCE_DELIMITER, new ArrayList<Integer>(used_colors).subList(0, Math.min(used_colors.size(), MAX_REMEMBERED_COLORS)));
	    settings.edit().putString(PREFKEY_RECENT_COLORS, serialized_colors).commit();
	}
}