package org.openintents.historify.ui.views;


import java.util.ArrayList;
import java.util.List;

import org.openintents.historify.R;

import android.app.Service;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView.ScaleType;

/**
 * 
 * ActionBar implementation for devices prior to 3.0
 * 
 * @author berke.andras
 *
 */
public class ActionBar {

	public static class Action {
		public final View.OnClickListener onClickListener;
		public final int iconResId;
		
		public Action(int iconResId, View.OnClickListener onClickListener) {
			this.iconResId = iconResId;
			this.onClickListener = onClickListener;
		}
	}
	
	private Context mContext;
	private ViewGroup mContentView;
	private String mTitle;
	private List<Action> mActions;
	
	public ActionBar(ViewGroup contentView, Integer titleResId) {
		
		mContext = contentView.getContext();
		mContentView = contentView;
		mTitle = titleResId == null ? null : mContext.getString(titleResId);
		mActions = new ArrayList<Action>();
		
	}

	public void add(Action action) {
		mActions.add(action);
	}
	
	public void setup() {
		
		if(mTitle==null) {
			//no title means we have to display the logo
			addImage(R.drawable.actionbar_logo);
		} else {
			//display short logo and title
			addImage(R.drawable.actionbar_logo_short);
			addTitleAndSpacing();
			for(Action a : mActions)
				addAction(a);
		}
		
	}

	private void addAction(final Action action) {
		
		addSeparator();
		
		 // Create the button
        ImageButton actionButton = (ImageButton) inflate(R.layout.actionbar_button);
        actionButton.setImageResource(action.iconResId);
        actionButton.setScaleType(ImageView.ScaleType.CENTER);
        actionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                action.onClickListener.onClick(view);
            }
        });
        
        mContentView.addView(actionButton);
        
	}

	private void addImage(int imageResId) {
		
		ImageView logo = new ImageView(mContext);
		logo.setImageResource(imageResId);
		logo.setClickable(false);
		logo.setScaleType(ScaleType.CENTER);
        mContentView.addView(logo);

	}
	
	private void addSeparator() {
		ImageView separator = new ImageView(mContext);
        separator.setLayoutParams(
                new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.FILL_PARENT));
        separator.setBackgroundResource(R.drawable.actionbar_separator);
        mContentView.addView(separator);
	}
	
	private void addTitleAndSpacing() {
		
        LinearLayout.LayoutParams spacing = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.FILL_PARENT);
        spacing.weight = 1;

		TextView title = (TextView) inflate(R.layout.actionbar_title);
		title.setText(mTitle);
		title.setLayoutParams(spacing);
		
		mContentView.addView(title);
	}

	private View inflate(int resId) {
		return ((LayoutInflater)mContext.getSystemService(Service.LAYOUT_INFLATER_SERVICE)).inflate(resId, null);
	}
}
