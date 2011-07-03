package org.openintents.historify.data.aggregation;

import org.openintents.historify.data.model.source.AbstractSource;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

public class MergedContentObserver extends ContentObserver{

	private AbstractSource mSource;
	private Uri mUriToNotify;
	private Context mContext;
	
	public MergedContentObserver(Context context, AbstractSource source, Uri uriToNotify) {
		super(new Handler());
		mContext = context;
		mSource = source;
		mUriToNotify = uriToNotify;
	}

	@Override
	public boolean deliverSelfNotifications() {
		return true;
	}
	
	@Override
	public void onChange(boolean selfChange) {
		super.onChange(selfChange);
		Log.v(EventAggregator.N,"Contents of "+mSource.getName()+" have been changed.");
		mContext.getContentResolver().notifyChange(mUriToNotify, null);
		
	}
}
