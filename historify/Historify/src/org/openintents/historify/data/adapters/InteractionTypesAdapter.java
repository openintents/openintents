package org.openintents.historify.data.adapters;

import org.openintents.historify.R;
import org.openintents.historify.data.loaders.InteractionTypeLoader;
import org.openintents.historify.data.loaders.SourceIconHelper;
import org.openintents.historify.data.model.source.DefaultInteractionType;
import org.openintents.historify.data.model.source.InteractionType;
import org.openintents.historify.uri.ContentUris;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class InteractionTypesAdapter extends BaseAdapter {

	protected Activity mContext;

	private InteractionTypeLoader mLoader;
	protected SourceIconHelper mSourceIconHelper;
	private Cursor mCursor;

	private SourcesChangedObserver mObserver;
	
	private DefaultInteractionType mDefaultInteractionType;
	private View mEmptyHintView;
	
	private class SourcesChangedObserver extends ContentObserver {

		public SourcesChangedObserver(Handler handler) {
			super(handler);
		}
		
		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}
		
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			refresh();
		}
	}

	public InteractionTypesAdapter(Activity context, View emptyHintView) {

		mContext = context;
		mLoader = new InteractionTypeLoader();
		mSourceIconHelper = new SourceIconHelper();
		mDefaultInteractionType = new DefaultInteractionType(context);
		mEmptyHintView = emptyHintView;
		load();
	}

	/** Open cursor. */
	public void load() {

		doLoad();
		
		mObserver = new SourcesChangedObserver(new Handler()); 
		mContext
		 .getContentResolver()
		 .registerContentObserver(ContentUris.Sources, true, mObserver);
		refreshHintVisibity();
	}
	
	private void doLoad() {
		mCursor = mLoader.openManagedCursor(mContext);
		notifyDataSetChanged();
	}
	
	public void refresh() {
		
		if(mCursor!=null) {
			mCursor.requery();
			notifyDataSetChanged();
			refreshHintVisibity();
		}
	}


	private void refreshHintVisibity() {
		mEmptyHintView.setVisibility(getCount()>1 ? View.GONE : View.VISIBLE);
	}

	public int getCount() {
		return (mCursor == null ? 0 : mCursor.getCount())+1;
	}

	public InteractionType getItem(int position) {
		if(position==0)
			return mDefaultInteractionType;
		else {
			return (InteractionType) (mCursor == null ? null : mLoader.loadFromCursor(mCursor,
					position-1));	
		}
		
	}

	public long getItemId(int position) {
		return -1;
	}

	public View getView(int position, View convertView, ViewGroup parent) {

		InteractionType item = getItem(position);

		if(convertView==null) {
			convertView = ((LayoutInflater)parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.listitem_interact, null);
		}
		
		ImageView iv = (ImageView)convertView.findViewById(R.id.interact_listitem_imgIcon);
		mSourceIconHelper.toImageView(mContext, item.getEventIcon(), iv);
		
		TextView tv = (TextView)convertView.findViewById(R.id.interact_listitem_txtName);
		tv.setText(item.getActionTitle());
		
		return convertView;

	}
	

}