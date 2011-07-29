package org.openintents.historify.ui.views;

import org.openintents.historify.R;
import org.openintents.historify.data.loaders.ContactIconHelper;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.preferences.PreferenceManager;
import org.openintents.historify.preferences.Prefs;
import org.openintents.historify.ui.views.popup.TimeLineOptionsPopupWindow;
import org.openintents.historify.ui.views.popup.ToolTipPopupWindow;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.PopupWindow.OnDismissListener;

public class TimeLineTopPanel {
	
	private Contact mContact;
	
	private Context mContext;
	private ViewGroup mContentView;
	private TextView mTxtContact;
	private ImageView mImgContactIcon;	
	private Button mBtnOptions;

	private ActionBar mActionBar;

	private boolean mVisible;
	
	public TimeLineTopPanel(ViewGroup contentView) {
		
		mContentView = contentView;
		mContext = contentView.getContext();
		
		mTxtContact = (TextView) contentView.findViewById(R.id.timeline_txtContact);
		mImgContactIcon = (ImageView) contentView.findViewById(R.id.timeline_imgContactIcon);
		
		mBtnOptions = (Button)contentView.findViewById(R.id.timeline_btnOptions);
		mBtnOptions.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onTimeLineOptionsSelected();
			}
		});

	}

	public void setActionBar(ActionBar actionBar) {
		mActionBar = actionBar;
	}
	
	public void init(Contact contact) {
		
		mContact = contact;
		
		if(mContact!=null) {
			
			mTxtContact.setText(mContact.getName());
			
			Drawable icon = ContactIconHelper.getIconDrawable(mContext, mContact.getLookupKey());
			if(icon==null) mImgContactIcon.setImageResource(R.drawable.contact_default_large);
			else mImgContactIcon.setImageDrawable(icon);
			
		} else {
			mTxtContact.setText("");
		}	
		
		mVisible = PreferenceManager.getInstance(mContext).getBooleanPreference(Prefs.TIMELINE_TOP_PANEL_VISIBILITY, Prefs.DEF_TIMELINE_TOP_PANEL_VISIBILITY);
		if(!mVisible) onHide(false);
	}
	
	private void onTimeLineOptionsSelected() {
		new TimeLineOptionsPopupWindow(this).show(mBtnOptions);
	}

	public Context getContext() {
		return mContext;
	}

	public void onHide() {
		boolean needToShowToolTip = PreferenceManager.getInstance(mContext).getBooleanPreference(Prefs.TOOLTIP_RESTORE_TOP_PANEL_VISIBILITY, Prefs.DEF_TOOLTIP_VISIBILITY);
		onHide(needToShowToolTip);
	}
	
	private void onHide(boolean displayTooltip) {
		
		mContentView.setVisibility(View.GONE);
		
		if(displayTooltip) {
			//show restore tooltip
			View viewHSymbol = mActionBar.getHSymbol();
			final ToolTipPopupWindow popupWindow = new ToolTipPopupWindow(mContext, R.string.timeline_msg_restore);
			popupWindow.setOnDismissListener(new OnDismissListener() {
				public void onDismiss() {					
					boolean needToShowInFuture = popupWindow.needToShowInFuture();
					PreferenceManager.getInstance(mContext).setPreference(Prefs.TOOLTIP_RESTORE_TOP_PANEL_VISIBILITY, needToShowInFuture);
				}
			});
			popupWindow.show(viewHSymbol);
		}
		
		//set the h! symbol clickable
		mActionBar.setHSymbolClickable(new View.OnClickListener() {
			public void onClick(View v) {
				onShow();
			}
		});
	}

	public void onShow() {
		mContentView.setVisibility(View.VISIBLE);
		mActionBar.setHSymbolClickable(null);
	}
	
}
