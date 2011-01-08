/* 
 * Copyright (C) 2008 OpenIntents.org
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

import org.openintents.distribution.AboutDialog;
import org.openintents.intents.ColorPickerIntents;
import org.openintents.widget.ColorCircle;
import org.openintents.widget.ColorSlider;
import org.openintents.widget.OnColorChangedListener;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ColorPickerActivity extends Activity 
	implements OnColorChangedListener {
	
	ColorCircle mColorCircle;
	ColorSlider mSaturation;
	ColorSlider mValue;
	
	Intent mIntent;
	

	private static final int DIALOG_ABOUT = 1;

	private static final int MENU_ABOUT = Menu.FIRST;
	private static final int MENU_RECENT_COLORS = MENU_ABOUT + 1;
	
	private static final int REQUEST_CODE_RECENT_COLOR = 1;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.colorpicker);

        // Get original color
        mIntent = getIntent();
        if (mIntent == null) {
        	mIntent = new Intent();
        }
        

        int color;
        final ColorPickerState state = (ColorPickerState) getLastNonConfigurationInstance();
        if (state != null) {
        	color = state.mColor;
        } else {
        	color = mIntent.getIntExtra(ColorPickerIntents.EXTRA_COLOR, Color.BLACK);
        }

        initializeColor(color);
	}
	
	void initializeColor(int color) {

        mColorCircle = (ColorCircle) findViewById(R.id.colorcircle);
        mColorCircle.setOnColorChangedListener(this);
        mColorCircle.setColor(color);

        mSaturation = (ColorSlider) findViewById(R.id.saturation);
        mSaturation.setOnColorChangedListener(this);
        mSaturation.setColors(color, Color.BLACK);

        mValue = (ColorSlider) findViewById(R.id.value);
        mValue.setOnColorChangedListener(this);
        mValue.setColors(Color.WHITE, color);
	}
		
	
	
    class ColorPickerState {
    	int mColor;
    }

    @Override
    public Object onRetainNonConfigurationInstance() {
    	ColorPickerState state = new ColorPickerState();
    	state.mColor = this.mColorCircle.getColor();
        return state;
    }
	
	

	public int toGray(int color) {
		int a = Color.alpha(color);
		int r = Color.red(color);
		int g = Color.green(color);
		int b = Color.blue(color);
		int gray = (r + g + b) / 3;
		return Color.argb(a, gray, gray, gray);
	}
	
	
	public void onColorChanged(View view, int newColor) {
		if (view == mColorCircle) {
			mValue.setColors(0xFFFFFFFF, newColor);
	        mSaturation.setColors(newColor, 0xff000000);
		} else if (view == mSaturation) {
			mColorCircle.setColor(newColor);
			mValue.setColors(0xFFFFFFFF, newColor);
		} else if (view == mValue) {
			mColorCircle.setColor(newColor);
		}
		
	}

	
	public void onColorPicked(View view, int newColor) {
		// We can return result
		mIntent.putExtra(ColorPickerIntents.EXTRA_COLOR, newColor);

	    SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		RecentColorsActivity.addRecentColor(settings, newColor);

		setResult(RESULT_OK, mIntent);
		finish();
	}
	
	/////////////////////////////////////////////////////
	// Menu
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_RECENT_COLORS, 0, R.string.recent_colors)
		  .setTitleCondensed(getResources().getString(R.string.recent_colors_condensed))
		  .setIcon(android.R.drawable.ic_menu_recent_history).setShortcut('0', 'r');

		
		menu.add(0, MENU_ABOUT, 0, R.string.about)
		  .setIcon(android.R.drawable.ic_menu_info_details) .setShortcut('1', 'a');

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
		case MENU_RECENT_COLORS:
			this.startActivityForResult(new Intent(this, RecentColorsActivity.class), REQUEST_CODE_RECENT_COLOR);
			return true;
		case MENU_ABOUT:
			showAboutBox();
			return true;
		}
		return super.onOptionsItemSelected(item);

	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case DIALOG_ABOUT:
			return new AboutDialog(this);
		}
		return null;
	}
	
	private void showAboutBox() {
		AboutDialog.showDialogOrStartActivity(this, DIALOG_ABOUT);
	}
	
	

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode != RESULT_OK)
			return;

  	   	switch (requestCode) {
		case REQUEST_CODE_RECENT_COLOR:
		{
			int color = data.getIntExtra(ColorPickerIntents.EXTRA_COLOR, Color.BLACK);
			initializeColor(color);
			break;
		}
   		default:
	    	break;
	   }
    }
}
