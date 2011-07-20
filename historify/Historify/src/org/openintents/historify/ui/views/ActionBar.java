package org.openintents.historify.ui.views;


import org.openintents.historify.R;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * 
 * ActionBar implementation for devices prior to 3.0
 * 
 * @author berke.andras
 *
 */
public class ActionBar {

	private Context mContext;
	private ViewGroup mContentView;
	private String mTitle;
	
	public ActionBar(ViewGroup contentView, Integer titleResId) {
		
		mContext = contentView.getContext();
		mContentView = contentView;
		mTitle = titleResId == null ? null : mContext.getString(titleResId);
	}

	public void setup() {
		
		if(mTitle==null) {
			//no title means we have to display the logo
			addLogo();
		}
		
	}
	
	private void addLogo() {
		
		ImageView logo = new ImageView(mContext);
		logo.setImageResource(R.drawable.actionbar_logo);
		logo.setClickable(false);
		logo.setScaleType(ScaleType.CENTER);
        mContentView.addView(logo);

	}

	
	
}
