package org.openintents.historify.ui.views.popup;

import org.openintents.historify.R;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

public class ToolTipPopupWindow extends AbstractPopupWindow{

	private TextView mTxtMessage;
	private CheckBox mChkShowAgain;
	
	public ToolTipPopupWindow(Context context, int msgResId) {
		super(context);
		
		mTxtMessage.setText(msgResId);
		
		setArrowGravity(Gravity.LEFT);
	}

	@Override
	protected void addContent(ViewGroup contentRoot) {
		
		ViewGroup contentView = (ViewGroup) ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.popupwindow_tooltip, contentRoot);
		mTxtMessage = (TextView)contentView.findViewById(R.id.tooltip_txtMessage);
		mChkShowAgain = (CheckBox) contentView.findViewById(R.id.tooltip_chkShowAgain);
	}

	public boolean needToShowInFuture() {
		return mChkShowAgain.isChecked();
	}

	
}
