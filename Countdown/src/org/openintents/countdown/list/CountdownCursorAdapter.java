package org.openintents.countdown.list;

import org.openintents.countdown.db.Countdown;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

public class CountdownCursorAdapter extends CursorAdapter {


	Context mContext;
	OnCountdownClickListener mListener;
	
	public CountdownCursorAdapter(Context context, Cursor c, OnCountdownClickListener listener) {
		super(context, c);
		mContext = context;
		mListener = listener;
	}
	
	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		CountdownListItemView cliv = (CountdownListItemView) view;
		

		long id = cursor.getLong(cursor
				.getColumnIndexOrThrow(Countdown.Durations._ID));
		String title = cursor.getString(cursor
				.getColumnIndexOrThrow(Countdown.Durations.TITLE));
		long duration = cursor.getLong(cursor
				.getColumnIndexOrThrow(Countdown.Durations.DURATION));
		long deadline = cursor.getLong(cursor
				.getColumnIndexOrThrow(Countdown.Durations.DEADLINE_DATE));
		
		if (TextUtils.isEmpty(title)) {
			title = context.getString(android.R.string.untitled);
		}
		
		cliv.setTitle(title);
		cliv.setDuration(duration);
		cliv.setDeadline(deadline);
		
		cliv.setListeners(mListener, id);
		

	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		return new CountdownListItemView(context);
	}
	
	public interface OnCountdownClickListener {
		public void onCountdownPanelClick(long id);
		
		public void onStartClick(long id);
	}

}
