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

package org.openintents.flashlight;

import org.openintents.widget.ColorCircle;
import org.openintents.widget.ColorSlider;
import org.openintents.widget.OnColorChangedListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ColorPickerActivity extends Activity 
	implements OnColorChangedListener {
	
	public final static String INTENT_PICK_COLOR = "org.openintents.action.PICK_COLOR";
	public final static String EXTRA_COLOR = "org.openintents.extra.color";

	ColorCircle mColorCircle;
	ColorSlider mSaturation;
	
	Intent mIntent;
	
	int mColor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.colorpicker);

        // Get original color
        mIntent = getIntent();
        if (mIntent == null) {
        	mIntent = new Intent();
        }
        
        mColor = mIntent.getIntExtra(EXTRA_COLOR, 0);
        
        mColorCircle = (ColorCircle) findViewById(R.id.colorcircle);
        
        mColorCircle.setOnColorChangedListener(this);
        mColorCircle.setColor(mColor);
        
        mSaturation = (ColorSlider) findViewById(R.id.saturation);
        
        mSaturation.setOnColorChangedListener(this);
		mSaturation.setColors(0xFFFFFFFF, mColor);
        
	}

	@Override
	public void onColorChanged(View view, int newColor) {
		if (view == mColorCircle) {
			mSaturation.setColors(0xFFFFFFFF, newColor);
		} else if (view == mSaturation) {
			mColorCircle.setColor(newColor);
		}
		
	}

	@Override
	public void onColorPicked(View view, int newColor) {
		// We can return result
		mIntent.putExtra(EXTRA_COLOR, newColor);

		setResult(RESULT_OK, mIntent);
		finish();
	}
	
}
