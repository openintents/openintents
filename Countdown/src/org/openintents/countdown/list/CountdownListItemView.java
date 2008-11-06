package org.openintents.countdown.list;

import org.openintents.countdown.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CountdownListItemView extends LinearLayout {

	Context mContext;
	
	private long mDuration;
	private long mDeadline;
	private TextView mTitle;
	private TextView mDurationView;
	private TextView mCountdownView;
	
	public CountdownListItemView(Context context) {
		super(context);
		mContext = context;

		// inflate rating
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(
				R.layout.countdownlist_item, this, true);
		
		mTitle = (TextView) findViewById(R.id.text);
		mDurationView = (TextView) findViewById(R.id.duration);
		mCountdownView = (TextView) findViewById(R.id.countdown);
	}

	/**
	 * Convenience method to set the title of a NewsView
	 */
	public void setTitle(String title) {
		mTitle.setText(title);
	}

	public void setDuration(long duration) {
		mDuration = duration;
		updateCountdown();
	}
	
	public void setDeadline(long deadline) {
		mDeadline = deadline;
		updateCountdown();
	}
	
	public void updateCountdown() {
		long now = System.currentTimeMillis();
		
		TypedArray a;
		
		long delta = mDeadline - now;
		
		mDurationView.setText("" + getDurationString(mDuration));
		
		if (delta > 0) {
			//mDurationView.setText("");
			mCountdownView.setText("" + getDurationString(delta));
			mCountdownView.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
			//mDurationView.setTextColor(0xffff00ff);
			//mDurationView.setTextSize(24);
		} else if (delta > -3000) {
			//mDurationView.setText("" + getDurationString(mDuration));
			mCountdownView.setText("" + getDurationString(0));
			//mDurationView.setTextAppearance(mContext, android.R.style.TextAppearance_Large);
			mCountdownView.setTextColor(0xffff0000);
			//mDurationView.setTextSize(24);
		} else {
			//mDurationView.setText("" + getDurationString(mDuration));
			mCountdownView.setText("");
			//mDurationView.setTextAppearance(mContext, android.R.style.TextAppearance_Small);
			//mDurationView.setTextSize(24);
		}
		requestLayout();
		invalidate();
	}
	
	public static String getDurationString(long duration) {
		int seconds = (int) (duration / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;
		int hours = minutes / 60;
		minutes = minutes % 60;
		
		return "" + hours + ":" 
			+ (minutes < 10 ? "0" : "")
			+ minutes + ":"
			+ (seconds < 10 ? "0" : "")
			+ seconds;
	}

}
