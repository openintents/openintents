package org.openintents.countdown.list;

import org.openintents.countdown.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CountdownListItemView extends LinearLayout {

	Context mContext;
	
	private long mDuration;
	private long mDeadline;
	private TextView mTitle;
	private TextView mDurationView;
	
	public CountdownListItemView(Context context) {
		super(context);
		mContext = context;

		// inflate rating
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		inflater.inflate(
				R.layout.countdownlist_item, this, true);
		
		mTitle = (TextView) findViewById(android.R.id.text1);
		mDurationView = (TextView) findViewById(android.R.id.text2);
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
		
		long delta = mDeadline - now;
		
		if (delta > 0) {
			mDurationView.setText("Countdown: " + getDurationString(delta));
		} else if (delta > -3000) {
			mDurationView.setText("Boom!" + getDurationString(0));
		} else {
			mDurationView.setText("Timer: " + getDurationString(mDuration));
		}
	}
	
	private String getDurationString(long duration) {
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
