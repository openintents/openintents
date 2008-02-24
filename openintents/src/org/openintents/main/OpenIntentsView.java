/* 
 * Copyright (C) 2007-2008 OpenIntents.org
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

package org.openintents.main;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openintents.OpenIntents;
import org.openintents.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.Resources;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.AlignmentSpan;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TabHost.TabSpec;


/**
 * Main activity to start simple activities for ContentProviders.
 * 
 * Currently supported:
 * LocationsProvider
 * TagsProvider
 * 
 *
 */
public class OpenIntentsView extends Activity {

	private TabHost mTabHost;
	
	/*
	GridView mGridMain;
	GridView mGridSettings;
	*/
	
	TableLayout mGridMain;
	TableLayout mGridSettings;
	
	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.main);

		Context context = this;
        // Get the Resources object from our context
        Resources res = context.getResources();
    
		mTabHost = (TabHost)findViewById(R.id.tabhost);
		mTabHost.setup();
		
		TabSpec tabspec = mTabHost.newTabSpec("openintents");
		tabspec.setIndicator(res.getString(R.string.openintents), res.getDrawable(R.drawable.openintents002a_32));
		tabspec.setContent(R.id.content1);
		mTabHost.addTab(tabspec);
		
		tabspec = mTabHost.newTabSpec("settings");
		tabspec.setIndicator(res.getString(R.string.settings), res.getDrawable(R.drawable.settings001a_32));
		tabspec.setContent(R.id.content2);
		mTabHost.addTab(tabspec);
		
		mTabHost.setCurrentTab(0);
		
		// loadApps(); // do this in onresume?

		/*
        mGridMain = (GridView) findViewById(R.id.grid_main);
        mGridMain.setAdapter(new AppsAdapter(this, OpenIntents.MAIN_CATEGORY));
        //mGridMain.setAddsStatesFromChildren(true);
        //mGridMain.
        
        mGridSettings = (GridView) findViewById(R.id.grid_settings);
        mGridSettings.setAdapter(new AppsAdapter(this, OpenIntents.SETTINGS_CATEGORY));
        */
		
		mGridMain = (TableLayout) findViewById(R.id.grid_main);
		mGridSettings = (TableLayout) findViewById(R.id.grid_settings);
		
		
		// fill the list manually:
		fillGrid(mGridMain, OpenIntents.MAIN_CATEGORY);
		fillGrid(mGridSettings, OpenIntents.SETTINGS_CATEGORY);
	}

	void fillGrid(TableLayout table, String category) {
		
		// Get all actions in 'category'
		Intent mainIntent = new Intent(Intent.MAIN_ACTION, null);
        mainIntent.addCategory(category);

        List<ResolveInfo> apps = getPackageManager().queryIntentActivities(mainIntent, 0);
        
        // Sort the list alphabetically
        
        final Comparator<ResolveInfo> RESOLVEINFO_ORDER =
            new Comparator<ResolveInfo>() {
				public int compare(ResolveInfo o1, ResolveInfo o2) {
					String s1 = o1.activityInfo.loadLabel(getPackageManager()).toString();
					String s2 = o2.activityInfo.loadLabel(getPackageManager()).toString();
					return s1.compareTo(s2);
			}
		};

        Collections.sort(apps, RESOLVEINFO_ORDER);
        
        // Put them into the table layout
        int max = apps.size();
        int row = 0;
        int col = 0;
        int pos = 0;
        int colmax = 3;
        TableRow rowview = new TableRow(this);

        while (pos < max) {
        	LinearLayout ll = getCustomButton(apps, pos);
        	if (col == 0) {
        		rowview.addView(ll, new TableRow.LayoutParams(1));
        	} else {
        		rowview.addView(ll, new TableRow.LayoutParams());
        	}
        	col++;
        	if (col >= colmax) {
        		col = 0;
        		row++;
        		table.addView(rowview, new TableLayout.LayoutParams());
        		rowview = new TableRow(this);
        	};
        	pos++;
        }
        if (col > 0) {
        	table.addView(rowview, new TableLayout.LayoutParams());
        }
        /*
        for (int i=0; i<max; i++) {
        	
            // Add the button to the linearlayout:
            grid.addView(ll);
        }
        */
	}
	
	private LinearLayout getCustomButton(List<ResolveInfo> apps, int pos) {
		ResolveInfo info = apps.get(pos);

    	// Add image:
    	ImageView iv = new ImageView(this);
        iv.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
        iv.setScaleType(ImageView.ScaleType.FIT_END);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
        		48,
        		48);
        lp.topMargin = 10;
        iv.setLayoutParams(lp);
        
        
        // Add label:
        TextView tv = new TextView(this);
        lp = new LinearLayout.LayoutParams(
        		80,
        		35);
        lp.bottomMargin = 6;
        tv.setLayoutParams(lp);
        
        tv.setText(info.activityInfo.loadLabel(getPackageManager()));
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(15);
        
        // First convert text to 'spannable'
		tv.setText(tv.getText(), TextView.BufferType.SPANNABLE);
		Spannable str = (Spannable) tv.getText();
		
		// Align center
     	str.setSpan(
				new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 
				0, tv.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
        
        // Add both to a custom button:
        LinearLayout ll = new LinearLayout(this);
        lp = new LinearLayout.LayoutParams(
        		LinearLayout.LayoutParams.WRAP_CONTENT,
        		LinearLayout.LayoutParams.WRAP_CONTENT);
        ll.setLayoutParams(lp);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.addView(iv);
        ll.addView(tv);
        ll.setBackground(android.R.drawable.button_background);
        ll.setFocusable(true);
        ll.setClickable(true);
        ll.setGravity(Gravity.CENTER);
        
        // Make the button clickable:
        ll.setOnClickListener(new myOnClickListener(info));
        
		return ll;
	}
	
	class myOnClickListener implements OnClickListener {
		ResolveInfo mInfo;
		
		myOnClickListener(ResolveInfo info) {
			mInfo = info;
		}
		
		
		public void onClick(View arg0) {
			Intent intent = new Intent(
					Intent.MAIN_ACTION, 
					null);
			intent.setClassName(
					mInfo.activityInfo.packageName, 
					mInfo.activityInfo.name);
			startActivity(intent);
		}
	}
	
	/*
	public void onItemClick(AdapterView adapterView, View view, int position,
			long id) {
		switch (position) {
		case 0:
			Intent intent = new Intent(this, LocationsView.class);
			startActivity(intent);
			break;
			
		case 1:
			intent = new Intent(this, TagsView.class);
			startActivity(intent);
			break;
			
		case 2:
			intent = new Intent(this, ShoppingView.class);
			startActivity(intent);
			break;
			
		case 3:
			intent = new Intent(this, SensorSimulatorView.class);
			startActivity(intent);
			break;
		
		case 4:
			intent = new Intent(this, ContentBrowserView.class);
			startActivity(intent);
			break;
			
		case 5:
			intent = new Intent(this, MagnoliaSettings.class);
			startActivity(intent);
			break;
			
		case 6:
			intent = new Intent(this, MagnoliaTagging.class);
			startActivity(intent);
			break;
			
		case 7:
			intent = new Intent(OpenIntentsView.this, About.class);
			startActivity(intent);
			break;
			
		default:
			throw new IllegalArgumentException("Unknown position " + position);
		}

	}
	*/
	/*
	private OnClickListener mAboutListener = new OnClickListener() {
		public void onClick(View v) {
			Intent intent = new Intent(OpenIntents.this, About.class);
			startActivity(intent);
	    }
	};
	*/

/*
    
    
    public class AppsAdapter extends BaseAdapter {
    	
    	private String mCategory;
    	private List<ResolveInfo> mApps;
    	
        public AppsAdapter(Context context, String category) {
        	mCategory = category;
        	loadApps();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
        	
        	ImageView i = new ImageView(OpenIntentsView.this);
            ResolveInfo info = mApps.get(position);

            i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
            i.setScaleType(ImageView.ScaleType.FIT_CENTER);
            i.setLayoutParams(new LinearLayout.LayoutParams(
            		LinearLayout.LayoutParams.WRAP_CONTENT,
            		LinearLayout.LayoutParams.WRAP_CONTENT));
        	/*
             ImageView i = new ImageView(OpenIntentsView.this);
            ResolveInfo info = mApps.get(position);

            i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
            i.setScaleType(ImageView.ScaleType.FIT_CENTER);
            i.setLayoutParams(new Gallery.LayoutParams(50, 50));
            * 
             * /
        	/*
        	ViewInflate v = getViewInflate();
        	
            View i = v.inflate(R.layout.main_imagetextbutton, parent, null);
            
            ResolveInfo info = mApps.get(position);

            //i.setImageDrawable(info.activityInfo.loadIcon(getPackageManager()));
            //i.setScaleType(ImageView.ScaleType.FIT_CENTER);
            i.setLayoutParams(new Gallery.LayoutParams(50, 50));
            return i;
            * /
            
            TextView tv = new TextView(OpenIntentsView.this);
            tv.setLayoutParams(new LinearLayout.LayoutParams(
            		LinearLayout.LayoutParams.WRAP_CONTENT,
            		LinearLayout.LayoutParams.WRAP_CONTENT));
            tv.setText(info.activityInfo.loadLabel(getPackageManager()));
            tv.setTextColor(Color.BLACK);
            
            LinearLayout ll = new LinearLayout(OpenIntentsView.this);
            ll.setLayoutParams(new GridView.LayoutParams(80, 80));
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.addView(i);
            ll.addView(tv);
            ll.setBackground(android.R.drawable.button_background);
            ll.setFocusable(true);
            ll.setClickable(true);
            
            
        	
            //parent.setBackground(android.R.drawable.button_background);
            //convertView.setBackground(android.R.drawable.button_background);
            //parent.getChildAt(0).setBackground(android.R.drawable.button_background);
            return ll;
        }


        public final int getCount() {
            return mApps.size();
        }

        public final Object getItem(int position) {
            return mApps.get(position);
        }

        public final long getItemId(int position) {
            return position;
        }
        

        private void loadApps() {
            Intent mainIntent = new Intent(Intent.MAIN_ACTION, null);
            mainIntent.addCategory(mCategory);

            mApps = getPackageManager().queryIntentActivities(mainIntent, 0);
        }
    }
    
*/
}
