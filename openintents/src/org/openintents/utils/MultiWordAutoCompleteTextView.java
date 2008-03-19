package org.openintents.utils;

import java.util.Map;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;

public class MultiWordAutoCompleteTextView extends AutoCompleteTextView {
	private static final String DEFAULT_SEPARATOR = ",";
	private String mSeparator = DEFAULT_SEPARATOR;

	public MultiWordAutoCompleteTextView(Context context) {
		super(context);
	}

	@SuppressWarnings("unchecked")
	public MultiWordAutoCompleteTextView(Context context, AttributeSet attrs,
			Map inflateParams) {
		super(context, attrs, inflateParams);
	}

	@SuppressWarnings("unchecked")
	public MultiWordAutoCompleteTextView(Context context, AttributeSet attrs,
			Map inflateParams, int defStyle) {
		super(context, attrs, inflateParams, defStyle);
	}

	/**
	 * Gets the separator used to delimit multiple words. Defaults to "," if
	 * never specified.
	 */
	public String getSeparator() {
		return mSeparator;
	}

	/**
	 * Sets the separator used to delimit multiple words. Defaults to "," if
	 * never specified.
	 * 
	 * @param separator
	 */
	public void setSeparator(String separator) {
		mSeparator = separator;
	}

	@Override
	protected void performFiltering(CharSequence text, int keyCode) {
		String newText = text.toString();
		if (newText.indexOf(mSeparator) != -1) {
			int lastIndex = newText.lastIndexOf(mSeparator);
			if (lastIndex != newText.length() - 1) {
				newText = newText.substring(lastIndex + 1).trim();
				if (newText.length() >= getThreshold()) {
					text = newText;
				}
			}
		}
		super.performFiltering(text, keyCode);
	}

	@Override
	protected void replaceText(CharSequence text) {
		String newText = getText().toString();
		if (newText.indexOf(mSeparator) != -1) {
			int lastIndex = newText.lastIndexOf(mSeparator);
			newText = newText.substring(0, lastIndex + 1) + text.toString();
		} else {
			newText = text.toString();
		}
		super.replaceText(newText);
	}

}
