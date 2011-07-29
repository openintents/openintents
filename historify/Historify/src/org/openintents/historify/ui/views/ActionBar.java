package org.openintents.historify.ui.views;


import java.util.ArrayList;
import java.util.List;

import org.openintents.historify.R;
import org.openintents.historify.ui.views.popup.ActionBarDropDownMenu;
import org.openintents.historify.ui.views.popup.ActionBarDropDownMenu.MenuModel;

import android.app.Service;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
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
		public final String title;
		public final int iconResId;
		
		public Action(int iconResId, View.OnClickListener onClickListener) {
			this.iconResId = iconResId;
			this.title = "";
			this.onClickListener = onClickListener;
		}

		public Action(String title, OnClickListener onClickListener) {
			this.iconResId = 0;
			this.title = title;
			this.onClickListener = onClickListener;
		}
		
		@Override
		public String toString() {
			return title;
		}
	}
	
	private Context mContext;
	private String mTitle;
	private List<Action> mActions;
	
	private ViewGroup mContentView;
	private ImageView mLogo;
	
	public ActionBar(ViewGroup contentView, int titleResId) {
		init(contentView, contentView.getContext().getString(titleResId));		
	}

	public ActionBar(ViewGroup contentView, String title) {
		init(contentView, title);
	}

	private void init(ViewGroup contentView, String title) {
		mContext = contentView.getContext();
		mContentView = contentView;
		mTitle = title;
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
		
		mLogo = new ImageView(mContext);
		mLogo.setImageResource(imageResId);
		mLogo.setClickable(false);
		mLogo.setScaleType(ScaleType.CENTER);
        mContentView.addView(mLogo);

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

	public View getHSymbol() {
		return mLogo;
	}

	public void setHSymbolClickable(final MenuModel menu) {
		
		if(menu==null) {
			mLogo.setOnClickListener(null);
			mLogo.setImageResource(R.drawable.actionbar_logo_short);
		} else {
			mLogo.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					ActionBarDropDownMenu dropDownMenu =
						new ActionBarDropDownMenu(mContext);
					dropDownMenu.setMenu(menu);
					dropDownMenu.show(mContentView);
				}
			});
			mLogo.setImageResource(R.drawable.actionbar_logo_short_clickable);
		}
	}

}
