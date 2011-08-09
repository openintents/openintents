/* 
 * Copyright (C) 2011 OpenIntents.org
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

package org.openintents.historify.ui.views;

import org.openintents.historify.R;
import org.openintents.historify.data.adapters.TimeLineAdapter;
import org.openintents.historify.data.loaders.ContactIconHelper;
import org.openintents.historify.data.model.Contact;
import org.openintents.historify.preferences.Pref;
import org.openintents.historify.preferences.PreferenceManager;
import org.openintents.historify.ui.views.popup.InteractPopupWindow;
import org.openintents.historify.ui.views.popup.MyAvatarPopupWindow;
import org.openintents.historify.ui.views.popup.TimeLineOptionsPopupWindow;
import org.openintents.historify.ui.views.popup.ToolTipPopupWindow;
import org.openintents.historify.ui.views.popup.ActionBarDropDownMenu.MenuModel;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.PopupWindow.OnDismissListener;

public class TimeLineTopPanel {
	
	private static class HMenuBuilder {
		public MenuModel build(final TimeLineTopPanel panel) {
			return new MenuModel(panel.getContext())
			.add(R.string.timeline_hmenu_filter, new OnClickListener() {
				public void onClick(View v) {
					panel.onTimeLineOptionsSelected();
				}
			})
			.add(R.string.timeline_hmenu_interact, new OnClickListener() {
				public void onClick(View v) {
					panel.onInteractSelected();
				}
			})
			.add(R.string.timeline_hmenu_restore, new OnClickListener() {
				public void onClick(View v) {
					panel.onShow();
				}
			});
		}
	}
	
	private Contact mContact;
	
	private Context mContext;
	private ViewGroup mContentView;
	
	private TextView mTxtUser; 
	private ImageView mImgUserIcon;
	private TextView mTxtContact;
	private ImageView mImgContactIcon;
	
	private Button mBtnOptions;

	private ActionBar mActionBar;
	private ListView mLstTimeLine;
	
	private boolean mVisible;
	
	public TimeLineTopPanel(ViewGroup contentView) {
		
		mContentView = contentView;
		mContext = contentView.getContext();
		
		mTxtUser = (TextView)contentView.findViewById(R.id.timeline_txtUser);
		mImgUserIcon = (ImageView)contentView.findViewById(R.id.timeline_imgUserIcon);
		mTxtContact = (TextView) contentView.findViewById(R.id.timeline_txtContact);
		mImgContactIcon = (ImageView) contentView.findViewById(R.id.timeline_imgContactIcon);
		
		mBtnOptions = (Button)contentView.findViewById(R.id.timeline_btnOptions);
		mBtnOptions.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onTimeLineOptionsSelected();
			}
		});
		
		mImgUserIcon.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new MyAvatarPopupWindow(mContext).show(mTxtUser);
			}
		});
		
		mImgContactIcon.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				onInteractSelected();
			}
		});

	}

	public void setActionBar(ActionBar actionBar) {
		mActionBar = actionBar;
	}
	
	public void init(Contact contact, ListView lstTimeLine) {

		mLstTimeLine = lstTimeLine;
		
		mContact = contact;
		
		if(mContact!=null) {
			
			mTxtContact.setText(mContact.getGivenName());
			
			Drawable icon = ContactIconHelper.getIconDrawable(mContext, mContact.getLookupKey());
			if(icon==null) mImgContactIcon.setImageResource(R.drawable.contact_default_large);
			else mImgContactIcon.setImageDrawable(icon);
			
		} else {
			mTxtContact.setText("");
		}	
		
		loadUserIcon();
		
		mVisible = PreferenceManager.getInstance(mContext).getBooleanPreference(Pref.TIMELINE_TOP_PANEL_VISIBILITY, Pref.DEF_TIMELINE_TOP_PANEL_VISIBILITY);
		if(!mVisible) onHide(false);
	}
	
	private void onTimeLineOptionsSelected() {
		
		TimeLineOptionsPopupWindow popupWindow =
			new TimeLineOptionsPopupWindow(this, (TimeLineAdapter) mLstTimeLine.getAdapter());
		popupWindow.initView(mVisible);
		
		if(mVisible) {
			popupWindow.show(mBtnOptions);	
		} else {
			popupWindow.setArrowGravity(Gravity.LEFT);
			popupWindow.show(mActionBar.getHSymbol());
		}
		
		
	}

	protected void onInteractSelected() {

		InteractPopupWindow popupWindow = new InteractPopupWindow(mContext, mContact.getLookupKey());
		
		if(mVisible) {
			popupWindow.show(mTxtContact);
		} else {
			popupWindow.setArrowGravity(Gravity.LEFT);
			popupWindow.show(mActionBar.getHSymbol());
		}
		
	}

	
	public Context getContext() {
		return mContext;
	}

	public void onHide() {
		boolean needToShowToolTip = PreferenceManager.getInstance(mContext).getBooleanPreference(Pref.TOOLTIP_RESTORE_TOP_PANEL_VISIBILITY, Pref.DEF_TOOLTIP_VISIBILITY);
		onHide(needToShowToolTip);
		onUserChangedVisibility();
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
					PreferenceManager.getInstance(mContext).setPreference(Pref.TOOLTIP_RESTORE_TOP_PANEL_VISIBILITY, needToShowInFuture);
				}
			});
			popupWindow.show(viewHSymbol);
		}
		
		//set the h! symbol clickable
		mActionBar.setHSymbolClickable(new HMenuBuilder().build(this));
	}

	public void onShow() {
		mContentView.setVisibility(View.VISIBLE);
		mActionBar.setHSymbolClickable(null);
		onUserChangedVisibility();
	}

	private void onUserChangedVisibility() {
		mVisible = mContentView.getVisibility() == View.VISIBLE;
		PreferenceManager.getInstance(mContext).setPreference(Pref.TIMELINE_TOP_PANEL_VISIBILITY, mVisible);
	}

	public Contact getContact() {
		return mContact;
	}

	public void loadUserIcon() {
		ContactIconHelper.loadMyAvatar(mContext, mImgUserIcon);
	}
	
}
