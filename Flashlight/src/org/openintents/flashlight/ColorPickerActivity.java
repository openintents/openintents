package org.openintents.flashlight;

import org.openintents.widget.ColorCircle;
import org.openintents.widget.ColorCircle.OnColorChangedListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class ColorPickerActivity extends Activity 
	implements OnColorChangedListener {
	
	public final static String INTENT_PICK_COLOR = "org.openintents.action.PICK_COLOR";
	public final static String EXTRA_COLOR = "org.openintents.extra.color";

	ColorCircle mColorCircle;
	
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
        
	}

	@Override
	public void onColorChanged(ColorCircle colorcircle, int newColor) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onColorPicked(ColorCircle colorcircle, int newColor) {
		// We can return result
		mIntent.putExtra(EXTRA_COLOR, newColor);

		setResult(RESULT_OK, mIntent);
		finish();
	}
	
}
