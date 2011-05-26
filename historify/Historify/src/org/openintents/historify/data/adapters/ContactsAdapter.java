package org.openintents.historify.data.adapters;

import org.openintents.historify.data.loaders.ContactLoader;
import org.openintents.historify.data.model.Contact;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ContactsAdapter extends BaseAdapter {

	private Activity mContext;
	
	private ContactLoader mLoader;
	private Cursor mCursor;
	
	private boolean mStarredOnly;

	public ContactsAdapter(Activity context, boolean starredOnly) {

		mContext = context;
		mStarredOnly = starredOnly;
		mLoader = new ContactLoader();
		
		load();
	}	

	public void load() {
		
        mCursor = mLoader.openCursor(mContext, mStarredOnly); 
        notifyDataSetChanged();
	}

	public int getCount() {
		return mCursor == null ? 0 : mCursor.getCount();
	}

	public Contact getItem(int position) {
		return mCursor == null ? null : mLoader.loadFromCursor(mCursor, position);
	}
	
	public long getItemId(int position) {
		return -1;
	}
	
	public View getView(int position, View convertView, ViewGroup parent) {

		Contact contact = getItem(position);

		if (convertView == null) {
			convertView = ((LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE))
					.inflate(android.R.layout.two_line_list_item, null);
		}

		TextView txtName = (TextView) convertView.findViewById(android.R.id.text1);
		txtName.setText(contact.getName());

		return convertView;
	}
}
