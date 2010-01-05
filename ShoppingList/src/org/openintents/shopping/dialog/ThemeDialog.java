package org.openintents.shopping.dialog;

import java.util.List;

import org.openintents.shopping.R;
import org.openintents.util.ThemeUtils;
import org.openintents.util.ThemeUtils.ThemeInfo;

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
import android.view.ViewGroup.LayoutParams;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class ThemeDialog extends AlertDialog implements OnClickListener, OnCancelListener, OnCheckedChangeListener {
	private static final String TAG = "ThemeDialog";
	
	private static final String BUNDLE_THEME = "theme";
	
	Context mContext;
	RadioGroup mRadioGroup;
	ThemeDialogListener mListener;
	ScrollView mScrollView;
	
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
		
		mScrollView = (ScrollView) view
				.findViewById(R.id.scrollview);
		
		fillThemes();
		
		setIcon(android.R.drawable.ic_menu_manage);
		setTitle(R.string.theme_pick);
		
		setButton(Dialog.BUTTON_POSITIVE, mContext.getText(R.string.ok), this);
		setButton(Dialog.BUTTON_NEGATIVE, mContext.getText(R.string.cancel), this);
		setOnCancelListener(this);
		
		updateList();
	}
	
	public void fillThemes() {
		List<ThemeInfo> listinfo = ThemeUtils.getThemeInfos(mContext, ThemeUtils.SHOPPING_LIST_THEME);
		
		//mRadioGroup.removeAllViews();
		
		int i = 0;
		for (ThemeInfo ti : listinfo) {
			RadioButton rb = new RadioButton(mContext);
			rb.setText(ti.title);
			LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			rb.setTag(ti);
			rb.setId(i);
			i++;
			mRadioGroup.addView(rb, lp);
		}
	}
	
	public void updateList() {

		String theme = mListener.onLoadTheme();
		
		// Check special cases:
		if (theme.equals("1")) {
			mRadioGroup.check(R.id.radio1);
			return;
		} else if (theme.equals("2")) {
			mRadioGroup.check(R.id.radio2);
			return;
		} else if (theme.equals("3")) {
			mRadioGroup.check(R.id.radio3);
			return;
		}
		
		int max = mRadioGroup.getChildCount();
		
		for (int i = 0; i < max; i++) {
			RadioButton rb = (RadioButton) mRadioGroup.getChildAt(i);
			ThemeInfo ti = (ThemeInfo) rb.getTag();
			if (ti != null && ti.styleName.equals(theme)) {
				mRadioGroup.check(rb.getId());
				
				// Scroll to new position
				// (Does not work, because a layout pass
				//  is probably still missing...)
				mScrollView.scrollTo(0, rb.getTop());
				break;
			}
		}
	}
	
	@Override
	public Bundle onSaveInstanceState() {
		Log.d(TAG, "onSaveInstanceState");
		
		Bundle b = super.onSaveInstanceState();
		String theme = getSelectedTheme();
		b.putString(BUNDLE_THEME, theme);
		return b;
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);

		Log.d(TAG, "onRestore");
		
		String theme = getSelectedTheme();
		
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(BUNDLE_THEME)) {
				theme = savedInstanceState.getString(BUNDLE_THEME);

				Log.d(TAG, "onRestore theme " + theme);
			}
		}
		
		mListener.onSetTheme(theme);
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
		String theme = getSelectedTheme();
		mListener.onSaveTheme(theme);
		mListener.onSetTheme(theme);
	}

	private String getSelectedTheme() {
		int r = mRadioGroup.getCheckedRadioButtonId();
		
		// Check special cases first
		if (r == R.id.radio1) {
			return "1";
		} else if (r == R.id.radio2) {
			return "2";
		} else if (r == R.id.radio3) {
			return "3";
		}
		
		// Now generic case from remote packages
		RadioButton rb = (RadioButton) mRadioGroup.findViewById(r);
		
		if (rb != null) {
			ThemeInfo ti = (ThemeInfo) rb.getTag();
			
			return ti.styleName;
		} else {
			return null;
		}
	}
	
	public void pressCancel() {
		/* User clicked No so do some stuff */
		String theme = mListener.onLoadTheme();
		mListener.onSetTheme(theme);
	}

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		
		// Backward compatibility:
		if (checkedId == R.id.radio1) {
			mListener.onSetTheme("1");
			return;
		} else if (checkedId == R.id.radio2) {
			mListener.onSetTheme("2");
			return;
		} else if (checkedId == R.id.radio3) {
			mListener.onSetTheme("3");
			return;
		}
		
		// Generic case:
		
		RadioButton rb = (RadioButton) group.findViewById(checkedId);
		
		if (rb != null) {
			ThemeInfo ti = (ThemeInfo) rb.getTag();
	
			mListener.onSetTheme(ti.styleName);
		}
	}
	
	public interface ThemeDialogListener {
		void onSetTheme(String theme);
		String onLoadTheme();
		void onSaveTheme(String theme);
	}

}
