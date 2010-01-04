package org.openintents.shopping.dialog;

import org.openintents.shopping.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ThemeDialog extends AlertDialog implements OnClickListener, OnCancelListener, OnCheckedChangeListener {
	private static final String TAG = "ThemeDialog";
	
	private static final String BUNDLE_THEME = "theme";
	
	Context mContext;
	RadioGroup mRadioGroup;
	ThemeDialogListener mListener;
	
	public ThemeDialog(Context context) {
		super(context);
		mContext = context;
		init();
	}
	
	public ThemeDialog(Context context, ThemeDialogListener listener) {
		super(context);
		mContext = context;
		mListener = listener;
		init();
	}

	private void init() {

		LayoutInflater inflate = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View view = inflate.inflate(R.layout.shopping_theme_settings,
				null);
		setView(view);

		mRadioGroup = (RadioGroup) view
				.findViewById(R.id.radiogroup);
		mRadioGroup.setOnCheckedChangeListener(this);
		
		setIcon(android.R.drawable.ic_menu_manage);
		setTitle(R.string.theme_pick);
		
		setButton(Dialog.BUTTON_POSITIVE, mContext.getText(R.string.ok), this);
		setButton(Dialog.BUTTON_NEGATIVE, mContext.getText(R.string.cancel), this);
		setOnCancelListener(this);
		
		updateList();
	}
	
	public void updateList() {

		int themeId = mListener.onLoadTheme();
		switch (themeId) {
		case 1:
			mRadioGroup.check(R.id.radio1);
			break;
		case 2:
			mRadioGroup.check(R.id.radio2);
			break;
		case 3:
			mRadioGroup.check(R.id.radio3);
			break;
		}
	}
	
	@Override
	public Bundle onSaveInstanceState() {
		Log.d(TAG, "onSaveInstanceState");
		
		Bundle b = super.onSaveInstanceState();
		int theme = getSelectedTheme();
		b.putInt(BUNDLE_THEME, theme);
		return b;
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		Log.d(TAG, "onRestore");
		
		int theme = getSelectedTheme();
		
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(BUNDLE_THEME)) {
				theme = savedInstanceState.getInt(BUNDLE_THEME);

				Log.d(TAG, "onRestore theme " + theme);
			}
		}
		
		mListener.onSetTheme(theme);
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	public void onClick(DialogInterface dialog, int which) {
    	if (which == BUTTON_POSITIVE) {
    		pressOk();
    	} else if (which == BUTTON_NEGATIVE) {
    		pressCancel();
    	}
		
	}

	@Override
	public void onCancel(DialogInterface arg0) {
		pressCancel();
	}
	
	public void pressOk() {

		/* User clicked Yes so do some stuff */
		int themeId = getSelectedTheme();
		mListener.onSaveTheme(themeId);
		mListener.onSetTheme(themeId);
	}

	private int getSelectedTheme() {
		int r = mRadioGroup.getCheckedRadioButtonId();
		int themeId = 0;
		switch (r) {
		case R.id.radio1:
			themeId = 1;
			break;
		case R.id.radio2:
			themeId = 2;
			break;
		case R.id.radio3:
			themeId = 3;
			break;
		}
		return themeId;
	}
	
	public void pressCancel() {
		/* User clicked No so do some stuff */
		int themeId = mListener.onLoadTheme();
		mListener.onSetTheme(themeId);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.radio1:
			mListener.onSetTheme(1);
			break;
		case R.id.radio2:
			mListener.onSetTheme(2);
			break;
		case R.id.radio3:
			mListener.onSetTheme(3);
			break;
		case R.id.radio3 + 1:
			mListener.onSetTheme(4);
			break;

		}
	}
	
	public interface ThemeDialogListener {
		void onSetTheme(int theme);
		int onLoadTheme();
		void onSaveTheme(int theme);
	}

}
