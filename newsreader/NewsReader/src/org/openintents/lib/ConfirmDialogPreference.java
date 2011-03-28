package org.openintents.lib;

import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

public class ConfirmDialogPreference extends DialogPreference {
	private Context mContext = null;
	
	public ConfirmDialogPreference(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mContext = context;
	}
	
    public ConfirmDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
    }
    
    public Context getContext() {
    	return mContext;
    }
    
	@Override
	public void onClick(DialogInterface dialog, int which) {
		// TODO Auto-generated method stub
		super.onClick(dialog, which);
		if (which == DialogInterface.BUTTON1) {
			mOnConfirmDialogPreferenceListener.onConfirmDialogPreference();
		}
	}
	
	OnConfirmDialogPreferenceListener mOnConfirmDialogPreferenceListener = null;
	
	public void setOnConfirmDialogPreferenceListener(OnConfirmDialogPreferenceListener listener) {
		mOnConfirmDialogPreferenceListener = listener;
	}

	public interface OnConfirmDialogPreferenceListener {
		public void onConfirmDialogPreference();
	}

}
